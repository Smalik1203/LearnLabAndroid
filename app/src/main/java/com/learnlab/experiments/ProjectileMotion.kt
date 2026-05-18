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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.MetricChip
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Projectile Motion Lab — vacuum projectile, sliders for v₀, θ, h, g.
 * Save shots to overlay them on the next launch. Target band on the ground.
 *
 *   R = v₀² sin(2θ) / g          (when h = 0)
 *   H = v₀² sin²(θ) / 2g + h
 *   T = (v₀ sinθ + √(v₀² sin²θ + 2gh)) / g
 */

private data class SavedTrail(
    val id: Long,
    val v0: Float, val angleDeg: Float, val h0: Float, val gravity: Float,
    val range: Float, val maxH: Float, val flightT: Float,
    val color: Color,
)

private val TRAIL_COLORS = listOf(
    Color(0xFF10B981), Color(0xFF0EA5E9), Color(0xFFA855F7),
    Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF14B8A6),
)
private val GRAVITY_PRESETS = listOf(
    GravityPreset2("Earth", 9.8f),
    GravityPreset2("Moon", 1.62f),
    GravityPreset2("Mars", 3.71f),
    GravityPreset2("Jupiter", 24.79f),
)
private data class GravityPreset2(val label: String, val g: Float)

@Composable
fun ProjectileMotion(controls: ExperimentControls) {
    val t = LL.tokens

    var v0 by remember { mutableStateOf(30f) }       // m/s
    var angleDeg by remember { mutableStateOf(45f) } // °
    var h0 by remember { mutableStateOf(0f) }        // m
    var g by remember { mutableStateOf(9.8f) }       // m/s²
    var target by remember { mutableStateOf(80f) }   // m
    var showVectors by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }

    var simT by remember { mutableStateOf(0f) }
    var playing by remember { mutableStateOf(false) }
    val trails = remember { mutableStateListOf<SavedTrail>() }

    val rad by remember { derivedStateOf { angleDeg * PI.toFloat() / 180f } }
    val vx0 by remember { derivedStateOf { v0 * cos(rad) } }
    val vy0 by remember { derivedStateOf { v0 * sin(rad) } }
    val flightT by remember {
        derivedStateOf { ((vy0 + sqrt(vy0 * vy0 + 2f * g * h0)) / g).coerceAtLeast(0.01f) }
    }
    val range by remember { derivedStateOf { vx0 * flightT } }
    val maxH by remember { derivedStateOf { h0 + vy0 * vy0 / (2f * g) } }

    // Reset sim time when parameters change while idle
    LaunchedEffect(v0, angleDeg, h0, g, playing) {
        if (!playing) simT = 0f
    }

    // Animation loop
    LaunchedEffect(playing, flightT) {
        if (!playing) return@LaunchedEffect
        var startNanos = 0L
        var elapsedAtStart = simT
        while (playing) {
            withFrameNanos { now ->
                if (startNanos == 0L) startNanos = now
                val secs = ((now - startNanos) / 1_000_000_000.0).toFloat()
                val newT = elapsedAtStart + secs
                if (newT >= flightT) {
                    simT = flightT
                    playing = false
                } else {
                    simT = newT
                }
            }
        }
    }

    val landed = simT >= flightT
    val tClamped = simT.coerceAtMost(flightT)
    val px = vx0 * tClamped
    val py = (h0 + vy0 * tClamped - 0.5f * g * tClamped * tClamped).coerceAtLeast(0f)
    val pvx = vx0
    val pvy = vy0 - g * tClamped
    val speed = hypot(pvx, pvy)
    val angleNow = (atan2(pvy, pvx) * 180f / PI.toFloat())

    LaunchedEffect(landed, playing, trails.size) {
        if (landed) controls.onProgress(minOf(1f, 0.4f + trails.size * 0.2f))
        else controls.onProgress(if (playing) 0.3f else 0.1f)
        if (trails.size >= 2) controls.onComplete(1f)
    }

    fun launch() { simT = 0f; playing = true }
    fun pauseSim() { playing = false }
    fun resume() { playing = true }
    fun reset() { simT = 0f; playing = false }
    fun saveShot() {
        val color = TRAIL_COLORS[trails.size % TRAIL_COLORS.size]
        trails.add(SavedTrail(
            id = System.currentTimeMillis(),
            v0 = v0, angleDeg = angleDeg, h0 = h0, gravity = g,
            range = range, maxH = maxH, flightT = flightT, color = color,
        ))
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Trajectory viewer + footer ────────────────────────────
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("TRAJECTORY VIEWER", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText("x-axis: distance · y-axis: height · scale auto-fits",
                        color = t.ink400, size = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToggleChip("Velocity", showVectors) { showVectors = !showVectors }
                    ToggleChip("Grid", showGrid) { showGrid = !showGrid }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Canvas
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(t.surface2)) {
                TrajectoryCanvas(
                    vx0 = vx0, vy0 = vy0, gravity = g, h0 = h0, flightT = flightT,
                    range = range, maxH = maxH,
                    simT = tClamped, playing = playing, landed = landed,
                    angleDeg = angleDeg, target = target,
                    trails = trails,
                    showVectors = showVectors, showGrid = showGrid,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetricChip("t", "%.2f s".format(tClamped))
                    MetricChip("x", "%.1f m".format(px))
                    MetricChip("y", "%.1f m".format(py))
                    MetricChip("|v|", "%.1f m/s".format(speed))
                    MetricChip("ang", "%.0f°".format(angleNow))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!playing && !landed) {
                        PrimaryButton(label = "Launch ▶", onClick = { launch() })
                    }
                    if (playing) {
                        SecondaryButton(label = "Pause", onClick = { pauseSim() })
                    }
                    if (!playing && simT > 0f && simT < flightT) {
                        SecondaryButton(label = "Resume", onClick = { resume() })
                    }
                    if (playing || simT > 0f) {
                        SecondaryButton(label = "Reset", onClick = { reset() })
                    }
                    if (landed) {
                        GhostButton(label = "Save shot", onClick = { saveShot() })
                    }
                }
            }
        }

        // ── Right side: controls + readouts + saved ───────────────
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ControlsCard(
                v0 = v0, onV0 = { v0 = it },
                angleDeg = angleDeg, onAngle = { angleDeg = it },
                h0 = h0, onH0 = { h0 = it },
                g = g, onG = { g = it },
                target = target, onTarget = { target = it },
                disabled = playing,
            )
            ReadoutsCard(
                range = range, maxH = maxH, flightT = flightT,
                vx0 = vx0, vy0 = vy0, target = target, landed = landed,
                v0 = v0, angleDeg = angleDeg, gravity = g,
            )
            SavedShotsList(trails = trails, onClear = { trails.clear() })
        }
    }
}

