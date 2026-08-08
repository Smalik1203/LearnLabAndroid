package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.experiments.ch06kit.ActivityCard
import com.learnlab.experiments.ch06kit.ContextCard
import com.learnlab.experiments.ch06kit.MicroscopeCircle
import com.learnlab.experiments.ch06kit.NoteBox
import com.learnlab.experiments.ch06kit.NoteTone
import com.learnlab.experiments.ch06kit.QuoteCard
import com.learnlab.experiments.ch06kit.RevealCell
import com.learnlab.experiments.ch06kit.SlideDeck
import com.learnlab.experiments.ch06kit.SnapSlider
import com.learnlab.experiments.ch06kit.TemplateA
import com.learnlab.experiments.ch06kit.TemplateB
import com.learnlab.experiments.ch06kit.drawDoughBowl
import com.learnlab.experiments.ch06kit.drawTextAt
import com.learnlab.experiments.ch06kit.drawYeastGlyph
import com.learnlab.store.ExperimentControls
import kotlin.math.cos
import kotlin.math.sin

/**
 * Simulation 5 — Activity 2.8: yeast fermentation. Discover, through experiment, that dough rises
 * because living yeast ferments sugar and releases CO₂ — and only when it is warm.
 */

private enum class Temp(val label: String) { Warm("Warm ~37°C"), Cold("Cold ~5°C"), Hot("Hot ~65°C") }

private data class Bowl(var yeast: Boolean, var temp: Temp, var sugar: Boolean)

/** Final dough rise (0..1) for a set of conditions, before factoring in elapsed time. */
private fun riseTarget(b: Bowl): Float = when {
    !b.yeast -> 0.04f
    b.temp == Temp.Hot -> 0.05f       // yeast killed
    b.temp == Temp.Cold -> 0.12f      // too slow
    !b.sugar -> 0.22f                 // only flour's own sugar
    else -> 1.0f
}

private fun riseAt(b: Bowl, hours: Float): Float {
    val frac = (hours / 4f).coerceIn(0f, 1f)
    return riseTarget(b) * frac
}

@Composable
fun YeastFermentation(controls: ExperimentControls) {
    var a by remember { mutableStateOf(Bowl(yeast = true, temp = Temp.Warm, sugar = true)) }
    var b by remember { mutableStateOf(Bowl(yeast = false, temp = Temp.Warm, sugar = true)) }
    SlideDeck(
        controls = controls,
        slideCount = 7,
        eyebrow = "ACTIVITY 2.8 · YEAST FERMENTATION",
        topics = listOf("Introduction", "What is yeast", "Setup", "Time-lapse", "Variables", "Record", "In food"),
    ) { page ->
        when (page) {
            0 -> YeastOpener()
            1 -> YeastConcept()
            2 -> YeastSetup(a, b, { a = it }, { b = it })
            3 -> YeastTimeLapse(a, b)
            4 -> YeastVariables()
            5 -> YeastRecord()
            else -> YeastFood()
        }
    }
}

@Composable
private fun YeastOpener() {
    TemplateA(
        badge = "SECTION 2.4.2 · 20 MIN",
        title = "Why does dough rise — and what does that have to do with microorganisms?",
        lead = "You may have seen bread dough slowly grow in a warm kitchen, noticed bread's soft spongy texture " +
            "full of tiny holes, or smelled the slightly fermented smell of proofing dough. All of it has the same " +
            "cause: yeast — and yeast is a microorganism.",
    ) {
        QuoteCard(
            text = "Yeast is a type of microorganism. It belongs to a group of microorganisms called fungi. " +
                "Yeast grows well in warm conditions.",
            attribution = "Curiosity, Grade 8, Chapter 2",
        )
        ContextCard(
            label = "The experiment",
            body = "Activity 2.8 sets up two bowls of dough — one with yeast, one without — and asks what happens " +
                "after 4–5 hours. Form your prediction, then test it by changing the conditions yourself.",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThoughtBubble("Add yeast and keep it warm — will it rise more, less, or the same as dough without yeast?", Modifier.weight(1f))
            ThoughtBubble("Does water temperature matter? What if it were cold?", Modifier.weight(1f))
            ThoughtBubble("What is the yeast actually doing inside the dough?", Modifier.weight(1f))
        }
    }
}

