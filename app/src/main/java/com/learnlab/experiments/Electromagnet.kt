package com.learnlab.experiments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.GhostButton
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Electromagnet — Grade 8 Chapter 4 §4.1.1 (Activities 4.2, 4.3, 4.4).
 *
 * Toy strength model:
 *   strength = current × (turns / 50) × coreFactor
 * with coreFactor 4.0 (iron), 2.5 (steel), 1.0 (air).
 * Clips held ∝ strength (capped at 16).
 *
 * The student-facing variables match the textbook 'Think-like-a-scientist':
 * change cells (current), change turns, change core, observe poles & clip count.
 */

private enum class Core { Iron, Steel, Air }

private const val MAX_CLIPS = 16
private const val SMOOTH = 0.18f

// Clip slot states (per clip).
private const val SLOT_AT_TRAY = 0
private const val SLOT_ANCHORED_LEFT = 1
private const val SLOT_ANCHORED_RIGHT = 2

@Composable
fun Electromagnet(controls: ExperimentControls) {
    val t = LL.tokens

    var switchOn   by remember { mutableStateOf(false) }
    var turns      by remember { mutableStateOf(50) }
    var current    by remember { mutableStateOf(1.5f) }
    var core       by remember { mutableStateOf(Core.Iron) }
    var appsOpen   by remember { mutableStateOf(false) }
    var chevronPhase by remember { mutableStateOf(0f) }

    // Progress flags
    var switchedOnOnce by remember { mutableStateOf(false) }
    var changedTurnsOnce by remember { mutableStateOf(false) }
    var changedCurrentOnce by remember { mutableStateOf(false) }
    var changedCoreOnce by remember { mutableStateOf(false) }
    var openedAppsOnce by remember { mutableStateOf(false) }

    // Clip slots — start all in tray
    val clipSlots = remember {
        mutableStateListOf<Int>().apply { repeat(MAX_CLIPS) { add(SLOT_AT_TRAY) } }
    }

    val coreFactor = when (core) {
        Core.Iron -> 4.0f
        Core.Steel -> 2.5f
        Core.Air -> 1.0f
    }
    val rawStrength = if (switchOn) current * (turns / 50f) * coreFactor else 0f
    // Max possible strength = 3.0 × (100/50) × 4.0 = 24 → normalize to 0..1
    val strengthN = (rawStrength / 24f).coerceIn(0f, 1f)
    val clipsTarget = (rawStrength * 3f).toInt().coerceIn(0, MAX_CLIPS)

    // Animate the strength meter
    val animatedStrength by animateFloatAsState(
        targetValue = strengthN,
        animationSpec = tween(400),
        label = "strength-meter",
    )

    // Per-frame loop: chevron phase + clip pickup/drop animation
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var clipTickAccum = 0f
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0f
                else ((now - lastNanos) / 1e9f).coerceAtMost(0.05f)
                lastNanos = now

                if (switchOn) {
                    val speed = 220f * (0.4f + current * 0.5f)
                    chevronPhase = (chevronPhase + speed * dt) % 60f
                }

                // Pickup / drop animation: every 100 ms, move one clip toward
                // matching the current clipsTarget. Anchored clips stay where
                // they are unless we need to drop them (strength fell).
                clipTickAccum += dt
                if (clipTickAccum >= 0.10f) {
                    clipTickAccum = 0f
                    val anchored = clipSlots.count { it != SLOT_AT_TRAY }
                    if (anchored < clipsTarget) {
                        // Find first tray-slot and anchor it, alternating sides
                        val nextSide = if (
                            clipSlots.count { it == SLOT_ANCHORED_LEFT } <=
                            clipSlots.count { it == SLOT_ANCHORED_RIGHT }
                        ) SLOT_ANCHORED_LEFT else SLOT_ANCHORED_RIGHT
                        val i = clipSlots.indexOfFirst { it == SLOT_AT_TRAY }
                        if (i >= 0) clipSlots[i] = nextSide
                    } else if (anchored > clipsTarget) {
                        // Drop one anchored clip — prefer the side with more
                        val left = clipSlots.count { it == SLOT_ANCHORED_LEFT }
                        val right = clipSlots.count { it == SLOT_ANCHORED_RIGHT }
                        val dropSide = if (left >= right) SLOT_ANCHORED_LEFT else SLOT_ANCHORED_RIGHT
                        val i = clipSlots.indexOfLast { it == dropSide }
                        if (i >= 0) clipSlots[i] = SLOT_AT_TRAY
                    }
                }
            }
        }
    }

    LaunchedEffect(switchedOnOnce, changedTurnsOnce, changedCurrentOnce, changedCoreOnce, openedAppsOnce) {
        val flags = listOf(switchedOnOnce, changedTurnsOnce, changedCurrentOnce, changedCoreOnce, openedAppsOnce)
        controls.onProgress(flags.count { it } / 5f)
        if (flags.all { it }) controls.onComplete(1f)
    }

    val clipsHeld = clipSlots.count { it != SLOT_AT_TRAY }

    Row(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp)),
            ) {
                BenchScene(
                    switchOn = switchOn,
                    turns = turns,
                    current = current,
                    core = core,
                    strengthN = strengthN,
                    chevronPhase = chevronPhase,
                    clipSlots = clipSlots,
                )
            }
            FooterHint(switchOn, clipsHeld)
        }

        ControlsPanel(
            switchOn = switchOn,
            onToggleSwitch = {
                switchOn = !switchOn
                if (switchOn) switchedOnOnce = true
            },
            turns = turns,
            onTurnsChange = {
                turns = it
                changedTurnsOnce = true
            },
            current = current,
            onCurrentChange = {
                current = it
                if (switchOn) changedCurrentOnce = true
            },
            core = core,
            onCoreChange = {
                core = it
                changedCoreOnce = true
            },
            strengthN = animatedStrength,
            clipsHeld = clipsHeld,
            appsOpen = appsOpen,
            onToggleApps = {
                appsOpen = !appsOpen
                if (appsOpen) openedAppsOnce = true
            },
            onReset = {
                switchOn = false
                turns = 50
                current = 1.5f
                core = Core.Iron
                chevronPhase = 0f
                for (i in clipSlots.indices) clipSlots[i] = SLOT_AT_TRAY
            },
            modifier = Modifier.width(290.dp).fillMaxHeight(),
        )
    }
}

