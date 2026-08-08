package com.learnlab.experiments.ch06kit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Shared cell / organelle Canvas drawers for the Grade 8 Ch.2 "Invisible Living World"
 * simulations. Extracted from the original CellExplorer so several simulations (cell
 * observation, levels of organisation, …) can share the same accurate NCERT-style art.
 *
 * Everything here is plain DrawScope work + data; it depends only on Compose graphics.
 */

// ── Data model ──────────────────────────────────────────────────────

internal enum class CellType { Plant, Animal }

internal enum class AnimKind { Pulse, AtpSparks, LightRays, DnaHelix, ProteinChain, VesiclePinch, FluidLevel, Membrane }

internal data class Vec3(val x: Float, val y: Float, val z: Float)

internal data class OrganelleKind(
    val id: String,
    val name: String,
    val color: Color,
    val radiusUnit: Float,            // 0..1, fraction of cellR
    val shape: OrganelleShape,
    val role: String,                 // one-liner
    val presentInPlant: Boolean,
    val presentInAnimal: Boolean,
    val animKind: AnimKind,
    val detail: String,               // 3–4 line description
)

internal enum class OrganelleShape { Circle, Oval, Disk, Squiggle, RoundedRect, PairedRods }

internal data class OrganelleInstance(val kindId: String, val pos: Vec3)

internal val KINDS: List<OrganelleKind> = listOf(
    OrganelleKind(
        id = "nucleus", name = "Nucleus", color = Color(0xFF7C3AED),
        radiusUnit = 0.22f, shape = OrganelleShape.Circle,
        role = "Control centre — stores DNA, directs all cell activity.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.DnaHelix,
        detail = "The nucleus controls all the activities of the cell and regulates its growth. " +
            "It holds the cell's DNA and is itself covered by a thin membrane. " +
            "It is usually the most visible structure once a cell is stained.",
    ),
    OrganelleKind(
        id = "mitochondrion", name = "Mitochondrion", color = Color(0xFFEF4444),
        radiusUnit = 0.11f, shape = OrganelleShape.Oval,
        role = "Power-house — releases energy for the cell.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.AtpSparks,
        detail = "Mitochondria release energy from food, which the cell uses to grow and work. " +
            "Active cells like muscle have many mitochondria; resting cells have fewer.",
    ),
    OrganelleKind(
        id = "chloroplast", name = "Chloroplast", color = Color(0xFF10B981),
        radiusUnit = 0.12f, shape = OrganelleShape.Oval,
        role = "Makes food using sunlight (photosynthesis).",
        presentInPlant = true, presentInAnimal = false,
        animKind = AnimKind.LightRays,
        detail = "Chloroplasts are plastids that contain the green pigment chlorophyll. " +
            "They capture sunlight and use it to make glucose. " +
            "Only plant cells (and some algae) have them — which is why plant cells can look green.",
    ),
    OrganelleKind(
        id = "ribosome", name = "Ribosome", color = Color(0xFFFCD34D),
        radiusUnit = 0.04f, shape = OrganelleShape.Circle,
        role = "Protein factory — builds the cell's proteins.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.ProteinChain,
        detail = "Ribosomes are tiny structures that link amino acids together to build proteins. " +
            "Some float free in the cytoplasm; others sit on the endoplasmic reticulum.",
    ),
    OrganelleKind(
        id = "er", name = "Endoplasmic reticulum", color = Color(0xFF60A5FA),
        radiusUnit = 0.18f, shape = OrganelleShape.Squiggle,
        role = "Transport network — moves materials around the cell.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.VesiclePinch,
        detail = "The endoplasmic reticulum is a network of folded membranes that carries materials " +
            "through the cytoplasm. Finished molecules bud off in small sacs called vesicles.",
    ),
    OrganelleKind(
        id = "golgi", name = "Golgi apparatus", color = Color(0xFFF59E0B),
        radiusUnit = 0.10f, shape = OrganelleShape.Disk,
        role = "Packaging plant — packs and ships materials.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.VesiclePinch,
        detail = "The Golgi apparatus is a stack of flattened sacs that modifies, sorts, and packs " +
            "materials made by the cell, then sends them off in vesicles.",
    ),
    OrganelleKind(
        id = "vacuole", name = "Vacuole", color = Color(0xFFA7F3D0),
        radiusUnit = 0.30f, shape = OrganelleShape.Circle,
        role = "Storage tank — stores water, food and waste. Large in plant cells.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.FluidLevel,
        detail = "A vacuole stores substances, removes waste, and helps maintain the shape of the cell. " +
            "Plant cells usually have one large central vacuole; animal cells have small ones, if any.",
    ),
    OrganelleKind(
        id = "centriole", name = "Centriole", color = Color(0xFFE879F9),
        radiusUnit = 0.06f, shape = OrganelleShape.PairedRods,
        role = "Helps the cell divide.",
        presentInPlant = false, presentInAnimal = true,
        animKind = AnimKind.Pulse,
        detail = "Centrioles are a pair of short cylinders that help organise cell division in animal cells. " +
            "Plant cells manage to divide without them.",
    ),
    OrganelleKind(
        id = "lysosome", name = "Lysosome", color = Color(0xFFFB923C),
        radiusUnit = 0.05f, shape = OrganelleShape.Circle,
        role = "Digestion sac — breaks down waste and old parts.",
        presentInPlant = false, presentInAnimal = true,
        animKind = AnimKind.Membrane,
        detail = "Lysosomes are sacs of digestive enzymes that break down worn-out parts, food, and " +
            "invading microbes — recycling the materials for the cell.",
    ),
    OrganelleKind(
        id = "cell-wall", name = "Cell wall", color = Color(0xFF065F46),
        radiusUnit = 0f, shape = OrganelleShape.RoundedRect,
        role = "Rigid outer jacket — gives plant cells shape and strength.",
        presentInPlant = true, presentInAnimal = false,
        animKind = AnimKind.Pulse,
        detail = "The cell wall sits just outside the cell membrane in plant cells. " +
            "Made of cellulose, it provides shape and strength, and is why plant cells pack together " +
            "firmly like bricks in a wall.",
    ),
)

internal fun kindOf(id: String): OrganelleKind = KINDS.first { it.id == id }

