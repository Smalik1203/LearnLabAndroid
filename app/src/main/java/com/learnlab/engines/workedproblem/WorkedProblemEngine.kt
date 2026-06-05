package com.learnlab.engines.workedproblem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.Card
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton

/**
 * Renders a JSON-driven worked-problem walkthrough.
 *
 * Layout (landscape, tablet) — two columns:
 *   ┌─────────────────────┬───────────────────────┐
 *   │                     │  STEP N OF M          │
 *   │    DIAGRAM          │  prompt               │
 *   │    (top of left,    │  reveal / answer      │
 *   │     weighted)       │                       │
 *   │                     │  (when last step is   │
 *   │                     │   revealed, this pane │
 *   ├─────────────────────┤   swaps to the final  │
 *   │  PROBLEM            │   answer card)        │
 *   │  GIVEN  /  FIND     │                       │
 *   │  (bottom of left)   │  ─────────────────    │
 *   │                     │  ‹ Prev  • • ○ ○ ○ ○  │
 *   │                     │  Next ›  · Reset      │
 *   └─────────────────────┴───────────────────────┘
 *
 * Pedagogy: teacher reads the prompt, asks the class to attempt on paper,
 * taps "Reveal step ▶" to validate. Then "Next ›" advances. After the last
 * step is revealed, the right column swaps to the final answer card.
 *
 * State is kept internal for now — there's no parent observer. If/when this
 * engine needs progress tracking or persistence, lift `currentStep` and
 * `revealed` into a hoisted `ActivityState` per CLAUDE.md §4.
 */
