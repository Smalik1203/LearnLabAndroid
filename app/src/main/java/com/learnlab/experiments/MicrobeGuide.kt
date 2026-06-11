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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class Organism { Amoeba, Paramecium, PondAlga, BreadMould, Mould, SoilAlga, Bacteria }

private data class Card(
    val microbe: Organism,
    val name: String,
    val group: String,
    val description: String,
)

private val POND_CARDS = listOf(
    Card(Organism.Amoeba, "Amoeba", "Protozoa",
        "A single cell with no fixed shape. It keeps changing shape and crawls using finger-like pseudopodia."),
    Card(Organism.Paramecium, "Paramecium", "Protozoa",
        "A single slipper-shaped cell. It swims from place to place using tiny hair-like cilia covering its body."),
    Card(Organism.PondAlga, "Algae", "Alga",
        "A single cell that looks green because of a green pigment (chlorophyll). It moves with the help of specialised whip-like structures."),
)

private val SOIL_CARDS = listOf(
    Card(Organism.BreadMould, "Bread mould", "Fungi",
        "A branched filament without chlorophyll, ending in round sac-like structures that hold spores."),
    Card(Organism.Mould, "Mould", "Fungi",
        "A branched filament without chlorophyll, ending in fine brush-like structures that hold spores."),
    Card(Organism.SoilAlga, "Algae", "Alga",
        "Spherical cells containing chlorophyll — the green pigment that lets them make their own food."),
    Card(Organism.Bacteria, "Bacteria", "Bacteria",
        "The smallest of all. They can be spherical, comma, spiral or rod-shaped, often with one long hair-like structure and many tiny projections around the cell."),
)

private val ALL_CARDS = POND_CARDS + SOIL_CARDS

@Composable
fun MicrobeGuide(controls: ExperimentControls) {
    val t = LL.tokens
    var page by remember { mutableStateOf(0) }
    val totalPages = 9 // 0=intro, 1–3=pond, 4–7=soil, 8=summary

    LaunchedEffect(page) {
        controls.onProgress(page / (totalPages - 1).toFloat())
        if (page == totalPages - 1) controls.onComplete(1f)
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
                "THE INVISIBLE LIVING WORLD",
                color = t.ink500, size = 11.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    0 -> IntroPage()
                    in 1..3 -> OrganismPage(ALL_CARDS[page - 1], "In pond water")
                    in 4..7 -> OrganismPage(ALL_CARDS[page - 1], "In soil suspension")
                    else -> SummaryPage()
                }
            }
            PageNavRow(page, totalPages) { page = it }
        }
    }
}