// Plant cell layout (cell wall and large central vacuole; chloroplasts present)
internal val PLANT_LAYOUT: List<OrganelleInstance> = listOf(
    OrganelleInstance("vacuole",      Vec3( 0.05f,  0.05f,  0.10f)),
    OrganelleInstance("nucleus",      Vec3(-0.55f, -0.05f, -0.25f)),
    OrganelleInstance("mitochondrion",Vec3( 0.55f, -0.40f,  0.30f)),
    OrganelleInstance("mitochondrion",Vec3(-0.30f,  0.55f, -0.10f)),
    OrganelleInstance("chloroplast",  Vec3( 0.40f,  0.45f, -0.30f)),
    OrganelleInstance("chloroplast",  Vec3(-0.50f,  0.40f,  0.40f)),
    OrganelleInstance("chloroplast",  Vec3( 0.65f, -0.05f, -0.40f)),
    OrganelleInstance("ribosome",     Vec3(-0.20f, -0.50f,  0.55f)),
    OrganelleInstance("ribosome",     Vec3( 0.15f, -0.60f, -0.30f)),
    OrganelleInstance("ribosome",     Vec3(-0.65f,  0.20f,  0.20f)),
    OrganelleInstance("er",           Vec3( 0.30f, -0.20f,  0.10f)),
    OrganelleInstance("golgi",        Vec3(-0.45f, -0.45f,  0.25f)),
)

// Animal cell layout (no cell wall, no chloroplasts; centrioles + lysosomes)
internal val ANIMAL_LAYOUT: List<OrganelleInstance> = listOf(
    OrganelleInstance("nucleus",      Vec3( 0.00f,  0.00f, -0.10f)),
    OrganelleInstance("mitochondrion",Vec3( 0.55f, -0.35f,  0.30f)),
    OrganelleInstance("mitochondrion",Vec3(-0.50f,  0.45f, -0.20f)),
    OrganelleInstance("mitochondrion",Vec3( 0.40f,  0.55f,  0.15f)),
    OrganelleInstance("ribosome",     Vec3(-0.30f, -0.55f,  0.40f)),
    OrganelleInstance("ribosome",     Vec3( 0.25f, -0.60f, -0.15f)),
    OrganelleInstance("ribosome",     Vec3(-0.60f,  0.10f,  0.30f)),
    OrganelleInstance("er",           Vec3( 0.35f, -0.10f,  0.20f)),
    OrganelleInstance("golgi",        Vec3(-0.50f, -0.30f,  0.25f)),
    OrganelleInstance("vacuole",      Vec3( 0.55f,  0.40f, -0.30f)),
    OrganelleInstance("centriole",    Vec3(-0.25f,  0.30f,  0.55f)),
    OrganelleInstance("lysosome",     Vec3(-0.05f,  0.55f,  0.30f)),
    OrganelleInstance("lysosome",     Vec3( 0.60f,  0.10f,  0.10f)),
)

// Label placement for the persistent organelle labels around each layout.
internal data class LabelPlacement(
    val anchorDx: Float, val anchorDy: Float,
    val labelX: Float, val labelY: Float,
)

internal val PLANT_LABELS: Map<String, LabelPlacement> = mapOf(
    "nucleus"       to LabelPlacement(-1.0f,  0.0f, -1.60f, -0.10f),
    "mitochondrion" to LabelPlacement( 1.3f,  0.0f,  1.55f, -0.55f),
    "chloroplast"   to LabelPlacement( 1.3f,  0.0f,  1.55f,  0.55f),
    "ribosome"      to LabelPlacement( 0.0f, -1.5f,  0.30f, -1.50f),
    "er"            to LabelPlacement( 1.5f,  0.0f,  1.55f, -0.18f),
    "golgi"         to LabelPlacement( 0.0f, -1.5f, -1.55f, -0.65f),
    "vacuole"       to LabelPlacement( 0.0f,  1.0f,  0.25f,  1.55f),
)

internal val ANIMAL_LABELS: Map<String, LabelPlacement> = mapOf(
    "nucleus"       to LabelPlacement(-1.0f,  0.0f, -1.60f, -0.10f),
    "mitochondrion" to LabelPlacement( 1.3f,  0.0f,  1.55f, -0.55f),
    "ribosome"      to LabelPlacement( 0.0f, -1.5f,  0.20f, -1.50f),
    "er"            to LabelPlacement( 1.5f,  0.0f,  1.55f,  0.20f),
    "golgi"         to LabelPlacement( 0.0f, -1.5f, -1.55f, -0.50f),
    "vacuole"       to LabelPlacement( 1.3f,  0.0f,  1.55f,  0.60f),
    "centriole"     to LabelPlacement( 0.0f,  1.3f, -1.55f,  0.55f),
    "lysosome"      to LabelPlacement( 0.0f,  1.2f, -0.30f,  1.55f),
)

internal data class ProjectedPoint(val x: Float, val y: Float, val zRot: Float, val scale: Float)

// Project a Vec3 to screen Offset — flat NCERT-style. z is a static layering hint only.
internal fun project(pos: Vec3, cx: Float, cy: Float, cellR: Float): ProjectedPoint =
    ProjectedPoint(x = cx + pos.x * cellR, y = cy + pos.y * cellR, zRot = pos.z, scale = 1f)

internal fun hitTestCell(tap: Offset, canvasW: Float, canvasH: Float, cellType: CellType): String? {
    val cx = canvasW / 2f
    val cy = canvasH / 2f
    val cellR = min(canvasW, canvasH) * 0.35f
    val instances = if (cellType == CellType.Plant) PLANT_LAYOUT else ANIMAL_LAYOUT
    val projected = instances.map { it to project(it.pos, cx, cy, cellR) }
        .sortedByDescending { it.second.zRot }
    for ((inst, p) in projected) {
        val k = kindOf(inst.kindId)
        val r = cellR * k.radiusUnit
        val hitR = r * 1.3f
        val d = Offset(tap.x - p.x, tap.y - p.y).getDistance()
        if (d < hitR) return k.id
    }
    return null
}

/**
 * Draws a complete labelled cell (wall/membrane + organelles + leader labels) centred in the
 * canvas. Used by the Fig 2.5 schematic and the levels-of-organisation cell level.
 */
