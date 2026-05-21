package com.learnlab.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learnlab.content.AllExperiments
import com.learnlab.content.Chapters
import com.learnlab.design.AmberBright
import com.learnlab.design.CyanBright
import com.learnlab.design.EmeraldBright
import com.learnlab.design.NavyDeep
import com.learnlab.design.OnSurfaceHigh
import com.learnlab.design.OnSurfaceLow
import com.learnlab.design.OnSurfaceMed
import com.learnlab.design.SurfaceCard
import com.learnlab.design.SurfaceDark
import com.learnlab.design.SurfaceElevated
import com.learnlab.design.SuccessGreen
import com.learnlab.store.AppState

@Composable
fun HomeScreen(state: AppState, onScienceClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(NavyDeep, SurfaceDark, Color(0xFF0E1F3D)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Learn Lab",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceHigh,
                    )
                    Text(
                        text = "NCERT Virtual Labs · Grade 6",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SuccessGreen.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                "Offline Ready",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessGreen,
                            )
                        }
                    }
                    IconButton(onClick = { /* settings — future */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OnSurfaceMed)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Two-column layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                // Left column — subject cards
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
                        SubjectCard(
                            title = "Science",
                            icon = Icons.Default.Science,
                            color = CyanBright,
                            description = "Physics · Chemistry · Biology",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = onScienceClick,
                        )
                        SubjectCard(
                            title = "Mathematics",
                            icon = Icons.Default.Calculate,
                            color = AmberBright,
                            description = "Geometry · Algebra · Statistics",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            enabled = false,
                            onClick = {},
                        )
                        SubjectCard(
                            title = "Social Science",
                            icon = Icons.Default.Public,
                            color = EmeraldBright,
                            description = "History · Geography · Civics",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            enabled = false,
                            onClick = {},
                        )
                    }
                }

                // Right column — stats
                Column(
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    QuickStatsCard(
                        labsAvailable = AllExperiments.size,
                        chaptersSeeded = Chapters.size,
                        grade = 6,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    title: String,
    icon: ImageVector,
    color: Color,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "subjectElevation",
    )
    val cardColor = if (enabled) SurfaceCard else SurfaceCard.copy(alpha = 0.5f)
    val accentColor = if (enabled) color else color.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clickable(enabled = enabled) { pressed = true; onClick() },
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.18f), Color.Transparent)))
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) OnSurfaceHigh else OnSurfaceLow,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (enabled) description else "Coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMed,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (enabled) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.align(Alignment.BottomEnd).size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(labsAvailable: Int, chaptersSeeded: Int, grade: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Quick Stats", style = MaterialTheme.typography.labelLarge, color = OnSurfaceMed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem("$labsAvailable", "Labs", CyanBright)
                VerticalDivider()
                StatItem("$chaptersSeeded", "Chapters", AmberBright)
                VerticalDivider()
                StatItem("$grade", "Grade", EmeraldBright)
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
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "100% Offline Ready",
                        style = MaterialTheme.typography.labelMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
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

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.height(40.dp).width(1.dp).background(SurfaceElevated))
}
