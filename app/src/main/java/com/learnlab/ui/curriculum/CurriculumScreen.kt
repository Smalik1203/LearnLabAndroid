package com.learnlab.ui.curriculum

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.Chapters
import com.learnlab.content.Experiment
import com.learnlab.design.LL
import com.learnlab.design.LLAnimation
import com.learnlab.design.LLText
import com.learnlab.design.MeshBackground
import com.learnlab.design.Radius
import com.learnlab.design.Spacing
import com.learnlab.design.bounceClickable
import com.learnlab.design.gradProgress
import com.learnlab.engines.experimentRegistry
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

@Composable
fun CurriculumScreen(
    state: AppState,
    grade: Int,
    onExperimentSelected: (String) -> Unit,
    onChapterSelected: (String) -> Unit = {},
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val gradeChapters = remember(grade) {
        Chapters.filter { it.grade == grade }.sortedBy { it.number }
    }
    val expandedIds: SnapshotStateList<String> = rememberSaveable(
        grade,
        saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() },
        ),
    ) { mutableListOf<String>().toMutableStateList() }

    // Dynamic curriculum implementation progress calculation
    val totalExperiments = remember(gradeChapters) {
        gradeChapters.sumOf { it.experiments.size }
    }
    val availableExperiments = remember(gradeChapters) {
        gradeChapters.flatMap { it.experiments }.count { it.id in experimentRegistry.keys }
    }
    val progressRatio = if (totalExperiments > 0) {
        availableExperiments.toFloat() / totalExperiments.toFloat()
    } else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                state = state,
                title = "Back to Grades",
                showBack = true,
                onBack = onBack,
                onHomeClick = onHome,
            )

            // Dynamic header card containing curriculum completion progress ring
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Grade $grade Curriculum",
                        style = MaterialTheme.typography.headlineMedium,
                        color = t.ink50,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    LLText(
                        "${gradeChapters.size} Chapters · $totalExperiments Labs",
                        color = t.ink400,
                        size = 14.sp,
                        weight = FontWeight.Medium
                    )
                }

                // Completion progress ring
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        LLText("Curriculum Engine", color = t.accent700, size = 11.sp, weight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        LLText("$availableExperiments of $totalExperiments Labs Active", color = t.ink400, size = 12.sp, weight = FontWeight.SemiBold)
                    }

                    val progressBrush = gradProgress()
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 4.5.dp.toPx()
                            val minDim = size.minDimension
                            val r = minDim / 2 - strokeW

                            // Track
                            drawCircle(
                                color = t.surface3.copy(alpha = 0.25f),
                                radius = r,
                                style = Stroke(width = strokeW)
                            )
                            // Sweep Progress
                            drawArc(
                                brush = progressBrush,
                                startAngle = -90f,
                                sweepAngle = progressRatio * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                        LLText(
                            "${(progressRatio * 100).toInt()}%",
                            color = t.ink50,
                            size = 11.sp,
                            weight = FontWeight.Black
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                gradeChapters.forEach { chapter ->
                    item(key = chapter.id) {
                        ChapterCard(
                            chapter = chapter,
                            isExpanded = chapter.id in expandedIds,
                            onToggle = {
                                if (chapter.id in expandedIds) expandedIds.remove(chapter.id)
                                else expandedIds.add(chapter.id)
                            },
                            onExperimentSelected = onExperimentSelected,
                            onReadChapter = chapterJsonIdFor(chapter.id)?.let { jsonId ->
                                { onChapterSelected(jsonId) }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Maps a Chapters-table id (e.g. "ch02") to its JSON chapter id (e.g. "g6-sci-ch02") if a theory file exists. */
private fun chapterJsonIdFor(chapterIdInTable: String): String? = when (chapterIdInTable) {
    "ch02" -> "g6-sci-ch02"
    else -> null
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onExperimentSelected: (String) -> Unit,
    onReadChapter: (() -> Unit)? = null,
) {
    val t = LL.tokens
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "chevron")
    
    // Border highlights
    val glowColor by animateColorAsState(
        if (isExpanded) t.accent500.copy(alpha = 0.5f) else t.lineStrong.copy(alpha = 0.15f),
        label = "chapterGlow"
    )
    val cardBg = if (chapter.comingSoon) t.surface.copy(alpha = 0.12f) else t.surface.copy(alpha = 0.35f)
    val glassBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (isExpanded) 0.15f else 0.05f), Color.Transparent)
    )

    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 12.dp else 4.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    )
                )
                .background(cardBg)
                .border(1.dp, glassBorder, RoundedCornerShape(Radius.lg))
                .border(
                    width = 1.dp,
                    color = glowColor,
                    shape = RoundedCornerShape(Radius.lg)
                )
        ) {
            // Chapter header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !chapter.comingSoon) { onToggle() }
                    .background(
                        if (isExpanded) t.accent50.copy(alpha = 0.08f) else Color.Transparent
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Chapter number badge
                    Surface(
                        shape = RoundedCornerShape(Radius.sm),
                        color = when {
                            chapter.comingSoon -> t.surface2.copy(alpha = 0.2f)
                            isExpanded -> t.accent50.copy(alpha = 0.2f)
                            else -> t.surface2.copy(alpha = 0.35f)
                        },
                        modifier = Modifier.size(44.dp),
                        border = borderConstraintFor(isExpanded, chapter.comingSoon)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${chapter.number}",
                                style = MaterialTheme.typography.titleSmall,
                                color = when {
                                    chapter.comingSoon -> t.ink500
                                    isExpanded -> t.accent700
                                    else -> t.ink200
                                },
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "CHAPTER ${chapter.number}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isExpanded) t.accent700 else t.ink500,
                            letterSpacing = 1.8.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            chapter.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (chapter.comingSoon) t.ink500 else t.ink50,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chapter.comingSoon) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = t.surface2.copy(alpha = 0.3f),
                            modifier = Modifier.border(1.dp, t.line.copy(alpha = 0.3f), RoundedCornerShape(50))
                        ) {
                            Text(
                                "Coming soon",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.ink500,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    } else {
                        LLText(
                            "${chapter.experiments.size} Labs",
                            color = t.ink400,
                            size = 13.sp,
                            weight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = t.ink200,
                            modifier = Modifier.size(20.dp).rotate(rotation),
                        )
                    }
                }
            }

            // Expanded experiments list
            if (isExpanded && !chapter.comingSoon) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line.copy(alpha = 0.3f)))
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (onReadChapter != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(Radius.sm))
                                .background(t.accent50.copy(alpha = 0.15f))
                                .clickable { onReadChapter() }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Read full chapter textbook theory",
                                style = MaterialTheme.typography.labelMedium,
                                color = t.accent700,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = t.accent500,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    chapter.experiments.forEach { experiment ->
                        ExperimentRow(experiment = experiment, onClick = { onExperimentSelected(experiment.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun borderConstraintFor(isExpanded: Boolean, isComingSoon: Boolean): BorderStroke? {
    val t = LL.tokens
    return when {
        isComingSoon -> null
        isExpanded -> BorderStroke(1.dp, t.accent300)
        else -> BorderStroke(1.dp, t.line.copy(alpha = 0.5f))
    }
}

@Composable
private fun ExperimentRow(experiment: Experiment, onClick: () -> Unit) {
    val t = LL.tokens
    val isRunning = experiment.id in experimentRegistry.keys

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .bounceClickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    experiment.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = t.ink50,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isRunning) {
                    // Micro badge for live labs
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        LLText(
                            "LIVE",
                            color = Color(0xFF10B981),
                            size = 8.sp,
                            weight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                experiment.blurb,
                style = MaterialTheme.typography.labelSmall,
                color = t.ink400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = CircleShape,
            color = if (isRunning) t.accent50.copy(alpha = 0.2f) else t.surface2.copy(alpha = 0.4f),
            modifier = Modifier.size(32.dp),
            border = BorderStroke(0.5.dp, if (isRunning) t.accent300 else t.line.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isRunning) t.accent700 else t.ink400,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
