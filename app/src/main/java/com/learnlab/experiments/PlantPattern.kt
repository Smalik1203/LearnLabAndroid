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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.ExperimentControls

/**
 * Plant Combinator. 3 binary features × 2 = 8 combinations.
 * Only 2 are valid (monocot, dicot). Student flips dials, sees a live plant
 * visualization, and tracks which combos they've tried.
 */

private enum class Cot { ONE, TWO }
private enum class Ven { PARALLEL, RETICULATE }
private enum class Root { FIBROUS, TAPROOT }

private data class Combo(val cot: Cot, val ven: Ven, val root: Root) {
    fun key() = "${cot.name}-${ven.name}-${root.name}"
}

private val MONOCOT = Combo(Cot.ONE, Ven.PARALLEL, Root.FIBROUS)
private val DICOT   = Combo(Cot.TWO, Ven.RETICULATE, Root.TAPROOT)

private enum class Verdict { MONOCOT, DICOT, MISMATCH }
private fun classify(c: Combo): Verdict = when (c) {
    MONOCOT -> Verdict.MONOCOT
    DICOT   -> Verdict.DICOT
    else    -> Verdict.MISMATCH
}

private val ALL_COMBOS: List<Combo> = buildList {
    for (cot in Cot.entries) for (ven in Ven.entries) for (root in Root.entries) add(Combo(cot, ven, root))
}

