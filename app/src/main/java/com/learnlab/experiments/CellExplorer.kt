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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.experiments.ch06kit.ActivityCard
import com.learnlab.experiments.ch06kit.Badge
import com.learnlab.experiments.ch06kit.CellType
import com.learnlab.experiments.ch06kit.ContextCard
import com.learnlab.experiments.ch06kit.InfoCard
import com.learnlab.experiments.ch06kit.MicroscopeCircle
import com.learnlab.experiments.ch06kit.NoteBox
import com.learnlab.experiments.ch06kit.NoteTone
import com.learnlab.experiments.ch06kit.QuoteCard
import com.learnlab.experiments.ch06kit.RevealCell
import com.learnlab.experiments.ch06kit.SlideDeck
import com.learnlab.experiments.ch06kit.TemplateA
import com.learnlab.experiments.ch06kit.TemplateB
import com.learnlab.experiments.ch06kit.drawCheekField
import com.learnlab.experiments.ch06kit.drawLabelledCell
import com.learnlab.experiments.ch06kit.drawOnionField
import com.learnlab.store.ExperimentControls

/**
 * Simulation 2 — Activities 2.2 & 2.3: the onion peel (plant cell) and cheek cell (animal cell).
 * Paired virtual labs: prepare each slide, observe both side by side, compare, then meet the full
 * cell structure (Fig 2.5).
 */

private data class CellStructure(
    val id: String, val name: String, val function: String,
    val inOnion: Boolean, val inCheek: Boolean,
)

private val STRUCTURES = listOf(
    CellStructure("wall", "Cell wall",
        "Rigid outer layer. Present only in plant cells. Provides shape and strength; made of cellulose.",
        inOnion = true, inCheek = false),
    CellStructure("membrane", "Cell membrane",
        "Present in all cells. Separates the cell from its surroundings. Porous — lets useful materials in and waste out.",
        inOnion = true, inCheek = true),
    CellStructure("nucleus", "Nucleus",
        "Controls all the activities of the cell and regulates its growth.",
        inOnion = true, inCheek = true),
    CellStructure("cytoplasm", "Cytoplasm",
        "Fills the space between membrane and nucleus. Holds the cell's components; most life processes happen here.",
        inOnion = true, inCheek = true),
)

@Composable
fun CellExplorer(controls: ExperimentControls) {
    SlideDeck(
        controls = controls,
        slideCount = 7,
        eyebrow = "ACTIVITIES 2.2 & 2.3 · ONION & CHEEK CELL",
        topics = listOf(
            "Overview", "Why we stain", "Activity 2.2 · onion", "Activity 2.3 · cheek",
            "Observe both", "Record", "Cell structure",
        ),
    ) { page ->
        when (page) {
            0 -> OpenerSlide()
            1 -> WhyStainSlide()
            2 -> OnionProcedureSlide()
            3 -> CheekProcedureSlide()
            4 -> ObservationSlide()
            5 -> RecordSlide()
            else -> StructureSlide()
        }
    }
}

// ── Slide 1: opener ─────────────────────────────────────────────────

