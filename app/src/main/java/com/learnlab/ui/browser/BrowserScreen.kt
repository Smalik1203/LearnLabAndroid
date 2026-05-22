package com.learnlab.ui.browser

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.Experiment
import com.learnlab.content.findChapters
import com.learnlab.design.AmberBright
import com.learnlab.design.CyanBright
import com.learnlab.design.CyanMid
import com.learnlab.design.EmeraldBright
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

private data class SubjectDef(val id: String, val name: String, val color: Color, val icon: ImageVector)

private val subjectDefs = listOf(
    SubjectDef("science",        "Science",        CyanBright,    Icons.Default.Science),
    SubjectDef("mathematics",    "Mathematics",    AmberBright,   Icons.Default.Calculate),
    SubjectDef("social_science", "Social Science", EmeraldBright, Icons.Default.Public),
)

@Composable
fun BrowserScreen(
    state: AppState,
    grade: Int,
    initialSubjectId: String,
    onExperimentSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    var selectedSubjectId by remember { mutableStateOf(initialSubjectId) }
    var selectedChapterId by remember { mutableStateOf<String?>(null) }

    val chapters = findChapters(grade, selectedSubjectId)
    val selectedChapter = chapters.firstOrNull { it.id == selectedChapterId }
        ?: chapters.firstOrNull()

    // Reset chapter when subject changes
    LaunchedEffect(selectedSubjectId) {
        selectedChapterId = chapters.firstOrNull()?.id
    }

    val accentColor = subjectDefs.firstOrNull { it.id == selectedSubjectId }?.color ?: CyanBright

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(NavyDeep, SurfaceDark, Color(0xFF0E1F3D))))
    ) {
        TopBar(state = state, title = "Grade $grade", showBack = true, onBack = onBack)

        Row(modifier = Modifier.fillMaxSize()) {

            // ── Subject pane ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(SurfaceDark),
            ) {
                Text(
                    "SUBJECT",
                    style         = MaterialTheme.typography.labelSmall,
                    color         = OnSurfaceLow,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                    modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
                subjectDefs.forEach { subject ->
                    val isSelected = subject.id == selectedSubjectId
                    val subjectChapters = findChapters(grade, subject.id)
                    val hasContent = subjectChapters.isNotEmpty()
                    SubjectItem(
                        subject    = subject,
                        isSelected = isSelected,
                        hasContent = hasContent,
                        onClick    = {
                            selectedSubjectId = subject.id
                            selectedChapterId = null
                        },
                    )
                }
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(SurfaceElevated))

            // ── Chapter pane ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(SurfaceMid),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "CHAPTERS",
                        style         = MaterialTheme.typography.labelSmall,
                        color         = OnSurfaceLow,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp,
                    )
                    if (chapters.isNotEmpty()) {
                        Text("${chapters.size}", style = MaterialTheme.typography.labelSmall, color = accentColor)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
                if (chapters.isEmpty()) {
                    ComingSoonPane("No chapters available yet for this grade and subject.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 6.dp),
                    ) {
                        items(chapters, key = { it.id }) { chapter ->
                            ChapterItem(
                                chapter    = chapter,
                                isSelected = chapter.id == (selectedChapter?.id),
                                accent     = accentColor,
                                onClick    = { selectedChapterId = chapter.id },
                            )
                        }
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(SurfaceElevated))

            // ── Topic pane ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(NavyDeep),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "TOPICS",
                            style         = MaterialTheme.typography.labelSmall,
                            color         = OnSurfaceLow,
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 1.6.sp,
                        )
                        if (selectedChapter != null) {
                            Text(
                                selectedChapter.title,
                                style     = MaterialTheme.typography.labelMedium,
                                color     = accentColor,
                                maxLines  = 1,
                                overflow  = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (selectedChapter != null) {
                        Text(
                            "${selectedChapter.experiments.size} topics",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceLow,
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))

                when {
                    chapters.isEmpty() -> ComingSoonPane("Select a subject with content.")
                    selectedChapter == null -> ComingSoonPane("Select a chapter to see its topics.")
                    selectedChapter.experiments.isEmpty() -> ComingSoonPane("No topics yet for this chapter.")
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(selectedChapter.experiments, key = { it.id }) { experiment ->
                            TopicCard(
                                experiment = experiment,
                                accent     = accentColor,
                                onClick    = { onExperimentSelected(experiment.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun SubjectItem(
    subject: SubjectDef,
    isSelected: Boolean,
    hasContent: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) subject.color.copy(alpha = 0.12f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) subject.color.copy(alpha = 0.15f) else SurfaceCard,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    subject.icon,
                    contentDescription = null,
                    tint = if (isSelected) subject.color else (if (hasContent) OnSurfaceMed else OnSurfaceLow),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                subject.name,
                style      = MaterialTheme.typography.labelMedium,
                color      = if (isSelected) subject.color else (if (hasContent) OnSurfaceHigh else OnSurfaceLow),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (!hasContent) {
                Text("Coming soon", style = MaterialTheme.typography.labelSmall, color = OnSurfaceLow)
            }
        }
        if (isSelected) {
            Box(modifier = Modifier.width(3.dp).height(24.dp).background(subject.color, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
private fun ChapterItem(chapter: Chapter, isSelected: Boolean, accent: Color, onClick: () -> Unit) {
    val bg = if (isSelected) accent.copy(alpha = 0.10f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) accent.copy(alpha = 0.15f) else SurfaceCard,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "${chapter.number}",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (isSelected) accent else OnSurfaceMed,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Ch ${chapter.number}",
                style         = MaterialTheme.typography.labelSmall,
                color         = if (isSelected) accent else OnSurfaceLow,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
            )
            Text(
                chapter.title,
                style    = MaterialTheme.typography.labelMedium,
                color    = if (isSelected) OnSurfaceHigh else OnSurfaceMed,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "${chapter.experiments.size}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) accent else OnSurfaceLow,
        )
    }
}

@Composable
private fun TopicCard(experiment: Experiment, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.06f), Color.Transparent)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    experiment.title,
                    style      = MaterialTheme.typography.titleSmall,
                    color      = OnSurfaceHigh,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    experiment.blurb,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = OnSurfaceMed,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    experiment.source,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = OnSurfaceLow,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(12.dp))
            Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronRight, null, tint = CyanMid, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ComingSoonPane(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            message,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceLow,
            modifier = Modifier.padding(24.dp),
        )
    }
}
