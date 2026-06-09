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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.design.SubjectBiology
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics
import com.learnlab.store.AppState

@Composable
fun HomeScreen(state: AppState, onScienceClick: () -> Unit) {
    val t = LL.tokens
    var tab by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(t.bg, t.bgDeep))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 40.dp),
        ) {
            LLText("What do you want to learn?", color = t.ink50, size = 30.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LLText(
                "Pick a subject to explore its interactive labs.",
                color = t.ink400, size = 16.sp,
            )

            Spacer(Modifier.height(24.dp))
            PillTabBar(
                tabs = listOf("Interactive Labs", "Tutorials"),
                selected = tab,
                onSelect = { tab = it },
                enabledTabs = listOf(true, false),
                modifier = Modifier.fillMaxWidth(0.5f),
            )

            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SubjectCard(
                    title = "Science",
                    icon = Icons.Filled.Science,
                    color = SubjectPhysics,
                    description = "Physics · Chemistry · Biology",
                    modifier = Modifier.weight(1f).height(280.dp),
                    onClick = onScienceClick,
                )
                SubjectCard(
                    title = "Mathematics",
                    icon = Icons.Filled.Calculate,
                    color = SubjectMath,
                    description = "Geometry · Algebra · Statistics",
                    modifier = Modifier.weight(1f).height(280.dp),
                    enabled = false,
                    onClick = {},
                )
                SubjectCard(
                    title = "Social Science",
                    icon = Icons.Filled.Public,
                    color = SubjectBiology,
                    description = "History · Geography · Civics",
                    modifier = Modifier.weight(1f).height(280.dp),
                    enabled = false,
                    onClick = {},
                )
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
    val accent = if (enabled) color else color.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                    ),
                )
                .padding(24.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(18.dp))
                LLText(
                    title,
                    color = if (enabled) t.ink50 else t.ink500,
                    size = 24.sp,
                    weight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                LLText(
                    if (enabled) description else "Coming soon",
                    color = t.ink400,
                    size = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