internal fun DrawScope.drawLabelledCell(
    textMeasurer: TextMeasurer,
    cellType: CellType,
    labelColor: Color,
    cxFrac: Float = 0.5f,
    cyFrac: Float = 0.5f,
    radiusFrac: Float = 0.35f,
    showLabels: Boolean = true,
) {
    val w = size.width; val h = size.height
    val cx = w * cxFrac; val cy = h * cyFrac
    val cellR = min(w, h) * radiusFrac
    val instances = if (cellType == CellType.Plant) PLANT_LAYOUT else ANIMAL_LAYOUT

    drawCellOutline(cx, cy, cellR, isPlant = cellType == CellType.Plant)

    if (showLabels) {
        if (cellType == CellType.Plant) {
            drawLabel(textMeasurer, "Cell wall",
                anchor = Offset(cx - cellR * 1.18f, cy - cellR * 0.9f),
                labelEnd = Offset(cx - cellR * 1.45f, cy - cellR * 1.1f),
                color = Color(0xFF065F46))
        }
        drawLabel(textMeasurer, "Cell membrane",
            anchor = Offset(cx - cellR * 0.71f, cy + cellR * 0.71f),
            labelEnd = Offset(cx - cellR * 1.15f, cy + cellR * 1.0f),
            color = Color(0xFFEC4899))
    }

    val projected = instances.map { it to project(it.pos, cx, cy, cellR) }
        .sortedBy { it.second.zRot }
    for ((inst, p) in projected) {
        val k = kindOf(inst.kindId)
        val r = cellR * k.radiusUnit
        drawOrganelle(k, Offset(p.x, p.y), r)
    }

    if (showLabels) {
        val labelTable = if (cellType == CellType.Plant) PLANT_LABELS else ANIMAL_LABELS
        val labelled = mutableSetOf<String>()
        for (inst in instances) {
            if (inst.kindId in labelled) continue
            val place = labelTable[inst.kindId] ?: continue
            val k = kindOf(inst.kindId)
            val p = project(inst.pos, cx, cy, cellR)
            val r = cellR * k.radiusUnit
            val anchor = Offset(p.x + place.anchorDx * r, p.y + place.anchorDy * r)
            val labelEnd = Offset(cx + place.labelX * cellR, cy + place.labelY * cellR)
            drawLabel(textMeasurer, k.name, anchor, labelEnd, color = labelColor)
            labelled += inst.kindId
        }
    }
}

internal fun DrawScope.drawCellOutline(
    cx: Float, cy: Float, cellR: Float,
    isPlant: Boolean,
    wallStrokeColor: Color = Color(0xFF065F46),
    wallStrokeWidth: Float = 3f,
) {
    if (isPlant) {
        val outer = cellR * 1.20f
        val left = cx - outer
        val top = cy - outer
        val side = outer * 2f
        drawRect(Color(0xFF065F46).copy(alpha = 0.18f),
            topLeft = Offset(left, top), size = Size(side, side))
        drawRect(wallStrokeColor,
            topLeft = Offset(left, top), size = Size(side, side),
            style = Stroke(wallStrokeWidth))
        val lamInset = 10f
        drawRect(Color(0xFFCBD5E1).copy(alpha = 0.85f),
            topLeft = Offset(left + lamInset, top + lamInset),
            size = Size(side - lamInset * 2f, side - lamInset * 2f),
            style = Stroke(2f))
        val inset = 4f
        drawRect(wallStrokeColor.copy(alpha = 0.55f),
            topLeft = Offset(left + inset, top + inset),
            size = Size(side - inset * 2f, side - inset * 2f),
            style = Stroke(1f))
        val hatchCol = Color(0xFF065F46).copy(alpha = 0.50f)
        val hatchStep = 16f
        val hatchInset = 8f
        var x = left + hatchInset + 8f
        while (x < left + side - hatchInset - 8f) {
            drawLine(hatchCol, Offset(x, top + hatchInset),
                Offset(x + 10f, top + hatchInset + 10f), strokeWidth = 1f)
            x += hatchStep
        }
        x = left + hatchInset + 8f
        while (x < left + side - hatchInset - 8f) {
            drawLine(hatchCol, Offset(x, top + side - hatchInset),
                Offset(x + 10f, top + side - hatchInset - 10f), strokeWidth = 1f)
            x += hatchStep
        }
        var yy = top + hatchInset + 8f
        while (yy < top + side - hatchInset - 8f) {
            drawLine(hatchCol, Offset(left + hatchInset, yy),
                Offset(left + hatchInset + 10f, yy + 10f), strokeWidth = 1f)
            yy += hatchStep
        }
        yy = top + hatchInset + 8f
        while (yy < top + side - hatchInset - 8f) {
            drawLine(hatchCol, Offset(left + side - hatchInset, yy),
                Offset(left + side - hatchInset - 10f, yy + 10f), strokeWidth = 1f)
            yy += hatchStep
        }
    }

    val memTL = Offset(cx - cellR, cy - cellR)
    val memSize = Size(cellR * 2f, cellR * 2f)
    val cytoBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFBCFE8).copy(alpha = 0.30f),
            Color(0xFFFBCFE8).copy(alpha = 0.12f),
        ),
        center = Offset(cx, cy), radius = cellR,
    )
    drawOval(cytoBrush, topLeft = memTL, size = memSize)
    drawOval(Color(0xFFEC4899), topLeft = memTL, size = memSize, style = Stroke(2.5f))
    drawOval(Color(0xFFEC4899).copy(alpha = 0.55f),
        topLeft = Offset(memTL.x + 4f, memTL.y + 4f),
        size = Size(memSize.width - 8f, memSize.height - 8f),
        style = Stroke(1f))
    val proteinCount = 10
    val proteinCol = Color(0xFFC026D3).copy(alpha = 0.90f)
    for (i in 0 until proteinCount) {
        val a = i * (2f * PI.toFloat() / proteinCount) + 0.15f
        val px = cx + cos(a) * cellR
        val py = cy + sin(a) * cellR
        drawCircle(proteinCol, 3.5f, Offset(px, py))
        drawCircle(Color(0xFFFBCFE8), 1.4f, Offset(px, py))
    }
}

internal fun DrawScope.drawOrganelle(k: OrganelleKind, center: Offset, r: Float) {
    when (k.id) {
        "nucleus" -> drawNucleus(center, r, k.color)
        "mitochondrion" -> drawMitochondrion(center, r, k.color)
        "chloroplast" -> drawChloroplast(center, r, k.color)
        "ribosome" -> drawRibosome(center, r, k.color)
        "er" -> drawER(center, r, k.color)
        "golgi" -> drawGolgi(center, r, k.color)
        "vacuole" -> drawVacuole(center, r, k.color)
        "centriole" -> drawCentriole(center, r, k.color)
        "lysosome" -> drawLysosome(center, r, k.color)
        "cell-wall" -> { /* drawn at cell-outline level */ }
        else -> drawCircle(k.color, r, center)
    }
}

