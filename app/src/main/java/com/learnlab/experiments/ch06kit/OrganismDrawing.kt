package com.learnlab.experiments.ch06kit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared NCERT-style microorganism glyphs (Tables 2.1 & 2.2 + the classification explorer).
 * Extracted from the original MicrobeGuide and extended with yeast, Lactobacillus, Rhizobium and
 * the virus from the "Ever heard of…" box.
 */

internal fun DrawScope.drawAmoebaGlyph(c: Offset, r: Float) {
    val body = Color(0xFF38BDF8).copy(alpha = 0.30f)
    val edge = Color(0xFF0369A1)
    val lobes = 8
    val path = Path()
    for (i in 0..lobes) {
        val a = i * (2f * PI.toFloat() / lobes)
        val pseudo = 1f + 0.30f * sin(i * 1.9f)
        val px = c.x + cos(a) * r * 1.4f * pseudo
        val py = c.y + sin(a) * r * 1.4f * pseudo
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, body)
    drawPath(path, edge, style = Stroke(2f))
    drawCircle(Color(0xFF7C3AED).copy(alpha = 0.6f), r * 0.38f, Offset(c.x - r * 0.1f, c.y))
    drawCircle(Color(0xFF4C1D95), r * 0.38f, Offset(c.x - r * 0.1f, c.y), style = Stroke(1.2f))
}

internal fun DrawScope.drawParameciumGlyph(c: Offset, r: Float) {
    val body = Color(0xFF65A30D).copy(alpha = 0.28f)
    val edge = Color(0xFF3F6212)
    val path = Path().apply {
        moveTo(c.x - r * 1.7f, c.y)
        cubicTo(c.x - r * 1.4f, c.y - r * 1.1f, c.x + r * 0.6f, c.y - r * 1.0f, c.x + r * 1.7f, c.y - r * 0.35f)
        cubicTo(c.x + r * 2.0f, c.y - r * 0.1f, c.x + r * 2.0f, c.y + r * 0.3f, c.x + r * 1.5f, c.y + r * 0.7f)
        cubicTo(c.x + r * 0.4f, c.y + r * 1.1f, c.x - r * 1.4f, c.y + r * 1.0f, c.x - r * 1.7f, c.y)
        close()
    }
    drawPath(path, body)
    drawPath(path, edge, style = Stroke(2f))
    drawLine(edge.copy(alpha = 0.7f), Offset(c.x - r * 0.2f, c.y - r * 0.2f),
        Offset(c.x + r * 0.8f, c.y + r * 0.3f), strokeWidth = 1.5f)
    val n = 22
    for (i in 0 until n) {
        val a = i * (2f * PI.toFloat() / n)
        val ex = c.x + cos(a) * r * 1.7f
        val ey = c.y + sin(a) * r * 0.95f
        drawLine(edge.copy(alpha = 0.6f), Offset(ex, ey),
            Offset(ex + cos(a) * r * 0.3f, ey + sin(a) * r * 0.3f), strokeWidth = 1f)
    }
    drawCircle(Color(0xFF365314).copy(alpha = 0.5f), r * 0.3f, c)
}

internal fun DrawScope.drawGreenAlgaGlyph(c: Offset, r: Float, flagellated: Boolean) {
    val brush = Brush.radialGradient(
        colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF15803D)),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.5f,
    )
    drawCircle(brush, r * 1.1f, c)
    drawCircle(Color(0xFF14532D), r * 1.1f, c, style = Stroke(2f))
    val cup = Path().apply {
        addArc(Rect(c.x - r * 0.7f, c.y - r * 0.5f, c.x + r * 0.7f, c.y + r * 0.8f), 20f, 140f)
    }
    drawPath(cup, Color(0xFF14532D).copy(alpha = 0.6f), style = Stroke(r * 0.25f, cap = StrokeCap.Round))
    if (flagellated) {
        for (s in listOf(-1f, 1f)) {
            val tail = Path().apply {
                moveTo(c.x + s * r * 0.4f, c.y - r * 1.0f)
                cubicTo(c.x + s * r * 1.2f, c.y - r * 1.8f,
                    c.x + s * r * 0.6f, c.y - r * 2.2f,
                    c.x + s * r * 1.4f, c.y - r * 2.6f)
            }
            drawPath(tail, Color(0xFF14532D), style = Stroke(1.6f, cap = StrokeCap.Round))
        }
    }
}