@Composable
private fun ThoughtBubble(text: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(14.dp)).padding(12.dp),
    ) {
        LLText("💭  $text", color = t.ink200, size = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun YeastConcept() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    TemplateA(
        badge = "SECTION 2.4.2 · UNDERSTAND FIRST",
        title = "Yeast is alive — and it breathes",
        lead = "Yeast is a unicellular fungus; each grain of yeast powder holds millions of cells. Like all living " +
            "things it respires, breaking down food for energy. Its food here is the sugar you added. Through " +
            "fermentation, yeast turns sugar into carbon dioxide and a little alcohol.",
    ) {
        Box(
            Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(12.dp)).background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            Canvas(Modifier.fillMaxSize().padding(10.dp)) { drawFermentationDiagram(tm, t.ink400) }
        }
        LLText(
            "The carbon dioxide forms millions of tiny bubbles trapped by the stretchy gluten in the flour — that " +
                "is what makes dough light, spongy, and fluffy. The alcohol gives it the slightly fermented smell.",
            color = t.ink200, size = 13.sp, lineHeight = 19.sp,
        )
        NoteBox(
            text = "Warm water (around 37°C) activates the yeast so it consumes sugar and releases CO₂ quickly. " +
                "Cold water slows or stops it; water hotter than ~60°C kills the yeast cells.",
            tone = NoteTone.Info, label = "Why warm water?",
        )
    }
}

private fun DrawScope.drawFermentationDiagram(tm: TextMeasurer, ink: Color) {
    val w = size.width; val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f)
    drawYeastGlyph(c, kotlin.math.min(w, h) * 0.12f)
    // sugar in (left)
    drawCircle(Color(0xFF38BDF8), h * 0.05f, Offset(w * 0.2f, h * 0.5f))
    drawTextAt(tm, "sugar", Offset(w * 0.13f, h * 0.62f), ink, 11.sp, FontWeight.SemiBold)
    drawArrow(Offset(w * 0.27f, h * 0.5f), Offset(w * 0.37f, h * 0.5f), ink)
    // CO2 + alcohol out (right)
    drawArrow(Offset(w * 0.63f, h * 0.42f), Offset(w * 0.73f, h * 0.32f), ink)
    drawCircle(Color(0xFF9CA3AF).copy(alpha = 0.5f), h * 0.05f, Offset(w * 0.78f, h * 0.28f))
    drawTextAt(tm, "CO₂", Offset(w * 0.83f, h * 0.24f), ink, 11.sp, FontWeight.SemiBold)
    drawArrow(Offset(w * 0.63f, h * 0.58f), Offset(w * 0.73f, h * 0.68f), ink)
    drawTextAt(tm, "alcohol", Offset(w * 0.75f, h * 0.7f), ink, 11.sp, FontWeight.SemiBold)
}

