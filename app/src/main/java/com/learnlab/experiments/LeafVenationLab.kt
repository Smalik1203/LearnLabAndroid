package com.learnlab.experiments

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.bounceClickable
import com.learnlab.store.ExperimentControls

/**
 * Leaf Venation Lab (NCERT Activity 2.5).
 * Phase A — observe hibiscus, banana and grass side by side and meet the
 * words veins → venation → reticulate → parallel.
 * Phase B — classify the Activity 2.7 plants one at a time: the class votes
 * reticulate or parallel and the veins draw themselves in to settle it.
 */

private enum class Venation { Reticulate, Parallel }

private enum class BladeShape { Ovate, Lanceolate, Heart, BroadOblong, NarrowBlade }

private class LeafSpec(
    val name: String,
    val shape: BladeShape,
    val widthScale: Float,
    val venation: Venation,
    val fact: String,
)

// Phase A — the three leaves from book Fig 2.4, in observe order.
private val hibiscusLeaf = LeafSpec("Hibiscus", BladeShape.Ovate, 1f, Venation.Reticulate,
    "A net of fine veins spreads on both sides of one thick middle vein.")
private val bananaLeaf = LeafSpec("Banana", BladeShape.BroadOblong, 1f, Venation.Parallel,
    "Long veins run side by side from base to tip, never crossing.")
private val grassLeaf = LeafSpec("Grass", BladeShape.NarrowBlade, 1f, Venation.Parallel,
    "On a thin blade the veins still travel straight and parallel.")

// Phase B — the Activity 2.7 plant cast, so the root-type lab can reuse them.
private val leaves = listOf(
    LeafSpec("Lemongrass", BladeShape.NarrowBlade, 0.85f, Venation.Parallel,
        "A tall grassy herb — its narrow blades carry straight, side-by-side veins."),
    LeafSpec("Marigold", BladeShape.Lanceolate, 1f, Venation.Reticulate,
        "The garden genda leaf shows a branching net between its veins."),
    LeafSpec("Sadabahar", BladeShape.Ovate, 0.95f, Venation.Reticulate,
        "Periwinkle (sadabahar) has glossy leaves veined in a fine mesh."),
    LeafSpec("Chickpea", BladeShape.Ovate, 0.7f, Venation.Reticulate,
        "Chana's small leaflets are net-veined — and the plant grows a taproot."),
    LeafSpec("Wheat", BladeShape.NarrowBlade, 1f, Venation.Parallel,
        "The plant behind your roti has long blades with parallel veins."),
    LeafSpec("Maize", BladeShape.NarrowBlade, 1.7f, Venation.Parallel,
        "Maize is a tall grass — broad blades, but the veins still run parallel."),
    LeafSpec("Tulsi", BladeShape.Ovate, 0.75f, Venation.Reticulate,
        "Small leaf, same rule — tulsi's veins branch out into a fine net."),
    LeafSpec("Mango", BladeShape.Lanceolate, 1f, Venation.Reticulate,
        "Hold a mango leaf to the light and the net between the veins glows through."),
)

private val reticulateColor = Color(0xFFA855F7)
private val parallelColor = Color(0xFF0EA5E9)
private val correctColor = Color(0xFF10B981)
private val wrongColor = Color(0xFFF43F5E)

private enum class Phase { Observe, Classify, Done }

@Composable
fun LeafVenationLab(controls: ExperimentControls) {
    var phase by remember { mutableStateOf(Phase.Observe) }

    var round by remember { mutableIntStateOf(0) }
    var guess by remember { mutableStateOf<Venation?>(null) }
    var score by remember { mutableIntStateOf(0) }

    // onStep 0 = Observe phase, onStep 1 = Classify phase, onStep 2 = results.
    LaunchedEffect(phase) {
        when (phase) {
            Phase.Observe -> controls.onStep(0)
            Phase.Classify -> controls.onStep(1)
            Phase.Done -> controls.onStep(2)
        }
    }
    LaunchedEffect(phase, round) {
        val classified = if (phase == Phase.Observe) 0f else round.toFloat()
        // Treat the Observe phase as one full "round" worth of progress.
        controls.onProgress((classified + 1f) / (leaves.size + 1f))
    }
    LaunchedEffect(phase) {
        if (phase == Phase.Done) controls.onComplete(score / leaves.size.toFloat())
    }

    when (phase) {
        Phase.Observe -> {
            ObservePhase(onDone = { phase = Phase.Classify })
            return
        }
        Phase.Done -> {
            SummaryCard(score = score, onReplay = {
                round = 0; guess = null; score = 0; phase = Phase.Observe
            })
            return
        }
        Phase.Classify -> {}
    }

    ClassifyPhase(
        round = round,
        guess = guess,
        score = score,
        onGuess = { picked ->
            guess = picked
            if (picked == leaves[round].venation) score++
        },
        onNext = {
            if (round == leaves.lastIndex) phase = Phase.Done
            else { round++; guess = null }
        },
    )
}

