package com.learnlab.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SubjectBiology
import com.learnlab.design.SubjectChemistry
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics
import com.learnlab.store.AppState

/** Landing hero — mirrors the web Home: centered gradient headline, subtitle,
 *  glowing teal→green CTA, ambient floating science icons, footer. */
@Composable
fun LandingScreen(
    state: AppState,
    onBegin: () -> Unit,
    onOpenExperiment: (String) -> Unit,
    onViewAllHistory: () -> Unit,
) {
    val t = LL.tokens
    ChromeScaffold(
        state = state,
        onLogo = {},
        onOpenExperiment = onOpenExperiment,
        onViewAllHistory = onViewAllHistory,
    ) {
        Box(Modifier.fillMaxSize()) {
            // Ambient floating icons scattered across the canvas.
            FloatingIcon(Icons.Filled.Hub, SubjectPhysics, Alignment.TopStart, 0, 64.dp, 0.30f)
            FloatingIcon(Icons.Filled.Science, SubjectChemistry, Alignment.TopEnd, 1, 70.dp, 0.30f)
            FloatingIcon(Icons.Filled.Calculate, SubjectMath, Alignment.CenterStart, 2, 80.dp, 0.22f)
            FloatingIcon(Icons.Filled.Biotech, SubjectBiology, Alignment.BottomEnd, 3, 76.dp, 0.30f)

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Learn Through",
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.linearGradient(
                            listOf(SubjectPhysics, SubjectChemistry, Color(0xFF60A5FA), SubjectMath),
                        ),
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Experiments",
                    style = MaterialTheme.typography.displayLarge,
                    color = t.ink50,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                LLText(
                    "Discover the beauty of science through hands-on experiments.\nBuild intuition first, understand concepts later.",
                    color = t.ink200,
                    size = 18.sp,
                    lineHeight = 28.sp,
                    align = TextAlign.Center,
                )
                Spacer(Modifier.height(34.dp))
                PrimaryButton(
                    label = "Begin learning",
                    onClick = onBegin,
                    trailing = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0A0A0F),
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun BoxScope.FloatingIcon(
    icon: ImageVector,
    color: Color,
    alignment: Alignment,
    phase: Int,
    size: Dp,
    alpha: Float,
) {
    val transition = rememberInfiniteTransition(label = "float")
    val dy by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200 + phase * 500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dy",
    )
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(64.dp)
            .offset(y = dy.dp)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color.copy(alpha = alpha), modifier = Modifier.size(size))
    }
}

private typealias BoxScope = androidx.compose.foundation.layout.BoxScope