/* ──────────────────────── Trajectory canvas ───────────────────── */

@Composable
private fun TrajectoryCanvas(
    vx0: Float, vy0: Float, gravity: Float, h0: Float, flightT: Float,
    range: Float, maxH: Float,
    simT: Float, playing: Boolean, landed: Boolean,
    angleDeg: Float, target: Float,
    trails: List<SavedTrail>,
    showVectors: Boolean, showGrid: Boolean,
) {
    val t = LL.tokens
    val sky1 = Color(0xFFE0F2FE); val sky2 = Color(0xFFF8FAFC)
    val groundTint = Color(0xFF94A3B8).copy(alpha = 0.18f)
    val gridColor = t.line
    val axisColor = t.lineStrong

    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val padL = 60f; val padR = 30f; val padT = 30f; val padB = 50f
        val w = size.width; val h = size.height
        val usableW = w - padL - padR
        val usableH = h - padT - padB

        // Scale: stable across saved trails
        val maxRangeView = maxOf(
            range * 1.15f,
            (trails.maxOfOrNull { it.range * 1.15f } ?: 0f),
            maxOf(20f, target * 1.2f),
        )
        val maxHView = maxOf(
            maxH * 1.15f,
            (trails.maxOfOrNull { it.maxH * 1.15f } ?: 0f),
            10f,
        )
        val sx = usableW / maxRangeView
        val sy = usableH / maxHView

        fun w2s(wx: Float, wy: Float) = Offset(padL + wx * sx, h - padB - wy * sy)

        // Sky
        drawRect(sky1, topLeft = Offset(0f, 0f), size = Size(w, h - padB))
        // Ground
        drawRect(groundTint, topLeft = Offset(0f, h - padB), size = Size(w, padB))
        drawLine(Color(0xFF64748B), Offset(0f, h - padB), Offset(w, h - padB), strokeWidth = 1.5f)

        // Grid
        if (showGrid) {
            val stepX = niceStep(maxRangeView)
            val stepY = niceStep(maxHView)
            var x = 0f
            while (x <= maxRangeView) {
                val p = w2s(x, 0f)
                drawLine(gridColor, Offset(p.x, padT), Offset(p.x, h - padB), strokeWidth = 1f)
                x += stepX
            }
            var y = 0f
            while (y <= maxHView) {
                val p = w2s(0f, y)
                drawLine(gridColor, Offset(padL, p.y), Offset(w - padR, p.y), strokeWidth = 1f)
                y += stepY
            }
        }

        // Saved trails
        trails.forEach { tr ->
            drawProjectilePath(
                vx0 = tr.v0 * cos(tr.angleDeg * PI.toFloat() / 180f),
                vy0 = tr.v0 * sin(tr.angleDeg * PI.toFloat() / 180f),
                g = tr.gravity, h0 = tr.h0, totalT = tr.flightT, upToT = tr.flightT,
                color = tr.color.copy(alpha = 0.6f), strokeWidth = 2f,
                w2s = ::w2s,
            )
        }

        // Predicted (faint) full path when idle
        if (!playing && !landed) {
            drawProjectilePath(
                vx0 = vx0, vy0 = vy0, g = gravity, h0 = h0,
                totalT = flightT, upToT = flightT,
                color = axisColor, strokeWidth = 1.5f, dashed = true,
                w2s = ::w2s,
            )
        }

        // Active trail
        if (playing || landed) {
            drawProjectilePath(
                vx0 = vx0, vy0 = vy0, g = gravity, h0 = h0,
                totalT = flightT, upToT = simT,
                color = Color(0xFF10B981), strokeWidth = 2.5f,
                w2s = ::w2s,
            )
        }

        // Target band on ground
        if (target > 0f) {
            val left = w2s(target - 2f, 0f)
            val right = w2s(target + 2f, 0f)
            drawLine(Color(0xFFDC2626), Offset(left.x, left.y), Offset(right.x, right.y),
                strokeWidth = 5f, cap = StrokeCap.Round)
        }

        // Launcher
        val launch = w2s(0f, h0)
        drawCircle(Color(0xFF0F172A), radius = 6f, center = launch)
        // Barrel (rotated)
        val barrelLen = 22f
        val ang = -angleDeg * PI.toFloat() / 180f
        val bx = launch.x + cos(ang) * barrelLen
        val by = launch.y + sin(ang) * barrelLen
        drawLine(Color(0xFF0F172A), launch, Offset(bx, by), strokeWidth = 6f, cap = StrokeCap.Round)

        // Projectile + velocity vectors
        if (playing || landed) {
            val px = vx0 * simT
            val py = (h0 + vy0 * simT - 0.5f * gravity * simT * simT).coerceAtLeast(0f)
            val p = w2s(px, py)
            drawCircle(Color(0xFFF59E0B), radius = 7f, center = p)
            drawCircle(Color(0xFFB45309), radius = 7f, center = p, style = Stroke(1.5f))

            if (showVectors && playing) {
                val pvx = vx0
                val pvy = vy0 - gravity * simT
                val arrowS = 0.6f // 0.6s "what-if"
                val vxEnd = Offset(p.x + pvx * sx * arrowS, p.y)
                val vyEnd = Offset(p.x, p.y - pvy * sy * arrowS)
                drawArrow(p, vxEnd, Color(0xFF0EA5E9))
                drawArrow(p, vyEnd, Color(0xFFEF4444))
            }
        }

        // Landing marker
        if (landed) {
            val r = w2s(range, 0f)
            drawLine(Color(0xFF10B981), Offset(r.x - 8f, r.y), Offset(r.x + 8f, r.y),
                strokeWidth = 3f, cap = StrokeCap.Round)
        }

        // Max height marker (after landing)
        if (landed) {
            val apex = w2s(0f, maxH)
            drawLine(Color(0xFFA855F7), Offset(padL, apex.y), Offset(w - padR, apex.y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f)))
        }
    }
}

