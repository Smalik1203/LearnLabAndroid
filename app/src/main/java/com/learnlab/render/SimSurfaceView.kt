package com.learnlab.render

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.android.UiHelper

/**
 * The native render target. A [SurfaceView] (NOT TextureView) so the compositor
 * can hand the GPU its own hardware layer — TextureView routes every frame back
 * through the View hierarchy as a texture, adding a copy + a frame of latency we
 * cannot afford on entry hardware.
 *
 * Touch is handled here, on the view that owns the surface, rather than through
 * the Compose pointer pipeline — one fewer thread hop between finger and camera.
 * Chrome buttons (reset/launch) live in the Compose overlay and call in via the
 * [GestureRelay].
 */
@SuppressLint("ViewConstructor")
class SimSurfaceView(
    context: Context,
    scene: SimScene,
    tier: DeviceTier,
    onProgress: (Float) -> Unit,
    onError: (Throwable) -> Unit,
) : SurfaceView(context) {

    val relay = GestureRelay()
    private val quality = RenderQuality(context.applicationContext, tier)
    private val renderThread = SimRenderThread(
        scene = scene,
        quality = quality,
        relay = relay,
        postProgress = { p -> post { onProgress(p) } },   // hop back to UI thread
        postError = { t -> post { onError(t) } },
    )

    private var surfaceW = 0
    private var surfaceH = 0
    private var lastX = 0f
    private var lastY = 0f
    private var dragPointerId = -1

    // ScaleGestureDetector handles two-finger pinch; we keep single-finger drag
    // ourselves so the two never fight over the same MotionEvent stream.
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(d: ScaleGestureDetector): Boolean {
                relay.addPinch(d.scaleFactor)
                return true
            }
        },
    )

    // UiHelper owns the SurfaceHolder lifecycle and feeds us the native Surface.
    // Callbacks arrive on the UI thread; the render thread re-marshals internally.
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
        isOpaque = true
        renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                renderThread.onSurfaceAvailable(surface, surfaceW, surfaceH)
            }
            override fun onDetachedFromSurface() {
                renderThread.onSurfaceDestroyed()
            }
            override fun onResized(w: Int, h: Int) {
                surfaceW = w; surfaceH = h
                renderThread.onSurfaceResized(w, h)
            }
        }
    }

    init {
        uiHelper.attachTo(this)
    }

    /** Send a discrete command (launch, reset, slider commit) to the scene. */
    fun command(code: Int, a: Float = 0f, b: Float = 0f) = relay.postCommand(code, a, b)

    fun onLifecyclePause() = renderThread.pause()
    fun onLifecycleResume() = renderThread.resume()

    /** Must be called when the host leaves composition — frees the Engine. */
    fun release() {
        uiHelper.detach()
        renderThread.shutdown()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragPointerId = event.getPointerId(0)
                lastX = event.x; lastY = event.y
                relay.setTouching(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && dragPointerId != -1) {
                    val idx = event.findPointerIndex(dragPointerId)
                    if (idx != -1) {
                        val dx = event.getX(idx) - lastX
                        val dy = event.getY(idx) - lastY
                        lastX = event.getX(idx); lastY = event.getY(idx)
                        // px -> radians. Tuned so a full-screen drag ~= half turn.
                        relay.addDrag(dx * DRAG_TO_RAD, dy * DRAG_TO_RAD)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // If the dragging finger lifted, hand off to a remaining one.
                val upId = event.getPointerId(event.actionIndex)
                if (upId == dragPointerId) {
                    val other = (0 until event.pointerCount)
                        .firstOrNull { event.getPointerId(it) != upId }
                    if (other != null) {
                        dragPointerId = event.getPointerId(other)
                        lastX = event.getX(other); lastY = event.getY(other)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragPointerId = -1
                relay.setTouching(false)
            }
        }
        return true
    }

    private companion object {
        const val DRAG_TO_RAD = 0.006f
    }
}