private fun DrawScope.drawArrow(from: Offset, to: Offset, color: Color) {
    drawLine(color, from, to, strokeWidth = 2.5f, cap = StrokeCap.Round)
    val dx = to.x - from.x; val dy = to.y - from.y
    val len = kotlin.math.hypot(dx, dy)
    val ux = dx / len; val uy = dy / len
    drawLine(color, to, Offset(to.x - ux * 8f - uy * 5f, to.y - uy * 8f + ux * 5f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(color, to, Offset(to.x - ux * 8f + uy * 5f, to.y - uy * 8f - ux * 5f), strokeWidth = 2.5f, cap = StrokeCap.Round)
}

@Composable
private fun YeastSetup(a: Bowl, b: Bowl, onA: (Bowl) -> Unit, onB: (Bowl) -> Unit) {
    TemplateB(
        left = {
            ActivityCard(
                code = "ACTIVITY 2.8 · INVESTIGATE",
                title = "Set up your two bowls",
                description = "Choose the conditions for each bowl, then run the time-lapse on the next slide. Try " +
                    "warm vs cold, yeast vs none, sugar vs none.",
                tags = listOf("🫙  Bowl A and Bowl B", "🎛️  Choose each condition", "⏱️  Run the time-lapse next"),
            )
            NoteBox(
                text = "Default setup matches the textbook: Bowl A = yeast + warm + sugar (experimental); " +
                    "Bowl B = no yeast + warm + sugar (control).",
                tone = NoteTone.Info,
            )
        },
        right = {
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BowlSetup("Bowl A", a, onA, Modifier.weight(1f))
                BowlSetup("Bowl B", b, onB, Modifier.weight(1f))
            }
        },
    )
}

@Composable
private fun BowlSetup(title: String, bowl: Bowl, onChange: (Bowl) -> Unit, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LLText(title, color = t.ink50, size = 15.sp, weight = FontWeight.Bold)
        SegLabel("Yeast")
        SegToggle(listOf("Add yeast", "No yeast"), if (bowl.yeast) 0 else 1) { onChange(bowl.copy(yeast = it == 0)) }
        SegLabel("Water temperature")
        SegToggle(Temp.entries.map { it.label }, Temp.entries.indexOf(bowl.temp)) { onChange(bowl.copy(temp = Temp.entries[it])) }
        SegLabel("Sugar")
        SegToggle(listOf("Add sugar", "No sugar"), if (bowl.sugar) 0 else 1) { onChange(bowl.copy(sugar = it == 0)) }
    }
}

@Composable
private fun SegLabel(text: String) {
    LLText(text.uppercase(), color = LL.tokens.ink500, size = 10.sp, weight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
}

@Composable
private fun SegToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val t = LL.tokens
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (sel) t.accent50 else t.surface)
                    .border(1.dp, if (sel) t.accent500 else t.line, RoundedCornerShape(8.dp))
                    .clickable { onSelect(i) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LLText(label, color = if (sel) t.accent700 else t.ink400, size = 11.sp,
                    weight = FontWeight.SemiBold, align = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun YeastTimeLapse(a: Bowl, b: Bowl) {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    var hour by remember { mutableStateOf(0) }
    val hours = hour.toFloat()
    TemplateB(
        left = {
            ActivityCard(
                code = "ACTIVITY 2.8 · OBSERVE",
                title = "Watch what happens over time",
                description = "Scrub the slider through 0–5 hours and watch both bowls change. Zoom in to see the " +
                    "yeast and CO₂ bubbles inside Bowl A.",
                tags = listOf("⏱️  Scrub 0–5 hours", "🔬  Microscope view of Bowl A", "📋  Note the changes"),
            )
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                MicroscopeCircle(Modifier.fillMaxSize()) { c, r -> drawYeastMicro(a, hours, c, r) }
            }
            LLText("Microscope · inside Bowl A", color = t.ink400, size = 11.sp,
                weight = FontWeight.SemiBold, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        right = {
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DoughBowlView("Bowl A", a, hours, Modifier.weight(1f))
                DoughBowlView("Bowl B", b, hours, Modifier.weight(1f))
            }
            SnapSlider("Time", listOf("0h", "1h", "2h", "3h", "4h", "5h"), hour, { hour = it })
            NoteBox(
                text = "Did your prediction match? Which variable made the biggest difference — yeast, temperature, " +
                    "or sugar?",
                tone = NoteTone.Info,
            )
        },
    )
}

@Composable
private fun DoughBowlView(title: String, bowl: Bowl, hours: Float, modifier: Modifier = Modifier) {
    val t = LL.tokens
    val rise = riseAt(bowl, hours)
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText(title, color = t.ink50, size = 13.sp, weight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                drawDoughBowl(size.width / 2f, size.height * 0.55f, size.width * 0.7f, size.height * 0.7f, rise)
            }
        }
        val state = when {
            rise >= 0.8f -> "Risen & fluffy"
            rise >= 0.35f -> "Rising…"
            rise >= 0.15f -> "Barely changed"
            else -> "No change"
        }
        LLText(state, color = if (rise >= 0.35f) t.accent700 else t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
        LLText(condText(bowl), color = t.ink500, size = 10.sp, align = TextAlign.Center)
    }
}

private fun condText(b: Bowl): String =
    "${if (b.yeast) "yeast" else "no yeast"} · ${b.temp.label.substringBefore(" ")} · ${if (b.sugar) "sugar" else "no sugar"}"

private fun DrawScope.drawYeastMicro(bowl: Bowl, hours: Float, c: Offset, r: Float) {
    val rise = riseAt(bowl, hours)
    val cells = 2 + (rise * 9).toInt()
    for (i in 0 until cells) {
        val a = i * 2.39996f
        val rr = r * 0.7f * ((i + 1f) / (cells + 1f))
        val p = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr)
        drawYeastGlyph(p, r * 0.13f)
    }
    // CO2 bubbles grow with rise
    val bubbles = (rise * 10).toInt()
    for (i in 0 until bubbles) {
        val a = i * 1.7f
        val rr = r * 0.55f * ((i + 1f) / (bubbles + 1f))
        drawCircle(Color(0xFF9CA3AF).copy(alpha = 0.4f), r * 0.06f, Offset(c.x + sin(a) * rr, c.y - cos(a) * rr))
    }
}