private fun DrawScope.drawNucleus(c: Offset, r: Float, color: Color) {
    val brush = Brush.radialGradient(
        colors = listOf(
            color.copy(alpha = 0.50f),
            color.copy(alpha = 0.80f),
            color.copy(red = color.red * 0.55f, green = color.green * 0.55f, blue = color.blue * 0.55f),
        ),
        center = Offset(c.x - r * 0.25f, c.y - r * 0.25f),
        radius = r * 1.3f,
    )
    drawCircle(brush, r, c)
    drawCircle(Color(0xFF4C1D95).copy(alpha = 0.25f), r * 0.95f, c, style = Stroke(r * 0.10f))
    drawCircle(Color(0xFF4C1D95), r * 1.04f, c, style = Stroke(1.6f))
    drawCircle(Color(0xFF4C1D95).copy(alpha = 0.55f), r * 0.96f, c, style = Stroke(1f))
    if (r > 8f) {
        val poreCount = 10
        val poreCol = Color(0xFFFAF5FF)
        val poreEdge = Color(0xFF4C1D95)
        for (i in 0 until poreCount) {
            val a = i * (2f * PI.toFloat() / poreCount) + 0.15f
            val px = c.x + cos(a) * r * 1.00f
            val py = c.y + sin(a) * r * 1.00f
            drawCircle(poreCol, 2.4f, Offset(px, py))
            drawCircle(poreEdge, 2.4f, Offset(px, py), style = Stroke(0.8f))
        }
    }
    val chroma = Color(0xFFE9D5FF).copy(alpha = 0.75f)
    val r6 = r * 0.62f
    val strands = 8
    for (i in 0 until strands) {
        val a = i * (2f * PI.toFloat() / strands) + 0.3f
        val p1 = Offset(c.x + cos(a) * r * 0.18f, c.y + sin(a) * r * 0.18f)
        val p2 = Offset(c.x + cos(a) * r6, c.y + sin(a) * r6)
        val mid = Offset((p1.x + p2.x) / 2f + cos(a + 1.5f) * r * 0.14f,
            (p1.y + p2.y) / 2f + sin(a + 1.5f) * r * 0.14f)
        val path = Path().apply { moveTo(p1.x, p1.y); quadraticTo(mid.x, mid.y, p2.x, p2.y) }
        drawPath(path, chroma, style = Stroke(1.4f, cap = StrokeCap.Round))
    }
    val nlc = Offset(c.x + r * 0.18f, c.y - r * 0.06f)
    drawCircle(Color(0xFFCA8A04), r * 0.26f, nlc)
    drawCircle(Color(0xFFFCD34D), r * 0.21f, nlc)
    drawCircle(Color(0xFF92400E).copy(alpha = 0.55f), r * 0.08f, Offset(nlc.x + r * 0.03f, nlc.y))
}

private fun DrawScope.drawMitochondrion(c: Offset, r: Float, color: Color) {
    val w = r * 2.8f
    val h = r * 1.5f
    val tl = Offset(c.x - w / 2f, c.y - h / 2f)
    val outer = Brush.linearGradient(
        colors = listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.75f)),
        start = Offset(tl.x, tl.y), end = Offset(tl.x + w, tl.y + h),
    )
    drawOval(outer, topLeft = tl, size = Size(w, h))
    val matrixTL = Offset(c.x - w * 0.43f, c.y - h * 0.37f)
    val matrixSize = Size(w * 0.86f, h * 0.74f)
    val matrixBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)), center = c, radius = w * 0.45f,
    )
    drawOval(matrixBrush, topLeft = matrixTL, size = matrixSize)
    if (r > 6f) {
        val cristaCol = Color(0xFFFCA5A5).copy(alpha = 0.95f)
        val cristaCount = 10
        val matrixLeft = matrixTL.x
        val matrixTop = matrixTL.y
        val matrixW = matrixSize.width
        val matrixH = matrixSize.height
        for (i in 0 until cristaCount) {
            val tFrac = (i + 0.5f) / cristaCount.toFloat()
            val xMid = matrixLeft + matrixW * tFrac
            val fromTop = i % 2 == 0
            val yEdge = if (fromTop) matrixTop + 2f else matrixTop + matrixH - 2f
            val yTip = if (fromTop) matrixTop + matrixH * 0.78f else matrixTop + matrixH * 0.22f
            val controlOffset = matrixW * 0.045f
            val path = Path().apply {
                moveTo(xMid - controlOffset, yEdge)
                quadraticTo(xMid, yTip, xMid + controlOffset, yEdge)
            }
            drawPath(path, cristaCol, style = Stroke(1.4f, cap = StrokeCap.Round))
        }
        val granuleCol = Color(0xFFFEF3C7).copy(alpha = 0.7f)
        listOf(
            Offset(c.x - w * 0.18f, c.y + h * 0.08f),
            Offset(c.x + w * 0.12f, c.y - h * 0.12f),
            Offset(c.x + w * 0.30f, c.y + h * 0.18f),
        ).forEach { drawCircle(granuleCol, r * 0.06f, it) }
    }
    drawOval(Color(0xFF7F1D1D), topLeft = tl, size = Size(w, h), style = Stroke(1.4f))
    drawOval(Color(0xFFFCA5A5).copy(alpha = 0.55f),
        topLeft = Offset(tl.x + 3f, tl.y + 3f), size = Size(w - 6f, h - 6f), style = Stroke(0.8f))
}

