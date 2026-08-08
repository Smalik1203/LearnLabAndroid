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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.experiments.ch06kit.Badge
import com.learnlab.experiments.ch06kit.drawAmoebaGlyph
import com.learnlab.experiments.ch06kit.drawBacteriaGlyph
import com.learnlab.experiments.ch06kit.drawBreadMouldGlyph
import com.learnlab.experiments.ch06kit.drawGreenAlgaGlyph
import com.learnlab.experiments.ch06kit.drawLactobacillusGlyph
import com.learnlab.experiments.ch06kit.drawMouldGlyph
import com.learnlab.experiments.ch06kit.drawParameciumGlyph
import com.learnlab.experiments.ch06kit.drawRhizobiumGlyph
import com.learnlab.experiments.ch06kit.drawVirusGlyph
import com.learnlab.experiments.ch06kit.drawYeastGlyph
import com.learnlab.store.ExperimentControls

/**
 * Simulation 7 — Microorganism classification explorer. Filter organisms by category, tap a card
 * for a full detail panel, or switch to a comparison-table view. Covers Tables 2.1 & 2.2 plus the
 * yeast, Lactobacillus, Rhizobium and the virus mentioned across the chapter.
 */

private enum class Cat(val label: String, val color: Color) {
    Protozoa("Protozoa", Color(0xFF0EA5E9)),
    Bacteria("Bacteria", Color(0xFFB45309)),
    Fungi("Fungi", Color(0xFFA855F7)),
    Algae("Algae", Color(0xFF22C55E)),
    Virus("Virus", Color(0xFFE11D48)),
}

private data class MicrobeInfo(
    val name: String,
    val cat: Cat,
    val cellCount: String,
    val features: String,
    val whereFound: String,
    val movement: String,
    val benefit: String,
    val detail: String,
    val draw: DrawScope.(Offset, Float) -> Unit,
)

