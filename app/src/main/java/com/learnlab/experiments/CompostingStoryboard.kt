package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.experiments.ch06kit.Badge
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Simulation 4 — Activity 2.7: composting / decomposition. A six-panel time-lapse storyboard
 * showing fruit & vegetable peels turning into dark manure as microbes break them down.
 */

@Composable
fun CompostingStoryboard(controls: ExperimentControls) {
    val t = LL.tokens
    LaunchedEffect(Unit) { controls.onProgress(1f); controls.onComplete(1f) }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LLText("ACTIVITY 2.7 · COMPOSTING & DECOMPOSITION", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                LLText("Bacteria & fungi break complex peels into simple, nutrient-rich manure.",
                    color = t.ink400, size = 11.sp)
            }
            Column(modifier = Modifier.fillMaxWidth().weight(4f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CPanel("Day 0", "Fresh fruit & vegetable peels are placed in a container of garden soil and covered. The soil already holds billions of bacteria and fungi.", Modifier.weight(1f)) { drawDay0() }
                    CPanel("Days 3–5", "No visible change yet — but bacteria and fungi have begun colonising the peels, secreting enzymes that break down complex molecules.", Modifier.weight(1f)) { drawDay3() }
                    CPanel("Week 1", "White fuzzy growth appears — bread mould (a fungus). The peels soften as fungi break down cellulose and starch.", Modifier.weight(1f)) { drawWeek1() }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CPanel("Week 2", "The peels have broken down a lot. Bacteria now dominate; the complex molecules have become simpler compounds that dissolve in water.", Modifier.weight(1f)) { drawWeek2() }
                    CPanel("Week 3", "The peels have become manure — dark, rich, crumbly, earthy. This is what gardeners add to soil to make it fertile.", Modifier.weight(1f)) { drawWeek3() }
                    CPanel("The cycle", "The nutrients return to the soil and are taken up by plant roots. Dead matter becomes new life — and microbes drive the cycle.", Modifier.weight(1f)) { drawCycle() }
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(t.amber50)
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Badge("Our scientific heritage", tint = t.amber700)
                    LLText(
                        "Ancient Indian texts, particularly the Vedas, refer to the word 'Krimi' — meaning " +
                            "different tiny entities, both visible (Drishya) and invisible (Adrishya). Various Vedic " +
                            "texts mention their beneficial and harmful effects; the Atharvaveda also refers to 'Krimi'.",
                        color = t.ink200, size = 12.sp, lineHeight = 17.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CPanel(time: String, caption: String, modifier: Modifier = Modifier, figure: DrawScope.() -> Unit) {
    val t = LL.tokens
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(t.accent50).padding(horizontal = 9.dp, vertical = 3.dp)) {
            LLText(time, color = t.accent700, size = 10.sp, weight = FontWeight.Bold, letterSpacing = 0.8.sp)
        }
        Box(Modifier.fillMaxWidth().weight(1f)) { Canvas(Modifier.fillMaxSize()) { figure() } }
        LLText(caption, color = t.ink400, size = 10.sp, lineHeight = 13.sp)
    }
}

// ── figures ─────────────────────────────────────────────────────────

private fun DrawScope.container(soilTop: Float): Triple<Float, Float, Float> {
    val w = size.width; val h = size.height
    val left = w * 0.16f; val right = w * 0.84f; val bottom = h * 0.9f
    val cw = right - left
    // container walls
    drawRect(Color(0xFF6B4A36).copy(alpha = 0.18f), topLeft = Offset(left, soilTop), size = Size(cw, bottom - soilTop))
    drawLine(Color(0xFF8A6240), Offset(left, h * 0.2f), Offset(left, bottom), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFF8A6240), Offset(right, h * 0.2f), Offset(right, bottom), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFF8A6240), Offset(left, bottom), Offset(right, bottom), strokeWidth = 3f, cap = StrokeCap.Round)
    return Triple(left, right, bottom)
}

private fun DrawScope.soil(left: Float, right: Float, top: Float, bottom: Float, color: Color) {
    drawRect(color, topLeft = Offset(left + 2f, top), size = Size(right - left - 4f, bottom - top))
}

private fun DrawScope.peelBits(left: Float, right: Float, y: Float, scale: Float, dark: Float) {
    val cols = listOf(Color(0xFFFB923C), Color(0xFFFACC15), Color(0xFF84CC16))
    val mix = { c: Color -> androidx.compose.ui.graphics.lerp(c, Color(0xFF3F2410), dark) }
    val n = 4
    for (i in 0 until n) {
        val x = left + (right - left) * (0.18f + i * 0.21f)
        drawOval(mix(cols[i % 3]), topLeft = Offset(x, y), size = Size((right - left) * 0.16f * scale, (right - left) * 0.08f * scale))
    }
}

private fun DrawScope.magnifier(cx: Float, cy: Float, r: Float, content: DrawScope.(Offset, Float) -> Unit) {
    drawCircle(Color(0xFFFDFDFD), r, Offset(cx, cy))
    content(Offset(cx, cy), r)
    drawCircle(Color(0xFF475569), r, Offset(cx, cy), style = Stroke(2.5f))
    drawLine(Color(0xFF475569), Offset(cx + r * 0.7f, cy + r * 0.7f), Offset(cx + r * 1.5f, cy + r * 1.5f), strokeWidth = 3f, cap = StrokeCap.Round)
}

