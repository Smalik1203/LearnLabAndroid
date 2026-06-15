package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.learnlab.design.LL
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics
import com.learnlab.store.AppState

/**
 * Shared chrome for non-immersive pages (landing, learn, tutorials):
 * top nav, the content area, a right-edge history FAB, and a bottom-right theme
 * FAB — all over the dark page-gradient background.
 */
@Composable
fun PageChrome(
    state: AppState,
    onLogo: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onHistory: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val t = LL.tokens
    val dark = t.isDark
    // Web page background (styles.css): vertical near-black base + a soft cyan glow
    // on the left and a purple glow on the right. Light mode stays a plain wash.
    val baseTop = t.bg
    val baseMid = t.surface
    val baseBottom = t.bgDeep
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (dark) {
                    drawRect(
                        Brush.verticalGradient(
                            0f to baseTop,
                            0.22f to baseTop,
                            0.60f to baseMid,
                            1f to baseBottom,
                        ),
                    )
                    val r = size.width * 0.55f
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SubjectPhysics.copy(alpha = 0.10f), Color.Transparent),
                            center = Offset(size.width * 0.18f, size.height * 0.45f),
                            radius = r,
                        ),
                    )
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SubjectMath.copy(alpha = 0.10f), Color.Transparent),
                            center = Offset(size.width * 0.82f, size.height * 0.50f),
                            radius = r,
                        ),
                    )
                } else {
                    drawRect(Brush.verticalGradient(listOf(baseTop, baseBottom)))
                }
            },
    ) {
        Column(Modifier.fillMaxSize()) {
            TopNav(
                onLogo = onLogo,
                onSearch = onSearch,
                onHistory = onHistory,
                onLogin = onLogin,
                onSignUp = onSignUp,
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }

        // Bottom-right theme toggle FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(t.surface)
                .border(1.dp, t.line, CircleShape)
                .clickable { state.toggleTheme() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle theme",
                tint = t.ink400,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
