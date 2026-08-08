package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
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
import com.learnlab.experiments.ch06kit.CELL_SHAPES
import com.learnlab.experiments.ch06kit.ContextCard
import com.learnlab.experiments.ch06kit.NoteBox
import com.learnlab.experiments.ch06kit.NoteTone
import com.learnlab.experiments.ch06kit.QuoteCard
import com.learnlab.experiments.ch06kit.RevealCard
import com.learnlab.experiments.ch06kit.SlideDeck
import com.learnlab.experiments.ch06kit.SnapSlider
import com.learnlab.experiments.ch06kit.TemplateA
import com.learnlab.experiments.ch06kit.TemplateB
import com.learnlab.experiments.ch06kit.drawAmoebaGlyph
import com.learnlab.experiments.ch06kit.drawTextAt
import com.learnlab.store.ExperimentControls

/**
 * Simulation 3 — Levels of organisation: cell → tissue → organ → organ system → organism.
 * An interactive zoom-out, a cell-shapes gallery, unicellular vs multicellular, and discussion.
 */

private val LEVELS = listOf("Cell", "Tissue", "Organ", "Organ system", "Organism")
private val LEVEL_DEFS = listOf(
    "A single muscle cell from the stomach wall — spindle-shaped and flexible.",
    "Muscle tissue — a group of similar cells working together.",
    "The stomach — an organ made of several tissues, including muscle.",
    "The digestive system — several organs working together to digest food.",
    "All organ systems together make up a complete organism.",
)

@Composable
fun LevelsOfOrganisation(controls: ExperimentControls) {
    SlideDeck(
        controls = controls,
        slideCount = 5,
        eyebrow = "SECTION 2.2 · LEVELS OF ORGANISATION",
        topics = listOf("Introduction", "Zoom explorer", "Why cells differ", "Unicellular vs multicellular", "Discuss"),
    ) { page ->
        when (page) {
            0 -> LvlOpener()
            1 -> LvlZoom()
            2 -> LvlShapes()
            3 -> LvlUniMulti()
            else -> LvlDiscuss()
        }
    }
}

@Composable
private fun LvlOpener() {
    TemplateA(
        badge = "SECTION 2.2 · 15 MIN",
        title = "How do cells build a complete living organism?",
        lead = "You know all living beings are made of cells. But how does a single microscopic cell become a " +
            "stomach, a leaf, or an eye? Cells do not work alone — they organise into progressively larger and " +
            "more complex structures, each doing what no single cell could manage alone.",
    ) {
        QuoteCard(
            text = "Cell is the basic unit of life, just like a brick is the basic unit of a wall.",
            attribution = "Curiosity, Grade 8, Chapter 2",
        )
        ContextCard(
            label = "The hierarchy",
            body = "Five levels of organisation. Smallest: a single cell. Largest: a complete organism. In " +
                "between are three levels — tissue, organ, and organ system. Together they show how life is " +
                "assembled from its smallest pieces.",
        )
    }
}

@Composable
private fun LvlZoom() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    var level by remember { mutableStateOf(0) }
    TemplateB(
        left = {
            ActivityCard(
                code = "SECTION 2.2 · EXPLORE",
                title = "Zoom out from one cell to a whole organism",
                description = "Drag the slider to zoom out through the five levels and watch how cells build up " +
                    "into you.",
                tags = listOf(
                    "🔬  Start at the cell level",
                    "⬅➡  Drag to zoom out",
                    "🫀  Example: the digestive system",
                ),
            )
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LLText(LEVELS[level].uppercase(), color = t.accent700, size = 13.sp,
                        weight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    LLText(LEVEL_DEFS[level], color = t.ink200, size = 12.sp, lineHeight = 17.sp)
                }
            }
        },
        right = {
            Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))) {
                Canvas(Modifier.fillMaxSize().padding(14.dp)) {
                    when (level) {
                        0 -> drawCellLevel(tm, t.ink400)
                        1 -> drawTissueLevel()
                        2 -> drawOrganLevel(tm, t.ink400)
                        3 -> drawSystemLevel(tm, t.ink400)
                        else -> drawOrganismLevel(tm, t.ink400)
                    }
                }
            }
            SnapSlider("Zoom level", LEVELS, level, { level = it })
        },
    )
}

