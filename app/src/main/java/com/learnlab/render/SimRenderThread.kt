package com.learnlab.render

import android.os.Handler
import android.os.HandlerThread
import android.view.Choreographer
import android.view.Surface
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport

/**
 * Owns the Filament Engine and the render loop. Runs on its OWN thread with its
 * own Looper + Choreographer, fully decoupled from the Compose UI thread — a
 * Compose recomposition storm can never stall a frame, and a heavy physics step
 * can never stall touch handling.
 *
 * Lifecycle marshalling: the SurfaceView (UI thread) calls the public methods,
 * which post Runnables onto this thread's Handler. Every Filament call therefore
 * executes on this single thread — Filament's hard requirement.
 */
class SimRenderThread(
    private val scene: SimScene,
    private val quality: RenderQuality,
    private val relay: GestureRelay,
    private val postProgress: (Float) -> Unit,
    private val postError: (Throwable) -> Unit,
) {
    private val thread = HandlerThread("LearnLab-Render").apply { start() }
    private val handler = Handler(thread.looper)
    private val choreographer: Choreographer by lazy { Choreographer.getInstance() }

    // --- Filament handles (render thread only) ---
    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var filaScene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private var cameraEntity = 0
    private var swapChain: SwapChain? = null
    private var surface: Surface? = null

    private val cameraRig = CameraRig()
    private val input = SimInput()

    private var initialized = false
    private var sceneReady = false
    private var running = false
    private var destroyed = false

    private var lastFrameNanos = 0L
    private var width = 0
    private var height = 0
    private var progressTick = 0

    private val budgetNanos: Long
        get() = 1_000_000_000L / quality.current.targetFps

    // Single reused frame callback — re-posted every frame, never reallocated.
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running || destroyed) return
            // Re-arm first so a thrown exception below can't kill the loop silently.
            choreographer.postFrameCallback(this)
            renderFrame(frameTimeNanos)
        }
    }

    // ---- Public API (called from UI thread) ----

    fun onSurfaceAvailable(newSurface: Surface, w: Int, h: Int) = handler.post {
        if (destroyed) return@post
        try {
            ensureEngine()
            surface = newSurface
            width = w; height = h
            swapChain?.let { engine.destroySwapChain(it) }
            // LDR swapchain: no alpha/HDR buffer — saves a huge amount of memory
            // bandwidth on a fill-rate-bound entry GPU. Bandwidth is the bottleneck.
            swapChain = engine.createSwapChain(newSurface)
            view.viewport = Viewport(0, 0, w, h)
            cameraRig.setProjection(camera, w, h)
            ensureScene()
            scene.onResized(simContext(), w, h)
            applyQuality()
            startLoop()
        } catch (t: Throwable) {
            postError(t)
        }
    }

    fun onSurfaceResized(w: Int, h: Int) = handler.post {
        if (!initialized || destroyed) return@post
        width = w; height = h
        view.viewport = Viewport(0, 0, w, h)
        cameraRig.setProjection(camera, w, h)
        if (sceneReady) scene.onResized(simContext(), w, h)
    }

    fun onSurfaceDestroyed() = handler.post {
        stopLoop()
        swapChain?.let { engine.destroySwapChain(it) }
        swapChain = null
        surface = null
    }

    /** Full teardown. After this the thread is dead and the instance is spent. */
    fun shutdown() {
        handler.post {
            stopLoop()
            if (initialized) {
                if (sceneReady) runCatching { scene.onDestroy(simContext()) }
                swapChain?.let { engine.destroySwapChain(it) }
                engine.destroyCameraComponent(cameraEntity)
                EntityManager.get().destroy(cameraEntity)
                engine.destroyRenderer(renderer)
                engine.destroyView(view)
                engine.destroyScene(filaScene)
                engine.destroy()
            }
            destroyed = true
            thread.quitSafely()
        }
    }

    fun pause() = handler.post { stopLoop() }
    fun resume() = handler.post { if (initialized && swapChain != null) startLoop() }

    // ---- Render thread internals ----

    private fun ensureEngine() {
        if (initialized) return
        // GLES3 backend explicitly. Filament auto-falls-back to GLES2 features it
        // can't get, but we target the GLES3 feature set as the baseline.
        engine = Engine.create()
        renderer = engine.createRenderer().apply {
            // Clear to scene background each frame; skip the readback path.
            clearOptions = clearOptions.apply { clear = true }
        }
        filaScene = engine.createScene()
        cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        view = engine.createView().apply {
            scene = filaScene
            camera = this@SimRenderThread.camera
            // Pre-size; real viewport set when the surface arrives.
            viewport = Viewport(0, 0, 1, 1)
        }
        initialized = true
    }

    private fun ensureScene() {
        if (sceneReady) return
        val ctx = simContext()
        scene.onSurfaceCreated(ctx)   // may throw -> caught by caller -> error state
        scene.onConfigure(ctx)
        sceneReady = true
    }

    private var cachedContext: SimContext? = null
    private fun simContext(): SimContext {
        cachedContext?.let { return it }
        val ctx = SimContext(
            engine = engine,
            scene = filaScene,
            transformManager = engine.transformManager,
            quality = { quality.current },
        )
        cachedContext = ctx
        return ctx
    }

    private fun startLoop() {
        if (running) return
        running = true
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    private fun stopLoop() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val chain = swapChain ?: return
        if (!sceneReady) return

        // Delta time, clamped so a stall (GC, surface change) doesn't teleport the
        // sim by a huge step. Fixed-ish dt keeps physics stable on slow frames.
        val dt = if (lastFrameNanos == 0L) {
            0.0166f
        } else {
            ((frameTimeNanos - lastFrameNanos) * 1e-9f).coerceIn(0f, 0.05f)
        }
        lastFrameNanos = frameTimeNanos

        // 1. Input -> camera + scene. drainInto is the only lock taken per frame.
        relay.drainInto(input)
        cameraRig.applyInput(input)
        relay.drainCommands { code, a, b -> scene.onCommand(cachedContext!!, code, a, b) }

        // 2. Simulation step (allocation-free by contract).
        scene.update(cachedContext!!, dt, input)
        cameraRig.apply(camera)

        // 3. Present. beginFrame returns false when Filament's frame pacing
        //    decides to skip this vsync (e.g. we're ahead) — we honour it and do
        //    NOT render, which is what keeps pacing smooth instead of bursty.
        if (renderer.beginFrame(chain, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
        }

        // 4. Adaptive quality bookkeeping. Measure wall time of the work above.
        val spent = System.nanoTime() - frameTimeNanos
        quality.onFrame(spent, budgetNanos)
        if (quality.dirty) { applyQuality(); quality.clearDirty() }

        // 5. Progress to the shell, throttled (~6/s) to avoid UI-thread spam.
        if (++progressTick >= 10) {
            progressTick = 0
            postProgress(scene.progress())
        }
    }

    /** Pushes the current [QualitySettings] onto the Filament View. Cheap enough
     *  to call on a tier change; never called per frame. */
    private fun applyQuality() {
        val q = quality.current

        view.isPostProcessingEnabled = q.postProcessing
        view.antiAliasing = if (q.postProcessing) View.AntiAliasing.FXAA else View.AntiAliasing.NONE

        view.multiSampleAntiAliasingOptions = view.multiSampleAntiAliasingOptions.apply {
            enabled = q.msaaSamples > 1
            sampleCount = q.msaaSamples
        }

        // Fixed-fraction downscale implemented via Filament dynamic resolution with
        // a pinned scale — gives us [renderScale] without reallocating render
        // targets ourselves. Bilinear upscale on present is free on the GPU.
        view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
            enabled = true
            homogeneousScaling = true
            minScale = q.renderScale
            maxScale = if (q.dynamicResolution) 1.0f else q.renderScale
            quality = View.QualityLevel.LOW
        }

        // LDR colour buffer + lower shadow quality: less bandwidth, the dominant
        // cost on Mali/Adreno entry parts. Shadows toggled per tier.
        view.renderQuality = view.renderQuality.apply {
            hdrColorBuffer = View.QualityLevel.LOW
        }
        // NB: setShadowingEnabled is public but the matching getter is
        // package-private in filament-android, so Kotlin can't form a `var`
        // property — call the setter method directly.
        view.setShadowingEnabled(q.shadowsEnabled)
    }
}
