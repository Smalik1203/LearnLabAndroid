package com.learnlab.render

/**
 * The one place UI-thread input crosses to the render thread. Compose pointer
 * callbacks (on the UI thread) accumulate deltas here; the render loop drains
 * them once per frame on the render thread.
 *
 * Design choices that matter for jank:
 *  - Continuous gestures (drag/pinch) are *accumulated*, not queued. A fast
 *    drag firing 200 events/s must not create 200 objects or 200 queue nodes —
 *    we just add into a few floats under a tiny lock. O(1) memory forever.
 *  - Discrete commands (launch/reset) go through a fixed-capacity ring of
 *    preallocated primitive slots — no per-event allocation, bounded backlog.
 *  - The lock is held for only a few field writes, never across Filament calls,
 *    so the render thread is never blocked waiting on the UI thread.
 */
class GestureRelay {

    private val lock = Any()

    // --- continuous, accumulated ---
    private var accDragYaw = 0f
    private var accDragPitch = 0f
    private var accPinch = 1f
    private var touching = false

    // --- discrete command ring (single-producer UI / single-consumer render) ---
    private val cap = 32
    private val cmdCode = IntArray(cap)
    private val cmdA = FloatArray(cap)
    private val cmdB = FloatArray(cap)
    private var head = 0
    private var tail = 0

    /** UI thread: a pan in pixels, pre-converted to radians by the caller. */
    fun addDrag(yawRad: Float, pitchRad: Float) {
        synchronized(lock) { accDragYaw += yawRad; accDragPitch += pitchRad }
    }

    /** UI thread: multiplicative pinch (>1 zoom in). */
    fun addPinch(scale: Float) {
        synchronized(lock) { accPinch *= scale }
    }

    fun setTouching(down: Boolean) {
        synchronized(lock) { touching = down }
    }

    /** UI thread: enqueue a discrete command. Drops on overflow (UI spamming
     *  faster than we render is a non-event; the latest state still applies). */
    fun postCommand(code: Int, a: Float = 0f, b: Float = 0f) {
        synchronized(lock) {
            val next = (head + 1) % cap
            if (next == tail) return        // full — drop oldest-overflow
            cmdCode[head] = code; cmdA[head] = a; cmdB[head] = b
            head = next
        }
    }

    /** Render thread: copy accumulated continuous input into [out] and reset. */
    fun drainInto(out: SimInput) {
        synchronized(lock) {
            out.dragYaw = accDragYaw
            out.dragPitch = accDragPitch
            out.pinchScale = accPinch
            out.touching = touching
            accDragYaw = 0f; accDragPitch = 0f; accPinch = 1f
        }
    }

    /**
     * Render thread: pump pending discrete commands to the scene. Snapshots the
     * ring under lock, then dispatches *outside* the lock so a slow
     * [SimScene.onCommand] never blocks the UI thread.
     */
    inline fun drainCommands(dispatch: (code: Int, a: Float, b: Float) -> Unit) {
        while (true) {
            val packed = nextCommand() ?: break
            dispatch(packed.code, packed.a, packed.b)
        }
    }

    /** Pulls one command (allocation-free path uses [reuse]). */
    fun nextCommand(): Command? {
        synchronized(lock) {
            if (tail == head) return null
            reuse.code = cmdCode[tail]; reuse.a = cmdA[tail]; reuse.b = cmdB[tail]
            tail = (tail + 1) % cap
        }
        return reuse
    }

    // Single reused holder — only ever read on the render thread between pulls.
    private val reuse = Command()

    class Command {
        @JvmField var code = 0
        @JvmField var a = 0f
        @JvmField var b = 0f
    }
}
