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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
                .padding(horizontal = Spacing.xxxl, vertical = Spacing.xl + Spacing.xs)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    LLText(
                        text = "Learn Lab",
                        color = OnSurfaceHigh,
                        size = 32.sp,
                        weight = FontWeight.ExtraBold,
                    )
                    LLText(
                        text = "NCERT Virtual Labs · Grade 6",
                        color = CyanBright,
                        size = 13.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Surface(
                        shape = RoundedCornerShape(Radius.pill),
                        color = SuccessGreen.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs + 2.dp),
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(IconSize.sm),
                            )
                            LLText(
                                "Offline Ready",
                                color = SuccessGreen,
                                size = 13.sp,
                                weight = FontWeight.Medium,
                            )
                        }
                    }
                    IconButton(onClick = { /* settings — future */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = OnSurfaceMed,
                            modifier = Modifier.size(IconSize.lg),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Two-column layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxl),
            ) {
                // Left column — subject cards
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg + Spacing.xs),
                ) {
                    LLText(
                        "Select Subject",
                        color = OnSurfaceMed,
                        size = 14.sp,
                        weight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
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

                // Right column — stats. Min ~ for readability, max prevents it
                // stealing too much space from subject cards on wide displays.
                Column(
                    modifier = Modifier.widthIn(min = 240.dp, max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
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
            .shadow(elevation, RoundedCornerShape(Radius.xxl))
            .clickable(enabled = enabled) { pressed = true; onClick() },
        shape = RoundedCornerShape(Radius.xxl),
        color = cardColor,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.18f), Color.Transparent)))
                .padding(Spacing.lg + Spacing.xs),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm + 2.dp)) {
                Surface(
                    shape = RoundedCornerShape(Radius.md),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(IconSize.xl - 4.dp))
                    }
                }
                LLText(
                    text = title,
                    color = if (enabled) OnSurfaceHigh else OnSurfaceLow,
                    size = 18.sp,
                    weight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LLText(
                    text = if (enabled) description else "Coming soon",
                    color = OnSurfaceMed,
                    size = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (enabled) {
                Surface(
                    shape = RoundedCornerShape(Radius.pill),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.align(Alignment.BottomEnd).size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(IconSize.md),
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
        shape = RoundedCornerShape(Radius.xl),
        color = SurfaceCard,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg + Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            LLText("Quick Stats", color = OnSurfaceMed, size = 13.sp, weight = FontWeight.Medium)
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
                shape = RoundedCornerShape(Radius.pill),
                color = SuccessGreen.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(IconSize.sm),
                    )
                    Spacer(Modifier.width(Spacing.xs + 2.dp))
                    LLText(
                        "100% Offline Ready",
                        color = SuccessGreen,
                        size = 13.sp,
                        weight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LLText(value, color = color, size = 22.sp, weight = FontWeight.Bold, maxLines = 1)
        LLText(label, color = OnSurfaceMed, size = 11.sp, maxLines = 1)
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.height(40.dp).width(1.dp).background(SurfaceElevated))
}