private val MICROBES = listOf(
    MicrobeInfo("Amoeba", Cat.Protozoa, "Unicellular", "Single cell, no fixed shape", "Pond water, soil, intestine",
        "Pseudopods (false feet)", "Some harmful",
        "Amoeba has no fixed shape. It moves by extending part of its cytoplasm — a pseudopod — and engulfs food by surrounding it. Found in pond water, soil, and even the human intestine.",
        { c, r -> drawAmoebaGlyph(c, r) }),
    MicrobeInfo("Paramecium", Cat.Protozoa, "Unicellular", "Slipper-shaped, covered in cilia", "Pond water",
        "Cilia (beating hairs)", "Harmless",
        "Paramecium is slipper-shaped and covered in thousands of cilia — tiny hair-like structures it beats in waves to move and to sweep food into its oral groove.",
        { c, r -> drawParameciumGlyph(c, r) }),
    MicrobeInfo("Algae (pond)", Cat.Algae, "Unicellular", "Green, has chlorophyll", "Pond water",
        "Flagella", "Beneficial",
        "Algae are plant-like organisms that contain chlorophyll and photosynthesise, making their own food from sunlight. They are primary producers and make over half of Earth's oxygen.",
        { c, r -> drawGreenAlgaGlyph(c, r, flagellated = true) }),
    MicrobeInfo("Algae (soil)", Cat.Algae, "Unicellular", "Spherical, has chlorophyll", "Soil suspension",
        "Non-motile", "Beneficial",
        "In soil, algae appear as spherical green cells. The green pigment chlorophyll lets them make their own food, enriching the soil with organic matter and oxygen.",
        { c, r -> drawGreenAlgaGlyph(c, r, flagellated = false) }),
    MicrobeInfo("Bread mould", Cat.Fungi, "Multicellular", "Branched filament, sac-like tips", "Soil, bread",
        "Non-motile", "Mostly beneficial",
        "Bread mould (Rhizopus) grows as a network of branching filaments called hyphae. The sac-like tips are sporangia, which hold spores that spread through the air to new food.",
        { c, r -> drawBreadMouldGlyph(c, r) }),
    MicrobeInfo("Mould (Penicillium)", Cat.Fungi, "Multicellular", "Branched filament, brush-like tips", "Soil",
        "Non-motile", "Beneficial",
        "This mould (Penicillium) has brush-like spore-bearing structures. Alexander Fleming noticed a Penicillium mould killing bacteria on a dish — leading to the discovery of penicillin.",
        { c, r -> drawMouldGlyph(c, r) }),
    MicrobeInfo("Yeast", Cat.Fungi, "Unicellular", "Oval cell, reproduces by budding", "Food (dough, batter)",
        "Non-motile", "Beneficial",
        "Yeast is a unicellular fungus that reproduces by budding — a small bud grows on the parent cell and separates. It ferments sugar into CO₂ and alcohol, and is used in baking.",
        { c, r -> drawYeastGlyph(c, r) }),
    MicrobeInfo("Bacteria", Cat.Bacteria, "Unicellular", "Spherical, comma, spiral or rod", "Soil, water, air, body",
        "Flagella", "Mostly beneficial",
        "Bacteria are prokaryotes — they have no well-defined nucleus, only a nucleoid region holding their DNA. The most abundant organisms on Earth: most are beneficial decomposers, nitrogen fixers and gut flora; a few cause disease.",
        { c, r -> drawBacteriaGlyph(c, r) }),
    MicrobeInfo("Lactobacillus", Cat.Bacteria, "Unicellular", "Rod-shaped, forms chains", "Curd, gut",
        "Non-motile", "Beneficial",
        "Lactobacillus ferments lactose (milk sugar) into lactic acid, which coagulates milk protein to form curd. Used to make yoghurt, cheese and idli/dosa batter, and lives in the human gut.",
        { c, r -> drawLactobacillusGlyph(c, r) }),
    MicrobeInfo("Rhizobium", Cat.Bacteria, "Unicellular", "Rod-shaped, lives in root nodules", "Root nodules of legumes",
        "Non-motile", "Beneficial",
        "Rhizobium lives symbiotically in the root nodules of legumes (beans, peas, lentils). It traps nitrogen from the air and converts it into a form plants can use — naturally enriching the soil.",
        { c, r -> drawRhizobiumGlyph(c, r) }),
    MicrobeInfo("Virus", Cat.Virus, "Acellular", "Genetic material in a protein coat", "Inside a host cell",
        "Non-motile", "Often harmful",
        "Viruses are not cells. They are only genetic material (DNA or RNA) inside a protein coat. They reproduce only by entering a living cell and hijacking its machinery — which sets them apart from all other microorganisms.",
        { c, r -> drawVirusGlyph(c, r) }),
)

@Composable
fun MicrobeGuide(controls: ExperimentControls) {
    val t = LL.tokens
    var tab by remember { mutableStateOf(0) }
    var filter by remember { mutableStateOf<Cat?>(null) }
    var selected by remember { mutableStateOf(MICROBES.first()) }
    var opened by remember { mutableStateOf(setOf(MICROBES.first().name)) }
    var tableSeen by remember { mutableStateOf(false) }

    LaunchedEffect(opened, tableSeen) {
        val p = (opened.size.coerceAtMost(4) / 4f) * 0.7f + (if (tableSeen) 0.3f else 0f)
        controls.onProgress(p.coerceIn(0f, 1f))
        if (tableSeen && opened.size >= 4) controls.onComplete(1f)
    }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                LLText("MICROORGANISM CLASSIFICATION EXPLORER", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                PillTabBar(
                    tabs = listOf("Cards", "Table"),
                    selected = tab,
                    onSelect = { tab = it; if (it == 1) tableSeen = true },
                    modifier = Modifier.width(200.dp),
                )
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                if (tab == 0) {
                    GridTab(filter, { filter = it }, selected, { selected = it; opened = opened + it.name })
                } else {
                    TableTab()
                }
            }
        }
    }
}