@Composable
private fun LvlShapes() {
    val t = LL.tokens
    var expanded by remember { mutableStateOf<String?>(null) }
    TemplateA(
        badge = "SECTION 2.1.1 · VARIATION IN CELLS",
        title = "Why do cells look so different from each other?",
        lead = "Not all cells look alike. Each shape is a solution to a specific job: a muscle cell is a spindle " +
            "so it can contract; a nerve cell is long and branched so it can carry messages far; a cheek cell is " +
            "flat so it tiles into a lining; some plant cells are tubes that carry water upward.",
    ) {
        for (rowIdx in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                for (colIdx in 0 until 2) {
                    val shape = CELL_SHAPES[rowIdx * 2 + colIdx]
                    val isOpen = expanded == shape.id
                    Column(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp)).background(t.surface)
                            .border(if (isOpen) 2.dp else 1.dp, if (isOpen) t.accent500 else t.line, RoundedCornerShape(12.dp))
                            .clickable { expanded = if (isOpen) null else shape.id }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.height(70.dp).clip(RoundedCornerShape(8.dp)).background(t.surface2)
                                .padding(4.dp).then(Modifier.fillMaxWidth(0.42f))) {
                                Canvas(Modifier.fillMaxSize()) {
                                    shape.drawer(this, Offset(size.width / 2f, size.height / 2f),
                                        kotlin.math.min(size.width, size.height) * 0.32f)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                LLText(shape.name, color = t.ink50, size = 13.sp, weight = FontWeight.Bold)
                                LLText(shape.tagline, color = t.ink400, size = 11.sp)
                            }
                        }
                        if (isOpen) LLText(shape.detail, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
        NoteBox(
            text = "The unique shape, size, and structure of a cell help it carry out its specific job. Shape is " +
                "not random — it is purpose-built.",
            tone = NoteTone.Info, label = "Key principle",
        )
    }
}

@Composable
private fun LvlUniMulti() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    TemplateA(
        badge = "SECTION 2.5 · UNICELLULAR & MULTICELLULAR",
        title = "Can one cell do everything a whole organism needs?",
        lead = "Complex organisms begin as a single cell — an egg — that divides again and again into a body of " +
            "many cells. These are multicellular organisms (animals, plants, humans). But some organisms are just " +
            "one cell: bacteria and Amoeba each eat, breathe, move, and reproduce with a single cell.",
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UniMultiPanel("Amoeba — one cell does everything", Modifier.weight(1f)) {
                drawAmoebaGlyph(Offset(size.width / 2f, size.height / 2f), kotlin.math.min(size.width, size.height) * 0.22f)
            }
            UniMultiPanel("Human — trillions of specialised cells", Modifier.weight(1f)) {
                drawOrganismLevel(tm, Color(0xFF94A3B8))
            }
        }
        NoteBox(
            text = "The yolk of an ostrich egg is a single cell — the largest known cell, about 130–170 mm across. " +
                "The shell and white are extra non-cellular material that protect and nourish it.",
            tone = NoteTone.Warn, label = "Ever heard of…",
        )
    }
}

@Composable
private fun UniMultiPanel(caption: String, modifier: Modifier = Modifier, draw: DrawScope.() -> Unit) {
    val t = LL.tokens
    Column(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f)) { Canvas(Modifier.fillMaxSize()) { draw() } }
        LLText(caption, color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
    }
}

@Composable
private fun LvlDiscuss() {
    TemplateB(
        leftWeight = 2f, rightWeight = 3f,
        left = {
            ActivityCard(
                code = "SECTION 2.2 · DISCUSS",
                title = "Connect and reflect",
                description = "Use these questions to consolidate how cells, organisation, and the link between " +
                    "structure and function fit together.",
                tags = listOf("💬  Discuss in pairs", "📝  Note your reasoning", "🏫  Share with the class"),
            )
        },
        right = {
            RevealCard(
                "The textbook compares a cell to a brick and a tissue to a wall. Extend it — what is an organ? An organ system? Where does the analogy break down?",
                "An organ is like a whole structure built from walls (e.g. a room); an organ system is like a building. The analogy breaks down because cells are alive, communicate, and specialise — bricks don't.",
            )
            RevealCard(
                "A stomach muscle cell and a brain nerve cell carry the same DNA. Why do they look so different?",
                "Different cells switch on different parts of the same DNA, so each builds the structures it needs for its own job — same instructions, different pages used.",
            )
            RevealCard(
                "An Amoeba does every life function in one cell; a human uses trillions of specialised cells. What are the advantages of each?",
                "One cell is simple and self-sufficient but limited in size and ability. Many specialised cells let an organism grow large and do complex things, but they must cooperate and depend on each other.",
            )
            NoteBox(
                text = "Cells are not just the smallest living things — they are the unit of function. Every " +
                    "property of life, from breathing to thinking, traces back to what cells do.",
                tone = NoteTone.Info, label = "Conclusion",
            )
        },
    )
}