private fun DrawScope.drawDay0() {
    val (l, r, b) = container(size.height * 0.45f)
    soil(l, r, size.height * 0.55f, b, Color(0xFF6B4A36).copy(alpha = 0.55f))
    peelBits(l, r, size.height * 0.45f, 1f, 0f)
    // thin soil layer line on top
    drawLine(Color(0xFF6B4A36), Offset(l + 2f, size.height * 0.43f), Offset(r - 2f, size.height * 0.43f), strokeWidth = 2f)
}

private fun DrawScope.drawDay3() {
    val (l, r, b) = container(size.height * 0.45f)
    soil(l, r, size.height * 0.45f, b, Color(0xFF6B4A36).copy(alpha = 0.6f))
    peelBits(l, r, size.height * 0.48f, 1f, 0.05f)
    magnifier(size.width * 0.74f, size.height * 0.3f, size.width * 0.15f) { c, rr ->
        drawCircle(Color(0xFFB45309), rr * 0.12f, Offset(c.x - rr * 0.3f, c.y - rr * 0.2f))
        drawLine(Color(0xFFB45309), Offset(c.x, c.y), Offset(c.x + rr * 0.4f, c.y + rr * 0.3f), strokeWidth = 3f, cap = StrokeCap.Round)
        // hypha threads
        drawLine(Color(0xFF6B7280), Offset(c.x - rr * 0.4f, c.y + rr * 0.3f), Offset(c.x + rr * 0.2f, c.y - rr * 0.4f), strokeWidth = 1.4f)
    }
}

private fun DrawScope.drawWeek1() {
    val (l, r, b) = container(size.height * 0.45f)
    soil(l, r, size.height * 0.45f, b, Color(0xFF5A3E2C).copy(alpha = 0.65f))
    peelBits(l, r, size.height * 0.48f, 0.9f, 0.2f)
    // white fuzzy growth
    for (i in 0 until 6) {
        val x = l + (r - l) * (0.2f + i * 0.12f)
        drawCircle(Color.White.copy(alpha = 0.7f), (r - l) * 0.04f, Offset(x, size.height * 0.5f))
    }
    magnifier(size.width * 0.74f, size.height * 0.3f, size.width * 0.15f) { c, rr ->
        for (i in 0 until 5) {
            val a = i * 1.3f
            drawLine(Color(0xFF6B7280), c, Offset(c.x + cos(a) * rr * 0.7f, c.y + sin(a) * rr * 0.7f), strokeWidth = 1.5f)
        }
    }
}

private fun DrawScope.drawWeek2() {
    val (l, r, b) = container(size.height * 0.4f)
    soil(l, r, size.height * 0.4f, b, Color(0xFF44301F).copy(alpha = 0.8f))
    peelBits(l, r, size.height * 0.55f, 0.6f, 0.6f)
    magnifier(size.width * 0.74f, size.height * 0.3f, size.width * 0.15f) { c, rr ->
        for (i in 0 until 8) {
            val a = i * 0.9f
            val rrr = rr * 0.6f * ((i + 1f) / 9f)
            drawCircle(Color(0xFFA855F7).copy(alpha = 0.7f), rr * 0.07f, Offset(c.x + cos(a) * rrr, c.y + sin(a) * rrr))
        }
    }
}

private fun DrawScope.drawWeek3() {
    val (l, r, b) = container(size.height * 0.35f)
    soil(l, r, size.height * 0.35f, b, Color(0xFF3B2A1A))
    // crumbly texture
    for (i in 0 until 40) {
        val x = l + (r - l) * ((i * 37 % 100) / 100f)
        val y = size.height * 0.4f + (size.height * 0.45f) * ((i * 53 % 100) / 100f)
        drawCircle(Color(0xFF5A3E2C).copy(alpha = 0.7f), (r - l) * 0.015f, Offset(x, y))
    }
}

private fun DrawScope.drawCycle() {
    val w = size.width; val h = size.height
    val (l, r, b) = container(h * 0.55f)
    soil(l, r, h * 0.55f, b, Color(0xFF3B2A1A))
    // plant
    val stemX = (l + r) / 2f
    drawLine(Color(0xFF15803D), Offset(stemX, h * 0.55f), Offset(stemX, h * 0.2f), strokeWidth = 3f, cap = StrokeCap.Round)
    for (s in listOf(-1f, 1f)) {
        drawOval(Color(0xFF22C55E), topLeft = Offset(stemX + s * w * 0.02f, h * 0.25f), size = Size(w * 0.12f, h * 0.08f))
    }
    drawOval(Color(0xFF22C55E), topLeft = Offset(stemX - w * 0.06f, h * 0.18f), size = Size(w * 0.12f, h * 0.08f))
    // roots
    for (s in listOf(-1f, 0f, 1f)) {
        drawLine(Color(0xFF8A6240), Offset(stemX, h * 0.55f), Offset(stemX + s * w * 0.1f, h * 0.8f), strokeWidth = 1.6f, cap = StrokeCap.Round)
    }
    // bacteria dots near roots
    for (i in 0 until 5) {
        val a = i * 1.2f
        drawCircle(Color(0xFFA855F7).copy(alpha = 0.7f), w * 0.012f, Offset(stemX + cos(a) * w * 0.09f, h * 0.72f + sin(a) * h * 0.06f))
    }
}
