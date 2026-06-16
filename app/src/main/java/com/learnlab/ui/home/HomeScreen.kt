package com.learnlab.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Subject
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.design.interactiveCard
import com.learnlab.store.AppState

@Composable
fun HomeScreen(
    state: AppState,
    onOpenExperiment: (String) -> Unit,
    onLogo: () -> Unit,
    onViewAllHistory: () -> Unit,
    onOpenTextbook: (Int) -> Unit,
) {
    val t = LL.tokens
    var tab by remember { mutableIntStateOf(0) }
    var modalSubject by remember { mutableStateOf<Subject?>(null) }

    ChromeScaffold(
        state = state,
        onLogo = onLogo,
        onOpenExperiment = onOpenExperiment,
        onViewAllHistory = onViewAllHistory,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            LLText(
                if (tab == 0) "What do you want to learn?" else "Tutorials",
                color = t.ink50, size = 34.sp, weight = FontWeight.Bold, align = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            LLText(
                if (tab == 0) "Select a field to explore its interactive experiments"
                else "Browse step-by-step guides and video masterclasses.",
                color = t.ink400, size = 16.sp, align = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))
            PillTabBar(
                tabs = listOf("Interactive Labs", "Tutorials"),
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.width(360.dp),
            )

            Spacer(Modifier.height(40.dp))
            if (tab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SubjectCard(Subject.PHYSICS, Modifier.weight(1f)) { modalSubject = Subject.PHYSICS }
                    SubjectCard(Subject.CHEMISTRY, Modifier.weight(1f)) { modalSubject = Subject.CHEMISTRY }
                    SubjectCard(Subject.MATHEMATICS, Modifier.weight(1f)) { modalSubject = Subject.MATHEMATICS }
                    SubjectCard(Subject.BIOLOGY, Modifier.weight(1f)) { modalSubject = Subject.BIOLOGY }
                }
            } else {
                TutorialsList(Modifier.fillMaxWidth(0.7f))
            }
        }
    }

    modalSubject?.let { subj ->
        SubjectTopicsModal(
            subject = subj,
            onDismiss = { modalSubject = null },
            onOpenTextbook = { grade -> modalSubject = null; onOpenTextbook(grade) },
        )
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(300.dp)
            .interactiveCard(interaction, glowColor = subject.color, shape = RoundedCornerShape(24.dp))
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
    ) {
        // faint subject glow behind the icon
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(subject.color.copy(alpha = 0.18f), Color.Transparent),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(6.dp))
            // The provided 3D clay icons already include the colored disc + glow.
            Image(
                painter = painterResource(subject.iconRes),
                contentDescription = null,
                modifier = Modifier.size(124.dp),
            )
            Spacer(Modifier.height(16.dp))
            LLText(subject.displayName, color = t.ink50, size = 24.sp, weight = FontWeight.Bold, align = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            LLText(subject.description, color = t.ink400, size = 14.sp, lineHeight = 20.sp, align = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = t.ink400, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private sealed interface TutorialBlock {
    data class Intro(val text: String) : TutorialBlock
    data class Heading(val text: String) : TutorialBlock
    data class Paragraph(val text: String) : TutorialBlock          // may contain **bold**
    data class NumberedStep(val number: Int, val text: String) : TutorialBlock  // **bold** lead phrase
}

private data class TutorialItem(val title: String, val blocks: List<TutorialBlock>)

private val TUTORIALS = listOf(
    TutorialItem("Getting Started", listOf(
        TutorialBlock.Intro("LearnLab is an interactive science lab where you learn by doing. Choose a subject, pick a grade, and open an experiment to explore it hands-on."),
        TutorialBlock.Heading("Steps to start"),
        TutorialBlock.NumberedStep(1, "**Open the homepage** — you'll see four subject cards: Physics, Chemistry, Mathematics, and Biology."),
        TutorialBlock.NumberedStep(2, "**Pick a subject** — a topic picker opens; choose a grade, then pick a topic to start."),
        TutorialBlock.NumberedStep(3, "**Open the experiment** — the topic launches an interactive stage you can play with right away."),
        TutorialBlock.NumberedStep(4, "**Explore hands-on** — drag, slide, and tap the controls to change variables and watch the science respond. Use **‹ Previous** and **Next ›** at the bottom to step through the activity."),
        TutorialBlock.Paragraph("From any experiment, use **Back to Lab** (top-left) to return to the homepage. The **theme toggle** — bottom-right on the homepage, top-right inside an experiment — switches between dark and light mode."),
    )),
    TutorialItem("Tips & Tricks", listOf(
        TutorialBlock.Intro("Get more out of LearnLab with these simple ideas."),
        TutorialBlock.Heading("While you explore"),
        TutorialBlock.Paragraph("**Change the controls** — move sliders, try different options, or repeat an action. Seeing how the result changes is how you build intuition."),
        TutorialBlock.Paragraph("**Read the instruction banner** — the bar above each experiment guides you with prompts like \"What happens when…?\". Use them as a mini-challenge."),
        TutorialBlock.Heading("Work through the steps"),
        TutorialBlock.Paragraph("Move through each activity in order with **‹ Previous** and **Next ›**, and watch the progress bar fill as you go. Rushing past steps can leave gaps."),
        TutorialBlock.Heading("Comfort"),
        TutorialBlock.Paragraph("Use the **theme toggle** to switch between dark and light mode on any page. Your choice is saved for next time."),
    )),
    TutorialItem("Using the Lab", listOf(
        TutorialBlock.Intro("A quick guide to the homepage and experiment screens so you can move around easily."),
        TutorialBlock.Heading("Homepage"),
        TutorialBlock.Paragraph("**Subject cards** — tap a subject (Physics, Chemistry, Mathematics, Biology) to open the topic picker, then choose a grade and a topic. Use the **Interactive Labs** and **Tutorials** tabs to switch between experiments and these guides."),
        TutorialBlock.Paragraph("**Search & History** — use the **Search** and **History** icons in the top bar to find an experiment or reopen a recent one. Tap the **LearnLab** wordmark any time to return home."),
        TutorialBlock.Heading("Inside an experiment"),
        TutorialBlock.Paragraph("**Back to Lab** (top-left) returns you to the homepage. The title and a **progress bar** sit at the top; the **instruction banner** below tells you what to do. Step through with **‹ Previous** and **Next ›**, and use the **Home** and **theme** buttons at the top-right any time."),
    )),
    TutorialItem("How Experiments Work", listOf(
        TutorialBlock.Intro("Every LearnLab experiment is hands-on: you change things, watch what happens, and follow the on-screen steps to build understanding."),
        TutorialBlock.Heading("Interact"),
        TutorialBlock.Paragraph("Use the controls — sliders, buttons, drag-and-drop — to change variables and watch the simulation respond in real time. No jargon, no wrong moves; just try things."),
        TutorialBlock.Heading("Follow the steps"),
        TutorialBlock.Paragraph("The **instruction banner** at the top guides your exploration, often with a prompt like \"What happens when you change the angle?\". Move through the activity with **‹ Previous** and **Next ›**, and watch the **progress bar** fill as you go."),
        TutorialBlock.Heading("Moving around"),
        TutorialBlock.Paragraph("Use **‹ Previous** and **Next ›** to move between steps. **Back to Lab** (top-left) returns you to the homepage, where you can pick another experiment — in the same subject or a different one."),
    )),
)

private fun tutorialBold(text: String, boldColor: Color): AnnotatedString =
    buildAnnotatedString {
        text.split("**").forEachIndexed { i, part ->
            if (i % 2 == 1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = boldColor))
                append(part); pop()
            } else append(part)
        }
    }

