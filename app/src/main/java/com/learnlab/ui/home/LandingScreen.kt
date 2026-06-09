package com.learnlab.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Landing hero — mirrors the web Home page: a gradient headline + subtitle + CTA
 * on the left, floating subject icons on the right.
 */
@Composable
fun LandingScreen(state: AppState, onBegin: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(t.bg, t.bgDeep))),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left — copy + CTA
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Learn Through\nExperiments",
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.linearGradient(
                            listOf(SubjectPhysics, SubjectChemistry, SubjectMath),
                        ),
                    ),
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(20.dp))
                LLText(
                    "Discover the beauty of science through interactive, hands-on virtual labs — built for the classroom.",
                    color = t.ink200,
                    size = 18.sp,
                    lineHeight = 28.sp,
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
                Spacer(Modifier.height(32.dp))
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

            // Right — floating subject icons
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                FloatingIcon(Icons.Filled.Science, SubjectPhysics, Alignment.TopCenter, 0, 110.dp)
                FloatingIcon(Icons.Filled.Calculate, SubjectMath, Alignment.CenterStart, 1, 92.dp)
                FloatingIcon(Icons.Filled.Biotech, SubjectChemistry, Alignment.Center, 2, 104.dp)
                FloatingIcon(Icons.Filled.Public, SubjectBiology, Alignment.BottomEnd, 3, 96.dp)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.FloatingIcon(
    icon: ImageVector,
    color: Color,
    alignment: Alignment,
    phase: Int,
    size: androidx.compose.ui.unit.Dp,
) {
    val transition = rememberInfiniteTransition(label = "float")
    val dy by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000 + phase * 400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dy",
    )
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(y = dy.dp)
            .size(size)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.06f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(size * 0.42f))
    }
}
