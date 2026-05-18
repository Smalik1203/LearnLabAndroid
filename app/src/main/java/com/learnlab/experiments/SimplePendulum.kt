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
import androidx.compose.foundation.layout.heightIn
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Simple Pendulum Lab — full native port of the React experiment.
 *
 * Semi-implicit Euler at 4 sub-steps per frame:
 *   ω ← ω − (g/L) sin θ · sdt
 *   θ ← θ + ω · sdt
 *
 * Mass affects bob radius only — it does not appear in the period equation,
 * which is the pedagogical point.
 */

private data class SavedPendulum(
    val id: Long,
    val length: Float,
    val mass: Float,
    val theta0: Float,
    val gravity: Float,
    val color: Color,
    var theta: Float,
    var omega: Float,
)

private val PENDULUM_COLORS = listOf(
    Color(0xFF0EA5E9), Color(0xFFA855F7), Color(0xFFF43F5E), Color(0xFFF59E0B),
)

private data class GravityPreset(val label: String, val g: Float)
private val GRAVITY_PRESETS = listOf(
    GravityPreset("Earth", 9.8f),
    GravityPreset("Moon", 1.62f),
    GravityPreset("Mars", 3.71f),
    GravityPreset("Jupiter", 24.79f),
)

@Composable
fun SimplePendulum(controls: ExperimentControls) {
    val t = LL.tokens

    var length by remember { mutableStateOf(1.0f) }       // metres
    var mass by remember { mutableStateOf(0.5f) }         // kg (visual)
    var theta0Deg by remember { mutableStateOf(30f) }     // degrees
    var gravity by remember { mutableStateOf(9.8f) }      // m/s²

    var theta by remember { mutableStateOf(degToRad(30f)) }
    var omega by remember { mutableStateOf(0f) }
    var elapsed by remember { mutableStateOf(0f) }
    var playing by remember { mutableStateOf(false) }
    var measuredPeriod by remember { mutableStateOf<Float?>(null) }
    val history = remember { mutableStateListOf<Pair<Float, Float>>() } // (t, theta)
    val saved = remember { mutableStateListOf<SavedPendulum>() }

    // Reset integration if parameters change while paused
    LaunchedEffect(length, theta0Deg, gravity, playing) {
        if (!playing) {
            theta = degToRad(theta0Deg)
            omega = 0f
            elapsed = 0f
            history.clear()
            measuredPeriod = null
        }
    }

    // Physics loop
    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect
        var lastNanos = 0L
        var lastCross: Float? = null
        while (playing) {
            withFrameNanos { now ->
                if (lastNanos == 0L) lastNanos = now
                var dt = ((now - lastNanos) / 1_000_000_000.0).toFloat()
                lastNanos = now
                if (dt > 0.05f) dt = 0.05f

                val subSteps = 4
                val sdt = dt / subSteps
                var th = theta; var om = omega
                val prevTheta = th
                repeat(subSteps) {
                    om -= (gravity / length) * sin(th) * sdt
                    th += om * sdt
                }
                theta = th; omega = om
                elapsed += dt

                history.add(elapsed to th)
                while (history.isNotEmpty() && elapsed - history.first().first > 10f) {
                    history.removeAt(0)
                }
                // Detect + → − zero crossing for measured period
                if (prevTheta > 0f && th <= 0f) {
                    val lc = lastCross
                    if (lc != null) measuredPeriod = elapsed - lc
                    lastCross = elapsed
                }

                // Advance saved pendulums on the same clock
                for (i in saved.indices) {
                    val p = saved[i]
                    var pth = p.theta; var pom = p.omega
                    repeat(subSteps) {
                        pom -= (p.gravity / p.length) * sin(pth) * sdt
                        pth += pom * sdt
                    }
                    saved[i] = p.copy(theta = pth, omega = pom)
                }
            }
        }
    }

    val smallAngleT by remember {
        derivedStateOf { (2f * PI.toFloat() * sqrt(length / gravity)) }
    }

    LaunchedEffect(elapsed, measuredPeriod, saved.size) {
        var p = 0.2f
        if (elapsed > 2f) p = 0.5f
        if (measuredPeriod != null) p = 0.7f
        if (saved.isNotEmpty()) p = 0.9f
        if (saved.size >= 2) { p = 1f; controls.onComplete(1f) }
        controls.onProgress(p)
    }

    fun reset() {
        playing = false
        theta = degToRad(theta0Deg)
        omega = 0f
        elapsed = 0f
        history.clear()
        measuredPeriod = null
    }
    fun save() {
        if (saved.size >= 4) return
        val color = PENDULUM_COLORS[saved.size % PENDULUM_COLORS.size]
        saved.add(SavedPendulum(
            id = System.currentTimeMillis(),
            length = length, mass = mass, theta0 = degToRad(theta0Deg),
            gravity = gravity, color = color,
            theta = degToRad(theta0Deg), omega = 0f,
        ))
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Scene + graph + controls strip ───────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            // Header — compact, chips on top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LLText(
                    "PENDULUM SCENE",
                    color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MetricChip("t", "%.1fs".format(elapsed))
                    MetricChip("θ", "%.0f°".format(radToDeg(theta)))
                    MetricChip("ω", "%.1f".format(omega))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Scene (3 parts of vertical space) + graph (1 part)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .background(t.surface2),
            ) {
                PendulumScene(
                    activeLength = length, activeMass = mass,
                    activeTheta = theta, activeTheta0 = degToRad(theta0Deg),
                    saved = saved,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(t.surface2)
                    .padding(8.dp),
            ) { AngleGraph(history, elapsed) }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // Footer — compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PeriodCallout("Predicted", "%.2fs".format(smallAngleT), accent = false)
                    PeriodCallout(
                        "Measured",
                        measuredPeriod?.let { "%.2fs".format(it) } ?: "—",
                        accent = true,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!playing) {
                        PrimaryButton(label = "Start", onClick = { playing = true })
                    } else {
                        SecondaryButton(label = "Pause", onClick = { playing = false })
                    }
                    SecondaryButton(label = "Reset", onClick = { reset() })
                    GhostButton(
                        label = if (saved.size >= 4) "Full" else "Save",
                        onClick = { save() },
                    )
                }
            }
        }

        // ── Right side: controls, insight, saved list ────────────
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ControlsCard(
                length = length, onLength = { length = it },
                mass = mass, onMass = { mass = it },
                theta0Deg = theta0Deg, onTheta0 = { theta0Deg = it },
                gravity = gravity, onGravity = { gravity = it },
                disabled = playing,
            )
            InsightCard(length = length, gravity = gravity,
                theta0Deg = theta0Deg, smallAngleT = smallAngleT)
            SavedList(saved = saved, onClear = { saved.clear() })
        }
    }
}

