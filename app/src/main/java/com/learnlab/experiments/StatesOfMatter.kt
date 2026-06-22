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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.store.ExperimentControls
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * States of Matter — particle-behaviour simulator (Grade 8, Chapter 1 preview: particles).
 * Pick a substance, drag the temperature, and watch the particles move from a tight vibrating
 * grid (solid) to a flowing pool (liquid) to a fast-moving gas that fills the box. Transitions
 * happen gradually around each substance's real freeze/boil points.
 */

private data class Substance(val name: String, val freeze: Float, val boil: Float)

private val SUBSTANCES = listOf(
    Substance("Water (H₂O)", 0f, 100f),
    Substance("Iron", 1538f, 2862f),
    Substance("Oxygen", -218f, -183f),
)

private class Particle(val homeX: Float, val homeY: Float) {
    var x = homeX; var y = homeY; var vx = 0f; var vy = 0f
}

private const val GRID = 6
private const val LO = 0.045f
private const val HI = 0.955f

@Composable
fun StatesOfMatter(controls: ExperimentControls) {
    val t = LL.tokens

    var subIndex by remember { mutableStateOf(0) }
    val sub = SUBSTANCES[subIndex]
    val tempMin = sub.freeze - 50f
    val tempMax = sub.boil + 50f
    var temp by remember { mutableStateOf(0f) }
    // Start each substance cold (solid) so the journey runs solid -> liquid -> gas.
    LaunchedEffect(subIndex) { temp = sub.freeze - 30f }

    // Transition half-width: a few degrees, or 3% of the range, whichever is larger.
    val hw = maxOf(3f, 0.03f * (tempMax - tempMin))
    val meltFrac = smooth01((temp - (sub.freeze - hw)) / (2f * hw))
    val boilFrac = smooth01((temp - (sub.boil - hw)) / (2f * hw))
    val energyTarget = meltFrac + boilFrac // 0 = solid .. 1 = liquid .. 2 = gas

    val melting = meltFrac > 0.06f && meltFrac < 0.94f && boilFrac < 0.06f
    val boiling = boilFrac > 0.06f && boilFrac < 0.94f
    val stateLabel = when {
        boiling -> "BOILING"
        melting -> "MELTING"
        energyTarget < 0.5f -> "SOLID"
        energyTarget < 1.5f -> "LIQUID"
        else -> "GAS"
    }
    val accent = Color(0xFF2DD4BF)

    // Completion tracking
    var sawSolid by remember { mutableStateOf(false) }
    var sawLiquid by remember { mutableStateOf(false) }
    var sawGas by remember { mutableStateOf(false) }
    var sawMelt by remember { mutableStateOf(false) }
    var sawBoil by remember { mutableStateOf(false) }
    LaunchedEffect(stateLabel) {
        when (stateLabel) {
            "SOLID" -> sawSolid = true
            "LIQUID" -> sawLiquid = true
            "GAS" -> sawGas = true
            "MELTING" -> sawMelt = true
            "BOILING" -> sawBoil = true
        }
    }
    LaunchedEffect(sawSolid, sawLiquid, sawGas, sawMelt, sawBoil) {
        var p = 0.1f
        val states = listOf(sawSolid, sawLiquid, sawGas).count { it }
        p += states * 0.2f
        if (sawMelt) p += 0.12f
        if (sawBoil) p += 0.12f
        controls.onProgress(p.coerceAtMost(1f))
        if (sawSolid && sawLiquid && sawGas && sawMelt && sawBoil) controls.onComplete(1f)
    }

    // Particle system + a tick that forces the canvas to redraw each frame.
    val particles = remember {
        Array(GRID * GRID) { i ->
            val gx = LO + (HI - LO) * ((i % GRID) + 0.5f) / GRID
            val gy = LO + (HI - LO) * ((i / GRID) + 0.5f) / GRID
            Particle(gx, gy)
        }
    }
    var tick by remember { mutableStateOf(0) }
    var eSmooth by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) last = now
                val dt = min(0.05f, (now - last) / 1_000_000_000f)
                last = now
                // Read the live `temp`/`subIndex` state each frame (not a captured snapshot).
                val eTarget = energyForState(subIndex, temp)
                eSmooth += (eTarget - eSmooth) * (dt * 2.5f).coerceIn(0f, 1f)
                stepParticles(particles, eSmooth, dt)
                tick++
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Scene ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("PARTICLE SIMULATOR", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText("Drag the temperature. Watch how the particles arrange and move.",
                        color = t.ink400, size = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) { LLText(stateLabel, color = accent, size = 14.sp, weight = FontWeight.Bold, letterSpacing = 1.sp) }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Particle view
                Box(modifier = Modifier.weight(2f).fillMaxHeight().background(t.surface2)) {
                    Canvas(Modifier.fillMaxSize().padding(10.dp)) {
                        val w = size.width; val h = size.height
                        val r = 7.dp.toPx()
                        // Read `tick` so the draw is re-invalidated every animation frame.
                        if (tick >= 0) {
                            for (p in particles) {
                                drawCircle(accent, r, Offset(p.x * w, p.y * h))
                            }
                        }
                    }
                    LLText("microscopic view", color = t.ink500, size = 10.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
                }
                Box(Modifier.fillMaxHeight().width(1.dp).background(t.line))
                // Macro view + thermometer
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(t.surface2)) {
                    MacroAndThermo(energy = eSmooth, frac = ((temp - tempMin) / (tempMax - tempMin)).coerceIn(0f, 1f), accent = accent)
                    LLText("bulk view", color = t.ink500, size = 10.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
                    LLText("%.0f °C".format(temp), color = t.ink50, size = 15.sp, weight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            val prompt = when {
                melting -> "Watch closely: particles are breaking free of the grid. What happens to their movement right here?"
                boiling -> "Where do the particles go when they leave the liquid? Can you see the gas forming?"
                stateLabel == "GAS" -> "The particles fill the whole box, moving fast in every direction — this is diffusion."
                stateLabel == "LIQUID" -> "Particles stay close but slide past each other — the substance flows and takes the container's shape."
                else -> "Particles sit in a tight, ordered grid, only vibrating in place — a definite shape and volume."
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                LLText(prompt, color = t.ink200, size = 13.sp, lineHeight = 18.sp)
            }
        }

        // ── Controls ────────────────────────────────────────────
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ControlCard("SUBSTANCE") {
                PillTabBar(
                    tabs = SUBSTANCES.map { it.name.substringBefore(" ") },
                    selected = subIndex,
                    onSelect = { subIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PointChip("Freezes", "%.0f °C".format(sub.freeze))
                    PointChip("Boils", "%.0f °C".format(sub.boil))
                }
            }
            ControlCard("TEMPERATURE") {
                LLSlider(
                    label = "Temperature",
                    value = temp,
                    onValueChange = { temp = it },
                    min = tempMin, max = tempMax, unit = "°C",
                    valueFormat = { "%.0f".format(it) },
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LLText("WHAT TO LOOK FOR", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                LLText("• Solid: a tight, ordered grid — particles only vibrate in place.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Liquid: same closeness, but disordered — particles slide past each other.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Gas: far apart, fast, filling all the space available.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• The same substance reaches every state — only the energy changes.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

private fun stepParticles(particles: Array<Particle>, e: Float, dt: Float) {
    val hp = (1f - e).coerceIn(0f, 1f)                 // home-grid pull (solid)
    val liquidness = (1f - abs(e - 1f)).coerceIn(0f, 1f)
    val grav = liquidness * 0.0016f
    val ag = 0.00025f + 0.0011f * e
    val damp = if (e > 1.3f) 0.985f else 0.9f
    val k = (dt * 60f).coerceIn(0.5f, 1.5f)            // frame-rate normaliser
    for (p in particles) {
        p.vx += (p.homeX - p.x) * hp * 0.18f
        p.vy += (p.homeY - p.y) * hp * 0.18f
        p.vx += (Random.nextFloat() - 0.5f) * ag
        p.vy += (Random.nextFloat() - 0.5f) * ag
        p.vy += grav
        p.vx *= damp; p.vy *= damp
        if (e > 1.5f) {                                 // gas keeps a minimum speed
            val sp = hypot(p.vx, p.vy)
            val minSp = 0.0035f * (e - 1.3f)
            if (sp > 0.0001f && sp < minSp) { p.vx *= minSp / sp; p.vy *= minSp / sp }
        }
        p.x += p.vx * k; p.y += p.vy * k
        if (p.x < LO) { p.x = LO; p.vx = -p.vx * 0.85f }
        if (p.x > HI) { p.x = HI; p.vx = -p.vx * 0.85f }
        if (p.y < LO) { p.y = LO; p.vy = -p.vy * 0.85f }
        if (p.y > HI) { p.y = HI; p.vy = -p.vy * 0.85f }
    }
}

@Composable
private fun MacroAndThermo(energy: Float, frac: Float, accent: Color) {
    val t = LL.tokens
    val blockBlue = Color(0xFF60A5FA)
    Canvas(Modifier.fillMaxSize().padding(top = 26.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)) {
        val w = size.width; val h = size.height
        // Container (left ~62%) and thermometer (right).
        val contW = w * 0.6f
        val contLeft = 0f
        val contTop = h * 0.12f
        val contH = h * 0.8f
        drawRect(t.line, topLeft = Offset(contLeft, contTop), size = Size(contW, contH), style = Stroke(2f))
        val s = energy.coerceIn(0f, 2f)
        when {
            s < 1f -> { // solid -> liquid morph: a block that settles into a pool
                val solidness = 1f - s
                val blockH = contH * (0.42f + 0.18f * (1f - solidness))
                val blockW = contW * (0.5f + 0.2f * (1f - solidness))
                val bx = contLeft + (contW - blockW) / 2f
                val by = contTop + contH - blockH
                drawRoundRect(
                    blockBlue.copy(alpha = 0.85f),
                    topLeft = Offset(bx, by), size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * (1f - solidness) + 4f),
                )
            }
            s < 1.7f -> { // liquid pooled at the bottom
                val poolH = contH * 0.45f
                drawRect(blockBlue.copy(alpha = 0.8f),
                    topLeft = Offset(contLeft, contTop + contH - poolH), size = Size(contW, poolH))
            }
            else -> { // gas: faint haze fills the container
                drawRect(accent.copy(alpha = 0.14f),
                    topLeft = Offset(contLeft, contTop), size = Size(contW, contH))
            }
        }
        // Thermometer (right side)
        val tubeX = w * 0.82f
        val tubeTop = contTop
        val tubeBottom = contTop + contH - 14f
        val tubeW = 10f
        drawLine(t.lineStrong, Offset(tubeX, tubeTop), Offset(tubeX, tubeBottom), strokeWidth = tubeW)
        val fillTop = tubeBottom - (tubeBottom - tubeTop) * frac
        drawLine(Color(0xFFEF4444), Offset(tubeX, fillTop), Offset(tubeX, tubeBottom), strokeWidth = tubeW - 2f)
        drawCircle(Color(0xFFEF4444), 11f, Offset(tubeX, contTop + contH))
    }
}

@Composable
private fun ControlCard(title: String, content: @Composable () -> Unit) {
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
        LLText(title, color = t.ink500, size = 11.sp, weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        content()
    }
}

@Composable
private fun PointChip(label: String, value: String) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(label, color = t.ink500, size = 11.sp)
        Spacer(Modifier.width(6.dp))
        LLText(value, color = t.ink50, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

private fun energyForState(subIndex: Int, temp: Float): Float {
    val sb = SUBSTANCES[subIndex]
    val tmin = sb.freeze - 50f
    val tmax = sb.boil + 50f
    val hw = maxOf(3f, 0.03f * (tmax - tmin))
    val m = smooth01((temp - (sb.freeze - hw)) / (2f * hw))
    val b = smooth01((temp - (sb.boil - hw)) / (2f * hw))
    return m + b
}

private fun smooth01(x: Float): Float {
    val c = x.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}
