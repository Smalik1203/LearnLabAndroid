package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.MetricChip
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Magnetic Field & Motor Functioning — Grade 8.
 *
 *   F  = B·I·L           force on one coil side
 *   τ  = N·B·I·A         torque (commutator keeps direction constant)
 *   I_rotor · dω/dt = τ − friction·ω
 *
 * Top-down view of a DC motor: two permanent magnets create a uniform field;
 * a rectangular coil rotates between them. The commutator + brushes flip the
 * current direction every half-turn, so the torque always pushes the coil
 * the same way.
 */

private const val LENGTH_M = 0.05f
private const val AREA_M2 = 0.02f
private const val INERTIA = 0.02f
private const val BRAKE = 8f

@Composable
fun MotorFunctioning(controls: ExperimentControls) {
    val t = LL.tokens

    var current by remember { mutableStateOf(2.0f) }
    var fieldB by remember { mutableStateOf(0.5f) }
    var turns by remember { mutableStateOf(80f) }
    var friction by remember { mutableStateOf(0.2f) }
    var direction by remember { mutableStateOf(1) }
    var powerOn by remember { mutableStateOf(false) }

    var theta by remember { mutableStateOf(0f) }
    var omega by remember { mutableStateOf(0f) }

    var poweredOnOnce by remember { mutableStateOf(false) }
    var changedKnob by remember { mutableStateOf(false) }
    var reversedOnce by remember { mutableStateOf(false) }
    var openedFleming by remember { mutableStateOf(false) }
    var flemingOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) { lastNanos = now; return@withFrameNanos }
                val dt = min(0.05f, ((now - lastNanos) / 1_000_000_000f))
                lastNanos = now
                if (powerOn) {
                    val tau = turns * fieldB * current * AREA_M2 * direction
                    omega = (omega + dt * tau / INERTIA) / (1f + dt * friction / INERTIA)
                } else {
                    omega *= kotlin.math.max(0f, 1f - BRAKE * dt)
                }
                theta = ((theta + omega * dt) % (2f * PI.toFloat()) + 2f * PI.toFloat()) %
                    (2f * PI.toFloat())
            }
        }
    }

    LaunchedEffect(poweredOnOnce, changedKnob, reversedOnce, openedFleming) {
        var p = 0f
        if (poweredOnOnce) p = 0.25f
        if (poweredOnOnce && changedKnob) p = 0.5f
        if (poweredOnOnce && changedKnob && reversedOnce) p = 0.75f
        if (poweredOnOnce && changedKnob && reversedOnce && openedFleming) {
            p = 1f
            controls.onComplete(1f)
        }
        controls.onProgress(p)
    }

    val forcePerSide = fieldB * current * LENGTH_M
    val torque = turns * fieldB * current * AREA_M2 * abs(cos(theta))
    val rpm = abs(omega) * 60f / (2f * PI.toFloat())
    val angleDeg = theta * 180f / PI.toFloat()

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                    LLText("MOTOR BENCH", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText(
                        if (powerOn) "Watch the coil spin. Try the controls on the right."
                        else "Switch on the power to spin the coil.",
                        color = t.ink400, size = 12.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    MetricChip("F", "%.3f N".format(forcePerSide))
                    MetricChip("τ", "%.3f N·m".format(torque))
                    MetricChip("ω", "%.0f RPM".format(rpm))
                    MetricChip("θ", "%.0f°".format(angleDeg))
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(t.accent50)
                            .border(1.dp, t.accent500, RoundedCornerShape(999.dp))
                            .clickable {
                                flemingOpen = true
                                openedFleming = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText("i", color = t.accent700, size = 14.sp,
                            weight = FontWeight.Bold)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(t.surface2)) {
                MotorScene(
                    theta = theta,
                    direction = direction,
                    powerOn = powerOn,
                    forceScale = (forcePerSide / 0.25f).coerceIn(0f, 1f),
                    fieldStrength = (fieldB / 1.0f).coerceIn(0f, 1f),
                )
                if (flemingOpen) {
                    FlemingPopover(onDismiss = { flemingOpen = false })
                }
            }
        }

        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText("CIRCUIT", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                LLSlider("Current (I)", current,
                    { current = it; changedKnob = true },
                    0.5f, 5.0f, 0.1f, "A",
                    info = "Electric current through the coil. Higher current → stronger push on the coil.",
                    valueFormat = { "%.1f".format(it) })
                LLSlider("Field strength (B)", fieldB,
                    { fieldB = it; changedKnob = true },
                    0.1f, 1.0f, 0.05f, "T",
                    info = "Strength of the magnetic field between the magnets.",
                    valueFormat = { "%.2f".format(it) })
                LLSlider("Turns (N)", turns,
                    { turns = it },
                    10f, 200f, 5f, "turns",
                    info = "Loops of wire in the coil. More turns multiply the torque.",
                    valueFormat = { "%.0f".format(it) })
                LLSlider("Friction", friction,
                    { friction = it },
                    0f, 1f, 0.05f, "",
                    info = "Resistance to rotation. Higher friction slows the motor faster.",
                    valueFormat = { "%.2f".format(it) })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LLText("CONTROLS", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                PrimaryButton(
                    label = if (powerOn) "Power off" else "Power on",
                    onClick = {
                        powerOn = !powerOn
                        if (powerOn) poweredOnOnce = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    label = "Reverse current",
                    onClick = {
                        direction = -direction
                        reversedOnce = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    label = "Reset",
                    onClick = {
                        theta = 0f
                        omega = 0f
                        powerOn = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

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
                LLText("• A current in a magnetic field feels a push (F = B·I·L).",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Two opposite pushes on the coil's two sides → torque → rotation.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• The commutator flips the current every half-turn so the push always spins it the same way.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Reverse the current → the motor spins the other way.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Stronger field or higher current → bigger torque → faster spin.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun MotorScene(
    theta: Float,
    direction: Int,
    powerOn: Boolean,
    forceScale: Float,
    fieldStrength: Float,
) {
    val textMeasurer = rememberTextMeasurer()
    val t = LL.tokens
    val inkLabel = t.ink400
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val cx = w / 2f

            // Layout anchors
            val magCy = h * 0.32f
            val magW = w * 0.08f
            val magH = h * 0.34f
            val magGap = h * 0.03f
            val leftMagInner = cx - h * 0.26f
            val rightMagInner = cx + h * 0.26f

            val coilR = h * 0.11f

            val commCy = h * 0.66f
            val commR = h * 0.05f

            val brushW = w * 0.018f
            val brushH = h * 0.07f
            val brushLx = cx - commR
            val brushRx = cx + commR

            val battCy = h * 0.86f
            val battW = w * 0.16f
            val battH = h * 0.06f
            val battLx = cx - battW / 2f
            val battRx = cx + battW / 2f

            // ── Magnets ──
            drawRect(Color(0xFFEF4444),
                topLeft = Offset(leftMagInner - magW, magCy - magH / 2f),
                size = Size(magW, magH))
            drawRect(Color(0xFF0F172A),
                topLeft = Offset(leftMagInner - magW, magCy - magH / 2f),
                size = Size(magW, magH),
                style = Stroke(1.5f))
            drawRect(Color(0xFF3B82F6),
                topLeft = Offset(rightMagInner, magCy - magH / 2f),
                size = Size(magW, magH))
            drawRect(Color(0xFF0F172A),
                topLeft = Offset(rightMagInner, magCy - magH / 2f),
                size = Size(magW, magH),
                style = Stroke(1.5f))

            // N / S text on inner pole face
            drawCenteredText(textMeasurer, "N",
                Offset(leftMagInner - magW * 0.3f, magCy),
                color = Color.White, size = 28.sp, weight = FontWeight.Bold)
            drawCenteredText(textMeasurer, "S",
                Offset(rightMagInner + magW * 0.3f, magCy),
                color = Color.White, size = 28.sp, weight = FontWeight.Bold)

            // Magnet labels
            drawCenteredText(textMeasurer, "Permanent magnet",
                Offset(leftMagInner - magW / 2f, magCy - magH / 2f - 14f),
                color = inkLabel, size = 10.sp)
            drawCenteredText(textMeasurer, "Permanent magnet",
                Offset(rightMagInner + magW / 2f, magCy - magH / 2f - 14f),
                color = inkLabel, size = 10.sp)

            // ── Field arrows N → S ──
            val nLines = 5
            val lineSpacing = magH * 0.16f
            val fieldColor = Color(0xFF94A3B8).copy(alpha = 0.4f + 0.5f * fieldStrength)
            for (i in 0 until nLines) {
                val yLine = magCy - magH * 0.32f + i * lineSpacing
                drawLine(fieldColor,
                    Offset(leftMagInner + magGap, yLine),
                    Offset(rightMagInner - magGap, yLine),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                val ah = 6f
                val ax = rightMagInner - magGap
                drawLine(fieldColor, Offset(ax, yLine),
                    Offset(ax - ah, yLine - ah * 0.7f), strokeWidth = 1.5f)
                drawLine(fieldColor, Offset(ax, yLine),
                    Offset(ax - ah, yLine + ah * 0.7f), strokeWidth = 1.5f)
            }
            drawCenteredText(textMeasurer, "Magnetic field (B)",
                Offset(cx, magCy + magH / 2f + 12f),
                color = Color(0xFF94A3B8), size = 10.sp)

            // ── Coil ──
            val sideA = Offset(cx + coilR * cos(theta), magCy + coilR * sin(theta))
            val sideB = Offset(cx - coilR * cos(theta), magCy - coilR * sin(theta))
            drawLine(Color(0xFFB45309).copy(alpha = 0.95f),
                sideA, sideB,
                strokeWidth = 4f, cap = StrokeCap.Round)
            val sideADotOut = direction == 1
            drawCoilSide(sideA, isOut = sideADotOut)
            drawCoilSide(sideB, isOut = !sideADotOut)
            drawCircle(Color(0xFF334155), 6f, Offset(cx, magCy))

            // Coil leader-line + label
            val coilLabelEnd = Offset(rightMagInner - magGap - 20f, magCy + magH / 2f - 18f)
            drawLine(inkLabel, Offset(cx + coilR * 0.7f, magCy + coilR * 0.7f),
                coilLabelEnd, strokeWidth = 1f)
            drawTextAt(textMeasurer,"Coil",
                Offset(coilLabelEnd.x + 4f, coilLabelEnd.y - 6f),
                color = inkLabel, size = 10.sp)

            // ── Force arrows ──
            val torqueDir = direction.toFloat()
            val tangentA = Offset(-sin(theta) * torqueDir, cos(theta) * torqueDir)
            val tangentB = Offset(sin(theta) * torqueDir, -cos(theta) * torqueDir)
            val maxLen = h * 0.10f
            val arrLen = maxLen * forceScale.coerceAtLeast(0.20f)
            val pulse = if (powerOn) 1f else 0.65f
            drawForceArrow(sideA, sideA + tangentA * (arrLen * pulse), Color(0xFFFCA5A5))
            drawForceArrow(sideB, sideB + tangentB * (arrLen * pulse), Color(0xFFFCA5A5))
            drawTextAt(textMeasurer,"F = B·I·L",
                Offset(sideA.x + 14f, sideA.y + 14f),
                color = Color(0xFFFCA5A5), size = 10.sp, weight = FontWeight.SemiBold)

            // ── Commutator (two half-disks, flipping every π) ──
            val commTopGold = ((theta / PI.toFloat()).toInt() % 2 == 0)
            val gold = Color(0xFFCA8A04)
            val copper = Color(0xFFB45309)
            drawArc(if (commTopGold) gold else copper,
                startAngle = 180f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(cx - commR, commCy - commR),
                size = Size(commR * 2f, commR * 2f))
            drawArc(if (commTopGold) copper else gold,
                startAngle = 0f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(cx - commR, commCy - commR),
                size = Size(commR * 2f, commR * 2f))
            drawCircle(Color(0xFF0F172A), commR, Offset(cx, commCy), style = Stroke(1.5f))
            drawLine(Color(0xFF0F172A),
                Offset(cx - commR, commCy), Offset(cx + commR, commCy),
                strokeWidth = 1.5f)

            // Commutator label
            drawTextAt(textMeasurer,"Commutator",
                Offset(cx + commR + 10f, commCy - 6f),
                color = inkLabel, size = 10.sp)

            // ── Brushes ──
            drawRect(Color(0xFF64748B),
                topLeft = Offset(brushLx - brushW, commCy - brushH / 2f),
                size = Size(brushW, brushH))
            drawRect(Color(0xFF64748B),
                topLeft = Offset(brushRx, commCy - brushH / 2f),
                size = Size(brushW, brushH))
            drawTextAt(textMeasurer,"Brushes",
                Offset(brushLx - brushW - 60f, commCy - 6f),
                color = inkLabel, size = 10.sp)

            // ── Battery ──
            drawRect(Color(0xFF1E293B),
                topLeft = Offset(battLx, battCy - battH / 2f),
                size = Size(battW, battH))
            drawRect(Color(0xFF94A3B8),
                topLeft = Offset(battLx, battCy - battH / 2f),
                size = Size(battW, battH),
                style = Stroke(1.5f))
            // long bar (+) and short bar (−) inside body
            val barX1 = battLx + battW * 0.30f
            val barX2 = battLx + battW * 0.40f
            drawLine(Color(0xFFFCA5A5),
                Offset(barX1, battCy - battH * 0.35f),
                Offset(barX1, battCy + battH * 0.35f),
                strokeWidth = 3f)
            drawLine(Color(0xFF93C5FD),
                Offset(barX2, battCy - battH * 0.20f),
                Offset(barX2, battCy + battH * 0.20f),
                strokeWidth = 3f)
            drawCenteredText(textMeasurer, "+",
                Offset(battLx + 14f, battCy),
                color = Color(0xFFFCA5A5), size = 14.sp, weight = FontWeight.Bold)
            drawCenteredText(textMeasurer, "−",
                Offset(battRx - 14f, battCy),
                color = Color(0xFF93C5FD), size = 14.sp, weight = FontWeight.Bold)
            drawCenteredText(textMeasurer, "Battery",
                Offset(cx, battCy + battH / 2f + 10f),
                color = inkLabel, size = 10.sp)

            // ── Wires battery → brushes ──
            val wireColor = if (powerOn) Color(0xFFFBBF24) else Color(0xFF64748B)
            val wireMidY = (battCy - battH / 2f + commCy + brushH / 2f) / 2f
            val leftBrushBottom = Offset(brushLx - brushW / 2f, commCy + brushH / 2f)
            val rightBrushBottom = Offset(brushRx + brushW / 2f, commCy + brushH / 2f)
            val battTopL = Offset(battLx + 14f, battCy - battH / 2f)
            val battTopR = Offset(battRx - 14f, battCy - battH / 2f)

            drawPath(
                Path().apply {
                    moveTo(battTopL.x, battTopL.y)
                    lineTo(battTopL.x, wireMidY)
                    lineTo(leftBrushBottom.x, wireMidY)
                    lineTo(leftBrushBottom.x, leftBrushBottom.y)
                },
                color = wireColor,
                style = Stroke(2.5f, cap = StrokeCap.Round),
            )
            drawPath(
                Path().apply {
                    moveTo(battTopR.x, battTopR.y)
                    lineTo(battTopR.x, wireMidY)
                    lineTo(rightBrushBottom.x, wireMidY)
                    lineTo(rightBrushBottom.x, rightBrushBottom.y)
                },
                color = wireColor,
                style = Stroke(2.5f, cap = StrokeCap.Round),
            )
        }
    }
}

private fun DrawScope.drawTextAt(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight),
    )
    drawText(textLayoutResult = layout, topLeft = topLeft)
}

private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    center: Offset,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight),
    )
    drawText(textLayoutResult = layout, topLeft = Offset(
        center.x - layout.size.width / 2f,
        center.y - layout.size.height / 2f,
    ))
}

private fun DrawScope.drawCoilSide(pos: Offset, isOut: Boolean) {
    val r = 14f
    drawCircle(Color(0xFFFEF3C7), r, pos)
    drawCircle(Color(0xFF92400E), r, pos, style = Stroke(2f))
    if (isOut) {
        drawCircle(Color(0xFF92400E), 4f, pos)
    } else {
        val s = r * 0.55f
        drawLine(Color(0xFF92400E),
            Offset(pos.x - s, pos.y - s), Offset(pos.x + s, pos.y + s),
            strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(Color(0xFF92400E),
            Offset(pos.x - s, pos.y + s), Offset(pos.x + s, pos.y - s),
            strokeWidth = 2.5f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawForceArrow(start: Offset, end: Offset, color: Color) {
    drawLine(color, start, end, strokeWidth = 3f, cap = StrokeCap.Round)
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val ux = dx / len; val uy = dy / len
    val px = -uy; val py = ux
    val head = 8f
    drawLine(color, end,
        Offset(end.x - ux * head + px * head * 0.5f, end.y - uy * head + py * head * 0.5f),
        strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(color, end,
        Offset(end.x - ux * head - px * head * 0.5f, end.y - uy * head - py * head * 0.5f),
        strokeWidth = 3f, cap = StrokeCap.Round)
}

@Composable
private fun FlemingPopover(onDismiss: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.lineStrong, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .clickable(enabled = false, onClick = {}),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LLText("FLEMING'S LEFT-HAND RULE",
                color = t.accent700, size = 12.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            LLText(
                "Hold your left hand so thumb, first finger and second finger " +
                    "are at right angles to each other.",
                color = t.ink200, size = 14.sp, lineHeight = 20.sp,
            )
            LLText("• First finger → direction of the Field (B)",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            LLText("• Second finger → direction of the Current (I)",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            LLText("• Thumb → direction of the Force (F = B·I·L)",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(4.dp))
            LLText(
                "On opposite sides of the coil the current points opposite ways, " +
                    "so the forces are opposite — together they make the coil rotate.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                SecondaryButton("Got it", onClick = onDismiss)
            }
        }
    }
}
