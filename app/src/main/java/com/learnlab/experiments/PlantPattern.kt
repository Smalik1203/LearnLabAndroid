package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.learnlab.design.PrimaryButton
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.bounceClickable
import com.learnlab.store.ExperimentControls

/**
 * Plant Pattern — NCERT Activity 2.7 ("Let us relate and analyse").
 *
 * Students recall what they saw in the venation lab (2.5) and the root lab (2.6)
 * and fill an interactive Table 2.4 for five nursery saplings. Each plant shows
 * its leaf and its root system; the class taps two cells — venation and root type.
 *
 * Phase 0 — fill the table (onStep 0).
 * Phase 1 — table complete: cells colour-code and the rule appears (onStep 1).
 * Phase 2 — predict a new plant's root type from its venation (onStep 2).
 *
 * Colour convention is shared with LeafVenationLab: violet = reticulate,
 * sky = parallel.
 */

private enum class VenationKind { Reticulate, Parallel }
private enum class RootType { Taproot, Fibrous }

private val reticulateColor = Color(0xFFA855F7)
private val parallelColor = Color(0xFF0EA5E9)
private val correctColor = Color(0xFF10B981)
private val wrongColor = Color(0xFFF43F5E)

private fun venColor(v: VenationKind) = if (v == VenationKind.Reticulate) reticulateColor else parallelColor
private fun rootColor(r: RootType) = if (r == RootType.Taproot) reticulateColor else parallelColor

private class PlantSpec(
    val name: String,
    val venation: VenationKind,
    val root: RootType,
)

// Table 2.4 cast. Lemongrass is the book's worked example (parallel + fibrous);
// sadabahar and chickpea are the book's reticulate + taproot examples; wheat is
// fibrous + parallel. These plants also appear in the venation and root labs.
private val plants = listOf(
    PlantSpec("Lemongrass", VenationKind.Parallel, RootType.Fibrous),
    PlantSpec("Marigold", VenationKind.Reticulate, RootType.Taproot),
    PlantSpec("Sadabahar", VenationKind.Reticulate, RootType.Taproot),
    PlantSpec("Chickpea", VenationKind.Reticulate, RootType.Taproot),
    PlantSpec("Wheat", VenationKind.Parallel, RootType.Fibrous),
)

private const val TOTAL_CELLS = 10 // 5 plants × 2 cells

