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
import androidx.compose.runtime.LaunchedEffect
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
import com.learnlab.design.SpeakControls
import com.learnlab.design.rememberReadAloud

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
    // every step ≤ currentStep that has been revealed. Once the final
    // answer is shown, all annotations come on — the diagram becomes
    // the consolidated visual summary.
    val activeAnnotations: Set<String> = remember(currentStep, revealed.size, showFinalAnswer) {
        if (showFinalAnswer) {
            config.steps.mapNotNull { it.annotation }.toSet()
        } else {
            revealed.mapNotNull { idx -> config.steps.getOrNull(idx)?.annotation }.toSet()
        }
    }

    // Which "find" symbols have been resolved. Wired from step annotations:
    //   timeOfFlight → T,   maxHeight → H,   range → R
    // Steps without one of those annotations don't tick anything.
    val foundSymbols: Set<String> = remember(activeAnnotations) {
        buildSet {
            if ("timeOfFlight" in activeAnnotations) add("T")
            if ("maxHeight" in activeAnnotations) add("H")
            if ("range" in activeAnnotations) add("R")
        }
    }

    val readAloud = rememberReadAloud()

    // The text to narrate for whatever the right column is currently showing:
    // the final answer once reached, otherwise the step prompt plus its reveal
    // lines once revealed.
    val spokenText: String = remember(currentStep, isCurrentRevealed, showFinalAnswer, showBonus) {
        if (showFinalAnswer) {
            buildString {
                append(config.finalAnswer)
                if (showBonus && config.bonus != null) {
                    append(". ")
                    append(config.bonus)
                }
            }
        } else {
            val step = config.steps[currentStep]
            buildString {
                append(step.prompt)
                if (isCurrentRevealed && step.reveal.isNotEmpty()) {
                    append(". ")
                    append(step.reveal.joinToString(". "))
                }
            }
        }
    }

    // Navigating to a different view stops any in-progress narration so we
    // never read step 1 while step 2 is on screen.
    LaunchedEffect(currentStep, showFinalAnswer) {
        readAloud.stop()
    }

    Row(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Left column: thin givens strip on top, large diagram below ─
        Column(
            modifier = Modifier.weight(0.65f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GivensStrip(
                given = config.given,
                find = config.find,
                foundSymbols = foundSymbols,
            )

            // Diagram pane — now occupies the rest of the left column,
            // visually dominant as requested.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
        }

        // ── Right column: step content + nav bar at bottom ────────────
        Column(
            modifier = Modifier.weight(0.35f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpeakControls(
                text = spokenText,
                controller = readAloud,
                modifier = Modifier.fillMaxWidth(),
            )

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

/**
 * Compact strip above the diagram. Shows the given quantities on the
 * left and the "Find" symbols on the right as small pills that tick
 * off (accent fill + ✓) once the corresponding step has been revealed.
 *
 * Replaces the old verbose Problem card. The problem statement itself
 * is communicated by the diagram + the step prompts; repeating it as
 * prose duplicates information.
 */
@Composable
private fun GivensStrip(
    given: List<Quantity>,
    find: List<Quantity>,
    foundSymbols: Set<String>,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Given quantities — separated by a thin middle dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            given.forEachIndexed { i, q ->
                if (i > 0) {
                    LLText("·", color = t.ink600, size = 13.sp)
                }
                GivenItem(sym = q.sym, value = q.value.orEmpty())
            }
        }

        // Find pills
        if (find.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LLText(
                    "FIND",
                    color = t.ink500,
                    size = 10.sp,
                    weight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                )
                find.forEach { q ->
                    FindPill(sym = q.sym, found = q.sym in foundSymbols)
                }
            }
        }
    }
}

/** A single "sym = value" item in the givens strip, color-coded. */
@Composable
private fun GivenItem(sym: String, value: String) {
    val t = LL.tokens
    // Gravity gets the gravity color so it visually connects to its
    // role in equations. Other givens stay neutral — they are inputs,
    // not motion components.
    val symColor = if (sym == "g") t.motionGravity else t.ink50
    val valueColor = if (sym == "g") t.motionGravity else t.ink200
    Row(verticalAlignment = Alignment.CenterVertically) {
        LLText(
            sym,
            color = symColor,
            size = 13.sp,
            weight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        )
        LLText(
            " = $value",
            color = valueColor,
            size = 13.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** A pill for a "find" symbol — ticks green with ✓ when resolved. */
@Composable
private fun FindPill(sym: String, found: Boolean) {
    val t = LL.tokens
    val bg = if (found) t.accent50 else t.surface2
    val fg = if (found) t.accent700 else t.ink500
    val border = if (found) t.accent500 else t.line
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(
            sym,
            color = fg,
            size = 12.sp,
            weight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        )
        if (found) {
            Spacer(Modifier.width(4.dp))
            LLText("✓", color = fg, size = 11.sp, weight = FontWeight.Bold)
        }
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

            step.teacherNote?.let { note ->
                TeacherNoteStrip(note)
            }

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
 * Muted strip under the prompt that surfaces teacher-only cues — a
 * suggested question to throw at the class, and/or a common mistake
 * to call out. Visually distinct from student-facing text so the
 * teacher can read it without it being mistaken for content the
 * class should follow.
 *
 * Both fields are optional. If both are present they stack.
 */
@Composable
private fun TeacherNoteStrip(note: TeacherNote) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        note.ask?.let { ask ->
            Row(verticalAlignment = Alignment.Top) {
                LLText(
                    "ASK",
                    color = t.accent700,
                    size = 9.sp,
                    weight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.width(44.dp).padding(top = 2.dp),
                )
                LLText(
                    ask,
                    color = t.ink200,
                    size = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        note.mistake?.let { mistake ->
            Row(verticalAlignment = Alignment.Top) {
                LLText(
                    "WATCH",
                    color = t.amber700,
                    size = 9.sp,
                    weight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.width(44.dp).padding(top = 2.dp),
                )
                LLText(
                    mistake,
                    color = t.ink200,
                    size = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
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
