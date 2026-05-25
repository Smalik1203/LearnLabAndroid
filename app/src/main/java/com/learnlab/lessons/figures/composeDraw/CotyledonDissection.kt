package com.learnlab.lessons.figures.composeDraw

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * Chickpea (dicot) splits into two halves; maize (monocot) wobbles but stays whole.
 * Slider drives the split. Tap chickpea to trigger a split-and-snap-back; tap maize
 * to make it wobble.
 */
@Composable
fun CotyledonDissection(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val p = lessonPalette()
    var split by remember { mutableFloatStateOf(0f) } // 0 closed, 1 fully split (chickpea only)
    var maizeWobble by remember { mutableFloatStateOf(0f) }
    var focused by remember { mutableIntStateOf(-1) } // 0 dicot 1 monocot

    val splitAnim by animateFloatAsState(split, tween(700), label = "split")
    val wobbleAnim by animateFloatAsState(maizeWobble, tween(400), label = "wobble")

    Column(modifier = modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Inside a seed", color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
            LLText("Tap each seed · drag slider to split the chickpea", color = t.ink500, size = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg)),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left: chickpea
                Box(
                    modifier = Modifier
                        .weight(1f).fillMaxHeight()
                        .clickable {
                            focused = if (focused == 0) -1 else 0
                            split = if (split > 0.5f) 0f else 1f
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawChickpea(splitAnim, focused == 0, p.violet.accent)
                    }
                }
                // Divider
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(t.line))
                // Right: maize
                Box(
                    modifier = Modifier
                        .weight(1f).fillMaxHeight()
                        .clickable {
                            focused = if (focused == 1) -1 else 1
                            maizeWobble = if (maizeWobble > 0.5f) 0f else 1f
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawMaize(wobbleAnim, focused == 1, p.sky.accent)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val info = when (focused) {
            0 -> Triple("Chickpea — Dicot",
                "Splits into two halves. Each half is a cotyledon — stored food for the baby plant.",
                p.violet.accent)
            1 -> Triple("Maize — Monocot",
                "Stays whole. Has only one cotyledon inside.",
                p.sky.accent)
            else -> Triple("Two seeds, two patterns",
                "Tap each seed to learn — or drag the slider to split the chickpea.",
                t.ink400)
        }
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
                .background(t.surface2).padding(14.dp),
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(info.third))
            Spacer(Modifier.width(10.dp))
            Column {
                LLText(info.first, color = t.ink50, size = 15.sp, weight = FontWeight.Bold)
                LLText(info.second, color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        SplitSlider(value = split, onChange = { split = it })
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChickpea(
    split: Float, focused: Boolean, accent: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = kotlin.math.min(w, h) * 0.30f
    val bean = Color(0xFFD97706)
    val beanShade = Color(0xFFB45309)
    val embryo = Color(0xFF22C55E)

    val splitOffset = r * 1.05f * split

    // Left half
    val leftCx = cx - splitOffset
    drawHalfBean(Offset(leftCx, cy), r, isLeft = true, bean, beanShade)
    // Right half
    val rightCx = cx + splitOffset
    drawHalfBean(Offset(rightCx, cy), r, isLeft = false, bean, beanShade)

    // When split, draw the tiny embryo between halves and label cotyledons
    if (split > 0.4f) {
        val alpha = ((split - 0.4f) / 0.6f).coerceIn(0f, 1f)
        // Embryo at bottom centre
        drawCircle(color = embryo.copy(alpha = alpha), radius = r * 0.10f,
            center = Offset(cx, cy + r * 0.10f))
        // Small "baby leaves" sprouting up
        drawLine(color = embryo.copy(alpha = alpha),
            start = Offset(cx, cy + r * 0.10f),
            end = Offset(cx, cy - r * 0.10f),
            strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Pointer arrows + labels for both cotyledons
        if (split > 0.7f) {
            val a = ((split - 0.7f) / 0.3f).coerceIn(0f, 1f)
            // Arrow to left half
            drawLine(color = accent.copy(alpha = a),
                start = Offset(leftCx - r * 0.6f, cy - r * 1.3f),
                end = Offset(leftCx - r * 0.2f, cy - r * 0.8f),
                strokeWidth = 2f)
            drawCircle(color = accent.copy(alpha = a), radius = 4f,
                center = Offset(leftCx - r * 0.2f, cy - r * 0.8f))
            // Arrow to right half
            drawLine(color = accent.copy(alpha = a),
                start = Offset(rightCx + r * 0.6f, cy - r * 1.3f),
                end = Offset(rightCx + r * 0.2f, cy - r * 0.8f),
                strokeWidth = 2f)
            drawCircle(color = accent.copy(alpha = a), radius = 4f,
                center = Offset(rightCx + r * 0.2f, cy - r * 0.8f))
        }
    }

    if (focused) {
        drawCircle(color = accent.copy(alpha = 0.2f), radius = r * 1.6f,
            center = Offset(cx, cy), style = Stroke(width = 4f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHalfBean(
    center: Offset, r: Float, isLeft: Boolean, bean: Color, shade: Color,
) {
    // Roughly a vertical "D" — flat side faces inward
    val path = Path().apply {
        if (isLeft) {
            // Flat right edge
            moveTo(center.x, center.y - r)
            cubicTo(center.x - r * 1.2f, center.y - r * 0.9f,
                center.x - r * 1.2f, center.y + r * 0.9f,
                center.x, center.y + r)
            lineTo(center.x, center.y - r)
            close()
        } else {
            // Flat left edge
            moveTo(center.x, center.y - r)
            cubicTo(center.x + r * 1.2f, center.y - r * 0.9f,
                center.x + r * 1.2f, center.y + r * 0.9f,
                center.x, center.y + r)
            lineTo(center.x, center.y - r)
            close()
        }
    }
    drawPath(path, color = bean)
    drawPath(path, color = shade, style = Stroke(width = 2f))
    // Inner highlight
    drawArc(
        color = bean.copy(alpha = 0.5f).let { Color(0xFFFEF3C7).copy(alpha = 0.5f) },
        startAngle = if (isLeft) 120f else 240f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
        size = Size(r * 1.6f, r * 1.6f),
        style = Stroke(width = 4f, cap = StrokeCap.Round),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMaize(
    wobble: Float, focused: Boolean, accent: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val rW = kotlin.math.min(w, h) * 0.22f
    val rH = kotlin.math.min(w, h) * 0.34f
    val tilt = kotlin.math.sin(wobble * Math.PI * 4f).toFloat() * 4f

    val kernelColor = Color(0xFFFBBF24)
    val kernelShade = Color(0xFFB45309)
    val embryo = Color(0xFF22C55E)

    // Kernel body — teardrop / oval pointing down
    val path = Path().apply {
        moveTo(cx + tilt, cy - rH)
        cubicTo(cx + rW * 1.1f + tilt, cy - rH * 0.5f,
            cx + rW * 0.9f + tilt, cy + rH * 0.7f,
            cx + tilt, cy + rH)
        cubicTo(cx - rW * 0.9f + tilt, cy + rH * 0.7f,
            cx - rW * 1.1f + tilt, cy - rH * 0.5f,
            cx + tilt, cy - rH)
        close()
    }
    drawPath(path, color = kernelColor)
    drawPath(path, color = kernelShade, style = Stroke(width = 2f))

    // Single embryo at the bottom (the monocot teaching point: ONE cotyledon)
    drawOval(color = embryo,
        topLeft = Offset(cx - rW * 0.3f + tilt, cy + rH * 0.5f),
        size = Size(rW * 0.6f, rH * 0.4f))
    drawOval(color = Color(0xFF15803D),
        topLeft = Offset(cx - rW * 0.3f + tilt, cy + rH * 0.5f),
        size = Size(rW * 0.6f, rH * 0.4f),
        style = Stroke(width = 1.5f))
    // Label hint when focused
    if (focused) {
        drawCircle(color = accent.copy(alpha = 0.2f),
            radius = rH * 1.3f,
            center = Offset(cx, cy),
            style = Stroke(width = 4f))
    }
}

@Composable
private fun SplitSlider(value: Float, onChange: (Float) -> Unit) {
    val t = LL.tokens
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Split the chickpea", color = t.ink400, size = 13.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetSplit("Closed", value < 0.15f) { onChange(0f) }
                PresetSplit("Half", value in 0.45f..0.55f) { onChange(0.5f) }
                PresetSplit("Open", value > 0.95f) { onChange(1f) }
            }
        }
        SliderTrack(value = value, onChange = onChange)
    }
}

@Composable
private fun PresetSplit(label: String, active: Boolean, onClick: () -> Unit) {
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
