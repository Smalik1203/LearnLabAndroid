package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.bounceClickable
import com.learnlab.store.ExperimentControls
import kotlinx.coroutines.delay

data class LabFood(
    val id: String,
    val name: String,
    val emoji: String,
    val positive: Boolean,
    val note: String,
)

data class ReagentConfig(
    val prompt: String,
    val reagentName: String,
    val actionLabel: String,
    val positiveLabel: String,
    val negativeLabel: String,
    val positiveColor: Color,
    val negativeColor: Color,
    val positiveBadge: String,
    val negativeBadge: String,
    val reagentDropColor: Color = Color(0xFFD97706), // iodine amber by default
    val isFatPaperTest: Boolean = false,
)

private enum class Phase { Predict, Reacting, Revealed }

private data class FoodState(val prediction: Boolean? = null, val phase: Phase = Phase.Predict)

@Composable
fun LabReagentScreen(
    foods: List<LabFood>,
    reagent: ReagentConfig,
    controls: ExperimentControls,
) {
    val t = LL.tokens

    val states: SnapshotStateMap<String, FoodState> = remember(foods) {
        mutableStateMapOf<String, FoodState>().apply { foods.forEach { put(it.id, FoodState()) } }
    }
    var focusId by remember(foods) { mutableStateOf(foods.first().id) }

    val tested by remember { derivedStateOf { states.values.count { it.phase == Phase.Revealed } } }
    val correct by remember {
        derivedStateOf {
            foods.count { f ->
                val s = states[f.id] ?: return@count false
                s.phase == Phase.Revealed && s.prediction == f.positive
            }
        }
    }

    LaunchedEffect(tested) {
        controls.onProgress(tested / foods.size.toFloat())
        if (tested == foods.size) controls.onComplete(correct / foods.size.toFloat())
    }

    val benchBg = if (t.isDark) t.surface.copy(alpha = 0.2f) else t.surface.copy(alpha = 0.7f)
    val borderBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        // Left: lab bench grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.md))
                .background(benchBg)
                .border(1.dp, borderBrush, RoundedCornerShape(Radius.md))
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f).padding(end = 16.dp)) {
                    LLText("LAB BENCH", color = t.accent700, size = 11.sp,
                        weight = FontWeight.Bold, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(4.dp))
                    LLText(reagent.prompt, color = t.ink50, size = 14.sp, lineHeight = 20.sp, weight = FontWeight.SemiBold)
                }
                GhostButton(
                    label = "Reset Bench",
                    onClick = {
                        foods.forEach { states[it.id] = FoodState() }
                        focusId = foods.first().id
                        controls.onProgress(0f)
                    },
                )
            }
            Spacer(Modifier.height(20.dp))
            // 4-column grid of samples
            val rows = foods.chunked(4)
            rows.forEachIndexed { i, row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { f ->
                        Box(modifier = Modifier.weight(1f)) {
                            Sample(
                                food = f,
                                state = states.getValue(f.id),
                                reagent = reagent,
                                focused = focusId == f.id,
                                onClick = { focusId = f.id },
                            )
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                if (i < rows.lastIndex) Spacer(Modifier.height(16.dp))
            }
        }

        // Right: focus + log
        Column(modifier = Modifier.width(400.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val focus = foods.first { it.id == focusId }
            FocusCard(
                food = focus,
                state = states.getValue(focusId),
                reagent = reagent,
                onPredict = { p -> states[focus.id] = states.getValue(focus.id).copy(prediction = p) },
                onApply = {
                    states[focus.id] = states.getValue(focus.id).copy(phase = Phase.Reacting)
                },
                onAfterReact = {
                    states[focus.id] = states.getValue(focus.id).copy(phase = Phase.Revealed)
                },
                onNext = {
                    val nxt = foods.firstOrNull { states.getValue(it.id).phase != Phase.Revealed }
                    if (nxt != null) focusId = nxt.id
                },
            )
            LogTable(foods = foods, states = states, reagent = reagent, tested = tested, correct = correct)
        }
    }
}

