package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.AllExperiments
import com.learnlab.content.Chapters
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.gradLogo
import com.learnlab.store.AppState

@Composable
fun TopBar(state: AppState) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(t.surface)
            .border(width = 1.dp, color = t.line, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: logo + brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(gradLogo()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
            Box(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                LLText("LearnLab", color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold)
                Box(Modifier.size(8.dp))
                LLText("·", color = t.ink600, size = 16.sp)
                Box(Modifier.size(8.dp))
                LLText("Sciences", color = t.ink400, size = 14.sp)
            }
        }

        // Right: count + theme toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText(
                "${AllExperiments.size} experiments across ${Chapters.size} chapters",
                color = t.ink500, size = 12.sp,
            )
            Box(Modifier.size(12.dp))
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
