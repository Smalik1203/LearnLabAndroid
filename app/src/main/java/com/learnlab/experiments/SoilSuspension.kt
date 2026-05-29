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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Soil Suspension — Grade 8 "The Invisible Living World".
 *
 * Prepare a soil suspension (soil + water, stirred, settled), take a drop from
 * the clear top layer, mount it on a slide, and observe under a microscope that
 * soil water teems with moving microorganisms too small to see with bare eyes.
 */

private enum class Stage(val order: Int) {
    Soil(0), Water(1), Stir(2), Settle(3), Drop(4), Slide(5), Microscope(6);
}

private enum class MicrobeType { Ciliate, Amoeba, Rod, Flagellate }

private data class Microbe(
    val type: MicrobeType,
    val startX: Float,        // normalized within field, -1..1
    val startY: Float,
    val baseSize: Float,      // fraction of field radius
    val phase: Float,
    val driftAngle: Float,
    val speed: Float,
    val minMag: Int,          // smallest magnification at which it's visible
)

private data class Debris(val x: Float, val y: Float, val r: Float, val drift: Float)

@Composable
fun SoilSuspension(controls: ExperimentControls) {
    val t = LL.tokens

    var stage by remember { mutableStateOf(Stage.Soil) }
    var magnification by remember { mutableStateOf(100) }   // 40 / 100 / 400
    var focus by remember { mutableStateOf(0.25f) }          // 0..1, sharp near 0.6
    var time by remember { mutableStateOf(0f) }
    var settleProgress by remember { mutableStateOf(0f) }
    var infoOpen by remember { mutableStateOf(false) }

    var madeSuspension by remember { mutableStateOf(false) }
    var viewedScope by remember { mutableStateOf(false) }
    var changedMag by remember { mutableStateOf(false) }
    var focusedSharp by remember { mutableStateOf(false) }

    // Field organisms + debris, generated once.
    val microbes = remember {
        val rnd = Random(42)
        buildList {
            repeat(5) {
                add(Microbe(MicrobeType.Ciliate, rnd.nextFloat() * 1.4f - 0.7f,
                    rnd.nextFloat() * 1.4f - 0.7f, 0.12f + rnd.nextFloat() * 0.05f,
                    rnd.nextFloat() * 6.28f, rnd.nextFloat() * 6.28f,
                    0.05f + rnd.nextFloat() * 0.05f, 40))
            }
            repeat(3) {
                add(Microbe(MicrobeType.Amoeba, rnd.nextFloat() * 1.4f - 0.7f,
                    rnd.nextFloat() * 1.4f - 0.7f, 0.15f + rnd.nextFloat() * 0.05f,
                    rnd.nextFloat() * 6.28f, rnd.nextFloat() * 6.28f,
                    0.025f + rnd.nextFloat() * 0.03f, 40))
            }
            repeat(4) {
                add(Microbe(MicrobeType.Flagellate, rnd.nextFloat() * 1.4f - 0.7f,
                    rnd.nextFloat() * 1.4f - 0.7f, 0.07f + rnd.nextFloat() * 0.03f,
                    rnd.nextFloat() * 6.28f, rnd.nextFloat() * 6.28f,
                    0.08f + rnd.nextFloat() * 0.06f, 100))
            }
            repeat(9) {
                add(Microbe(MicrobeType.Rod, rnd.nextFloat() * 1.6f - 0.8f,
                    rnd.nextFloat() * 1.6f - 0.8f, 0.045f + rnd.nextFloat() * 0.02f,
                    rnd.nextFloat() * 6.28f, rnd.nextFloat() * 6.28f,
                    0.1f + rnd.nextFloat() * 0.08f, 400))
            }
        }
    }
    val debris = remember {
        val rnd = Random(7)
        List(10) { Debris(rnd.nextFloat() * 1.6f - 0.8f, rnd.nextFloat() * 1.6f - 0.8f,
            0.02f + rnd.nextFloat() * 0.03f, rnd.nextFloat() * 6.28f) }
    }

    // Frame loop — advances animation time and the settle progress.
    LaunchedEffect(stage) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) { last = now; return@withFrameNanos }
                val dt = min(0.05f, (now - last) / 1_000_000_000f)
                last = now
                time += dt
                if (stage == Stage.Settle && settleProgress < 1f) {
                    settleProgress = (settleProgress + dt / 3f).coerceAtMost(1f)
                }
            }
        }
    }

    // Focus sharpness peaks around 0.6.
    val sharp = (1f - (abs(focus - 0.6f) / 0.45f)).coerceIn(0f, 1f)
    if (sharp > 0.85f && stage == Stage.Microscope) focusedSharp = true

    // Progress milestones.
    LaunchedEffect(madeSuspension, viewedScope, changedMag, focusedSharp) {
        var p = 0f
        if (madeSuspension) p = 0.25f
        if (madeSuspension && viewedScope) p = 0.5f
        if (madeSuspension && viewedScope && changedMag) p = 0.75f
        if (madeSuspension && viewedScope && changedMag && focusedSharp) {
            p = 1f
            controls.onComplete(1f)
        }
        controls.onProgress(p)
    }

    val instruction = when (stage) {
        Stage.Soil -> "Spoon moist soil into the beaker — never with bare hands."
        Stage.Water -> "Pour water over the soil."
        Stage.Stir -> "Stir with the glass rod to make a cloudy suspension."
        Stage.Settle -> "Let the mixture settle — heavy bits sink, the top clears."
        Stage.Drop -> "Take a drop from the clear top layer with the dropper."
        Stage.Slide -> "Place the drop on the slide and lower the coverslip."
        Stage.Microscope -> "Focus and change magnification — watch the microbes move."
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                    LLText("SOIL SUSPENSION", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText(instruction, color = t.ink400, size = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    MetricChip("Step", "${stage.order + 1}/7")
                    if (stage == Stage.Microscope) MetricChip("Mag", "${magnification}×")
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(t.accent50)
                            .border(1.dp, t.accent500, RoundedCornerShape(999.dp))
                            .clickable { infoOpen = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText("i", color = t.accent700, size = 14.sp, weight = FontWeight.Bold)
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(t.surface2)) {
                SoilScene(
                    stage = stage,
                    time = time,
                    settleProgress = settleProgress,
                    magnification = magnification,
                    sharp = sharp,
                    microbes = microbes,
                    debris = debris,
                )
                if (infoOpen) {
                    MicrobeInfoPopover(onDismiss = { infoOpen = false })
                }
            }
        }

        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Controls card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText("BENCH", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)

                if (stage != Stage.Microscope) {
                    val nextLabel = when (stage) {
                        Stage.Soil -> "Add water"
                        Stage.Water -> "Stir"
                        Stage.Stir -> "Let it settle"
                        Stage.Settle -> "Take a drop"
                        Stage.Drop -> "Place on slide"
                        Stage.Slide -> "View under microscope"
                        Stage.Microscope -> ""
                    }
                    val canAdvance = stage != Stage.Settle || settleProgress >= 1f
                    PrimaryButton(
                        label = if (stage == Stage.Settle && settleProgress < 1f) "Settling…" else nextLabel,
                        onClick = {
                            stage = Stage.entries[stage.order + 1]
                            if (stage == Stage.Stir) madeSuspension = true
                            if (stage == Stage.Microscope) viewedScope = true
                        },
                        enabled = canAdvance,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LLText("Magnification", color = t.ink200, size = 12.sp,
                        weight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(40, 100, 400).forEach { m ->
                            val sel = magnification == m
                            val bg = if (sel) t.accent600 else t.surface2
                            val fg = if (sel) Color.White else t.ink200
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bg)
                                    .clickable {
                                        if (m != magnification) changedMag = true
                                        magnification = m
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) { LLText("${m}×", color = fg, size = 13.sp, weight = FontWeight.SemiBold) }
                        }
                    }
                    LLSlider(
                        label = "Focus", value = focus, onValueChange = { focus = it },
                        min = 0f, max = 1f, step = null, unit = "",
                        info = "Turn the focus knob until the organisms become sharp.",
                        valueFormat = { if (sharp > 0.85f) "sharp" else "%.0f%%".format(it * 100) },
                    )
                    SecondaryButton(
                        label = "Start over",
                        onClick = {
                            stage = Stage.Soil
                            settleProgress = 0f
                            magnification = 100
                            focus = 0.25f
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // What to look for
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
                LLText("• The stirred water looks dirty — those are fine soil particles in suspension.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• A drop from the top layer still hides tiny living things.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Under the microscope you see small organisms moving on their own.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• Higher magnification reveals the smallest of all — bacteria.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
                LLText("• These microorganisms (microbes) are too small to see with the naked eye.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun SoilScene(
    stage: Stage,
    time: Float,
    settleProgress: Float,
    magnification: Int,
    sharp: Float,
    microbes: List<Microbe>,
    debris: List<Debris>,
) {
    val textMeasurer = rememberTextMeasurer()
    val t = LL.tokens
    val inkLabel = t.ink400
    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (stage == Stage.Microscope) {
                drawMicroscope(textMeasurer, time, magnification, sharp, microbes, debris, inkLabel)
            } else {
                drawPrep(textMeasurer, stage, time, settleProgress, inkLabel)
            }
        }
    }
}

// ── Preparation stages: beaker, rod, dropper, slide ──
private fun DrawScope.drawPrep(
    textMeasurer: TextMeasurer,
    stage: Stage,
    time: Float,
    settleProgress: Float,
    inkLabel: Color,
) {
    val w = size.width; val h = size.height
    val cx = w / 2f
    // Beaker geometry
    val bw = min(w, h) * 0.34f
    val bh = min(w, h) * 0.5f
    val bx = cx - bw / 2f
    val by = h * 0.30f
    val glass = Color(0xFF94A3B8)
    val rim = Color(0xFF64748B)

    val soilDark = Color(0xFF6B3F18)
    val soilLight = Color(0xFF8B5A2B)
    val cloudy = Color(0xFFB08D57)
    val clearWater = Color(0xFFBAE6FD)

    // Liquid level depends on stage
    val hasWater = stage.order >= Stage.Water.order
    val liquidTop = by + bh * 0.22f
    val liquidBottom = by + bh - 4f

    // Beaker back (liquid fill first, then glass outline)
    if (hasWater) {
        when (stage) {
            Stage.Water -> {
                // water sitting over a soil mound
                drawRect(clearWater.copy(alpha = 0.5f),
                    topLeft = Offset(bx + 4f, liquidTop),
                    size = Size(bw - 8f, liquidBottom - liquidTop))
            }
            Stage.Stir -> {
                drawRect(cloudy.copy(alpha = 0.85f),
                    topLeft = Offset(bx + 4f, liquidTop),
                    size = Size(bw - 8f, liquidBottom - liquidTop))
            }
            Stage.Settle, Stage.Drop, Stage.Slide -> {
                // top clears, sediment grows at the bottom as it settles
                val clearFrac = if (stage == Stage.Settle) settleProgress else 1f
                val sedimentH = (bh * 0.18f) * clearFrac + bh * 0.04f
                val muddyTop = liquidTop
                val muddyBottom = liquidBottom - sedimentH
                // upper layer: cloudy → clear
                val topColor = lerp(cloudy.copy(alpha = 0.85f), clearWater.copy(alpha = 0.45f), clearFrac)
                drawRect(topColor,
                    topLeft = Offset(bx + 4f, muddyTop),
                    size = Size(bw - 8f, muddyBottom - muddyTop))
                // sediment
                drawRect(soilDark.copy(alpha = 0.9f),
                    topLeft = Offset(bx + 4f, muddyBottom),
                    size = Size(bw - 8f, liquidBottom - muddyBottom))
            }
            else -> {}
        }
    }
    // Soil mound at the bottom (before water mixes it in)
    if (stage == Stage.Soil || stage == Stage.Water) {
        val moundH = bh * 0.22f
        val moundTop = liquidBottom - moundH
        val moundPath = Path().apply {
            moveTo(bx + 6f, liquidBottom)
            cubicTo(bx + bw * 0.3f, moundTop, bx + bw * 0.7f, moundTop, bx + bw - 6f, liquidBottom)
            close()
        }
        drawPath(moundPath, soilLight)
        drawPath(moundPath, soilDark, style = Stroke(1.5f))
        // soil specks
        for (i in 0 until 14) {
            val sx = bx + 10f + (i * 37 % (bw - 20f).toInt())
            val sy = liquidBottom - 4f - (i * 13 % (moundH * 0.7f).toInt())
            drawCircle(soilDark, 2f, Offset(sx, sy))
        }
    }
    // Suspension specks floating while stirring
    if (stage == Stage.Stir) {
        for (i in 0 until 26) {
            val a = i * 0.61f + time * 1.5f
            val rr = (bw * 0.32f) * (0.3f + 0.7f * ((i * 7 % 10) / 10f))
            val sx = cx + cos(a) * rr * 0.6f
            val sy = (liquidTop + liquidBottom) / 2f + sin(a * 1.3f) * (liquidBottom - liquidTop) * 0.3f
            drawCircle(soilDark.copy(alpha = 0.6f), 2.5f, Offset(sx, sy))
        }
    }

    // Glass beaker outline
    drawRect(glass.copy(alpha = 0.12f), topLeft = Offset(bx, by), size = Size(bw, bh))
    drawLine(glass, Offset(bx, by), Offset(bx, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx + bw, by), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx, by + bh), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    // pour lip
    drawLine(rim, Offset(bx - 6f, by), Offset(bx + 10f, by), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx + bw - 10f, by), Offset(bx + bw + 6f, by), strokeWidth = 3f, cap = StrokeCap.Round)

    // Tools per stage
    when (stage) {
        Stage.Soil -> {
            // spoon dipping in
            val handleEnd = Offset(bx + bw + 60f, by - 30f)
            val scoop = Offset(cx, by + bh * 0.55f)
            drawLine(Color(0xFF9CA3AF), scoop, handleEnd, strokeWidth = 5f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFCBD5E1), 12f, scoop)
            drawCircle(Color(0xFF6B3F18), 8f, scoop)
            drawTextAt(textMeasurer, "Use a spoon — not your hands",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Water -> {
            // pouring stream from top-right
            val sx = bx + bw + 30f
            drawLine(clearWater, Offset(sx, by - 40f), Offset(bx + bw * 0.6f, liquidTop),
                strokeWidth = 4f, cap = StrokeCap.Round)
            drawTextAt(textMeasurer, "Add water", Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Stir -> {
            // glass rod, wobbling
            val wob = sin(time * 6f) * 14f
            drawLine(Color(0xFFE2E8F0),
                Offset(cx + wob, by - 36f),
                Offset(cx - wob * 0.4f, liquidBottom - 10f),
                strokeWidth = 5f, cap = StrokeCap.Round)
            drawTextAt(textMeasurer, "Soil suspension (cloudy water)",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Settle -> {
            drawTextAt(textMeasurer,
                if (settleProgress < 1f) "Settling…" else "Top layer is clear now",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Drop -> {
            // dropper above the clear top layer
            val dx = cx
            val droTop = by - 60f
            drawRoundRectCompat(Color(0xFFCBD5E1), dx - 7f, droTop, 14f, 46f, 6f)
            drawLine(Color(0xFF94A3B8), Offset(dx, droTop + 46f), Offset(dx, liquidTop - 8f),
                strokeWidth = 3f)
            // a drop from the top (clear) layer
            val drop = (time * 0.6f) % 1f
            val dropY = liquidTop - 8f + drop * 18f
            drawCircle(clearWater, 5f, Offset(dx, dropY))
            drawTextAt(textMeasurer, "One drop from the top layer",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Slide -> {
            // a glass slide with drop + coverslip, drawn below the beaker area
            val slY = by + bh + 40f
            val slX = cx - bw * 0.6f
            val slW = bw * 1.2f
            drawRect(Color(0xFFE2E8F0).copy(alpha = 0.5f),
                topLeft = Offset(slX, slY), size = Size(slW, 26f))
            drawRect(Color(0xFF94A3B8), topLeft = Offset(slX, slY), size = Size(slW, 26f),
                style = Stroke(1.5f))
            // coverslip
            drawRect(Color(0xFFBAE6FD).copy(alpha = 0.45f),
                topLeft = Offset(cx - 26f, slY + 3f), size = Size(52f, 20f))
            drawRect(Color(0xFF7DD3FC), topLeft = Offset(cx - 26f, slY + 3f), size = Size(52f, 20f),
                style = Stroke(1f))
            drawTextAt(textMeasurer, "Slide + coverslip ready", Offset(slX, slY + 36f), inkLabel, 11.sp)
        }
        else -> {}
    }
}

// ── Microscope field of view ──
private fun DrawScope.drawMicroscope(
    textMeasurer: TextMeasurer,
    time: Float,
    magnification: Int,
    sharp: Float,
    microbes: List<Microbe>,
    debris: List<Debris>,
    inkLabel: Color,
) {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h / 2f
    val fieldR = min(w, h) * 0.42f

    // Outside the eyepiece — dark surround
    drawRect(Color(0xFF0B0B0F), topLeft = Offset.Zero, size = size)

    val circle = Path().apply { addOval(Rect(cx - fieldR, cy - fieldR, cx + fieldR, cy + fieldR)) }
    clipPath(circle) {
        // fluid background (pale soil-water)
        drawRect(Color(0xFFF2EFD6), topLeft = Offset.Zero, size = size)
        drawRect(Color(0xFFE6E2BC).copy(alpha = 0.5f),
            topLeft = Offset(0f, cy), size = Size(w, cy))

        val magScale = when (magnification) { 40 -> 0.7f; 100 -> 1.0f; else -> 1.7f }
        val blur = 1f - sharp

        // soil debris specks (always present, non-living)
        for (d in debris) {
            val px = cx + (d.x + sin(time * 0.2f + d.drift) * 0.02f) * fieldR
            val py = cy + (d.y + cos(time * 0.15f + d.drift) * 0.02f) * fieldR
            drawCircle(Color(0xFF8B7355).copy(alpha = 0.5f * (0.4f + 0.6f * sharp)),
                d.r * fieldR * magScale, Offset(px, py))
        }

        // microorganisms
        for (m in microbes) {
            if (magnification < m.minMag) continue
            // drift + wrap within the field
            val drift = m.speed * time
            var nx = m.startX + cos(m.driftAngle) * drift + sin(time * 0.8f + m.phase) * 0.04f
            var ny = m.startY + sin(m.driftAngle) * drift + cos(time * 0.7f + m.phase) * 0.04f
            nx = ((nx + 1f).mod(2f)) - 1f
            ny = ((ny + 1f).mod(2f)) - 1f
            val px = cx + nx * fieldR
            val py = cy + ny * fieldR
            val r = m.baseSize * fieldR * magScale
            val alpha = (0.35f + 0.65f * sharp)
            // blurred halo when out of focus
            if (blur > 0.1f) {
                drawCircle(Color(0xFF4D7C0F).copy(alpha = 0.12f * blur),
                    r * (1.6f + blur), Offset(px, py))
            }
            when (m.type) {
                MicrobeType.Ciliate -> drawCiliate(px, py, r, time + m.phase, sharp)
                MicrobeType.Amoeba -> drawAmoeba(px, py, r, time + m.phase, sharp)
                MicrobeType.Flagellate -> drawFlagellate(px, py, r, time + m.phase, sharp)
                MicrobeType.Rod -> drawRod(px, py, r, time + m.phase, alpha)
            }
        }
    }

    // Eyepiece vignette ring
    drawCircle(Color(0xFF0B0B0F), fieldR + 40f, Offset(cx, cy), style = Stroke(80f))
    drawCircle(Color(0xFF3F3F46), fieldR, Offset(cx, cy), style = Stroke(3f))

    // Scale bar + magnification readout (bottom of field)
    val barY = cy + fieldR - 22f
    val barX = cx - fieldR * 0.5f
    drawLine(Color(0xFF1F2937), Offset(barX, barY), Offset(barX + fieldR * 0.3f, barY),
        strokeWidth = 3f, cap = StrokeCap.Round)
    drawTextAt(textMeasurer, "${magnification}×", Offset(cx + fieldR * 0.2f, barY - 10f),
        Color(0xFF1F2937), 12.sp, FontWeight.Bold)
    if (sharp <= 0.85f) {
        drawCenteredText(textMeasurer, "Adjust focus…", Offset(cx, cy - fieldR - 24f),
            inkLabel, 12.sp)
    }
}

private fun DrawScope.drawCiliate(cx: Float, cy: Float, r: Float, ph: Float, sharp: Float) {
    val body = Color(0xFF65A30D).copy(alpha = 0.30f + 0.45f * sharp)
    val edge = Color(0xFF3F6212).copy(alpha = 0.5f + 0.5f * sharp)
    val wob = sin(ph * 3f) * r * 0.12f
    val rect = Rect(cx - r * 1.5f, cy - r * 0.7f + wob, cx + r * 1.5f, cy + r * 0.7f + wob)
    drawOval(body, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height))
    drawOval(edge, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height),
        style = Stroke(1.4f))
    // cilia
    val n = 16
    for (i in 0 until n) {
        val a = i * (2f * PI.toFloat() / n)
        val ex = cx + cos(a) * r * 1.5f
        val ey = cy + sin(a) * r * 0.7f + wob
        val beat = sin(ph * 6f + i) * r * 0.18f
        drawLine(edge, Offset(ex, ey),
            Offset(ex + cos(a) * (r * 0.3f) + beat * sin(a), ey + sin(a) * (r * 0.3f) + beat * cos(a)),
            strokeWidth = 1f)
    }
    // macronucleus
    drawCircle(Color(0xFF365314).copy(alpha = 0.4f + 0.4f * sharp), r * 0.3f, Offset(cx, cy + wob))
}

private fun DrawScope.drawAmoeba(cx: Float, cy: Float, r: Float, ph: Float, sharp: Float) {
    val body = Color(0xFF0EA5E9).copy(alpha = 0.18f + 0.25f * sharp)
    val edge = Color(0xFF0369A1).copy(alpha = 0.4f + 0.5f * sharp)
    val lobes = 7
    val path = Path()
    for (i in 0..lobes) {
        val a = i * (2f * PI.toFloat() / lobes)
        val pseudo = 1f + 0.28f * sin(ph * 1.5f + i * 1.7f)
        val px = cx + cos(a) * r * 1.4f * pseudo
        val py = cy + sin(a) * r * 1.4f * pseudo
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, body)
    drawPath(path, edge, style = Stroke(1.4f))
    drawCircle(Color(0xFF7C3AED).copy(alpha = 0.4f + 0.4f * sharp), r * 0.32f,
        Offset(cx + sin(ph) * r * 0.2f, cy + cos(ph) * r * 0.2f))
}

private fun DrawScope.drawFlagellate(cx: Float, cy: Float, r: Float, ph: Float, sharp: Float) {
    val body = Color(0xFFCA8A04).copy(alpha = 0.35f + 0.4f * sharp)
    val edge = Color(0xFF854D0E).copy(alpha = 0.5f + 0.4f * sharp)
    drawOval(body, topLeft = Offset(cx - r * 0.8f, cy - r), size = Size(r * 1.6f, r * 2f))
    drawOval(edge, topLeft = Offset(cx - r * 0.8f, cy - r), size = Size(r * 1.6f, r * 2f),
        style = Stroke(1.2f))
    // whipping flagellum
    val tail = Path().apply {
        moveTo(cx, cy + r)
        cubicTo(cx + sin(ph * 8f) * r, cy + r * 2f,
            cx - sin(ph * 8f + 1f) * r, cy + r * 3f,
            cx + sin(ph * 8f + 2f) * r * 0.6f, cy + r * 4f)
    }
    drawPath(tail, edge, style = Stroke(1.2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawRod(cx: Float, cy: Float, r: Float, ph: Float, alpha: Float) {
    val body = Color(0xFF7C2D12).copy(alpha = 0.5f * alpha)
    val ang = ph * 0.5f
    val dx = cos(ang) * r * 1.4f
    val dy = sin(ang) * r * 1.4f
    drawLine(body, Offset(cx - dx, cy - dy), Offset(cx + dx, cy + dy),
        strokeWidth = r * 0.9f, cap = StrokeCap.Round)
}

@Composable
private fun MicrobeInfoPopover(onDismiss: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.lineStrong, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .clickable(enabled = false, onClick = {}),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LLText("WHAT AM I LOOKING AT?", color = t.accent700, size = 12.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            LLText(
                "The drop of soil water holds tiny living things that move on their own. " +
                    "They are far too small to see with the unaided eye — only the microscope reveals them.",
                color = t.ink200, size = 14.sp, lineHeight = 20.sp,
            )
            LLText("• These are microorganisms, or microbes (micro = very small).",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            LLText("• Soil and pond water are full of them — the same kind of moving creatures.",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            LLText("• Bigger ones (like ciliates and amoebae) show at low power; bacteria need high power.",
                color = t.ink400, size = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SecondaryButton("Got it", onClick = onDismiss)
            }
        }
    }
}

// ── small helpers ──
private fun lerp(a: Color, b: Color, f: Float): Color = Color(
    red = a.red + (b.red - a.red) * f,
    green = a.green + (b.green - a.green) * f,
    blue = a.blue + (b.blue - a.blue) * f,
    alpha = a.alpha + (b.alpha - a.alpha) * f,
)

private fun DrawScope.drawRoundRectCompat(
    color: Color, left: Float, top: Float, w: Float, h: Float, radius: Float,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
    )
}

private fun DrawScope.drawTextAt(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight))
    drawText(textLayoutResult = layout, topLeft = topLeft)
}

private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    center: Offset,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight))
    drawText(textLayoutResult = layout,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}
