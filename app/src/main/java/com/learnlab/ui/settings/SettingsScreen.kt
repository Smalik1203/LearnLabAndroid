package com.learnlab.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.CyanBright
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

private val fontScaleOptions = listOf(
    Triple(1.00f, "Normal",      "Default size"),
    Triple(1.25f, "Large",       "25% bigger"),
    Triple(1.50f, "Extra Large", "50% bigger"),
)

@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit) {
    val isDark        by state.theme
    val fontScale     by state.fontSizeScale
    val keepScreen    by state.keepScreenOn
    val isFullscreen  by state.fullscreen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(NavyDeep, SurfaceDark, Color(0xFF0E1F3D))))
    ) {
        TopBar(state = state, title = "Settings", showBack = true, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurfaceHigh,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Adjust the app to suit your classroom.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanBright,
                )
            }

            // ── Appearance ───────────────────────────────────────────────────
            item {
                SettingsSection(title = "Appearance") {
                    // Theme
                    SettingRow(label = "Theme", description = "Background and colour palette") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeChip(
                                label    = "Dark",
                                selected = isDark == "dark",
                                onClick  = { if (isDark != "dark") state.toggleTheme() },
                            )
                            ThemeChip(
                                label    = "Light",
                                selected = isDark == "light",
                                onClick  = { if (isDark != "light") state.toggleTheme() },
                            )
                        }
                    }
                    SettingDivider()
                    // Text size
                    SettingRow(label = "Text Size", description = "Scales all text in the app") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fontScaleOptions.forEach { (scale, label, _) ->
                                FontScaleChip(
                                    label    = label,
                                    selected = fontScale == scale,
                                    onClick  = { state.setFontScale(scale) },
                                )
                            }
                        }
                    }
                }
            }

            // ── Classroom ────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Classroom") {
                    SettingRow(
                        label       = "Keep Screen On",
                        description = "Prevent the tablet from sleeping during a lesson",
                    ) {
                        Switch(
                            checked         = keepScreen,
                            onCheckedChange = { state.setKeepScreenOn(it) },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor       = NavyDeep,
                                checkedTrackColor       = CyanBright,
                                uncheckedTrackColor     = SurfaceMid,
                                uncheckedThumbColor     = OnSurfaceLow,
                            ),
                        )
                    }
                    SettingDivider()
                    SettingRow(
                        label       = "Fullscreen",
                        description = "Hide the system status and navigation bars",
                    ) {
                        Switch(
                            checked         = isFullscreen,
                            onCheckedChange = { state.setFullscreen(it) },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor       = NavyDeep,
                                checkedTrackColor       = CyanBright,
                                uncheckedTrackColor     = SurfaceMid,
                                uncheckedThumbColor     = OnSurfaceLow,
                            ),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = OnSurfaceLow,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.6.sp,
            modifier      = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    description: String,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = OnSurfaceHigh, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.labelSmall, color = OnSurfaceMed)
        }
        control()
    }
}

@Composable
private fun SettingDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = if (selected) CyanBright else SurfaceMid,
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = if (selected) NavyDeep else OnSurfaceMed,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FontScaleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = if (selected) CyanBright else SurfaceMid,
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = if (selected) NavyDeep else OnSurfaceMed,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