// ───────────────────────── BenchScene Canvas ─────────────────────────

@Composable
private fun BenchScene(
    switchOn: Boolean,
    turns: Int,
    current: Float,
    core: Core,
    strengthN: Float,
    chevronPhase: Float,
    clipSlots: List<Int>,
) {
    val t = LL.tokens
    val wireCopper = Color(0xFFD97706)
    val accent = t.accent500

    Canvas(Modifier.fillMaxSize().padding(8.dp)) {
        val w = size.width
        val h = size.height

        // Nail centre
        val nailCx = w * 0.55f
        val nailCy = h * 0.45f
        val nailLen = w * 0.40f
        val nailH = 20f
        val leftEnd = Offset(nailCx - nailLen / 2f, nailCy)
        val rightEnd = Offset(nailCx + nailLen / 2f, nailCy)

        // Battery + wire path geometry
        val batteryLeft = w * 0.05f
        val batteryRight = batteryLeft + 50f
        val batteryTop = h * 0.20f
        val batteryBot = h * 0.40f
        val batteryMidY = (batteryTop + batteryBot) / 2f
        val switchRight = w * 0.95f
        val switchLeft = switchRight - 50f
        val switchY = h * 0.30f

        val wireColor = if (t.isDark) Color(0xFFD4D4D8) else Color(0xFF3F3F46)

        // Wire path: battery top → left of coil; right of coil → switch → battery bottom
        // Top wire — battery to left coil end (slightly above the nail centre to avoid the coil)
        val coilLeftX = leftEnd.x
        val coilRightX = rightEnd.x
        val topWireY = h * 0.18f
        // Battery up
        drawLine(wireColor,
            Offset((batteryLeft + batteryRight) / 2f, batteryTop),
            Offset((batteryLeft + batteryRight) / 2f, topWireY),
            strokeWidth = 3f, cap = StrokeCap.Round)
        // Across to coil left
        drawLine(wireColor,
            Offset((batteryLeft + batteryRight) / 2f, topWireY),
            Offset(coilLeftX - 18f, topWireY),
            strokeWidth = 3f, cap = StrokeCap.Round)
        // Down to coil start
        drawLine(wireColor,
            Offset(coilLeftX - 18f, topWireY),
            Offset(coilLeftX - 18f, nailCy - nailH),
            strokeWidth = 3f, cap = StrokeCap.Round)
        // Right side of coil to switch
        drawLine(wireColor,
            Offset(coilRightX + 18f, nailCy - nailH),
            Offset(coilRightX + 18f, switchY),
            strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(wireColor,
            Offset(coilRightX + 18f, switchY),
            Offset(switchLeft, switchY),
            strokeWidth = 3f, cap = StrokeCap.Round)
        // Switch back down
        drawLine(wireColor,
            Offset(switchRight, switchY),
            Offset(switchRight, batteryBot + 10f),
            strokeWidth = 3f, cap = StrokeCap.Round)
        // Bottom return
        drawLine(wireColor,
            Offset(switchRight, batteryBot + 10f),
            Offset((batteryLeft + batteryRight) / 2f, batteryBot + 10f),
            strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(wireColor,
            Offset((batteryLeft + batteryRight) / 2f, batteryBot + 10f),
            Offset((batteryLeft + batteryRight) / 2f, batteryBot),
            strokeWidth = 3f, cap = StrokeCap.Round)

        // Battery body
        drawRect(
            color = if (switchOn) accent else Color(0xFF52525B),
            topLeft = Offset(batteryLeft, batteryTop),
            size = Size(batteryRight - batteryLeft, batteryBot - batteryTop),
        )
        drawRect(
            color = wireColor,
            topLeft = Offset(batteryLeft, batteryTop),
            size = Size(batteryRight - batteryLeft, batteryBot - batteryTop),
            style = Stroke(1.5f),
        )
        // + and - terminals
        val terminalY1 = batteryTop + 8f
        val terminalY2 = batteryBot - 8f
        drawLine(Color.White, Offset(batteryLeft + 6f, terminalY1),
            Offset(batteryRight - 6f, terminalY1), 1.5f)
        drawLine(Color.White, Offset(batteryLeft + 18f, terminalY1 - 4f),
            Offset(batteryLeft + 18f, terminalY1 + 4f), 1.5f)
        drawLine(Color.White, Offset(batteryLeft + 6f, terminalY2),
            Offset(batteryRight - 6f, terminalY2), 1.5f)

        // Switch
        drawCircle(wireColor, 4f, Offset(switchLeft, switchY))
        drawCircle(wireColor, 4f, Offset(switchRight, switchY))
        if (switchOn) {
            drawLine(accent, Offset(switchLeft, switchY), Offset(switchRight, switchY),
                strokeWidth = 4f, cap = StrokeCap.Round)
        } else {
            drawLine(wireColor, Offset(switchLeft, switchY),
                Offset(switchRight - 6f, switchY - 16f),
                strokeWidth = 4f, cap = StrokeCap.Round)
        }

        // Nail (drawn UNDER the coil, so coil overlaps)
        drawNail(leftEnd, rightEnd, nailH, core, t.isDark)

        // Coil
        val turnVisCount = when (turns) {
            10 -> 6; 20 -> 8; 50 -> 10; else -> 13
        }
        drawCoil(
            leftEnd = leftEnd,
            rightEnd = rightEnd,
            nailH = nailH,
            turnCount = turnVisCount,
            wireColor = wireCopper,
            on = switchOn,
            chevronPhase = chevronPhase,
            accent = accent,
        )

        // Field arcs from right (N) to left (S)
        if (strengthN > 0.05f) {
            drawFieldArcs(
                leftEnd = leftEnd,
                rightEnd = rightEnd,
                strengthN = strengthN,
                color = accent.copy(alpha = (0.20f + strengthN * 0.55f).coerceAtMost(0.85f)),
            )
        }

        // Pole labels (only when on)
        if (switchOn) {
            // N at right end, S at left end
            drawPoleLabel(rightEnd, "N", t)
            drawPoleLabel(leftEnd, "S", t)
        }

        // Paperclip tray + flying clips
        val trayY = h * 0.86f
        val trayXStart = w * 0.10f
        val trayXEnd = w * 0.90f
        val slotSpacing = (trayXEnd - trayXStart) / (MAX_CLIPS - 1)
        // Tray outline
        drawLine(wireColor.copy(alpha = 0.4f),
            Offset(trayXStart - 10f, trayY + 12f),
            Offset(trayXEnd + 10f, trayY + 12f),
            strokeWidth = 1.2f)

        // Draw clips — at-tray ones along the bottom; anchored ones at the nail ends
        var leftAnchorCount = 0
        var rightAnchorCount = 0
        clipSlots.forEachIndexed { i, state ->
            when (state) {
                SLOT_AT_TRAY -> {
                    val cx = trayXStart + i * slotSpacing
                    drawClip(Offset(cx, trayY), color = Color(0xFFA1A1AA))
                }
                SLOT_ANCHORED_LEFT -> {
                    val pos = anchorPos(leftEnd, leftAnchorCount, isLeft = true, nailH = nailH)
                    drawClip(pos, color = Color(0xFFA1A1AA))
                    leftAnchorCount++
                }
                SLOT_ANCHORED_RIGHT -> {
                    val pos = anchorPos(rightEnd, rightAnchorCount, isLeft = false, nailH = nailH)
                    drawClip(pos, color = Color(0xFFA1A1AA))
                    rightAnchorCount++
                }
            }
        }
    }
}

private fun anchorPos(end: Offset, idx: Int, isLeft: Boolean, nailH: Float): Offset {
    // Stack clips at the nail tip, fanning slightly outward as more anchor
    val outX = if (isLeft) -1f else 1f
    val baseX = end.x + outX * (10f + (idx / 3) * 12f)
    val baseY = end.y + ((idx % 3) - 1) * (nailH * 0.9f)
    return Offset(baseX, baseY)
}

private fun DrawScope.drawNail(left: Offset, right: Offset, h: Float, core: Core, isDark: Boolean) {
    val cy = (left.y + right.y) / 2f
    val length = right.x - left.x
    // Head (rectangle, slightly bigger than shaft)
    val headW = 20f
    val headH = h * 1.6f
    when (core) {
        Core.Iron -> {
            // Solid darker gray
            val body = Color(0xFF6B7280)
            val edge = Color(0xFF374151)
            drawRect(body, topLeft = Offset(left.x - headW, cy - headH / 2f),
                size = Size(headW, headH))
            drawRect(edge, topLeft = Offset(left.x - headW, cy - headH / 2f),
                size = Size(headW, headH), style = Stroke(1.2f))
            // Shaft
            drawRect(body, topLeft = Offset(left.x, cy - h / 2f),
                size = Size(length, h))
            drawRect(edge, topLeft = Offset(left.x, cy - h / 2f),
                size = Size(length, h), style = Stroke(1.2f))
            // Tip triangle
            val tipPath = Path().apply {
                moveTo(right.x, cy - h / 2f)
                lineTo(right.x + 14f, cy)
                lineTo(right.x, cy + h / 2f)
                close()
            }
            drawPath(tipPath, body)
            drawPath(tipPath, edge, style = Stroke(1.2f))
        }
        Core.Steel -> {
            val body = Color(0xFF9CA3AF)
            val edge = Color(0xFF4B5563)
            val shine = Color(0xFFE5E7EB)
            drawRect(body, topLeft = Offset(left.x - headW, cy - headH / 2f),
                size = Size(headW, headH))
            drawRect(edge, topLeft = Offset(left.x - headW, cy - headH / 2f),
                size = Size(headW, headH), style = Stroke(1.2f))
            drawRect(body, topLeft = Offset(left.x, cy - h / 2f),
                size = Size(length, h))
            // Shine stripe along top edge of shaft
            drawLine(shine,
                Offset(left.x + 2f, cy - h / 2f + 3f),
                Offset(right.x - 2f, cy - h / 2f + 3f),
                strokeWidth = 1.5f)
            drawRect(edge, topLeft = Offset(left.x, cy - h / 2f),
                size = Size(length, h), style = Stroke(1.2f))
            val tipPath = Path().apply {
                moveTo(right.x, cy - h / 2f)
                lineTo(right.x + 14f, cy)
                lineTo(right.x, cy + h / 2f)
                close()
            }
            drawPath(tipPath, body)
            drawPath(tipPath, edge, style = Stroke(1.2f))
        }
        Core.Air -> {
            // Dashed outline only — no fill
            val edge = if (isDark) Color(0xFFA1A1AA) else Color(0xFF6B7280)
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
            val shaftPath = Path().apply {
                moveTo(left.x, cy - h / 2f)
                lineTo(right.x, cy - h / 2f)
                lineTo(right.x + 14f, cy)
                lineTo(right.x, cy + h / 2f)
                lineTo(left.x, cy + h / 2f)
                close()
            }
            drawPath(shaftPath, edge, style = Stroke(1.5f, pathEffect = dash))
            val headPath = Path().apply {
                moveTo(left.x - headW, cy - headH / 2f)
                lineTo(left.x, cy - headH / 2f)
                lineTo(left.x, cy + headH / 2f)
                lineTo(left.x - headW, cy + headH / 2f)
                close()
            }
            drawPath(headPath, edge, style = Stroke(1.5f, pathEffect = dash))
            // Small label
            // (Text drawing in Canvas is awkward — we rely on the readout
            //  card for the explicit "Air core" label.)
        }
    }
}

private fun DrawScope.drawCoil(
    leftEnd: Offset,
    rightEnd: Offset,
    nailH: Float,
    turnCount: Int,
    wireColor: Color,
    on: Boolean,
    chevronPhase: Float,
    accent: Color,
) {
    val cy = (leftEnd.y + rightEnd.y) / 2f
    val coilStartX = leftEnd.x + 4f
    val coilEndX = rightEnd.x - 4f
    val coilLen = coilEndX - coilStartX
    val gap = coilLen / (turnCount - 1).coerceAtLeast(1)
    val loopR = nailH * 1.05f

    // Draw each loop: front arc (under the nail), back arc (over the nail)
    for (i in 0 until turnCount) {
        val cx = coilStartX + i * gap
        // Back arc (over the nail, drawn first so front comes over)
        drawArc(
            color = wireColor.copy(alpha = 0.75f),
            startAngle = 180f, sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - loopR * 0.45f, cy - loopR),
            size = Size(loopR * 0.9f, loopR * 2f),
            style = Stroke(2.2f),
        )
    }
    for (i in 0 until turnCount) {
        val cx = coilStartX + i * gap
        // Front arc (under the nail)
        drawArc(
            color = wireColor,
            startAngle = 0f, sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - loopR * 0.45f, cy - loopR),
            size = Size(loopR * 0.9f, loopR * 2f),
            style = Stroke(2.5f),
        )
    }

    // Animated chevrons along the bottom of the coil (where current "flows visible")
    if (on) {
        val y = cy + loopR + 4f
        var x = coilStartX + (chevronPhase % gap)
        while (x < coilEndX) {
            drawLine(accent, Offset(x - 6f, y - 4f), Offset(x, y), 2f, cap = StrokeCap.Round)
            drawLine(accent, Offset(x - 6f, y + 4f), Offset(x, y), 2f, cap = StrokeCap.Round)
            x += gap
        }
    }
}