@Composable
private fun Sample(
    food: LabFood,
    state: FoodState,
    reagent: ReagentConfig,
    focused: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val revealed = state.phase == Phase.Revealed
    val defaultBorder = t.line.copy(alpha = 0.4f)
    val focusedBorder = t.accent500
    val borderColor by animateColorAsState(if (focused) focusedBorder else defaultBorder, label = "sample-b")
    val bg = if (focused) t.accent50.copy(alpha = 0.15f) else t.surface2.copy(alpha = 0.4f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(Radius.sm))
            .bounceClickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Petri dish circle — styled as realistic concentric glass rings
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(t.surface3.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2
                // Outer wall
                drawCircle(Color.White.copy(alpha = 0.25f), r - 1.dp.toPx(), style = Stroke(1.5.dp.toPx()))
                // Inner rim
                drawCircle(Color.White.copy(alpha = 0.12f), r - 4.dp.toPx(), style = Stroke(1.dp.toPx()))
                // Glass highlight arc top-left
                drawArc(
                    color = Color.White.copy(alpha = 0.4f),
                    startAngle = 210f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                    style = Stroke(1.5.dp.toPx())
                )
            }

            LLText(food.emoji, size = 28.sp, color = Color.Unspecified)

            if (revealed) {
                // Chemical reaction liquid spot inside dish
                val spotColor = if (food.positive) reagent.positiveColor else reagent.negativeColor
                val animatedRadius by animateFloatAsState(
                    targetValue = 24.dp.value,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 150f),
                    label = "liquidSpot"
                )
                Box(
                    modifier = Modifier
                        .size(animatedRadius.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(spotColor.copy(alpha = 0.85f), spotColor.copy(alpha = 0.6f))
                            )
                        )
                        .border(0.5.dp, spotColor.copy(alpha = 0.8f), CircleShape)
                )
            }
        }
        LLText(food.name, color = t.ink50, size = 13.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun FocusCard(
    food: LabFood,
    state: FoodState,
    reagent: ReagentConfig,
    onPredict: (Boolean) -> Unit,
    onApply: () -> Unit,
    onAfterReact: () -> Unit,
    onNext: () -> Unit,
) {
    val t = LL.tokens

    val liquidColor by animateColorAsState(
        when {
            state.phase == Phase.Predict -> Color(0xFFE2E8F0).copy(alpha = 0.3f) // clean clear water
            food.positive -> reagent.positiveColor
            else -> reagent.negativeColor
        },
        animationSpec = tween(1100, easing = EaseInOutSine),
        label = "liquid-color",
    )

    // Animated bubble positions rising for reacting state
    val infiniteTransition = rememberInfiniteTransition(label = "bubbleRise")
    val bubbleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -45f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "b1"
    )
    val bubbleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -55f,
        animationSpec = infiniteRepeatable(animation = tween(1300, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "b2"
    )

    LaunchedEffect(food.id, state.phase) {
        if (state.phase == Phase.Reacting) {
            delay(1300)
            onAfterReact()
        }
    }

    val cardBg = if (t.isDark) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.85f)
    val borderBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(cardBg)
            .border(1.dp, borderBrush, RoundedCornerShape(Radius.md))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface3.copy(alpha = 0.3f))
                    .border(1.dp, t.line.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { LLText(food.emoji, size = 22.sp, color = Color.Unspecified) }
            Spacer(Modifier.width(12.dp))
            Column {
                LLText("ACTIVE SPECIMEN", color = t.accent700, size = 11.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.8.sp)
                LLText(food.name, color = t.ink50, size = 18.sp, weight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(16.dp))

        // ── Illustration Panel ──────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(t.surface2.copy(alpha = 0.25f))
                .border(1.dp, t.line.copy(alpha = 0.3f), RoundedCornerShape(Radius.sm)),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (reagent.isFatPaperTest) {
                    drawFatPaperIllustration(reagent.positiveColor, state.phase, food.positive)
                } else {
                    drawTestTubeIllustration(liquidColor, state.phase, reagent.reagentDropColor, bubbleOffset1, bubbleOffset2)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        when (state.phase) {
            Phase.Predict -> {
                LLText(
                    "Predict result: Will the ${reagent.reagentName} react positive?",
                    color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PredictChoice("Yes, positive color", state.prediction == true, Modifier.weight(1f)) { onPredict(true) }
                    PredictChoice("No, negative/no color", state.prediction == false, Modifier.weight(1f)) { onPredict(false) }
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    label = "${reagent.actionLabel}",
                    onClick = onApply,
                    enabled = state.prediction != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Phase.Reacting -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LLText("Adding droplets of ${reagent.reagentName}...", color = t.accent700, size = 14.sp, weight = FontWeight.Bold)
                }
            }
            Phase.Revealed -> {
                Row {
                    Badge(
                        label = if (food.positive) reagent.positiveLabel else reagent.negativeLabel,
                        positive = food.positive,
                    )
                    if (state.prediction != null) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            label = if (state.prediction == food.positive) "PREDICTION CORRECT" else "PREDICTION OFF",
                            positive = state.prediction == food.positive,
                            soft = true,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LLText(food.note, color = t.ink200, size = 13.sp, lineHeight = 18.sp, weight = FontWeight.Medium)
                Spacer(Modifier.height(16.dp))
                SecondaryButton(
                    label = "Next Sample →",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PredictChoice(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val t = LL.tokens
    val border = if (active) t.accent500 else t.line.copy(alpha = 0.4f)
    val bg = if (active) t.accent50.copy(alpha = 0.15f) else t.surface2.copy(alpha = 0.2f)
    val fg = if (active) t.accent700 else t.ink200
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(Radius.sm))
            .bounceClickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { LLText(label, color = fg, size = 13.sp, weight = FontWeight.SemiBold) }
}

@Composable
private fun Badge(label: String, positive: Boolean, soft: Boolean = false) {
    val t = LL.tokens
    val (bg, fg) = when {
        soft && positive -> t.accent50.copy(alpha = 0.2f) to t.accent700
        soft && !positive -> Color(0xFFEF4444).copy(alpha = 0.1f) to Color(0xFFEF4444)
        positive -> t.accent700 to Color.White
        else -> t.amber700 to Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) { LLText(label.uppercase(), color = fg, size = 10.sp,
        weight = FontWeight.Black, letterSpacing = 1.4.sp) }
}

@Composable
private fun LogTable(
    foods: List<LabFood>,
    states: SnapshotStateMap<String, FoodState>,
    reagent: ReagentConfig,
    tested: Int,
    correct: Int,
) {
    val t = LL.tokens
    val cardBg = if (t.isDark) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.85f)
    val borderBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.md))
            .background(cardBg)
            .border(1.dp, borderBrush, RoundedCornerShape(Radius.md)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Observation Log", color = t.ink50, size = 14.sp, weight = FontWeight.Bold)
            LLText("$tested/${foods.size} Tested · $correct Match", color = t.accent700, size = 12.sp, weight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line.copy(alpha = 0.3f)))
        LazyColumn {
            items(foods, key = { it.id }) { f ->
                val s = states.getValue(f.id)
                val isRevealed = s.phase == Phase.Revealed
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(f.emoji, size = 16.sp, color = Color.Unspecified)
                    Spacer(Modifier.width(10.dp))
                    LLText(f.name, color = t.ink200, size = 13.sp, modifier = Modifier.weight(1f), weight = FontWeight.SemiBold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Pill(
                            label = when {
                                s.prediction == null -> "—"
                                s.prediction -> reagent.positiveBadge
                                else -> reagent.negativeBadge
                            },
                            positive = s.prediction == true,
                        )
                        Pill(
                            label = when {
                                !isRevealed -> "—"
                                f.positive -> reagent.positiveBadge
                                else -> reagent.negativeBadge
                            },
                            positive = isRevealed && f.positive,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        when {
                            !isRevealed || s.prediction == null -> LLText(
                                "·", color = t.ink600, size = 14.sp, weight = FontWeight.Bold,
                            )
                            s.prediction == f.positive -> Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Correct",
                                tint = t.accent500,
                                modifier = Modifier.size(16.dp),
                            )
                            else -> Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Incorrect",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line.copy(alpha = 0.3f)))
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") Brush.linearGradient(listOf(Color.Black, Color.Black))
}

@Composable
private fun Pill(label: String, positive: Boolean) {
    val t = LL.tokens
    val (bg, fg) = when {
        label == "—" -> t.surface3.copy(alpha = 0.3f) to t.ink500
        positive -> t.accent50.copy(alpha = 0.2f) to t.accent700
        else -> t.amber50.copy(alpha = 0.2f) to t.amber700
    }
    Box(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label.uppercase(), color = fg, size = 9.sp,
            weight = FontWeight.Black, letterSpacing = 1.2.sp)
    }
}

// ──────────────────────── lab illustrations ────────────────────────

private fun DrawScope.drawTestTubeIllustration(
    liquidColor: Color,
    phase: Phase,
    dropColor: Color,
    bubbleOffset1: Float,
    bubbleOffset2: Float,
) {
    val w = size.width
    val h = size.height

    // Tube geometry — centered
    val tubeW = w * 0.12f
    val radius = tubeW / 2f
    val cx = w * 0.50f
    val tL = cx - radius
    val tR = cx + radius
    val tubeTop = h * 0.24f
    val bodyBottom = h * 0.82f
    val tubeTip = bodyBottom + radius
    val k = radius * 0.5523f

    // 1. Draw Liquid Fill using vertical fluid gradients
    val fillTop = tubeTop + (bodyBottom - tubeTop) * 0.40f
    val liquidPath = Path().apply {
        moveTo(tL, fillTop)
        lineTo(tR, fillTop)
        lineTo(tR, bodyBottom)
        cubicTo(tR, bodyBottom + k, cx + k, tubeTip, cx, tubeTip)
        cubicTo(cx - k, tubeTip, tL, bodyBottom + k, tL, bodyBottom)
        close()
    }
    drawPath(
        path = liquidPath,
        brush = Brush.verticalGradient(
            colors = listOf(liquidColor.copy(alpha = 0.85f), liquidColor)
        )
    )

    // 2. Draw Tube Outline (Glossy glass style)
    val tubePath = Path().apply {
        moveTo(tL, tubeTop)
        lineTo(tL, bodyBottom)
        cubicTo(tL, bodyBottom + k, cx - k, tubeTip, cx, tubeTip)
        cubicTo(cx + k, tubeTip, tR, bodyBottom + k, tR, bodyBottom)
        lineTo(tR, tubeTop)
    }
    drawPath(tubePath, Color(0xFF64748B), style = Stroke(2.5f, cap = StrokeCap.Round))

    // 3. Highlight reflection on the glass (High-fidelity design detail)
    val highlightPath = Path().apply {
        moveTo(tR - 2.5f, tubeTop + 4f)
        lineTo(tR - 2.5f, bodyBottom)
        cubicTo(tR - 2.5f, bodyBottom + k - 1f, cx + k - 1f, tubeTip - 2f, cx, tubeTip - 2f)
    }
    drawPath(highlightPath, Color.White.copy(alpha = 0.45f), style = Stroke(1.5f))

    // 4. Tube lip/rim flare
    drawOval(
        color = Color(0xFF64748B),
        topLeft = Offset(tL - 2.dp.toPx(), tubeTop - 2.dp.toPx()),
        size = Size(tubeW + 4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(2f)
    )

    // Dropper rubber bulb (always visible above tube)
    val bulbTop = h * 0.03f
    val bulbH = h * 0.14f
    val bulbBottom = bulbTop + bulbH
    drawOval(
        dropColor.copy(alpha = 0.85f),
        topLeft = Offset(cx - tubeW * 0.55f, bulbTop),
        size = Size(tubeW * 1.10f, bulbH),
    )
    // Glass needle from bulb to near tube mouth
    val needleBottom = tubeTop - h * 0.03f
    drawLine(Color(0xFF64748B), Offset(cx, bulbBottom), Offset(cx, needleBottom), strokeWidth = 2.5f)

    // Reagent drop at needle tip (when reagent applied)
    if (phase != Phase.Predict) {
        val dropPath = Path().apply {
            moveTo(cx, needleBottom)
            cubicTo(cx + 6f, needleBottom + 5f, cx + 7f, needleBottom + 11f, cx, needleBottom + 16f)
            cubicTo(cx - 7f, needleBottom + 11f, cx - 6f, needleBottom + 5f, cx, needleBottom)
        }
        drawPath(dropPath, dropColor)
    }

    // Reaction bubbles (Rising up, reacting phase only)
    if (phase == Phase.Reacting) {
        val b1Y = h * 0.76f + bubbleOffset1
        val b2Y = h * 0.82f + bubbleOffset2
        if (b1Y > fillTop) {
            drawCircle(Color.White.copy(alpha = 0.65f), 4.5f, Offset(cx - tubeW * 0.20f, b1Y))
            drawCircle(Color(0xFF94A3B8).copy(alpha = 0.5f), 4.5f, Offset(cx - tubeW * 0.20f, b1Y), style = Stroke(0.8f))
        }
        if (b2Y > fillTop) {
            drawCircle(Color.White.copy(alpha = 0.65f), 3.5f, Offset(cx + tubeW * 0.15f, b2Y))
            drawCircle(Color(0xFF94A3B8).copy(alpha = 0.5f), 3.5f, Offset(cx + tubeW * 0.15f, b2Y), style = Stroke(0.8f))
        }
    }
}

private fun DrawScope.drawFatPaperIllustration(
    spotColor: Color,
    phase: Phase,
    foodIsPositive: Boolean,
) {
    val w = size.width
    val h = size.height

    // Paper sheet centered in canvas
    val pL = w * 0.30f
    val pT = h * 0.08f
    val pW = w * 0.40f
    val pH = h * 0.84f

    // Paper background — textured light parchment
    drawRect(Color(0xFFFCFAF7), topLeft = Offset(pL, pT), size = Size(pW, pH))
    // Faint grid guidelines
    for (i in 1..8) {
        drawLine(
            Color(0xFFF1ECE4),
            Offset(pL + 8f, pT + pH * (i / 9f)),
            Offset(pL + pW - 8f, pT + pH * (i / 9f)),
            strokeWidth = 1f,
        )
    }
    // Paper border
    drawRect(Color(0xFFD1D5DB).copy(alpha = 0.7f), topLeft = Offset(pL, pT), size = Size(pW, pH), style = Stroke(1.5f))

    // Grease spot (represented as premium radial opacity gradients)
    if (phase != Phase.Predict) {
        val spotCx = w * 0.50f
        val spotCy = h * 0.50f
        val spotR = pW * 0.28f
        if (foodIsPositive) {
            // Translucent grease patch
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(spotColor.copy(alpha = 0.65f), spotColor.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(spotCx, spotCy),
                    radius = spotR
                ),
                radius = spotR,
                center = Offset(spotCx, spotCy)
            )
            // Shiny highlight offset on grease
            if (phase == Phase.Revealed) {
                drawCircle(
                    Color.White.copy(alpha = 0.4f),
                    spotR * 0.3f,
                    Offset(spotCx - spotR * 0.25f, spotCy - spotR * 0.25f),
                )
            }
        } else {
            // Dry patch (faint white/grey circle that fades completely)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE5E7EB).copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(spotCx, spotCy),
                    radius = pW * 0.16f
                ),
                radius = pW * 0.16f,
                center = Offset(spotCx, spotCy)
            )
        }
    }
}