@Composable
private fun TutorialsList(modifier: Modifier = Modifier) {
    val t = LL.tokens
    var expanded by remember { mutableStateOf(-1) }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TUTORIALS.forEachIndexed { i, item ->
            item(key = item.title) {
                val isOpen = expanded == i
                val rowInteraction = remember { MutableInteractionSource() }
                val cardBrush = Brush.linearGradient(
                    0.0f to if (t.isDark) t.surface2 else t.surface,
                    0.5f to t.surface,
                    1.0f to com.learnlab.design.SubjectMath
                        .copy(alpha = if (t.isDark) 0.12f else 0.06f)
                        .compositeOver(t.surface),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .interactiveCard(
                            rowInteraction,
                            glowColor = com.learnlab.design.SubjectChemistry,
                            shape = RoundedCornerShape(14.dp),
                            backgroundBrush = cardBrush,
                            hoverScale = 1.01f,
                            pressScale = 0.995f,
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = rowInteraction,
                                indication = null,
                                role = Role.Button,
                                onClickLabel = if (isOpen) "Collapse" else "Expand",
                            ) { expanded = if (isOpen) -1 else i }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = com.learnlab.design.SubjectChemistry, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(14.dp))
                        LLText(item.title, color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(
                            if (isOpen) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                            contentDescription = null, tint = t.ink400, modifier = Modifier.size(20.dp),
                        )
                    }
                    AnimatedVisibility(isOpen) {
                        Column(Modifier.padding(start = 52.dp, end = 18.dp, bottom = 16.dp)) {
                            item.blocks.forEachIndexed { bi, block ->
                                TutorialBlockView(block, isFirst = bi == 0)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialBlockView(block: TutorialBlock, isFirst: Boolean) {
    val t = LL.tokens
    when (block) {
        is TutorialBlock.Intro -> {
            if (!isFirst) Spacer(Modifier.height(12.dp))
            LLText(block.text, color = t.ink200, size = 15.sp, lineHeight = 22.sp)
        }
        is TutorialBlock.Heading -> {
            Spacer(Modifier.height(if (isFirst) 0.dp else 16.dp))
            LLText(
                block.text, color = com.learnlab.design.SubjectChemistry,
                size = 15.sp, weight = FontWeight.Bold, lineHeight = 20.sp,
            )
            Spacer(Modifier.height(8.dp))
        }
        is TutorialBlock.Paragraph -> {
            if (!isFirst) Spacer(Modifier.height(10.dp))
            Text(
                tutorialBold(block.text, t.ink50), color = t.ink200,
                fontFamily = com.learnlab.design.Inter, fontSize = 15.sp, lineHeight = 22.sp,
            )
        }
        is TutorialBlock.NumberedStep -> {
            if (!isFirst) Spacer(Modifier.height(10.dp))
            Row {
                Text(
                    "${block.number}.", color = com.learnlab.design.SubjectChemistry,
                    fontFamily = com.learnlab.design.Inter, fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, lineHeight = 22.sp, modifier = Modifier.width(22.dp),
                )
                Text(
                    tutorialBold(block.text, t.ink50), color = t.ink200,
                    fontFamily = com.learnlab.design.Inter, fontSize = 15.sp, lineHeight = 22.sp,
                )
            }
        }
    }
}
