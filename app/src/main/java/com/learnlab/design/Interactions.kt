package com.learnlab.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Web-style interactive card surface: a subtle hover lift (scale + shadow +
 * subject-colored border glow) and a gentle press-in, driven off a shared
 * [interactionSource]. Pass that same source to the card's `clickable` so the
 * states stay in sync. Hover fires for mouse/stylus; press for touch.
 *
 * Applies graphicsLayer → shadow → clip → background → border → hoverable, so
 * the card body just adds `clickable(interactionSource, …)` + content.
 */
@Composable
fun Modifier.interactiveCard(
    interactionSource: MutableInteractionSource,
    glowColor: Color,
    shape: Shape = RoundedCornerShape(20.dp),
    background: Color = LL.tokens.surface,
    backgroundBrush: Brush? = null,
    hoverScale: Float = 1.03f,
    pressScale: Float = 0.97f,
): Modifier {
    val t = LL.tokens
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    // The target device is a touch tablet where hover never fires, so the lift,
    // glow and shadow react to press too — that's the only feedback on touch.
    val active = hovered || pressed

    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> pressScale
            hovered -> hoverScale
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "cardScale",
    )
    val lift by animateDpAsState(
        targetValue = if (hovered && !pressed) (-2).dp else 0.dp,
        animationSpec = tween(220),
        label = "cardLift",
    )
    val elevation by animateDpAsState(
        targetValue = if (active) 18.dp else 0.dp,
        animationSpec = tween(220),
        label = "cardElevation",
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) glowColor.copy(alpha = 0.55f) else t.line,
        animationSpec = tween(220),
        label = "cardBorder",
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationY = lift.toPx()
        }
        .shadow(elevation, shape, clip = false, spotColor = glowColor, ambientColor = glowColor)
        .clip(shape)
        .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier.background(background))
        .border(1.dp, borderColor, shape)
        .hoverable(interactionSource)
}
