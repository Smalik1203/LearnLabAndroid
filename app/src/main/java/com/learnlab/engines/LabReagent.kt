package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
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
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlinx.coroutines.delay

/**
 * Native port of src/runtime/engines/LabReagent.tsx. Predict → apply →
 * reveal loop, shared between Iodine, Fat, and Protein tests.
 */

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

    Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        // Left: lab bench grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f).padding(end = 16.dp)) {
                    LLText("LAB BENCH", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(4.dp))
                    LLText(reagent.prompt, color = t.ink400, size = 13.sp, lineHeight = 18.sp)
                }
                GhostButton(
                    label = "Reset",
                    onClick = {
                        foods.forEach { states[it.id] = FoodState() }
                        focusId = foods.first().id
                        controls.onProgress(0f)
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
            // 4-column grid of samples
            val rows = foods.chunked(4)
            rows.forEachIndexed { i, row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                if (i < rows.lastIndex) Spacer(Modifier.height(12.dp))
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
    val borderColor by animateColorAsState(if (focused) t.accent500 else t.line, label = "sample-b")
    val bg = if (focused) t.accent50 else t.surface2
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Petri dish circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(t.surface3)
                .border(1.dp, t.lineStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            LLText(food.emoji, size = 28.sp, color = Color.Unspecified)
            if (revealed) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (food.positive) reagent.positiveColor else reagent.negativeColor),
                )
            }
        }
        LLText(food.name, color = t.ink200, size = 12.sp, weight = FontWeight.SemiBold)
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
            state.phase == Phase.Predict -> Color(0xFFF5F3EF)
            food.positive -> reagent.positiveColor
            else -> reagent.negativeColor
        },
        animationSpec = tween(800),
        label = "liquid-color",
    )

    LaunchedEffect(food.id, state.phase) {
        if (state.phase == Phase.Reacting) {
            delay(1100)
            onAfterReact()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface3),
                contentAlignment = Alignment.Center,
            ) { LLText(food.emoji, size = 22.sp, color = Color.Unspecified) }
            Spacer(Modifier.width(12.dp))
            Column {
                LLText("SAMPLE", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                LLText(food.name, color = t.ink50, size = 18.sp, weight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(12.dp))

        // ── Illustration ──────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(10.dp)),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (reagent.isFatPaperTest) {
                    drawFatPaperIllustration(reagent.positiveColor, state.phase, food.positive)
                } else {
                    drawTestTubeIllustration(liquidColor, state.phase, reagent.reagentDropColor)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        when (state.phase) {
            Phase.Predict -> {
                LLText(
                    "Predict — will the ${reagent.reagentName} react with this sample?",
                    color = t.ink200, size = 14.sp,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PredictChoice("Yes, will react", state.prediction == true) { onPredict(true) }
                    PredictChoice("No change", state.prediction == false) { onPredict(false) }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    label = "${reagent.actionLabel} →",
                    onClick = onApply,
                    enabled = state.prediction != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Phase.Reacting -> {
                LLText("Applying ${reagent.reagentName}…", color = t.ink400, size = 14.sp)
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
                            label = if (state.prediction == food.positive) "Prediction matched" else "Prediction off",
                            positive = state.prediction == food.positive,
                            soft = true,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LLText(food.note, color = t.ink400, size = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                SecondaryButton(
                    label = "Next sample →",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PredictChoice(label: String, active: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    val border = if (active) t.accent500 else t.lineStrong
    val bg = if (active) t.accent50 else t.surface2
    val fg = if (active) t.accent700 else t.ink200
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) { LLText(label, color = fg, size = 14.sp, weight = FontWeight.Medium) }
}

@Composable
private fun Badge(label: String, positive: Boolean, soft: Boolean = false) {
    val t = LL.tokens
    val (bg, fg) = when {
        soft && positive -> t.accent50 to t.accent700
        soft && !positive -> t.rose50 to t.rose700
        positive -> t.ink50 to t.bgDeep
        else -> t.amber50 to t.amber700
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) { LLText(label.uppercase(), color = fg, size = 10.sp,
        weight = FontWeight.Bold, letterSpacing = 1.4.sp) }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Observation log", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
            LLText("$tested/${foods.size} · $correct matched", color = t.ink500, size = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        LazyColumn {
            items(foods, key = { it.id }) { f ->
                val s = states.getValue(f.id)
                val isRevealed = s.phase == Phase.Revealed
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(f.emoji, size = 16.sp, color = Color.Unspecified)
                    Spacer(Modifier.width(8.dp))
                    LLText(f.name, color = t.ink200, size = 13.sp, modifier = Modifier.weight(1f))
                    Pill(
                        label = when {
                            s.prediction == null -> "—"
                            s.prediction -> reagent.positiveBadge
                            else -> reagent.negativeBadge
                        },
                        positive = s.prediction == true,
                    )
                    Spacer(Modifier.width(6.dp))
                    Pill(
                        label = when {
                            !isRevealed -> "—"
                            f.positive -> reagent.positiveBadge
                            else -> reagent.negativeBadge
                        },
                        positive = isRevealed && f.positive,
                    )
                    Spacer(Modifier.width(6.dp))
                    when {
                        !isRevealed || s.prediction == null -> LLText(
                            "·", color = t.ink600, size = 14.sp, weight = FontWeight.Bold,
                        )
                        s.prediction == f.positive -> Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Correct",
                            tint = t.accent700,
                            modifier = Modifier.size(18.dp),
                        )
                        else -> Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Incorrect",
                            tint = t.rose600,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            }
        }
    }
    // unused guard
    @Suppress("UNUSED_EXPRESSION") Brush.linearGradient(listOf(Color.Black, Color.Black))
}

@Composable
private fun Pill(label: String, positive: Boolean) {
    val t = LL.tokens
    val (bg, fg) = when {
        label == "—" -> t.surface3 to t.ink500
        positive -> t.ink50 to t.bgDeep
        else -> t.amber50 to t.amber700
    }
    Box(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label.uppercase(), color = fg, size = 10.sp,
            weight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

// ──────────────────────── lab illustrations ────────────────────────

private fun DrawScope.drawTestTubeIllustration(
    liquidColor: Color,
    phase: Phase,
    dropColor: Color,
) {
    val w = size.width
    val h = size.height

    // Tube geometry — centered
    val tubeW = w * 0.13f
    val radius = tubeW / 2f
    val cx = w * 0.50f
    val tL = cx - radius
    val tR = cx + radius
    val tubeTop = h * 0.26f
    val bodyBottom = h * 0.84f
    val tubeTip = bodyBottom + radius
    // k for bezier quarter-circle approximation
    val k = radius * 0.5523f

    // Liquid fill (lower ~58% of tube body)
    val fillTop = tubeTop + (bodyBottom - tubeTop) * 0.42f
    val liquidPath = Path().apply {
        moveTo(tL, fillTop)
        lineTo(tR, fillTop)
        lineTo(tR, bodyBottom)
        cubicTo(tR, bodyBottom + k, cx + k, tubeTip, cx, tubeTip)
        cubicTo(cx - k, tubeTip, tL, bodyBottom + k, tL, bodyBottom)
        close()
    }
    drawPath(liquidPath, liquidColor)

    // Tube outline (open top, curved bottom)
    val tubePath = Path().apply {
        moveTo(tL, tubeTop)
        lineTo(tL, bodyBottom)
        cubicTo(tL, bodyBottom + k, cx - k, tubeTip, cx, tubeTip)
        cubicTo(cx + k, tubeTip, tR, bodyBottom + k, tR, bodyBottom)
        lineTo(tR, tubeTop)
    }
    drawPath(tubePath, Color(0xFF64748B), style = Stroke(2f, cap = StrokeCap.Round))

    // Glass inner highlight strip
    drawRect(
        Color(0xFFEFF6FF).copy(alpha = 0.38f),
        topLeft = Offset(tL + 2f, fillTop + 2f),
        size = Size(tubeW * 0.22f, (bodyBottom - fillTop) * 0.9f),
    )

    // Graduation marks on left wall
    for (i in 1..3) {
        val gY = tubeTop + (bodyBottom - tubeTop) * (i * 0.23f)
        drawLine(Color(0xFF94A3B8), Offset(tL, gY), Offset(tL + tubeW * 0.26f, gY), 1.5f)
    }

    // Dropper rubber bulb (always visible above tube)
    val bulbTop = h * 0.03f
    val bulbH = h * 0.15f
    val bulbBottom = bulbTop + bulbH
    drawOval(
        dropColor.copy(alpha = 0.82f),
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
            cubicTo(cx + 5f, needleBottom + 5f, cx + 6f, needleBottom + 11f, cx, needleBottom + 15f)
            cubicTo(cx - 6f, needleBottom + 11f, cx - 5f, needleBottom + 5f, cx, needleBottom)
        }
        drawPath(dropPath, dropColor)
    }

    // Reaction bubbles (Reacting phase only)
    if (phase == Phase.Reacting) {
        listOf(
            Offset(cx - tubeW * 0.20f, h * 0.68f),
            Offset(cx + tubeW * 0.12f, h * 0.75f),
            Offset(cx - tubeW * 0.08f, h * 0.81f),
        ).forEach { b ->
            drawCircle(Color.White.copy(alpha = 0.60f), 5f, b)
            drawCircle(Color(0xFF94A3B8).copy(alpha = 0.55f), 5f, b, style = Stroke(1f))
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
    val pL = w * 0.28f
    val pT = h * 0.09f
    val pW = w * 0.44f
    val pH = h * 0.83f

    // Paper background
    drawRect(Color(0xFFF9F7F3), topLeft = Offset(pL, pT), size = Size(pW, pH))
    // Faint ruled lines
    for (i in 1..7) {
        drawLine(
            Color(0xFFE5E5E5),
            Offset(pL + 8f, pT + pH * (i / 8f)),
            Offset(pL + pW - 8f, pT + pH * (i / 8f)),
            strokeWidth = 0.8f,
        )
    }
    // Paper border
    drawRect(Color(0xFFD1D5DB), topLeft = Offset(pL, pT), size = Size(pW, pH), style = Stroke(1.5f))

    // Grease / dry spot (only after reagent applied)
    if (phase != Phase.Predict) {
        val spotCx = w * 0.50f
        val spotCy = h * 0.50f
        val spotR = pW * 0.30f
        if (foodIsPositive) {
            // Translucent oily patch
            drawCircle(spotColor.copy(alpha = 0.48f), spotR, Offset(spotCx, spotCy))
            drawCircle(spotColor.copy(alpha = 0.22f), spotR * 0.55f, Offset(spotCx, spotCy))
            // Light-through highlight on revealed phase
            if (phase == Phase.Revealed) {
                drawCircle(
                    Color.White.copy(alpha = 0.32f),
                    spotR * 0.36f,
                    Offset(spotCx - spotR * 0.22f, spotCy - spotR * 0.22f),
                )
            }
        } else {
            // Dry water mark — barely visible
            drawCircle(Color(0xFFE5E7EB).copy(alpha = 0.38f), pW * 0.18f, Offset(spotCx, spotCy))
        }
    }
}
