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
 * Three plants at the SAME scale next to a human silhouette. Tap a plant to
 * focus + show info. Slider grows them from seedling (0) to full size (1).
 */
@Composable
fun PlantHeightsAtScale(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val p = lessonPalette()

    var grow by remember { mutableFloatStateOf(1f) }
    var focused by remember { mutableIntStateOf(-1) } // 0 tomato, 1 rose, 2 mango

    val growAnim by animateFloatAsState(grow, tween(700), label = "grow")

    Column(modifier = modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Plants at scale", color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
            LLText("Tap a plant · drag the slider to grow", color = t.ink500, size = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .pointerInput(Unit) {
                    detectTapZones { zone -> focused = if (focused == zone) -1 else zone }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawScene(growAnim, focused, p.emerald.accent, p.amber.accent, p.violet.accent)
            }
        }
        Spacer(Modifier.height(10.dp))
        // Focused plant info
        val info = when (focused) {
            0 -> Triple("Tomato — a herb", "Short. Soft, green stem. Knee-high or less.", p.emerald.accent)
            1 -> Triple("Rose — a shrub", "Medium. Several woody stems starting near the ground.", p.amber.accent)
            2 -> Triple("Mango — a tree", "Tall. Hard, thick, woody trunk. Branches start high up.", p.violet.accent)
            else -> Triple("Three plant groups", "Tap herb, shrub, or tree to learn how each one is grouped.", t.ink400)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(t.surface2)
                .padding(14.dp),
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(info.third))
            Spacer(Modifier.width(10.dp))
            Column {
                LLText(info.first, color = t.ink50, size = 15.sp, weight = FontWeight.Bold)
                LLText(info.second, color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        GrowSlider(value = grow, onChange = { grow = it })
    }
}

/** Detects which of three vertical thirds was tapped → 0/1/2. */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapZones(
    onTap: (Int) -> Unit,
) {
    detectTapGestures { offset ->
        val third = size.width / 3f
        val zone = (offset.x / third).toInt().coerceIn(0, 2)
        onTap(zone)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScene(
    grow: Float, focused: Int, herbColor: Color, shrubColor: Color, treeColor: Color,
) {
    val w = size.width
    val h = size.height
    val ground = h * 0.92f
    val sky = Color(0xFFE0F2FE)
    val groundColor = Color(0xFF92400E).copy(alpha = 0.2f)

    // Sky / ground
    drawRect(color = sky.copy(alpha = 0.4f), size = Size(w, ground))
    drawRect(color = groundColor, topLeft = Offset(0f, ground), size = Size(w, h - ground))

    // Reference human silhouette (always full size for scale)
    val humanH = h * 0.45f
    val humanX = w * 0.45f
    drawHuman(Offset(humanX, ground), humanH)

    // Three plant zones — left to right
    val z0 = w * 0.12f
    val z1 = w * 0.30f
    val z2 = w * 0.72f

    drawHerb(Offset(z0, ground), targetH = h * 0.18f * grow,
        color = herbColor, highlight = focused == 0)
    drawShrub(Offset(z1, ground), targetH = h * 0.32f * grow,
        color = shrubColor, highlight = focused == 1)
    drawTree(Offset(z2, ground), targetH = h * 0.78f * grow,
        color = treeColor, highlight = focused == 2)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHuman(base: Offset, height: Float) {
    val ink = Color(0xFF334155)
    val headR = height * 0.10f
    val bodyTop = base.y - height
    val headCy = bodyTop + headR
    drawCircle(color = ink, radius = headR, center = Offset(base.x, headCy))
    // Body
    drawLine(ink, Offset(base.x, headCy + headR), Offset(base.x, base.y - height * 0.30f),
        strokeWidth = 5f, cap = StrokeCap.Round)
    // Legs
    drawLine(ink, Offset(base.x, base.y - height * 0.30f), Offset(base.x - height * 0.12f, base.y),
        strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(ink, Offset(base.x, base.y - height * 0.30f), Offset(base.x + height * 0.12f, base.y),
        strokeWidth = 5f, cap = StrokeCap.Round)
    // Arms
    drawLine(ink, Offset(base.x, headCy + headR + 8f),
        Offset(base.x - height * 0.18f, base.y - height * 0.45f),
        strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(ink, Offset(base.x, headCy + headR + 8f),
        Offset(base.x + height * 0.18f, base.y - height * 0.45f),
        strokeWidth = 5f, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHerb(base: Offset, targetH: Float, color: Color, highlight: Boolean) {
    // Soft green stem + a couple of leaves + a tiny red fruit
    val stem = Color(0xFF4ADE80)
    val leaf = Color(0xFF22C55E)
    val tomato = Color(0xFFEF4444)
    drawLine(color = stem, start = base, end = Offset(base.x, base.y - targetH),
        strokeWidth = 4f, cap = StrokeCap.Round)
    drawOval(color = leaf,
        topLeft = Offset(base.x - targetH * 0.25f, base.y - targetH * 0.6f),
        size = Size(targetH * 0.5f, targetH * 0.22f))
    drawOval(color = leaf,
        topLeft = Offset(base.x - targetH * 0.18f, base.y - targetH * 0.30f),
        size = Size(targetH * 0.36f, targetH * 0.18f))
    drawCircle(color = tomato, radius = targetH * 0.07f,
        center = Offset(base.x + targetH * 0.14f, base.y - targetH * 0.55f))
    if (highlight) highlightRing(base, w = targetH * 0.6f, h = targetH * 1.05f, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShrub(base: Offset, targetH: Float, color: Color, highlight: Boolean) {
    val wood = Color(0xFF92400E)
    val foliage = Color(0xFF16A34A)
    val flower = Color(0xFFF472B6)
    // Multiple woody stems splaying from base
    for (i in -2..2) {
        val tilt = i * 0.06f
        drawLine(color = wood,
            start = base,
            end = Offset(base.x + targetH * tilt * 1.8f, base.y - targetH * 0.95f + kotlin.math.abs(i) * targetH * 0.05f),
            strokeWidth = 3f, cap = StrokeCap.Round)
    }
    // Foliage cluster
    drawCircle(color = foliage, radius = targetH * 0.35f, center = Offset(base.x, base.y - targetH * 0.65f))
    drawCircle(color = foliage, radius = targetH * 0.30f, center = Offset(base.x + targetH * 0.18f, base.y - targetH * 0.45f))
    drawCircle(color = foliage, radius = targetH * 0.28f, center = Offset(base.x - targetH * 0.18f, base.y - targetH * 0.40f))
    // A flower
    drawCircle(color = flower, radius = targetH * 0.08f, center = Offset(base.x + targetH * 0.10f, base.y - targetH * 0.78f))
    if (highlight) highlightRing(base, w = targetH * 1.2f, h = targetH * 1.1f, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTree(base: Offset, targetH: Float, color: Color, highlight: Boolean) {
    val trunkColor = Color(0xFF7C2D12)
    val canopy = Color(0xFF16A34A)
    val trunkH = targetH * 0.55f
    val trunkW = targetH * 0.06f
    // Trunk
    drawRect(color = trunkColor,
        topLeft = Offset(base.x - trunkW / 2f, base.y - trunkH),
        size = Size(trunkW, trunkH))
    // Canopy — three overlapping circles for a fuller look
    drawCircle(color = canopy, radius = targetH * 0.30f, center = Offset(base.x, base.y - trunkH - targetH * 0.20f))
    drawCircle(color = canopy, radius = targetH * 0.26f, center = Offset(base.x - targetH * 0.22f, base.y - trunkH - targetH * 0.08f))
    drawCircle(color = canopy, radius = targetH * 0.26f, center = Offset(base.x + targetH * 0.22f, base.y - trunkH - targetH * 0.08f))
    drawCircle(color = canopy, radius = targetH * 0.22f, center = Offset(base.x, base.y - trunkH - targetH * 0.36f))
    // A mango
    drawOval(color = Color(0xFFFBBF24),
        topLeft = Offset(base.x + targetH * 0.10f, base.y - trunkH - targetH * 0.12f),
        size = Size(targetH * 0.10f, targetH * 0.12f))
    if (highlight) highlightRing(base, w = targetH * 0.95f, h = targetH * 1.05f, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.highlightRing(base: Offset, w: Float, h: Float, color: Color) {
    drawOval(color = color.copy(alpha = 0.25f),
        topLeft = Offset(base.x - w / 2f, base.y - h),
        size = Size(w, h * 0.9f),
        style = Stroke(width = 4f))
}

/* ─────────────── Slider ─────────────── */

@Composable
private fun GrowSlider(value: Float, onChange: (Float) -> Unit) {
    val t = LL.tokens
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText("Grow", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetChipSmall("Seed", value < 0.15f) { onChange(0.1f) }
                PresetChipSmall("Half", value in 0.45f..0.55f) { onChange(0.5f) }
                PresetChipSmall("Full", value > 0.95f) { onChange(1f) }
            }
        }
        SliderTrack(value = value, onChange = onChange)
    }
}

@Composable
private fun PresetChipSmall(label: String, active: Boolean, onClick: () -> Unit) {
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
