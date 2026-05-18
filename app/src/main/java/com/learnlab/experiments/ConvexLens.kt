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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.MetricChip
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlin.math.abs

/**
 * Convex Lens — Optical Bench.
 * Thin-lens equation: 1/o + 1/i = 1/f → i = o·f / (o − f), m = −i/o
 * Focus quality drives the projected image's blur — 1.0 = sharp, 0 = parallel rays.
 */

private data class LensImage(
    val v: Float,           // image distance (cm), positive number
    val m: Float,           // magnification
    val virtual: Boolean,   // image is virtual (object inside f)
    val parallel: Boolean,  // u == f → image at infinity
)

private fun computeImage(u: Float, f: Float): LensImage {
    if (abs(u - f) < 0.5f) return LensImage(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, false, true)
    val i = (u * f) / (u - f)
    val m = -i / u
    return LensImage(v = abs(i), m = m, virtual = i < 0, parallel = false)
}

private data class SavedLens(
    val id: Long, val f: Float, val u: Float, val d: Float,
    val v: Float, val m: Float, val color: Color,
)
private val LENS_COLORS = listOf(
    Color(0xFF0EA5E9), Color(0xFFA855F7), Color(0xFFF43F5E), Color(0xFFF59E0B),
)

@Composable
fun ConvexLens(controls: ExperimentControls) {
    val t = LL.tokens
    var f by remember { mutableStateOf(20f) }   // focal length, cm
    var u by remember { mutableStateOf(60f) }   // candle distance from lens
    var d by remember { mutableStateOf(30f) }   // screen distance from lens
    var showRays by remember { mutableStateOf(false) }
    val saved = remember { mutableStateListOf<SavedLens>() }

    val result by remember { derivedStateOf { computeImage(u, f) } }
    val focusQuality by remember {
        derivedStateOf {
            if (result.virtual || result.parallel) 0f
            else maxOf(0f, 1f - abs(d - result.v) / 25f)
        }
    }

    var reachedFocusOnce by remember { mutableStateOf(false) }
    LaunchedEffect(focusQuality) {
        if (focusQuality > 0.85f) reachedFocusOnce = true
    }
    LaunchedEffect(u, d, f, saved.size, reachedFocusOnce) {
        var p = 0.2f
        if (u != 60f || d != 30f || f != 20f) p = 0.4f
        if (reachedFocusOnce) p = 0.7f
        if (saved.size >= 2) { p = 1f; controls.onComplete(1f) }
        controls.onProgress(p)
    }

    fun snapToFocus() {
        if (!result.virtual && !result.parallel) d = result.v
    }
    fun save() {
        if (saved.size >= 4) return
        val color = LENS_COLORS[saved.size % LENS_COLORS.size]
        saved.add(SavedLens(System.currentTimeMillis(), f, u, d, result.v, result.m, color))
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Bench ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("OPTICAL BENCH", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText("Move the candle and screen sliders. Find the screen position where the image looks sharp.",
                        color = t.ink400, size = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToggleChipL("Light path", showRays) { showRays = !showRays }
                    FocusMeter(quality = focusQuality)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Scene
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(t.surface2)) {
                BenchScene(
                    f = f, u = u, d = d, result = result,
                    focusQuality = focusQuality, showRays = showRays,
                    saved = saved,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetricChip("candle", "u = ${u.toInt()} cm")
                    MetricChip("screen", "d = ${d.toInt()} cm")
                    val vText = when {
                        result.parallel -> "v = ∞"
                        result.virtual -> "v = %.1f cm (virtual)".format(result.v)
                        else -> "v = %.1f cm".format(result.v)
                    }
                    MetricChip("image", vText)
                    MetricChip("m", if (result.m.isFinite()) "%.2f".format(result.m) else "—")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(label = "Snap to focus",
                        onClick = { snapToFocus() },
                        enabled = !result.virtual && !result.parallel)
                    GhostButton(
                        label = if (saved.size >= 4) "Slots full" else "Save setup",
                        onClick = { save() })
                }
            }
        }

        // ── Right panel ──────────────────────────────────────────
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LensControlsCard(
                f = f, onF = { f = it },
                u = u, onU = { u = it },
                d = d, onD = { d = it },
            )
            HintCard(result = result, focusQuality = focusQuality, d = d)
            FormulaCard(f = f, u = u, v = result.v, m = result.m)
            SavedLensList(saved = saved, onClear = { saved.clear() })
        }
    }
}

