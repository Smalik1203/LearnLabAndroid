package com.learnlab.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.learnlab.design.AmberBright
import com.learnlab.design.CyanBright
import com.learnlab.design.EmeraldBright
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.MeshBackground
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.Spacing
import com.learnlab.design.bounceClickable
import com.learnlab.design.gradHeadline
import com.learnlab.store.AppState
import kotlin.math.sin

@Composable
fun HomeScreen(state: AppState, onScienceClick: () -> Unit) {
    val t = LL.tokens
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gorgeous Mesh Background
        MeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xxxl, vertical = Spacing.xl)
        ) {
            // Floating Header Bar area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "LearnLab",
                        style = TextStyle(
                            brush = gradHeadline(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    LLText(
                        text = "NCERT Virtual Science Labs",
                        color = t.ink400,
                        size = 13.sp,
                        weight = FontWeight.Medium
                    )
                }
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(t.surface.copy(alpha = if (t.isDark) 0.3f else 0.7f))
                            .border(1.dp, t.line.copy(alpha = 0.4f), CircleShape)
                            .bounceClickable { showSettings = !showSettings },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = t.ink200,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (showSettings) {
                        val density = LocalDensity.current
                        val offsetPx = with(density) { 56.dp.roundToPx() }
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, offsetPx),
                            onDismissRequest = { showSettings = false },
                            properties = PopupProperties(focusable = true),
                        ) {
                            SettingsMenu(state = state, onDismiss = { showSettings = false })
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Main Curriculum Selection Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                LLText(
                    "Select Subject",
                    color = t.ink50,
                    size = 18.sp,
                    weight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SubjectCard(
                        title = "Science",
                        icon = Icons.Default.Science,
                        color = CyanBright,
                        description = "Interactive Physics, Chemistry, & Biology experiments.",
                        modifier = Modifier.weight(1f).height(240.dp),
                        onClick = onScienceClick,
                        subjectType = "science"
                    )
                    SubjectCard(
                        title = "Mathematics",
                        icon = Icons.Default.Calculate,
                        color = AmberBright,
                        description = "Visualise geometry, algebra, and statistics.",
                        modifier = Modifier.weight(1f).height(240.dp),
                        enabled = false,
                        onClick = {},
                        subjectType = "math"
                    )
                    SubjectCard(
                        title = "Social Science",
                        icon = Icons.Default.Public,
                        color = EmeraldBright,
                        description = "Explore interactive maps, civics, & history assets.",
                        modifier = Modifier.weight(1f).height(240.dp),
                        enabled = false,
                        onClick = {},
                        subjectType = "social"
                    )
                }
            }
        }
    }
}

/**
 * Premium SubjectCard with subject-specific vector lines drawn dynamically on canvas background.
 */
@Composable
private fun SubjectCard(
    title: String,
    icon: ImageVector,
    color: Color,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    subjectType: String
) {
    val t = LL.tokens
    val cardColor = if (enabled) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.15f)
    val accentColor = if (enabled) color else color.copy(alpha = 0.4f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f),
            Color.Transparent
        )
    )

    Surface(
        modifier = modifier
            .bounceClickable(enabled = enabled, onClick = onClick)
            .shadow(
                elevation = if (enabled) 12.dp else 4.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(Radius.lg),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor)
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.12f), accentColor.copy(alpha = 0.02f))
                    )
                )
                .border(1.dp, cardBorder, RoundedCornerShape(Radius.lg))
        ) {
            // Draw subject-specific premium vector background lines
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                when (subjectType) {
                    "science" -> {
                        // Drawing atom orbital rings
                        drawCircle(
                            color = accentColor.copy(alpha = 0.04f),
                            center = Offset(w * 0.85f, h * 0.3f),
                            radius = 90.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.06f),
                            center = Offset(w * 0.85f, h * 0.3f),
                            radius = 60.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.08f),
                            center = Offset(w * 0.85f, h * 0.3f),
                            radius = 30.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    "math" -> {
                        // Draw trigonometric sine waves
                        val path = Path()
                        val startY = h * 0.4f
                        path.moveTo(0f, startY)
                        for (x in 0..w.toInt() step 5) {
                            val y = startY + sin(x.toFloat() * 0.02f) * 18.dp.toPx()
                            path.lineTo(x.toFloat(), y)
                        }
                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = 0.05f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                    "social" -> {
                        // Draw globe grids
                        val centerY = h * 0.3f
                        val centerX = w * 0.8f
                        drawCircle(
                            color = accentColor.copy(alpha = 0.04f),
                            center = Offset(centerX, centerY),
                            radius = 80.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawLine(
                            color = accentColor.copy(alpha = 0.04f),
                            start = Offset(centerX - 80.dp.toPx(), centerY),
                            end = Offset(centerX + 80.dp.toPx(), centerY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Foreground layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(accentColor.copy(alpha = 0.12f))
                            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(Radius.sm)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    if (enabled) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = t.surface3.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            LLText(
                                "Locked",
                                color = t.ink600,
                                size = 11.sp,
                                weight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LLText(
                        text = title,
                        color = if (enabled) t.ink50 else t.ink500,
                        size = 20.sp,
                        weight = FontWeight.Bold,
                    )
                    LLText(
                        text = if (enabled) description else "Coming soon to your curriculum.",
                        color = t.ink400,
                        size = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenu(state: AppState, onDismiss: () -> Unit) {
    val t = LL.tokens
    val menuBg = if (t.isDark) t.surface.copy(alpha = 0.65f) else t.surface.copy(alpha = 0.9f)
    val menuBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Column(
        modifier = Modifier
            .width(320.dp)
            .shadow(16.dp, RoundedCornerShape(Radius.md))
            .clip(RoundedCornerShape(Radius.md))
            .background(menuBg)
            .border(1.dp, menuBorder, RoundedCornerShape(Radius.md))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LLText(
            "SETTINGS",
            color = t.accent700,
            size = 11.sp,
            weight = FontWeight.Bold,
            letterSpacing = 1.8.sp
        )

        // — Appearance —
        LLText("Appearance", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ThemePill(
                label = "Light",
                icon = Icons.Default.LightMode,
                selected = !state.isDark,
                onClick = { if (state.isDark) state.toggleTheme() },
                modifier = Modifier.weight(1f),
            )
            ThemePill(
                label = "Dark",
                icon = Icons.Default.DarkMode,
                selected = state.isDark,
                onClick = { if (!state.isDark) state.toggleTheme() },
                modifier = Modifier.weight(1f),
            )
        }

        // Divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line.copy(alpha = 0.4f)))

        // — About —
        LLText("About System", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LLText("LearnLab Portal", color = t.ink200, size = 13.sp, weight = FontWeight.Bold)
            LLText("Interactive NCERT content designed for flat panels and student tablets.", color = t.ink400, size = 11.sp, lineHeight = 15.sp)
            LLText("v1.0.0 (High Fidelity Edition)", color = t.ink500, size = 10.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            SecondaryButton(label = "Done", onClick = onDismiss)
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val bg = if (selected) t.accent50.copy(alpha = 0.25f) else t.surface2.copy(alpha = 0.2f)
    val fg = if (selected) t.accent700 else t.ink400
    val borderColor = if (selected) t.accent300 else t.line.copy(alpha = 0.4f)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .bounceClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        LLText(label, color = fg, size = 13.sp, weight = FontWeight.SemiBold)
    }
}
