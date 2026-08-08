package com.learnlab.experiments.ch06kit

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared lab-glassware Canvas drawers (beakers, bowls, rods) for the fermentation simulations
 * (yeast dough, curd). Extracted from the original CurdFormation.
 */

internal fun DrawScope.beakerAt(bx: Float, by: Float, bw: Float, bh: Float, fill: Color, fillFrac: Float) {
    val glass = Color(0xFFADB5BD); val rim = Color(0xFF6B7280)
    val liquidTop = by + bh * (1f - fillFrac); val liquidBottom = by + bh - 3f
    drawRect(fill, topLeft = Offset(bx + 4f, liquidTop), size = Size(bw - 8f, (liquidBottom - liquidTop).coerceAtLeast(0f)))
    drawRect(Color(0xFFEFF6FF).copy(alpha = 0.18f), topLeft = Offset(bx, by), size = Size(bw, bh))
    drawLine(glass, Offset(bx, by), Offset(bx, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx + bw, by), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx, by + bh), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx - 5f, by), Offset(bx + 8f, by), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx + bw - 8f, by), Offset(bx + bw + 5f, by), strokeWidth = 3f, cap = StrokeCap.Round)
}

internal fun DrawScope.drawCurdBowl(c: Offset, r: Float) {
    val edge = Color(0xFF94A3B8)
    drawCircle(Color(0xFFF5EED4), r * 0.9f, Offset(c.x, c.y - r * 0.12f))
    val bowl = Path().apply {
        moveTo(c.x - r, c.y)
        cubicTo(c.x - r, c.y + r * 0.95f, c.x + r, c.y + r * 0.95f, c.x + r, c.y)
        close()
    }
    drawPath(bowl, Color(0xFFE2E8F0))
    drawPath(bowl, edge, style = Stroke(2f))
    drawLine(edge, Offset(c.x - r * 1.06f, c.y), Offset(c.x + r * 1.06f, c.y), strokeWidth = 2f, cap = StrokeCap.Round)
}

internal fun DrawScope.rod(center: Offset, len: Float, thick: Float, color: Color, angle: Float) {
    val dx = cos(angle) * len / 2f; val dy = sin(angle) * len / 2f
    drawLine(color, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = thick, cap = StrokeCap.Round)
}

internal fun DrawScope.drawRoundRectCompat(color: Color, left: Float, top: Float, w: Float, h: Float, radius: Float) {
    drawRoundRect(color = color, topLeft = Offset(left, top), size = Size(w, h), cornerRadius = CornerRadius(radius, radius))
}

/**
 * A round mixing bowl seen from the side, holding dough. [rise] 0..1 controls how high the dough
 * domes above the rim (yeast fermentation). At high rise the surface shows bubbles and a crack.
 */
internal fun DrawScope.drawDoughBowl(cx: Float, cy: Float, w: Float, h: Float, rise: Float) {
    val left = cx - w / 2f; val right = cx + w / 2f
    val rim = cy - h * 0.18f
    val bottom = cy + h * 0.42f
    // bowl
    val bowl = Path().apply {
        moveTo(left, rim)
        cubicTo(left, bottom, right, bottom, right, rim)
    }
    drawPath(bowl, Color(0xFFE5E7EB))
    drawPath(bowl, Color(0xFF9CA3AF), style = Stroke(2.5f))
    drawLine(Color(0xFF6B7280), Offset(left - 4f, rim), Offset(right + 4f, rim), strokeWidth = 2.5f, cap = StrokeCap.Round)
    // dough mound
    val doughTan = Color(0xFFE7D4A8)
    val doughEdge = Color(0xFFB99B5E)
    val domeTop = rim - rise * h * 0.42f
    val dl = left + 6f; val dr = right - 6f; val db = bottom - 5f
    val dough = Path().apply {
        moveTo(dl, rim)
        cubicTo(dl, domeTop, dr, domeTop, dr, rim)            // domed top
        cubicTo(dr, db, dl, db, dl, rim)                       // rounded bottom
        close()
    }
    drawPath(dough, doughTan)
    drawPath(dough, doughEdge, style = Stroke(1.6f))
    // bubbles appear as the dough rises
    if (rise > 0.25f) {
        val n = (rise * 7).toInt()
        for (i in 0 until n) {
            val a = i * 2.39996f
            val rr = (w * 0.30f) * ((i + 1f) / (n + 1f))
            val bx = cx + cos(a) * rr
            val by = (rim + domeTop) / 2f + sin(a) * (h * 0.12f)
            drawCircle(Color(0xFFFBF3DE), w * 0.035f, Offset(bx, by))
            drawCircle(doughEdge.copy(alpha = 0.5f), w * 0.035f, Offset(bx, by), style = Stroke(1f))
        }
    }
    if (rise > 0.7f) {
        drawLine(doughEdge.copy(alpha = 0.7f), Offset(cx - w * 0.18f, domeTop + 6f),
            Offset(cx + w * 0.12f, domeTop + 12f), strokeWidth = 1.6f, cap = StrokeCap.Round)
    }
}

/**
 * A round bowl holding milk that may set into curd. [set] 0..1: 0 = pourable milk (a tilted
 * surface line), 1 = firm gelled curd (domed, holds shape). [tint] colours the rim accent.
 */
internal fun DrawScope.drawMilkBowl(cx: Float, cy: Float, w: Float, h: Float, set: Float, tint: Color) {
    val left = cx - w / 2f; val right = cx + w / 2f
    val rim = cy - h * 0.18f
    val bottom = cy + h * 0.42f
    val bowl = Path().apply {
        moveTo(left, rim)
        cubicTo(left, bottom, right, bottom, right, rim)
    }
    // contents
    val milk = androidx.compose.ui.graphics.lerp(Color(0xFFFAFAFA), Color(0xFFF5EED4), set)
    val contents = Path().apply {
        moveTo(left + 5f, rim)
        lineTo(right - 5f, rim)
        cubicTo(right - 5f, bottom - 3f, left + 5f, bottom - 3f, left + 5f, rim)
        close()
    }
    drawPath(contents, milk)
    drawPath(bowl, Color(0xFF9CA3AF), style = Stroke(2.5f))
    drawLine(tint, Offset(left - 4f, rim), Offset(right + 4f, rim), strokeWidth = 3f, cap = StrokeCap.Round)
    if (set < 0.4f) {
        // liquid: a slightly tilted surface line + ripple
        drawLine(Color(0xFFCBD5E1), Offset(left + 8f, rim + 6f), Offset(right - 8f, rim + 10f), strokeWidth = 1.6f)
    } else {
        // set curd: gelled wobble lines
        for (i in 1..3) {
            val gy = rim + (bottom - rim) * (i * 0.22f)
            val segs = 5; val sw = (w - 16f) / segs
            for (j in 0 until segs) {
                val sx = left + 8f + j * sw
                val o = if (j % 2 == 0) 2.5f else -2.5f
                drawLine(tint.copy(alpha = 0.3f), Offset(sx, gy + o), Offset(sx + sw, gy - o), strokeWidth = 1.3f)
            }
        }
    }
}