@Composable
private fun ClassifyPhase(
    round: Int,
    guess: Venation?,
    score: Int,
    onGuess: (Venation) -> Unit,
    onNext: () -> Unit,
) {
    val t = LL.tokens

    val reveal = remember { Animatable(0f) }
    LaunchedEffect(round, guess) {
        if (guess == null) reveal.snapTo(0f)
        else reveal.animateTo(1f, tween(1600))
    }

    val leaf = leaves[round]

    Row(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Leaf stage
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot("Reticulate · net", reticulateColor)
                LegendDot("Parallel · side by side", parallelColor)
            }
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)) {
                drawLeafBlade(leaf.shape, leaf.widthScale)
                if (reveal.value > 0f) {
                    drawVeins(leaf.shape, leaf.widthScale, leaf.venation, reveal.value)
                }
            }
            LLText(leaf.name, color = t.ink50, size = 24.sp, weight = FontWeight.Bold)
            LLText(
                if (guess == null) "Veins hidden" else when (leaf.venation) {
                    Venation.Reticulate -> "Reticulate venation"
                    Venation.Parallel -> "Parallel venation"
                },
                color = if (guess == null) t.ink500 else venationColor(leaf.venation),
                size = 13.sp, weight = FontWeight.SemiBold,
            )
        }

        // Vote panel
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LLText(
                    "LEAF ${round + 1} OF ${leaves.size}",
                    color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                )
                ScoreChip(score)
            }
            LLText(
                "What will the veins of this leaf look like?",
                color = t.ink50, size = 20.sp, weight = FontWeight.SemiBold, lineHeight = 26.sp,
            )
            LLText(
                "Take a class vote, then tap the answer. The veins will draw themselves in.",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp,
            )

            VoteButton(
                title = "Reticulate",
                subtitle = "Veins branch into a net",
                color = reticulateColor,
                state = voteState(guess, Venation.Reticulate, leaf.venation),
                onClick = { if (guess == null) onGuess(Venation.Reticulate) },
            )
            VoteButton(
                title = "Parallel",
                subtitle = "Veins run side by side",
                color = parallelColor,
                state = voteState(guess, Venation.Parallel, leaf.venation),
                onClick = { if (guess == null) onGuess(Venation.Parallel) },
            )

            Spacer(Modifier.weight(1f))

            if (guess != null) {
                VerdictCard(
                    correct = guess == leaf.venation,
                    leaf = leaf,
                )
                PrimaryButton(
                    label = if (round == leaves.lastIndex) "See results" else "Next leaf",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ─────────────── Phase A — Observe and compare ─────────────── */

// Four taps walk the class through the words. Each tap reveals more.
private val observeSteps = listOf(
    ObserveStep(
        "Look closely at three leaves",
        "Hibiscus, banana and a blade of grass. Notice the fine lines running across each one.",
        revealVeins = false, highlightVein = false,
    ),
    ObserveStep(
        "Those fine lines are veins",
        "Tap to draw them in. Every leaf is criss-crossed by veins.",
        revealVeins = true, highlightVein = false,
    ),
    ObserveStep(
        "The pattern of veins is the venation",
        "Now compare the patterns. The hibiscus looks different from the banana and the grass.",
        revealVeins = true, highlightVein = false,
    ),
    ObserveStep(
        "Two patterns: reticulate and parallel",
        "Hibiscus shows a net on both sides of a thick middle vein — reticulate. Banana and grass run side by side — parallel.",
        revealVeins = true, highlightVein = true,
    ),
)

private class ObserveStep(
    val title: String,
    val body: String,
    val revealVeins: Boolean,
    val highlightVein: Boolean,
)

@Composable
private fun ObservePhase(onDone: () -> Unit) {
    val t = LL.tokens
    var step by remember { mutableIntStateOf(0) }
    val current = observeSteps[step]

    val reveal = remember { Animatable(0f) }
    LaunchedEffect(current.revealVeins) {
        if (current.revealVeins) {
            if (reveal.value < 1f) reveal.animateTo(1f, tween(1500))
        } else {
            reveal.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText(
                "OBSERVE AND COMPARE",
                color = t.ink500, size = 11.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LegendDot("Reticulate · net", reticulateColor)
                LegendDot("Parallel · side by side", parallelColor)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ObserveLeaf(hibiscusLeaf, reveal.value, current.highlightVein, Modifier.weight(1f))
            ObserveLeaf(bananaLeaf, reveal.value, current.highlightVein, Modifier.weight(1f))
            ObserveLeaf(grassLeaf, reveal.value, current.highlightVein, Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LLText(current.title, color = t.ink50, size = 22.sp, weight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    LLText(current.body, color = t.ink200, size = 14.sp, lineHeight = 20.sp)
                }
                Spacer(Modifier.width(20.dp))
                if (step == observeSteps.lastIndex) {
                    PrimaryButton(label = "Start classifying", onClick = onDone)
                } else {
                    PrimaryButton(
                        label = if (step == 0) "Reveal the veins" else "Next",
                        onClick = { step++ },
                    )
                }
            }
        }
    }
}

@Composable
private fun ObserveLeaf(
    leaf: LeafSpec,
    reveal: Float,
    highlight: Boolean,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val showLabel = highlight && reveal > 0f
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(
                if (showLabel) 2.dp else 1.dp,
                if (showLabel) venationColor(leaf.venation).copy(alpha = 0.6f) else t.line,
                RoundedCornerShape(Radius.lg),
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            drawLeafBlade(leaf.shape, leaf.widthScale)
            if (reveal > 0f) drawVeins(leaf.shape, leaf.widthScale, leaf.venation, reveal)
        }
        LLText(leaf.name, color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
        LLText(
            if (showLabel) when (leaf.venation) {
                Venation.Reticulate -> "Reticulate"
                Venation.Parallel -> "Parallel"
            } else " ",
            color = venationColor(leaf.venation),
            size = 14.sp, weight = FontWeight.SemiBold,
        )
    }
}

private fun venationColor(v: Venation) =
    if (v == Venation.Reticulate) reticulateColor else parallelColor

private enum class VoteState { Idle, Disabled, ChosenCorrect, ChosenWrong, Answer }

private fun voteState(guess: Venation?, button: Venation, actual: Venation): VoteState = when {
    guess == null -> VoteState.Idle
    guess == button && button == actual -> VoteState.ChosenCorrect
    guess == button -> VoteState.ChosenWrong
    button == actual -> VoteState.Answer
    else -> VoteState.Disabled
}

@Composable
private fun VoteButton(
    title: String,
    subtitle: String,
    color: Color,
    state: VoteState,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val borderColor = when (state) {
        VoteState.Idle -> color.copy(alpha = 0.5f)
        VoteState.ChosenCorrect -> correctColor
        VoteState.ChosenWrong -> wrongColor
        VoteState.Answer -> correctColor.copy(alpha = 0.6f)
        VoteState.Disabled -> t.line
    }
    val bg = when (state) {
        VoteState.ChosenCorrect, VoteState.Answer -> correctColor.copy(alpha = 0.10f)
        VoteState.ChosenWrong -> wrongColor.copy(alpha = 0.10f)
        else -> color.copy(alpha = 0.06f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable(enabled = state == VoteState.Idle, onClick = onClick)
            .clip(RoundedCornerShape(Radius.lg))
            .background(bg)
            .border(2.dp, borderColor, RoundedCornerShape(Radius.lg))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Column {
            LLText(title, color = t.ink50, size = 17.sp, weight = FontWeight.Bold)
            LLText(subtitle, color = t.ink400, size = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        when (state) {
            VoteState.ChosenCorrect -> LLText("✓", color = correctColor, size = 22.sp, weight = FontWeight.Bold)
            VoteState.ChosenWrong -> LLText("✗", color = wrongColor, size = 22.sp, weight = FontWeight.Bold)
            VoteState.Answer -> LLText("✓", color = correctColor.copy(alpha = 0.6f), size = 22.sp, weight = FontWeight.Bold)
            else -> {}
        }
    }
}

@Composable
private fun VerdictCard(correct: Boolean, leaf: LeafSpec) {
    val t = LL.tokens
    val accent = if (correct) correctColor else wrongColor
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(Radius.lg))
            .padding(16.dp),
    ) {
        LLText(
            if (correct) "Correct — ${leaf.name.lowercase()} is ${leaf.venation.name.lowercase()}"
            else "Not this time — ${leaf.name.lowercase()} is ${leaf.venation.name.lowercase()}",
            color = accent, size = 14.sp, weight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        LLText(leaf.fact, color = t.ink200, size = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun ScoreChip(score: Int) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(Radius.pill))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        LLText("Score $score", color = t.ink200, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        LLText(label, color = color, size = 12.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun SummaryCard(score: Int, onReplay: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LLText(
                "LEAF DETECTIVE — RESULTS",
                color = t.ink500, size = 11.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            Spacer(Modifier.height(10.dp))
            LLText(
                "$score of ${leaves.size} leaves read correctly",
                color = t.ink50, size = 24.sp, weight = FontWeight.Bold,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LegendDot("Net-like mesh → reticulate", reticulateColor)
                LegendDot("Side-by-side lines → parallel", parallelColor)
            }
            Spacer(Modifier.height(22.dp))
            SecondaryButton(label = "Play again", onClick = onReplay)
        }
    }
}

/* ─────────────── Leaf drawing ─────────────── */

private class BladeGeom(val cx: Float, val cy: Float, val leafW: Float, val leafH: Float)

private fun DrawScope.bladeGeom(shape: BladeShape, widthScale: Float): BladeGeom {
    val w = size.width
    val h = size.height
    val leafW = when (shape) {
        BladeShape.Ovate -> w * 0.55f
        BladeShape.Lanceolate -> w * 0.34f
        BladeShape.Heart -> w * 0.60f
        BladeShape.BroadOblong -> w * 0.48f
        BladeShape.NarrowBlade -> w * 0.13f
    } * widthScale
    val leafH = when (shape) {
        BladeShape.Heart -> h * 0.84f
        else -> h * 0.90f
    }
    return BladeGeom(w / 2f, h / 2f, leafW, leafH)
}

private fun bladePath(g: BladeGeom, shape: BladeShape): Path {
    val top = g.cy - g.leafH / 2f
    val bottom = g.cy + g.leafH / 2f
    return when (shape) {
        BladeShape.Ovate -> Path().apply {
            moveTo(g.cx, top)
            cubicTo(g.cx + g.leafW / 2f, g.cy - g.leafH / 2.4f,
                g.cx + g.leafW / 2f, g.cy + g.leafH / 2.4f, g.cx, bottom)
            cubicTo(g.cx - g.leafW / 2f, g.cy + g.leafH / 2.4f,
                g.cx - g.leafW / 2f, g.cy - g.leafH / 2.4f, g.cx, top)
            close()
        }
        BladeShape.Lanceolate -> Path().apply {
            moveTo(g.cx, top)
            cubicTo(g.cx + g.leafW / 2f, top + g.leafH * 0.22f,
                g.cx + g.leafW / 2f, top + g.leafH * 0.52f, g.cx, bottom)
            cubicTo(g.cx - g.leafW / 2f, top + g.leafH * 0.52f,
                g.cx - g.leafW / 2f, top + g.leafH * 0.22f, g.cx, top)
            close()
        }
        BladeShape.Heart -> Path().apply {
            moveTo(g.cx, top + g.leafH * 0.14f)
            cubicTo(g.cx + g.leafW * 0.58f, top - g.leafH * 0.06f,
                g.cx + g.leafW * 0.55f, top + g.leafH * 0.58f, g.cx, bottom)
            cubicTo(g.cx - g.leafW * 0.55f, top + g.leafH * 0.58f,
                g.cx - g.leafW * 0.58f, top - g.leafH * 0.06f, g.cx, top + g.leafH * 0.14f)
            close()
        }
        BladeShape.BroadOblong -> Path().apply {
            moveTo(g.cx, top)
            cubicTo(g.cx + g.leafW / 2f, g.cy - g.leafH / 3f,
                g.cx + g.leafW / 2f, g.cy + g.leafH / 3f, g.cx, bottom)
            cubicTo(g.cx - g.leafW / 2f, g.cy + g.leafH / 3f,
                g.cx - g.leafW / 2f, g.cy - g.leafH / 3f, g.cx, top)
            close()
        }
        BladeShape.NarrowBlade -> Path().apply {
            moveTo(g.cx, top)
            quadraticTo(g.cx + g.leafW * 0.6f, g.cy - g.leafH * 0.1f, g.cx + g.leafW * 0.2f, bottom)
            lineTo(g.cx - g.leafW * 0.2f, bottom)
            quadraticTo(g.cx - g.leafW * 0.6f, g.cy - g.leafH * 0.1f, g.cx, top)
            close()
        }
    }
}

private fun DrawScope.drawLeafBlade(shape: BladeShape, widthScale: Float) {
    val g = bladeGeom(shape, widthScale)
    val path = bladePath(g, shape)
    drawPath(path, color = Color(0xFFB2DFB2).copy(alpha = 0.4f))
    drawPath(path, color = Color(0xFF1F6B3A), style = Stroke(width = 2.5f))
}

private fun DrawScope.drawVeins(shape: BladeShape, widthScale: Float, venation: Venation, reveal: Float) {
    val g = bladeGeom(shape, widthScale)
    val color = venationColor(venation)
    val top = g.cy - g.leafH / 2f
    val bottom = g.cy + g.leafH / 2f
    val midribStart = if (shape == BladeShape.Heart) top + g.leafH * 0.14f else top

    // Midrib draws first, side veins follow
    drawLineProgressive(
        from = Offset(g.cx, midribStart + 6f),
        to = Offset(g.cx, bottom - 6f),
        color = color, stroke = 3f, t = (reveal / 0.35f).coerceAtMost(1f),
    )
    when (venation) {
        Venation.Reticulate -> {
            val sideVeins = 6
            for (i in 1..sideVeins) {
                val tNorm = i.toFloat() / (sideVeins + 1)
                val y = midribStart + (bottom - midribStart) * tNorm
                val phase = ((reveal - 0.35f) / 0.65f * (sideVeins + 1) - (i - 1)).coerceIn(0f, 1f)
                if (phase > 0f) {
                    val spread = g.leafW * 0.45f * (1f - tNorm * 0.5f)
                    val drop = g.leafH * ((1f - tNorm) * 0.25f + 0.12f) * 0.6f
                    drawLineProgressive(Offset(g.cx, y), Offset(g.cx + spread, y + drop), color, 2f, phase)
                    drawLineProgressive(Offset(g.cx, y), Offset(g.cx - spread, y + drop), color, 2f, phase)
                    // Faint mesh links between neighbouring side veins
                    if (phase > 0.6f && i < sideVeins) {
                        val sub = (phase - 0.6f) / 0.4f
                        val nextY = y + (bottom - midribStart) / (sideVeins + 1)
                        drawLine(color.copy(alpha = 0.45f * sub),
                            Offset(g.cx + spread * 0.5f, y + drop * 0.5f),
                            Offset(g.cx + spread * 0.65f, nextY),
                            strokeWidth = 1.2f, cap = StrokeCap.Round)
                        drawLine(color.copy(alpha = 0.45f * sub),
                            Offset(g.cx - spread * 0.5f, y + drop * 0.5f),
                            Offset(g.cx - spread * 0.65f, nextY),
                            strokeWidth = 1.2f, cap = StrokeCap.Round)
                    }
                }
            }
        }
        Venation.Parallel -> {
            val n = if (shape == BladeShape.NarrowBlade) 7 else 10
            val spreadFraction = if (shape == BladeShape.NarrowBlade) 0.7f else 0.85f
            for (i in 0 until n) {
                val tNorm = (i + 1).toFloat() / (n + 1)
                val phase = ((reveal - 0.3f) / 0.7f * (n + 1) - i).coerceIn(0f, 1f)
                if (phase > 0f) {
                    val offsetX = (tNorm - 0.5f) * g.leafW * spreadFraction
                    val vein = Path().apply {
                        moveTo(g.cx + offsetX, top + 12f)
                        cubicTo(
                            g.cx + offsetX * 0.95f, g.cy - g.leafH * 0.15f,
                            g.cx + offsetX * 0.95f, g.cy + g.leafH * 0.15f,
                            g.cx + offsetX * 0.8f, bottom - 12f,
                        )
                    }
                    // Fade-in stands in for length-wise drawing of a curved path
                    drawPath(vein, color = color.copy(alpha = phase),
                        style = Stroke(width = 1.6f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

private fun DrawScope.drawLineProgressive(from: Offset, to: Offset, color: Color, stroke: Float, t: Float) {
    if (t <= 0f) return
    val tip = Offset(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)
    drawLine(color = color, start = from, end = tip, strokeWidth = stroke, cap = StrokeCap.Round)
}
