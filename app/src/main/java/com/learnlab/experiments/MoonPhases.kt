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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.ExperimentControls
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Moon Phases — Earth-Moon-Sun model (Grade 8, Chapter 1 preview: the Moon).
 * Drag the Moon around its orbit. The Sun always lights the half of the Moon that faces it;
 * what changes is the slice of that lit half we see from Earth. Snaps to the eight named phases.
 */

private data class Phase(val name: String, val day: Int, val desc: String)

private val PHASES = listOf(
    Phase("New Moon", 1,
        "The Moon is between Earth and the Sun. The sunlit half faces completely away from us — we see only the dark side. The Moon is invisible in the night sky."),
    Phase("Waxing Crescent", 4,
        "The Moon has moved slightly along its orbit. A thin sliver of the sunlit side is now visible from Earth. \"Waxing\" means growing — the visible portion will increase over the coming days."),
    Phase("First Quarter", 7,
        "The Moon has completed one quarter of its orbit. We now see exactly half the Moon illuminated — the right half. Despite the name \"quarter,\" we see half the Moon's face."),
    Phase("Waxing Gibbous", 11,
        "More than half the Moon is now visible from Earth. \"Gibbous\" means hump-shaped — we see the Moon as more than a semicircle but not yet a full circle."),
    Phase("Full Moon", 15,
        "The Earth is between the Moon and the Sun. The entire sunlit half of the Moon faces us directly. We see a complete bright circle — a full moon."),
    Phase("Waning Gibbous", 18,
        "The Moon has passed full and is moving away. We still see more than half illuminated, but the lit area is now decreasing. \"Waning\" means shrinking."),
    Phase("Third Quarter", 22,
        "We again see exactly half the Moon illuminated — but this time the left half. The Moon has completed three quarters of its orbit around Earth."),
    Phase("Waning Crescent", 26,
        "Only a thin crescent remains visible, now on the opposite side from the waxing crescent. Within a few days, the Moon will return to the new moon position and the cycle will repeat."),
)

private const val EARTH_FX = 0.6f
private const val EARTH_FY = 0.5f
private const val ORBIT_FR = 0.30f

