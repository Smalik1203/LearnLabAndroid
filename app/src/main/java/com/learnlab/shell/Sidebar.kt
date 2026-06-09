package com.learnlab.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.gradLogo
import com.learnlab.store.AppState
import com.learnlab.ui.navigation.Routes

/**
 * Collapsible left rail, mirroring the web sidebar (64dp collapsed ↔ 240dp expanded).
 * Holds the brand, primary nav (Home, Courses), and a theme toggle pinned at the
 * bottom. Tutorial/Contact/Help are intentionally omitted on Android.
 */
@Composable
fun Sidebar(
    state: AppState,
    current: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    var expanded by remember { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (expanded) 240.dp else 72.dp,
        animationSpec = tween(220),
        label = "sidebarWidth",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(t.surface)
            .border(width = 1.dp, color = t.line, shape = RoundedCornerShape(0.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp),
    ) {
        // Brand + collapse toggle
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradLogo()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Science,
                    contentDescription = null,
                    tint = Color(0xFF0A0A0F),
                    modifier = Modifier.size(22.dp),
                )
            }
            SidebarLabel(expanded) {
                Spacer(Modifier.width(12.dp))
                LLText("LearnLab", color = t.ink50, size = 18.sp, weight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(28.dp))

        NavItem(
            icon = Icons.Filled.Home,
            label = "Home",
            selected = current == Routes.LANDING,
            expanded = expanded,
            onClick = { onNavigate(Routes.LANDING) },
        )
        Spacer(Modifier.height(6.dp))
        NavItem(
            icon = Icons.Filled.GridView,
            label = "Courses",
            selected = current == Routes.HOME || current?.startsWith("grade-select") == true ||
                current?.startsWith("curriculum") == true,
            expanded = expanded,
            onClick = { onNavigate(Routes.HOME) },
        )

        Spacer(Modifier.weight(1f))

        // Theme toggle pinned at the bottom (web parity).
        NavItem(
            icon = if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            label = if (state.isDark) "Light mode" else "Dark mode",
            selected = false,
            expanded = expanded,
            onClick = { state.toggleTheme() },
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val fg = if (selected) t.accent700 else t.ink400
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) t.accent50 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(22.dp))
        SidebarLabel(expanded) {
            Spacer(Modifier.width(14.dp))
            LLText(label, color = fg, size = 14.sp, weight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SidebarLabel(expanded: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(tween(150)) + expandHorizontally(tween(180)),
        exit = fadeOut(tween(100)) + shrinkHorizontally(tween(150)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { content() }
    }
}