private fun DrawScope.drawFieldArcs(
    leftEnd: Offset,
    rightEnd: Offset,
    strengthN: Float,
    color: Color,
) {
    val cx = (leftEnd.x + rightEnd.x) / 2f
    val cy = (leftEnd.y + rightEnd.y) / 2f
    val baseR = (rightEnd.x - leftEnd.x) / 2f
    val arcCount = 5
    for (i in 1..arcCount) {
        val r = baseR + i * 14f + strengthN * 8f
        // Arc above the nail
        drawArc(
            color = color,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(1.5f),
        )
        // small arrow head at the leftmost point of each arc (south pole)
        val sxL = cx - r
        val sxR = cx + r
        drawLine(color, Offset(sxL, cy), Offset(sxL + 4f, cy - 4f), 1.5f)
        drawLine(color, Offset(sxL, cy), Offset(sxL + 4f, cy + 4f), 1.5f)
        drawLine(color, Offset(sxR, cy), Offset(sxR - 4f, cy - 4f), 1.5f)
        drawLine(color, Offset(sxR, cy), Offset(sxR - 4f, cy + 4f), 1.5f)
    }
}

private fun DrawScope.drawPoleLabel(end: Offset, label: String, t: com.learnlab.design.LearnLabTokens) {
    // We draw a small filled circle as the pole marker; the actual letter is
    // best rendered in a Compose Text overlay since Canvas text needs a
    // TextMeasurer. For simplicity we draw the marker only — the polarity
    // is implied by the field-arc direction and by the readout card.
    val ringColor = if (label == "N") Color(0xFFE11D48) else Color(0xFF3B82F6)
    drawCircle(ringColor.copy(alpha = 0.85f), 7f, end)
    drawCircle(Color.White, 7f, end, style = Stroke(1.5f))
    @Suppress("UNUSED_PARAMETER") val unused = t
}

