package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.experiments.ch06kit.drawLactobacillusGlyph
import com.learnlab.experiments.ch06kit.drawMilkBowl
import com.learnlab.experiments.ch06kit.rod
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Simulation 6 — Activity 2.9: curd formation. A nine-panel storyboard testing two conditions —
 * Bowl A (lukewarm milk, warm place → curd) vs Bowl B (cold milk, refrigerator → stays liquid).
 */

private val GREEN = Color(0xFF10B981)
private val BLUE = Color(0xFF38BDF8)

@Composable
fun CurdFormation(controls: ExperimentControls) {
    val t = LL.tokens
    LaunchedEffect(Unit) { controls.onProgress(1f); controls.onComplete(1f) }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LLText("ACTIVITY 2.9 · CURD FORMATION STORYBOARD", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Legend("Bowl A · warm", GREEN)
                    Legend("Bowl B · cold", BLUE)
                }
            }
            // 3×3 storyboard grid
            Column(modifier = Modifier.fillMaxWidth().weight(3f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Panel("1 · The question", "Curd is made from milk daily — add a spoon of curd to warm milk and hours later it sets. But why? And what if the milk is cold?", null, Modifier.weight(1f)) { drawQuestionFig() }
                    Panel("2 · Two conditions", "Bowl A: lukewarm milk in a warm place. Bowl B: cold milk in the fridge. Everything else is the same.", null, Modifier.weight(1f)) { drawTwoConditions() }
                    Panel("3 · The inoculum", "The spoon of curd is the starter — it carries live Lactobacillus bacteria that will (or won't) multiply.", null, Modifier.weight(1f)) { drawInoculum() }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Panel("4 · Six hours pass", "Both bowls are covered and left. Inside A something invisible happens; inside B, very little.", null, Modifier.weight(1f)) { drawWaiting() }
                    Panel("5 · The reveal", "After 6 hours, Bowl A has set into thick, slightly sour curd. Bowl B is still liquid milk.", null, Modifier.weight(1f)) { drawReveal() }
                    Panel("6 · Zoom into Bowl A", "Lactobacillus multiplied, ate lactose, and released lactic acid — the acid made milk protein (casein) clump into a gel.", GREEN, Modifier.weight(1f)) { drawZoomA() }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Panel("7 · Why A worked", "Warmth → Lactobacillus active → lactose eaten → lactic acid → protein coagulates → curd.", GREEN, Modifier.weight(1f)) { drawWhyA() }
                    Panel("8 · Why B failed", "In the cold, Lactobacillus go dormant. No lactose used, no acid, no coagulation — the milk stays liquid. The bacteria are alive, just inactive.", BLUE, Modifier.weight(1f)) { drawWhyB() }
                    Panel("9 · Conclusion", "Curd needs both a live starter and the right temperature. Remove either and curd does not form.", null, Modifier.weight(1f)) { drawConclusion() }
                }
            }
            // analysis questions
            AnalysisStrip(modifier = Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun Legend(label: String, tint: Color) {
    val t = LL.tokens
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(tint))
        LLText(label, color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun Panel(
    title: String,
    caption: String,
    tint: Color?,
    modifier: Modifier = Modifier,
    figure: DrawScope.() -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(t.surface2)
            .border(1.dp, tint?.copy(alpha = 0.5f) ?: t.line, RoundedCornerShape(12.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText(title.uppercase(), color = tint ?: t.ink500, size = 9.5.sp,
            weight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Box(Modifier.fillMaxWidth().weight(1f)) { Canvas(Modifier.fillMaxSize()) { figure() } }
        LLText(caption, color = t.ink400, size = 9.5.sp, lineHeight = 12.5.sp)
    }
}

// ── analysis ────────────────────────────────────────────────────────

private val ANALYSIS = listOf(
    "Why did Bowl A form curd but Bowl B did not?" to
        "Warmth let Lactobacillus multiply and make lactic acid in A; the cold fridge kept them dormant in B, so no acid formed and the milk stayed liquid.",
    "What if Bowl B were left at room temperature overnight?" to
        "It would warm up, the bacteria would become active, and it would slowly set into curd (and turn sour).",
    "Why does curd get more sour the longer it is kept warm?" to
        "The bacteria keep fermenting lactose into more lactic acid, lowering the pH further, so it tastes more sour.",
    "Why might Bowl B's milk be 'a little sour' even with no curd?" to
        "A few bacteria still work very slowly in the cold, making a small amount of lactic acid — enough to taste slightly sour but not to set curd.",
)

@Composable
private fun AnalysisStrip(modifier: Modifier = Modifier) {
    val t = LL.tokens
    var selected by remember { mutableStateOf(0) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ANALYSIS.forEachIndexed { i, (q, _) ->
                val sel = i == selected
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp))
                        .background(if (sel) t.accent50 else t.surface2)
                        .border(1.dp, if (sel) t.accent500 else t.line, RoundedCornerShape(8.dp))
                        .clickable { selected = i }.padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    LLText("${i + 1}. $q", color = if (sel) t.accent700 else t.ink400, size = 10.sp,
                        weight = FontWeight.SemiBold, lineHeight = 13.sp)
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(8.dp)).padding(10.dp),
        ) {
            LLText(ANALYSIS[selected].second, color = t.ink200, size = 11.sp, lineHeight = 15.sp)
        }
    }
}

// ── figures ─────────────────────────────────────────────────────────

private fun DrawScope.drawGlassOfMilk(cx: Float, cy: Float, w: Float, h: Float) {
    val left = cx - w / 2f; val right = cx + w / 2f; val top = cy - h / 2f; val bot = cy + h / 2f
    drawRect(Color(0xFFFAFAFA), topLeft = Offset(left + 2f, top + h * 0.18f), size = Size(w - 4f, h * 0.82f - 2f))
    drawLine(Color(0xFF9CA3AF), Offset(left, top), Offset(left + w * 0.08f, bot), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(Color(0xFF9CA3AF), Offset(right, top), Offset(right - w * 0.08f, bot), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(Color(0xFF9CA3AF), Offset(left + w * 0.08f, bot), Offset(right - w * 0.08f, bot), strokeWidth = 2f, cap = StrokeCap.Round)
}

private fun DrawScope.drawQuestionFig() {
    val w = size.width; val h = size.height
    drawGlassOfMilk(w * 0.25f, h * 0.55f, w * 0.22f, h * 0.5f)
    val ink = Color(0xFF94A3B8)
    drawLine(ink, Offset(w * 0.42f, h * 0.55f), Offset(w * 0.54f, h * 0.55f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(ink, Offset(w * 0.54f, h * 0.55f), Offset(w * 0.50f, h * 0.50f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(ink, Offset(w * 0.54f, h * 0.55f), Offset(w * 0.50f, h * 0.60f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawMilkBowl(w * 0.76f, h * 0.6f, w * 0.32f, h * 0.5f, set = 1f, tint = GREEN)
}

private fun DrawScope.drawTwoConditions() {
    val w = size.width; val h = size.height
    drawMilkBowl(w * 0.28f, h * 0.55f, w * 0.4f, h * 0.6f, set = 0f, tint = GREEN)
    sun(w * 0.28f, h * 0.18f, h * 0.07f)
    drawMilkBowl(w * 0.72f, h * 0.55f, w * 0.4f, h * 0.6f, set = 0f, tint = BLUE)
    snowflake(w * 0.72f, h * 0.18f, h * 0.07f)
}

private fun DrawScope.drawInoculum() {
    val w = size.width; val h = size.height
    // spoon with curd
    val sp = Offset(w * 0.35f, h * 0.6f)
    drawLine(Color(0xFF9CA3AF), sp, Offset(w * 0.1f, h * 0.85f), strokeWidth = 4f, cap = StrokeCap.Round)
    drawCircle(Color(0xFFCBD5E1), w * 0.1f, sp)
    drawCircle(Color(0xFFF5EED4), w * 0.075f, sp)
    rod(Offset(sp.x - 4f, sp.y), w * 0.04f, w * 0.018f, Color(0xFFA855F7), 0.4f)
    rod(Offset(sp.x + 5f, sp.y - 3f), w * 0.04f, w * 0.018f, Color(0xFFA855F7), -0.5f)
    // magnifier
    val mc = Offset(w * 0.7f, h * 0.42f); val mr = w * 0.2f
    drawCircle(Color(0xFFFDE68A).copy(alpha = 0.25f), mr, mc)
    drawCircle(Color(0xFF94A3B8), mr, mc, style = Stroke(2.5f))
    drawLactobacillusGlyph(mc, mr * 0.4f)
    drawLine(Color(0xFF6B7280), Offset(mc.x + mr * 0.7f, mc.y + mr * 0.7f), Offset(w * 0.95f, h * 0.85f), strokeWidth = 4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawWaiting() {
    val w = size.width; val h = size.height
    // clock
    val cc = Offset(w * 0.5f, h * 0.28f); val cr = h * 0.18f
    drawCircle(Color(0xFFE5E7EB), cr, cc)
    drawCircle(Color(0xFF6B7280), cr, cc, style = Stroke(2f))
    drawLine(Color(0xFF374151), cc, Offset(cc.x, cc.y - cr * 0.6f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(Color(0xFF374151), cc, Offset(cc.x + cr * 0.5f, cc.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
    // covered bowls
    drawCoveredBowl(w * 0.26f, h * 0.72f, w * 0.34f, h * 0.4f, GREEN)
    sun(w * 0.1f, h * 0.6f, h * 0.05f)
    drawCoveredBowl(w * 0.72f, h * 0.72f, w * 0.34f, h * 0.4f, BLUE)
    snowflake(w * 0.9f, h * 0.6f, h * 0.05f)
}

private fun DrawScope.drawCoveredBowl(cx: Float, cy: Float, w: Float, h: Float, tint: Color) {
    drawMilkBowl(cx, cy, w, h, set = 0f, tint = tint)
    // cloth cover
    drawLine(tint.copy(alpha = 0.6f), Offset(cx - w / 2f - 4f, cy - h * 0.18f), Offset(cx + w / 2f + 4f, cy - h * 0.18f), strokeWidth = 4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawReveal() {
    val w = size.width; val h = size.height
    drawMilkBowl(w * 0.28f, h * 0.55f, w * 0.42f, h * 0.62f, set = 1f, tint = GREEN)
    drawMilkBowl(w * 0.72f, h * 0.55f, w * 0.42f, h * 0.62f, set = 0f, tint = BLUE)
}

private fun DrawScope.drawZoomA() {
    val w = size.width; val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f); val r = kotlin.math.min(w, h) * 0.44f
    drawCircle(Color(0xFFFDFDFD), r, c)
    // coagulated protein mesh
    val pts = (0 until 7).map {
        val a = it * 2.39996f
        Offset(c.x + cos(a) * r * 0.55f, c.y + sin(a) * r * 0.55f)
    }
    for (i in pts.indices) for (j in i + 1 until pts.size) {
        if ((pts[i] - pts[j]).getDistance() < r * 0.9f)
            drawLine(Color(0xFFCBD5E1), pts[i], pts[j], strokeWidth = 1.4f)
    }
    // dense lactobacillus
    for (i in 0 until 6) {
        val a = i * 1.9f
        val rr = r * 0.6f * ((i + 1f) / 7f)
        drawLactobacillusGlyph(Offset(c.x + cos(a) * rr, c.y + sin(a) * rr), r * 0.16f)
    }
    drawCircle(Color(0xFF1F2937), r, c, style = Stroke(r * 0.06f))
}

private fun DrawScope.drawWhyA() {
    val w = size.width; val h = size.height
    val cy = h * 0.5f
    val xs = listOf(0.12f, 0.32f, 0.52f, 0.72f, 0.9f)
    val cols = listOf(Color(0xFFF59E0B), Color(0xFFA855F7), Color(0xFFEF4444), Color(0xFF94A3B8), GREEN)
    xs.forEachIndexed { i, fx ->
        drawCircle(cols[i], h * 0.1f, Offset(w * fx, cy))
        if (i < xs.size - 1) {
            drawLine(Color(0xFF94A3B8), Offset(w * fx + h * 0.1f, cy), Offset(w * xs[i + 1] - h * 0.1f, cy), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawWhyB() {
    val w = size.width; val h = size.height
    // thermometer (cold)
    val tx = w * 0.25f
    drawLine(Color(0xFF94A3B8), Offset(tx, h * 0.2f), Offset(tx, h * 0.7f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
    drawCircle(BLUE, w * 0.07f, Offset(tx, h * 0.75f))
    drawLine(BLUE, Offset(tx, h * 0.55f), Offset(tx, h * 0.7f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
    snowflake(w * 0.25f, h * 0.12f, h * 0.06f)
    // dormant rods (greyed)
    for (i in 0 until 4) {
        val a = i * 1.6f
        rod(Offset(w * 0.65f + cos(a) * w * 0.12f, h * 0.5f + sin(a) * h * 0.18f), w * 0.12f, w * 0.05f, Color(0xFF9CA3AF), a)
    }
}

private fun DrawScope.drawConclusion() {
    val w = size.width; val h = size.height
    drawMilkBowl(w * 0.3f, h * 0.5f, w * 0.4f, h * 0.55f, set = 1f, tint = GREEN)
    drawMilkBowl(w * 0.72f, h * 0.5f, w * 0.4f, h * 0.55f, set = 0f, tint = BLUE)
}

private fun DrawScope.sun(cx: Float, cy: Float, r: Float) {
    drawCircle(Color(0xFFF59E0B), r, Offset(cx, cy))
    for (i in 0 until 8) {
        val a = i * (PI.toFloat() / 4f)
        drawLine(Color(0xFFF59E0B), Offset(cx + cos(a) * r * 1.3f, cy + sin(a) * r * 1.3f),
            Offset(cx + cos(a) * r * 1.8f, cy + sin(a) * r * 1.8f), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.snowflake(cx: Float, cy: Float, r: Float) {
    for (i in 0 until 6) {
        val a = i * (PI.toFloat() / 3f)
        drawLine(BLUE, Offset(cx, cy), Offset(cx + cos(a) * r, cy + sin(a) * r), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}
