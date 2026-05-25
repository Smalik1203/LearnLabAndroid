package com.learnlab.lessons.figures

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * NCERT Fig 3.7 — "Testing for the presence of starch in various food items".
 * Two students at a bench with an iodine bottle, a dropper, and four small
 * food samples on dishes. Stylised Canvas illustration so we don't ship any
 * raster assets.
 */
@Composable
fun IodineBenchFigure() {
    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            val w = size.width; val h = size.height
            val benchY = h * 0.72f

            // Bench top
            drawRect(Color(0xFFCBD5E1),
                topLeft = Offset(0f, benchY),
                size = Size(w, h - benchY))
            drawLine(Color(0xFF64748B),
                Offset(0f, benchY), Offset(w, benchY), strokeWidth = 2f)

            // Two student silhouettes behind the bench (girl on left, boy on right)
            drawStudent(centerX = w * 0.32f, baseY = benchY - 6f,
                skin = Color(0xFFFBCFE8), shirt = Color(0xFFFCE7F3), hair = Color(0xFF1F2937),
                hairBun = true)
            drawStudent(centerX = w * 0.68f, baseY = benchY - 6f,
                skin = Color(0xFFFDE68A), shirt = Color(0xFFE0F2FE), hair = Color(0xFF111827),
                hairBun = false)

            // Iodine bottle (centre-left of bench)
            run {
                val bx = w * 0.40f; val by = benchY - 50f
                drawRect(Color(0xFF92400E),
                    topLeft = Offset(bx - 14f, by), size = Size(28f, 8f))
                drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(bx - 18f, by + 8f),
                    size = Size(36f, 38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                )
                // label band
                drawRect(Color(0xFFFEF3C7),
                    topLeft = Offset(bx - 18f, by + 18f),
                    size = Size(36f, 14f))
            }

            // Dropper (held by right student over the dishes)
            run {
                val dx = w * 0.55f; val dy = benchY - 70f
                drawRect(Color(0xFF334155),
                    topLeft = Offset(dx, dy), size = Size(6f, 40f))
                drawCircle(Color(0xFFCA8A04),
                    radius = 7f, center = Offset(dx + 3f, dy + 48f))
                // Falling drop
                drawCircle(Color(0xFFCA8A04),
                    radius = 3f, center = Offset(dx + 3f, dy + 62f))
            }

            // Four food sample dishes on the bench
            val dishY = benchY + 6f
            val dishW = 36f
            val gap = 14f
            val totalW = 4 * dishW + 3 * gap
            val startX = (w - totalW) / 2f + dishW / 2f
            val foods = listOf(
                Color(0xFFFBBF24), // potato
                Color(0xFFA7F3D0), // cucumber
                Color(0xFFFED7AA), // bread
                Color(0xFFFDE68A), // rice
            )
            foods.forEachIndexed { i, c ->
                val cx = startX + i * (dishW + gap)
                // dish
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(cx - dishW / 2f, dishY),
                    size = Size(dishW, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f),
                )
                // food blob
                drawCircle(c, radius = 9f, center = Offset(cx, dishY + 6f))
            }

            // "iodine" label arrow pointing to bottle
            val arrowStart = Offset(w * 0.25f, h * 0.18f)
            val arrowEnd = Offset(w * 0.38f, benchY - 30f)
            drawLine(Color(0xFF475569), arrowStart, arrowEnd,
                strokeWidth = 1.5f, cap = StrokeCap.Round)
        }
    }
}

/** Tiny standing student silhouette — head, shoulders, lab coat. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStudent(
    centerX: Float, baseY: Float,
    skin: Color, shirt: Color, hair: Color,
    hairBun: Boolean,
) {
    val headR = 14f
    val headCy = baseY - 60f
    // Lab coat (trapezoid via path)
    val coatTop = headCy + headR + 2f
    val coatBottom = baseY
    val coat = Path().apply {
        moveTo(centerX - 12f, coatTop)
        lineTo(centerX + 12f, coatTop)
        lineTo(centerX + 24f, coatBottom)
        lineTo(centerX - 24f, coatBottom)
        close()
    }
    drawPath(coat, shirt)
    drawPath(coat, Color(0xFF94A3B8), style = Stroke(1f))
    // Shirt placket (centre line)
    drawLine(Color(0xFF94A3B8),
        Offset(centerX, coatTop), Offset(centerX, coatBottom), strokeWidth = 1f)
    // Head
    drawCircle(skin, radius = headR, center = Offset(centerX, headCy))
    // Hair cap
    drawArc(
        color = hair,
        startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(centerX - headR, headCy - headR),
        size = Size(headR * 2f, headR * 2f),
        style = Stroke(width = 4f, cap = StrokeCap.Round),
    )
    if (hairBun) {
        drawCircle(hair, radius = 6f,
            center = Offset(centerX + 4f, headCy - headR - 2f))
    }
}