private fun DrawScope.drawChloroplast(c: Offset, r: Float, color: Color) {
    val w = r * 2.8f
    val h = r * 1.6f
    val tl = Offset(c.x - w / 2f, c.y - h / 2f)
    val stroma = Brush.radialGradient(
        colors = listOf(Color(0xFFA7F3D0), color.copy(alpha = 0.85f), Color(0xFF047857)),
        center = c, radius = w * 0.55f,
    )
    drawOval(stroma, topLeft = tl, size = Size(w, h))
    if (r > 6f) {
        val granaDark = Color(0xFF064E3B)
        val granaLight = Color(0xFF065F46)
        val granaCount = 5
        val discsPerStack = 5
        val firstStackX = c.x - w * 0.32f
        val stackSpacing = w * 0.16f
        val discW = r * 0.32f
        val discH = r * 0.08f
        val stackTop = c.y - r * 0.45f
        val centerYsOfStacks = FloatArray(granaCount) { c.y }
        for (g in 0 until granaCount) {
            val cxg = firstStackX + g * stackSpacing
            val yJitter = if (g % 2 == 0) -r * 0.06f else r * 0.06f
            val sTop = stackTop + yJitter
            for (i in 0 until discsPerStack) {
                val y0 = sTop + i * (discH + 1.5f)
                drawOval(if (i % 2 == 0) granaDark else granaLight,
                    topLeft = Offset(cxg - discW / 2f, y0), size = Size(discW, discH))
            }
            centerYsOfStacks[g] = sTop + (discsPerStack * (discH + 1.5f)) / 2f
        }
        val lamella = Color(0xFF064E3B).copy(alpha = 0.60f)
        for (g in 0 until granaCount - 1) {
            val x1 = firstStackX + g * stackSpacing + discW / 2f
            val x2 = firstStackX + (g + 1) * stackSpacing - discW / 2f
            val y = (centerYsOfStacks[g] + centerYsOfStacks[g + 1]) / 2f
            drawLine(lamella, Offset(x1, y), Offset(x2, y), strokeWidth = 1.2f)
        }
    }
    drawOval(Color(0xFF064E3B), topLeft = tl, size = Size(w, h), style = Stroke(1.4f))
    drawOval(Color(0xFF065F46).copy(alpha = 0.55f),
        topLeft = Offset(tl.x + 3f, tl.y + 3f), size = Size(w - 6f, h - 6f), style = Stroke(0.8f))
}

private fun DrawScope.drawRibosome(c: Offset, r: Float, color: Color) {
    drawCircle(color, r, c)
    drawCircle(color.copy(alpha = 0.7f), r * 0.65f, Offset(c.x + r * 0.4f, c.y - r * 0.4f))
    drawCircle(Color(0xFFB45309).copy(alpha = 0.6f), r, c, style = Stroke(0.8f))
}

private fun DrawScope.drawER(c: Offset, r: Float, color: Color) {
    val span = r * 1.7f
    val cisternaCount = 3
    val gap = r * 0.30f
    val baseY = c.y - gap * (cisternaCount - 1) / 2f
    val membraneCol = color.copy(alpha = 0.95f)
    val membraneStrokeW = r * 0.10f
    for (i in 0 until cisternaCount) {
        val yLine = baseY + i * gap
        val path = Path().apply {
            moveTo(c.x - span, yLine + r * 0.05f * (i - 1f))
            cubicTo(
                c.x - span * 0.35f, yLine - r * 0.25f,
                c.x + span * 0.25f, yLine + r * 0.30f,
                c.x + span, yLine - r * 0.10f,
            )
        }
        drawPath(path, membraneCol, style = Stroke(membraneStrokeW, cap = StrokeCap.Round))
    }
    if (r > 10f) {
        val ribCol = Color(0xFFFCD34D)
        val ribStroke = Color(0xFFB45309).copy(alpha = 0.5f)
        for (i in 0 until cisternaCount) {
            val yLine = baseY + i * gap
            for (j in 0 until 7) {
                val tFrac = j / 7f
                val px = c.x - span + tFrac * (2 * span)
                val curveOffset = sin(tFrac * PI.toFloat() * 1.4f) * r * 0.20f * (1f - i * 0.15f)
                val side = if (j % 2 == 0) -1f else 1f
                val py = yLine + curveOffset + side * (membraneStrokeW / 2f + r * 0.06f)
                drawCircle(ribCol, r * 0.07f, Offset(px, py))
                drawCircle(ribStroke, r * 0.07f, Offset(px, py), style = Stroke(0.6f))
            }
        }
    }
}

private fun DrawScope.drawGolgi(c: Offset, r: Float, color: Color) {
    val sacCount = 6
    val sacWMax = r * 2.6f
    val sacWMin = r * 1.7f
    val sacH = r * 0.30f
    val gap = r * 0.22f
    val stackTop = c.y - (sacCount - 1) * gap / 2f - sacH * 0.3f
    for (i in 0 until sacCount) {
        val tFrac = i / (sacCount - 1f)
        val sacW = sacWMax + (sacWMin - sacWMax) * tFrac
        val xJitter = if (i % 2 == 0) -r * 0.06f else r * 0.06f
        val y = stackTop + i * gap
        val sacCol = color.copy(alpha = 0.95f - i * 0.06f)
        drawArc(sacCol, startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(c.x - sacW / 2f + xJitter, y - sacH * 0.6f),
            size = Size(sacW, sacH * 1.2f), style = Stroke(2f, cap = StrokeCap.Round))
    }
    if (r > 6f) {
        val vCol = color.copy(alpha = 0.92f)
        val vEdge = Color(0xFF92400E).copy(alpha = 0.55f)
        val transY = stackTop + (sacCount - 1) * gap
        val budRight = c.x + sacWMin / 2f
        val buds = listOf(
            Offset(budRight + r * 0.10f, transY + r * 0.20f) to r * 0.16f,
            Offset(budRight + r * 0.32f, transY + r * 0.42f) to r * 0.13f,
            Offset(budRight + r * 0.55f, transY + r * 0.62f) to r * 0.10f,
            Offset(c.x - sacWMin / 2f - r * 0.30f, transY + r * 0.45f) to r * 0.12f,
        )
        buds.forEach { (pt, rad) ->
            drawCircle(vCol, rad, pt)
            drawCircle(vEdge, rad, pt, style = Stroke(0.8f))
        }
    }
}

