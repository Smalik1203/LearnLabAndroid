package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.MetricChip
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Electromagnetic Induction — Class 10 Physics.
 *
 *   Φ(d) = orientation · B · A / (1 + (d/d₀)²)     (toy flux model)
 *   EMF  = −N · dΦ/dt                              (Faraday)
 *   I    = EMF / R
 *
 * Drag the magnet to change d. Galvanometer needle is driven by I.
 * Auto-oscillate moves the magnet sinusoidally → sine-wave EMF (AC generator).
 */

private const val D0_CM = 14f
private const val COIL_AREA = 0.02f  // m² (visual constant)

@Composable
fun EMInduction(controls: ExperimentControls) {
    val t = LL.tokens

    var N by remember { mutableStateOf(60f) }              // turns
    var B by remember { mutableStateOf(0.8f) }             // T
    var R by remember { mutableStateOf(2.0f) }             // Ω
    var orientation by remember { mutableStateOf(1) }      // ±1
    var autoOsc by remember { mutableStateOf(false) }
    var autoFreq by remember { mutableStateOf(1.2f) }      // Hz
    var magnetXCm by remember { mutableStateOf(-30f) }     // cm, drag target

    // Physics state (kept as MutableStates so the UI updates each frame)
    var flux by remember { mutableStateOf(0f) }
    var emf by remember { mutableStateOf(0f) }
    var current by remember { mutableStateOf(0f) }
    var velocity by remember { mutableStateOf(0f) }
    var needleAngle by remember { mutableStateOf(0f) }
    var flowPhase by remember { mutableStateOf(0f) }
    val history = remember { mutableStateListOf<Pair<Float, Float>>() } // (t, emf)

    var sawPos by remember { mutableStateOf(false) }
    var sawNeg by remember { mutableStateOf(false) }
    var peakEmf by remember { mutableStateOf(0f) }
    var usedAuto by remember { mutableStateOf(false) }

    // Animation loop (always running so the needle returns to centre when idle)
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var elapsed = 0f
        var prevMagnetForV = magnetXCm
        var prevFlux = orientation * B * COIL_AREA /
            (1f + ((magnetXCm) / D0_CM) * ((magnetXCm) / D0_CM))
        flux = prevFlux
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) lastNanos = now
                val dt = min(0.05f, ((now - lastNanos) / 1_000_000_000.0).toFloat())
                lastNanos = now
                elapsed += dt

                // Auto-oscillate drives magnet position
                if (autoOsc) {
                    usedAuto = true
                    val amp = 25f
                    val centre = -15f
                    magnetXCm = centre + amp * sin(2f * PI.toFloat() * autoFreq * elapsed)
                }

                // Flux & Faraday EMF (smoothed for visual)
                val d = magnetXCm
                val phi = orientation * B * COIL_AREA / (1f + (d / D0_CM) * (d / D0_CM))
                val dPhi = phi - prevFlux
                prevFlux = phi
                val rawEmf = if (dt > 0f) -N * (dPhi / dt) else 0f
                val alpha = 0.65f
                emf = alpha * emf + (1f - alpha) * rawEmf
                current = if (R > 0.001f) emf / R else 0f
                flux = phi

                // Velocity (smoothed)
                val v = if (dt > 0f) (magnetXCm - prevMagnetForV) / dt else 0f
                prevMagnetForV = magnetXCm
                velocity = velocity * 0.7f + v * 0.3f

                // Needle target angle ∝ I, clamp to ±60°
                val targetAngle = max(-60f, min(60f, current * 25f))
                needleAngle = needleAngle * 0.85f + targetAngle * 0.15f

                // Wire flow phase
                flowPhase += current * dt * 60f

                // History (last 6 s)
                history.add(elapsed to emf)
                while (history.isNotEmpty() && elapsed - history.first().first > 6f) {
                    history.removeAt(0)
                }

                // Completion tracking
                if (abs(emf) > peakEmf) peakEmf = abs(emf)
                if (emf > 0.4f) sawPos = true
                if (emf < -0.4f) sawNeg = true
            }
        }
    }

    LaunchedEffect(sawPos, sawNeg, usedAuto, peakEmf) {
        var p = 0.15f
        if (peakEmf > 0.3f) p = 0.4f
        if (sawPos || sawNeg) p = 0.6f
        if (sawPos && sawNeg) p = 0.85f
        if (sawPos && sawNeg && usedAuto) { p = 1f; controls.onComplete(1f) }
        controls.onProgress(p)
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Scene + graph ──────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("INDUCTION BENCH", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText("Drag the bar magnet horizontally. Watch the galvanometer swing.",
                        color = t.ink400, size = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetricChip("Φ", "%.4f Wb".format(flux))
                    MetricChip("EMF", "%.2f V".format(emf))
                    MetricChip("I", "%.2f A".format(current))
                    MetricChip("v", "%.1f cm/s".format(velocity))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Scene
            Box(modifier = Modifier.fillMaxWidth().weight(3f).background(t.surface2)) {
                EMScene(
                    magnetXCm = magnetXCm,
                    onDrag = { dxCm ->
                        if (!autoOsc) magnetXCm = (magnetXCm + dxCm).coerceIn(-90f, 60f)
                    },
                    orientation = orientation,
                    current = current,
                    needleAngleDeg = needleAngle,
                    flowPhase = flowPhase,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // EMF graph
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(t.surface2).padding(8.dp)) {
                EmfGraph(history = history)
            }
        }

        // ── Right: controls ────────────────────────────────────
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CoilParamsCard(
                N = N, onN = { N = it },
                B = B, onB = { B = it },
                R = R, onR = { R = it },
                orientation = orientation, onOrientation = { orientation = it },
            )
            AutoOscCard(
                enabled = autoOsc, onEnable = { autoOsc = it },
                freq = autoFreq, onFreq = { autoFreq = it },
                onNudgeIn = { if (!autoOsc) magnetXCm = (magnetXCm + 10f).coerceIn(-90f, 60f) },
                onNudgeOut = { if (!autoOsc) magnetXCm = (magnetXCm - 10f).coerceIn(-90f, 60f) },
            )
            InsightInductionCard()
        }
    }
}

