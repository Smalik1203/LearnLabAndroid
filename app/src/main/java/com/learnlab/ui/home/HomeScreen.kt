package com.learnlab.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Subject
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.design.interactiveCard
import com.learnlab.shell.PageChrome
import com.learnlab.store.AppState

@Composable
fun HomeScreen(
    state: AppState,
    onOpenExperiment: (String) -> Unit,
    onLogo: () -> Unit,
) {
    val t = LL.tokens
    var tab by remember { mutableIntStateOf(0) }
    var modalSubject by remember { mutableStateOf<Subject?>(null) }

    PageChrome(state = state, onLogo = onLogo) {
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
                    SubjectCard(Subject.PHYSICS, Icons.Filled.Hub, Modifier.weight(1f)) { modalSubject = Subject.PHYSICS }
                    SubjectCard(Subject.CHEMISTRY, Icons.Filled.Science, Modifier.weight(1f)) { modalSubject = Subject.CHEMISTRY }
                    SubjectCard(Subject.MATHEMATICS, Icons.Filled.Calculate, Modifier.weight(1f)) { modalSubject = Subject.MATHEMATICS }
                    SubjectCard(Subject.BIOLOGY, Icons.Filled.Biotech, Modifier.weight(1f)) { modalSubject = Subject.BIOLOGY }
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
            onTopic = { id -> modalSubject = null; onOpenExperiment(id) },
        )
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    icon: ImageVector,
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
            Spacer(Modifier.height(8.dp))
            GlowDisc(subject.color, icon)
            Spacer(Modifier.height(20.dp))
            LLText(subject.displayName, color = t.ink50, size = 24.sp, weight = FontWeight.Bold, align = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            LLText(subject.description, color = t.ink400, size = 14.sp, lineHeight = 20.sp, align = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = t.ink400, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Bright clay-style orb: soft glow halo + gradient-filled circle + white icon. */
@Composable
private fun GlowDisc(color: Color, icon: ImageVector) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(lerp(color, Color.White, 0.35f), color))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
    }
}

private data class TutorialItem(val title: String, val body: String)

private val TUTORIALS = listOf(
    TutorialItem("Getting Started", "Pick a subject, choose your grade, then open a topic to launch an interactive lab. Everything runs offline."),
    TutorialItem("How Experiments Work", "Each lab is hands-on: drag, slide, and tap to change variables and watch the science respond in real time."),
    TutorialItem("Using the Lab", "Use the controls panel to adjust inputs. The procedure stepper at the top guides you through each activity."),
    TutorialItem("Tips & Tricks", "Get more out of LearnLab with these simple ideas — project to the class, compare runs, and revisit the reader for theory."),
)

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .interactiveCard(
                            rowInteraction,
                            glowColor = com.learnlab.design.SubjectChemistry,
                            shape = RoundedCornerShape(14.dp),
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
                        LLText(
                            item.body,
                            color = t.ink200, size = 15.sp, lineHeight = 22.sp,
                            modifier = Modifier.padding(start = 52.dp, end = 18.dp, bottom = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