private fun DrawScope.drawClip(centre: Offset, color: Color) {
    // Paperclip — drawn as two stacked rounded rectangles (an inner U and outer U)
    val w = 11f
    val h = 5f
    drawRoundRect(
        color,
        topLeft = Offset(centre.x - w / 2f, centre.y - h / 2f),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f),
        style = Stroke(1.5f),
    )
    drawRoundRect(
        color.copy(alpha = 0.75f),
        topLeft = Offset(centre.x - w / 2f + 1.5f, centre.y - h / 2f + 1.5f),
        size = Size(w - 3f, h - 3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius((h - 3f) / 2f),
        style = Stroke(1f),
    )
}

// ───────────────────────── FooterHint ─────────────────────────

@Composable
private fun FooterHint(switchOn: Boolean, clipsHeld: Int) {
    val t = LL.tokens
    val text = when {
        !switchOn -> "Switch on the circuit. Watch how many paper clips the nail can lift."
        clipsHeld == 0 -> "Field is too weak to pick up clips. Try more turns, more cells, or an iron core."
        clipsHeld < 6 -> "Weak field. Increase the turns or cells, or switch to an iron core."
        clipsHeld < 12 -> "Field is moderate. Iron core + more turns gives the strongest pull."
        else -> "Strong field — the nail is fully loaded with clips."
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

// ───────────────────────── ControlsPanel ─────────────────────────

@Composable
private fun ControlsPanel(
    switchOn: Boolean,
    onToggleSwitch: () -> Unit,
    turns: Int,
    onTurnsChange: (Int) -> Unit,
    current: Float,
    onCurrentChange: (Float) -> Unit,
    core: Core,
    onCoreChange: (Core) -> Unit,
    strengthN: Float,
    clipsHeld: Int,
    appsOpen: Boolean,
    onToggleApps: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier,
) {
    val t = LL.tokens
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Controls card
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
            LLText("TURNS", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp)
            TurnsPicker(turns, onTurnsChange)
            LLText("CORE", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp)
            CorePicker(core, onCoreChange)
            LLSlider(
                label = "Current",
                value = current,
                onValueChange = onCurrentChange,
                min = 0.5f, max = 3.0f,
                step = 0.5f, unit = "× cell",
                enabled = switchOn,
                valueFormat = { "%.1f".format(it) },
            )
            StrengthMeter(strengthN)
            GhostButton(label = "Reset", onClick = onReset, modifier = Modifier.fillMaxWidth())
        }
        ReadoutCard(switchOn, turns, current, core, clipsHeld)
        ApplicationsCard(open = appsOpen, onToggle = onToggleApps)
    }
}