@Composable
fun MoonPhases(controls: ExperimentControls) {
    val t = LL.tokens
    var angle by remember { mutableStateOf(0f) }          // 0 = New Moon, 180 = Full
    var visited by remember { mutableStateOf(setOf(0)) }
    var showConcepts by remember { mutableStateOf(false) }

    val idx = (((angle / 45f).roundToInt()) % 8 + 8) % 8
    val phase = PHASES[idx]
    val lit = Color(0xFFE8EBF0)
    val sun = Color(0xFFFFB020)

    fun visit(i: Int) { if (i !in visited) visited = visited + i }
    LaunchedEffectVisited(visited.size) { p ->
        controls.onProgress((p / 8f).coerceIn(0.1f, 1f))
        if (p >= 8) controls.onComplete(1f)
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ── Orbital view ───────────────────────────────────
            Column(
                modifier = Modifier.weight(1.4f).fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)).background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)),
            ) {
                LLText("ORBITAL VIEW — top-down", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp))
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    val snapped = ((angle / 45f).roundToInt() % 8 + 8) % 8
                                    angle = snapped * 45f
                                    visit(snapped)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val ex = size.width * EARTH_FX
                                    val ey = size.height * EARTH_FY
                                    val phi = atan2(-(change.position.y - ey), change.position.x - ex)
                                    var a = 180f - (phi * 180f / PI.toFloat())
                                    a = ((a % 360f) + 360f) % 360f
                                    angle = a
                                },
                            )
                        },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width; val h = size.height
                        val earth = Offset(w * EARTH_FX, h * EARTH_FY)
                        val orbitR = minOf(w, h) * ORBIT_FR

                        // Sun rays (faint parallel lines coming from the left)
                        for (i in 0..6) {
                            val y = h * (0.12f + i * 0.13f)
                            drawLine(sun.copy(alpha = 0.18f), Offset(0f, y), Offset(earth.x - orbitR, y),
                                strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                        }
                        // Sun (off to the left)
                        drawCircle(sun.copy(alpha = 0.25f), 46f, Offset(0f, h / 2f))
                        drawCircle(sun, 30f, Offset(0f, h / 2f))

                        // Orbit path (dashed) + 8 snap dots
                        drawCircle(t.lineStrong, orbitR, earth, style = Stroke(1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))))
                        for (i in 0 until 8) {
                            val pos = orbitPos(earth, orbitR, i * 45f)
                            drawCircle(if (i == idx) Color(0xFF2DD4BF) else t.ink600, 4f, pos)
                        }

                        // Earth (with a hint of continents)
                        drawCircle(Color(0xFF2E5FA3), 13.dp.toPx(), earth)
                        drawCircle(Color(0xFF4F9E5E), 4.dp.toPx(), Offset(earth.x - 4f, earth.y - 3f))
                        drawCircle(Color(0xFF4F9E5E), 3.dp.toPx(), Offset(earth.x + 5f, earth.y + 4f))

                        // Viewing-angle line Earth -> Moon
                        val moon = orbitPos(earth, orbitR, angle)
                        drawLine(t.ink500.copy(alpha = 0.6f), earth, moon, strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)))

                        // Moon on the orbit — lit half ALWAYS faces the Sun (left)
                        val mr = 11.dp.toPx()
                        drawCircle(Color(0xFF2B2B33), mr, moon)
                        drawArc(lit, 90f, 180f, useCenter = true,
                            topLeft = Offset(moon.x - mr, moon.y - mr), size = Size(2 * mr, 2 * mr))
                        drawCircle(t.ink400, mr, moon, style = Stroke(1f))
                    }
                    LLText("☀ Sun", color = sun, size = 12.sp, weight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
                    LLText("Drag the Moon around its orbit", color = t.ink500, size = 11.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
                }
            }

            // ── Phase panel ────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)).background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LLText("WHAT YOU SEE FROM EARTH", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(12.dp))
                Canvas(Modifier.size(150.dp)) {
                    val r = size.minDimension / 2f - 3.dp.toPx()
                    drawMoon(center, r, angle / 360f, lit = lit, dark = Color(0xFF2B2B33))
                    drawCircle(t.ink400, r, center, style = Stroke(1f))
                }
                Spacer(Modifier.height(14.dp))
                LLText(phase.name, color = t.ink50, size = 22.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LLText("~Day ${phase.day} of the 29-day cycle", color = Color(0xFF2DD4BF), size = 12.sp,
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
                        .clickable { angle = i * 45f; visit(i) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    val active = i == idx
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(999.dp))
                            .background(if (active) Color(0xFF2DD4BF).copy(alpha = 0.18f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.size(30.dp)) {
                            val r = size.minDimension / 2f - 1.dp.toPx()
                            drawMoon(center, r, i / 8f, lit = lit, dark = Color(0xFF2B2B33))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    LLText(ph.name.split(" ").last(), color = if (active) Color(0xFF2DD4BF) else t.ink500,
                        size = 9.sp, weight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Discovery tasks + key concepts ──────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TaskCard("1", "Move to New Moon, then Full Moon. Where is Earth relative to the Sun and Moon each time?", Modifier.weight(1f))
            TaskCard("2", "Drag from New to Full Moon — the lit area grows on the right first. Why always the right? (The Sun stays put.)", Modifier.weight(1f))
            TaskCard("3", "Find the positions where exactly half is lit. How many are there in one orbit? What are they called?", Modifier.weight(1f))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp))
                    .background(if (showConcepts) Color(0xFF2DD4BF) else t.surface2)
                    .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                    .clickable { showConcepts = !showConcepts }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                LLText(if (showConcepts) "Hide concepts" else "Key concepts ▾",
                    color = if (showConcepts) Color(0xFF0A0A0F) else Color(0xFF2DD4BF),
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

private fun orbitPos(earth: Offset, r: Float, angleDeg: Float): Offset {
    val phi = (180f - angleDeg) * PI.toFloat() / 180f
    return Offset(earth.x + r * cos(phi), earth.y - r * sin(phi))
}

/** Geometrically-accurate phase: [phase] 0 = new, 0.5 = full; waxing lights the right half. */
private fun DrawScope.drawMoon(c: Offset, r: Float, phase: Float, lit: Color, dark: Color) {
    drawCircle(dark, r, c)
    val startAngle = if (phase < 0.5f) -90f else 90f
    drawArc(lit, startAngle, 180f, useCenter = true,
        topLeft = Offset(c.x - r, c.y - r), size = Size(2 * r, 2 * r))
    val x = cos(phase * 2f * PI.toFloat())
    val ew = r * kotlin.math.abs(x)
    drawOval(if (x < 0f) lit else dark, topLeft = Offset(c.x - ew, c.y - r), size = Size(2 * ew, 2 * r))
    // Subtle craters — darker dabs that read on the lit area and vanish on the dark side.
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
                .background(Color(0xFF2DD4BF).copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) { LLText(n, color = Color(0xFF2DD4BF), size = 12.sp, weight = FontWeight.Bold) }
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

@Composable
private fun LaunchedEffectVisited(count: Int, block: (Int) -> Unit) {
    androidx.compose.runtime.LaunchedEffect(count) { block(count) }
}
