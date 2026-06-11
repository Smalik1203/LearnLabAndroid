package com.learnlab.render

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES30
import android.os.Build

/**
 * Classifies the device once at startup so the renderer can pick a starting
 * quality tier. We do NOT trust marketing specs — we read RAM + the actual GL
 * vendor/renderer string, because two "entry" tablets with the same SoC name
 * can have wildly different driver quality.
 *
 * Tiering is a *starting point* only. Thermal/throttle behaviour at runtime is
 * handled separately by [RenderQuality], which can demote us further while a
 * lesson is running. We never promote past the detected ceiling — a weak panel
 * that happens to be cold for 30s should not get effects it can't sustain.
 */
enum class DeviceTier {
    /** 2GB-class IFP / budget tablet, old Mali-4xx/Adreno-3xx-era driver. */
    LOW,
    /** 3-4GB entry tablet, Mali-G52/Adreno-5xx-class. The design target. */
    MID,
    /** 6GB+ with a competent driver. Gets the nice-to-haves, never relied on. */
    HIGH;

    companion object {
        /**
         * Cheap, allocation-light classification. Safe to call on the main
         * thread during startup. The GL string probe requires a live GL context
         * so callers that have one should pass it; otherwise we fall back to
         * RAM + API level, which is conservative (biases toward LOW).
         */
        fun detect(context: Context, glRenderer: String? = null): DeviceTier {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mem = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
            val totalMb = mem.totalMem / (1024L * 1024L)
            val lowRamFlagged = am.isLowRamDevice

            // Known-weak GPU families seen on Indian-market budget IFPs/tablets.
            // Matching is substring + case-insensitive; driver strings are messy.
            val weakGpu = glRenderer?.lowercase()?.let { r ->
                r.contains("mali-4") || r.contains("mali-t6") || r.contains("mali-t7") ||
                    r.contains("adreno (tm) 3") || r.contains("adreno (tm) 4") ||
                    r.contains("powervr")
            } ?: false

            return when {
                lowRamFlagged || totalMb < 2600 || weakGpu -> LOW
                totalMb < 5000 || Build.VERSION.SDK_INT < 29 -> MID
                else -> HIGH
            }
        }

        /**
         * Reads the GL_RENDERER string from a live context. Must be called on a
         * thread with a current GL/EGL context (i.e. the render thread after
         * Filament's Engine is up). Returns null off-thread instead of crashing.
         */
        fun currentGlRenderer(): String? =
            runCatching { GLES30.glGetString(GLES30.GL_RENDERER) }.getOrNull()
    }
}

/**
 * The concrete knobs each tier turns. These are *data*, not behaviour, so the
 * render loop can read them branch-free. Every field here costs frame budget;
 * the defaults are tuned so MID holds 60fps with headroom on a Mali-G52.
 */
data class QualitySettings(
    /** Internal render resolution as a fraction of the surface. The single
     *  biggest lever on a fill-rate-bound entry GPU. <1.0 renders smaller and
     *  upscales on present — barely visible at classroom distance, huge win. */
    val renderScale: Float,
    /** MSAA sample count (1 = off). Off on LOW; entry GPUs choke on resolve. */
    val msaaSamples: Int,
    /** Filament dynamic-resolution lets the GPU auto-drop scale under load.
     *  We pair it with our own thermal tier so the floor is sane. */
    val dynamicResolution: Boolean,
    /** Real-time shadows are expensive (extra depth pass + sampling). Off below
     *  HIGH; a blob/baked shadow reads fine from the back of a room. */
    val shadowsEnabled: Boolean,
    /** Post-processing (bloom/AA/tonemap chain). Tonemap is cheap; bloom is not.
     *  LOW disables the whole stack and uses Filament's "fast" path. */
    val postProcessing: Boolean,
    /** Target frame interval. We always aim 60; the field exists so a pathologic
     *  panel can be pinned to 30 deliberately rather than juddering between. */
    val targetFps: Int,
    /** Max simultaneous high-detail entities before the scene must use LOD/
     *  instancing. Templates read this to decide trajectory-trail density etc. */
    val detailBudget: Int,
) {
    companion object {
        fun forTier(tier: DeviceTier): QualitySettings = when (tier) {
            DeviceTier.LOW -> QualitySettings(
                renderScale = 0.65f,        // ~42% of the pixels of native
                msaaSamples = 1,
                dynamicResolution = true,
                shadowsEnabled = false,
                postProcessing = false,
                targetFps = 60,
                detailBudget = 24,
            )
            DeviceTier.MID -> QualitySettings(
                renderScale = 0.85f,
                msaaSamples = 1,            // FXAA-via-post instead of MSAA
                dynamicResolution = true,
                shadowsEnabled = false,
                postProcessing = true,
                targetFps = 60,
                detailBudget = 64,
            )
            DeviceTier.HIGH -> QualitySettings(
                renderScale = 1.0f,
                msaaSamples = 4,
                dynamicResolution = false,
                shadowsEnabled = true,
                postProcessing = true,
                targetFps = 60,
                detailBudget = 128,
            )
        }
    }
}