@Composable
private fun TurnsPicker(turns: Int, onChange: (Int) -> Unit) {
    val t = LL.tokens
    val options = listOf(10, 20, 50, 100)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { opt ->
            val selected = turns == opt
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) t.accent500 else t.surface)
                    .border(1.dp,
                        if (selected) t.accent500 else t.lineStrong,
                        RoundedCornerShape(6.dp))
                    .clickable { onChange(opt) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LLText(
                    "$opt",
                    color = if (selected) Color.White else t.ink200,
                    size = 13.sp,
                    weight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CorePicker(core: Core, onChange: (Core) -> Unit) {
    val t = LL.tokens
    val options = listOf(Core.Iron to "Iron", Core.Steel to "Steel", Core.Air to "Air")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (opt, label) ->
            val selected = core == opt
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) t.accent500 else t.surface)
                    .border(1.dp,
                        if (selected) t.accent500 else t.lineStrong,
                        RoundedCornerShape(6.dp))
                    .clickable { onChange(opt) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LLText(
                    label,
                    color = if (selected) Color.White else t.ink200,
                    size = 12.sp,
                    weight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StrengthMeter(strengthN: Float) {
    val t = LL.tokens
    val label = when {
        strengthN < 0.05f -> "—"
        strengthN < 0.30f -> "Weak"
        strengthN < 0.60f -> "Moderate"
        strengthN < 0.85f -> "Strong"
        else -> "Very strong"
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("STRENGTH", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f))
            LLText(label, color = t.accent700, size = 11.sp, weight = FontWeight.SemiBold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(t.surface3),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(strengthN)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(t.accent500),
            )
        }
    }
}

@Composable
private fun ReadoutCard(switchOn: Boolean, turns: Int, current: Float, core: Core, clipsHeld: Int) {
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
        StatLine2("Switch", if (switchOn) "ON" else "OFF")
        StatLine2("Turns", "$turns")
        StatLine2("Current", "%.1f × cell".format(current))
        StatLine2("Core", when (core) {
            Core.Iron -> "Iron (strongest)"
            Core.Steel -> "Steel"
            Core.Air -> "Air (no core)"
        })
        StatLine2("Clips held", "$clipsHeld / $MAX_CLIPS")
    }
}