private fun DrawScope.drawVacuole(c: Offset, r: Float, color: Color) {
    val brush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFECFDF5).copy(alpha = 0.55f),
            color.copy(alpha = 0.40f),
            color.copy(alpha = 0.20f),
        ),
        center = c, radius = r,
    )
    drawCircle(brush, r, c)
    drawCircle(Color(0xFF047857).copy(alpha = 0.70f), r, c, style = Stroke(1.6f))
    if (r > 18f) {
        val highlight = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 0.5f,
        )
        drawCircle(highlight, r * 0.6f, Offset(c.x - r * 0.3f, c.y - r * 0.3f))
        val drift = Color(0xFF10B981).copy(alpha = 0.18f)
        for (i in 0 until 3) {
            val yy = c.y + (i - 1) * r * 0.25f + r * 0.05f
            val path = Path().apply {
                moveTo(c.x - r * 0.55f, yy)
                quadraticTo(c.x, yy - r * 0.15f, c.x + r * 0.55f, yy)
            }
            drawPath(path, drift, style = Stroke(1f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawCentriole(c: Offset, r: Float, color: Color) {
    val rodW = r * 0.8f
    val rodH = r * 1.8f
    val vRodTL = Offset(c.x - rodW - r * 0.25f, c.y - rodH / 2f)
    drawRect(color, topLeft = vRodTL, size = Size(rodW, rodH))
    val hRodTL = Offset(c.x + r * 0.25f, c.y - rodW / 2f)
    drawRect(color, topLeft = hRodTL, size = Size(rodH, rodW))
    if (r > 6f) {
        val tickCol = Color(0xFFF5D0FE)
        val vEndTop = Offset(vRodTL.x + rodW / 2f, vRodTL.y)
        val vEndBot = Offset(vRodTL.x + rodW / 2f, vRodTL.y + rodH)
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(vEndTop.x - rodW / 2f, vEndTop.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(vEndTop.x - rodW / 2f, vEndTop.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f), style = Stroke(0.8f))
        for (i in 0 until 9) {
            val a = i * (2f * PI.toFloat() / 9f) + 0.2f
            drawCircle(tickCol, 1.2f, Offset(vEndTop.x + cos(a) * rodW * 0.42f, vEndTop.y + sin(a) * rodW * 0.15f))
        }
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(vEndBot.x - rodW / 2f, vEndBot.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(vEndBot.x - rodW / 2f, vEndBot.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f), style = Stroke(0.8f))
        val hEndL = Offset(hRodTL.x, hRodTL.y + rodW / 2f)
        val hEndR = Offset(hRodTL.x + rodH, hRodTL.y + rodW / 2f)
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(hEndL.x - rodW * 0.15f, hEndL.y - rodW / 2f), size = Size(rodW * 0.30f, rodW))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(hEndL.x - rodW * 0.15f, hEndL.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW), style = Stroke(0.8f))
        for (i in 0 until 9) {
            val a = i * (2f * PI.toFloat() / 9f) + 0.2f
            drawCircle(tickCol, 1.2f, Offset(hEndR.x + cos(a) * rodW * 0.15f, hEndR.y + sin(a) * rodW * 0.42f))
        }
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(hEndR.x - rodW * 0.15f, hEndR.y - rodW / 2f), size = Size(rodW * 0.30f, rodW))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(hEndR.x - rodW * 0.15f, hEndR.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW), style = Stroke(0.8f))
        val str = Color(0xFFF5D0FE).copy(alpha = 0.75f)
        for (i in 1..2) {
            val xx = vRodTL.x + rodW * (i / 3f)
            drawLine(str, Offset(xx, vRodTL.y + 2f), Offset(xx, vRodTL.y + rodH - 2f), strokeWidth = 0.6f)
            val yy = hRodTL.y + rodW * (i / 3f)
            drawLine(str, Offset(hRodTL.x + 2f, yy), Offset(hRodTL.x + rodH - 2f, yy), strokeWidth = 0.6f)
        }
    }
    drawRect(Color(0xFF86198F), topLeft = vRodTL, size = Size(rodW, rodH), style = Stroke(0.8f))
    drawRect(Color(0xFF86198F), topLeft = hRodTL, size = Size(rodH, rodW), style = Stroke(0.8f))
}

