package com.learnlab.experiments.ch06kit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.min

/**
 * A circular microscope / lens viewport: a dark rim, a light field of view, and a [draw] callback
 * that paints the specimen clipped to the circle. Optional [hitTest] maps a tap to a structure id
 * which is forwarded to [onHit] — used for the click-to-identify cell structures.
 *
 * The field radius is the same value used for both drawing and hit-testing, so taps land correctly
 * even under the shell's FitToWindow scaling (pointer coordinates are canvas-local).
 */
@Composable
fun MicroscopeCircle(
    modifier: Modifier = Modifier,
    rimColor: Color = Color(0xFF1F2937),
    hitTest: ((tap: Offset, center: Offset, radius: Float) -> String?)? = null,
    onHit: ((String) -> Unit)? = null,
    draw: DrawScope.(center: Offset, radius: Float) -> Unit,
) {
    Box(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hitTest, onHit) {
                    if (hitTest == null || onHit == null) return@pointerInput
                    detectTapGestures { tap ->
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val radius = min(size.width, size.height) / 2f * 0.92f
                        hitTest(tap, c, radius)?.let { onHit(it) }
                    }
                },
        ) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val outerR = min(size.width, size.height) / 2f
            val radius = outerR * 0.92f
            // field of view background
            drawCircle(Color(0xFFFDFDFD), radius, c)
            // specimen, clipped to the circle
            clipPath(Path().apply { addOval(Rect(c.x - radius, c.y - radius, c.x + radius, c.y + radius)) }) {
                draw(c, radius)
            }
            // dark rim (eyepiece)
            drawCircle(rimColor, radius, c, style = Stroke(outerR * 0.06f))
            drawCircle(rimColor.copy(alpha = 0.35f), radius - outerR * 0.05f, c, style = Stroke(1.5f))
        }
    }
}
