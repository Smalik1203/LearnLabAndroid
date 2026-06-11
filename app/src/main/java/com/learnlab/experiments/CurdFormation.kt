package com.learnlab.experiments

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class Cond(
    val letter: String,
    val shortLabel: String,
    val tint: Color,
    val edge: Color,
    val inoculum: Boolean,
) {
    A("A", "Warm + Starter", Color(0xFF10B981), Color(0xFF047857), true),
    B("B", "Scalding Hot",   Color(0xFFE11D48), Color(0xFFBE123C), true),
    C("C", "No Starter",     Color(0xFFF59E0B), Color(0xFFB45309), false),
    D("D", "4 °C · Fridge",  Color(0xFF38BDF8), Color(0xFF0369A1), true),
}

@Composable
fun CurdFormation(controls: ExperimentControls) {
    val t = LL.tokens
    var page by remember { mutableStateOf(0) }
    var hoursRun by remember { mutableStateOf(false) }
    val lapse = remember { Animatable(0f) }

    LaunchedEffect(hoursRun) {
        if (hoursRun) lapse.animateTo(1f, animationSpec = tween(2500))
    }
    LaunchedEffect(page) {
        controls.onProgress(page / 2f)
        if (page == 2) controls.onComplete(1f)
    }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LLText(
                "CURD FORMATION · A CONTROLLED EXPERIMENT",
                color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            LegendStrip()
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    0 -> SetupPage()
                    1 -> SixHoursPage(lapse.value, hoursRun) { hoursRun = true }
                    else -> SciencePage()
                }
            }
            PageNavRow(page, 3) { page = it }
        }
    }
}

// ── page composables ────────────────────────────────────────────────

@Composable
private fun SetupPage() {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Panel(Modifier.weight(1f), "The Question", "Today's question: what makes milk become curd?") {
            QuestionFigure(Modifier.weight(1f))
            PanelCaption("Milk can become curd — but what does it actually need to set?")
        }
        Panel(Modifier.weight(1f), "Four Beakers", "Change one thing at a time — that's a fair test.") {
            FourBeakerRow(0f, Modifier.weight(1f))
            PanelCaption("Each beaker changes just one variable: temperature or starter culture.")
        }
        Panel(Modifier.weight(1f), "What's a Starter?", "Inoculum = a living starter culture.") {
            InoculumFigure(Modifier.weight(1f))
            PanelCaption("A spoon of old curd is the “inoculum” — it carries living Lactobacillus.")
        }
    }
}

@Composable
private fun SixHoursPage(lapseProgress: Float, hoursRun: Boolean, onRun: () -> Unit) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Panel(Modifier.weight(1f), "Six Hours Pass", "Leave them undisturbed and let time work.") {
            HourglassFigure(lapseProgress, Modifier.weight(1f))
            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                label = if (lapseProgress >= 1f) "✓ 6 hours later" else "Run 6 hours",
                onClick = onRun,
                enabled = lapseProgress < 1f && !hoursRun,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Panel(Modifier.weight(1f), "The Reveal", "Only warm milk with a living starter set.") {
            FourBeakerRow(lapseProgress, Modifier.weight(1f))
            PanelCaption(
                if (lapseProgress >= 0.5f) "Hour 6 — only beaker A has set into curd!"
                else "Hour 0 — all four beakers look exactly the same.",
            )
        }
        Panel(Modifier.weight(1f), "Zoom into A", "Texture liquid → gel · smell sour · pH drops.") {
            ZoomFigure(lapseProgress, Modifier.weight(1f))
            PanelCaption("Inside beaker A the bacteria multiplied and the milk turned acidic.")
        }
    }
}

@Composable
private fun SciencePage() {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Panel(Modifier.weight(1f), "Lactobacillus at Work", "Lactic acid coagulates milk protein → curd.") {
            CoagulationFigure(Modifier.weight(1f))
            PanelCaption("Lactobacillus turn milk sugar into lactic acid; the acid makes milk proteins clump into a gel.")
        }
        Panel(Modifier.weight(1f), "Why B, C & D Failed", "Bacteria must be alive, present — and warm.") {
            RevealCard(
                "Why didn’t boiled milk (B) form curd?",
                "The scalding-hot milk killed the Lactobacillus in the starter — no living bacteria were left to ferment it.",
            )
            RevealCard(
                "Why did cold milk (D) fail?",
                "In the fridge, Lactobacillus turn dormant. They work far too slowly in the cold to set milk within six hours.",
            )
            Spacer(Modifier.weight(1f))
            PanelCaption("Beaker C had no starter at all — no bacteria went in, so nothing could ferment.")
        }
        Panel(Modifier.weight(1f), "Conclusion", "Fermentation = the right microbe + the right conditions.") {
            ConclusionFigure(Modifier.weight(1f))
            PanelCaption("Curd forms only when living Lactobacillus have a starter and warmth. Remove either, and fermentation stops.")
        }
    }
}

