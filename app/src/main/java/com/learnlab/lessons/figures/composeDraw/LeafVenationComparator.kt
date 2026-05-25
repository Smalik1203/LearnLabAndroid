package com.learnlab.lessons.figures.composeDraw

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * Leaf venation comparator — three leaves (hibiscus, banana, grass).
 * Tap a leaf to focus. Slider scrubs the vein-drawing animation. Each
 * leaf draws its veins from midrib outward as the slider advances.
 */
@Composable
fun LeafVenationComparator(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val p = lessonPalette()

    var focused by remember { mutableIntStateOf(-1) } // 0 hibiscus, 1 banana, 2 grass
    var reveal by remember { mutableFloatStateOf(0f) } // 0..1 scrub
    var sliderDragging by remember { mutableFloatStateOf(0f) }

    val animatedReveal by animateFloatAsState(
        targetValue = reveal, animationSpec = tween(800), label = "reveal",
    )

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header chip row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendChip("Reticulate", p.violet.accent)
            LLText("Tap a leaf to focus · drag the slider to reveal veins",
                color = t.ink500, size = 13.sp)
            LegendChip("Parallel", p.sky.accent)
        }
        // Three leaf canvases
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.72f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LeafCell(
                name = "Hibiscus",
                pattern = VeinPattern.Reticulate,
                accent = p.violet.accent,
                focused = focused == 0,
                reveal = animatedReveal,
                onTap = { focused = if (focused == 0) -1 else 0 },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            LeafCell(
                name = "Banana",
                pattern = VeinPattern.ParallelBroad,
                accent = p.sky.accent,
                focused = focused == 1,
                reveal = animatedReveal,
                onTap = { focused = if (focused == 1) -1 else 1 },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            LeafCell(
                name = "Grass",
                pattern = VeinPattern.ParallelNarrow,
                accent = p.sky.accent,
                focused = focused == 2,
                reveal = animatedReveal,
                onTap = { focused = if (focused == 2) -1 else 2 },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        // Slider
        RevealSlider(
            value = reveal,
            onChange = { reveal = it.coerceIn(0f, 1f) },
            onPreset = { reveal = it },
        )
    }
}

private enum class VeinPattern { Reticulate, ParallelBroad, ParallelNarrow }

@Composable
private fun LeafCell(
    name: String,
    pattern: VeinPattern,
    accent: Color,
    focused: Boolean,
    reveal: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(if (focused) accent.copy(alpha = 0.10f) else t.surface)
            .border(if (focused) 3.dp else 1.dp,
                if (focused) accent else t.line, RoundedCornerShape(Radius.lg))
            .clickable { onTap() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            drawLeaf(pattern, reveal, accent)
        }
        Spacer(Modifier.height(8.dp))
        LLText(name, color = if (focused) accent else t.ink50,
            size = 18.sp, weight = FontWeight.Bold)
        LLText(
            when (pattern) {
                VeinPattern.Reticulate -> "Reticulate venation"
                else -> "Parallel venation"
            },
            color = t.ink400, size = 12.sp,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    pattern: VeinPattern,
    reveal: Float,
    accent: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    val leafFill = Color(0xFFB2DFB2).copy(alpha = 0.4f)
    val leafEdge = Color(0xFF1F6B3A)
    val veinColor = accent

    when (pattern) {
        VeinPattern.Reticulate -> {
            // Broad ovate leaf
            val leafW = w * 0.78f
            val leafH = h * 0.86f
            val path = Path().apply {
                moveTo(cx, cy - leafH / 2f)
                cubicTo(cx + leafW / 2f, cy - leafH / 2.4f,
                    cx + leafW / 2f, cy + leafH / 2.4f,
                    cx, cy + leafH / 2f)
                cubicTo(cx - leafW / 2f, cy + leafH / 2.4f,
                    cx - leafW / 2f, cy - leafH / 2.4f,
                    cx, cy - leafH / 2f)
                close()
            }
            drawPath(path, color = leafFill)
            drawPath(path, color = leafEdge, style = Stroke(width = 2.5f))

            // Midrib
            drawLineProgressive(
                from = Offset(cx, cy - leafH / 2f + 6f),
                to = Offset(cx, cy + leafH / 2f - 6f),
                color = veinColor, stroke = 3f, t = reveal.coerceAtMost(0.4f) / 0.4f,
            )
            // Side veins (reticulate — branching net)
            val sideVeins = 6
            for (i in 1..sideVeins) {
                val tNorm = i.toFloat() / (sideVeins + 1)
                val y = cy - leafH / 2f + leafH * tNorm
                val phase = ((reveal - 0.4f) * (sideVeins + 1) - (i - 1)).coerceIn(0f, 1f)
                if (phase > 0f) {
                    val angle = (1f - tNorm) * 0.25f + 0.15f
                    // Right side
                    drawLineProgressive(
                        from = Offset(cx, y),
                        to = Offset(cx + leafW * 0.45f * (1f - tNorm * 0.5f), y + leafH * angle),
                        color = veinColor, stroke = 2f, t = phase,
                    )
                    // Left side
                    drawLineProgressive(
                        from = Offset(cx, y),
                        to = Offset(cx - leafW * 0.45f * (1f - tNorm * 0.5f), y + leafH * angle),
                        color = veinColor, stroke = 2f, t = phase,
                    )
                    // Net cross-links (mesh feel)
                    if (phase > 0.6f) {
                        val sub = ((phase - 0.6f) / 0.4f).coerceIn(0f, 1f)
                        val nextY = y + leafH * 0.10f
                        if (nextY < cy + leafH / 2f - 8f) {
                            drawLine(
                                color = veinColor.copy(alpha = 0.5f * sub),
                                start = Offset(cx + leafW * 0.20f, y + leafH * 0.04f),
                                end = Offset(cx + leafW * 0.30f, nextY),
                                strokeWidth = 1.2f, cap = StrokeCap.Round,
                            )
                            drawLine(
                                color = veinColor.copy(alpha = 0.5f * sub),
                                start = Offset(cx - leafW * 0.20f, y + leafH * 0.04f),
                                end = Offset(cx - leafW * 0.30f, nextY),
                                strokeWidth = 1.2f, cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }
        }
        VeinPattern.ParallelBroad -> {
            // Long elliptical (banana) leaf
            val leafW = w * 0.62f
            val leafH = h * 0.92f
            val path = Path().apply {
                moveTo(cx, cy - leafH / 2f)
                cubicTo(cx + leafW / 2f, cy - leafH / 3f,
                    cx + leafW / 2f, cy + leafH / 3f,
                    cx, cy + leafH / 2f)
                cubicTo(cx - leafW / 2f, cy + leafH / 3f,
                    cx - leafW / 2f, cy - leafH / 3f,
                    cx, cy - leafH / 2f)
                close()
            }
            drawPath(path, color = leafFill)
            drawPath(path, color = leafEdge, style = Stroke(width = 2.5f))

            // Midrib first
            drawLineProgressive(
                from = Offset(cx, cy - leafH / 2f + 6f),
                to = Offset(cx, cy + leafH / 2f - 6f),
                color = veinColor, stroke = 3f, t = reveal.coerceAtMost(0.3f) / 0.3f,
            )
            // Parallel veins curve from base to tip (like banana)
            val n = 10
            for (i in 0 until n) {
                val tNorm = (i + 1).toFloat() / (n + 1)
                val phase = ((reveal - 0.3f) * (n + 1) - i).coerceIn(0f, 1f)
                if (phase > 0f) {
                    val offsetX = (tNorm - 0.5f) * leafW * 0.85f
                    val path2 = Path().apply {
                        moveTo(cx + offsetX, cy - leafH / 2f + 12f)
                        cubicTo(
                            cx + offsetX * 0.95f, cy - leafH * 0.15f,
                            cx + offsetX * 0.95f, cy + leafH * 0.15f,
                            cx + offsetX, cy + leafH / 2f - 12f,
                        )
                    }
                    drawProgressivePath(path2, veinColor, 1.6f, phase)
                }
            }
        }
        VeinPattern.ParallelNarrow -> {
            // Very narrow grass blade
            val leafW = w * 0.18f
            val leafH = h * 0.94f
            val path = Path().apply {
                moveTo(cx, cy - leafH / 2f)
                quadraticTo(cx + leafW * 0.6f, cy - leafH * 0.1f, cx + leafW * 0.2f, cy + leafH / 2f)
                lineTo(cx - leafW * 0.2f, cy + leafH / 2f)
                quadraticTo(cx - leafW * 0.6f, cy - leafH * 0.1f, cx, cy - leafH / 2f)
                close()
            }
            drawPath(path, color = leafFill)
            drawPath(path, color = leafEdge, style = Stroke(width = 2f))
            // Many tight parallel lines
            val n = 7
            for (i in 0 until n) {
                val tNorm = (i + 1).toFloat() / (n + 1)
                val phase = ((reveal) * (n + 1) - i).coerceIn(0f, 1f)
                if (phase > 0f) {
                    val offsetX = (tNorm - 0.5f) * leafW * 0.7f
                    val path2 = Path().apply {
                        moveTo(cx + offsetX, cy - leafH / 2f + 8f)
                        quadraticTo(
                            cx + offsetX * 1.1f, cy,
                            cx + offsetX * 0.5f, cy + leafH / 2f - 8f,
                        )
                    }
                    drawProgressivePath(path2, veinColor, 1.4f, phase)
                }
            }
        }
    }
}

/** Draws a line from `from` toward `to`, where `t∈[0,1]` controls how much. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLineProgressive(
    from: Offset, to: Offset, color: Color, stroke: Float, t: Float,
) {
    if (t <= 0f) return
    val tip = Offset(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)
    drawLine(color = color, start = from, end = tip, strokeWidth = stroke, cap = StrokeCap.Round)
}

/** Approximates progressive draw of a curved path by dashing — simple but readable. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProgressivePath(
    path: Path, color: Color, stroke: Float, t: Float,
) {
    // We can't easily clip a Path by length without PathMeasure. Use opacity ramp as
    // a stand-in: at t=1 fully opaque, at lower t fades in. For an IFP it reads as
    // "this vein is appearing".
    drawPath(path, color = color.copy(alpha = t), style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/* ─────────────── Slider + legend chip ─────────────── */

@Composable
private fun RevealSlider(value: Float, onChange: (Float) -> Unit, onPreset: (Float) -> Unit) {
    val t = LL.tokens
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Reveal veins", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetChip("Hide", value == 0f) { onPreset(0f) }
                PresetChip("Midrib", value > 0f && value < 0.5f) { onPreset(0.35f) }
                PresetChip("All", value >= 0.95f) { onPreset(1f) }
            }
        }
        SliderTrack(value = value, onChange = onChange)
    }
}

@Composable
private fun PresetChip(label: String, active: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(if (active) t.accent500 else t.surface2)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        LLText(label, color = if (active) t.surface else t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        LLText(label, color = color, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
