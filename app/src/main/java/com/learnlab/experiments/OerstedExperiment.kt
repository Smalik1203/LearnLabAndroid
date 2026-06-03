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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.design.GhostButton
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/**
 * Ørsted's Discovery — Grade 8 Chapter 4, Activity 4.1.
 *
 * Toy field model:
 *   targetAngle = K · I / r · sideSign · dirSign     (clamped to ±π/2)
 * where r is the perpendicular distance from the compass to the wire's
 * centreline, sideSign flips for above/below (or left/right of the wire
 * cross-section), and dirSign flips with current direction.
 *
 * The compass needle is smoothed toward `targetAngle` by 15 % per frame —
 * same as the EMInduction galvanometer.
 */

private enum class Scene { Bench, FieldMap }

private const val K_FIELD = 80f          // tuning constant — 1A at ~80px gives ~45° deflection
private const val MIN_R   = 24f          // closest-approach clamp so we never divide near-zero
private const val SMOOTH  = 0.15f        // needle-angle smoothing factor per frame
private const val MAX_ANGLE = (PI / 2f).toFloat()

@Composable
fun OerstedExperiment(controls: ExperimentControls) {
    val t = LL.tokens

    var switchOn       by remember { mutableStateOf(false) }
    var reversed       by remember { mutableStateOf(false) }
    var current        by remember { mutableStateOf(0.8f) }
    var scene          by remember { mutableStateOf(Scene.Bench) }
    var oerstedOpen    by remember { mutableStateOf(false) }
    var draggedPos     by remember { mutableStateOf(Offset(0.65f, 0.30f)) }

    // 6 bench-view compasses at normalised positions inside the canvas.
    // Top row (above wire) at y=0.20, bottom row (below wire) at y=0.62.
    val benchCompassPositions = remember {
        listOf(
            Offset(0.22f, 0.20f), Offset(0.50f, 0.20f), Offset(0.78f, 0.20f),
            Offset(0.22f, 0.62f), Offset(0.50f, 0.62f), Offset(0.78f, 0.62f),
        )
    }
    val benchAngles = remember { mutableStateListOf<Float>().apply { repeat(6) { add(0f) } } }
    var draggedAngle by remember { mutableStateOf(0f) }
    var chevronPhase by remember { mutableStateOf(0f) }

    // progress flags
    var switchedOnOnce by remember { mutableStateOf(false) }
    var reversedOnce by remember { mutableStateOf(false) }
    var draggedOnce by remember { mutableStateOf(false) }
    var oerstedOpenedOnce by remember { mutableStateOf(false) }

    // Bench-view wire centreline (y as a fraction of canvas height). The 6
    // compasses are positioned relative to this line.
    val benchWireFracY = 0.41f

    // Per-frame loop: advance chevron phase + smooth compass needle angles.
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0f else
                    ((now - lastNanos) / 1e9f).coerceAtMost(0.05f)
                lastNanos = now
                if (switchOn) {
                    val speed = 220f * (0.4f + current)
                    chevronPhase = (chevronPhase + (if (reversed) -1f else 1f) * speed * dt) % 60f
                }
                // Smooth bench compass needles toward their per-position target.
                // We compute targets in fractional canvas coords, scaled by an
                // assumed reference canvas size so the K constant has units of
                // "px·A". The Canvas re-computes the right scale at draw time.
                for (i in benchCompassPositions.indices) {
                    val p = benchCompassPositions[i]
                    val tgt = targetAngle(
                        cxFrac = p.x, cyFrac = p.y,
                        wireYFrac = benchWireFracY,
                        currentAmps = if (switchOn) current else 0f,
                        reversed = reversed,
                        canvasRefDim = 600f,
                    )
                    benchAngles[i] = benchAngles[i] + (tgt - benchAngles[i]) * SMOOTH
                }
                // Smooth the dragged compass needle (cross-section: wire at centre 0.5,0.5).
                val tgt2 = targetAngle(
                    cxFrac = draggedPos.x, cyFrac = draggedPos.y,
                    wireYFrac = 0.5f, wireXFrac = 0.5f,
                    currentAmps = if (switchOn) current else 0f,
                    reversed = reversed,
                    crossSection = true,
                    canvasRefDim = 600f,
                )
                draggedAngle = draggedAngle + (tgt2 - draggedAngle) * SMOOTH
            }
        }
    }

    LaunchedEffect(switchedOnOnce, reversedOnce, draggedOnce, oerstedOpenedOnce) {
        val flags = listOf(switchedOnOnce, reversedOnce, draggedOnce, oerstedOpenedOnce)
        controls.onProgress(flags.count { it } / 4f)
        if (flags.all { it }) controls.onComplete(1f)
    }

    Row(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Scene tabs
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SceneTab(label = "BENCH", active = scene == Scene.Bench) { scene = Scene.Bench }
                SceneTab(label = "FIELD MAP", active = scene == Scene.FieldMap) { scene = Scene.FieldMap }
            }
            // Canvas region
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp)),
            ) {
                when (scene) {
                    Scene.Bench -> BenchScene(
                        switchOn = switchOn,
                        reversed = reversed,
                        current = current,
                        wireFracY = benchWireFracY,
                        compassPositions = benchCompassPositions,
                        compassAngles = benchAngles,
                        chevronPhase = chevronPhase,
                    )
                    Scene.FieldMap -> FieldMapScene(
                        switchOn = switchOn,
                        reversed = reversed,
                        current = current,
                        draggedPos = draggedPos,
                        onDrag = { newPos ->
                            draggedPos = Offset(newPos.x.coerceIn(0.05f, 0.95f),
                                newPos.y.coerceIn(0.05f, 0.95f))
                            draggedOnce = true
                        },
                        draggedAngle = draggedAngle,
                    )
                }
            }
            // Footer hint
            FooterHint(scene)
        }

        // Right controls panel
        ControlsPanel(
            switchOn = switchOn,
            onToggleSwitch = {
                switchOn = !switchOn
                if (switchOn) switchedOnOnce = true
            },
            reversed = reversed,
            onReverse = {
                reversed = !reversed
                if (switchOn) reversedOnce = true
            },
            current = current,
            onCurrentChange = { current = it },
            onReset = {
                switchOn = false
                reversed = false
                current = 0.8f
                draggedPos = Offset(0.65f, 0.30f)
                for (i in benchAngles.indices) benchAngles[i] = 0f
                draggedAngle = 0f
                chevronPhase = 0f
            },
            oerstedOpen = oerstedOpen,
            onToggleOersted = {
                oerstedOpen = !oerstedOpen
                if (oerstedOpen) oerstedOpenedOnce = true
            },
            modifier = Modifier.width(290.dp).fillMaxHeight(),
        )
    }
}