@Composable
private fun StatLine2(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LLText(label, color = Color.White.copy(alpha = 0.70f), size = 11.sp,
            modifier = Modifier.weight(1f))
        LLText(value, color = Color.White, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun ApplicationsCard(open: Boolean, onToggle: () -> Unit) {
    val t = LL.tokens
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("APPLICATIONS", color = t.accent700, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f))
            LLText(if (open) "▾" else "▸", color = t.ink400, size = 14.sp,
                weight = FontWeight.Bold)
        }
        if (open) {
            AppRow("🔔", "Electric bell",
                "An electromagnet pulls a striker against a gong; the motion breaks the circuit, the spring resets it, and it rings.")
            AppRow("🧲", "MRI scanner",
                "Superconducting electromagnets create the huge magnetic field that aligns hydrogen protons in your body for imaging.")
            AppRow("🏗️", "Scrap-metal crane",
                "Switch on to grip a load of iron; switch off to drop it. Permanent magnets can't do this — that's why electromagnets are used.")
            AppRow("🚄", "Maglev train",
                "Pulsed electromagnets levitate and propel the train above the rail — no wheels, no friction.")
        } else {
            LLText("Tap to see where electromagnets are used →",
                color = t.ink500, size = 12.sp)
        }
    }
}

@Composable
private fun AppRow(icon: String, title: String, body: String) {
    val t = LL.tokens
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        LLText(icon, size = 18.sp, color = Color.Unspecified)
        Column(Modifier.weight(1f)) {
            LLText(title, color = t.ink50, size = 12.sp, weight = FontWeight.Bold)
            LLText(body, color = t.ink400, size = 11.sp, lineHeight = 15.sp)
        }
    }
}