// ── level drawers ───────────────────────────────────────────────────

private fun DrawScope.drawSpindle(c: Offset, w: Float, h: Float, fill: Color, edge: Color) {
    val path = Path().apply {
        moveTo(c.x - w / 2f, c.y)
        cubicTo(c.x - w * 0.25f, c.y - h, c.x + w * 0.25f, c.y - h, c.x + w / 2f, c.y)
        cubicTo(c.x + w * 0.25f, c.y + h, c.x - w * 0.25f, c.y + h, c.x - w / 2f, c.y)
        close()
    }
    drawPath(path, fill)
    drawPath(path, edge, style = Stroke(2f))
}

private fun DrawScope.drawCellLevel(tm: TextMeasurer, ink: Color) {
    val c = Offset(size.width * 0.45f, size.height / 2f)
    val w = size.width * 0.5f; val h = size.height * 0.16f
    drawSpindle(c, w, h, Color(0xFFFCA5A5).copy(alpha = 0.55f), Color(0xFFB91C1C))
    for (i in 1..8) {
        val x = c.x - w / 2f + w * (i / 9f)
        drawLine(Color(0xFFB91C1C).copy(alpha = 0.3f), Offset(x, c.y - h * 0.5f), Offset(x, c.y + h * 0.5f), strokeWidth = 1f)
    }
    drawCircle(Color(0xFF7C3AED), h * 0.30f, c)
    drawTextAt(tm, "nucleus", Offset(c.x + h * 0.4f, c.y - h * 0.9f), ink, 10.sp, FontWeight.SemiBold)
    drawTextAt(tm, "cell membrane", Offset(c.x + w * 0.45f, c.y), ink, 10.sp, FontWeight.SemiBold)
    drawTextAt(tm, "cytoplasm", Offset(c.x - w * 0.2f, c.y + h * 0.95f), ink, 10.sp, FontWeight.SemiBold)
}

private fun DrawScope.drawTissueLevel() {
    val cols = 4; val rows = 3
    val cw = size.width / (cols + 0.5f); val ch = size.height / (rows + 0.5f)
    for (r in 0 until rows) for (col in 0 until cols) {
        val cx = cw * (col + 0.6f) + (if (r % 2 == 1) cw * 0.4f else 0f)
        val cy = ch * (r + 0.7f)
        drawSpindle(Offset(cx, cy), cw * 0.9f, ch * 0.30f, Color(0xFFFCA5A5).copy(alpha = 0.5f), Color(0xFFB91C1C))
        drawCircle(Color(0xFF7C3AED), ch * 0.08f, Offset(cx, cy))
    }
}

private fun DrawScope.drawStomach(cx: Float, cy: Float, s: Float, fill: Color, edge: Color, wallLines: Boolean) {
    val path = Path().apply {
        moveTo(cx - s * 0.2f, cy - s)
        cubicTo(cx + s * 0.9f, cy - s * 0.9f, cx + s, cy + s * 0.2f, cx + s * 0.4f, cy + s * 0.7f)
        cubicTo(cx + s * 0.1f, cy + s, cx - s * 0.7f, cy + s * 0.9f, cx - s * 0.7f, cy + s * 0.2f)
        cubicTo(cx - s * 0.7f, cy - s * 0.4f, cx - s * 0.5f, cy - s * 0.8f, cx - s * 0.2f, cy - s)
        close()
    }
    drawPath(path, fill)
    drawPath(path, edge, style = Stroke(2.5f))
    if (wallLines) {
        drawPath(path, Color(0xFFB91C1C).copy(alpha = 0.4f), style = Stroke(6f))
    }
}

private fun DrawScope.drawOrganLevel(tm: TextMeasurer, ink: Color) {
    val cx = size.width * 0.42f; val cy = size.height * 0.5f; val s = kotlin.math.min(size.width, size.height) * 0.32f
    drawStomach(cx, cy, s, Color(0xFFFBCFE8).copy(alpha = 0.6f), Color(0xFFBE185D), wallLines = true)
    drawTextAt(tm, "stomach (organ)", Offset(cx + s * 0.6f, cy - s), ink, 11.sp, FontWeight.SemiBold)
    drawTextAt(tm, "muscle tissue in the wall", Offset(cx + s * 0.6f, cy), ink, 10.sp, FontWeight.SemiBold)
}