/* ──────────────────────── Scene canvas ─────────────────────────── */

@Composable
private fun BenchScene(
    f: Float, u: Float, d: Float,
    result: LensImage, focusQuality: Float, showRays: Boolean,
    saved: List<SavedLens>,
) {
    val t = LL.tokens
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val w = size.width; val h = size.height
        val benchTop = h * 0.72f
        val padL = 60f; val padR = 60f
        val lensX = w * 0.45f

        // Pick pixel-per-cm scale so candle, image and screen all fit.
        val rightExtent = maxOf(
            d + 10f,
            if (result.virtual) 30f else minOf(result.v + 10f, 300f),
            60f,
        )
        val leftExtent = maxOf(u + 10f, 60f)
        val scale = minOf((lensX - padL) / leftExtent, (w - lensX - padR) / rightExtent)

        fun xAt(cmFromLens: Float, leftSide: Boolean) =
            if (leftSide) lensX - cmFromLens * scale else lensX + cmFromLens * scale

        // Sky/background gradient (subtle)
        drawRect(t.surface2, topLeft = Offset(0f, 0f), size = Size(w, benchTop))

        // Bench bar
        drawRect(Color(0xFF94A3B8).copy(alpha = 0.4f),
            topLeft = Offset(0f, benchTop), size = Size(w, h - benchTop))
        // Bench grad ticks every 10 cm
        run {
            var cmRight = 0f
            while (cmRight < rightExtent) {
                val x = xAt(cmRight, leftSide = false)
                drawLine(Color(0xFF64748B), Offset(x, benchTop), Offset(x, benchTop + 8f), strokeWidth = 1f)
                cmRight += 10f
            }
            var cmLeft = 0f
            while (cmLeft < leftExtent) {
                val x = xAt(cmLeft, leftSide = true)
                drawLine(Color(0xFF64748B), Offset(x, benchTop), Offset(x, benchTop + 8f), strokeWidth = 1f)
                cmLeft += 10f
            }
        }
        // Optical axis
        val axisY = benchTop - 60f
        drawLine(Color(0xFF94A3B8), Offset(0f, axisY), Offset(w, axisY),
            strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f)))

        // Lens
        val lensH = 120f
        val lensW = 14f
        drawOval(
            color = Color(0xFFBAE6FD).copy(alpha = 0.7f),
            topLeft = Offset(lensX - lensW, axisY - lensH / 2f),
            size = Size(lensW * 2f, lensH),
        )
        drawLine(Color(0xFF0284C7),
            Offset(lensX, axisY - lensH / 2f),
            Offset(lensX, axisY + lensH / 2f),
            strokeWidth = 2.5f, cap = StrokeCap.Round)
        // F markers
        listOf(-f, f).forEach { fx ->
            val x = lensX + fx * scale
            drawLine(Color(0xFF94A3B8), Offset(x, axisY - 4f), Offset(x, axisY + 4f), strokeWidth = 1.5f)
        }

        // Candle (object) on left
        val candleX = xAt(u, leftSide = true)
        val candleH = 60f
        drawRect(Color(0xFFFCD34D),
            topLeft = Offset(candleX - 4f, axisY - candleH),
            size = Size(8f, candleH))
        drawCircle(Color(0xFFF59E0B), radius = 7f, center = Offset(candleX, axisY - candleH - 6f))
        // Stand
        drawLine(Color(0xFF0F172A), Offset(candleX - 14f, axisY), Offset(candleX + 14f, axisY), strokeWidth = 2.5f)

        // Screen on right
        val screenX = xAt(d, leftSide = false)
        val screenColor = Color(0xFFE2E8F0)
        drawRect(screenColor,
            topLeft = Offset(screenX - 3f, axisY - 90f),
            size = Size(6f, 180f))
        // Screen frame
        drawLine(Color(0xFF334155),
            Offset(screenX, axisY - 90f), Offset(screenX, axisY + 90f),
            strokeWidth = 2f)

        // Projected image on the screen
        if (!result.virtual && !result.parallel) {
            val imgFullH = candleH * abs(result.m)
            // At sharp focus we show a crisp inverted candle silhouette;
            // when out of focus we widen and dim the splash.
            val blur = 1f - focusQuality
            val splashW = 8f + blur * 60f
            val alpha = 0.25f + focusQuality * 0.75f
            val imgTop = axisY - imgFullH.coerceAtMost(160f)
            // Inverted candle (because m < 0 when image is real)
            if (focusQuality > 0.4f) {
                // Crisp inverted candle
                drawRect(Color(0xFFFCD34D).copy(alpha = alpha),
                    topLeft = Offset(screenX - 4f, imgTop),
                    size = Size(8f, axisY - imgTop))
                drawCircle(Color(0xFFF59E0B).copy(alpha = alpha),
                    radius = 5f, center = Offset(screenX, imgTop - 4f))
            } else {
                // Blur splash
                drawOval(
                    color = Color(0xFFF59E0B).copy(alpha = alpha),
                    topLeft = Offset(screenX - splashW / 2f, axisY - imgFullH.coerceAtMost(140f)),
                    size = Size(splashW, imgFullH.coerceAtMost(140f)),
                )
            }
        }

        // Saved ghosts (faint markers at their screen position)
        saved.forEach { s ->
            val gx = xAt(s.d, leftSide = false)
            drawLine(s.color.copy(alpha = 0.7f),
                Offset(gx, axisY - 70f), Offset(gx, axisY + 70f),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))
        }

        // Ray overlay
        if (showRays && !result.virtual && !result.parallel) {
            val candleTopY = axisY - candleH
            val imageX = xAt(result.v, leftSide = false)
            val imageH = candleH * abs(result.m)
            val imageTopY = axisY - 0f
            val imageBottomY = if (result.m < 0f) axisY + imageH else axisY - imageH
            // Ray 1: parallel to axis → through F on far side
            drawLine(Color(0xFF10B981).copy(alpha = 0.8f),
                Offset(candleX, candleTopY), Offset(lensX, candleTopY),
                strokeWidth = 1.5f)
            drawLine(Color(0xFF10B981).copy(alpha = 0.8f),
                Offset(lensX, candleTopY), Offset(imageX, imageBottomY),
                strokeWidth = 1.5f)
            // Ray 2: through optical centre, undeviated
            drawLine(Color(0xFFA855F7).copy(alpha = 0.8f),
                Offset(candleX, candleTopY), Offset(imageX, imageBottomY),
                strokeWidth = 1.5f)
            // Ray 3: through F on object side → parallel to axis after lens
            val fLeft = xAt(f, leftSide = true)
            drawLine(Color(0xFFEF4444).copy(alpha = 0.8f),
                Offset(candleX, candleTopY), Offset(lensX, candleTopY * 0.6f + axisY * 0.4f),
                strokeWidth = 1.5f)
            drawLine(Color(0xFFEF4444).copy(alpha = 0.8f),
                Offset(lensX, candleTopY * 0.6f + axisY * 0.4f),
                Offset(imageX, candleTopY * 0.6f + axisY * 0.4f),
                strokeWidth = 1.5f)
            // markers for F points (left and right)
            drawCircle(Color(0xFF94A3B8), 3f, Offset(fLeft, axisY))
            drawCircle(Color(0xFF94A3B8), 3f, Offset(lensX + f * scale, axisY))
        }
    }
}