// ── chrome ──────────────────────────────────────────────────────────

@Composable
private fun LegendStrip() {
    val t = LL.tokens
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Cond.values().forEach { cond ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(t.surface2)
                    .border(1.dp, cond.tint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier.size(18.dp).clip(CircleShape).background(cond.tint),
                    contentAlignment = Alignment.Center,
                ) { LLText(cond.letter, color = Color.White, size = 10.sp, weight = FontWeight.Bold) }
                LLText(cond.shortLabel, color = t.ink400, size = 11.sp)
            }
        }
    }
}

@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    title: String,
    callout: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(title.uppercase(), color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
        content()
        CalloutChip(callout)
    }
}

@Composable
private fun PanelCaption(text: String) {
    LLText(text, color = LL.tokens.ink400, size = 12.sp, lineHeight = 17.sp)
}

@Composable
private fun CalloutChip(text: String) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(t.accent50)
            .border(1.dp, t.accent500.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        LLText(text, color = t.accent700, size = 12.sp, weight = FontWeight.Medium, lineHeight = 16.sp)
    }
}

@Composable
private fun PageNavRow(page: Int, totalPages: Int, onPage: (Int) -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val prevEnabled = page > 0
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (prevEnabled) t.surface2 else t.surface)
                .border(1.dp, t.line, RoundedCornerShape(8.dp))
                .clickable(enabled = prevEnabled) { onPage(page - 1) }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) { LLText("← Prev", color = if (prevEnabled) t.ink200 else t.ink600, size = 13.sp) }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalPages) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (i == page) t.accent500 else t.surface3),
                )
            }
        }

        val nextEnabled = page < totalPages - 1
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (nextEnabled) t.accent500 else t.surface)
                .border(1.dp, if (nextEnabled) t.accent600 else t.line, RoundedCornerShape(8.dp))
                .clickable(enabled = nextEnabled) { onPage(page + 1) }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            LLText(
                "Next →", size = 13.sp, weight = FontWeight.SemiBold,
                color = if (nextEnabled) Color.White else t.ink600,
            )
        }
    }
}

@Composable
private fun RevealCard(question: String, answer: String) {
    val t = LL.tokens
    var revealed by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(10.dp))
            .clickable { revealed = !revealed }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LLText(if (revealed) "▾" else "▸", color = t.accent600, size = 13.sp, weight = FontWeight.Bold)
            LLText(question, color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold, lineHeight = 16.sp)
        }
        if (revealed) LLText(answer, color = t.ink400, size = 12.sp, lineHeight = 17.sp)
        else LLText("Tap to reveal", color = t.ink500, size = 11.sp)
    }
}

// ── figures ─────────────────────────────────────────────────────────

@Composable
private fun QuestionFigure(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val ink = t.ink500
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(t.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width; val h = size.height
            // beaker (left) — empty milk
            beakerAt(w * 0.07f, h * 0.28f, w * 0.26f, h * 0.54f, Color(0xFFF8FAFC), 0.80f)
            // arrow
            drawLine(ink, Offset(w * 0.42f, h * 0.55f), Offset(w * 0.62f, h * 0.55f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.62f, h * 0.55f), Offset(w * 0.56f, h * 0.50f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.62f, h * 0.55f), Offset(w * 0.56f, h * 0.60f), strokeWidth = 3f, cap = StrokeCap.Round)
            // curd bowl (right)
            drawCurdBowl(Offset(w * 0.82f, h * 0.58f), w * 0.13f)
        }
        LLText(
            "?", color = t.accent600, size = 26.sp, weight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        )
    }
}

