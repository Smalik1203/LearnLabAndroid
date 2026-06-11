package com.learnlab.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens specific to the 3D simulation surface. The base [LearnLabTokens]
 * are tuned for a teacher reading a dashboard at arm's length; these are tuned
 * for the SECOND audience — 30-40 students reading a projected/mirrored panel
 * from the back of a classroom. Everything here is bigger, higher-contrast, and
 * uses larger hit targets than the normal app chrome.
 *
 * Rule of thumb baked in: legibility caps out around a 1:200 height ratio, i.e.
 * text must be ~1/50th of viewing distance tall. On a 65" panel seen from 8m,
 * that's a ~32sp floor for anything students must read. Hence the scale below.
 */
object SimType {
    // Minimum readable-from-the-back sizes. Smaller than these = teacher-only.
    val hudLabel = 20.sp          // unit chips on the sim, teacher-proximate
    val readout = 34.sp           // live numeric readouts (velocity, angle)
    val calloutBody = 28.sp       // explanation text overlaid on the scene
    val calloutTitle = 40.sp      // the one thing the class reads at a glance
    val displayWeight = FontWeight.Bold
    val readoutWeight = FontWeight.ExtraBold
}

object SimMetrics {
    /** Touch targets on the control dock. Larger than Material's 48dp min — a
     *  teacher taps these standing up, often without looking. */
    val controlTouch = 64.dp
    val controlGap = 16.dp
    val dockPadding = 20.dp
    val calloutMaxWidthFraction = 0.42f   // never let text cover the hero sim
}

/**
 * Colours for HUD elements that sit ON TOP of an arbitrary 3D scene. They cannot
 * rely on the theme surface behind them, so each carries its own scrim. High
 * alpha + dark base guarantees contrast over a bright sky or a white lab bench.
 */
object SimHud {
    val scrim = Color(0xCC0B1329)          // 80% deep-navy — readable over anything
    val scrimSoft = Color(0x990B1329)
    val onScrim = Color(0xFFF8FAFC)
    val onScrimDim = Color(0xFFCBD5E1)

    // Reuse the motion-semantic palette from the base tokens so a vector is the
    // same colour in the 3D scene, the readout, and the textbook explanation.
    val horizontal = Color(0xFF84CC16)     // == LearnLabTokens.motionHorizontal
    val vertical = Color(0xFF60A5FA)       // == LearnLabTokens.motionVertical
    val gravity = Color(0xFFFB923C)        // == LearnLabTokens.motionGravity

    /** A bottom-anchored gradient so the control dock recedes into the scene
     *  instead of sitting on an opaque bar that competes with the simulation. */
    val dockGradientTop = Color(0x00000000)
    val dockGradientBottom = Color(0xB3030712)
}
