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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.learnlab.design.NavyDeep
import com.learnlab.design.OnSurfaceHigh
import com.learnlab.design.OnSurfaceLow
import com.learnlab.design.OnSurfaceMed
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
                .padding(horizontal = 40.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Curriculum",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurfaceHigh,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${Chapters.size} chapters · ${Chapters.sumOf { it.experiments.size }} experiments",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanBright,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Chapters.forEach { chapter ->
                item(key = chapter.id) {
                    ChapterCard(
                        chapter = chapter,
                        isExpanded = expanded[chapter.id] == true,
                        onToggle = { expanded[chapter.id] = expanded[chapter.id] != true },
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
    onExperimentSelected: (String) -> Unit,
) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "chevron")
    val borderColor by animateColorAsState(
        if (isExpanded) CyanBright.copy(alpha = 0.5f) else SurfaceElevated,
        label = "chapterBorder",
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Chapter number badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpanded) CyanBright.copy(alpha = 0.15f) else SurfaceMid,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${chapter.number}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isExpanded) CyanBright else OnSurfaceMed,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "CH ${chapter.number}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isExpanded) CyanBright else OnSurfaceLow,
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            chapter.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isExpanded) OnSurfaceHigh else OnSurfaceMed,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${chapter.experiments.size} exp",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceLow,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OnSurfaceMed,
                        modifier = Modifier.size(20.dp).rotate(rotation),
                    )
                }
            }

            // Expanded experiments list
            if (isExpanded) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
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
                color = OnSurfaceHigh,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                experiment.blurb,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = CyanBright.copy(alpha = 0.10f),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CyanMid,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