@Composable
private fun GridTab(filter: Cat?, onFilter: (Cat?) -> Unit, selected: MicrobeInfo, onSelect: (MicrobeInfo) -> Unit) {
    val t = LL.tokens
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // filter rail
        Column(
            modifier = Modifier.width(120.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterButton("All", null, filter == null, onFilter)
            Cat.entries.forEach { c -> FilterButton(c.label, c, filter == c, onFilter) }
        }
        // card grid
        val shown = MICROBES.filter { filter == null || it.cat == filter }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shown.chunked(2).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { m ->
                        MicrobeCard(m, m.name == selected.name, Modifier.weight(1f)) { onSelect(m) }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        // detail panel
        DetailPanel(selected, Modifier.width(300.dp).fillMaxHeight())
    }
}

@Composable
private fun FilterButton(label: String, cat: Cat?, selected: Boolean, onFilter: (Cat?) -> Unit) {
    val t = LL.tokens
    val tint = cat?.color ?: t.accent500
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(if (selected) tint.copy(alpha = 0.15f) else t.surface2)
            .border(1.dp, if (selected) tint else t.line, RoundedCornerShape(8.dp))
            .clickable { onFilter(cat) }.padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(tint))
            LLText(label, color = if (selected) tint else t.ink400, size = 12.sp, weight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MicrobeCard(m: MicrobeInfo, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = modifier.height(116.dp).clip(RoundedCornerShape(12.dp)).background(t.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) m.cat.color else t.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp)).background(t.surface2)) {
            Canvas(Modifier.fillMaxSize().padding(6.dp)) {
                m.draw(this, Offset(size.width / 2f, size.height / 2f), kotlin.math.min(size.width, size.height) * 0.26f)
            }
        }
        LLText(m.name, color = t.ink50, size = 11.sp, weight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun DetailPanel(m: MicrobeInfo, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(t.bgDeep)
            .border(1.dp, t.lineStrong, RoundedCornerShape(14.dp)).padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(m.name, color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
        Badge(m.cat.label, tint = m.cat.color)
        Box(Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(10.dp)).background(t.surface)) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                m.draw(this, Offset(size.width / 2f, size.height / 2f), kotlin.math.min(size.width, size.height) * 0.26f)
            }
        }
        DetailRow("Cell count", m.cellCount)
        DetailRow("Key features", m.features)
        DetailRow("Where found", m.whereFound)
        DetailRow("Movement", m.movement)
        DetailRow("Beneficial / harmful", m.benefit)
        LLText(m.detail, color = t.ink200, size = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val t = LL.tokens
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        LLText(label.uppercase(), color = t.ink500, size = 9.5.sp, weight = FontWeight.SemiBold, letterSpacing = 1.sp)
        LLText(value, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun TableTab() {
    val t = LL.tokens
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).border(1.dp, t.line, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF9F1239)).padding(horizontal = 10.dp, vertical = 9.dp)) {
            THead("Organism", 2f); THead("Category", 1.4f); THead("Cell count", 1.4f)
            THead("Movement", 1.6f); THead("Where found", 2f); THead("Benefit?", 1.4f)
        }
        MICROBES.forEachIndexed { i, m ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(if (i % 2 == 0) t.surface else t.surface2.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(m.cat.color))
                    LLText(m.name, color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold)
                }
                TCell(m.cat.label, 1.4f); TCell(m.cellCount, 1.4f)
                TCell(m.movement, 1.6f); TCell(m.whereFound, 2f); TCell(m.benefit, 1.4f)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.THead(text: String, weight: Float) {
    LLText(text.uppercase(), color = Color.White, size = 10.sp, weight = FontWeight.Bold, letterSpacing = 0.8.sp,
        modifier = Modifier.weight(weight))
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TCell(text: String, weight: Float) {
    LLText(text, color = LL.tokens.ink200, size = 11.sp, lineHeight = 14.sp, modifier = Modifier.weight(weight))
}
