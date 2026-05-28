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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
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

// Cabinet projection: back of scene tilts up (and left or right depending on mirror).
// World axes: +x right, +y down, +z toward viewer (front).
private const val DEPTH_DX = 0.45f
private const val DEPTH_DY = 0.30f

private fun projP(
    x: Float, y: Float, z: Float,
    origin: Offset,
    mirror: Boolean = false,
): Offset {
    val dx = if (mirror) -z * DEPTH_DX else z * DEPTH_DX
    val dy = z * DEPTH_DY
    return Offset(origin.x + x + dx, origin.y + y + dy)
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
    val purple = Color(0xFFA855F7)
    val coilColor = Color(0xFFB45309)
    val coilHighlight = Color(0xFFFBBF24)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val origin = Offset(w / 2f, h * 0.44f)
            val u = min(w, h) * 0.0038f

            // World dimensions
            val magW = u * 38f
            val magH = u * 110f
            val magD = u * 60f
            val magGap = u * 70f
            val coilR = u * 28f
            val coilZ = u * 52f
            val shaftLen = u * 38f
            val commZ = coilZ + shaftLen
            val commR = u * 14f
            val brushW = u * 7f
            val brushH = u * 22f
            val battY = u * 95f
            val battW = u * 80f
            val battH = u * 22f
            val battZ = commZ + u * 30f

            // ── Magnets (3D cuboids, inner faces visible via mirrored projection) ──
            val nXc = -(magGap + magW / 2f)
            drawCuboid3D(
                origin,
                xMin = nXc - magW / 2f, xMax = nXc + magW / 2f,
                yMin = -magH / 2f, yMax = magH / 2f,
                zMin = -magD / 2f, zMax = magD / 2f,
                frontColor = Color(0xFFEF4444),
                topColor = Color(0xFFB91C1C),
                sideColor = Color(0xFFDC2626),
                borderColor = Color(0xFF7F1D1D),
                mirror = true,
            )
            drawCenteredText(textMeasurer, "N",
                center = projP(nXc + magW / 2f, 0f, 0f, origin, mirror = true),
                color = Color.White, size = 38.sp, weight = FontWeight.Bold)

            val sXc = magGap + magW / 2f
            drawCuboid3D(
                origin,
                xMin = sXc - magW / 2f, xMax = sXc + magW / 2f,
                yMin = -magH / 2f, yMax = magH / 2f,
                zMin = -magD / 2f, zMax = magD / 2f,
                frontColor = Color(0xFF3B82F6),
                topColor = Color(0xFF1D4ED8),
                sideColor = Color(0xFF2563EB),
                borderColor = Color(0xFF1E3A8A),
                mirror = false,
            )
            drawCenteredText(textMeasurer, "S",
                center = projP(sXc - magW / 2f, 0f, 0f, origin, mirror = false),
                color = Color.White, size = 38.sp, weight = FontWeight.Bold)

            drawCenteredText(textMeasurer, "North pole",
                projP(nXc, -magH / 2f - u * 14f, 0f, origin, mirror = true),
                color = Color(0xFFB91C1C), size = 11.sp, weight = FontWeight.SemiBold)
            drawCenteredText(textMeasurer, "South pole",
                projP(sXc, -magH / 2f - u * 14f, 0f, origin, mirror = false),
                color = Color(0xFF1D4ED8), size = 11.sp, weight = FontWeight.SemiBold)

            // ── Magnetic field (B) ──
            val nLines = 5
            val lineSpacing = magH * 0.17f
            val fieldGreen = Color(0xFF22C55E)
            val fieldColor = fieldGreen.copy(alpha = 0.35f + 0.55f * fieldStrength)
            for (i in 0 until nLines) {
                val yLine = -magH * 0.34f + i * lineSpacing
                val start = projP(-magGap + u * 3f, yLine, 0f, origin)
                val end = projP(magGap - u * 3f, yLine, 0f, origin)
                drawLine(fieldColor, start, end,
                    strokeWidth = 1.6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                val ah = 7f
                drawLine(fieldColor, end,
                    Offset(end.x - ah, end.y - ah * 0.65f), strokeWidth = 1.6f)
                drawLine(fieldColor, end,
                    Offset(end.x - ah, end.y + ah * 0.65f), strokeWidth = 1.6f)
            }
            drawCenteredText(textMeasurer, "B  magnetic field",
                projP(0f, magH / 2f + u * 16f, 0f, origin),
                color = fieldGreen, size = 11.sp, weight = FontWeight.SemiBold)

            // ── Armature coil ──
            // Long sides parallel to z-axis at angle theta around the z-axis.
            val legAx = coilR * sin(theta)
            val legAy = -coilR * cos(theta)
            val legBx = -coilR * sin(theta)
            val legBy =  coilR * cos(theta)

            val legABack = projP(legAx, legAy, -coilZ, origin)
            val legAFront = projP(legAx, legAy, coilZ, origin)
            val legBBack = projP(legBx, legBy, -coilZ, origin)
            val legBFront = projP(legBx, legBy, coilZ, origin)

            // Back short side (connects legA-back to legB-back)
            drawLine(coilColor, legABack, legBBack,
                strokeWidth = 4.5f, cap = StrokeCap.Round)

            // Long active sides
            drawLine(coilColor, legABack, legAFront,
                strokeWidth = 5.5f, cap = StrokeCap.Round)
            drawLine(coilColor, legBBack, legBFront,
                strokeWidth = 5.5f, cap = StrokeCap.Round)

            // Shaft (axis of rotation, +z direction toward commutator)
            val shaftBack = projP(0f, 0f, coilZ, origin)
            val commCenter = projP(0f, 0f, commZ, origin)
            drawLine(Color(0xFF52525B), shaftBack, commCenter,
                strokeWidth = 3.5f, cap = StrokeCap.Round)

            // Coil-to-commutator wires (front short sides routed via commutator outer edge)
            val attachAx = commR * sin(theta)
            val attachAy = -commR * cos(theta)
            val attachBx = -commR * sin(theta)
            val attachBy =  commR * cos(theta)
            val attachA = projP(attachAx, attachAy, commZ - u * 1f, origin)
            val attachB = projP(attachBx, attachBy, commZ - u * 1f, origin)
            drawLine(coilColor, legAFront, attachA,
                strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(coilColor, legBFront, attachB,
                strokeWidth = 3.5f, cap = StrokeCap.Round)

            // ── Commutator (front disc split into two halves) ──
            val gold = Color(0xFFCA8A04)
            val copper = Color(0xFFB45309)
            val degrees = theta * 180f / PI.toFloat()
            withTransform({
                rotate(degrees, pivot = commCenter)
            }) {
                drawArc(gold,
                    startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(commCenter.x - commR, commCenter.y - commR),
                    size = Size(commR * 2f, commR * 2f))
                drawArc(copper,
                    startAngle = 0f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(commCenter.x - commR, commCenter.y - commR),
                    size = Size(commR * 2f, commR * 2f))
                drawLine(Color(0xFF1F2937),
                    Offset(commCenter.x - commR, commCenter.y),
                    Offset(commCenter.x + commR, commCenter.y),
                    strokeWidth = 1.5f)
            }
            drawCircle(Color(0xFF1F2937), commR, commCenter, style = Stroke(1.5f))

            // ── Brushes (touch commutator from below) ──
            val brushLCx = commCenter.x - commR - brushW * 0.5f
            val brushRCx = commCenter.x + commR + brushW * 0.5f
            val brushCy = commCenter.y + brushH * 0.35f
            drawRect(Color(0xFF52525B),
                topLeft = Offset(brushLCx - brushW / 2f, brushCy - brushH / 2f),
                size = Size(brushW, brushH))
            drawRect(Color(0xFF18181B),
                topLeft = Offset(brushLCx - brushW / 2f, brushCy - brushH / 2f),
                size = Size(brushW, brushH), style = Stroke(1f))
            drawRect(Color(0xFF52525B),
                topLeft = Offset(brushRCx - brushW / 2f, brushCy - brushH / 2f),
                size = Size(brushW, brushH))
            drawRect(Color(0xFF18181B),
                topLeft = Offset(brushRCx - brushW / 2f, brushCy - brushH / 2f),
                size = Size(brushW, brushH), style = Stroke(1f))

            // ── Current direction chevrons on coil legs ──
            // Current in legA is along +z * direction; in legB along -z * direction.
            val chevOffset = u * 9f
            val legAMid = projP(legAx, legAy, 0f, origin)
            val legAChevTo = projP(legAx, legAy, direction * chevOffset, origin)
            drawArrowChevron(legAMid, legAChevTo, coilHighlight)
            val legBMid = projP(legBx, legBy, 0f, origin)
            val legBChevTo = projP(legBx, legBy, -direction * chevOffset, origin)
            drawArrowChevron(legBMid, legBChevTo, coilHighlight)

            // ── Force arrows on the two active legs (vertical in world) ──
            // F = I*L × B. Current along z, B along +x → force along ±y.
            val pulse = if (powerOn) 1f else 0.55f
            val arrLen = u * 40f * forceScale.coerceAtLeast(0.30f) * pulse
            val fA = direction * arrLen
            val fB = -direction * arrLen
            val fAEnd = Offset(legAMid.x, legAMid.y + fA)
            val fBEnd = Offset(legBMid.x, legBMid.y + fB)
            drawForceArrow3D(legAMid, fAEnd, purple)
            drawForceArrow3D(legBMid, fBEnd, purple)
            drawTextAt(textMeasurer, "F",
                Offset(fAEnd.x + 6f, fAEnd.y - if (fA > 0) -6f else 14f),
                color = purple, size = 14.sp, weight = FontWeight.Bold)
            drawTextAt(textMeasurer, "F",
                Offset(fBEnd.x + 6f, fBEnd.y - if (fB > 0) -6f else 14f),
                color = purple, size = 14.sp, weight = FontWeight.Bold)

            // ── Battery + wires ──
            val battCenterScreen = projP(0f, battY, battZ, origin)
            val battTopLeft = Offset(battCenterScreen.x - battW / 2f,
                                     battCenterScreen.y - battH / 2f)
            // Body (cylindrical look via rounded rect)
            drawRoundRect(
                color = Color(0xFFE7E5E4),
                topLeft = battTopLeft,
                size = Size(battW, battH),
                cornerRadius = CornerRadius(battH / 2f, battH / 2f),
            )
            drawRoundRect(
                color = Color(0xFF52525B),
                topLeft = battTopLeft,
                size = Size(battW, battH),
                cornerRadius = CornerRadius(battH / 2f, battH / 2f),
                style = Stroke(1.5f),
            )
            // Positive cap (red nub on right end)
            drawRect(Color(0xFFEF4444),
                topLeft = Offset(battTopLeft.x + battW - u * 5f,
                                 battCenterScreen.y - battH * 0.22f),
                size = Size(u * 4f, battH * 0.44f))
            drawCenteredText(textMeasurer, "+",
                Offset(battTopLeft.x + battW - battH * 0.5f, battCenterScreen.y),
                color = Color(0xFF7F1D1D), size = 14.sp, weight = FontWeight.Bold)
            drawCenteredText(textMeasurer, "−",
                Offset(battTopLeft.x + battH * 0.5f, battCenterScreen.y),
                color = Color(0xFF27272A), size = 14.sp, weight = FontWeight.Bold)
            drawCenteredText(textMeasurer, "Battery",
                Offset(battCenterScreen.x, battTopLeft.y + battH + 12f),
                color = inkLabel, size = 10.sp)

            // Wires (battery terminals → brushes) — bend up around the assembly
            val wireColor = if (powerOn) coilHighlight else Color(0xFF94A3B8)
            val rightTerm = Offset(battTopLeft.x + battW + u * 3f, battCenterScreen.y)
            val leftTerm = Offset(battTopLeft.x - u * 3f, battCenterScreen.y)
            val rightBrushPort = Offset(brushRCx + brushW / 2f, brushCy + brushH * 0.2f)
            val leftBrushPort = Offset(brushLCx - brushW / 2f, brushCy + brushH * 0.2f)

            val rightWire = Path().apply {
                moveTo(rightTerm.x, rightTerm.y)
                quadraticTo(
                    rightTerm.x + u * 30f, (rightTerm.y + rightBrushPort.y) / 2f,
                    rightBrushPort.x, rightBrushPort.y,
                )
            }
            drawPath(rightWire, wireColor, style = Stroke(3f, cap = StrokeCap.Round))

            val leftWire = Path().apply {
                moveTo(leftTerm.x, leftTerm.y)
                quadraticTo(
                    leftTerm.x - u * 30f, (leftTerm.y + leftBrushPort.y) / 2f,
                    leftBrushPort.x, leftBrushPort.y,
                )
            }
            drawPath(leftWire, wireColor, style = Stroke(3f, cap = StrokeCap.Round))

            // ── Component labels ──
            drawTextAt(textMeasurer, "Armature (coil)",
                Offset(legAFront.x + 8f, legAFront.y + u * 12f),
                color = inkLabel, size = 10.sp)
            drawTextAt(textMeasurer, "Commutator",
                Offset(commCenter.x + commR + 6f, commCenter.y - 6f),
                color = inkLabel, size = 10.sp)
            drawTextAt(textMeasurer, "Brush",
                Offset(brushLCx - 46f, brushCy - 4f),
                color = inkLabel, size = 10.sp)
            drawTextAt(textMeasurer, "Brush",
                Offset(brushRCx + 10f, brushCy - 4f),
                color = inkLabel, size = 10.sp)
            drawTextAt(textMeasurer, "F = B · I · L",
                Offset(w - 110f, 6f),
                color = purple, size = 12.sp, weight = FontWeight.SemiBold)
        }
    }
}