/* ──────────────────────── Scene canvas ─────────────────────────── */

@Composable
private fun EMScene(
    magnetXCm: Float,
    onDrag: (dxCm: Float) -> Unit,
    orientation: Int,
    current: Float,
    needleAngleDeg: Float,
    flowPhase: Float,
) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, drag ->
                        // 4 px ≈ 1 cm by default; scale per actual width below.
                        val pxPerCm = size.width / 200f
                        if (pxPerCm > 0f) onDrag(drag.x / pxPerCm)
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val cx = w / 2f
            val pxPerCm = w / 200f

            // Bench (subtle)
            val benchY = h * 0.6f
            drawRect(Color(0xFF94A3B8).copy(alpha = 0.25f),
                topLeft = Offset(0f, benchY),
                size = Size(w, h - benchY))

            // Coil (vertical stack of loops, centred at cx, axis vertical)
            val coilCx = cx
            val coilCy = h * 0.45f
            val coilR = 50f
            // Draw coil as 6 thin ellipses
            for (i in 0 until 6) {
                val y = coilCy - 30f + i * 12f
                drawOval(
                    color = Color(0xFFB45309).copy(alpha = 0.9f),
                    topLeft = Offset(coilCx - coilR, y - 6f),
                    size = Size(coilR * 2f, 12f),
                    style = Stroke(2.5f),
                )
            }
            // Coil core line
            drawLine(Color(0xFF92400E),
                Offset(coilCx - coilR, coilCy), Offset(coilCx + coilR, coilCy),
                strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3f)))

            // Magnet
            val magCx = cx + magnetXCm * pxPerCm
            val magW = 80f; val magH = 28f
            val nColor = if (orientation == 1) Color(0xFFEF4444) else Color(0xFF94A3B8)
            val sColor = if (orientation == 1) Color(0xFF94A3B8) else Color(0xFFEF4444)
            drawRect(nColor, topLeft = Offset(magCx - magW / 2f, h * 0.42f),
                size = Size(magW / 2f, magH))
            drawRect(sColor, topLeft = Offset(magCx, h * 0.42f),
                size = Size(magW / 2f, magH))
            drawRect(Color(0xFF0F172A),
                topLeft = Offset(magCx - magW / 2f, h * 0.42f),
                size = Size(magW, magH),
                style = Stroke(1.5f))
            // N / S text via tiny line bars (Canvas can't draw text easily without TextMeasurer)
            // We mark poles with small dots
            drawCircle(Color.White, 3f,
                Offset(magCx - magW / 4f, h * 0.42f + magH / 2f))
            drawCircle(Color.White, 3f,
                Offset(magCx + magW / 4f, h * 0.42f + magH / 2f))

            // Wire from coil down to galvanometer (right side)
            val galvCx = w * 0.82f
            val galvCy = h * 0.7f
            val wirePath = Path().apply {
                moveTo(coilCx + coilR, coilCy + 20f)
                lineTo(coilCx + 90f, coilCy + 20f)
                lineTo(coilCx + 90f, galvCy)
                lineTo(galvCx - 40f, galvCy)
            }
            drawPath(wirePath, Color(0xFF334155), style = Stroke(2.5f, cap = StrokeCap.Round))
            // Animated current dashes
            val flowColor = if (current >= 0f) Color(0xFF10B981) else Color(0xFFEF4444)
            drawPath(
                wirePath, flowColor.copy(alpha = 0.85f),
                style = Stroke(
                    2.5f, cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(8f, 10f),
                        phase = -flowPhase,
                    ),
                ),
            )
            // Wire return
            val wireReturn = Path().apply {
                moveTo(coilCx + coilR, coilCy - 20f)
                lineTo(coilCx + 110f, coilCy - 20f)
                lineTo(coilCx + 110f, galvCy - 30f)
                lineTo(galvCx + 40f, galvCy - 30f)
                lineTo(galvCx + 40f, galvCy)
            }
            drawPath(wireReturn, Color(0xFF334155), style = Stroke(2.5f, cap = StrokeCap.Round))

            // Galvanometer (dial)
            val dialR = 50f
            drawCircle(Color.White, dialR, Offset(galvCx, galvCy))
            drawCircle(Color(0xFF334155), dialR, Offset(galvCx, galvCy), style = Stroke(2f))
            // Scale ticks
            for (i in -3..3) {
                val a = i * 20f * PI.toFloat() / 180f - PI.toFloat() / 2f
                val r0 = dialR - 6f; val r1 = dialR - 2f
                val sx = galvCx + cos(a) * r0; val sy = galvCy + sin(a) * r0
                val ex = galvCx + cos(a) * r1; val ey = galvCy + sin(a) * r1
                drawLine(Color(0xFF94A3B8), Offset(sx, sy), Offset(ex, ey), strokeWidth = 1f)
            }
            // 0 mark
            drawLine(Color(0xFF334155),
                Offset(galvCx, galvCy - dialR + 4f), Offset(galvCx, galvCy - dialR + 12f),
                strokeWidth = 2f)
            // Needle
            val needleA = -PI.toFloat() / 2f + needleAngleDeg * PI.toFloat() / 180f
            val needleLen = dialR - 8f
            drawLine(
                Color(0xFFB91C1C),
                Offset(galvCx, galvCy),
                Offset(galvCx + cos(needleA) * needleLen, galvCy + sin(needleA) * needleLen),
                strokeWidth = 2.5f, cap = StrokeCap.Round,
            )
            drawCircle(Color(0xFF0F172A), 4f, Offset(galvCx, galvCy))

            // LED (next to galvanometer) — brightness from |I|
            val ledCx = w * 0.95f; val ledCy = galvCy - 30f
            val brightness = min(1f, abs(current) / 2f)
            drawCircle(Color(0xFFFEF08A).copy(alpha = 0.2f + brightness * 0.8f),
                radius = 12f + brightness * 6f, center = Offset(ledCx, ledCy))
            drawCircle(Color(0xFFCA8A04), 6f, Offset(ledCx, ledCy))

            // Magnet movement hint arrows when |I| is small
            if (abs(current) < 0.05f) {
                val hintY = h * 0.42f + magH + 14f
                drawLine(Color(0xFF94A3B8),
                    Offset(magCx - 30f, hintY), Offset(magCx + 30f, hintY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))
            }
        }
    }
}

