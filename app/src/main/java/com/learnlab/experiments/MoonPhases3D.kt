package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.ExperimentControls
import io.github.sceneview.Scene
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Moon Phases — "Phase 2": the same Earth–Moon–Sun idea as [MoonPhases], but the orbital
 * view is a real 3D scene (Filament/SceneView) — a glowing Sun and a cratered Moon that
 * orbits Earth with smooth motion. A fixed sunlight direction lights exactly the half of the
 * Moon facing the Sun, so the phase emerges physically. The right panel still shows the
 * idealised "what you see from Earth" disc, reusing the 2D phase renderer.
 */

private data class Phase3(val name: String, val day: Int, val desc: String)

private val PHASES = listOf(
    Phase3("New Moon", 1,
        "The Moon is between Earth and the Sun. The sunlit half faces completely away from us — we see only the dark side. The Moon is invisible in the night sky."),
    Phase3("Waxing Crescent", 4,
        "The Moon has moved slightly along its orbit. A thin sliver of the sunlit side is now visible from Earth. \"Waxing\" means growing — the visible portion will increase over the coming days."),
    Phase3("First Quarter", 7,
        "The Moon has completed one quarter of its orbit. We now see exactly half the Moon illuminated — the right half. Despite the name \"quarter,\" we see half the Moon's face."),
    Phase3("Waxing Gibbous", 11,
        "More than half the Moon is now visible from Earth. \"Gibbous\" means hump-shaped — we see the Moon as more than a semicircle but not yet a full circle."),
    Phase3("Full Moon", 15,
        "The Earth is between the Moon and the Sun. The entire sunlit half of the Moon faces us directly. We see a complete bright circle — a full moon."),
    Phase3("Waning Gibbous", 18,
        "The Moon has passed full and is moving away. We still see more than half illuminated, but the lit area is now decreasing. \"Waning\" means shrinking."),
    Phase3("Third Quarter", 22,
        "We again see exactly half the Moon illuminated — but this time the left half. The Moon has completed three quarters of its orbit around Earth."),
    Phase3("Waning Crescent", 26,
        "Only a thin crescent remains visible, now on the opposite side from the waxing crescent. Within a few days, the Moon will return to the new moon position and the cycle will repeat."),
)

private const val ORBIT_R = 1.3f            // Moon orbital radius in scene units
private const val ORBIT_SPEED_DEG = 22f     // smooth auto-orbit speed (deg/sec)
private const val SPIN_SPEED_DEG = 34f      // Moon's own-axis rotation (deg/sec) — visibly revolving
private const val AXIS_TILT_DEG = 12f       // slight axial tilt for a natural look
private val ACCENT = Color(0xFF2DD4BF)

/** Moon's position on its orbit: angle 0 = New (sun side, -X), 180 = Full (+X). */
private fun moonOrbitPosition(angleDeg: Float): Position {
    val a = angleDeg * PI.toFloat() / 180f
    return Position(x = -ORBIT_R * cos(a), y = 0f, z = ORBIT_R * sin(a))
}