@Composable
private fun OpenerSlide() {
    TemplateA(
        badge = "SECTION 2.1 · 25 MIN",
        title = "Looking inside a cell for the first time",
        lead = "All living beings are made up of cells. In the next two activities you will prepare your " +
            "own microscope slides — one from an onion from any kitchen, one from a gentle scraping of " +
            "the inside of your own cheek — and observe the most fundamental unit of life directly.",
    ) {
        QuoteCard(
            text = "You will observe nearly rectangular structures under the microscope. These are the " +
                "cells of the onion peel, which are closely arranged without any space between them.",
            attribution = "Curiosity, Grade 8, Chapter 2",
        )
        ContextCard(
            label = "Two experiments, one question",
            body = "Activity 2.2 uses an onion — a plant. Activity 2.3 uses cells from the lining of your " +
                "mouth — an animal. Afterwards the textbook asks you to compare them. Before you begin: do " +
                "you think plant and animal cells will look the same, or different?",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "What you will discover",
                body = "Why plant and animal cells share some parts but differ in others — and what a " +
                    "coloured stain reveals.",
                modifier = Modifier.weight(1f),
            )
            InfoCard(
                title = "Key vocabulary",
                body = "Cell wall · Cell membrane · Nucleus · Cytoplasm · Safranin · Methylene blue",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Slide 2: why stain ──────────────────────────────────────────────

@Composable
private fun WhyStainSlide() {
    val t = LL.tokens
    TemplateA(
        badge = "SECTION 2.1 · UNDERSTAND FIRST",
        title = "Why do we add a coloured stain before observing?",
        lead = "Cells are nearly transparent. Without preparation, their internal structures are almost " +
            "impossible to tell apart — everything looks equally pale. A stain binds to specific parts of " +
            "the cell and makes them visible by adding colour.",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StainPanel("Without stain", "Almost invisible", stained = false, modifier = Modifier.weight(1f))
            StainPanel("With stain", "Structures appear", stained = true, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                title = "Safranin (Activity 2.2)",
                body = "A red stain. It colours the onion peel cells pink so the cell walls and outlines " +
                    "stand out clearly.",
                modifier = Modifier.weight(1f),
            )
            InfoCard(
                title = "Methylene blue (Activity 2.3)",
                body = "A blue stain. It makes the nucleus of each cheek cell show up as a dark oval inside " +
                    "the otherwise pale cell.",
                modifier = Modifier.weight(1f),
            )
        }
        NoteBox(
            text = "Staining does not damage or change the cells — it just makes their parts absorb light " +
                "differently so they become visible. Different stains bind to different molecules, which is " +
                "why biologists keep many of them.",
            tone = NoteTone.Info,
            label = "Good to know",
        )
    }
}

@Composable
private fun StainPanel(title: String, caption: String, stained: Boolean, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(title.uppercase(), color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
        Box(Modifier.fillMaxWidth().height(120.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f; val cy = size.height / 2f
                val r = kotlin.math.min(size.width, size.height) * 0.36f
                if (!stained) {
                    drawCircle(Color(0xFFCBD5E1).copy(alpha = 0.30f), r, Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f))
                } else {
                    drawCircle(Color(0xFFFBCFE8).copy(alpha = 0.45f), r, Offset(cx, cy))
                    drawCircle(Color(0xFFDB2777), r, Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.5f))
                    drawCircle(Color(0xFFBE185D), r * 0.32f, Offset(cx + r * 0.1f, cy))
                }
            }
        }
        LLText(caption, color = t.ink400, size = 11.sp, align = TextAlign.Center)
    }
}

// ── Slides 3 & 4: procedures ────────────────────────────────────────

@Composable
private fun OnionProcedureSlide() {
    TemplateB(
        left = {
            ActivityCard(
                code = "ACTIVITY 2.2 · TEACHER DEMONSTRATION",
                title = "Prepare the onion peel slide",
                description = "Patience matters — if the peel folds, tears, or traps air bubbles, the view " +
                    "will be unclear.",
                tags = listOf(
                    "🧅  Thin peel from the inner surface",
                    "🔴  Safranin stain for 30 seconds",
                    "📐  Lower the coverslip at 45° — no bubbles",
                ),
            )
        },
        right = {
            ProcedureStepper(
                listOf(
                    "🧫" to "Stain the peel with safranin (30 s)",
                    "🧅" to "Transfer the peel onto a slide",
                    "💧" to "Add a drop of glycerin",
                    "📐" to "Lower a coverslip at 45°",
                    "🧻" to "Blot the excess glycerin",
                    "🔬" to "Observe under the microscope",
                ),
            )
            LLText(
                "Pull the thin transparent layer from the inner surface of an onion piece — the onion peel. " +
                    "Stain it with safranin for 30 seconds, rinse, mount it flat on a slide in a drop of " +
                    "glycerin (which stops it drying out), then lower a coverslip so no air bubbles get trapped.",
                color = LL.tokens.ink200, size = 12.sp, lineHeight = 18.sp,
            )
            NoteBox(
                text = "Compare what you see with Fig. 2.3c (onion cells) and Fig. 2.3d (a brick wall). Why do " +
                    "you think the textbook makes that comparison?",
                tone = NoteTone.Info, label = "Compare",
            )
        },
    )
}

