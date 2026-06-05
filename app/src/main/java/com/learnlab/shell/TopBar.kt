package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.gradLogo
import com.learnlab.store.AppState

@Composable
fun TopBar(
    state: AppState,
    title: String? = null,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    onHomeClick: (() -> Unit)? = null,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(t.surface)
            .border(width = 1.dp, color = t.line, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left side
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = t.ink200,
                    )
                }
                if (title != null) {
                    Spacer(Modifier.width(4.dp))
                    LLText(
                        title,
                        color = t.ink50,
                        size = 16.sp,
                        weight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(gradLogo()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    LLText("LearnLab", color = t.ink50, size = 17.sp, weight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    LLText("·", color = t.ink600, size = 17.sp)
                    Spacer(Modifier.width(8.dp))
                    LLText("Sciences", color = t.ink400, size = 14.sp)
                }
            }
        }

        // Right: optional home, then theme toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onHomeClick != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(t.surface2)
                        .border(1.dp, t.line, CircleShape)
                        .clickable(onClick = onHomeClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Go to home",
                        tint = t.ink400,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(t.surface2)
                    .border(1.dp, t.line, CircleShape)
                    .clickable { state.toggleTheme() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = if (state.isDark) "Switch to light theme" else "Switch to dark theme",
                    tint = t.ink400,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