@Composable
fun PlantPattern(controls: ExperimentControls) {
    val t = LL.tokens

    val venGuess = remember { mutableStateMapOf<Int, VenationKind>() }
    val rootGuess = remember { mutableStateMapOf<Int, RootType>() }

    var phase by remember { mutableStateOf(0) } // 0 fill, 1 pattern, 2 predict
    var predictTapped by remember { mutableStateOf(false) }
    // Set when "Spot the pattern" is tapped while some cells contradict the
    // plant drawings — the table must be true before the rule is revealed,
    // otherwise the colour-coding would endorse a wrong fact.
    var showErrors by remember { mutableStateOf(false) }

    val filledCells by remember {
        derivedStateOf { venGuess.size + rootGuess.size }
    }
    val tableComplete by remember {
        derivedStateOf { venGuess.size == plants.size && rootGuess.size == plants.size }
    }
    val tableTrue by remember {
        derivedStateOf {
            plants.indices.all { i ->
                venGuess[i] == plants[i].venation && rootGuess[i] == plants[i].root
            }
        }
    }

    LaunchedEffect(phase) { controls.onStep(phase) }

    LaunchedEffect(filledCells, phase) {
        if (phase == 0) controls.onProgress(filledCells / TOTAL_CELLS.toFloat())
    }

    LaunchedEffect(predictTapped) {
        if (predictTapped) controls.onComplete(1f)
    }

    Row(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Table stage
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    LLText(
                        "TABLE 2.4 — VENATION & ROOTS",
                        color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    LLText(
                        "Recall each plant from the leaf and root labs, then tap its two cells.",
                        color = t.ink400, size = 13.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot("Reticulate · taproot", reticulateColor)
                    LegendDot("Parallel · fibrous", parallelColor)
                }
            }

            HeaderRow()

            plants.forEachIndexed { i, plant ->
                PlantRow(
                    plant = plant,
                    ven = venGuess[i],
                    root = rootGuess[i],
                    colorCoded = phase >= 1,
                    venFlagged = showErrors && venGuess[i] != plant.venation,
                    rootFlagged = showErrors && rootGuess[i] != plant.root,
                    onVen = { if (phase == 0) venGuess[i] = it },
                    onRoot = { if (phase == 0) rootGuess[i] = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Side panel
        Column(
            modifier = Modifier.width(340.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (phase) {
                0 -> FillPanel(
                    filled = filledCells,
                    complete = tableComplete,
                    showErrors = showErrors,
                    onSpotPattern = {
                        if (tableTrue) { showErrors = false; phase = 1 }
                        else showErrors = true
                    },
                )
                1 -> PatternPanel(onPredict = { phase = 2 })
                2 -> PredictPanel(
                    tapped = predictTapped,
                    onTap = { predictTapped = true },
                )
            }
        }
    }
}

/* ─────────────── Table ─────────────── */

@Composable
private fun HeaderRow() {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(t.surface2)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell("Plant", Modifier.weight(1.1f))
        HeaderCell("Leaf venation", Modifier.weight(1.3f))
        HeaderCell("Root type", Modifier.weight(1.3f))
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    val t = LL.tokens
    Box(modifier = modifier) {
        LLText(text.uppercase(), color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
    }
}

@Composable
private fun PlantRow(
    plant: PlantSpec,
    ven: VenationKind?,
    root: RootType?,
    colorCoded: Boolean,
    venFlagged: Boolean = false,
    rootFlagged: Boolean = false,
    onVen: (VenationKind) -> Unit,
    onRoot: (RootType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    // When the table is complete the pattern colour fills the whole row.
    val rowTint = if (colorCoded && ven != null) venColor(ven).copy(alpha = 0.06f) else Color.Transparent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(rowTint)
            .border(1.dp, t.line, RoundedCornerShape(Radius.md))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Plant: name + tiny leaf/root visual
        Row(
            modifier = Modifier.weight(1.1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(modifier = Modifier.size(54.dp)) {
                drawPlantGlyph(plant.venation, plant.root)
            }
            Spacer(Modifier.width(8.dp))
            LLText(plant.name, color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
        }

        // VenationKind toggle
        Row(modifier = Modifier.weight(1.3f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ToggleCell("Reticulate", reticulateColor,
                selected = ven == VenationKind.Reticulate,
                locked = colorCoded,
                flagged = venFlagged && ven == VenationKind.Reticulate,
                onClick = { onVen(VenationKind.Reticulate) }, modifier = Modifier.weight(1f))
            ToggleCell("Parallel", parallelColor,
                selected = ven == VenationKind.Parallel,
                locked = colorCoded,
                flagged = venFlagged && ven == VenationKind.Parallel,
                onClick = { onVen(VenationKind.Parallel) }, modifier = Modifier.weight(1f))
        }

        // Root toggle
        Row(modifier = Modifier.weight(1.3f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ToggleCell("Taproot", reticulateColor,
                selected = root == RootType.Taproot,
                locked = colorCoded,
                flagged = rootFlagged && root == RootType.Taproot,
                onClick = { onRoot(RootType.Taproot) }, modifier = Modifier.weight(1f))
            ToggleCell("Fibrous", parallelColor,
                selected = root == RootType.Fibrous,
                locked = colorCoded,
                flagged = rootFlagged && root == RootType.Fibrous,
                onClick = { onRoot(RootType.Fibrous) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToggleCell(
    label: String,
    color: Color,
    selected: Boolean,
    locked: Boolean,
    flagged: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val bg = if (selected) color.copy(alpha = if (locked) 0.18f else 0.12f) else t.surface2
    val border = when {
        flagged -> wrongColor
        selected -> color
        else -> t.line
    }
    val fg = if (selected) color else t.ink400
    Box(
        modifier = modifier
            .fillMaxHeight()
            .bounceClickable(enabled = !locked, onClick = onClick)
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(Radius.sm))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = fg, size = 13.sp,
            weight = if (selected) FontWeight.Bold else FontWeight.SemiBold)
    }
}

/* ─────────────── Side panels ─────────────── */

@Composable
private fun FillPanel(filled: Int, complete: Boolean, showErrors: Boolean, onSpotPattern: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LLText("FILL THE TABLE", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLText(
            "For each sapling, the class agrees on the leaf's venation and the shape of its roots — exactly what you watched in the last two labs.",
            color = t.ink200, size = 14.sp, lineHeight = 20.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(t.surface2)
                .padding(14.dp),
        ) {
            LLText("$filled of $TOTAL_CELLS cells filled",
                color = t.ink200, size = 14.sp, weight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
        if (complete) {
            if (showErrors) {
                LLText("Some cells don't match the plants. Look at each drawing again and fix the outlined cells.",
                    color = wrongColor, size = 13.sp, weight = FontWeight.SemiBold, lineHeight = 18.sp)
            } else {
                LLText("Table full. Now look down the two columns together.",
                    color = correctColor, size = 13.sp, weight = FontWeight.SemiBold, lineHeight = 18.sp)
            }
            PrimaryButton(
                label = "Spot the pattern",
                onClick = onSpotPattern,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LLText("Tap both cells for every row to continue.",
                color = t.ink500, size = 12.sp)
        }
    }
}

@Composable
private fun PatternPanel(onPredict: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LLText("TWO CLUES, ONE RULE", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        RuleLine(
            color = reticulateColor,
            head = "Net-veined leaf",
            tail = "→ taproot",
        )
        RuleLine(
            color = parallelColor,
            head = "Side-by-side veins",
            tail = "→ fibrous roots",
        )
        LLText(
            "The two columns line up every time. A leaf's venation tells you, without digging, what kind of roots the plant grows below.",
            color = t.ink200, size = 14.sp, lineHeight = 20.sp,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            label = "Test it on a new plant",
            onClick = onPredict,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RuleLine(color: Color, head: String, tail: String) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        LLText(head, color = t.ink50, size = 14.sp, weight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        LLText(tail, color = color, size = 14.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun PredictPanel(tapped: Boolean, onTap: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LLText("NEW PLANT — HIBISCUS", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(t.surface2)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(96.dp)) {
                drawPlantGlyph(VenationKind.Reticulate, RootType.Taproot, rootsHidden = !tapped)
            }
        }
        LLText(
            "Its leaf is net-veined — reticulate. The roots are still buried. What will they be?",
            color = t.ink200, size = 14.sp, lineHeight = 20.sp,
        )
        Spacer(Modifier.weight(1f))
        if (!tapped) {
            PrimaryButton(
                label = "Reveal: taproot",
                onClick = onTap,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(correctColor.copy(alpha = 0.10f))
                    .border(1.dp, correctColor.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                    .padding(14.dp),
            ) {
                LLText("Taproot — the rule holds.",
                    color = correctColor, size = 16.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LLText("Net veins above, one main root below. You read the roots from the leaf.",
                    color = t.ink200, size = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        LLText(label, color = color, size = 12.sp, weight = FontWeight.Bold)
    }
}

/* ─────────────── Plant glyph (leaf above, roots below) ─────────────── */

private fun DrawScope.drawPlantGlyph(
    venation: VenationKind,
    root: RootType,
    rootsHidden: Boolean = false,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val soilY = h * 0.5f
    val scale = minOf(w, h) / 60f

    val stemColor = Color(0xFF16A34A)
    val rootColor = Color(0xFF92400E)

    // Stem
    drawLine(stemColor, Offset(cx, h * 0.16f), Offset(cx, soilY),
        strokeWidth = 2.2f * scale, cap = StrokeCap.Round)

    // Leaf (a small blade at the top with veins matching the type)
    drawGlyphLeaf(Offset(cx, h * 0.16f), venation, scale)

    if (!rootsHidden) {
        drawGlyphRoots(Offset(cx, soilY), root, rootColor, scale)
    } else {
        // A few dashes hint that roots are still underground.
        val dash = Color(0xFF94A3B8)
        listOf(-0.18f, 0f, 0.18f).forEach { dx ->
            drawLine(dash,
                Offset(cx + dx * w, soilY + h * 0.18f),
                Offset(cx + dx * w, soilY + h * 0.30f),
                strokeWidth = 1.4f * scale, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawGlyphLeaf(top: Offset, venation: VenationKind, scale: Float) {
    val leafColor = Color(0xFFB2DFB2).copy(alpha = 0.55f)
    val outline = Color(0xFF1F6B3A)
    val veinColor = if (venation == VenationKind.Reticulate) Color(0xFFA855F7) else Color(0xFF0EA5E9)
    val lw = 14f * scale
    val lh = 18f * scale
    val cx = top.x
    val cy = top.y - lh * 0.4f

    val blade = Path().apply {
        moveTo(cx, cy - lh / 2f)
        cubicTo(cx + lw / 2f, cy - lh / 4f, cx + lw / 2f, cy + lh / 4f, cx, cy + lh / 2f)
        cubicTo(cx - lw / 2f, cy + lh / 4f, cx - lw / 2f, cy - lh / 4f, cx, cy - lh / 2f)
        close()
    }
    drawPath(blade, leafColor)
    drawPath(blade, outline, style = Stroke(1.2f * scale))

    // Midrib
    drawLine(veinColor, Offset(cx, cy - lh / 2f + 2f), Offset(cx, cy + lh / 2f - 2f),
        strokeWidth = 1.2f * scale, cap = StrokeCap.Round)

    if (venation == VenationKind.Reticulate) {
        listOf(-0.22f, 0.05f, 0.3f).forEach { tNorm ->
            val y = cy + lh * tNorm
            val spread = lw * 0.38f * (1f - tNorm * 0.4f)
            drawLine(veinColor.copy(alpha = 0.8f), Offset(cx, y), Offset(cx + spread, y + lh * 0.12f),
                strokeWidth = 0.9f * scale, cap = StrokeCap.Round)
            drawLine(veinColor.copy(alpha = 0.8f), Offset(cx, y), Offset(cx - spread, y + lh * 0.12f),
                strokeWidth = 0.9f * scale, cap = StrokeCap.Round)
        }
    } else {
        listOf(-0.28f, -0.12f, 0.12f, 0.28f).forEach { f ->
            val ox = lw * f
            val p = Path().apply {
                moveTo(cx + ox * 0.3f, cy - lh / 2f + 3f)
                cubicTo(cx + ox, cy - lh * 0.1f, cx + ox, cy + lh * 0.1f, cx + ox * 0.3f, cy + lh / 2f - 3f)
            }
            drawPath(p, veinColor.copy(alpha = 0.8f), style = Stroke(0.9f * scale, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawGlyphRoots(base: Offset, root: RootType, color: Color, scale: Float) {
    if (root == RootType.Taproot) {
        val s = 2.2f * scale
        drawLine(color, base, Offset(base.x, base.y + 26f * scale), strokeWidth = s, cap = StrokeCap.Round)
        listOf(
            8f to -9f, 8f to 9f, 16f to -8f, 16f to 8f,
        ).forEach { (sy, dx) ->
            val p = Path().apply {
                moveTo(base.x, base.y + sy * scale)
                quadraticTo(base.x + dx * 0.5f * scale, base.y + (sy + 8f) * scale,
                    base.x + dx * scale, base.y + (sy + 12f) * scale)
            }
            drawPath(p, color, style = Stroke(1.4f * scale, cap = StrokeCap.Round))
        }
    } else {
        val s = 1.6f * scale
        listOf(-12f, -7f, -2f, 3f, 8f, 13f).forEach { dx ->
            val p = Path().apply {
                moveTo(base.x, base.y)
                quadraticTo(base.x + dx * 0.6f * scale, base.y + 12f * scale,
                    base.x + dx * scale, base.y + 24f * scale)
            }
            drawPath(p, color, style = Stroke(s, cap = StrokeCap.Round))
        }
    }
}