private fun DrawScope.drawLysosome(c: Offset, r: Float, color: Color) {
    val brush = Brush.radialGradient(
        colors = listOf(color.copy(alpha = 0.95f), color, Color(0xFFC2410C)),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.3f,
    )
    val outline = Path().apply {
        val sides = 10
        for (i in 0 until sides) {
            val a = i * (2f * PI.toFloat() / sides)
            val rad = r * (1f + (if (i % 2 == 0) 0.05f else -0.05f))
            val x = c.x + cos(a) * rad
            val y = c.y + sin(a) * rad
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(outline, brush)
    drawPath(outline, Color(0xFF9A3412).copy(alpha = 0.75f), style = Stroke(1f))
    if (r > 5f) {
        val speck = Color(0xFF7C2D12).copy(alpha = 0.80f)
        val speckLight = Color(0xFFFEF3C7).copy(alpha = 0.6f)
        val dots = listOf(
            Offset(c.x - r * 0.30f, c.y - r * 0.10f) to r * 0.10f,
            Offset(c.x + r * 0.20f, c.y - r * 0.32f) to r * 0.09f,
            Offset(c.x + r * 0.35f, c.y + r * 0.10f) to r * 0.11f,
            Offset(c.x - r * 0.05f, c.y + r * 0.35f) to r * 0.09f,
            Offset(c.x - r * 0.40f, c.y + r * 0.18f) to r * 0.08f,
            Offset(c.x + r * 0.05f, c.y + r * 0.05f) to r * 0.07f,
        )
        dots.forEach { (pt, rad) ->
            drawCircle(speck, rad, pt)
            drawCircle(speckLight, rad * 0.45f, Offset(pt.x - rad * 0.3f, pt.y - rad * 0.3f))
        }
    }
}

// ── Cell-shape gallery (levels of organisation / variation in cells) ────

internal data class CellShape(
    val id: String, val name: String, val tagline: String, val detail: String,
    val drawer: DrawScope.(center: Offset, r: Float) -> Unit,
    val keyParts: List<String> = emptyList(),
)

internal val CELL_SHAPES: List<CellShape> = listOf(
    CellShape(
        id = "muscle", name = "Muscle cell", tagline = "Spindle-shaped",
        detail = "A muscle cell is shaped like a spindle — long, thin, and tapered at both ends — so it can " +
            "contract and relax to produce movement.",
        keyParts = listOf("Spindle shape — tapered ends", "Contracts and relaxes", "Found in the stomach wall, heart, limbs"),
        drawer = { c, r -> drawMuscleCell(c, r) },
    ),
    CellShape(
        id = "neuron", name = "Nerve cell (neuron)", tagline = "Long & branched",
        detail = "A nerve cell is very long and has many branches, so it can reach distant parts of the body " +
            "and pass on messages quickly.",
        keyParts = listOf("Cell body holds the nucleus", "Branches receive signals", "Long fibre carries the signal"),
        drawer = { c, r -> drawNeuronCell(c, r) },
    ),
    CellShape(
        id = "cheek", name = "Cheek cell", tagline = "Thin & flat",
        detail = "Cheek cells are thin and flat so they tile together smoothly to form the protective lining " +
            "of the inside of the mouth.",
        keyParts = listOf("Flat, irregular polygon", "Tiles together with no gaps", "Forms a protective lining"),
        drawer = { c, r -> drawFlatCheekCell(c, r) },
    ),
    CellShape(
        id = "plant-tube", name = "Water-carrying cell", tagline = "Long hollow tube",
        detail = "Some plant cells form long hollow tubes joined end to end, so they can carry water from the " +
            "roots all the way up to the leaves.",
        keyParts = listOf("Tube shape", "Hollow inside", "Joined end to end to carry water"),
        drawer = { c, r -> drawWaterTubeCell(c, r) },
    ),
)

private fun DrawScope.drawMuscleCell(c: Offset, r: Float) {
    val w = r * 2.4f
    val h = r * 0.7f
    val fill = Brush.linearGradient(
        colors = listOf(Color(0xFFFCA5A5), Color(0xFFEF4444), Color(0xFFB91C1C)),
        start = Offset(c.x - w / 2f, c.y), end = Offset(c.x + w / 2f, c.y),
    )
    val path = Path().apply {
        moveTo(c.x - w / 2f, c.y)
        cubicTo(c.x - w * 0.25f, c.y - h, c.x + w * 0.25f, c.y - h, c.x + w / 2f, c.y)
        cubicTo(c.x + w * 0.25f, c.y + h, c.x - w * 0.25f, c.y + h, c.x - w / 2f, c.y)
        close()
    }
    drawPath(path, fill)
    drawPath(path, Color(0xFF7F1D1D), style = Stroke(2f))
    // faint striations
    for (i in 1..7) {
        val x = c.x - w / 2f + w * (i / 8f)
        drawLine(Color(0xFF7F1D1D).copy(alpha = 0.35f),
            Offset(x, c.y - h * 0.5f), Offset(x, c.y + h * 0.5f), strokeWidth = 1f)
    }
    drawCircle(Color(0xFF7C3AED), r * 0.14f, c)
}

private fun DrawScope.drawNeuronCell(c: Offset, r: Float) {
    val somaC = Offset(c.x - r * 0.4f, c.y)
    val somaR = r * 0.45f
    val dendCol = Color(0xFFEC4899).copy(alpha = 0.85f)
    for (i in 0 until 6) {
        val a = (i * 2 * PI.toFloat() / 6f) + PI.toFloat() * 1.05f
        if (a > PI.toFloat() * 0.55f && a < PI.toFloat() * 1.45f) continue
        val start = Offset(somaC.x + cos(a) * somaR * 0.9f, somaC.y + sin(a) * somaR * 0.9f)
        val mid = Offset(somaC.x + cos(a) * somaR * 1.6f, somaC.y + sin(a) * somaR * 1.6f)
        drawLine(dendCol, start, mid, strokeWidth = 3.5f, cap = StrokeCap.Round)
        for (b in 0 until 3) {
            val branchA = a + (b - 1) * 0.4f
            val tip = Offset(mid.x + cos(branchA) * somaR * 0.7f, mid.y + sin(branchA) * somaR * 0.7f)
            drawLine(dendCol, mid, tip, strokeWidth = 2f, cap = StrokeCap.Round)
            drawCircle(dendCol, 1.5f, tip)
        }
    }
    val somaBrush = Brush.radialGradient(
        colors = listOf(Color(0xFFFBCFE8), Color(0xFFEC4899), Color(0xFFBE185D)),
        center = Offset(somaC.x - somaR * 0.3f, somaC.y - somaR * 0.3f), radius = somaR * 1.2f,
    )
    drawCircle(somaBrush, somaR, somaC)
    drawCircle(Color(0xFF9D174D).copy(alpha = 0.7f), somaR * 0.35f, somaC)
    drawCircle(Color(0xFFFCD34D), somaR * 0.10f, Offset(somaC.x + somaR * 0.05f, somaC.y))
    val axonY = c.y
    val axonStartX = somaC.x + somaR * 0.9f
    val segCount = 5
    val segLen = (c.x + r * 1.2f - axonStartX) / segCount
    val myelin = Color(0xFFFCD34D)
    val myelinEdge = Color(0xFFB45309)
    for (i in 0 until segCount) {
        val x0 = axonStartX + i * segLen + segLen * 0.05f
        val x1 = axonStartX + (i + 1) * segLen - segLen * 0.05f
        drawOval(myelin, topLeft = Offset(x0, axonY - r * 0.18f), size = Size(x1 - x0, r * 0.36f))
        drawOval(myelinEdge, topLeft = Offset(x0, axonY - r * 0.18f), size = Size(x1 - x0, r * 0.36f), style = Stroke(1f))
    }
    val termX = axonStartX + segCount * segLen
    for (b in 0 until 3) {
        val ay = axonY + (b - 1) * r * 0.18f
        drawLine(dendCol, Offset(termX, axonY), Offset(termX + r * 0.25f, ay), strokeWidth = 2f, cap = StrokeCap.Round)
        drawCircle(dendCol, 2f, Offset(termX + r * 0.25f, ay))
    }
}

private fun DrawScope.drawFlatCheekCell(c: Offset, r: Float) {
    // a single flat irregular polygon with a central nucleus (methylene-blue palette)
    val pts = listOf(
        Offset(c.x - r, c.y - r * 0.25f),
        Offset(c.x - r * 0.3f, c.y - r * 0.7f),
        Offset(c.x + r * 0.7f, c.y - r * 0.5f),
        Offset(c.x + r, c.y + r * 0.25f),
        Offset(c.x + r * 0.3f, c.y + r * 0.7f),
        Offset(c.x - r * 0.7f, c.y + r * 0.55f),
    )
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        close()
    }
    drawPath(path, Color(0xFFBFDBFE).copy(alpha = 0.55f))
    drawPath(path, Color(0xFF2563EB), style = Stroke(2f))
    drawCircle(Color(0xFF1E3A8A), r * 0.20f, c)
    drawCircle(Color(0xFF1E40AF).copy(alpha = 0.6f), r * 0.20f, c, style = Stroke(1.4f))
}

private fun DrawScope.drawWaterTubeCell(c: Offset, r: Float) {
    val w = r * 0.7f
    val h = r * 2.2f
    val left = c.x - w / 2f
    val top = c.y - h / 2f
    // three stacked tube segments joined end to end
    val seg = h / 3f
    val fill = Color(0xFFD9F99D).copy(alpha = 0.5f)
    for (i in 0 until 3) {
        val y0 = top + i * seg
        drawRect(fill, topLeft = Offset(left, y0), size = Size(w, seg - 2f))
        drawRect(Color(0xFF4D7C0F), topLeft = Offset(left, y0), size = Size(w, seg - 2f), style = Stroke(2f))
    }
    // hollow centre line + upward flow arrows
    drawLine(Color(0xFF65A30D).copy(alpha = 0.5f), Offset(c.x, top + 6f), Offset(c.x, top + h - 6f), strokeWidth = 1.2f)
    for (i in 0 until 3) {
        val ay = top + h - seg * i - seg * 0.5f
        drawLine(Color(0xFF3F6212), Offset(c.x, ay + 6f), Offset(c.x, ay - 6f), strokeWidth = 2f)
        drawLine(Color(0xFF3F6212), Offset(c.x, ay - 6f), Offset(c.x - 3f, ay - 1f), strokeWidth = 2f)
        drawLine(Color(0xFF3F6212), Offset(c.x, ay - 6f), Offset(c.x + 3f, ay - 1f), strokeWidth = 2f)
    }
}

// ── Microscope-view tissue drawers (onion peel + cheek mount) ──────────

/**
 * Onion-peel field of view (Fig 2.3c): rectangular cells packed edge-to-edge with no gaps,
 * tinted pink by safranin. [highlight] (wall/membrane/nucleus/cytoplasm) emphasises one structure.
 */
internal fun DrawScope.drawOnionField(stainAlpha: Float = 1f, highlight: String? = null) {
    val w = size.width; val h = size.height
    val fill = Color(0xFFFBCFE8).copy(alpha = 0.45f * stainAlpha)
    val cytoFill = Color(0xFFF9A8D4).copy(alpha = 0.65f * stainAlpha)
    val wall = Color(0xFFDB2777).copy(alpha = 0.85f * stainAlpha + 0.10f)
    val glow = Color(0xFFFACC15)
    val cols = 4; val rows = 5
    val cw = w / cols; val ch = h / rows
    for (rIdx in 0 until rows) {
        for (cIdx in 0 until cols) {
            val x = cIdx * cw
            val y = rIdx * ch
            val dx = if (rIdx % 2 == 0) 0f else cw * 0.15f
            val left = x + dx
            drawRect(if (highlight == "cytoplasm") cytoFill else fill,
                topLeft = Offset(left, y), size = Size(cw - 2f, ch - 2f))
            // cell membrane = inner inset stroke
            if (highlight == "membrane") {
                drawRect(glow, topLeft = Offset(left + 3f, y + 3f),
                    size = Size(cw - 8f, ch - 8f), style = Stroke(2f))
            }
            // cell wall = outer stroke
            drawRect(if (highlight == "wall") glow else wall,
                topLeft = Offset(left, y), size = Size(cw - 2f, ch - 2f),
                style = Stroke(if (highlight == "wall") 4f else 2.5f))
            val nC = Offset(left + cw * 0.30f, y + ch * 0.32f)
            if (highlight == "nucleus") drawCircle(glow, min(cw, ch) * 0.18f, nC, style = Stroke(2.5f))
            drawCircle(Color(0xFFBE185D).copy(alpha = 0.85f), min(cw, ch) * 0.12f, nC)
        }
    }
}

/**
 * Cheek-cell field of view (Fig 2.4): rounded, loosely-packed polygons that sometimes overlap,
 * tinted blue by methylene blue, each with a prominent nucleus.
 */
internal fun DrawScope.drawCheekField(stainAlpha: Float = 1f, highlight: String? = null) {
    val w = size.width; val h = size.height
    val fill = Color(0xFFBFDBFE).copy(alpha = 0.40f * stainAlpha)
    val cytoFill = Color(0xFF93C5FD).copy(alpha = 0.60f * stainAlpha)
    val edge = Color(0xFF2563EB).copy(alpha = 0.80f * stainAlpha + 0.10f)
    val glow = Color(0xFFFACC15)
    data class Blob(val cx: Float, val cy: Float, val r: Float)
    val blobs = listOf(
        Blob(w * 0.28f, h * 0.30f, min(w, h) * 0.20f),
        Blob(w * 0.62f, h * 0.26f, min(w, h) * 0.17f),
        Blob(w * 0.46f, h * 0.62f, min(w, h) * 0.22f),
        Blob(w * 0.78f, h * 0.62f, min(w, h) * 0.16f),
        Blob(w * 0.18f, h * 0.68f, min(w, h) * 0.15f),
    )
    blobs.forEach { b ->
        val path = Path().apply {
            val sides = 7
            for (i in 0 until sides) {
                val a = i * (2f * PI.toFloat() / sides) + 0.4f
                val rad = b.r * (0.85f + if (i % 2 == 0) 0.18f else 0f)
                val x = b.cx + cos(a) * rad
                val y = b.cy + sin(a) * rad * 0.85f
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path, if (highlight == "cytoplasm") cytoFill else fill)
        drawPath(path, if (highlight == "membrane") glow else edge,
            style = Stroke(if (highlight == "membrane") 3f else 2f))
        if (highlight == "nucleus") drawCircle(glow, b.r * 0.36f, Offset(b.cx, b.cy), style = Stroke(2.5f))
        drawCircle(Color(0xFF1E3A8A).copy(alpha = 0.9f), b.r * 0.28f, Offset(b.cx, b.cy))
    }
}

// ── Text-in-canvas helpers ───────────────────────────────────────────

internal fun DrawScope.drawTextAt(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight),
    )
    drawText(textLayoutResult = layout, topLeft = topLeft)
}

internal fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    anchor: Offset,
    labelEnd: Offset,
    color: Color,
    fontSize: TextUnit = 10.sp,
) {
    drawLine(color.copy(alpha = 0.70f), anchor, labelEnd, strokeWidth = 1f)
    drawCircle(color, 2.2f, anchor)
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = fontSize, fontWeight = FontWeight.SemiBold),
    )
    val pad = 4f
    val textTopLeft = if (labelEnd.x >= anchor.x) {
        Offset(labelEnd.x + pad, labelEnd.y - layout.size.height / 2f)
    } else {
        Offset(labelEnd.x - pad - layout.size.width, labelEnd.y - layout.size.height / 2f)
    }
    drawText(textLayoutResult = layout, topLeft = textTopLeft)
}