// ───────────────────────── physics ─────────────────────────

private fun targetAngle(
    cxFrac: Float,
    cyFrac: Float,
    wireYFrac: Float,
    wireXFrac: Float = -1f,                  // -1 = bench (line along x at wireYFrac)
    currentAmps: Float,
    reversed: Boolean,
    crossSection: Boolean = false,
    canvasRefDim: Float,
): Float {
    if (currentAmps <= 0f) return 0f
    val dirSign = if (reversed) -1f else 1f
    if (crossSection) {
        // Wire dot at (wireXFrac, wireYFrac). Field at point is tangent to the
        // circle around the dot. Needle deflection angle = angle of tangent
        // (the perpendicular to the radial direction), clamped.
        val dx = (cxFrac - wireXFrac) * canvasRefDim
        val dy = (cyFrac - wireYFrac) * canvasRefDim
        val r = max(MIN_R, hypot(dx, dy))
        // Magnitude of horizontal-plane field component proxies the deflection.
        val mag = (K_FIELD * currentAmps / r).coerceAtMost(MAX_ANGLE)
        // Tangent direction: perpendicular to radial, sense depends on current direction.
        val radialAngle = atan2(dy, dx).toFloat()
        // Needle target: rotate radial by +90° (counterclockwise) for current out of page,
        // -90° for current into page (which we tie to `reversed`).
        return clampAngle(radialAngle + (PI.toFloat() / 2f) * dirSign) * (mag / MAX_ANGLE)
    } else {
        // Bench: wire is a horizontal line at wireYFrac across canvas.
        val r = max(MIN_R, abs(cyFrac - wireYFrac) * canvasRefDim)
        val sideSign = if (cyFrac < wireYFrac) +1f else -1f
        val a = (K_FIELD * currentAmps * sideSign * dirSign / r)
        return a.coerceIn(-MAX_ANGLE, MAX_ANGLE)
    }
}