@Composable
fun PlantPattern(controls: ExperimentControls) {
    val t = LL.tokens
    var combo by remember { mutableStateOf(Combo(Cot.ONE, Ven.PARALLEL, Root.FIBROUS)) }
    val seen = remember { mutableStateMapOf<String, Boolean>().apply { put(combo.key(), true) } }

    val seenMono by remember { derivedStateOf { seen.containsKey(MONOCOT.key()) } }
    val seenDi by remember { derivedStateOf { seen.containsKey(DICOT.key()) } }

    LaunchedEffect(seen.size, seenMono, seenDi) {
        controls.onProgress(seen.size / ALL_COMBOS.size.toFloat())
        if (seenMono && seenDi) controls.onComplete(1f)
    }

    fun update(next: Combo) {
        combo = next
        seen[next.key()] = true
    }

    val verdict = classify(combo)

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Left: plant + dials
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                LLText("PLANT COMBINATOR", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(4.dp))
                LLText(
                    "Flip the three feature dials. See what kind of plant you've described — or if the combination doesn't exist in nature.",
                    color = t.ink400, size = 13.sp, lineHeight = 18.sp,
                )
            }
            // Plant canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                PlantCanvas(combo)
            }
            // Dials
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Dial(
                    label = "Cotyledons",
                    options = listOf("1 (one half)" to Cot.ONE, "2 (splits)" to Cot.TWO),
                    selected = combo.cot,
                    onSelect = { update(combo.copy(cot = it)) },
                    modifier = Modifier.weight(1f),
                )
                Dial(
                    label = "Leaf venation",
                    options = listOf("Parallel" to Ven.PARALLEL, "Reticulate" to Ven.RETICULATE),
                    selected = combo.ven,
                    onSelect = { update(combo.copy(ven = it)) },
                    modifier = Modifier.weight(1f),
                )
                Dial(
                    label = "Roots",
                    options = listOf("Fibrous" to Root.FIBROUS, "Taproot" to Root.TAPROOT),
                    selected = combo.root,
                    onSelect = { update(combo.copy(root = it)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Right: verdict + discoveries
        Column(
            modifier = Modifier.width(360.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VerdictCard(verdict)
            DiscoveriesCard(seen.keys, combo)
        }
    }
}

@Composable
private fun <T> Dial(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        LLText(label.uppercase(), color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { (text, value) ->
                val on = value == selected
                val bg = if (on) t.accent600 else t.surface2
                val fg = if (on) Color.White else t.ink200
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .clickable { onSelect(value) }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LLText(text, color = fg, size = 11.sp, weight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun VerdictCard(v: Verdict) {
    val t = LL.tokens
    val (titleText, bodyText, examples, borderColor, bg, accent) = when (v) {
        Verdict.MONOCOT -> Sextuple(
            "VALID MONOCOT",
            "One cotyledon, parallel-veined leaves, fibrous roots — a real plant body plan.",
            listOf("🌾 Wheat", "🌽 Maize", "🍌 Banana", "🎋 Grass"),
            t.accent500, t.accent50, t.accent700,
        )
        Verdict.DICOT -> Sextuple(
            "VALID DICOT",
            "Two cotyledons, reticulate-veined leaves, taproot — another real plant body plan.",
            listOf("🌳 Mango", "🌺 Hibiscus", "🫘 Chickpea", "🪴 Mustard"),
            t.accent500, t.accent50, t.accent700,
        )
        Verdict.MISMATCH -> Sextuple(
            "INCONSISTENT COMBO",
            "This combination doesn't occur naturally. In real plants, the three features travel together: 1 + parallel + fibrous (monocot) or 2 + reticulate + taproot (dicot).",
            emptyList(),
            t.rose300, t.rose50, t.rose700,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(titleText, color = accent, size = 11.sp,
            weight = FontWeight.Bold, letterSpacing = 1.8.sp)
        LLText(bodyText, color = t.ink200, size = 13.sp, lineHeight = 18.sp)
        if (examples.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            LLText("Examples in nature:", color = t.ink500, size = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                examples.forEach { ex ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(t.surface)
                            .border(1.dp, t.line, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { LLText(ex, color = t.ink200, size = 11.sp) }
                }
            }
        }
    }
}

private data class Sextuple<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F,
)
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component1() = a
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component2() = b
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component3() = c
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component4() = d
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component5() = e
private operator fun <A, B, C, D, E, F> Sextuple<A, B, C, D, E, F>.component6() = f

@Composable
private fun DiscoveriesCard(seenKeys: Set<String>, current: Combo) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LLText("Combos tried", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
            LLText("${seenKeys.size} / 8", color = t.ink500, size = 12.sp)
        }
        LLText("Goal: find both valid plant groups by experimenting with the dials.",
            color = t.ink500, size = 11.sp, lineHeight = 15.sp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ALL_COMBOS.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pair.forEach { c ->
                        val key = c.key()
                        val tried = seenKeys.contains(key)
                        val isCurrent = key == current.key()
                        val v = classify(c)
                        val bg = when {
                            !tried -> t.surface
                            v == Verdict.MISMATCH -> t.surface2
                            else -> t.accent50
                        }
                        val fg = when {
                            !tried -> t.ink600
                            v == Verdict.MISMATCH -> t.ink400
                            else -> t.accent700
                        }
                        val border = if (isCurrent) t.accent500 else t.line
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        ) {
                            LLText(
                                "${c.cot.label()}cot · ${c.ven.short()} · ${c.root.short()}",
                                color = fg, size = 10.sp, weight = FontWeight.SemiBold,
                            )
                            LLText(
                                if (!tried) "—" else when (v) {
                                    Verdict.MISMATCH -> "Mismatch"
                                    Verdict.MONOCOT -> "monocot"
                                    Verdict.DICOT -> "dicot"
                                },
                                color = fg, size = 10.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Cot.label() = if (this == Cot.ONE) "1" else "2"
private fun Ven.short() = if (this == Ven.PARALLEL) "para" else "reti"
private fun Root.short() = if (this == Root.FIBROUS) "fibr" else "tap"

/* ──────────────────────────── plant canvas ──────────────────── */

@Composable
private fun PlantCanvas(combo: Combo) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val cx = w / 2f; val cy = h / 2f
            val scale = minOf(w, h) / 320f

            // Soil tint
            drawRect(
                color = Color(0xFFFEF3C7).copy(alpha = 0.4f),
                topLeft = Offset(0f, h * 0.55f),
                size = Size(w, h * 0.45f),
            )

            // Stem
            drawRect(
                color = Color(0xFF16A34A),
                topLeft = Offset(cx - 5f * scale, cy - 30f * scale),
                size = Size(10f * scale, 80f * scale),
            )

            // Leaves
            drawLeaf(center = Offset(cx - 40f * scale, cy - 30f * scale), angleDeg = -25f, ven = combo.ven, scale = scale)
            drawLeaf(center = Offset(cx + 40f * scale, cy - 30f * scale), angleDeg = 25f, ven = combo.ven, scale = scale)

            // Seed (above stem)
            drawSeed(center = Offset(cx, cy - 60f * scale), cot = combo.cot, scale = scale)

            // Roots (below stem)
            drawRoots(base = Offset(cx, cy + 50f * scale), root = combo.root, scale = scale)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    center: Offset, angleDeg: Float, ven: Ven, scale: Float,
) {
    val rx = 44f * scale; val ry = 22f * scale
    val veinColor = Color(0xFF475569)
    val midGreen = Color(0xFFBBF7D0)
    val midGreenStroke = Color(0xFF16A34A)

    // Rotate around center: use translate + rotate via drawIntoCanvas
    drawIntoCanvas { c ->
        c.save()
        c.translate(center.x, center.y)
        c.rotate(angleDeg)
        // ellipse fill via path
        val p = Path().apply { addOval(androidx.compose.ui.geometry.Rect(-rx, -ry, rx, ry)) }
        drawPath(p, midGreen)
        drawPath(p, midGreenStroke, style = Stroke(1.5f * scale))
        // mid vein
        drawLine(veinColor, Offset(-rx, 0f), Offset(rx, 0f), strokeWidth = 1.5f * scale, cap = StrokeCap.Round)
        if (ven == Ven.RETICULATE) {
            val s = 0.8f * scale
            listOf(
                listOf(Offset(-30f, 0f), Offset(-25f, -10f), Offset(-10f, -16f)),
                listOf(Offset(-30f, 0f), Offset(-25f, 10f),  Offset(-10f, 16f)),
                listOf(Offset(-10f, 0f), Offset(0f, -10f),   Offset(15f, -14f)),
                listOf(Offset(-10f, 0f), Offset(0f, 10f),    Offset(15f, 14f)),
                listOf(Offset(15f, 0f),  Offset(25f, -8f),   Offset(35f, -10f)),
                listOf(Offset(15f, 0f),  Offset(25f, 8f),    Offset(35f, 10f)),
            ).forEach { pts ->
                val path = Path().apply {
                    moveTo(pts[0].x * scale, pts[0].y * scale)
                    quadraticBezierTo(pts[1].x * scale, pts[1].y * scale, pts[2].x * scale, pts[2].y * scale)
                }
                drawPath(path, veinColor, style = Stroke(s))
            }
        } else {
            val s = 0.8f * scale
            listOf(-10f, -5f, 5f, 10f).forEach { y ->
                val path = Path().apply {
                    moveTo(-42f * scale, y * scale)
                    quadraticBezierTo(0f, (y * 0.6f) * scale, 42f * scale, y * scale)
                }
                drawPath(path, veinColor, style = Stroke(s))
            }
        }
        c.restore()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeed(center: Offset, cot: Cot, scale: Float) {
    val fill = Color(0xFFFCD34D); val stroke = Color(0xFF92400E)
    val rx = 9f * scale; val ry = 14f * scale
    if (cot == Cot.TWO) {
        // two halves
        val gap = 8f * scale
        listOf(-gap, gap).forEach { dx ->
            val rect = androidx.compose.ui.geometry.Rect(
                center.x + dx - rx, center.y - ry,
                center.x + dx + rx, center.y + ry,
            )
            val p = Path().apply { addOval(rect) }
            drawPath(p, fill)
            drawPath(p, stroke, style = Stroke(1.5f * scale))
        }
    } else {
        val rect = androidx.compose.ui.geometry.Rect(
            center.x - rx, center.y - 16f * scale,
            center.x + rx, center.y + 16f * scale,
        )
        val p = Path().apply { addOval(rect) }
        drawPath(p, fill)
        drawPath(p, stroke, style = Stroke(1.5f * scale))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoots(base: Offset, root: Root, scale: Float) {
    val color = Color(0xFF92400E)
    val s = (if (root == Root.TAPROOT) 2.5f else 1.8f) * scale
    if (root == Root.TAPROOT) {
        // Central down + side roots
        drawLine(color, base, Offset(base.x, base.y + 100f * scale), strokeWidth = s, cap = StrokeCap.Round)
        listOf(
            Triple(base.x, base.y + 20f * scale, Offset(base.x - 30f * scale, base.y + 45f * scale)),
            Triple(base.x, base.y + 20f * scale, Offset(base.x + 30f * scale, base.y + 45f * scale)),
            Triple(base.x, base.y + 50f * scale, Offset(base.x - 35f * scale, base.y + 72f * scale)),
            Triple(base.x, base.y + 50f * scale, Offset(base.x + 35f * scale, base.y + 72f * scale)),
            Triple(base.x, base.y + 80f * scale, Offset(base.x - 20f * scale, base.y + 95f * scale)),
            Triple(base.x, base.y + 80f * scale, Offset(base.x + 20f * scale, base.y + 95f * scale)),
        ).forEach { (sx, sy, end) ->
            val p = Path().apply {
                moveTo(sx, sy)
                quadraticBezierTo(sx, (sy + end.y) / 2f, end.x, end.y)
            }
            drawPath(p, color, style = Stroke(s, cap = StrokeCap.Round))
        }
    } else {
        // Fibrous: many similar curves
        listOf(-55f, -35f, -15f, 0f, 15f, 35f, 55f).forEach { dx ->
            val end = Offset(base.x + dx * scale, base.y + 85f * scale)
            val p = Path().apply {
                moveTo(base.x, base.y)
                quadraticBezierTo(base.x + (dx * 0.6f) * scale, base.y + 35f * scale, end.x, end.y)
            }
            drawPath(p, color, style = Stroke(s, cap = StrokeCap.Round))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIntoCanvas(block: (androidx.compose.ui.graphics.Canvas) -> Unit) {
    block(drawContext.canvas)
}

@Suppress("unused")
private val _kept = Modifier.rotate(0f)