private fun DrawScope.drawProjectilePath(
    vx0: Float, vy0: Float, g: Float, h0: Float,
    totalT: Float, upToT: Float,
    color: Color, strokeWidth: Float,
    dashed: Boolean = false,
    w2s: (Float, Float) -> Offset,
) {
    val n = 80
    val end = upToT.coerceAtMost(totalT)
    val path = Path()
    for (i in 0..n) {
        val tt = i.toFloat() / n * end
        val x = vx0 * tt
        val y = (h0 + vy0 * tt - 0.5f * g * tt * tt).coerceAtLeast(0f)
        val p = w2s(x, y)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    drawPath(
        path, color,
        style = if (dashed) Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)))
                else Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawArrow(start: Offset, end: Offset, color: Color) {
    drawLine(color, start, end, strokeWidth = 2f, cap = StrokeCap.Round)
    // Tiny arrow head
    val dx = end.x - start.x; val dy = end.y - start.y
    val len = hypot(dx, dy); if (len < 0.001f) return
    val ux = dx / len; val uy = dy / len
    val head = 6f
    val left = Offset(end.x - ux * head + (-uy) * head * 0.5f,
                       end.y - uy * head + (ux) * head * 0.5f)
    val right = Offset(end.x - ux * head - (-uy) * head * 0.5f,
                       end.y - uy * head - (ux) * head * 0.5f)
    val p = Path().apply {
        moveTo(end.x, end.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close()
    }
    drawPath(p, color)
}

private fun niceStep(span: Float): Float {
    val target = 6
    val rough = span / target
    val pow = 10.0.pow(floor(log10(rough.toDouble()))).toFloat()
    val norm = rough / pow
    val step = when {
        norm < 1.5f -> 1f
        norm < 3f -> 2f
        norm < 7f -> 5f
        else -> 10f
    }
    return step * pow
}

/* ──────────────────────── Side panels ────────────────────────── */

@Composable
private fun ControlsCard(
    v0: Float, onV0: (Float) -> Unit,
    angleDeg: Float, onAngle: (Float) -> Unit,
    h0: Float, onH0: (Float) -> Unit,
    g: Float, onG: (Float) -> Unit,
    target: Float, onTarget: (Float) -> Unit,
    disabled: Boolean,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText("INITIAL CONDITIONS", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLText("Sliders are disabled while launching.", color = t.ink500, size = 11.sp)
        LLSlider("Initial speed", v0, onV0, 5f, 80f, 1f, "m/s", enabled = !disabled,
            valueFormat = { "%.0f".format(it) })
        LLSlider("Launch angle", angleDeg, onAngle, 5f, 85f, 1f, "°", enabled = !disabled,
            valueFormat = { "%.0f".format(it) })
        LLSlider("Launch height", h0, onH0, 0f, 50f, 1f, "m", enabled = !disabled,
            valueFormat = { "%.0f".format(it) })
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LLText("Gravity", color = t.ink200, size = 12.sp, weight = FontWeight.Medium)
                LLText("%.2f m/s²".format(g), color = t.ink500, size = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GRAVITY_PRESETS.forEach { p ->
                    val on = kotlin.math.abs(g - p.g) < 0.01f
                    val bg = if (on) t.accent600 else t.surface2
                    val fg = if (on) Color.White else t.ink200
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .clickable(enabled = !disabled) { onG(p.g) }
                            .padding(horizontal = 6.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) { LLText(p.label, color = fg, size = 11.sp, weight = FontWeight.SemiBold) }
                }
            }
        }
        LLSlider("Target distance", target, onTarget, 0f, 400f, 5f, "m", enabled = !disabled,
            valueFormat = { "%.0f".format(it) })
    }
}