private fun DrawScope.drawSystemLevel(tm: TextMeasurer, ink: Color) {
    val w = size.width; val h = size.height
    val cx = w * 0.42f
    val tract = Color(0xFFEC9DBE); val edge = Color(0xFFBE185D)
    // mouth
    drawCircle(tract, w * 0.025f, Offset(cx, h * 0.10f))
    // oesophagus
    drawLine(tract, Offset(cx, h * 0.12f), Offset(cx, h * 0.34f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
    // stomach
    drawStomach(cx + w * 0.02f, h * 0.42f, kotlin.math.min(w, h) * 0.13f, tract, edge, wallLines = false)
    // small intestine coil
    val coil = Path().apply {
        moveTo(cx, h * 0.52f)
        var yy = h * 0.55f; var dir = 1f
        while (yy < h * 0.82f) {
            quadraticTo(cx + dir * w * 0.14f, yy, cx, yy + h * 0.05f)
            yy += h * 0.05f; dir = -dir
        }
    }
    drawPath(coil, edge, style = Stroke(w * 0.018f, cap = StrokeCap.Round))
    // large intestine frame
    val li = Path().apply {
        moveTo(cx - w * 0.16f, h * 0.80f)
        lineTo(cx - w * 0.16f, h * 0.46f)
        lineTo(cx + w * 0.18f, h * 0.46f)
        lineTo(cx + w * 0.18f, h * 0.82f)
    }
    drawPath(li, tract, style = Stroke(w * 0.03f, cap = StrokeCap.Round))
    drawTextAt(tm, "digestive system", Offset(w * 0.62f, h * 0.46f), ink, 11.sp, FontWeight.SemiBold)
}

private fun DrawScope.drawOrganismLevel(tm: TextMeasurer, ink: Color) {
    val w = size.width; val h = size.height
    val cx = w * 0.5f
    val body = Color(0xFFCBD5E1).copy(alpha = 0.5f); val edge = Color(0xFF64748B)
    // head
    drawCircle(body, h * 0.09f, Offset(cx, h * 0.13f))
    drawCircle(edge, h * 0.09f, Offset(cx, h * 0.13f), style = Stroke(2f))
    // torso
    val torso = Path().apply {
        moveTo(cx - w * 0.12f, h * 0.24f)
        lineTo(cx + w * 0.12f, h * 0.24f)
        lineTo(cx + w * 0.10f, h * 0.66f)
        lineTo(cx - w * 0.10f, h * 0.66f)
        close()
    }
    drawPath(torso, body)
    drawPath(torso, edge, style = Stroke(2f))
    // legs
    drawLine(edge, Offset(cx - w * 0.05f, h * 0.66f), Offset(cx - w * 0.06f, h * 0.92f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    drawLine(edge, Offset(cx + w * 0.05f, h * 0.66f), Offset(cx + w * 0.06f, h * 0.92f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    drawLine(body, Offset(cx - w * 0.05f, h * 0.66f), Offset(cx - w * 0.06f, h * 0.92f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
    drawLine(body, Offset(cx + w * 0.05f, h * 0.66f), Offset(cx + w * 0.06f, h * 0.92f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
    // arms
    drawLine(edge, Offset(cx - w * 0.12f, h * 0.27f), Offset(cx - w * 0.18f, h * 0.5f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
    drawLine(edge, Offset(cx + w * 0.12f, h * 0.27f), Offset(cx + w * 0.18f, h * 0.5f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
    // digestive system inside
    drawStomach(cx + w * 0.02f, h * 0.40f, kotlin.math.min(w, h) * 0.07f, Color(0xFFEC9DBE), Color(0xFFBE185D), wallLines = false)
    val coil = Path().apply {
        moveTo(cx, h * 0.46f)
        var yy = h * 0.48f; var dir = 1f
        while (yy < h * 0.62f) {
            quadraticTo(cx + dir * w * 0.07f, yy, cx, yy + h * 0.035f)
            yy += h * 0.035f; dir = -dir
        }
    }
    drawPath(coil, Color(0xFFBE185D), style = Stroke(w * 0.012f, cap = StrokeCap.Round))
}
