package com.learnlab.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Extension modifier that applies a premium spring-loaded scaling effect
 * when pressed, giving a tactile "press down" feedback.
 */
@Composable
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    if (!enabled) return this
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = LLAnimation.TactileSpring,
        label = "bounceClick"
    )
    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Using scale instead of ripple for ultra-clean look
            enabled = enabled,
            onClick = onClick
        )
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val t = LL.tokens
    val bgBrush: Brush = if (enabled) gradCta() else Brush.linearGradient(listOf(t.surface3, t.surface3))
    val textColor = if (enabled) Color.White else t.ink500

    Box(
        modifier = modifier
            .bounceClickable(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(Radius.pill))
            .background(bgBrush)
            .border(
                width = 1.dp,
                color = if (enabled) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(Radius.pill)
            )
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(8.dp)) }
            LLText(
                label,
                color = textColor,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            if (trailing != null) { Box(Modifier.width(8.dp)); trailing() }
        }
    }
}

@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val t = LL.tokens
    val glassBg = if (t.isDark) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.75f)
    val glassBorder = if (t.isDark) t.lineStrong.copy(alpha = 0.3f) else t.lineStrong.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .bounceClickable(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(Radius.pill))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(Radius.pill))
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(8.dp)) }
            LLText(
                label,
                color = if (enabled) t.ink200 else t.ink600,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            if (trailing != null) { Box(Modifier.width(8.dp)); trailing() }
        }
    }
}

@Composable
fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .bounceClickable(onClick = onClick)
            .clip(RoundedCornerShape(Radius.sm))
            .background(t.surface2.copy(alpha = 0.4f))
            .border(1.dp, t.lineStrong.copy(alpha = 0.3f), RoundedCornerShape(Radius.sm))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = t.ink200, size = 13.sp, weight = FontWeight.Medium)
    }
}

/**
 * Premium glassmorphic card container with rounded corners, translucent background,
 * and vertical gradient border highlights.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val t = LL.tokens
    val cardBg = if (t.isDark) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.85f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f),
            Color.White.copy(alpha = 0.0f)
        )
    )

    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(Radius.lg),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(Radius.lg))
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
fun ChapterBadge(number: Int) {
    val t = LL.tokens
    LLText(
        "CHAPTER $number",
        color = t.ink500,
        size = 11.sp,
        weight = FontWeight.Bold,
        letterSpacing = 2.0.sp,
    )
}

@Composable
fun NumberPip(n: Int) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(t.accent50.copy(alpha = 0.3f))
            .border(1.dp, t.accent300, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LLText("$n", color = t.accent700, size = 15.sp, weight = FontWeight.Bold)
    }
}

/**
 * A beautiful glowing progress bar. Features a subtle neon background glow.
 */
@Composable
fun ProgressBar(value: Float, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(t.surface3.copy(alpha = 0.4f)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glowing base
        if (value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF06B6D4).copy(alpha = 0.3f), Color(0xFF10B981).copy(alpha = 0.3f))
                        )
                    )
            )
            // Main solid line
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .height(5.dp)
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(gradProgress()),
            )
        }
    }
}