private fun DrawScope.drawCuboid3D(
    origin: Offset,
    xMin: Float, xMax: Float,
    yMin: Float, yMax: Float,
    zMin: Float, zMax: Float,
    frontColor: Color,
    topColor: Color,
    sideColor: Color,
    borderColor: Color,
    mirror: Boolean,
) {
    fun p(x: Float, y: Float, z: Float) = projP(x, y, z, origin, mirror)
    val ftl = p(xMin, yMin, zMax)
    val ftr = p(xMax, yMin, zMax)
    val fbl = p(xMin, yMax, zMax)
    val fbr = p(xMax, yMax, zMax)
    val btl = p(xMin, yMin, zMin)
    val btr = p(xMax, yMin, zMin)

    // Top face (visible)
    val topPath = Path().apply {
        moveTo(ftl.x, ftl.y); lineTo(ftr.x, ftr.y)
        lineTo(btr.x, btr.y); lineTo(btl.x, btl.y); close()
    }
    drawPath(topPath, topColor)
    drawPath(topPath, borderColor, style = Stroke(1.5f))

    // Inner side face: right if mirror=true, left otherwise
    val sidePath = if (mirror) {
        val bbr = p(xMax, yMax, zMin)
        Path().apply {
            moveTo(ftr.x, ftr.y); lineTo(fbr.x, fbr.y)
            lineTo(bbr.x, bbr.y); lineTo(btr.x, btr.y); close()
        }
    } else {
        val bbl = p(xMin, yMax, zMin)
        Path().apply {
            moveTo(ftl.x, ftl.y); lineTo(fbl.x, fbl.y)
            lineTo(bbl.x, bbl.y); lineTo(btl.x, btl.y); close()
        }
    }
    drawPath(sidePath, sideColor)
    drawPath(sidePath, borderColor, style = Stroke(1.5f))

    // Front face (drawn last so labels sit clean on top)
    val frontPath = Path().apply {
        moveTo(ftl.x, ftl.y); lineTo(ftr.x, ftr.y)
        lineTo(fbr.x, fbr.y); lineTo(fbl.x, fbl.y); close()
    }
    drawPath(frontPath, frontColor)
    drawPath(frontPath, borderColor, style = Stroke(1.5f))
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

private fun DrawScope.drawForceArrow3D(start: Offset, end: Offset, color: Color) {
    drawLine(color, start, end, strokeWidth = 3.5f, cap = StrokeCap.Round)
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val ux = dx / len; val uy = dy / len
    val px = -uy; val py = ux
    val head = 10f
    drawLine(color, end,
        Offset(end.x - ux * head + px * head * 0.55f,
               end.y - uy * head + py * head * 0.55f),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(color, end,
        Offset(end.x - ux * head - px * head * 0.55f,
               end.y - uy * head - py * head * 0.55f),
        strokeWidth = 3.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawArrowChevron(start: Offset, end: Offset, color: Color) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val ux = dx / len; val uy = dy / len
    val px = -uy; val py = ux
    val head = 6f
    drawLine(color, end,
        Offset(end.x - ux * head + px * head * 0.55f,
               end.y - uy * head + py * head * 0.55f),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(color, end,
        Offset(end.x - ux * head - px * head * 0.55f,
               end.y - uy * head - py * head * 0.55f),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
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