@Composable
private fun EmfGraph(history: List<Pair<Float, Float>>) {
    val t = LL.tokens
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LLText("EMF vs time — appears when the magnet moves",
                color = t.ink500, size = 11.sp)
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val tMin = history.first().first
        val tMax = maxOf(history.last().first, tMin + 0.01f)
        val maxAbs = maxOf(0.5f, history.maxOf { abs(it.second) })
        val midY = h / 2f

        drawLine(t.lineStrong, Offset(0f, midY), Offset(w, midY), strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))

        val path = Path().apply {
            history.forEachIndexed { i, (tt, e) ->
                val x = (tt - tMin) / (tMax - tMin) * w
                val y = midY - (e / maxAbs) * (midY - 8f)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, Color(0xFF10B981), style = Stroke(2f, cap = StrokeCap.Round))
    }
}

/* ──────────────────────── Right cards ────────────────────────── */

@Composable
private fun CoilParamsCard(
    N: Float, onN: (Float) -> Unit,
    B: Float, onB: (Float) -> Unit,
    R: Float, onR: (Float) -> Unit,
    orientation: Int, onOrientation: (Int) -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LLText("COIL & MAGNET", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLSlider("Number of turns (N)", N, onN, 10f, 200f, 5f, "turns",
            valueFormat = { "%.0f".format(it) })
        LLSlider("Magnet strength (B)", B, onB, 0.1f, 2.0f, 0.05f, "T",
            valueFormat = { "%.2f".format(it) })
        LLSlider("Coil resistance (R)", R, onR, 0.5f, 10f, 0.25f, "Ω",
            valueFormat = { "%.2f".format(it) })
        Column {
            LLText("Pole facing coil", color = t.ink200, size = 12.sp,
                weight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1 to "N → coil", -1 to "S → coil").forEach { (value, label) ->
                    val on = orientation == value
                    val bg = if (on) t.accent600 else t.surface2
                    val fg = if (on) Color.White else t.ink200
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .clickable { onOrientation(value) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) { LLText(label, color = fg, size = 12.sp, weight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun AutoOscCard(
    enabled: Boolean, onEnable: (Boolean) -> Unit,
    freq: Float, onFreq: (Float) -> Unit,
    onNudgeIn: () -> Unit, onNudgeOut: () -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LLText("MOTION", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ToggleChipEM("Auto-oscillate", enabled) { onEnable(!enabled) }
        }
        LLSlider("Frequency", freq, onFreq, 0.2f, 4f, 0.1f, "Hz",
            enabled = enabled,
            valueFormat = { "%.1f".format(it) })
        if (!enabled) {
            LLText("Or nudge manually:", color = t.ink500, size = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NudgeButton("◀ Out 10 cm", onClick = onNudgeOut)
                NudgeButton("In 10 cm ▶", onClick = onNudgeIn)
            }
        }
    }
}

@Composable
private fun InsightInductionCard() {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText("WHAT TO LOOK FOR", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLText("• Only motion induces EMF. Hold the magnet still — needle reads zero.",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        LLText("• Reverse direction → needle swings the other way (Lenz's law).",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        LLText("• Faster motion → bigger EMF. EMF ∝ dΦ/dt.",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        LLText("• More turns N → bigger EMF for the same dΦ/dt.",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        LLText("• Auto-oscillate → sinusoidal EMF, exactly how AC generators work.",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun ToggleChipEM(label: String, on: Boolean, onToggle: () -> Unit) {
    val t = LL.tokens
    val bg = if (on) t.accent50 else t.surface2
    val fg = if (on) t.accent700 else t.ink500
    val border = if (on) t.accent500 else t.line
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) { LLText(label, color = fg, size = 12.sp, weight = FontWeight.SemiBold) }
}

@Composable
private fun NudgeButton(label: String, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .border(1.dp, t.lineStrong, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) { LLText(label, color = t.ink200, size = 12.sp, weight = FontWeight.Medium) }
}