@Composable
private fun CheekProcedureSlide() {
    TemplateB(
        left = {
            ActivityCard(
                code = "ACTIVITY 2.3 · LET US INVESTIGATE",
                title = "Prepare the cheek cell slide",
                description = "This specimen comes from your own body — cells lining the cheek are shed " +
                    "naturally, and a gentle scrape collects thousands.",
                tags = listOf(
                    "🚰  Rinse your mouth with water first",
                    "🔵  Methylene blue stain",
                    "✏️  Draw what you observe",
                ),
            )
        },
        right = {
            ProcedureStepper(
                listOf(
                    "🚰" to "Rinse your mouth with clean water",
                    "🦷" to "Gently scrape the inside of the cheek",
                    "🔵" to "Spread it in water; add methylene blue",
                    "💧" to "After 1 minute, add glycerin",
                    "📐" to "Place a coverslip; blot the excess",
                    "🔬" to "Observe and draw what you see",
                ),
            )
            NoteBox(
                text = "In Activity 2.2 the stain was safranin (red). Here it is methylene blue. Both are " +
                    "stains that make cells visible — but they bind to different molecules. That is why " +
                    "biologists have dozens of stains, each revealing a different part of the cell.",
                tone = NoteTone.Warn,
                label = "Notice the difference from Activity 2.2",
            )
            LLText(
                "You will see polygon-shaped structures — cheek cells, which form the inner lining of your " +
                    "mouth. They are thin and flat, a shape suited to forming a protective surface layer.",
                color = LL.tokens.ink200, size = 12.sp, lineHeight = 18.sp,
            )
        },
    )
}

@Composable
private fun ProcedureStepper(steps: List<Pair<String, String>>) {
    val t = LL.tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        steps.forEachIndexed { i, (emoji, label) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(t.accent50)
                        .border(1.dp, t.accent500, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    LLText("${i + 1}", color = t.accent700, size = 12.sp, weight = FontWeight.Bold)
                }
                LLText(emoji, size = 18.sp)
                LLText(label, color = t.ink400, size = 10.sp, lineHeight = 13.sp, align = TextAlign.Center)
            }
        }
    }
}

// ── Slide 5: observation ────────────────────────────────────────────

@Composable
private fun ObservationSlide() {
    val t = LL.tokens
    var selected by remember { mutableStateOf<String?>(null) }
    val sel = STRUCTURES.firstOrNull { it.id == selected }

    TemplateB(
        leftWeight = 2f, rightWeight = 3f,
        left = {
            ActivityCard(
                code = "ACTIVITIES 2.2 & 2.3 · OBSERVE",
                title = "What do you see under the microscope?",
                description = "Two views, side by side. Tap a structure below to identify it and see where " +
                    "it appears.",
                tags = listOf(
                    "🔬  ~400× magnification",
                    "👁️  Tap a structure to identify it",
                    "✏️  Note similarities and differences",
                ),
            )
            // detail / hint panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (sel == null) {
                    LLText("STRUCTURE", color = t.ink500, size = 10.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                    LLText("Tap a structure name to highlight it in both views and read what it does.",
                        color = t.ink400, size = 12.sp, lineHeight = 17.sp)
                } else {
                    LLText(sel.name, color = t.ink50, size = 15.sp, weight = FontWeight.Bold)
                    LLText(sel.function, color = t.ink200, size = 12.sp, lineHeight = 17.sp)
                    LLText(
                        "Onion peel: ${if (sel.inOnion) "present" else "absent"}   ·   " +
                            "Cheek cell: ${if (sel.inCheek) "present" else "absent"}",
                        color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold,
                    )
                }
            }
        },
        right = {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MicroscopeView("Onion peel — plant", Modifier.weight(1f)) { c, r ->
                    drawOnionField(highlight = selected)
                }
                MicroscopeView("Cheek cells — animal", Modifier.weight(1f)) { c, r ->
                    drawCheekField(highlight = if (selected == "wall") null else selected)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                STRUCTURES.forEach { s ->
                    StructureChip(s.name, s.id == selected, Modifier.weight(1f)) {
                        selected = if (selected == s.id) null else s.id
                    }
                }
            }
        },
    )
}