private fun clampAngle(a: Float): Float {
    var x = a
    while (x > PI) x -= (2 * PI).toFloat()
    while (x < -PI) x += (2 * PI).toFloat()
    return x
}

// ───────────────────────── scene composables ─────────────────────────

@Composable
private fun BenchScene(
    switchOn: Boolean,
    reversed: Boolean,
    current: Float,
    wireFracY: Float,
    compassPositions: List<Offset>,
    compassAngles: List<Float>,
    chevronPhase: Float,
) {
    val t = LL.tokens
    Canvas(Modifier.fillMaxSize().padding(8.dp)) {
        drawBenchCircuit(
            switchOn = switchOn,
            reversed = reversed,
            wireFracY = wireFracY,
            chevronPhase = chevronPhase,
            accent = t.accent500,
            wireColor = if (t.isDark) Color(0xFFD4D4D8) else Color(0xFF3F3F46),
            label = t.ink400,
        )
        // Compasses
        compassPositions.forEachIndexed { i, frac ->
            val centre = Offset(frac.x * size.width, frac.y * size.height)
            drawCompass(
                centre = centre,
                radius = 28f,
                needleAngleRad = compassAngles[i],
                ringColor = if (t.isDark) Color(0xFF71717A) else Color(0xFF52525B),
                tickColor = if (t.isDark) Color(0xFFA1A1AA) else Color(0xFF71717A),
                needleHead = Color(0xFFE11D48),
                needleTail = if (t.isDark) Color(0xFFD4D4D8) else Color(0xFF52525B),
                bg = if (t.isDark) Color(0xFF1F1F22) else Color(0xFFFFFFFF),
            )
        }
        // current label
        if (switchOn) {
            val cx = size.width / 2f
            val cy = wireFracY * size.height
            val lbl = if (reversed) "I → flowing right-to-left" else "I → flowing left-to-right"
            // simple "I = x.x A" tag near the wire's left
            // (We rely on the readout card for precise values; this just hints flow direction.)
            @Suppress("UNUSED_VARIABLE") val unused = lbl to (cx to cy)
        }
    }
}

