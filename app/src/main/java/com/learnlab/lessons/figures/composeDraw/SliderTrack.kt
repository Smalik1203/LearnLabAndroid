package com.learnlab.lessons.figures.composeDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.learnlab.design.LL
import com.learnlab.design.Radius

/**
 * A shared drag slider for the interactive diagrams. Drags update [value] in [0,1].
 *
 * Uses BoxWithConstraints + offset so the thumb position is always non-negative.
 */
@Composable
fun SliderTrack(
    value: Float,
    onChange: (Float) -> Unit,
    height: Dp = 36.dp,
) {
    val t = LL.tokens
    val v = value.coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x.coerceIn(0f, size.width.toFloat())
                    onChange((x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = maxWidth
        val thumbDiameter = 24.dp
        // Track (background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(t.surface2),
        )
        // Track (filled)
        Box(
            modifier = Modifier
                .fillMaxWidth(v)
                .height(8.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(t.accent500),
        )
        // Thumb — positioned via offset, never negative
        val thumbX = (trackWidth * v - thumbDiameter / 2f).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .offset(x = thumbX)
                .size(thumbDiameter)
                .clip(CircleShape)
                .background(t.accent500)
                .border(3.dp, Color.White, CircleShape),
        )
    }
}