internal fun DrawScope.drawBreadMouldGlyph(c: Offset, r: Float) {
    val filament = Color(0xFF92400E)
    val baseY = c.y + r * 1.3f
    drawLine(filament, Offset(c.x - r * 2.0f, baseY), Offset(c.x + r * 2.0f, baseY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    val stalkX = listOf(-1.3f, 0f, 1.3f)
    for (sx in stalkX) {
        val x = c.x + sx * r
        drawLine(filament, Offset(x, baseY), Offset(x, c.y - r * 0.8f), strokeWidth = 2f)
        drawCircle(Color(0xFF6B3F18), r * 0.5f, Offset(x, c.y - r * 1.1f))
        drawCircle(Color(0xFF3F2410), r * 0.5f, Offset(x, c.y - r * 1.1f), style = Stroke(1.2f))
        drawCircle(Color(0xFFFDE68A).copy(alpha = 0.7f), 1.6f, Offset(x - r * 0.15f, c.y - r * 1.2f))
        drawCircle(Color(0xFFFDE68A).copy(alpha = 0.7f), 1.6f, Offset(x + r * 0.15f, c.y - r * 1.05f))
    }
    for (s in listOf(-1f, 1f)) {
        drawLine(filament.copy(alpha = 0.7f), Offset(c.x + s * r * 1.3f, baseY),
            Offset(c.x + s * r * 1.7f, baseY + r * 0.5f), strokeWidth = 1.4f)
    }
}

internal fun DrawScope.drawMouldGlyph(c: Offset, r: Float) {
    val filament = Color(0xFF115E59)
    val baseY = c.y + r * 1.3f
    drawLine(filament, Offset(c.x - r * 2.0f, baseY), Offset(c.x + r * 2.0f, baseY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    val stalkX = listOf(-1.2f, 0f, 1.2f)
    for (sx in stalkX) {
        val x = c.x + sx * r
        val topY = c.y - r * 0.6f
        drawLine(filament, Offset(x, baseY), Offset(x, topY), strokeWidth = 2f)
        for (b in -2..2) {
            drawLine(Color(0xFF0F766E), Offset(x, topY),
                Offset(x + b * r * 0.18f, topY - r * 0.7f), strokeWidth = 1.4f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF99F6E4), 1.6f, Offset(x + b * r * 0.18f, topY - r * 0.75f))
        }
    }
}

internal fun DrawScope.drawBacteriaGlyph(c: Offset, r: Float) {
    val fill = Color(0xFFB45309).copy(alpha = 0.85f)
    val edge = Color(0xFF7C2D12)
    drawCircle(fill, r * 0.4f, Offset(c.x - r * 1.6f, c.y - r * 1.0f))
    drawCircle(edge, r * 0.4f, Offset(c.x - r * 1.6f, c.y - r * 1.0f), style = Stroke(1f))
    val comma = Path().apply {
        moveTo(c.x + r * 0.9f, c.y - r * 1.4f)
        cubicTo(c.x + r * 1.8f, c.y - r * 1.2f, c.x + r * 1.8f, c.y - r * 0.4f, c.x + r * 1.1f, c.y - r * 0.5f)
    }
    drawPath(comma, fill, style = Stroke(r * 0.34f, cap = StrokeCap.Round))
    val spiral = Path().apply {
        moveTo(c.x - r * 2.0f, c.y + r * 0.6f)
        var x = -2.0f
        while (x < 0.2f) {
            val px = c.x + x * r
            val py = c.y + r * 0.6f + sin(x * 6f) * r * 0.4f
            lineTo(px, py)
            x += 0.12f
        }
    }
    drawPath(spiral, fill, style = Stroke(r * 0.3f, cap = StrokeCap.Round))
    val rodC = Offset(c.x + r * 1.0f, c.y + r * 1.1f)
    drawLine(fill, Offset(rodC.x - r * 0.9f, rodC.y), Offset(rodC.x + r * 0.9f, rodC.y),
        strokeWidth = r * 0.7f, cap = StrokeCap.Round)
    val flag = Path().apply {
        moveTo(rodC.x + r * 0.9f, rodC.y)
        cubicTo(rodC.x + r * 1.6f, rodC.y - r * 0.3f, rodC.x + r * 1.6f, rodC.y + r * 0.5f,
            rodC.x + r * 2.3f, rodC.y + r * 0.2f)
    }
    drawPath(flag, edge, style = Stroke(1.4f, cap = StrokeCap.Round))
    for (i in 0 until 8) {
        val sx = rodC.x - r * 0.8f + i * (r * 0.22f)
        drawLine(edge.copy(alpha = 0.7f), Offset(sx, rodC.y - r * 0.35f),
            Offset(sx, rodC.y - r * 0.6f), strokeWidth = 1f)
        drawLine(edge.copy(alpha = 0.7f), Offset(sx, rodC.y + r * 0.35f),
            Offset(sx, rodC.y + r * 0.6f), strokeWidth = 1f)
    }
}

/** A single rod / bacillus capsule centred at [c]. */
internal fun DrawScope.drawRod(c: Offset, len: Float, thick: Float, fill: Color, edge: Color, angle: Float = 0f) {
    val dx = cos(angle) * len / 2f
    val dy = sin(angle) * len / 2f
    drawLine(fill, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth = thick, cap = StrokeCap.Round)
    drawLine(edge, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth = thick + 2f, cap = StrokeCap.Round)
    drawLine(fill, Offset(c.x - dx, c.y - dy), Offset(c.x + dx, c.y + dy), strokeWidth = thick, cap = StrokeCap.Round)
}

internal fun DrawScope.drawYeastGlyph(c: Offset, r: Float) {
    val fill = Brush.radialGradient(
        colors = listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFB45309)),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.5f,
    )
    // parent cell
    drawOval(fill, topLeft = Offset(c.x - r, c.y - r * 0.8f), size = Size(r * 2f, r * 1.6f))
    drawOval(Color(0xFF92400E), topLeft = Offset(c.x - r, c.y - r * 0.8f), size = Size(r * 2f, r * 1.6f), style = Stroke(2f))
    drawCircle(Color(0xFF7C2D12).copy(alpha = 0.5f), r * 0.3f, c) // nucleus
    // a small bud growing off the top-right
    val bud = Offset(c.x + r * 0.9f, c.y - r * 0.7f)
    drawCircle(fill, r * 0.5f, bud)
    drawCircle(Color(0xFF92400E), r * 0.5f, bud, style = Stroke(1.6f))
    // bud scar
    drawArc(Color(0xFF92400E), 200f, 140f, false,
        topLeft = Offset(c.x + r * 0.4f, c.y - r * 1.0f), size = Size(r * 0.5f, r * 0.5f), style = Stroke(1.4f))
}

internal fun DrawScope.drawLactobacillusGlyph(c: Offset, r: Float) {
    val fill = Color(0xFFA855F7).copy(alpha = 0.85f)
    val edge = Color(0xFF6B21A8)
    // a short chain of rods (Lactobacillus often forms chains)
    val rods = listOf(
        Offset(c.x - r * 1.1f, c.y - r * 0.6f),
        Offset(c.x, c.y),
        Offset(c.x + r * 1.1f, c.y + r * 0.6f),
        Offset(c.x - r * 0.2f, c.y + r * 1.2f),
    )
    rods.forEach { drawRod(it, r * 1.3f, r * 0.5f, fill, edge, angle = 0.5f) }
}

internal fun DrawScope.drawRhizobiumGlyph(c: Offset, r: Float) {
    // faint root-nodule background
    drawCircle(Color(0xFFD9F99D).copy(alpha = 0.5f), r * 1.8f, c)
    drawCircle(Color(0xFF4D7C0F).copy(alpha = 0.6f), r * 1.8f, c, style = Stroke(2f))
    val fill = Color(0xFF65A30D).copy(alpha = 0.9f)
    val edge = Color(0xFF3F6212)
    // rods packed inside the nodule
    val rods = listOf(
        Offset(c.x - r * 0.7f, c.y - r * 0.6f) to 0.3f,
        Offset(c.x + r * 0.5f, c.y - r * 0.5f) to -0.4f,
        Offset(c.x - r * 0.3f, c.y + r * 0.5f) to 0.6f,
        Offset(c.x + r * 0.7f, c.y + r * 0.6f) to 0.1f,
        Offset(c.x, c.y) to -0.2f,
    )
    rods.forEach { (p, ang) -> drawRod(p, r * 1.0f, r * 0.42f, fill, edge, angle = ang) }
}

internal fun DrawScope.drawVirusGlyph(c: Offset, r: Float) {
    val fill = Color(0xFFE11D48).copy(alpha = 0.85f)
    val edge = Color(0xFF9F1239)
    // icosahedral head (hexagon)
    val head = Path()
    for (i in 0 until 6) {
        val a = PI.toFloat() / 6f + i * (2f * PI.toFloat() / 6f)
        val px = c.x + cos(a) * r
        val py = c.y - r * 0.7f + sin(a) * r
        if (i == 0) head.moveTo(px, py) else head.lineTo(px, py)
    }
    head.close()
    drawPath(head, fill)
    drawPath(head, edge, style = Stroke(2f))
    // genetic material hint
    drawCircle(Color(0xFFFECDD3).copy(alpha = 0.7f), r * 0.35f, Offset(c.x, c.y - r * 0.7f))
    // tail sheath
    drawLine(edge, Offset(c.x, c.y + r * 0.2f), Offset(c.x, c.y + r * 1.1f), strokeWidth = r * 0.35f)
    // base plate
    drawLine(edge, Offset(c.x - r * 0.6f, c.y + r * 1.1f), Offset(c.x + r * 0.6f, c.y + r * 1.1f), strokeWidth = r * 0.18f, cap = StrokeCap.Round)
    // tail fibres / legs
    for (s in listOf(-1f, 1f)) {
        val legs = Path().apply {
            moveTo(c.x + s * r * 0.5f, c.y + r * 1.1f)
            cubicTo(c.x + s * r * 1.0f, c.y + r * 1.4f, c.x + s * r * 0.8f, c.y + r * 1.8f, c.x + s * r * 1.2f, c.y + r * 1.9f)
        }
        drawPath(legs, edge, style = Stroke(2f, cap = StrokeCap.Round))
    }
}