/* ─────────────────────────── Scene ────────────────────────── */

@Composable
private fun PendulumScene(
    activeLength: Float, activeMass: Float, activeTheta: Float, activeTheta0: Float,
    saved: List<SavedPendulum>,
) {
    val t = LL.tokens
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val w = size.width; val h = size.height
        val pivotY = 12f
        val padX = 40f
        val usableH = h - pivotY - 12f
        val maxL = maxOf(activeLength, saved.maxOfOrNull { it.length } ?: 0f, 1f)
        val scale = (usableH * 0.95f) / maxL

        // Ceiling
        drawLine(t.lineStrong, Offset(0f, pivotY), Offset(w, pivotY), strokeWidth = 2f)

        val all = buildList {
            add(Triple(activeLength, Triple(activeMass, activeTheta, activeTheta0), Color(0xFF10B981)))
            saved.forEach { p ->
                add(Triple(p.length, Triple(p.mass, p.theta, p.theta0), p.color))
            }
        }
        val n = all.size
        val usableW = w - 2f * padX
        val spacing = if (n == 1) 0f else usableW / (n - 1)
        val startX = if (n == 1) w / 2f else padX

        all.forEachIndexed { i, (L, mta, color) ->
            val (m, th, th0) = mta
            val pivotX = startX + i * spacing
            val lenPx = L * scale
            val bobX = pivotX + sin(th) * lenPx
            val bobY = pivotY + cos(th) * lenPx
            val bobR = 8f + sqrt(m) * 6f

            // Arc guide (dashed)
            if (abs(th0) > 0.01f) {
                val r = lenPx * 0.6f
                val startTh = -abs(th0); val endTh = abs(th0)
                val arcPath = Path().apply {
                    moveTo(pivotX + sin(startTh) * r, pivotY + cos(startTh) * r)
                    val steps = 30
                    for (k in 1..steps) {
                        val a = startTh + (endTh - startTh) * (k.toFloat() / steps)
                        lineTo(pivotX + sin(a) * r, pivotY + cos(a) * r)
                    }
                }
                drawPath(
                    arcPath, color.copy(alpha = 0.45f),
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))),
                )
            }
            // Pivot
            drawCircle(Color(0xFF0F172A), radius = 4f, center = Offset(pivotX, pivotY))
            // String
            drawLine(color.copy(alpha = 0.85f), Offset(pivotX, pivotY), Offset(bobX, bobY), strokeWidth = 1.5f)
            // Bob
            drawCircle(color, radius = bobR, center = Offset(bobX, bobY))
            drawCircle(Color(0xFF0F172A), radius = bobR, center = Offset(bobX, bobY),
                style = Stroke(1.2f))
        }
    }
}

