package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
 * Lab-protocol-checklist redesign. Left: live beaker/slide/microscope scene.
 * Right: NCERT procedure as six numbered steps that auto-check as the student
 * performs each one. Same procedure as the prior staged design, different
 * interaction shape (modelled after LabReagentScreen's two-column layout).
 */

private enum class Stage { Soil, Water, Stir, Settle, Drop, Slide, Microscope }

private enum class Step(val heading: String, val sub: String) {
    Collect("Spoon soil into the beaker",
        "Use a spoon or gloves — never touch moist soil with bare hands."),
    WaterAndStir("Pour water and stir",
        "Add water, stir with the glass rod until the liquid looks dirty."),
    Settle("Let it settle",
        "Set the beaker aside until the top layer clears."),
    Drop("Take a drop from the top",
        "Use the dropper to collect one drop of clear water."),
    Slide("Place on slide & coverslip",
        "Drop on the slide; gently lower the coverslip."),
    Microscope("Observe under the microscope",
        "Adjust magnification and focus until the microbes are sharp."),
}

private enum class RowState { Pending, Active, Done }

private enum class MicrobeType { Ciliate, Amoeba, Rod, Flagellate }

private data class Microbe(
    val type: MicrobeType,
    val startX: Float,
    val startY: Float,
    val baseSize: Float,
    val phase: Float,
    val driftAngle: Float,
    val speed: Float,
    val minMag: Int,
)

private data class Debris(val x: Float, val y: Float, val r: Float, val drift: Float)

private data class OrganismFact(val name: String, val group: String, val description: String)

private val NAMES: Map<MicrobeType, OrganismFact> = mapOf(
    MicrobeType.Ciliate to OrganismFact("Paramecium", "Protozoa",
        "Single slipper-shaped cell; moves with tiny hair-like cilia."),
    MicrobeType.Amoeba to OrganismFact("Amoeba", "Protozoa",
        "A single cell with no fixed shape; crawls with finger-like pseudopodia."),
    MicrobeType.Flagellate to OrganismFact("Algae", "Alga",
        "Single cell, green from chlorophyll; moves with whip-like flagella."),
    MicrobeType.Rod to OrganismFact("Bacteria", "Bacteria",
        "The smallest; rod, comma, spiral or spherical. Visible only at high power."),
)

private const val SPOTTED_TARGET = 4
private const val SELECTION_VISIBLE_SECS = 3.5f
private const val SHARP_HITTEST_THRESHOLD = 0.5f

@Composable
fun SoilSuspension(controls: ExperimentControls) {
    val t = LL.tokens

    var currentStep by remember { mutableStateOf(Step.Collect) }
    var done by remember { mutableStateOf(setOf<Step>()) }
    var renderStage by remember { mutableStateOf(Stage.Soil) }
    var time by remember { mutableStateOf(0f) }
    var settleProgress by remember { mutableStateOf(0f) }
    var settling by remember { mutableStateOf(false) }
    var stirRemaining by remember { mutableStateOf(0f) }
    var magnification by remember { mutableStateOf(100) }
    var focus by remember { mutableStateOf(0.25f) }
    var infoOpen by remember { mutableStateOf(false) }

    // Tap-to-identify state
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var selectionTime by remember { mutableStateOf(-99f) }
    var spotted by remember { mutableStateOf(setOf<MicrobeType>()) }

    val sharp = (1f - (abs(focus - 0.6f) / 0.45f)).coerceIn(0f, 1f)

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

    // Single frame loop drives time, stir countdown, settle progress.
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) { last = now; return@withFrameNanos }
                val dt = min(0.05f, (now - last) / 1_000_000_000f)
                last = now
                time += dt
                if (stirRemaining > 0f) {
                    stirRemaining -= dt
                    if (stirRemaining <= 0f) {
                        stirRemaining = 0f
                        done = done + Step.WaterAndStir
                        if (currentStep == Step.WaterAndStir) currentStep = Step.Settle
                    }
                }
                if (settling && settleProgress < 1f) {
                    settleProgress = (settleProgress + dt / 3f).coerceAtMost(1f)
                    if (settleProgress >= 1f) {
                        settling = false
                        done = done + Step.Settle
                        if (currentStep == Step.Settle) currentStep = Step.Drop
                    }
                }
            }
        }
    }

    // Step 6 auto-advances the scene; completion is gated on identifying all 4 organism types.
    LaunchedEffect(currentStep) {
        if (currentStep == Step.Microscope) renderStage = Stage.Microscope
    }
    LaunchedEffect(spotted, currentStep) {
        if (currentStep == Step.Microscope && spotted.size >= SPOTTED_TARGET &&
            Step.Microscope !in done) {
            done = done + Step.Microscope
            controls.onComplete(1f)
        }
    }

    // Auto-clear selection ~3.5 s after a tap so successive identifications work.
    LaunchedEffect(time, selectedIdx) {
        if (selectedIdx != null && time - selectionTime > SELECTION_VISIBLE_SECS) {
            selectedIdx = null
        }
    }

    // Progress milestones — steps 1-5 unchanged shape; step 6 climbs with spotted count.
    LaunchedEffect(done, spotted) {
        var p = 0f
        if (Step.WaterAndStir in done) p = 0.25f
        if (Step.Slide in done) {
            p = 0.50f + 0.50f * (spotted.size / SPOTTED_TARGET.toFloat())
        }
        if (Step.Microscope in done) p = 1f
        controls.onProgress(p)
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Scene card ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("SOIL SUSPENSION", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText(currentStep.heading, color = t.ink400, size = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(t.accent50)
                        .border(1.dp, t.accent500, CircleShape)
                        .clickable { infoOpen = true },
                    contentAlignment = Alignment.Center,
                ) {
                    LLText("i", color = t.accent700, size = 14.sp, weight = FontWeight.Bold)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(t.surface2)) {
                SoilScene(
                    stage = renderStage,
                    time = time,
                    settleProgress = settleProgress,
                    magnification = magnification,
                    focus = focus,
                    sharp = sharp,
                    microbes = microbes,
                    debris = debris,
                    selectedIdx = selectedIdx,
                    spotted = spotted,
                    onSelectMicrobe = { idx ->
                        selectedIdx = idx
                        selectionTime = time
                        spotted = spotted + microbes[idx].type
                    },
                )
                if (infoOpen) MicrobeInfoPopover(onDismiss = { infoOpen = false })
            }
        }

        // ── Procedure column ──
        Column(
            modifier = Modifier.width(360.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText("PROCEDURE", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Step.entries.forEachIndexed { idx, step ->
                    val state = when {
                        step in done -> RowState.Done
                        step == currentStep -> RowState.Active
                        else -> RowState.Pending
                    }
                    ProcedureRow(
                        index = idx + 1,
                        step = step,
                        state = state,
                        stirRunning = step == Step.WaterAndStir && stirRemaining > 0f,
                        settling = step == Step.Settle && settling,
                        settleFrac = settleProgress,
                        magnification = magnification,
                        focus = focus,
                        sharp = sharp,
                        spottedCount = spotted.size,
                        onDoIt = {
                            when (step) {
                                Step.Collect -> {
                                    renderStage = Stage.Soil
                                    done = done + Step.Collect
                                    currentStep = Step.WaterAndStir
                                }
                                Step.WaterAndStir -> {
                                    renderStage = Stage.Stir
                                    stirRemaining = 1.0f
                                }
                                Step.Settle -> {
                                    renderStage = Stage.Settle
                                    settleProgress = 0f
                                    settling = true
                                }
                                Step.Drop -> {
                                    renderStage = Stage.Drop
                                    done = done + Step.Drop
                                    currentStep = Step.Slide
                                }
                                Step.Slide -> {
                                    renderStage = Stage.Slide
                                    done = done + Step.Slide
                                    currentStep = Step.Microscope
                                }
                                Step.Microscope -> { /* completion gated on spotted == 4 */ }
                            }
                        },
                        onMagChange = { m -> magnification = m },
                        onFocusChange = { focus = it },
                    )
                }
            }
            SecondaryButton(
                label = "Start over",
                onClick = {
                    currentStep = Step.Collect
                    done = emptySet()
                    renderStage = Stage.Soil
                    settling = false
                    settleProgress = 0f
                    stirRemaining = 0f
                    magnification = 100
                    focus = 0.25f
                    selectedIdx = null
                    selectionTime = -99f
                    spotted = emptySet()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProcedureRow(
    index: Int,
    step: Step,
    state: RowState,
    stirRunning: Boolean,
    settling: Boolean,
    settleFrac: Float,
    magnification: Int,
    focus: Float,
    sharp: Float,
    spottedCount: Int,
    onDoIt: () -> Unit,
    onMagChange: (Int) -> Unit,
    onFocusChange: (Float) -> Unit,
) {
    val t = LL.tokens
    val active = state == RowState.Active
    val done = state == RowState.Done
    val bg = if (active) t.accent50 else Color.Transparent
    val border = if (active) t.accent500.copy(alpha = 0.45f) else t.line
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Marker
            val markerBg = when (state) {
                RowState.Done -> t.accent500
                RowState.Active -> t.accent50
                RowState.Pending -> Color.Transparent
            }
            val markerBorder = when (state) {
                RowState.Done -> t.accent500
                RowState.Active -> t.accent500
                RowState.Pending -> t.line
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(markerBg)
                    .border(1.5.dp, markerBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (done) {
                    LLText("✓", color = Color.White, size = 13.sp, weight = FontWeight.Bold)
                } else {
                    LLText("$index",
                        color = if (active) t.accent700 else t.ink400,
                        size = 12.sp, weight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                LLText(step.heading,
                    color = if (done) t.ink400 else t.ink50,
                    size = 13.sp, weight = FontWeight.SemiBold)
                LLText(step.sub,
                    color = if (done) t.ink500 else t.ink400,
                    size = 11.sp, lineHeight = 14.sp)
            }
        }

        if (active) {
            when (step) {
                Step.Collect -> PrimaryButton(label = "Add the soil", onClick = onDoIt,
                    modifier = Modifier.fillMaxWidth())
                Step.Drop -> PrimaryButton(label = "Take a drop", onClick = onDoIt,
                    modifier = Modifier.fillMaxWidth())
                Step.Slide -> PrimaryButton(label = "Place the coverslip", onClick = onDoIt,
                    modifier = Modifier.fillMaxWidth())
                Step.WaterAndStir -> {
                    PrimaryButton(
                        label = if (stirRunning) "Stirring…" else "Pour & stir",
                        onClick = onDoIt,
                        enabled = !stirRunning,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Step.Settle -> {
                    PrimaryButton(
                        label = if (settling) "Settling… ${(settleFrac * 100).toInt()}%"
                                else "Let it settle",
                        onClick = onDoIt,
                        enabled = !settling,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Step.Microscope -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LLText("Tap each organism to identify it",
                            color = t.ink200, size = 11.sp, weight = FontWeight.Medium)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (spottedCount == SPOTTED_TARGET) t.accent500
                                            else t.accent50)
                                .border(1.dp, t.accent500, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            LLText("Spotted $spottedCount/$SPOTTED_TARGET",
                                color = if (spottedCount == SPOTTED_TARGET) Color.White
                                        else t.accent700,
                                size = 11.sp, weight = FontWeight.SemiBold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(40, 100, 400).forEach { m ->
                            val sel = magnification == m
                            val bgB = if (sel) t.accent600 else t.surface2
                            val fg = if (sel) Color.White else t.ink200
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgB)
                                    .clickable { onMagChange(m) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) { LLText("${m}×", color = fg, size = 12.sp, weight = FontWeight.SemiBold) }
                        }
                    }
                    LLSlider(
                        label = "Focus", value = focus, onValueChange = onFocusChange,
                        min = 0f, max = 1f, step = null, unit = "",
                        info = "Sharp focus is required to identify a microbe.",
                        valueFormat = { if (sharp > 0.85f) "sharp" else "%.0f%%".format(it * 100) },
                    )
                }
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
    focus: Float,
    sharp: Float,
    microbes: List<Microbe>,
    debris: List<Debris>,
    selectedIdx: Int?,
    spotted: Set<MicrobeType>,
    onSelectMicrobe: (Int) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val t = LL.tokens
    val inkLabel = t.ink400

    // Snapshots so the pointerInput lambda sees current values without re-keying every frame.
    val liveTime by rememberUpdatedState(time)
    val liveMag by rememberUpdatedState(magnification)
    val liveSharp by rememberUpdatedState(sharp)
    val liveMicrobes by rememberUpdatedState(microbes)
    val liveOnSelect by rememberUpdatedState(onSelectMicrobe)

    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stage) {
                    if (stage != Stage.Microscope) return@pointerInput
                    detectTapGestures { tap ->
                        val w = size.width.toFloat(); val h = size.height.toFloat()
                        val cx = w / 2f; val cy = h / 2f
                        val fieldR = min(w, h) * 0.42f
                        if (liveSharp < SHARP_HITTEST_THRESHOLD) return@detectTapGestures
                        val tapDx = tap.x - cx; val tapDy = tap.y - cy
                        if (tapDx * tapDx + tapDy * tapDy > fieldR * fieldR) return@detectTapGestures
                        val magScale = when (liveMag) { 40 -> 0.7f; 100 -> 1.0f; else -> 1.7f }
                        var bestIdx: Int? = null
                        var bestDist = Float.MAX_VALUE
                        liveMicrobes.forEachIndexed { idx, m ->
                            if (liveMag < m.minMag) return@forEachIndexed
                            val pos = microbeScreenPos(m, liveTime, cx, cy, fieldR)
                            val r = m.baseSize * fieldR * magScale
                            val hitR = r * 1.6f
                            val dx = tap.x - pos.x; val dy = tap.y - pos.y
                            val dd = dx * dx + dy * dy
                            if (dd < hitR * hitR && dd < bestDist) {
                                bestDist = dd
                                bestIdx = idx
                            }
                        }
                        bestIdx?.let { liveOnSelect(it) }
                    }
                },
        ) {
            if (stage == Stage.Microscope) {
                drawMicroscope(textMeasurer, time, magnification, focus, sharp,
                    microbes, debris, selectedIdx, inkLabel)
            } else {
                drawPrep(textMeasurer, stage, time, settleProgress, inkLabel)
            }
        }
        // Identification card overlay
        if (stage == Stage.Microscope && selectedIdx != null) {
            val sel = microbes.getOrNull(selectedIdx)
            val fact = sel?.let { NAMES[it.type] }
            if (fact != null) {
                IdentificationCard(
                    fact = fact,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun IdentificationCard(fact: OrganismFact, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface)
            .border(1.dp, t.accent500.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LLText(fact.name, color = t.ink50, size = 15.sp, weight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(t.accent50)
                    .border(1.dp, t.accent500.copy(alpha = 0.5f),
                        RoundedCornerShape(999.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                LLText(fact.group, color = t.accent700, size = 10.sp,
                    weight = FontWeight.SemiBold)
            }
        }
        LLText(fact.description, color = t.ink400, size = 11.sp, lineHeight = 14.sp)
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

    val hasWater = stage in listOf(Stage.Water, Stage.Stir, Stage.Settle, Stage.Drop, Stage.Slide)
    val liquidTop = by + bh * 0.22f
    val liquidBottom = by + bh - 4f

    if (hasWater) {
        when (stage) {
            Stage.Water -> {
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
                val clearFrac = if (stage == Stage.Settle) settleProgress else 1f
                val sedimentH = (bh * 0.18f) * clearFrac + bh * 0.04f
                val muddyTop = liquidTop
                val muddyBottom = liquidBottom - sedimentH
                val topColor = lerp(cloudy.copy(alpha = 0.85f),
                    clearWater.copy(alpha = 0.45f), clearFrac)
                drawRect(topColor,
                    topLeft = Offset(bx + 4f, muddyTop),
                    size = Size(bw - 8f, muddyBottom - muddyTop))
                drawRect(soilDark.copy(alpha = 0.9f),
                    topLeft = Offset(bx + 4f, muddyBottom),
                    size = Size(bw - 8f, liquidBottom - muddyBottom))
            }
            else -> {}
        }
    }
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
        for (i in 0 until 14) {
            val sx = bx + 10f + (i * 37 % (bw - 20f).toInt())
            val sy = liquidBottom - 4f - (i * 13 % (moundH * 0.7f).toInt())
            drawCircle(soilDark, 2f, Offset(sx, sy))
        }
    }
    if (stage == Stage.Stir) {
        for (i in 0 until 26) {
            val a = i * 0.61f + time * 1.5f
            val rr = (bw * 0.32f) * (0.3f + 0.7f * ((i * 7 % 10) / 10f))
            val sx = cx + cos(a) * rr * 0.6f
            val sy = (liquidTop + liquidBottom) / 2f + sin(a * 1.3f) * (liquidBottom - liquidTop) * 0.3f
            drawCircle(soilDark.copy(alpha = 0.6f), 2.5f, Offset(sx, sy))
        }
    }

    drawRect(glass.copy(alpha = 0.12f), topLeft = Offset(bx, by), size = Size(bw, bh))
    drawLine(glass, Offset(bx, by), Offset(bx, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx + bw, by), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(glass, Offset(bx, by + bh), Offset(bx + bw, by + bh), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx - 6f, by), Offset(bx + 10f, by), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(rim, Offset(bx + bw - 10f, by), Offset(bx + bw + 6f, by), strokeWidth = 3f, cap = StrokeCap.Round)

    when (stage) {
        Stage.Soil -> {
            val handleEnd = Offset(bx + bw + 60f, by - 30f)
            val scoop = Offset(cx, by + bh * 0.55f)
            drawLine(Color(0xFF9CA3AF), scoop, handleEnd, strokeWidth = 5f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFCBD5E1), 12f, scoop)
            drawCircle(Color(0xFF6B3F18), 8f, scoop)
            drawTextAt(textMeasurer, "Use a spoon — not your hands",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Water -> {
            val sx = bx + bw + 30f
            drawLine(clearWater, Offset(sx, by - 40f), Offset(bx + bw * 0.6f, liquidTop),
                strokeWidth = 4f, cap = StrokeCap.Round)
            drawTextAt(textMeasurer, "Add water", Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Stir -> {
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
            val dx = cx
            val droTop = by - 60f
            drawRoundRectCompat(Color(0xFFCBD5E1), dx - 7f, droTop, 14f, 46f, 6f)
            drawLine(Color(0xFF94A3B8), Offset(dx, droTop + 46f), Offset(dx, liquidTop - 8f),
                strokeWidth = 3f)
            val drop = (time * 0.6f) % 1f
            val dropY = liquidTop - 8f + drop * 18f
            drawCircle(clearWater, 5f, Offset(dx, dropY))
            drawTextAt(textMeasurer, "One drop from the top layer",
                Offset(bx, by + bh + 14f), inkLabel, 11.sp)
        }
        Stage.Slide -> {
            val slY = by + bh + 40f
            val slX = cx - bw * 0.6f
            val slW = bw * 1.2f
            drawRect(Color(0xFFE2E8F0).copy(alpha = 0.5f),
                topLeft = Offset(slX, slY), size = Size(slW, 26f))
            drawRect(Color(0xFF94A3B8), topLeft = Offset(slX, slY), size = Size(slW, 26f),
                style = Stroke(1.5f))
            drawRect(Color(0xFFBAE6FD).copy(alpha = 0.45f),
                topLeft = Offset(cx - 26f, slY + 3f), size = Size(52f, 20f))
            drawRect(Color(0xFF7DD3FC), topLeft = Offset(cx - 26f, slY + 3f), size = Size(52f, 20f),
                style = Stroke(1f))
            drawTextAt(textMeasurer, "Slide + coverslip ready",
                Offset(slX, slY + 36f), inkLabel, 11.sp)
        }
        else -> {}
    }
}

// Projection used by both rendering and hit testing.
private fun microbeScreenPos(
    m: Microbe, time: Float, cx: Float, cy: Float, fieldR: Float,
): Offset {
    val drift = m.speed * time
    var nx = m.startX + cos(m.driftAngle) * drift + sin(time * 0.8f + m.phase) * 0.04f
    var ny = m.startY + sin(m.driftAngle) * drift + cos(time * 0.7f + m.phase) * 0.04f
    nx = ((nx + 1f).mod(2f)) - 1f
    ny = ((ny + 1f).mod(2f)) - 1f
    return Offset(cx + nx * fieldR, cy + ny * fieldR)
}

// ── Microscope field of view ──
private fun DrawScope.drawMicroscope(
    textMeasurer: TextMeasurer,
    time: Float,
    magnification: Int,
    focus: Float,
    sharp: Float,
    microbes: List<Microbe>,
    debris: List<Debris>,
    selectedIdx: Int?,
    inkLabel: Color,
) {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h / 2f
    val fieldR = min(w, h) * 0.42f

    // Background fill behind the chrome.
    drawRect(Color(0xFF111418), topLeft = Offset.Zero, size = size)
    // Microscope body around the eyepiece.
    drawMicroscopeBody(textMeasurer, cx, cy, fieldR, magnification, focus)

    val circle = Path().apply { addOval(Rect(cx - fieldR, cy - fieldR, cx + fieldR, cy + fieldR)) }
    clipPath(circle) {
        // Slide fluid background
        drawRect(Color(0xFFF2EFD6), topLeft = Offset.Zero, size = size)
        drawRect(Color(0xFFE6E2BC).copy(alpha = 0.5f),
            topLeft = Offset(0f, cy), size = Size(w, cy))

        val magScale = when (magnification) { 40 -> 0.7f; 100 -> 1.0f; else -> 1.7f }
        val blur = 1f - sharp

        // Debris
        for (d in debris) {
            val px = cx + (d.x + sin(time * 0.2f + d.drift) * 0.02f) * fieldR
            val py = cy + (d.y + cos(time * 0.15f + d.drift) * 0.02f) * fieldR
            drawCircle(Color(0xFF8B7355).copy(alpha = 0.5f * (0.4f + 0.6f * sharp)),
                d.r * fieldR * magScale, Offset(px, py))
        }

        // Microbes
        microbes.forEachIndexed { idx, m ->
            // Rods (bacteria) are unresolved at 100× — render as a faint hint streak.
            val isHintOnly = m.minMag == 400 && magnification == 100
            if (magnification < m.minMag && !isHintOnly) return@forEachIndexed

            val pos = microbeScreenPos(m, time, cx, cy, fieldR)
            val r = m.baseSize * fieldR * magScale
            val alpha = (0.35f + 0.65f * sharp)
            if (blur > 0.1f && !isHintOnly) {
                drawCircle(Color(0xFF4D7C0F).copy(alpha = 0.12f * blur),
                    r * (1.6f + blur), Offset(pos.x, pos.y))
            }

            if (isHintOnly) {
                // Faint streak — "something there, but not resolved"
                drawRod(pos.x, pos.y, r * 0.9f, time + m.phase, 0.08f)
            } else {
                when (m.type) {
                    MicrobeType.Ciliate -> drawCiliate(pos.x, pos.y, r, time + m.phase, sharp)
                    MicrobeType.Amoeba -> drawAmoeba(pos.x, pos.y, r, time + m.phase, sharp)
                    MicrobeType.Flagellate -> drawFlagellate(pos.x, pos.y, r, time + m.phase, sharp)
                    MicrobeType.Rod -> drawRod(pos.x, pos.y, r, time + m.phase, alpha)
                }
            }

            // Selection ring on the tapped microbe.
            if (selectedIdx == idx && !isHintOnly) {
                drawCircle(Color(0xFF10B981), r * 1.7f, pos, style = Stroke(2.5f))
            }
        }
    }

    // Eyepiece inner ring + soft vignette over the field edge.
    drawCircle(Color(0xFF0B0B0F), fieldR, Offset(cx, cy), style = Stroke(6f))
    drawCircle(Color(0xFF3F3F46), fieldR, Offset(cx, cy), style = Stroke(2f))

    if (sharp <= SHARP_HITTEST_THRESHOLD) {
        drawCenteredText(textMeasurer, "Adjust focus to identify…",
            Offset(cx, cy - fieldR - 18f), inkLabel, 12.sp)
    } else if (sharp <= 0.85f) {
        drawCenteredText(textMeasurer, "Sharpen the focus",
            Offset(cx, cy - fieldR - 18f), inkLabel, 12.sp)
    }
}

// ── Microscope body chrome: turret, eyepiece tube, stage, focus knobs ──
private fun DrawScope.drawMicroscopeBody(
    textMeasurer: TextMeasurer,
    cx: Float, cy: Float, fieldR: Float,
    magnification: Int,
    focus: Float,
) {
    val frame = Color(0xFF1F2937)
    val frameLight = Color(0xFF374151)
    val accent = Color(0xFF10B981)
    val knob = Color(0xFF4B5563)
    val pip = Color(0xFF6B7280)

    // Eyepiece tube above the field
    val tubeW = fieldR * 0.85f
    val tubeTop = cy - fieldR - fieldR * 0.55f
    drawRoundRectCompat(frame, cx - tubeW / 2f, tubeTop, tubeW, fieldR * 0.40f, 14f)
    drawRoundRectCompat(frameLight, cx - tubeW / 2f + 4f, tubeTop + 4f,
        tubeW - 8f, fieldR * 0.40f - 8f, 12f)

    // Objective turret — 3 short cylinders fanning out below the tube
    val turY = cy - fieldR * 0.95f
    val mags = listOf(40, 100, 400)
    mags.forEachIndexed { i, m ->
        val angle = (i - 1) * 0.45f
        val ox = cx + sin(angle) * fieldR * 0.50f
        val oy = turY + (1f - cos(angle)) * fieldR * 0.10f
        val isActive = m == magnification
        val barrelW = fieldR * 0.14f
        val barrelH = fieldR * 0.22f
        drawRoundRectCompat(if (isActive) accent.copy(alpha = 0.85f) else knob,
            ox - barrelW / 2f, oy, barrelW, barrelH, 6f)
        drawRoundRectCompat(if (isActive) accent else pip,
            ox - barrelW / 2f, oy + barrelH * 0.8f, barrelW, barrelH * 0.20f, 4f)
        drawCenteredText(textMeasurer, "${m}×",
            Offset(ox, oy + barrelH / 2f),
            color = if (isActive) Color.White else Color(0xFFD1D5DB),
            size = 9.sp, weight = FontWeight.Bold)
    }

    // Stage clips below the eyepiece field — a thin plate with two clamps
    val stageY = cy + fieldR + fieldR * 0.18f
    val stageW = fieldR * 2.4f
    drawRoundRectCompat(frame, cx - stageW / 2f, stageY, stageW, fieldR * 0.16f, 6f)
    drawRoundRectCompat(frameLight, cx - stageW / 2f + 3f, stageY + 3f,
        stageW - 6f, fieldR * 0.16f - 6f, 4f)
    // clips
    drawRoundRectCompat(knob, cx - fieldR * 0.35f, stageY - 6f, fieldR * 0.10f, 10f, 3f)
    drawRoundRectCompat(knob, cx + fieldR * 0.25f, stageY - 6f, fieldR * 0.10f, 10f, 3f)
    // slide hint visible under the field
    drawRoundRectCompat(Color(0xFFBAE6FD).copy(alpha = 0.7f),
        cx - fieldR * 0.55f, stageY + fieldR * 0.04f, fieldR * 1.1f, fieldR * 0.06f, 2f)

    // Side arm linking eyepiece to stage
    drawRoundRectCompat(frame, cx + fieldR * 1.05f, cy - fieldR * 0.4f,
        fieldR * 0.22f, fieldR * 1.6f, 10f)

    // Focus knobs on the left side
    val knobX = cx - fieldR * 1.20f
    val coarseY = cy - fieldR * 0.20f
    val fineY = cy + fieldR * 0.20f
    drawCircle(knob, fieldR * 0.16f, Offset(knobX, coarseY))
    drawCircle(pip, fieldR * 0.16f, Offset(knobX, coarseY), style = Stroke(2f))
    drawCircle(knob, fieldR * 0.11f, Offset(knobX, fineY))
    drawCircle(pip, fieldR * 0.11f, Offset(knobX, fineY), style = Stroke(2f))
    // The fine knob's index mark rotates with the focus slider position.
    val a = (focus * 2f - 1f) * PI.toFloat() * 0.95f
    drawLine(Color(0xFFE5E7EB),
        Offset(knobX, fineY),
        Offset(knobX + cos(a) * fieldR * 0.10f, fineY + sin(a) * fieldR * 0.10f),
        strokeWidth = 2f, cap = StrokeCap.Round)
    drawCenteredText(textMeasurer, "focus",
        Offset(knobX, fineY + fieldR * 0.18f), Color(0xFF9CA3AF), 9.sp)

    // Base
    val baseY = stageY + fieldR * 0.20f
    drawRoundRectCompat(frame, cx - fieldR * 1.35f, baseY,
        fieldR * 2.7f, fieldR * 0.20f, 12f)
}

private fun DrawScope.drawCiliate(cx: Float, cy: Float, r: Float, ph: Float, sharp: Float) {
    val body = Color(0xFF65A30D).copy(alpha = 0.30f + 0.45f * sharp)
    val edge = Color(0xFF3F6212).copy(alpha = 0.5f + 0.5f * sharp)
    val wob = sin(ph * 3f) * r * 0.12f
    val rect = Rect(cx - r * 1.5f, cy - r * 0.7f + wob, cx + r * 1.5f, cy + r * 0.7f + wob)
    drawOval(body, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height))
    drawOval(edge, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height),
        style = Stroke(1.4f))
    val n = 16
    for (i in 0 until n) {
        val a = i * (2f * PI.toFloat() / n)
        val ex = cx + cos(a) * r * 1.5f
        val ey = cy + sin(a) * r * 0.7f + wob
        val beat = sin(ph * 6f + i) * r * 0.18f
        drawLine(edge, Offset(ex, ey),
            Offset(ex + cos(a) * (r * 0.3f) + beat * sin(a),
                   ey + sin(a) * (r * 0.3f) + beat * cos(a)),
            strokeWidth = 1f)
    }
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