@Composable
private fun FieldMapScene(
    switchOn: Boolean,
    reversed: Boolean,
    current: Float,
    draggedPos: Offset,
    onDrag: (Offset) -> Unit,
    draggedAngle: Float,
) {
    val t = LL.tokens
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val frac = Offset(
                        change.position.x / size.width,
                        change.position.y / size.height,
                    )
                    onDrag(frac)
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val centre = Offset(size.width / 2f, size.height / 2f)
            val maxR = minOf(size.width, size.height) * 0.45f
            // Concentric field circles (only when current flowing)
            val lineColor = if (t.isDark) Color(0xFF52525B) else Color(0xFFD4D4D8)
            val intensity = if (switchOn) (0.3f + current * 0.35f).coerceAtMost(1f) else 0.25f
            for (i in 1..6) {
                val r = maxR * (i / 6f)
                drawCircle(
                    color = lineColor.copy(alpha = intensity),
                    radius = r,
                    center = centre,
                    style = Stroke(1f),
                )
            }
            // Tangent direction arrows on the smallest two circles (only when current on)
            if (switchOn) {
                val arrowR1 = maxR * 0.42f
                val arrowR2 = maxR * 0.72f
                val n = 8
                for (rRing in listOf(arrowR1, arrowR2)) {
                    for (i in 0 until n) {
                        val a = i * 2f * PI.toFloat() / n
                        val px = centre.x + rRing * cos(a)
                        val py = centre.y + rRing * sin(a)
                        val tang = a + (PI.toFloat() / 2f) * (if (reversed) -1f else 1f)
                        val dx = cos(tang) * 8f
                        val dy = sin(tang) * 8f
                        drawLine(
                            color = t.accent500,
                            start = Offset(px - dx, py - dy),
                            end = Offset(px + dx, py + dy),
                            strokeWidth = 1.6f,
                            cap = StrokeCap.Round,
                        )
                        // arrow head
                        val ax = px + dx
                        val ay = py + dy
                        val perpX = -sin(tang) * 3.5f
                        val perpY = cos(tang) * 3.5f
                        drawLine(t.accent500, Offset(ax, ay),
                            Offset(ax - dx * 0.5f + perpX, ay - dy * 0.5f + perpY), 1.6f)
                        drawLine(t.accent500, Offset(ax, ay),
                            Offset(ax - dx * 0.5f - perpX, ay - dy * 0.5f - perpY), 1.6f)
                    }
                }
            }
            // Wire dot — ⊙ (out) for normal, ⊗ (in) for reversed
            val wireR = 16f
            drawCircle(t.accent500, wireR, centre)
            drawCircle(
                if (t.isDark) Color.White else Color.Black,
                wireR,
                centre,
                style = Stroke(1.5f),
            )
            if (reversed) {
                // ⊗ — two diagonals
                val s = wireR * 0.65f
                drawLine(if (t.isDark) Color.White else Color.Black,
                    Offset(centre.x - s, centre.y - s), Offset(centre.x + s, centre.y + s), 2f)
                drawLine(if (t.isDark) Color.White else Color.Black,
                    Offset(centre.x - s, centre.y + s), Offset(centre.x + s, centre.y - s), 2f)
            } else {
                // ⊙ — central dot
                drawCircle(if (t.isDark) Color.White else Color.Black,
                    wireR * 0.30f, centre)
            }
            // Draggable compass
            val drag = Offset(draggedPos.x * size.width, draggedPos.y * size.height)
            drawCompass(
                centre = drag,
                radius = 30f,
                needleAngleRad = draggedAngle,
                ringColor = t.accent500,
                tickColor = if (t.isDark) Color(0xFFA1A1AA) else Color(0xFF71717A),
                needleHead = Color(0xFFE11D48),
                needleTail = if (t.isDark) Color(0xFFD4D4D8) else Color(0xFF52525B),
                bg = if (t.isDark) Color(0xFF1F1F22) else Color(0xFFFFFFFF),
                emphasized = true,
            )
            // Angle readout label next to dragged compass
            // (Kotlin compose's Canvas doesn't render text easily without
            // textMeasurer; we draw a small backdrop and rely on overlay below.)
        }
        // Angle readout overlay
        val deg = (draggedAngle * 180f / PI.toFloat())
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd,
        ) {
            Column(
                Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A3A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                LLText("DRAGGED COMPASS", color = Color.White.copy(alpha = 0.55f), size = 9.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.4.sp)
                LLText("${"%+.0f".format(deg)}°", color = Color.White, size = 16.sp,
                    weight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FooterHint(scene: Scene) {
    val t = LL.tokens
    val text = when (scene) {
        Scene.Bench ->
            "The wire's straight section is the conductor — compasses around it deflect when current flows."
        Scene.FieldMap ->
            "Tangent to a circle = direction of B. Right-hand rule: thumb along I, fingers curl with the field."
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        LLText(text, color = t.ink400, size = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun SceneTab(label: String, active: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) t.accent500 else t.surface2)
            .border(1.dp, if (active) t.accent500 else t.line, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        LLText(label, color = if (active) Color.White else t.ink400, size = 11.sp,
            weight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

// ───────────────────────── controls panel ─────────────────────────

@Composable
private fun ControlsPanel(
    switchOn: Boolean,
    onToggleSwitch: () -> Unit,
    reversed: Boolean,
    onReverse: () -> Unit,
    current: Float,
    onCurrentChange: (Float) -> Unit,
    onReset: () -> Unit,
    oerstedOpen: Boolean,
    onToggleOersted: () -> Unit,
    modifier: Modifier,
) {
    val t = LL.tokens
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Slider + buttons card
        Column(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LLText("CIRCUIT CONTROLS", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp)
            PrimaryButton(
                label = if (switchOn) "Switch OFF" else "Switch ON",
                onClick = onToggleSwitch,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                label = if (reversed) "Reverse → forward" else "Reverse current",
                onClick = onReverse,
                enabled = switchOn,
                modifier = Modifier.fillMaxWidth(),
            )
            LLSlider(
                label = "Current",
                value = current,
                onValueChange = onCurrentChange,
                min = 0f, max = 2f,
                step = 0.1f, unit = "A",
                enabled = switchOn,
                valueFormat = { "%.1f".format(it) },
            )
            GhostButton(
                label = "Reset",
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        // Readout card
        ReadoutCard(switchOn = switchOn, reversed = reversed, current = current)
        // About Ørsted card
        AboutOerstedCard(open = oerstedOpen, onToggle = onToggleOersted)
    }
}

@Composable
private fun ReadoutCard(switchOn: Boolean, reversed: Boolean, current: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A3A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText("READOUT", color = Color.White.copy(alpha = 0.55f),
            size = 10.sp, weight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(2.dp))
        StatLine("Switch", if (switchOn) "ON" else "OFF")
        StatLine("Current", if (switchOn) "%.1f A".format(current) else "0.0 A")
        StatLine("Direction", if (!switchOn) "—" else if (reversed) "← (reversed)" else "→ (forward)")
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LLText(label, color = Color.White.copy(alpha = 0.70f), size = 11.sp,
            modifier = Modifier.weight(1f))
        LLText(value, color = Color.White, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutOerstedCard(open: Boolean, onToggle: () -> Unit) {
    val t = LL.tokens
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("ABOUT ØRSTED · 1820", color = t.accent700, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp, modifier = Modifier.weight(1f))
            LLText(if (open) "▾" else "▸", color = t.ink400, size = 14.sp, weight = FontWeight.Bold)
        }
        if (open) {
            LLText(
                "Hans Christian Ørsted was a Danish professor. While giving a " +
                    "demonstration in 1820, he noticed a compass needle deflect " +
                    "whenever a nearby circuit was closed or opened.",
                color = t.ink200, size = 12.sp, lineHeight = 17.sp,
            )
            LLText(
                "He investigated until he was certain that an electric current " +
                    "produces a magnetic field. His paper opened the field of " +
                    "electromagnetism — eventually leading to motors and generators.",
                color = t.ink200, size = 12.sp, lineHeight = 17.sp,
            )
        } else {
            LLText("Tap to read the 1820 discovery story →",
                color = t.ink500, size = 12.sp)
        }
    }
}

// ───────────────────────── DrawScope helpers ─────────────────────────

private fun DrawScope.drawBenchCircuit(
    switchOn: Boolean,
    reversed: Boolean,
    wireFracY: Float,
    chevronPhase: Float,
    accent: Color,
    wireColor: Color,
    label: Color,
) {
    val w = size.width
    val h = size.height
    val wireY = h * wireFracY
    val bottomY = h * 0.84f
    val batteryLeft = w * 0.05f
    val batteryRight = batteryLeft + 56f
    val switchLeft = w - 56f - w * 0.05f
    val switchRight = w - w * 0.05f

    // Wire path: battery top → up → across → down → switch top
    val wireStroke = Stroke(3f, cap = StrokeCap.Round)
    // Vertical from battery up
    drawLine(wireColor,
        Offset((batteryLeft + batteryRight) / 2f, bottomY - 40f),
        Offset((batteryLeft + batteryRight) / 2f, wireY),
        strokeWidth = 3f, cap = StrokeCap.Round)
    // Horizontal middle segment
    drawLine(wireColor,
        Offset((batteryLeft + batteryRight) / 2f, wireY),
        Offset((switchLeft + switchRight) / 2f, wireY),
        strokeWidth = 3f, cap = StrokeCap.Round)
    // Vertical down to switch
    drawLine(wireColor,
        Offset((switchLeft + switchRight) / 2f, wireY),
        Offset((switchLeft + switchRight) / 2f, bottomY - 40f),
        strokeWidth = 3f, cap = StrokeCap.Round)
    // Bottom rail (battery to switch)
    drawLine(wireColor,
        Offset((batteryLeft + batteryRight) / 2f, bottomY),
        Offset((switchLeft + switchRight) / 2f, bottomY),
        strokeWidth = 3f, cap = StrokeCap.Round)
    // Battery vertical to bottom rail
    drawLine(wireColor,
        Offset((batteryLeft + batteryRight) / 2f, bottomY - 40f),
        Offset((batteryLeft + batteryRight) / 2f, bottomY),
        strokeWidth = 3f, cap = StrokeCap.Round)

    // Battery body
    drawRect(
        if (switchOn) accent.copy(alpha = 0.85f) else Color(0xFF52525B).copy(alpha = 0.85f),
        topLeft = Offset(batteryLeft, bottomY - 40f - 30f),
        size = Size(batteryRight - batteryLeft, 30f),
    )
    drawRect(
        wireColor,
        topLeft = Offset(batteryLeft, bottomY - 40f - 30f),
        size = Size(batteryRight - batteryLeft, 30f),
        style = Stroke(1.5f),
    )

    // Switch — drawn as a small hinged lever
    val swMidX = (switchLeft + switchRight) / 2f
    val swY = bottomY - 40f - 10f
    drawCircle(wireColor, 4f, Offset(switchLeft, swY))
    drawCircle(wireColor, 4f, Offset(switchRight, swY))
    if (switchOn) {
        drawLine(accent, Offset(switchLeft, swY), Offset(switchRight, swY),
            strokeWidth = 4f, cap = StrokeCap.Round)
    } else {
        // Open: lever tilted up
        drawLine(wireColor, Offset(switchLeft, swY),
            Offset(switchRight - 6f, swY - 18f), strokeWidth = 4f, cap = StrokeCap.Round)
    }

    // Current chevrons along the horizontal middle segment
    if (switchOn) {
        val y = wireY
        val xStart = (batteryLeft + batteryRight) / 2f
        val xEnd = (switchLeft + switchRight) / 2f
        var x = xStart + chevronPhase
        // Wrap phase: ensure first chevron lies within the segment
        while (x > xStart + 60f) x -= 60f
        while (x < xStart) x += 60f
        while (x < xEnd) {
            drawChevron(Offset(x, y), reversed, accent)
            x += 60f
        }
    }
}

private fun DrawScope.drawChevron(centre: Offset, reversed: Boolean, color: Color) {
    val len = 10f
    val h = 6f
    if (!reversed) {
        // > pointing right
        drawLine(color, Offset(centre.x - len, centre.y - h),
            Offset(centre.x, centre.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(centre.x - len, centre.y + h),
            Offset(centre.x, centre.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
    } else {
        // < pointing left
        drawLine(color, Offset(centre.x + len, centre.y - h),
            Offset(centre.x, centre.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color, Offset(centre.x + len, centre.y + h),
            Offset(centre.x, centre.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawCompass(
    centre: Offset,
    radius: Float,
    needleAngleRad: Float,
    ringColor: Color,
    tickColor: Color,
    needleHead: Color,
    needleTail: Color,
    bg: Color,
    emphasized: Boolean = false,
) {
    // Background disk + outer ring
    drawCircle(bg, radius, centre)
    drawCircle(
        ringColor,
        radius,
        centre,
        style = Stroke(if (emphasized) 2.5f else 1.5f),
    )
    // Cardinal ticks at N/E/S/W
    for (k in 0..3) {
        val a = k * (PI.toFloat() / 2f)
        val inner = Offset(centre.x + (radius - 5f) * sin(a),
            centre.y - (radius - 5f) * cos(a))
        val outer = Offset(centre.x + radius * sin(a),
            centre.y - radius * cos(a))
        drawLine(tickColor, inner, outer, strokeWidth = 1.2f, cap = StrokeCap.Round)
    }
    // Needle: head + tail, rotated by needleAngleRad
    val len = radius * 0.85f
    val tail = radius * 0.55f
    val dirX = sin(needleAngleRad)
    val dirY = -cos(needleAngleRad)
    val perpX = -dirY
    val perpY = dirX
    // Head (red)
    val tipH = Offset(centre.x + dirX * len, centre.y + dirY * len)
    val baseHA = Offset(centre.x + perpX * 3f, centre.y + perpY * 3f)
    val baseHB = Offset(centre.x - perpX * 3f, centre.y - perpY * 3f)
    val headPath = Path().apply {
        moveTo(tipH.x, tipH.y); lineTo(baseHA.x, baseHA.y); lineTo(baseHB.x, baseHB.y); close()
    }
    drawPath(headPath, needleHead)
    // Tail (gray)
    val tipT = Offset(centre.x - dirX * tail, centre.y - dirY * tail)
    val tailPath = Path().apply {
        moveTo(tipT.x, tipT.y); lineTo(baseHA.x, baseHA.y); lineTo(baseHB.x, baseHB.y); close()
    }
    drawPath(tailPath, needleTail)
    // Pivot
    drawCircle(ringColor, 2f, centre)
}

