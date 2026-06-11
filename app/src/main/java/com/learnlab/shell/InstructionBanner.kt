package com.learnlab.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLAnimation
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.design.bounceClickable

/**
 * Premium active-step procedure HUD.
 * Animates open/closed states of instructions with spring dynamics, styled as glassmorphism.
 */
@Composable
fun InstructionBanner(steps: List<String>, activeStep: Int = 0) {
    val t = LL.tokens
    // The experiment drives the open step; a tap only peeks at another one
    // until the experiment moves on again.
    var peek by remember(steps) { mutableStateOf<Int?>(null) }
    androidx.compose.runtime.LaunchedEffect(activeStep) { peek = null }
    val openStep = (peek ?: activeStep).coerceIn(0, (steps.size - 1).coerceAtLeast(0))
    val glassBg = if (t.isDark) t.surface.copy(alpha = 0.3f) else t.surface.copy(alpha = 0.7f)
    val glassBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.1f else 0.3f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(glassBg)
                .border(1.dp, glassBorder, RoundedCornerShape(Radius.md))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText(
                "Procedure",
                color = t.ink50,
                size = 15.sp,
                weight = FontWeight.Bold,
            )
            Box(Modifier.width(16.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { i, step ->
                    StepperPill(
                        number = i + 1,
                        text = step,
                        open = i == openStep,
                        onClick = { peek = i },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperPill(
    number: Int,
    text: String,
    open: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val pillBg = if (open) t.accent50.copy(alpha = 0.15f) else Color.Transparent
    val pillBorder = if (open) t.accent300 else t.lineStrong.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                )
            )
            .clip(RoundedCornerShape(20.dp))
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(20.dp))
            .bounceClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Step number indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (open) Brush.radialGradient(listOf(t.accent700, t.accent500))
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Circle border when closed
            if (!open) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, t.lineStrong.copy(alpha = 0.8f), CircleShape)
                )
            }
            LLText(
                "$number",
                color = if (open) Color.White else t.ink400,
                size = 11.sp,
                weight = FontWeight.Bold,
            )
        }

        // Expanded text step content
        AnimatedVisibility(
            visible = open,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(8.dp))
                LLText(
                    text,
                    color = t.ink50,
                    size = 13.sp,
                    weight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}
