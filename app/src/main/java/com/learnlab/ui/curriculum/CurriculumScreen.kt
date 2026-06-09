package com.learnlab.ui.curriculum

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.Chapters
import com.learnlab.content.Experiment
import com.learnlab.design.LL
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

@Composable
fun CurriculumScreen(
    state: AppState,
    grade: Int,
    onChapterSelected: (String) -> Unit,
    onExperimentSelected: (String) -> Unit,
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
 
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        TopBar(
            state = state,
            title = "Back to Grades",
            showBack = true,
            onBack = onBack,
            onHomeClick = onHome,
        )
 
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Grade $grade",
                    style = MaterialTheme.typography.headlineMedium,
                    color = t.ink50,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${gradeChapters.size} chapters · ${gradeChapters.sumOf { it.experiments.size }} experiments",
                    style = MaterialTheme.typography.bodySmall,
                    color = t.accent500,
                )
            }
        }
 
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        onChapterSelected = onChapterSelected,
                        onExperimentSelected = onExperimentSelected,
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
 
@Composable
private fun ChapterCard(
    chapter: Chapter,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onChapterSelected: (String) -> Unit,
    onExperimentSelected: (String) -> Unit,
) {
    val t = LL.tokens
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "chevron")
    val borderColor by animateColorAsState(
        if (isExpanded) t.accent500.copy(alpha = 0.5f) else t.lineStrong,
        label = "chapterBorder",
    )
 
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (chapter.comingSoon) t.surface.copy(alpha = 0.55f) else t.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Chapter header row — green gradient L→R for active chapters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !chapter.comingSoon) {
                        if (chapter.blocks.isNotEmpty()) {
                            onChapterSelected(chapter.id)
                        } else {
                            onToggle()
                        }
                    }
                    .background(
                        when {
                            chapter.comingSoon -> Color.Transparent
                            t.isDark           -> t.accent400.copy(alpha = 0.30f)
                            else               -> t.accent400.copy(alpha = 0.20f)
                        }
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Chapter number badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            chapter.comingSoon -> t.surface2.copy(alpha = 0.6f)
                            isExpanded -> t.accent500.copy(alpha = 0.15f)
                            else -> t.surface2
                        },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${chapter.number}",
                                style = MaterialTheme.typography.titleSmall,
                                color = when {
                                    chapter.comingSoon -> t.ink500
                                    isExpanded -> t.accent500
                                    else -> t.ink400
                                },
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "CH ${chapter.number}",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                chapter.comingSoon -> t.ink600
                                isExpanded -> t.accent500
                                else -> t.ink500
                            },
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            chapter.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = when {
                                chapter.comingSoon -> t.ink500
                                isExpanded -> t.ink50
                                else -> t.ink400
                            },
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chapter.comingSoon) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = t.surface2,
                        ) {
                            Text(
                                "Coming soon",
                                style = MaterialTheme.typography.labelSmall,
                                color = t.ink500,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Text(
                            "${chapter.experiments.size} exp",
                            style = MaterialTheme.typography.labelSmall,
                            color = t.ink500,
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = t.ink400,
                            modifier = Modifier.size(20.dp).rotate(rotation),
                        )
                    }
                }
            }

            // Expanded experiments list
            if (isExpanded && !chapter.comingSoon) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    chapter.experiments.forEach { experiment ->
                        ExperimentRow(experiment = experiment, onClick = { onExperimentSelected(experiment.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperimentRow(experiment: Experiment, onClick: () -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                experiment.title,
                style = MaterialTheme.typography.labelLarge,
                color = t.ink50,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                experiment.blurb,
                style = MaterialTheme.typography.labelSmall,
                color = t.ink400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = t.accent500.copy(alpha = 0.10f),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = t.accent400,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
