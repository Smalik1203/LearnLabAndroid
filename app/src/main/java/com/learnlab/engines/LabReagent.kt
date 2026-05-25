package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        Spacer(Modifier.height(20.dp))
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