@Composable
private fun InoculumFigure(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LL.tokens.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width; val h = size.height
            val bx = w * 0.16f; val by = h * 0.34f; val bw = w * 0.38f; val bh = h * 0.54f
            beakerAt(bx, by, bw, bh, Color(0xFFF8FAFC), 0.78f)
            // spoon dropper
            val tip = Offset(bx + bw * 0.50f, by + bh * 0.26f)
            drawLine(Color(0xFF9CA3AF), tip, Offset(w * 0.84f, h * 0.14f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFCBD5E1), 12f, tip)
            drawCircle(Color(0xFFF5EED4), 9f, tip)
            // bacteria rods in spoon dollop
            rod(Offset(tip.x - 4f, tip.y + 2f), 11f, 4f, Color(0xFF7C3AED), 0.4f)
            rod(Offset(tip.x + 5f, tip.y - 2f), 11f, 4f, Color(0xFF7C3AED), -0.6f)
            // bacteria rods in beaker
            rod(Offset(bx + bw * 0.50f, by + bh * 0.55f), 12f, 4f, Color(0xFF7C3AED), 0.2f)
        }
    }
}

@Composable
private fun FourBeakerRow(setProgress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LL.tokens.surface),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Cond.values().forEach { c -> BeakerCell(c, setProgress, Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun BeakerCell(cond: Cond, setProgress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) { drawBeaker(cond, setProgress) }
        // letter badge
        Box(
            Modifier.padding(top = 4.dp).size(18.dp).clip(CircleShape).background(cond.tint),
            contentAlignment = Alignment.Center,
        ) { LLText(cond.letter, color = Color.White, size = 10.sp, weight = FontWeight.Bold) }
        // result badge after time-lapse
        if (setProgress >= 0.9f) {
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp).size(16.dp)
                    .clip(CircleShape).background(cond.tint),
                contentAlignment = Alignment.Center,
            ) { LLText(if (cond == Cond.A) "✓" else "✗", color = Color.White, size = 9.sp, weight = FontWeight.Bold) }
        }
        // short condition label
        LLText(
            cond.shortLabel, color = cond.tint, size = 7.5.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
        )
    }
}