/* ──────────────────────── Right panel cards ──────────────────── */

@Composable
private fun LensControlsCard(
    f: Float, onF: (Float) -> Unit,
    u: Float, onU: (Float) -> Unit,
    d: Float, onD: (Float) -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LLText("LENS & BENCH", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLSlider("Focal length (f)", f, onF, 10f, 50f, 1f, "cm",
            valueFormat = { "%.0f".format(it) })
        LLSlider("Object distance (u)", u, onU, 5f, 200f, 1f, "cm",
            valueFormat = { "%.0f".format(it) })
        LLSlider("Screen distance (d)", d, onD, 5f, 200f, 1f, "cm",
            valueFormat = { "%.0f".format(it) })
    }
}

@Composable
private fun HintCard(result: LensImage, focusQuality: Float, d: Float) {
    val t = LL.tokens
    val (title, body, bg, fg) = when {
        result.parallel ->
            QuadHint("OBJECT AT FOCAL POINT",
                "Rays emerge parallel — no real image. Move the candle away from f.",
                t.amber50, t.amber700)
        result.virtual ->
            QuadHint("VIRTUAL IMAGE",
                "Object is inside the focal length. The image is virtual — it can't be cast onto a screen. Move the candle past f.",
                t.amber50, t.amber700)
        focusQuality > 0.9f ->
            QuadHint("SHARP FOCUS",
                "Screen sits at the image plane. The candle's image is in clear focus.",
                t.accent50, t.accent700)
        focusQuality > 0.5f ->
            QuadHint("CLOSE — NUDGE THE SCREEN",
                "Try sliding the screen a few cm closer or further. The image plane is at v = %.1f cm.".format(result.v),
                t.surface2, t.ink400)
        else ->
            QuadHint("OUT OF FOCUS",
                "Use ‘Snap to focus’ to send the screen to the image plane, or move it to about %.0f cm.".format(result.v),
                t.surface2, t.ink400)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText(title, color = fg, size = 11.sp,
            weight = FontWeight.Bold, letterSpacing = 1.6.sp)
        LLText(body, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
    }
    // d referenced so compiler keeps the parameter
    @Suppress("UNUSED_EXPRESSION") d
}

private data class QuadHint(val title: String, val body: String, val bg: Color, val fg: Color)

@Composable
private fun FormulaCard(f: Float, u: Float, v: Float, m: Float) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText("THIN-LENS EQUATION", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface2)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Column {
                LLText("1/v − 1/u = 1/f", color = t.ink400, size = 11.sp)
                LLText("v = u·f / (u − f)", color = t.ink400, size = 11.sp)
                if (v.isFinite()) {
                    LLText("v = %.0f·%.0f / (%.0f − %.0f) = %.1f cm".format(u, f, u, f, v),
                        color = t.ink50, size = 11.sp, weight = FontWeight.SemiBold)
                }
                LLText("m = −v / u = %s".format(if (m.isFinite()) "%.2f".format(m) else "—"),
                    color = t.ink200, size = 11.sp)
            }
        }
    }
}

