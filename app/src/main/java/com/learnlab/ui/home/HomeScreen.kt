package com.learnlab.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
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
import com.learnlab.design.SecondaryButton
import com.learnlab.design.SuccessGreen
import com.learnlab.store.AppState

@Composable
fun HomeScreen(state: AppState, onScienceClick: () -> Unit) {
    val t = LL.tokens
    var showSettings by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(t.bg, t.bgDeep)))
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
                        color = t.ink50,
                    )
                    Text(
                        text = "NCERT Virtual Labs",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanBright,
                    )
                }
                Box {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = t.ink400)
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

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    "Select Subject",
                    style = MaterialTheme.typography.titleSmall,
                    color = t.ink400,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SubjectCard(
                        title = "Science",
                        icon = Icons.Default.Science,
                        color = CyanBright,
                        description = "Physics · Chemistry · Biology",
                        modifier = Modifier.weight(1f).height(220.dp),
                        onClick = onScienceClick,
                    )
                    SubjectCard(
                        title = "Mathematics",
                        icon = Icons.Default.Calculate,
                        color = AmberBright,
                        description = "Geometry · Algebra · Statistics",
                        modifier = Modifier.weight(1f).height(220.dp),
                        enabled = false,
                        onClick = {},
                    )
                    SubjectCard(
                        title = "Social Science",
                        icon = Icons.Default.Public,
                        color = EmeraldBright,
                        description = "History · Geography · Civics",
                        modifier = Modifier.weight(1f).height(220.dp),
                        enabled = false,
                        onClick = {},
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
    val t = LL.tokens
    val cardColor = if (enabled) t.surface else t.surface.copy(alpha = 0.5f)
    val accentColor = if (enabled) color else color.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.22f), accentColor.copy(alpha = 0.06f))))
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(36.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) t.ink50 else t.ink500,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (enabled) description else "Coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = t.ink400,
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
private fun SettingsMenu(state: AppState, onDismiss: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .width(300.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.lineStrong, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LLText("SETTINGS", color = t.accent700, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.6.sp)

        // — Appearance —
        LLText("Appearance", color = t.ink200, size = 13.sp,
            weight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))

        // — About —
        LLText("About", color = t.ink200, size = 13.sp,
            weight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LLText("LearnLab",
                color = t.ink50, size = 14.sp, weight = FontWeight.Bold)
            LLText("NCERT Virtual Labs · for teacher-led tablet classrooms.",
                color = t.ink400, size = 11.sp, lineHeight = 14.sp)
            LLText("v0.0.1 · NCERT Grade 6–10 Science",
                color = t.ink500, size = 10.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            SecondaryButton(label = "Got it", onClick = onDismiss)
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
    val bg = if (selected) t.accent50 else t.surface2
    val fg = if (selected) t.accent700 else t.ink400
    val borderColor = if (selected) t.accent500 else t.line
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        LLText(label, color = fg, size = 13.sp, weight = FontWeight.SemiBold)
    }
}