@Composable
private fun YeastVariables() {
    TemplateB(
        leftWeight = 2f, rightWeight = 3f,
        left = {
            ActivityCard(
                code = "ACTIVITY 2.8 · DISCOVER",
                title = "Change one variable at a time",
                description = "Three controlled comparisons, each isolating one variable to test its effect on " +
                    "rising.",
                tags = listOf("🌡️  Temperature", "🍬  Sugar", "🧫  Yeast"),
            )
        },
        right = {
            ComparisonRow("Temperature", "Both: yeast + sugar", "Warm → rises", "Cold → does not rise",
                "Yeast is alive; cold slows its metabolism to almost nothing. This is why refrigerating dough " +
                    "stops it rising.")
            ComparisonRow("Sugar", "Both: yeast + warm", "Sugar → rises", "No sugar → barely rises",
                "Sugar is yeast's food. Without it there's nothing to ferment — only the flour's tiny natural " +
                    "sugar gives a small rise.")
            ComparisonRow("Yeast", "Both: warm + sugar", "Yeast → rises", "No yeast → no rise",
                "The core result: no yeast = no CO₂ = no rise. The rise is caused by the living organism, not a " +
                    "chemical reaction of flour and water.")
        },
    )
}

@Composable
private fun ComparisonRow(variable: String, both: String, win: String, lose: String, insight: String) {
    val t = LL.tokens
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LLText(variable.uppercase(), color = t.accent700, size = 11.sp, weight = FontWeight.Bold, letterSpacing = 1.sp)
            LLText("·  $both", color = t.ink500, size = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniResult(win, true, Modifier.weight(1f))
            MiniResult(lose, false, Modifier.weight(1f))
        }
        LLText(insight, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun MiniResult(label: String, success: Boolean, modifier: Modifier = Modifier) {
    val t = LL.tokens
    val tint = if (success) t.accent500 else t.rose600
    Row(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(t.surface)
            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(if (success) "✓" else "✗", color = tint, size = 13.sp, weight = FontWeight.Bold)
        LLText(label, color = t.ink200, size = 11.sp)
    }
}

@Composable
private fun YeastRecord() {
    val t = LL.tokens
    TemplateA(
        badge = "ACTIVITY 2.8 · RECORD",
        title = "Record your observations",
        lead = "Complete the observation table (tap to reveal), then answer the textbook's questions.",
    ) {
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, t.line, RoundedCornerShape(12.dp))) {
            Row(modifier = Modifier.fillMaxWidth().background(t.surface2).padding(horizontal = 10.dp, vertical = 8.dp)) {
                YHead("Condition", Modifier.weight(2.4f)); YHead("Volume", Modifier.weight(1.6f))
                YHead("Texture", Modifier.weight(1.6f)); YHead("Smell", Modifier.weight(1.6f))
            }
            YRow("Bowl A (yeast + warm + sugar)", "Increased ~50%", "Soft, spongy", "Slightly fermented", 0, t)
            YRow("Bowl B (no yeast + warm + sugar)", "No change", "Dense, flat", "Plain flour", 1, t)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            QLine("1. What is the role of yeast in making the dough rise?")
            QLine("2. Why was sugar added to the flour?")
            QLine("3. Why was warm water used instead of cold?")
            QLine("4. Name two other foods that use yeast fermentation. (idli, dosa, bhatura, cake)")
        }
    }
}