@Composable
fun WorkedProblemEngine(
    config: WorkedProblemConfig,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val t = LL.tokens

    var currentStep by remember { mutableStateOf(0) }
    val revealed = remember { mutableStateListOf<Int>() }
    var showBonus by remember { mutableStateOf(false) }

    val isCurrentRevealed = currentStep in revealed
    val isLastStep = currentStep == config.steps.lastIndex
    val showFinalAnswer = isLastStep && isCurrentRevealed

    // Which annotations should be live on the diagram right now =
    // every step ≤ currentStep that has been revealed.
    val activeAnnotations: Set<String> = remember(currentStep, revealed.size) {
        revealed.mapNotNull { idx -> config.steps.getOrNull(idx)?.annotation }.toSet()
    }

    Row(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Left column: diagram on top, problem below ────────────────
        Column(
            modifier = Modifier.weight(0.55f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Diagram pane
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)),
            ) {
                when (config.diagram) {
                    "projectile-launch" -> ProjectileDiagram(
                        annotations = activeAnnotations,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                    )
                    else -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText(
                            "Diagram \"${config.diagram}\" not implemented",
                            color = t.ink500, size = 13.sp,
                        )
                    }
                }
            }

            // Problem card (under the diagram)
            Card(
                modifier = Modifier.fillMaxWidth().weight(0.42f),
                padding = 20.dp,
            ) {
                ProblemCardContent(config)
            }
        }

        // ── Right column: step content + nav bar at bottom ────────────
        Column(
            modifier = Modifier.weight(0.45f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Step strip OR final answer card. Once last step is revealed
            // the answer takes over the same slot — feels like the
            // walkthrough has "arrived" at the answer rather than sliding
            // it in alongside the strip.
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (showFinalAnswer) {
                    FinalAnswerCard(
                        answer = config.finalAnswer,
                        bonus = config.bonus,
                        bonusShown = showBonus,
                        onToggleBonus = { showBonus = !showBonus },
                    )
                } else {
                    StepStrip(
                        step = config.steps[currentStep],
                        stepIndex = currentStep,
                        totalSteps = config.steps.size,
                        revealed = isCurrentRevealed,
                        onReveal = { if (currentStep !in revealed) revealed.add(currentStep) },
                    )
                }
            }

            // Nav row at the bottom of the right column. Dots are
            // compact and sit between Prev and Next; Reset / Back stack
            // on a second compact row to keep the main controls big.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SecondaryButton(
                        label = "‹ Prev",
                        onClick = { if (currentStep > 0) currentStep-- },
                        enabled = currentStep > 0,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        config.steps.indices.forEach { i ->
                            val isCurrent = i == currentStep
                            val isDone = i in revealed
                            val color = when {
                                isCurrent -> t.accent500
                                isDone -> t.accent300
                                else -> t.surface3
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { currentStep = i },
                            )
                        }
                    }

                    PrimaryButton(
                        label = "Next ›",
                        onClick = {
                            if (currentStep < config.steps.lastIndex) currentStep++
                        },
                        enabled = currentStep < config.steps.lastIndex && isCurrentRevealed,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        GhostButton(label = "← Back to Lab", onClick = onBack)
                        Spacer(Modifier.width(8.dp))
                    }
                    GhostButton(
                        label = "Reset",
                        onClick = {
                            currentStep = 0
                            revealed.clear()
                            showBonus = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemCardContent(config: WorkedProblemConfig) {
    val t = LL.tokens
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        item { SectionLabel("PROBLEM") }
        item {
            LLText(
                config.problem,
                color = t.ink50,
                size = 14.sp,
                lineHeight = 20.sp,
            )
        }

        if (config.given.isNotEmpty()) {
            item { SectionLabel("GIVEN") }
            items(config.given) { q ->
                QuantityRow(sym = q.sym, valueOrLabel = q.value ?: q.label.orEmpty())
            }
        }

        if (config.find.isNotEmpty()) {
            item { SectionLabel("FIND") }
            items(config.find) { q ->
                QuantityRow(sym = q.sym, valueOrLabel = q.label ?: q.value.orEmpty())
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val t = LL.tokens
    Column {
        LLText(
            text,
            color = t.accent500,
            size = 11.sp,
            weight = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
        )
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
    }
}

@Composable
private fun QuantityRow(sym: String, valueOrLabel: String) {
    val t = LL.tokens
    Row(verticalAlignment = Alignment.CenterVertically) {
        LLText(
            sym,
            color = t.ink50,
            size = 14.sp,
            weight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(56.dp),
        )
        LLText(
            "= $valueOrLabel".takeIf { valueOrLabel.contains(Regex("[0-9]")) } ?: valueOrLabel,
            color = t.ink400,
            size = 13.sp,
        )
    }
}

@Composable
private fun StepStrip(
    step: Step,
    stepIndex: Int,
    totalSteps: Int,
    revealed: Boolean,
    onReveal: () -> Unit,
) {
    val t = LL.tokens
    var showWhy by remember(stepIndex) { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LLText(
                    "STEP ${stepIndex + 1} OF $totalSteps",
                    color = t.accent500,
                    size = 10.sp,
                    weight = FontWeight.SemiBold,
                    letterSpacing = 1.8.sp,
                )
                if (step.whyAnswers.isNotEmpty()) {
                    WhyChip(
                        count = step.whyAnswers.size,
                        onClick = { showWhy = true },
                    )
                }
            }
            LLText(
                step.prompt,
                color = t.ink50,
                size = 16.sp,
                weight = FontWeight.SemiBold,
                lineHeight = 22.sp,
            )

            if (revealed) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    step.reveal.forEach { line ->
                        LLText(
                            line,
                            color = t.ink200,
                            size = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    PrimaryButton(label = "Reveal step  ▶", onClick = onReveal)
                }
            }
        }
    }

    if (showWhy) {
        WhyDialog(
            stepLabel = "STEP ${stepIndex + 1}",
            answers = step.whyAnswers,
            onDismiss = { showWhy = false },
        )
    }
}

/**
 * Small "But why?" chip. Lives at the right end of the STEP label row.
 * Only shown when the step has at least one whyAnswer.
 */
@Composable
private fun WhyChip(count: Int, onClick: () -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(t.accent50)
            .border(1.dp, t.accent300, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(
            "?  But why",
            color = t.accent700,
            size = 11.sp,
            weight = FontWeight.SemiBold,
        )
        if (count > 1) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(t.accent500),
                contentAlignment = Alignment.Center,
            ) {
                LLText(
                    "$count",
                    color = androidx.compose.ui.graphics.Color.White,
                    size = 9.sp,
                    weight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * Modal dialog showing the list of "But why?" follow-up questions for
 * the current step. Each question expands inline to reveal its answer.
 * Multiple questions can be open at once — teacher might want to compare.
 *
 * Designed to read well on a projector: large type, generous spacing,
 * one clear "Close" button. The background is dimmed (default Dialog
 * scrim) so the dialog visually sits on top without fully erasing the
 * step strip / diagram context behind.
 */
@Composable
private fun WhyDialog(
    stepLabel: String,
    answers: List<WhyAnswer>,
    onDismiss: () -> Unit,
) {
    val t = LL.tokens
    val openQuestions = remember { mutableStateListOf<Int>() }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(20.dp))
                .background(t.surface)
                .border(1.dp, t.lineStrong, RoundedCornerShape(20.dp))
                .padding(28.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        LLText(
                            "$stepLabel  ·  BUT WHY?",
                            color = t.accent500,
                            size = 11.sp,
                            weight = FontWeight.SemiBold,
                            letterSpacing = 1.8.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        LLText(
                            "Common follow-ups students ask",
                            color = t.ink400,
                            size = 13.sp,
                        )
                    }
                    GhostButton(label = "Close ✕", onClick = onDismiss)
                }

                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

                // Q&A list — scrollable in case it's long
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(answers.size) { i ->
                        val qa = answers[i]
                        val isOpen = i in openQuestions
                        WhyEntry(
                            qa = qa,
                            isOpen = isOpen,
                            onToggle = {
                                if (isOpen) openQuestions.remove(i)
                                else openQuestions.add(i)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyEntry(
    qa: WhyAnswer,
    isOpen: Boolean,
    onToggle: () -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isOpen) 10.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            LLText(
                qa.question,
                color = t.ink50,
                size = 15.sp,
                weight = FontWeight.SemiBold,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            LLText(
                if (isOpen) "−" else "+",
                color = t.accent500,
                size = 20.sp,
                weight = FontWeight.Bold,
            )
        }
        if (isOpen) {
            LLText(
                qa.answer,
                color = t.ink200,
                size = 14.sp,
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun FinalAnswerCard(
    answer: String,
    bonus: String?,
    bonusShown: Boolean,
    onToggleBonus: () -> Unit,
) {
    val t = LL.tokens
    Card(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LLText(
                "ANSWER",
                color = t.accent700,
                size = 10.sp,
                weight = FontWeight.SemiBold,
                letterSpacing = 1.8.sp,
            )
            LLText(
                answer,
                color = t.ink50,
                size = 18.sp,
                weight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )

            if (bonus != null) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(
                        if (bonusShown) "BONUS FORMULA" else "Show bonus formula",
                        color = t.accent500,
                        size = 12.sp,
                        weight = FontWeight.SemiBold,
                    )
                    GhostButton(
                        label = if (bonusShown) "Hide" else "Show",
                        onClick = onToggleBonus,
                    )
                }
                if (bonusShown) {
                    LLText(
                        bonus,
                        color = t.ink200,
                        size = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}
