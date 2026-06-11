package com.learnlab.render

import android.content.Context
import android.os.PowerManager

/**
 * Runtime quality controller. Owns the *current* [QualitySettings] and demotes
 * them when the device is thermally stressed or when our own frame-time monitor
 * reports sustained misses. This is the "degrade gracefully instead of
 * stuttering" requirement made concrete.
 *
 * Threading: mutated only on the render thread (the loop calls [onFrame] and
 * reads [current]). The Compose side never touches it directly — if the UI
 * wants to show a "performance mode" badge it observes via a snapshot callback.
 *
 * Zero-alloc: no allocations in [onFrame]; it only reads counters and flips an
 * int field. The expensive part (rebuilding the Filament view's render target)
 * happens at most once every [COOLDOWN_FRAMES] and is gated behind [dirty].
 */
class RenderQuality(
    context: Context,
    private val ceiling: DeviceTier,
) {
    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    // The three rungs we can sit on, cheapest-first. We never go below LOW.
    private val ladder = arrayOf(
        QualitySettings.forTier(DeviceTier.LOW),
        QualitySettings.forTier(DeviceTier.MID),
        QualitySettings.forTier(DeviceTier.HIGH),
    )

    // Start at the detected ceiling, then only ever move down at runtime.
    private var rung = ceiling.ordinal
    private val ceilingRung = ceiling.ordinal

    @Volatile
    var current: QualitySettings = ladder[rung]
        private set

    /** Set when [current] changed since the host last applied it to the View. */
    @Volatile
    var dirty: Boolean = false
        private set

    private var framesOverBudget = 0
    private var framesUnderBudget = 0
    private var cooldown = 0
    private var lastThermalCheckFrame = 0
    private var frameCounter = 0

    /**
     * Called once per rendered frame from the render loop.
     *
     * @param frameTimeNanos how long the last frame actually took to produce.
     * @param budgetNanos the target (16.6ms for 60fps).
     */
    fun onFrame(frameTimeNanos: Long, budgetNanos: Long) {
        frameCounter++
        if (cooldown > 0) cooldown--

        // Poll thermal status occasionally — getCurrentThermalStatus is a binder
        // call, far too heavy to run every frame. Once/second is plenty.
        if (frameCounter - lastThermalCheckFrame >= THERMAL_POLL_FRAMES) {
            lastThermalCheckFrame = frameCounter
            val status = powerManager.currentThermalStatus
            if (status >= PowerManager.THERMAL_STATUS_SEVERE) {
                // Hard demote immediately — the SoC is already throttling clocks.
                demote()
                return
            }
        }

        // Soft signal: our own frame timing. A handful of slow frames is noise
        // (GC, a touch-down spike); we only react to a sustained run.
        if (frameTimeNanos > budgetNanos + SLACK_NANOS) {
            framesOverBudget++
            framesUnderBudget = 0
            if (framesOverBudget >= OVER_BUDGET_TRIGGER && cooldown == 0) demote()
        } else {
            framesUnderBudget++
            framesOverBudget = 0
            // We deliberately do NOT auto-promote on a cool run. Promotion risks
            // an oscillation (promote -> overheat -> demote -> repeat) that reads
            // as periodic jank. A lesson is short; pick a floor and hold it.
        }
    }

    private fun demote() {
        if (rung <= 0) return
        rung--
        current = ladder[rung]
        dirty = true
        cooldown = COOLDOWN_FRAMES
        framesOverBudget = 0
    }

    /** Host calls this after it has reconfigured the Filament View. */
    fun clearDirty() { dirty = false }

    /** For a UI badge: are we running below what the hardware was rated for? */
    fun isDegraded(): Boolean = rung < ceilingRung

    private companion object {
        const val THERMAL_POLL_FRAMES = 60          // ~1s at 60fps
        const val OVER_BUDGET_TRIGGER = 30          // ~0.5s of sustained misses
        const val COOLDOWN_FRAMES = 180             // ~3s before another demote
        const val SLACK_NANOS = 2_000_000L          // 2ms tolerance over budget
    }
}