@Composable
private fun SavedLensList(saved: List<SavedLens>, onClear: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            LLText("Saved setups (${saved.size} / 4)",
                color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold)
            if (saved.isNotEmpty()) {
                Box(modifier = Modifier.clickable { onClear() }) {
                    LLText("Clear", color = t.ink500, size = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (saved.isEmpty()) {
            LLText("Save a setup once you've found a sharp focus.",
                color = t.ink500, size = 11.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(saved, key = { it.id }) { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(t.surface2)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                        Spacer(Modifier.width(8.dp))
                        LLText(
                            "f=${s.f.toInt()} · u=${s.u.toInt()} · d=${s.d.toInt()}",
                            color = t.ink200, size = 11.sp, modifier = Modifier.weight(1f),
                        )
                        LLText("v=%.1f m=%.2f".format(s.v, s.m),
                            color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusMeter(quality: Float) {
    val t = LL.tokens
    val pct = (quality * 100f).toInt()
    val color = when {
        quality > 0.85f -> t.accent700
        quality > 0.5f -> t.amber700
        else -> t.ink500
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(t.surface2)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Row {
            LLText("Focus: ", color = t.ink500, size = 11.sp)
            LLText("$pct%", color = color, size = 11.sp, weight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleChipL(label: String, on: Boolean, onToggle: () -> Unit) {
    val t = LL.tokens
    val bg = if (on) t.accent50 else t.surface2
    val fg = if (on) t.accent700 else t.ink500
    val border = if (on) t.accent500 else t.line
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) { LLText(label, color = fg, size = 11.sp, weight = FontWeight.SemiBold) }
}

// quiet IDE
@Suppress("unused")
private val _kept = Rect.Zero