@Composable
fun MoonPhases3D(controls: ExperimentControls) {
    val t = LL.tokens
    val lit = Color(0xFFE8EBF0)
    val dark = Color(0xFF2B2B33)

    val angle = remember { mutableFloatStateOf(0f) }   // live-read in the frame loop
    val playing = remember { mutableStateOf(true) }
    val dragging = remember { mutableStateOf(false) }
    val lastFrame = remember { LongArray(1) }
    val spin = remember { FloatArray(1) }   // Moon's accumulated own-axis rotation
    var visited by remember { mutableStateOf(setOf(0)) }
    var showConcepts by remember { mutableStateOf(false) }

    val a = angle.floatValue
    val idx = (((a / 45f).roundToInt()) % 8 + 8) % 8
    val phase = PHASES[idx]

    fun visit(i: Int) { if (i !in visited) visited = visited + i }
    LaunchedEffect(idx) { visit(idx) }
    LaunchedEffect(visited.size) {
        controls.onProgress((visited.size / 8f).coerceIn(0.1f, 1f))
        if (visited.size >= 8) controls.onComplete(1f)
    }

    // ── 3D scene graph (built once) ───────────────────────────────
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)

    val moonNode = remember {
        ModelNode(modelInstance = modelLoader.createModelInstance("models/moon.glb"), scaleToUnits = 0.85f)
    }
    val sunNode = remember {
        ModelNode(modelInstance = modelLoader.createModelInstance("models/sun.glb"), scaleToUnits = 1.4f).apply {
            position = Position(x = -3.8f, y = 0f, z = 0f)
        }
    }
    // Earth is drawn as a 2D overlay at the orbit centre (no Earth model supplied yet).
    // Moderate oblique angle: shows the Moon's day/night terminator as a clear curve on the
    // sphere while keeping Earth centred and the orbit readable.
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 3.0f, z = 3.8f)
        lookAt(Position(0f, 0f, 0f))
    }
    // Fixed "sunlight" travelling +X (the Sun sits on -X): the Moon's left/-X half is always lit.
    // A directional light's direction is NOT driven by the node transform — set it explicitly.
    val mainLight = rememberMainLightNode(engine) {
        lightDirection = Direction(x = 1f, y = -0.08f, z = 0f)
        intensity = 120_000f
    }
    val sceneNodes = rememberNodes {
        add(moonNode); add(sunNode)
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ── 3D orbital view ───────────────────────────────
            Column(
                modifier = Modifier.weight(1.4f).fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)).background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)),
            ) {
                LLText("ORBITAL VIEW — 3D", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp))
                Box(modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(12.dp))) {
                    Scene(
                        modifier = Modifier.fillMaxSize(),
                        engine = engine,
                        modelLoader = modelLoader,
                        materialLoader = materialLoader,
                        cameraNode = cameraNode,
                        mainLightNode = mainLight,
                        childNodes = sceneNodes,
                        cameraManipulator = null,   // we drive the Moon, not the camera
                        isOpaque = true,
                        onFrame = { frameNanos ->
                            val prev = lastFrame[0]
                            if (prev != 0L && playing.value && !dragging.value) {
                                val dt = ((frameNanos - prev) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.05f)
                                var next = angle.floatValue + dt * ORBIT_SPEED_DEG
                                next = ((next % 360f) + 360f) % 360f
                                angle.floatValue = next
                                spin[0] = (spin[0] + dt * SPIN_SPEED_DEG) % 360f
                            }
                            lastFrame[0] = frameNanos
                            moonNode.position = moonOrbitPosition(angle.floatValue)
                            // The Moon spins on its own (slightly tilted) axis as it orbits.
                            moonNode.rotation = Rotation(x = AXIS_TILT_DEG, y = spin[0], z = 0f)
                        },
                        // Soft, realistic shadow: a faint earthshine fill (not pure black) keeps the
                        // shadowed half's craters dimly visible as the Moon turns through the terminator.
                        onViewUpdated = { indirectLight?.intensity = 12_000f },
                    )
                    // Earth — a 2D placeholder at the orbit centre (no Earth model yet).
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(54.dp)) { drawEarth2D(center, size.minDimension / 2f - 1.dp.toPx()) }
                    }
                    // Drag-to-scrub overlay (snaps to the nearest of 8 phases on release).
                    Box(
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragging.value = true },
                                onDragEnd = {
                                    dragging.value = false
                                    val snapped = ((angle.floatValue / 45f).roundToInt() % 8 + 8) % 8
                                    angle.floatValue = snapped * 45f
                                },
                                onDrag = { change, drag ->
                                    change.consume()
                                    var next = angle.floatValue + drag.x * 0.4f
                                    next = ((next % 360f) + 360f) % 360f
                                    angle.floatValue = next
                                },
                            )
                        },
                    )
                    // Play / pause (teacher can stop on a phase to discuss).
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (playing.value) ACCENT.copy(alpha = 0.9f) else t.surface2)
                            .border(1.dp, ACCENT.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                            .clickable { playing.value = !playing.value }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        LLText(if (playing.value) "⏸ Pause" else "▶ Play",
                            color = if (playing.value) Color(0xFF0A0A0F) else ACCENT,
                            size = 12.sp, weight = FontWeight.SemiBold)
                    }
                    LLText("Drag to scrub · ☀ Sun lights the half facing it",
                        color = t.ink500, size = 11.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
                }
            }

            // ── Phase panel (what you see from Earth) ──────────
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)).background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LLText("WHAT YOU SEE FROM EARTH", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(12.dp))
                // The same Moon in 3D, lit to show the current phase as seen from Earth.
                EarthViewMoon3D(angle, Modifier.size(150.dp))
                Spacer(Modifier.height(14.dp))
                LLText(phase.name, color = t.ink50, size = 22.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LLText("~Day ${phase.day} of the 29-day cycle", color = ACCENT, size = 12.sp,
                    weight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                LLText(phase.desc, color = t.ink200, size = 13.sp, lineHeight = 19.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Phase strip ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(t.surface).border(1.dp, t.line, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PHASES.forEachIndexed { i, ph ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .clickable { playing.value = false; angle.floatValue = i * 45f; visit(i) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    val active = i == idx
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(999.dp))
                            .background(if (active) ACCENT.copy(alpha = 0.18f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.size(30.dp)) {
                            val r = size.minDimension / 2f - 1.dp.toPx()
                            drawMoon(center, r, i / 8f, lit = lit, dark = dark)
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    LLText(ph.name.split(" ").last(), color = if (active) ACCENT else t.ink500,
                        size = 9.sp, weight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Discovery tasks + key concepts ──────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TaskCard("1", "Press Pause on New Moon, then Full Moon. Where is Earth relative to the Sun and Moon each time?", Modifier.weight(1f))
            TaskCard("2", "Watch the lit edge grow on the right first as the Moon waxes. Why always the right? (The Sun stays put.)", Modifier.weight(1f))
            TaskCard("3", "Find the two positions where exactly half is lit. What are they called?", Modifier.weight(1f))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp))
                    .background(if (showConcepts) ACCENT else t.surface2)
                    .border(1.dp, ACCENT.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                    .clickable { showConcepts = !showConcepts }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                LLText(if (showConcepts) "Hide concepts" else "Key concepts ▾",
                    color = if (showConcepts) Color(0xFF0A0A0F) else ACCENT,
                    size = 12.sp, weight = FontWeight.SemiBold)
            }
        }

        if (showConcepts) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(t.surface).border(1.dp, t.line, RoundedCornerShape(14.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ConceptRow("Why the Moon does not change shape",
                    "The Moon is always a sphere. Half of it is always lit by the Sun and half is in shadow. What changes is your viewing angle from Earth — you see different proportions of the lit half as the Moon orbits.")
                ConceptRow("Why the cycle takes 29 days",
                    "The Moon orbits Earth in ~27.3 days, but Earth is also moving around the Sun — so the Moon needs ~2 extra days to return to the same Sun-Moon-Earth alignment. That 29.5-day cycle is the synodic month.")
                ConceptRow("How this gave us calendars",
                    "Early humans tracked the Moon's predictable 29-day rhythm long before clocks or writing — the basis of the first calendars. The word \"month\" comes from \"Moon\"; many calendars, including the Indian one, are lunar.")
            }
        }
    }
}

/**
 * The Moon as seen from Earth, in 3D: a single sphere facing the camera, lit by a directional
 * light that rotates with the orbital [angle] so the visible lit fraction matches the real phase.
 * Ambient is left on (a faint "earthshine") so the dark portion stays visible — same light/dark
 * information the 2D disc used to show.
 */
@Composable
private fun EarthViewMoon3D(angle: androidx.compose.runtime.FloatState, modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val moon = remember {
        ModelNode(modelInstance = modelLoader.createModelInstance("models/moon.glb"), scaleToUnits = 1.7f)
    }
    val camera = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0f, z = 2.6f)
        lookAt(Position(0f, 0f, 0f))
    }
    val light = rememberMainLightNode(engine) {
        intensity = 130_000f
        lightDirection = Direction(x = 0f, y = 0f, z = -1f)
    }
    val nodes = rememberNodes { add(moon) }
    Scene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = camera,
        mainLightNode = light,
        childNodes = nodes,
        cameraManipulator = null,
        isOpaque = false,
        onFrame = {
            val a = angle.floatValue * PI.toFloat() / 180f
            // angle 0 (new) -> lit far side (dark to us); 180 (full) -> lit near side; 90 -> right half.
            light.lightDirection = Direction(x = sin(a), y = 0f, z = cos(a))
        },
        // Dim (not kill) the ambient: a faint earthshine keeps the dark portion visible while
        // the directional Sun still carves a clear terminator, so the phase reads.
        onViewUpdated = { indirectLight?.intensity = 12_000f },
    )
}

