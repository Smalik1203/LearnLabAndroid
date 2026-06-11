package com.learnlab.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a slow-moving, gorgeous radial gradient mesh background.
 * Adapts automatically to the light/dark design theme.
 */
@Composable
fun MeshBackground(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    // Slow circular drifting angles
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )

    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw base linear background gradient
        val bgBrush = Brush.linearGradient(
            colors = listOf(t.bg, t.bgDeep),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )
        drawRect(brush = bgBrush)

        if (width > 0f && height > 0f) {
            // Glow colors
            val glowCyan = if (t.isDark) Color(0xFF0891B2).copy(alpha = 0.22f) else Color(0xFFCFFAFE).copy(alpha = 0.5f)
            val glowViolet = if (t.isDark) Color(0xFF7C3AED).copy(alpha = 0.18f) else Color(0xFFF3E8FF).copy(alpha = 0.4f)

            // Blob 1: Cyan, orbiting top-left/center
            val b1CenterX = width * 0.3f + cos(angle1) * 70.dp.toPx()
            val b1CenterY = height * 0.4f + sin(angle1) * 50.dp.toPx()
            val b1Radius = width.coerceAtLeast(height) * 0.55f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowCyan, Color.Transparent),
                    center = Offset(b1CenterX, b1CenterY),
                    radius = b1Radius
                ),
                center = Offset(b1CenterX, b1CenterY),
                radius = b1Radius
            )

            // Blob 2: Violet, orbiting bottom-right/center
            val b2CenterX = width * 0.7f + sin(angle2) * 80.dp.toPx()
            val b2CenterY = height * 0.6f + cos(angle2) * 60.dp.toPx()
            val b2Radius = width.coerceAtLeast(height) * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowViolet, Color.Transparent),
                    center = Offset(b2CenterX, b2CenterY),
                    radius = b2Radius
                ),
                center = Offset(b2CenterX, b2CenterY),
                radius = b2Radius
            )

            // 3. Draw subtle technical grid lines
            val gridStroke = if (t.isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f)
            val gridSize = 40.dp.toPx()

            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridStroke,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }

            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridStroke,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSize
            }
        }
    }
}
