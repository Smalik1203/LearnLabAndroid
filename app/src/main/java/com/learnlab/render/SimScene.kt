package com.learnlab.render

import com.google.android.filament.Engine
import com.google.android.filament.Scene
import com.google.android.filament.TransformManager

/**
 * The contract every per-activity simulation implements. This is the *stubbed
 * interface* the task asks for: the render framework (thread, camera, lighting,
 * gestures, quality) is built once and shared; each NCERT activity is a small
 * [SimScene] that only knows its own physics + meshes.
 *
 * HARD RULES for implementers (these keep us at 60fps):
 *  - [update] runs every frame on the render thread. It must NOT allocate. No
 *    `listOf`, no boxing, no capturing lambdas, no `Float3`/`Mat4` from
 *    filament-utils (they allocate). Use the preallocated scratch in [SimContext].
 *  - All Filament Engine calls happen on the render thread only. Never touch the
 *    Engine from Compose.
 *  - A scene reads input/commands that the framework hands it; it never reads a
 *    SurfaceView, a gesture detector, or app state directly.
 */
interface SimScene {

    /**
     * Build geometry, materials and lights. Called once after the GL context and
     * Filament Engine exist, before the first frame. Heavy work (mesh gen, asset
     * decode) belongs here, NOT in [update]. May fail (e.g. missing .filamat);
     * throw and the host will surface an error state instead of a black screen.
     */
    fun onSurfaceCreated(ctx: SimContext)

    /**
     * Apply the activity's data config (parsed from JSON by the bridge engine).
     * Separated from [onSurfaceCreated] so the same loaded scene can be
     * re-configured without rebuilding GL resources.
     */
    fun onConfigure(ctx: SimContext)

    /**
     * Advance the simulation by [dtSeconds] and write new transforms.
     * MUST be allocation-free. [input] is a reused, mutable snapshot — copy out
     * the primitives you need; do not retain the reference.
     */
    fun update(ctx: SimContext, dtSeconds: Float, input: SimInput)

    /**
     * A discrete UI action (launch, reset, pause, a slider commit). Codes are
     * defined per-template as plain ints to avoid enum allocation across the
     * thread boundary. [a]/[b] carry payload (e.g. slider value).
     */
    fun onCommand(ctx: SimContext, code: Int, a: Float, b: Float)

    /** Surface size changed. Recompute aspect-dependent layout if any. */
    fun onResized(ctx: SimContext, width: Int, height: Int)

    /**
     * 0..1 completion for the shell's progress bar / [ExperimentControls]. Read
     * once per frame on the render thread; cheap, no side effects.
     */
    fun progress(): Float

    /** Release any scene-owned Filament resources. Engine is destroyed by host. */
    fun onDestroy(ctx: SimContext)
}

/**
 * Everything a scene needs from the framework, handed in so scenes never reach
 * for globals. Holds the Filament handles plus a small pool of preallocated
 * scratch arrays so [SimScene.update] can do matrix math without allocating.
 */
class SimContext(
    val engine: Engine,
    val scene: Scene,
    val transformManager: TransformManager,
    /** Live quality knobs; scenes may scale their own detail to [QualitySettings.detailBudget]. */
    val quality: () -> QualitySettings,
) {
    /** Reusable column-major 4x4 for setTransform. One per context = single
     *  render thread, so no contention. Never resized, never reallocated. */
    val scratchMatrix: FloatArray = FloatArray(16)

    /** General-purpose float scratch for building geometry/positions. */
    val scratch3: FloatArray = FloatArray(3)

    /** Writes a translation+uniform-scale transform into [scratchMatrix] and
     *  pushes it to the entity. Allocation-free; the common case for moving a
     *  rigid prop (projectile, pendulum bob, ion) around a scene. */
    fun setTranslationScale(entityInstance: Int, x: Float, y: Float, z: Float, scale: Float) {
        val m = scratchMatrix
        m[0] = scale; m[1] = 0f;    m[2] = 0f;    m[3] = 0f
        m[4] = 0f;    m[5] = scale; m[6] = 0f;    m[7] = 0f
        m[8] = 0f;    m[9] = 0f;    m[10] = scale; m[11] = 0f
        m[12] = x;    m[13] = y;    m[14] = z;    m[15] = 1f
        transformManager.setTransform(entityInstance, m)
    }
}

/**
 * Per-frame continuous input, double-buffered by [GestureRelay] and handed to
 * [SimScene.update] as a read-only snapshot. Plain mutable primitives — no
 * allocation crosses the UI→render boundary each frame.
 */
class SimInput {
    /** Accumulated orbit drag since last frame, in radians. Consumed by camera. */
    @JvmField var dragYaw: Float = 0f
    @JvmField var dragPitch: Float = 0f
    /** Pinch zoom delta (multiplicative), 1.0 = no change. */
    @JvmField var pinchScale: Float = 1f
    /** Whether a finger is currently down (scenes may pause auto-rotate). */
    @JvmField var touching: Boolean = false

    fun reset() {
        dragYaw = 0f; dragPitch = 0f; pinchScale = 1f
    }
}