@Composable
private fun IntroPage() {
    val t = LL.tokens
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .weight(2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            Canvas(Modifier.fillMaxSize()) { drawIntroCollage() }
        }
        Column(
            Modifier.weight(3f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LLText("What are microorganisms?", color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
            LLText(
                "Microorganisms — or microbes — are living things too small to see with the naked eye. " +
                    "A single drop of pond water or soil suspension can hold many of them. " +
                    "Under a microscope they come alive: some move, some are green, some are just threads.",
                color = t.ink200, size = 14.sp, lineHeight = 22.sp,
            )
            Spacer(Modifier.height(4.dp))
            LLText("In this guide", color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "3 microbes found in pond water",
                    "4 microbes found in soil",
                    "Summary tables with key traits",
                ).forEach { line ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        LLText("•", color = t.accent500, size = 16.sp, weight = FontWeight.Bold)
                        LLText(line, color = t.ink200, size = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            LLText("Use the arrows below to explore each microbe.", color = t.ink500, size = 12.sp)
        }
    }
}

@Composable
private fun OrganismPage(card: Card, sectionLabel: String) {
    val t = LL.tokens
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .weight(2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            Canvas(Modifier.fillMaxSize().padding(16.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = minOf(size.width, size.height) * 0.38f
                when (card.microbe) {
                    Organism.Amoeba -> drawAmoebaGlyph(c, r)
                    Organism.Paramecium -> drawParameciumGlyph(c, r)
                    Organism.PondAlga -> drawGreenAlgaGlyph(c, r, flagellated = true)
                    Organism.BreadMould -> drawBreadMouldGlyph(c, r)
                    Organism.Mould -> drawMouldGlyph(c, r)
                    Organism.SoilAlga -> drawGreenAlgaGlyph(c, r, flagellated = false)
                    Organism.Bacteria -> drawBacteriaGlyph(c, r)
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(t.surface.copy(alpha = 0.9f))
                    .border(1.dp, t.line, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                LLText(sectionLabel, color = t.ink400, size = 10.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            }
        }
        Column(
            Modifier.weight(3f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText(card.name, color = t.ink50, size = 24.sp, weight = FontWeight.Bold)
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(t.accent50)
                        .border(1.dp, t.accent500.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    LLText(card.group, color = t.accent700, size = 12.sp, weight = FontWeight.SemiBold)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            LLText(card.description, color = t.ink200, size = 15.sp, lineHeight = 24.sp)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryPage() {
    val t = LL.tokens
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LLText("Summary", color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
        Row(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MicrobeTable(
                caption = "Table 2.1 — Organisms in pond water",
                rows = POND_CARDS,
                modifier = Modifier.weight(1f),
            )
            MicrobeTable(
                caption = "Table 2.2 — Organisms in soil suspension",
                rows = SOIL_CARDS,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PageNavRow(page: Int, total: Int, onPage: (Int) -> Unit) {
    val t = LL.tokens
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val canPrev = page > 0
        val canNext = page < total - 1
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (canPrev) t.accent500 else t.surface2)
                .border(1.dp, if (canPrev) t.accent500 else t.line, RoundedCornerShape(8.dp))
                .then(if (canPrev) Modifier.clickable { onPage(page - 1) } else Modifier)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            LLText("←", color = if (canPrev) Color.White else t.ink500, size = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                Box(
                    Modifier
                        .size(if (i == page) 10.dp else 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (i == page) t.accent500 else t.line),
                )
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (canNext) t.accent500 else t.surface2)
                .border(1.dp, if (canNext) t.accent500 else t.line, RoundedCornerShape(8.dp))
                .then(if (canNext) Modifier.clickable { onPage(page + 1) } else Modifier)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            LLText("→", color = if (canNext) Color.White else t.ink500, size = 16.sp)
        }
    }
}

@Composable
private fun MicrobeTable(caption: String, rows: List<Card>, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, t.line, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(t.surface2)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            LLText("#", color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold,
                modifier = Modifier.width(28.dp))
            LLText("ORGANISM", color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp, modifier = Modifier.weight(3f))
            LLText("WHAT IT LOOKS LIKE", color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp, modifier = Modifier.weight(6f))
        }
        rows.forEachIndexed { i, card ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(if (i % 2 == 0) t.surface else t.surface2.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LLText("${i + 1}", color = t.ink400, size = 13.sp, modifier = Modifier.width(28.dp))
                Column(modifier = Modifier.weight(3f)) {
                    LLText(card.name, color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold)
                    LLText(card.group, color = t.ink500, size = 11.sp)
                }
                LLText(card.description, color = t.ink200, size = 12.sp, lineHeight = 16.sp,
                    modifier = Modifier.weight(6f))
            }
        }
        LLText(caption, color = t.ink500, size = 11.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}

// ───────────────────────── intro collage ─────────────────────────

private fun DrawScope.drawIntroCollage() {
    val w = size.width
    val h = size.height
    val r = minOf(w, h) * 0.13f
    drawAmoebaGlyph(Offset(w * 0.28f, h * 0.25f), r)
    drawGreenAlgaGlyph(Offset(w * 0.72f, h * 0.25f), r * 0.9f, flagellated = true)
    drawParameciumGlyph(Offset(w * 0.50f, h * 0.52f), r * 1.05f)
    drawBacteriaGlyph(Offset(w * 0.28f, h * 0.78f), r * 0.75f)
    drawBreadMouldGlyph(Offset(w * 0.72f, h * 0.78f), r * 0.80f)
}

// ───────────────────────── organism glyphs ─────────────────────────

private fun DrawScope.drawAmoebaGlyph(c: Offset, r: Float) {
    val body = Color(0xFF38BDF8).copy(alpha = 0.30f)
    val edge = Color(0xFF0369A1)
    val lobes = 8
    val path = Path()
    for (i in 0..lobes) {
        val a = i * (2f * PI.toFloat() / lobes)
        val pseudo = 1f + 0.30f * sin(i * 1.9f)
        val px = c.x + cos(a) * r * 1.4f * pseudo
        val py = c.y + sin(a) * r * 1.4f * pseudo
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, body)
    drawPath(path, edge, style = Stroke(2f))
    drawCircle(Color(0xFF7C3AED).copy(alpha = 0.6f), r * 0.38f, Offset(c.x - r * 0.1f, c.y))
    drawCircle(Color(0xFF4C1D95), r * 0.38f, Offset(c.x - r * 0.1f, c.y), style = Stroke(1.2f))
}

private fun DrawScope.drawParameciumGlyph(c: Offset, r: Float) {
    val body = Color(0xFF65A30D).copy(alpha = 0.28f)
    val edge = Color(0xFF3F6212)
    val path = Path().apply {
        moveTo(c.x - r * 1.7f, c.y)
        cubicTo(c.x - r * 1.4f, c.y - r * 1.1f, c.x + r * 0.6f, c.y - r * 1.0f, c.x + r * 1.7f, c.y - r * 0.35f)
        cubicTo(c.x + r * 2.0f, c.y - r * 0.1f, c.x + r * 2.0f, c.y + r * 0.3f, c.x + r * 1.5f, c.y + r * 0.7f)
        cubicTo(c.x + r * 0.4f, c.y + r * 1.1f, c.x - r * 1.4f, c.y + r * 1.0f, c.x - r * 1.7f, c.y)
        close()
    }
    drawPath(path, body)
    drawPath(path, edge, style = Stroke(2f))
    drawLine(edge.copy(alpha = 0.7f), Offset(c.x - r * 0.2f, c.y - r * 0.2f),
        Offset(c.x + r * 0.8f, c.y + r * 0.3f), strokeWidth = 1.5f)
    val n = 22
    for (i in 0 until n) {
        val a = i * (2f * PI.toFloat() / n)
        val ex = c.x + cos(a) * r * 1.7f
        val ey = c.y + sin(a) * r * 0.95f
        drawLine(edge.copy(alpha = 0.6f), Offset(ex, ey),
            Offset(ex + cos(a) * r * 0.3f, ey + sin(a) * r * 0.3f), strokeWidth = 1f)
    }
    drawCircle(Color(0xFF365314).copy(alpha = 0.5f), r * 0.3f, c)
}

private fun DrawScope.drawGreenAlgaGlyph(c: Offset, r: Float, flagellated: Boolean) {
    val brush = Brush.radialGradient(
        colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF15803D)),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.5f,
    )
    drawCircle(brush, r * 1.1f, c)
    drawCircle(Color(0xFF14532D), r * 1.1f, c, style = Stroke(2f))
    val cup = Path().apply {
        addArc(
            androidx.compose.ui.geometry.Rect(c.x - r * 0.7f, c.y - r * 0.5f, c.x + r * 0.7f, c.y + r * 0.8f),
            20f, 140f,
        )
    }
    drawPath(cup, Color(0xFF14532D).copy(alpha = 0.6f), style = Stroke(r * 0.25f, cap = StrokeCap.Round))
    if (flagellated) {
        for (s in listOf(-1f, 1f)) {
            val tail = Path().apply {
                moveTo(c.x + s * r * 0.4f, c.y - r * 1.0f)
                cubicTo(c.x + s * r * 1.2f, c.y - r * 1.8f,
                    c.x + s * r * 0.6f, c.y - r * 2.2f,
                    c.x + s * r * 1.4f, c.y - r * 2.6f)
            }
            drawPath(tail, Color(0xFF14532D), style = Stroke(1.6f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawBreadMouldGlyph(c: Offset, r: Float) {
    val filament = Color(0xFF92400E)
    val baseY = c.y + r * 1.3f
    drawLine(filament, Offset(c.x - r * 2.0f, baseY), Offset(c.x + r * 2.0f, baseY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    val stalkX = listOf(-1.3f, 0f, 1.3f)
    for (sx in stalkX) {
        val x = c.x + sx * r
        drawLine(filament, Offset(x, baseY), Offset(x, c.y - r * 0.8f), strokeWidth = 2f)
        drawCircle(Color(0xFF6B3F18), r * 0.5f, Offset(x, c.y - r * 1.1f))
        drawCircle(Color(0xFF3F2410), r * 0.5f, Offset(x, c.y - r * 1.1f), style = Stroke(1.2f))
        drawCircle(Color(0xFFFDE68A).copy(alpha = 0.7f), 1.6f, Offset(x - r * 0.15f, c.y - r * 1.2f))
        drawCircle(Color(0xFFFDE68A).copy(alpha = 0.7f), 1.6f, Offset(x + r * 0.15f, c.y - r * 1.05f))
    }
    for (s in listOf(-1f, 1f)) {
        drawLine(filament.copy(alpha = 0.7f), Offset(c.x + s * r * 1.3f, baseY),
            Offset(c.x + s * r * 1.7f, baseY + r * 0.5f), strokeWidth = 1.4f)
    }
}

private fun DrawScope.drawMouldGlyph(c: Offset, r: Float) {
    val filament = Color(0xFF115E59)
    val baseY = c.y + r * 1.3f
    drawLine(filament, Offset(c.x - r * 2.0f, baseY), Offset(c.x + r * 2.0f, baseY),
        strokeWidth = 2.5f, cap = StrokeCap.Round)
    val stalkX = listOf(-1.2f, 0f, 1.2f)
    for (sx in stalkX) {
        val x = c.x + sx * r
        val topY = c.y - r * 0.6f
        drawLine(filament, Offset(x, baseY), Offset(x, topY), strokeWidth = 2f)
        for (b in -2..2) {
            drawLine(Color(0xFF0F766E), Offset(x, topY),
                Offset(x + b * r * 0.18f, topY - r * 0.7f), strokeWidth = 1.4f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF99F6E4), 1.6f, Offset(x + b * r * 0.18f, topY - r * 0.75f))
        }
    }
}

private fun DrawScope.drawBacteriaGlyph(c: Offset, r: Float) {
    val fill = Color(0xFFB45309).copy(alpha = 0.85f)
    val edge = Color(0xFF7C2D12)
    drawCircle(fill, r * 0.4f, Offset(c.x - r * 1.6f, c.y - r * 1.0f))
    drawCircle(edge, r * 0.4f, Offset(c.x - r * 1.6f, c.y - r * 1.0f), style = Stroke(1f))
    val comma = Path().apply {
        moveTo(c.x + r * 0.9f, c.y - r * 1.4f)
        cubicTo(c.x + r * 1.8f, c.y - r * 1.2f, c.x + r * 1.8f, c.y - r * 0.4f, c.x + r * 1.1f, c.y - r * 0.5f)
    }
    drawPath(comma, fill, style = Stroke(r * 0.34f, cap = StrokeCap.Round))
    val spiral = Path().apply {
        moveTo(c.x - r * 2.0f, c.y + r * 0.6f)
        var x = -2.0f
        while (x < 0.2f) {
            val px = c.x + x * r
            val py = c.y + r * 0.6f + sin(x * 6f) * r * 0.4f
            lineTo(px, py)
            x += 0.12f
        }
    }
    drawPath(spiral, fill, style = Stroke(r * 0.3f, cap = StrokeCap.Round))
    val rodC = Offset(c.x + r * 1.0f, c.y + r * 1.1f)
    drawLine(fill, Offset(rodC.x - r * 0.9f, rodC.y), Offset(rodC.x + r * 0.9f, rodC.y),
        strokeWidth = r * 0.7f, cap = StrokeCap.Round)
    val flag = Path().apply {
        moveTo(rodC.x + r * 0.9f, rodC.y)
        cubicTo(rodC.x + r * 1.6f, rodC.y - r * 0.3f, rodC.x + r * 1.6f, rodC.y + r * 0.5f,
            rodC.x + r * 2.3f, rodC.y + r * 0.2f)
    }
    drawPath(flag, edge, style = Stroke(1.4f, cap = StrokeCap.Round))
    for (i in 0 until 8) {
        val sx = rodC.x - r * 0.8f + i * (r * 0.22f)
        drawLine(edge.copy(alpha = 0.7f), Offset(sx, rodC.y - r * 0.35f),
            Offset(sx, rodC.y - r * 0.6f), strokeWidth = 1f)
        drawLine(edge.copy(alpha = 0.7f), Offset(sx, rodC.y + r * 0.35f),
            Offset(sx, rodC.y + r * 0.6f), strokeWidth = 1f)
    }
}