@Composable
private fun AngleGraph(history: List<Pair<Float, Float>>, tNow: Float) {
    val t = LL.tokens
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LLText("Angle vs time — appears once the pendulum starts swinging",
                color = t.ink500, size = 11.sp)
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val tMin = maxOf(0f, tNow - 10f)
        val tMax = maxOf(tNow, 1f)
        val maxAbs = maxOf(0.2f, history.maxOf { abs(it.second) })
        val midY = h / 2f
        // Zero axis
        drawLine(t.lineStrong, Offset(0f, midY), Offset(w, midY), strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))
        val path = Path().apply {
            history.forEachIndexed { i, (tt, th) ->
                val x = if (tMax > tMin) (tt - tMin) / (tMax - tMin) * w else 0f
                val y = midY - (th / maxAbs) * (midY - 10f)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, Color(0xFF10B981), style = Stroke(2f, cap = StrokeCap.Round))
    }
}

/* ─────────────────────────── Side panels ──────────────────── */

@Composable
private fun ControlsCard(
    length: Float, onLength: (Float) -> Unit,
    mass: Float, onMass: (Float) -> Unit,
    theta0Deg: Float, onTheta0: (Float) -> Unit,
    gravity: Float, onGravity: (Float) -> Unit,
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
        LLText(if (disabled) "Disabled during play. Pause to change." else "Changing parameters resets the swing.",
            color = t.ink500, size = 11.sp)
        LLSlider(
            label = "Length (L)", value = length, onValueChange = onLength,
            min = 0.2f, max = 3f, step = 0.05f, unit = "m", enabled = !disabled,
            valueFormat = { "%.2f".format(it) },
        )
        LLSlider(
            label = "Bob mass (m)", value = mass, onValueChange = onMass,
            min = 0.1f, max = 2f, step = 0.1f, unit = "kg", enabled = !disabled,
            valueFormat = { "%.1f".format(it) },
        )
        LLSlider(
            label = "Initial angle (θ₀)", value = theta0Deg, onValueChange = onTheta0,
            min = 5f, max = 80f, step = 1f, unit = "°", enabled = !disabled,
            valueFormat = { "%.0f".format(it) },
        )
        Column {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                LLText("Gravity (g)", color = t.ink200, size = 12.sp, weight = FontWeight.Medium)
                LLText("%.2f m/s²".format(gravity), color = t.ink500, size = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GRAVITY_PRESETS.forEach { p ->
                    val on = abs(gravity - p.g) < 0.01f
                    val bg = if (on) t.accent600 else t.surface2
                    val fg = if (on) Color.White else t.ink200
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .clickable(enabled = !disabled) { onGravity(p.g) }
                            .padding(horizontal = 6.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) { LLText(p.label, color = fg, size = 11.sp, weight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(length: Float, gravity: Float, theta0Deg: Float, smallAngleT: Float) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText("WHAT TO LOOK FOR", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LLText("Mass doesn't appear in the period formula. Swap mass — the period stays the same.",
            color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface2)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Column {
                LLText("T = 2π √(L/g)", color = t.ink400, size = 11.sp)
                LLText("= 2π √(${"%.2f".format(length)} / ${"%.2f".format(gravity)})",
                    color = t.ink400, size = 11.sp)
                LLText("= ${"%.3f".format(smallAngleT)} s",
                    color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold)
            }
        }
        if (theta0Deg > 20f) {
            LLText(
                "At θ₀ = ${theta0Deg.toInt()}°, the small-angle formula slightly under-predicts the real period. " +
                    "Measured T will be a bit larger.",
                color = t.amber700, size = 11.sp, lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun SavedList(saved: List<SavedPendulum>, onClear: () -> Unit) {
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
            LLText("Saved pendulums (${saved.size} / 4)",
                color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold)
            if (saved.isNotEmpty()) {
                Box(modifier = Modifier.clickable { onClear() }) {
                    LLText("Clear", color = t.ink500, size = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (saved.isEmpty()) {
            LLText("Save the current pendulum to add a copy that swings beside it.",
                color = t.ink500, size = 11.sp, lineHeight = 15.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(saved, key = { it.id }) { p ->
                    val period = 2f * PI.toFloat() * sqrt(p.length / p.gravity)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(t.surface2)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(p.color))
                        Spacer(Modifier.width(8.dp))
                        LLText(
                            "L=${"%.2f".format(p.length)}m · m=${"%.1f".format(p.mass)}kg · g=${"%.2f".format(p.gravity)}",
                            color = t.ink200, size = 11.sp, modifier = Modifier.weight(1f),
                        )
                        LLText("T≈${"%.2f".format(period)}s",
                            color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodCallout(label: String, value: String, accent: Boolean) {
    val t = LL.tokens
    val bg = if (accent) t.accent50 else t.surface2
    val fg = if (accent) t.accent700 else t.ink500
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row {
            LLText(label, color = fg, size = 11.sp, weight = FontWeight.Medium)
            LLText(": ", color = fg, size = 11.sp)
            LLText(value, color = fg, size = 11.sp, weight = FontWeight.SemiBold)
        }
    }
}

private fun degToRad(d: Float) = (d * PI / 180.0).toFloat()
private fun radToDeg(r: Float) = (r * 180.0 / PI).toFloat()