@Composable
private fun ReadoutsCard(
    range: Float, maxH: Float, flightT: Float,
    vx0: Float, vy0: Float, target: Float, landed: Boolean,
    v0: Float, angleDeg: Float, gravity: Float,
) {
    val t = LL.tokens
    val miss = range - target
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText("COMPUTED RESULTS", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Stat("Range R", "%.1f m".format(range), Modifier.weight(1f))
            Stat("Max height H", "%.1f m".format(maxH), Modifier.weight(1f), violet = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Stat("Time of flight T", "%.2f s".format(flightT), Modifier.weight(1f))
            Stat("Vx₀ · Vy₀", "%.1f · %.1f".format(vx0, vy0), Modifier.weight(1f))
        }
        if (target > 0f) {
            val bg = when {
                !landed -> t.surface2
                kotlin.math.abs(miss) < 3f -> t.accent50
                else -> t.amber50
            }
            val fg = when {
                !landed -> t.ink500
                kotlin.math.abs(miss) < 3f -> t.accent700
                else -> t.amber700
            }
            val msg = when {
                !landed -> "Target at ${target.toInt()} m. Predicted range %.1f m.".format(range)
                kotlin.math.abs(miss) < 3f -> "Hit the target. Within %.1f m.".format(kotlin.math.abs(miss))
                else -> "Missed by ${if (miss > 0) "+" else ""}${"%.1f".format(miss)} m (${if (miss > 0) "long" else "short"})."
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) { LLText(msg, color = fg, size = 12.sp) }
        }
        // Equations
        val rEq = (v0 * v0 * sin(2f * angleDeg * PI.toFloat() / 180f)) / gravity
        val hEq = (v0 * v0 * sin(angleDeg * PI.toFloat() / 180f).pow(2f)) / (2f * gravity)
        val tEq = (2f * v0 * sin(angleDeg * PI.toFloat() / 180f)) / gravity
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface2)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Column {
                LLText("Equations (h = 0):", color = t.ink200, size = 11.sp, weight = FontWeight.SemiBold)
                LLText("R = v² sin(2θ) / g = %.1f m".format(rEq), color = t.ink400, size = 11.sp)
                LLText("H = v² sin²(θ) / 2g = %.1f m".format(hEq), color = t.ink400, size = 11.sp)
                LLText("T = 2v sin(θ) / g = %.2f s".format(tEq), color = t.ink400, size = 11.sp)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier, violet: Boolean = false) {
    val t = LL.tokens
    val bg = if (violet) Color(0xFFF5F3FF) else t.surface2
    val fg = if (violet) Color(0xFF6D28D9) else t.ink200
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        LLText(label.uppercase(), color = t.ink500, size = 9.sp, letterSpacing = 1.4.sp)
        LLText(value, color = fg, size = 14.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun SavedShotsList(trails: List<SavedTrail>, onClear: () -> Unit) {
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
            LLText("Saved shots", color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold)
            if (trails.isNotEmpty()) {
                Box(modifier = Modifier.clickable { onClear() }) {
                    LLText("Clear all", color = t.ink500, size = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (trails.isEmpty()) {
            LLText("Launch a shot, then Save shot to overlay it on the next launch.",
                color = t.ink500, size = 11.sp, lineHeight = 15.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(trails, key = { it.id }) { tr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(t.surface2)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(tr.color))
                        Spacer(Modifier.width(8.dp))
                        LLText(
                            "v₀ ${tr.v0.toInt()} · θ ${tr.angleDeg.toInt()}° · h ${tr.h0.toInt()} · g ${"%.1f".format(tr.gravity)}",
                            color = t.ink200, size = 11.sp, modifier = Modifier.weight(1f),
                        )
                        LLText("R=${tr.range.toInt()} H=${tr.maxH.toInt()}",
                            color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleChip(label: String, on: Boolean, onToggle: () -> Unit) {
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