@Composable
private fun MicroscopeView(
    caption: String,
    modifier: Modifier = Modifier,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.(Offset, Float) -> Unit,
) {
    val t = LL.tokens
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MicroscopeCircle(modifier = Modifier.fillMaxWidth().weight(1f), draw = draw)
        LLText(caption, color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold,
            align = TextAlign.Center)
    }
}

@Composable
private fun StructureChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) t.accent50 else t.surface2)
            .border(1.dp, if (selected) t.accent500 else t.line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = if (selected) t.accent700 else t.ink400, size = 11.sp,
            weight = FontWeight.SemiBold, align = TextAlign.Center)
    }
}

// ── Slide 6: record / compare ───────────────────────────────────────

@Composable
private fun RecordSlide() {
    val t = LL.tokens
    data class Row5(val structure: String, val onion: String, val cheek: String, val function: String)
    val rows = listOf(
        Row5("Cell wall", "✓ present", "✗ absent", "Rigid outer layer — shape & strength (plant only)"),
        Row5("Cell membrane", "✓ present", "✓ present", "Separates the cell from its surroundings; porous"),
        Row5("Nucleus", "✓ present", "✓ present", "Controls the cell's activities"),
        Row5("Cytoplasm", "✓ present", "✓ present", "Holds the cell's parts; most life processes happen here"),
        Row5("Chloroplasts", "✗ not seen", "✗ not seen", "Not visible in either mount"),
    )
    TemplateA(
        badge = "ACTIVITIES 2.2 & 2.3 · RECORD",
        title = "Compare the two cells",
        lead = "Predict each cell, then tap to reveal. Think about each row before you peek.",
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(t.surface2).padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                TableHead("Structure", Modifier.weight(2f))
                TableHead("Onion peel?", Modifier.weight(1.6f))
                TableHead("Cheek cell?", Modifier.weight(1.6f))
                TableHead("Function", Modifier.weight(3f))
            }
            rows.forEachIndexed { i, r ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(if (i % 2 == 0) t.surface else t.surface2.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(r.structure, color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold,
                        modifier = Modifier.weight(2f))
                    RevealCell(r.onion, Modifier.weight(1.6f).padding(horizontal = 3.dp))
                    RevealCell(r.cheek, Modifier.weight(1.6f).padding(horizontal = 3.dp))
                    RevealCell(r.function, Modifier.weight(3f).padding(horizontal = 3.dp))
                }
            }
        }
        ContextCard(
            label = "Discuss",
            body = "The textbook compares Fig. 2.3c (onion cells) with Fig. 2.3d (a brick wall). In what way " +
                "do onion peel cells resemble a brick wall? What does that tell you about how cells are " +
                "arranged in plant tissue?",
        )
    }
}

@Composable
private fun TableHead(text: String, modifier: Modifier = Modifier) {
    LLText(text.uppercase(), color = LL.tokens.ink500, size = 10.sp,
        weight = FontWeight.SemiBold, letterSpacing = 1.2.sp, modifier = modifier)
}

// ── Slide 7: full cell structure (Fig 2.5) ──────────────────────────

@Composable
private fun StructureSlide() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    TemplateA(
        badge = "SECTION 2.1 · THE BIGGER PICTURE",
        title = "A cell is not a simple bag of liquid",
        lead = "Your two mounts showed only the basic parts — wall, membrane, nucleus, cytoplasm. The full " +
            "picture is richer: plant cells also have chloroplasts and a large central vacuole; animal cells " +
            "have many small vacuoles and centrioles.",
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SchematicCell("Animal cell", CellType.Animal, tm, Modifier.weight(1f))
            SchematicCell("Plant cell", CellType.Plant, tm, Modifier.weight(1f))
        }
        NoteBox(
            text = "A cell is a complex structure made of many parts, each with its own job — together they " +
                "let the cell, and the whole organism, work. (Fig. 2.5)",
            tone = NoteTone.Info,
            label = "Conclusion",
        )
    }
}

@Composable
private fun SchematicCell(
    title: String,
    type: CellType,
    tm: androidx.compose.ui.text.TextMeasurer,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(title.uppercase(), color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                drawLabelledCell(tm, type, labelColor = t.ink400, radiusFrac = 0.27f)
            }
        }
    }
}