/** Simple 2D Earth placeholder: ocean + a few landmasses, night side on the right (Sun is left). */
private fun DrawScope.drawEarth2D(c: Offset, r: Float) {
    drawCircle(Color(0xFF2E5FA3), r, c)
    val land = Color(0xFF4F9E5E)
    drawCircle(land, r * 0.32f, Offset(c.x - r * 0.30f, c.y - r * 0.24f))
    drawCircle(land, r * 0.22f, Offset(c.x + r * 0.30f, c.y + r * 0.08f))
    drawCircle(land, r * 0.16f, Offset(c.x - r * 0.02f, c.y + r * 0.40f))
    drawArc(Color(0x55000000), -90f, 180f, useCenter = true,
        topLeft = Offset(c.x - r, c.y - r), size = Size(2 * r, 2 * r))
    drawCircle(Color(0x668FB8FF), r, c, style = Stroke(2f))
}

/** Geometrically-accurate phase: [phase] 0 = new, 0.5 = full; waxing lights the right half. */
private fun DrawScope.drawMoon(c: Offset, r: Float, phase: Float, lit: Color, dark: Color) {
    drawCircle(dark, r, c)
    val startAngle = if (phase < 0.5f) -90f else 90f
    drawArc(lit, startAngle, 180f, useCenter = true,
        topLeft = Offset(c.x - r, c.y - r), size = Size(2 * r, 2 * r))
    val x = cos(phase * 2f * PI.toFloat())
    val ew = r * abs(x)
    drawOval(if (x < 0f) lit else dark, topLeft = Offset(c.x - ew, c.y - r), size = Size(2 * ew, 2 * r))
    val crater = Color(0xFF9AA3AE).copy(alpha = 0.45f)
    drawCircle(crater, r * 0.16f, Offset(c.x + r * 0.30f, c.y - r * 0.25f))
    drawCircle(crater, r * 0.11f, Offset(c.x + r * 0.05f, c.y + r * 0.32f))
    drawCircle(crater, r * 0.08f, Offset(c.x - r * 0.30f, c.y + r * 0.10f))
}

@Composable
private fun TaskCard(n: String, text: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Row(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp)).padding(12.dp),
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(999.dp))
                .background(ACCENT.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) { LLText(n, color = ACCENT, size = 12.sp, weight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp))
        LLText(text, color = t.ink200, size = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun ConceptRow(title: String, body: String) {
    val t = LL.tokens
    Column {
        LLText(title, color = t.ink50, size = 13.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        LLText(body, color = t.ink200, size = 12.sp, lineHeight = 17.sp)
    }
}
