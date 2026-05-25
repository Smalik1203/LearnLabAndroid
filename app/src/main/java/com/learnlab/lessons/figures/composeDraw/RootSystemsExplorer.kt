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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * Two plants in soil cross-section. Slider lifts both plants out of the soil
 * revealing their root systems. Tap a side to focus + show info.
 */
@Composable
fun RootSystemsExplorer(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val p = lessonPalette()
    var lift by remember { mutableFloatStateOf(0f) } // 0 = in ground, 1 = pulled out
    var focused by remember { mutableIntStateOf(-1) } // 0 left (taproot), 1 right (fibrous)
    val liftAnim by animateFloatAsState(lift, tween(700), label = "lift")

    Column(modifier = modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Root systems", color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
            LLText("Tap a plant · drag slider to pull it out", color = t.ink500, size = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focused = if (offset.x < size.width / 2f) {
                            if (focused == 0) -1 else 0
                        } else {
                            if (focused == 1) -1 else 1
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRootScene(liftAnim, focused, p.violet.accent, p.sky.accent)
            }
        }
        Spacer(Modifier.height(10.dp))
        val info = when (focused) {
            0 -> Triple("Mustard — Taproot", "One thick main root with smaller side roots branching off.", p.violet.accent)
            1 -> Triple("Grass — Fibrous", "Many thin roots of similar size — no single main root.", p.sky.accent)
            else -> Triple("Two root systems", "Tap a plant to learn — or drag the slider to lift them.", t.ink400)
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
        LiftSlider(value = lift, onChange = { lift = it })
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRootScene(
    lift: Float, focused: Int, leftAccent: Color, rightAccent: Color,
) {
    val w = size.width
    val h = size.height
    val groundY = h * 0.55f
    val sky = Color(0xFFFEF3C7)
    val soil = Color(0xFF7C2D12).copy(alpha = 0.35f)
    drawRect(color = sky.copy(alpha = 0.4f), size = Size(w, groundY))
    drawRect(color = soil, topLeft = Offset(0f, groundY), size = Size(w, h - groundY))
    // Soil texture dots
    val rng = java.util.Random(42)
    for (i in 0 until 80) {
        val x = rng.nextFloat() * w
        val y = groundY + rng.nextFloat() * (h - groundY)
        drawCircle(color = Color(0xFF92400E).copy(alpha = 0.25f), radius = 2f, center = Offset(x, y))
    }

    // Divider line down the middle
    drawLine(color = Color(0xFF000000).copy(alpha = 0.1f),
        start = Offset(w / 2f, 0f), end = Offset(w / 2f, h), strokeWidth = 1f)

    // Two plants
    drawTaprootPlant(Offset(w * 0.25f, groundY), h, lift, leftAccent, focused == 0)
    drawFibrousPlant(Offset(w * 0.75f, groundY), h, lift, rightAccent, focused == 1)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTaprootPlant(
    pivot: Offset, h: Float, lift: Float, accent: Color, highlight: Boolean,
) {
    val rootColor = Color(0xFFD97706)
    val stemColor = Color(0xFF15803D)
    val leafColor = Color(0xFF22C55E)
    val flowerColor = Color(0xFFFBBF24)

    val offsetY = -lift * h * 0.35f
    val rootBase = Offset(pivot.x, pivot.y + offsetY)

    // Stem above ground
    val stemH = h * 0.30f
    drawLine(color = stemColor, start = rootBase,
        end = Offset(rootBase.x, rootBase.y - stemH),
        strokeWidth = 5f, cap = StrokeCap.Round)
    // Leaves
    drawOval(color = leafColor,
        topLeft = Offset(rootBase.x - 30f, rootBase.y - stemH * 0.85f),
        size = Size(60f, 22f))
    drawOval(color = leafColor,
        topLeft = Offset(rootBase.x - 22f, rootBase.y - stemH * 0.55f),
        size = Size(44f, 18f))
    // Flower bunch
    for (i in -1..1) {
        drawCircle(color = flowerColor, radius = 5f,
            center = Offset(rootBase.x + i * 8f, rootBase.y - stemH - 4f))
    }

    // Taproot — one thick main root straight down + side branches
    val rootEnd = Offset(rootBase.x, rootBase.y + h * 0.30f)
    drawLine(color = rootColor, start = rootBase, end = rootEnd,
        strokeWidth = 6f, cap = StrokeCap.Round)
    // Side branches
    for (i in 1..5) {
        val tNorm = i.toFloat() / 6f
        val y = rootBase.y + h * 0.30f * tNorm
        val side = if (i % 2 == 0) 1 else -1
        drawLine(color = rootColor.copy(alpha = 0.85f),
            start = Offset(rootBase.x, y),
            end = Offset(rootBase.x + side * h * 0.10f * (1f - tNorm * 0.4f), y + h * 0.04f),
            strokeWidth = 2.5f, cap = StrokeCap.Round)
    }

    if (highlight) {
        drawRect(color = accent.copy(alpha = 0.10f),
            topLeft = Offset(rootBase.x - h * 0.18f, rootBase.y - stemH - 20f),
            size = Size(h * 0.36f, stemH + h * 0.32f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFibrousPlant(
    pivot: Offset, h: Float, lift: Float, accent: Color, highlight: Boolean,
) {
    val rootColor = Color(0xFFD97706)
    val bladeColor = Color(0xFF22C55E)

    val offsetY = -lift * h * 0.35f
    val rootBase = Offset(pivot.x, pivot.y + offsetY)

    // Grass blades
    for (i in -3..3) {
        val bladeH = h * 0.25f - kotlin.math.abs(i) * h * 0.015f
        drawLine(color = bladeColor,
            start = Offset(rootBase.x + i * 4f, rootBase.y),
            end = Offset(rootBase.x + i * 10f, rootBase.y - bladeH),
            strokeWidth = 4f, cap = StrokeCap.Round)
    }

    // Fibrous roots — many thin roots fanning down
    for (i in -5..5) {
        val angle = i * 0.10f
        val len = h * 0.25f + kotlin.math.abs(i) * h * 0.005f
        drawLine(color = rootColor,
            start = rootBase,
            end = Offset(rootBase.x + angle * len, rootBase.y + len * (1f - kotlin.math.abs(angle) * 0.2f)),
            strokeWidth = 2.5f, cap = StrokeCap.Round)
    }

    if (highlight) {
        drawRect(color = accent.copy(alpha = 0.10f),
            topLeft = Offset(rootBase.x - h * 0.18f, rootBase.y - h * 0.30f),
            size = Size(h * 0.36f, h * 0.62f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}

@Composable
private fun LiftSlider(value: Float, onChange: (Float) -> Unit) {
    val t = LL.tokens
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Pull plant out", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetMini("In", value < 0.15f) { onChange(0f) }
                PresetMini("Mid", value in 0.45f..0.55f) { onChange(0.5f) }
                PresetMini("Out", value > 0.95f) { onChange(1f) }
            }
        }
        SliderTrack(value = value, onChange = onChange)
    }
}

@Composable
private fun PresetMini(label: String, active: Boolean, onClick: () -> Unit) {
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