@Composable
private fun YHead(text: String, modifier: Modifier = Modifier) =
    LLText(text.uppercase(), color = LL.tokens.ink500, size = 10.sp, weight = FontWeight.SemiBold, letterSpacing = 1.sp, modifier = modifier)

@Composable
private fun YRow(cond: String, vol: String, tex: String, smell: String, idx: Int, t: com.learnlab.design.LearnLabTokens) {
    Row(
        modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) t.surface else t.surface2.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(cond, color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(2.4f))
        RevealCell(vol, Modifier.weight(1.6f).padding(horizontal = 3.dp))
        RevealCell(tex, Modifier.weight(1.6f).padding(horizontal = 3.dp))
        RevealCell(smell, Modifier.weight(1.6f).padding(horizontal = 3.dp))
    }
}

@Composable
private fun QLine(text: String) = LLText(text, color = LL.tokens.ink200, size = 12.sp, lineHeight = 17.sp)

@Composable
private fun YeastFood() {
    val t = LL.tokens
    TemplateA(
        badge = "SECTION 2.4.2 · THE BIGGER PICTURE",
        title = "Fermentation: microorganisms in your kitchen",
        lead = "Yeast is not the only kitchen microbe. Bacteria such as Lactobacillus ferment idli and dosa batter " +
            "and curd. Instead of alcohol (like yeast), these bacteria make lactic acid — which is why curd is sour. " +
            "You'll test this in Activity 2.9.",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FoodCard("🍞", "Bread", "Yeast", "CO₂ makes it rise", Modifier.weight(1f))
            FoodCard("🥞", "Idli / Dosa", "Lactobacillus", "Ferments the batter", Modifier.weight(1f))
            FoodCard("🥣", "Curd", "Lactobacillus", "Sets milk, turns it sour", Modifier.weight(1f))
            FoodCard("🫓", "Bhatura", "Yeast / ferment", "Light, fermented dough", Modifier.weight(1f))
        }
        NoteBox(
            text = "You buy fermented foods every day without thinking about the microbes that made them — the " +
                "soft bread, sour curd, light idli are all microbes consuming a food source and releasing " +
                "by-products. Humans have harnessed this for thousands of years.",
            tone = NoteTone.Info, label = "Conclusion",
        )
    }
}

@Composable
private fun FoodCard(emoji: String, name: String, organism: String, product: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LLText(emoji, size = 28.sp)
        LLText(name, color = t.ink50, size = 13.sp, weight = FontWeight.Bold)
        LLText(organism, color = t.accent700, size = 11.sp, weight = FontWeight.SemiBold)
        LLText(product, color = t.ink400, size = 10.sp, lineHeight = 13.sp, align = TextAlign.Center)
    }
}
