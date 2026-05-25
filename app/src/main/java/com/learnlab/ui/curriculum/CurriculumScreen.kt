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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.Chapters
import com.learnlab.content.Experiment
import com.learnlab.design.CyanBright
import com.learnlab.design.CyanMid
import com.learnlab.design.CyanSoft
import com.learnlab.design.IconSize
import com.learnlab.design.LLText
import com.learnlab.design.NavyDeep
import com.learnlab.design.OnSurfaceHigh
import com.learnlab.design.OnSurfaceLow
import com.learnlab.design.OnSurfaceMed
import com.learnlab.design.Radius
import com.learnlab.design.Spacing
import com.learnlab.design.SurfaceCard
import com.learnlab.design.SurfaceDark
import com.learnlab.design.SurfaceElevated
import com.learnlab.design.SurfaceMid
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

@Composable
fun CurriculumScreen(
    state: AppState,
    onExperimentSelected: (String) -> Unit,
    onChapterSelected: (String) -> Unit = {},
    onBack: () -> Unit,
) {
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            // Expand the first chapter by default
            Chapters.firstOrNull()?.let { put(it.id, true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(NavyDeep, SurfaceDark, Color(0xFF0E1F3D))))
    ) {
        TopBar(state = state, title = "Grade 6 Science", showBack = true, onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxxl, vertical = Spacing.lg + Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                LLText(
                    "Curriculum",
                    color = OnSurfaceHigh,
                    size = 26.sp,
                    weight = FontWeight.Bold,
                )
                LLText(
                    "${Chapters.size} chapters · ${Chapters.sumOf { it.experiments.size }} experiments",
                    color = CyanBright,
                    size = 13.sp,
                )
            }
        }

        if (Chapters.isEmpty()) {
            EmptyChaptersState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.xxxl, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Chapters.forEach { chapter ->
                    item(key = chapter.id) {
                        ChapterCard(
                            chapter = chapter,
                            isExpanded = expanded[chapter.id] == true,
                            onToggle = { expanded[chapter.id] = expanded[chapter.id] != true },
                            onExperimentSelected = onExperimentSelected,
                            onReadChapter = chapterJsonIdFor(chapter.id)?.let { jsonId ->
                                { onChapterSelected(jsonId) }
                            },
                        )
                    }
                }
                item { Spacer(Modifier.height(Spacing.xl)) }
            }
        }
    }
}

@Composable
private fun EmptyChaptersState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LLText(
                "No chapters yet",
                color = OnSurfaceHigh,
                size = 18.sp,
                weight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(Spacing.sm))
            LLText(
                "Chapters will appear here once content is added.",
                color = OnSurfaceMed,
                size = 13.sp,
            )
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
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "chevron")
    val borderColor by animateColorAsState(
        if (isExpanded) CyanBright.copy(alpha = 0.5f) else SurfaceElevated,
        label = "chapterBorder",
    )

    Surface(
        shape = RoundedCornerShape(Radius.xl),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Chapter header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .background(
                        if (isExpanded) Brush.horizontalGradient(
                            listOf(CyanBright.copy(alpha = 0.08f), Color.Transparent)
                        ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .padding(horizontal = Spacing.lg + Spacing.xs, vertical = Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Chapter number badge
                    Surface(
                        shape = RoundedCornerShape(Radius.sm + 2.dp),
                        color = if (isExpanded) CyanBright.copy(alpha = 0.15f) else SurfaceMid,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            LLText(
                                "${chapter.number}",
                                color = if (isExpanded) CyanBright else OnSurfaceMed,
                                size = 16.sp,
                                weight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.width(Spacing.lg))
                    Column {
                        LLText(
                            "CH ${chapter.number}",
                            color = if (isExpanded) CyanBright else OnSurfaceLow,
                            size = 11.sp,
                            letterSpacing = 1.6.sp,
                            weight = FontWeight.SemiBold,
                        )
                        LLText(
                            chapter.title,
                            color = if (isExpanded) OnSurfaceHigh else OnSurfaceMed,
                            size = 16.sp,
                            weight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LLText(
                        "${chapter.experiments.size} exp",
                        color = OnSurfaceLow,
                        size = 11.sp,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OnSurfaceMed,
                        modifier = Modifier.size(IconSize.md).rotate(rotation),
                    )
                }
            }

            // Expanded experiments list
            if (isExpanded) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
                Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                    if (onReadChapter != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.sm)
                                .clip(RoundedCornerShape(Radius.md))
                                .background(CyanBright.copy(alpha = 0.15f))
                                .clickable { onReadChapter() }
                                .padding(horizontal = Spacing.md, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LLText("Read full chapter", color = CyanBright, size = 14.sp, weight = FontWeight.Bold,
                                modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanBright,
                                modifier = Modifier.size(IconSize.md))
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
private fun ExperimentRow(experiment: Experiment, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LLText(
                experiment.title,
                color = OnSurfaceHigh,
                size = 14.sp,
                weight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LLText(
                experiment.blurb,
                color = OnSurfaceMed,
                size = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = RoundedCornerShape(Radius.pill),
            color = CyanBright.copy(alpha = 0.10f),
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CyanMid,
                    modifier = Modifier.size(IconSize.sm),
                )
            }
        }
    }
}