@Composable
private fun HourglassFigure(progress: Float, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(t.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(16.dp)) {
            val w = size.width; val h = size.height
            val cx = w / 2f
            val top = h * 0.16f; val bot = h * 0.84f; val mid = (top + bot) / 2f
            val hw = minOf(w, h) * 0.26f
            val frame = Color(0xFF94A3B8); val sand = Color(0xFFF59E0B)
            val outline = Path().apply {
                moveTo(cx - hw, top); lineTo(cx + hw, top)
                lineTo(cx + hw * 0.10f, mid); lineTo(cx + hw, bot); lineTo(cx - hw, bot)
                lineTo(cx - hw * 0.10f, mid); close()
            }
            drawPath(outline, frame.copy(alpha = 0.14f))
            drawPath(outline, frame, style = Stroke(2.5f))
            drawLine(frame, Offset(cx - hw - 4f, top), Offset(cx + hw + 4f, top), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(frame, Offset(cx - hw - 4f, bot), Offset(cx + hw + 4f, bot), strokeWidth = 4f, cap = StrokeCap.Round)
            val topFrac = 1f - progress
            if (topFrac > 0.02f) {
                val ty = mid + (top - mid) * topFrac; val tw = hw * topFrac
                val topSand = Path().apply { moveTo(cx - tw, ty); lineTo(cx + tw, ty); lineTo(cx, mid); close() }
                drawPath(topSand, sand)
            }
            if (progress > 0.02f) {
                val moundTop = bot - (bot - mid) * progress
                drawRect(sand, topLeft = Offset(cx - hw * progress, moundTop), size = Size(hw * 2f * progress, bot - moundTop))
            }
            if (progress in 0.02f..0.98f) drawLine(sand, Offset(cx, mid), Offset(cx, bot - 4f), strokeWidth = 2f)
        }
        LLText(
            "${(progress * 6f).toInt()} h", color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun ZoomFigure(progress: Float, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(t.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width; val h = size.height
            val cx = w * 0.40f; val cy = h * 0.50f; val r = minOf(w, h) * 0.38f
            drawCircle(Color(0xFFFDE68A).copy(alpha = 0.22f), r, Offset(cx, cy))
            drawCircle(Color(0xFF94A3B8), r, Offset(cx, cy), style = Stroke(2f))
            val n = 3 + (progress * 9f).toInt()
            for (i in 0 until n) {
                val a = i * 2.39996f
                val rr = r * 0.72f * ((i + 1f) / n)
                val px = cx + cos(a) * rr; val py = cy + sin(a) * rr
                rod(Offset(px, py), r * 0.36f, r * 0.16f, Color(0xFF7C3AED), a)
                drawCircle(Color(0xFFF59E0B).copy(alpha = 0.85f), r * 0.05f, Offset(px + r * 0.16f, py + r * 0.16f))
            }
            val sx = w * 0.80f; val sTop = h * 0.20f; val sw = w * 0.13f; val sh = h * 0.60f
            val ph = lerp(Color(0xFF65A30D), Color(0xFFEA580C), progress)
            drawRoundRectCompat(ph, sx, sTop, sw, sh, 4f)
            drawRoundRect(Color(0xFF64748B), topLeft = Offset(sx, sTop), size = Size(sw, sh), cornerRadius = CornerRadius(4f, 4f), style = Stroke(1.5f))
        }
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LLText("pH", color = LL.tokens.ink500, size = 10.sp, weight = FontWeight.SemiBold)
            LLText(phLabel(progress), color = LL.tokens.ink50, size = 13.sp, weight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CoagulationFigure(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LL.tokens.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width; val h = size.height; val u = minOf(w, h)
            val purple = Color(0xFF7C3AED)
            rod(Offset(w * 0.22f, h * 0.36f), u * 0.20f, u * 0.08f, purple, 0.3f)
            rod(Offset(w * 0.20f, h * 0.64f), u * 0.20f, u * 0.08f, purple, -0.4f)
            for (p in listOf(Offset(w * 0.34f, h * 0.50f), Offset(w * 0.40f, h * 0.36f), Offset(w * 0.40f, h * 0.66f))) {
                drawCircle(Color(0xFFF59E0B), u * 0.035f, p)
            }
            val ink = Color(0xFF64748B)
            drawLine(ink, Offset(w * 0.47f, h * 0.50f), Offset(w * 0.57f, h * 0.50f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.57f, h * 0.50f), Offset(w * 0.53f, h * 0.46f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(ink, Offset(w * 0.57f, h * 0.50f), Offset(w * 0.53f, h * 0.54f), strokeWidth = 3f, cap = StrokeCap.Round)
            val pts = listOf(
                Offset(w * 0.68f, h * 0.30f), Offset(w * 0.84f, h * 0.34f),
                Offset(w * 0.74f, h * 0.50f), Offset(w * 0.88f, h * 0.56f),
                Offset(w * 0.66f, h * 0.64f), Offset(w * 0.82f, h * 0.72f),
            )
            for (i in pts.indices) for (j in i + 1 until pts.size) {
                if ((pts[i] - pts[j]).getDistance() < w * 0.22f) {
                    drawLine(Color(0xFF94A3B8), pts[i], pts[j], strokeWidth = 1.5f)
                }
            }
            pts.forEach {
                drawCircle(Color(0xFFE2E8F0), u * 0.045f, it)
                drawCircle(Color(0xFF94A3B8), u * 0.045f, it, style = Stroke(1.2f))
            }
        }
    }
}

@Composable
private fun ConclusionFigure(modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(t.surface),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width; val h = size.height; val u = minOf(w, h)
            val bx = w * 0.10f; val by = h * 0.26f; val bw = w * 0.34f; val bh = h * 0.58f
            // curd in beaker A
            val gel = Color(0xFFF5EED4)
            drawRoundRectCompat(gel, bx + 4f, by + bh * 0.14f, bw - 8f, bh * 0.86f - 3f, 8f)
            val glass = Color(0xFFADB5BD)
            drawRect(Color(0xFFEFF6FF).copy(alpha = 0.18f), topLeft = Offset(bx, by), size = Size(bw, bh))
            drawLine(glass, Offset(bx, by), Offset(bx, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(glass, Offset(bx + bw, by), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(glass, Offset(bx, by + bh), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(Color(0xFF6B7280), Offset(bx - 5f, by), Offset(bx + 9f, by), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(Color(0xFF6B7280), Offset(bx + bw - 9f, by), Offset(bx + bw + 5f, by), strokeWidth = 3f, cap = StrokeCap.Round)
            // sun (warmth)
            val sun = Offset(w * 0.66f, h * 0.28f); val sr = u * 0.10f
            drawCircle(Color(0xFFF59E0B), sr, sun)
            for (i in 0 until 8) {
                val a = i * (PI.toFloat() / 4f)
                drawLine(Color(0xFFF59E0B),
                    Offset(sun.x + cos(a) * sr * 1.3f, sun.y + sin(a) * sr * 1.3f),
                    Offset(sun.x + cos(a) * sr * 1.8f, sun.y + sin(a) * sr * 1.8f),
                    strokeWidth = 2f, cap = StrokeCap.Round)
            }
            // spoon (starter)
            val sp = Offset(w * 0.66f, h * 0.68f)
            drawLine(Color(0xFF9CA3AF), sp, Offset(w * 0.88f, h * 0.82f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFCBD5E1), sr * 0.7f, sp)
            drawCircle(Color(0xFFF5EED4), sr * 0.5f, sp)
        }
        Box(
            Modifier.align(Alignment.TopStart).padding(8.dp).size(22.dp).clip(CircleShape).background(Cond.A.tint),
            contentAlignment = Alignment.Center,
        ) { LLText("✓", color = Color.White, size = 13.sp, weight = FontWeight.Bold) }
    }
}

private fun phLabel(progress: Float): String {
    val v = 6.5f - progress * 2.0f
    return ((v * 10f).toInt() / 10.0).toString()
}

// ── draw helpers ────────────────────────────────────────────────────

private fun DrawScope.drawBeaker(cond: Cond, setProgress: Float) {
    val w = size.width; val h = size.height
    val bw = w * 0.68f; val bh = h * 0.60f
    val bx = (w - bw) / 2f; val by = h * 0.28f
    val glass = Color(0xFFADB5BD); val rim = Color(0xFF6B7280)
    val milk = Color(0xFFFAFAFA)
    val liquidTop = by + bh * 0.12f; val liquidBottom = by + bh - 2f

    // liquid
    if (cond == Cond.A && setProgress > 0f) {
        val curdColor = lerp(milk, Color(0xFFF5EED4), setProgress)
        drawRoundRectCompat(curdColor, bx + 3f, liquidTop, bw - 6f, liquidBottom - liquidTop, 5f * setProgress)
        if (setProgress > 0.40f) {
            val segs = 6; val sw = (bw - 10f) / segs
            for (i in 1..3) {
                val sy = liquidTop + (liquidBottom - liquidTop) * (i * 0.22f)
                for (j in 0 until segs) {
                    val sx = bx + 5f + j * sw
                    val sy1 = sy + if (j % 2 == 0) 2.5f else -2.5f
                    val sy2 = sy + if (j % 2 == 0) -2.5f else 2.5f
                    drawLine(Cond.A.edge.copy(alpha = 0.22f * setProgress), Offset(sx, sy1), Offset(sx + sw, sy2), strokeWidth = 1.2f)
                }
            }
        }
    } else {
        drawRect(milk, topLeft = Offset(bx + 3f, liquidTop), size = Size(bw - 6f, liquidBottom - liquidTop))
    }

    // inoculum dot
    if (cond.inoculum && !(cond == Cond.A && setProgress > 0.55f)) {
        drawCircle(
            cond.tint.copy(alpha = if (cond == Cond.B) 0.18f else 0.42f),
            bw * 0.10f, Offset(bx + bw * 0.50f, liquidTop + (liquidBottom - liquidTop) * 0.35f),
        )
    }

    // condition indicators
    if (cond == Cond.B) {
        // boiling bubbles
        for (i in 0 until 5) {
            val bxb = bx + 12f + (i * 53 % (bw - 24f).toInt()).toFloat()
            val byb = liquidTop + 10f + (i * 31 % ((liquidBottom - liquidTop) * 0.35f).toInt()).toFloat()
            drawCircle(Color.White.copy(alpha = 0.80f), 3f, Offset(bxb, byb))
        }
        // steam wisps
        val steamTop = h * 0.05f; val steamMid = (by - steamTop) / 2f
        for (i in 0 until 3) {
            val sx = bx + bw * (0.22f + i * 0.28f)
            val steamPath = Path().apply {
                moveTo(sx, by - 2f)
                cubicTo(sx - w * 0.04f, by - steamMid * 0.6f, sx + w * 0.04f, by - steamMid * 1.1f, sx - w * 0.02f, steamTop + steamMid * 0.5f)
                cubicTo(sx - w * 0.05f, steamTop + steamMid * 0.2f, sx + w * 0.03f, steamTop, sx, steamTop)
            }
            drawPath(steamPath, Color(0xFFCBD5E1).copy(alpha = 0.68f), style = Stroke(2.2f, cap = StrokeCap.Round))
        }
    } else if (cond == Cond.C) {
        // crossed-out spoon (no starter)
        val spx = bx + bw * 0.70f; val spy = liquidTop + (liquidBottom - liquidTop) * 0.20f
        val spR = bw * 0.10f
        drawOval(cond.tint.copy(alpha = 0.28f), topLeft = Offset(spx - spR, spy - spR * 0.65f), size = Size(spR * 2f, spR * 1.30f))
        drawOval(cond.tint.copy(alpha = 0.52f), topLeft = Offset(spx - spR, spy - spR * 0.65f), size = Size(spR * 2f, spR * 1.30f), style = Stroke(1.5f))
        drawLine(cond.tint.copy(alpha = 0.52f), Offset(spx, spy + spR * 0.65f), Offset(spx + spR * 0.4f, spy + spR * 2.2f), strokeWidth = 1.8f, cap = StrokeCap.Round)
        val xr = spR * 0.85f
        drawLine(Color(0xFFEF4444), Offset(spx - xr, spy - xr * 0.65f), Offset(spx + xr, spy + xr * 0.65f), strokeWidth = 2.2f, cap = StrokeCap.Round)
        drawLine(Color(0xFFEF4444), Offset(spx + xr, spy - xr * 0.65f), Offset(spx - xr, spy + xr * 0.65f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    } else if (cond == Cond.D) {
        // ice crystals
        val crystalR = bw * 0.07f
        val crystals = listOf(
            Offset(bx + bw * 0.26f, liquidTop + (liquidBottom - liquidTop) * 0.32f),
            Offset(bx + bw * 0.60f, liquidTop + (liquidBottom - liquidTop) * 0.54f),
            Offset(bx + bw * 0.40f, liquidTop + (liquidBottom - liquidTop) * 0.72f),
            Offset(bx + bw * 0.76f, liquidTop + (liquidBottom - liquidTop) * 0.28f),
        )
        for (cp in crystals) {
            for (arm in 0 until 6) {
                val ang = arm * (PI.toFloat() / 3f)
                drawLine(cond.tint.copy(alpha = 0.65f), cp,
                    Offset(cp.x + cos(ang) * crystalR, cp.y + sin(ang) * crystalR),
                    strokeWidth = 1.6f, cap = StrokeCap.Round)
            }
            drawCircle(cond.tint.copy(alpha = 0.38f), crystalR * 0.22f, cp)
        }
        // frost on glass exterior
        for (i in 0 until 4) {
            val fy = by + bh * 0.55f + i * bh * 0.10f
            drawLine(cond.tint.copy(alpha = 0.38f), Offset(bx - 5f, fy), Offset(bx - 1f, fy - 5f), strokeWidth = 1.5f)
            drawLine(cond.tint.copy(alpha = 0.38f), Offset(bx + bw + 5f, fy), Offset(bx + bw + 1f, fy - 5f), strokeWidth = 1.5f)
        }
    }

    // graduation marks
    for (i in 1..3) {
        val gy = liquidTop + (liquidBottom - liquidTop) * (1f - i * 0.25f)
        drawLine(glass.copy(alpha = 0.36f), Offset(bx + 2f, gy), Offset(bx + 9f, gy), strokeWidth = 1.2f)
    }

    // glass body
    drawRect(Color(0xFFEFF6FF).copy(alpha = 0.18f), topLeft = Offset(bx, by), size = Size(bw, bh))
    drawLine(glass, Offset(bx, by), Offset(bx, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx + bw, by), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx, by + bh), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx - 5f, by), Offset(bx + 9f, by), strokeWidth = 3.5f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx + bw - 9f, by), Offset(bx + bw + 5f, by), strokeWidth = 3.5f, cap = StrokeCap.Round)
    // spout
    drawLine(rim, Offset(bx + bw + 4f, by), Offset(bx + bw + 9f, by - 5f), strokeWidth = 2f, cap = StrokeCap.Round)
}

private fun DrawScope.beakerAt(bx: Float, by: Float, bw: Float, bh: Float, fill: Color, fillFrac: Float) {
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

private fun DrawScope.drawCurdBowl(c: Offset, r: Float) {
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

private fun DrawScope.rod(center: Offset, len: Float, thick: Float, color: Color, angle: Float) {
    val dx = cos(angle) * len / 2f; val dy = sin(angle) * len / 2f
    drawLine(color, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth = thick, cap = StrokeCap.Round)
}

private fun DrawScope.drawRoundRectCompat(color: Color, left: Float, top: Float, w: Float, h: Float, radius: Float) {
    drawRoundRect(color = color, topLeft = Offset(left, top), size = Size(w, h), cornerRadius = CornerRadius(radius, radius))
}
