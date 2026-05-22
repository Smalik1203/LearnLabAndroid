package com.learnlab.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learnlab.content.findChapters
import com.learnlab.design.AmberBright
import com.learnlab.design.CyanBright
import com.learnlab.design.EmeraldBright
import com.learnlab.design.NavyDeep
import com.learnlab.design.NavyMid
import com.learnlab.design.OnSurfaceHigh
import com.learnlab.design.OnSurfaceLow
import com.learnlab.design.OnSurfaceMed
import com.learnlab.design.SurfaceCard
import com.learnlab.design.SurfaceDark
import com.learnlab.design.SurfaceElevated
import com.learnlab.design.SuccessGreen
import com.learnlab.store.AppState

private data class SubjectDef(
    val id: String,
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val description: String,
)

private val subjectDefs = listOf(
    SubjectDef("science",        "Science",        CyanBright,    Icons.Default.Science,   "Physics · Chemistry · Biology"),
    SubjectDef("mathematics",    "Mathematics",    AmberBright,   Icons.Default.Calculate, "Geometry · Algebra · Statistics"),
    SubjectDef("social_science", "Social Science", EmeraldBright, Icons.Default.Public,    "History · Geography · Civics"),
)

@Composable
fun HomeScreen(
    state: AppState,
    onSubjectClick: (grade: Int, subjectId: String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val selectedGrade by state.selectedGrade

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(NavyDeep, SurfaceDark, Color(0xFF0E1F3D))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 28.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Learn Lab",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceHigh,
                    )
                    Text(
                        "NCERT Virtual Labs · Grades 6–10",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = SuccessGreen.copy(alpha = 0.15f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.WifiOff, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Text("Offline Ready", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings", tint = OnSurfaceMed)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Grade selector
            Text(
                "Select Class",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceMed,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (6..10).forEach { grade ->
                    GradeChip(
                        grade    = grade,
                        selected = selectedGrade == grade,
                        onClick  = { state.setSelectedGrade(grade) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Subject cards + stats
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        "Select Subject",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurfaceMed,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        subjectDefs.forEach { subject ->
                            val chapters = findChapters(selectedGrade, subject.id)
                            val hasContent = chapters.isNotEmpty()
                            SubjectCard(
                                subject     = subject,
                                hasContent  = hasContent,
                                chapCount   = chapters.size,
                                topicCount  = chapters.sumOf { it.experiments.size },
                                modifier    = Modifier.weight(1f).fillMaxHeight(),
                                onClick     = { if (hasContent) onSubjectClick(selectedGrade, subject.id) },
                            )
                        }
                    }
                }

                // Stats
                Column(
                    modifier = Modifier.width(260.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val scienceChapters = findChapters(selectedGrade, "science")
                    QuickStatsCard(
                        grade      = selectedGrade,
                        topics     = scienceChapters.sumOf { it.experiments.size },
                        chapters   = scienceChapters.size,
                    )
                }
            }
        }
    }
}

// ─── Grade chip ───────────────────────────────────────────────────────────────

@Composable
private fun GradeChip(grade: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "gradeChipScale",
    )
    Surface(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) CyanBright else SurfaceCard,
        shadowElevation = if (selected) 8.dp else 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                "Class $grade",
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) NavyDeep else OnSurfaceHigh,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Subject card ─────────────────────────────────────────────────────────────

@Composable
private fun SubjectCard(
    subject: SubjectDef,
    hasContent: Boolean,
    chapCount: Int,
    topicCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "subjectElevation",
    )
    val accent = if (hasContent) subject.color else subject.color.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clickable(enabled = hasContent) { pressed = true; onClick() },
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCard,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.18f), Color.Transparent)))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(subject.icon, null, tint = accent, modifier = Modifier.size(28.dp))
                    }
                }
                Text(
                    subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (hasContent) OnSurfaceHigh else OnSurfaceLow,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (hasContent) "$chapCount chapters · $topicCount topics"
                    else subject.description + "\nComing soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMed,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (hasContent) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.align(Alignment.BottomEnd).size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowForward, null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ─── Stats card ───────────────────────────────────────────────────────────────

@Composable
private fun QuickStatsCard(grade: Int, topics: Int, chapters: Int) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = SurfaceCard) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Class $grade · Science", style = MaterialTheme.typography.labelLarge, color = OnSurfaceMed)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("$topics",   "Topics",   CyanBright)
                Box(modifier = Modifier.height(40.dp).width(1.dp).background(SurfaceElevated))
                StatItem("$chapters", "Chapters", AmberBright)
                Box(modifier = Modifier.height(40.dp).width(1.dp).background(SurfaceElevated))
                StatItem("$grade",    "Grade",    EmeraldBright)
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = SuccessGreen.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("100% Offline Ready", style = MaterialTheme.typography.labelMedium, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceMed, maxLines = 1)
    }
}
