package com.learnlab.experiments

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs
import kotlin.math.sin
import kotlinx.coroutines.launch

/**
 * Root System Explorer (NCERT Activity 2.6).
 * A digital stand-in for digging wild herbs out of an open patch: drag each plant
 * up to lift it clear of the soil, tap to wash the clinging mud off the roots, then
 * — only once both are clean — name what you see (taproot vs fibrous) and replant.
 * Terms arrive after the observation, mirroring the book's order.
 */

private enum class RootKind { Tap, Fibrous }

private class RootPlantSpec(
    val name: String,
    val kind: RootKind,
    val bloom: Color,        // flower / tuft accent
    val stem: Color,
)

/** First pair the class pulls; second pair (marigold + lemongrass) confirms the rule
 *  and seeds Activity 2.7, which reuses those exact plants. */
private val pairOne = listOf(
    RootPlantSpec("Mustard", RootKind.Tap, Color(0xFFFBE36B), Color(0xFF3F8F4E)),
    RootPlantSpec("Common grass", RootKind.Fibrous, Color(0xFF7CB342), Color(0xFF4E9A3A)),
)
private val pairTwo = listOf(
    RootPlantSpec("Marigold", RootKind.Tap, Color(0xFFFF9E2C), Color(0xFF3F8F4E)),
    RootPlantSpec("Lemongrass", RootKind.Fibrous, Color(0xFF8BC34A), Color(0xFF4E9A3A)),
)

private val tapColor = Color(0xFFC2703D)
private val fibrousColor = Color(0xFF7A4A2B)
private val soilColor = Color(0xFF6B4A2E)
private val soilTop = Color(0xFF7C5736)

@Composable
fun RootSystemExplorer(controls: ExperimentControls) {
    val t = LL.tokens
    val scope = rememberCoroutineScope()

    var round by remember { mutableStateOf(0) }            // 0 = first pair, 1 = confirm pair
    var finished by remember { mutableStateOf(false) }
    var showSecondPair by remember { mutableStateOf(false) }

    val plants = if (round == 0) pairOne else pairTwo

    // Per-plant lift (0 = in soil, 1 = fully out) and wash (0 = muddy, 1 = clean).
    val liftL = remember(round) { Animatable(0f) }
    val liftR = remember(round) { Animatable(0f) }
    val washL = remember(round) { Animatable(0f) }
    val washR = remember(round) { Animatable(0f) }
    val replant = remember(round) { Animatable(0f) }       // 0 = standing out, 1 = settled back

    var washedL by remember(round) { mutableStateOf(false) }
    var washedR by remember(round) { mutableStateOf(false) }
    var replanted by remember(round) { mutableStateOf(false) }

    val bothLifted = liftL.value > 0.9f && liftR.value > 0.9f
    val bothWashed = washedL && washedR
    val named = bothLifted && bothWashed

    // Phase index drives the shell's procedure banner.
    val phase = when {
        replanted -> 3
        named -> 2
        bothLifted -> 1
        else -> 0
    }
    LaunchedEffect(phase, round) { controls.onStep(phase) }

    // Progress: lifting the first pair fills the first half, naming + replanting the rest.
    LaunchedEffect(liftL.value, liftR.value, named, replanted, round) {
        if (finished) return@LaunchedEffect
        val lift = (liftL.value + liftR.value) / 2f
        val base = when {
            round == 0 && !named -> lift * 0.30f
            round == 0 && named && !replanted -> 0.35f
            round == 0 && replanted -> 0.45f
            round == 1 && !named -> 0.45f + lift * 0.25f
            round == 1 && named && !replanted -> 0.80f
            else -> 1f
        }
        controls.onProgress(base.coerceIn(0f, 1f))
    }

    LaunchedEffect(finished) { if (finished) controls.onComplete(1f) }

    if (finished) {
        DoneCard(
            sawSecondPair = showSecondPair,
            onReplay = {
                round = 0; finished = false; showSecondPair = false
            },
        )
        return
    }

    fun washLeft() { washedL = true; scope.launch { washL.animateTo(1f, tween(700)) } }
    fun washRight() { washedR = true; scope.launch { washR.animateTo(1f, tween(700)) } }
    fun doReplant() {
        replanted = true
        scope.launch { replant.animateTo(1f, tween(1100)) }
    }

    Row(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // ── Soil stage ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    LLText(
                        if (round == 0) "OPEN PATCH" else "TWO MORE TO CHECK",
                        color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                    )
                    LLText(
                        when {
                            replanted -> "Settle them back so they keep growing."
                            named -> "Roots clean — now you can name what you see."
                            bothLifted -> "Tap each plant to wash the mud off its roots."
                            else -> "Drag each plant up to ease it out of the soil."
                        },
                        color = t.ink400, size = 12.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot("Taproot", tapColor)
                    LegendDot("Fibrous", fibrousColor)
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PlantBed(
                    spec = plants[0],
                    lift = liftL,
                    wash = washL.value,
                    washed = washedL,
                    replant = replant.value,
                    named = named,
                    onWash = ::washLeft,
                    scope = scope,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                PlantBed(
                    spec = plants[1],
                    lift = liftR,
                    wash = washR.value,
                    washed = washedR,
                    replant = replant.value,
                    named = named,
                    onWash = ::washRight,
                    scope = scope,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }

        // ── Side panel ───────────────────────────────────────────
        Column(
            modifier = Modifier.width(290.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepCard(phase = phase, round = round)

            if (named) {
                NameCard(
                    spec = plants[0],
                    headline = if (plants[0].kind == RootKind.Tap)
                        "One thick main root with small side roots."
                    else "A bunch of similar thin roots from the stem base.",
                    term = if (plants[0].kind == RootKind.Tap) "Taproot" else "Fibrous roots",
                    color = if (plants[0].kind == RootKind.Tap) tapColor else fibrousColor,
                )
                NameCard(
                    spec = plants[1],
                    headline = if (plants[1].kind == RootKind.Tap)
                        "One thick main root with small side roots."
                    else "A bunch of similar thin roots from the stem base.",
                    term = if (plants[1].kind == RootKind.Tap) "Taproot" else "Fibrous roots",
                    color = if (plants[1].kind == RootKind.Tap) tapColor else fibrousColor,
                )
                CompareStrip()
            }

            Spacer(Modifier.weight(1f))

            if (named && !replanted) {
                LLText(
                    "Replant them so they keep growing.",
                    color = t.ink400, size = 12.sp, lineHeight = 16.sp,
                )
                PrimaryButton(
                    label = "Replant both",
                    onClick = { doReplant() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (replanted) {
                if (round == 0 && !showSecondPair) {
                    SecondaryButton(
                        label = "Try two more",
                        onClick = { showSecondPair = true; round = 1 },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                PrimaryButton(
                    label = "Finish",
                    onClick = { finished = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ─────────────────────────── Plant bed ─────────────────────────── */

@Composable
private fun PlantBed(
    spec: RootPlantSpec,
    lift: Animatable<Float, *>,
    wash: Float,
    washed: Boolean,
    replant: Float,
    named: Boolean,
    onWash: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val fullyOut = lift.value > 0.9f
    // Effective lift: a replant tween presses the plant back down into the soil.
    val shown = lift.value * (1f - replant)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(Radius.md)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(spec.name) {
                    detectVerticalDragGestures { _, dragAmount ->
                        // Drag up (negative) lifts the plant out; one full bed ≈ full lift.
                        val delta = -dragAmount / size.height.toFloat() * 1.6f
                        scope.launch {
                            lift.snapTo((lift.value + delta).coerceIn(0f, 1f))
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPlantScene(spec, lift = shown, wash = wash)
            }
            // Lift handle hint while still in the soil
            if (shown < 0.9f && replant == 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(t.surface3.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    LLText("↑ drag up", color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                replant > 0f -> LLText(
                    spec.name, color = t.ink200, size = 16.sp, weight = FontWeight.Bold,
                )
                named -> LLText(
                    spec.name, color = t.ink50, size = 17.sp, weight = FontWeight.Bold,
                )
                fullyOut && !washed -> WashButton(spec.bloom, onWash)
                fullyOut && washed -> LLText(
                    "Roots clean", color = t.accent700, size = 14.sp, weight = FontWeight.SemiBold,
                )
                else -> LLText(
                    spec.name, color = t.ink400, size = 15.sp, weight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun WashButton(accent: Color, onWash: () -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .bounceClickable(onClick = onWash)
            .clip(RoundedCornerShape(Radius.pill))
            .background(Color(0xFF38BDF8).copy(alpha = 0.16f))
            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(Radius.pill))
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText("Wash roots", color = Color(0xFF0284C7), size = 15.sp, weight = FontWeight.Bold)
        // accent referenced so it stays meaningful per-plant if styled later
        @Suppress("UNUSED_EXPRESSION") accent
        Spacer(Modifier.width(8.dp))
        LLText("💧", color = t.ink200, size = 16.sp)
    }
}

/* ─────────────────────────── Drawing ──────────────────────────── */

private fun DrawScope.drawPlantScene(spec: RootPlantSpec, lift: Float, wash: Float) {
    val w = size.width
    val h = size.height
    val soilLine = h * 0.56f          // where the soil surface sits
    val cx = w / 2f

    // How far the whole plant has risen, in px. Full lift clears the soil band.
    val rise = lift * (h * 0.40f)
    val baseY = soilLine - rise        // current stem base (root collar)

    // ── Roots (drawn first so the soil layer can mask them) ──
    val rootTop = baseY
    val rootDepth = h * 0.36f
    if (spec.kind == RootKind.Tap) drawTaproot(cx, rootTop, rootDepth, wash)
    else drawFibrous(cx, rootTop, rootDepth, wash)

    // ── Soil band (opaque): hides whatever is still below the surface ──
    drawRect(soilTop, topLeft = Offset(0f, soilLine - 4f), size = Size(w, 4f))
    drawRect(soilColor, topLeft = Offset(0f, soilLine), size = Size(w, h - soilLine))
    // soil texture flecks
    val rnd = (cx.toInt() % 7)
    for (i in 0 until 26) {
        val fx = ((i * 53 + rnd * 11) % w.toInt()).toFloat()
        val fy = soilLine + ((i * 37) % (h - soilLine).toInt()).toFloat()
        drawCircle(Color(0xFF553A22), 1.6f, Offset(fx, fy))
    }

    // ── Crumbs falling from the just-lifted root ball ──
    if (lift > 0.05f && lift < 0.98f && wash < 0.5f) {
        val n = 7
        for (i in 0 until n) {
            val phase = (lift * 6f + i) % 1f
            val fx = cx + ((i - 3) * (w * 0.04f))
            val fy = soilLine - rise * 0.4f + phase * (rise + 20f)
            if (fy < soilLine) {
                drawCircle(Color(0xFF6B4A2E).copy(alpha = 0.7f * (1f - phase)), 2.2f, Offset(fx, fy))
            }
        }
    }

    // ── Stem + foliage above the collar ──
    val topY = baseY - h * 0.30f
    drawLine(spec.stem, Offset(cx, baseY), Offset(cx, topY),
        strokeWidth = 6f, cap = StrokeCap.Round)

    if (spec.kind == RootKind.Tap) {
        // broad leaves + a flower cluster (mustard / marigold)
        for (s in listOf(-1f, 1f)) {
            for (j in 0..2) {
                val ly = baseY - h * (0.08f + j * 0.07f)
                val leaf = Path().apply {
                    moveTo(cx, ly)
                    quadraticTo(cx + s * w * 0.16f, ly - h * 0.04f,
                        cx + s * w * 0.20f, ly + h * 0.02f)
                    quadraticTo(cx + s * w * 0.10f, ly + h * 0.02f, cx, ly)
                    close()
                }
                drawPath(leaf, Color(0xFF3F8F4E).copy(alpha = 0.9f))
            }
        }
        // bloom cluster
        for (k in 0 until 6) {
            val a = k * 1.04f
            drawCircle(spec.bloom, 7f,
                Offset(cx + sin(a) * 12f, topY + (k % 2) * 8f - 4f))
        }
        drawCircle(spec.bloom.copy(alpha = 0.9f), 9f, Offset(cx, topY))
    } else {
        // a spray of narrow blades fanning out (grass / lemongrass)
        for (i in -3..3) {
            val tip = Offset(cx + i * w * 0.05f, topY - abs(i) * h * 0.015f)
            val blade = Path().apply {
                moveTo(cx, baseY)
                quadraticTo(cx + i * w * 0.045f, (baseY + tip.y) / 2f, tip.x, tip.y)
            }
            drawPath(blade, spec.stem.copy(alpha = 0.95f), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        }
        if (spec.bloom != Color(0xFF7CB342)) {
            // lemongrass tuft tinge
            drawCircle(spec.bloom.copy(alpha = 0.5f), 6f, Offset(cx, topY - 4f))
        }
    }
}

private fun DrawScope.drawTaproot(cx: Float, top: Float, depth: Float, wash: Float) {
    val tip = top + depth
    // one thick tapering main root
    val main = Path().apply {
        moveTo(cx - 5f, top)
        cubicTo(cx - 4f, top + depth * 0.4f, cx - 2f, top + depth * 0.75f, cx, tip)
        cubicTo(cx + 2f, top + depth * 0.75f, cx + 4f, top + depth * 0.4f, cx + 5f, top)
        close()
    }
    drawPath(main, tapColor)
    // small side rootlets branching off
    for (i in 1..5) {
        val tNorm = i / 6f
        val y = top + depth * tNorm
        val side = (5f * (1f - tNorm)).coerceAtLeast(1.5f)
        val len = (depth * 0.18f) * (1f - tNorm * 0.4f)
        for (s in listOf(-1f, 1f)) {
            val branch = Path().apply {
                moveTo(cx + s * side, y)
                quadraticTo(cx + s * (side + len * 0.6f), y + len * 0.3f,
                    cx + s * (side + len), y + len * 0.8f)
            }
            drawPath(branch, tapColor.copy(alpha = 0.9f), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
        }
    }
    drawMud(cx, top, depth, wash)
}

private fun DrawScope.drawFibrous(cx: Float, top: Float, depth: Float, wash: Float) {
    // a bunch of similar-sized thin roots from the same base
    val n = 11
    for (i in 0 until n) {
        val spreadT = (i - (n - 1) / 2f) / ((n - 1) / 2f)   // -1..1
        val endX = cx + spreadT * (depth * 0.42f)
        val endY = top + depth * (0.78f + 0.18f * (1f - abs(spreadT)))
        val root = Path().apply {
            moveTo(cx, top)
            cubicTo(
                cx + spreadT * depth * 0.10f, top + depth * 0.35f,
                endX * 0.7f + cx * 0.3f, top + depth * 0.6f,
                endX, endY,
            )
        }
        drawPath(root, fibrousColor.copy(alpha = 0.95f),
            style = Stroke(width = 2.6f, cap = StrokeCap.Round))
    }
    drawMud(cx, top, depth, wash)
}

/** Soil-coloured haze clinging to the roots; fades to nothing as [wash] → 1. */
private fun DrawScope.drawMud(cx: Float, top: Float, depth: Float, wash: Float) {
    val alpha = (1f - wash) * 0.55f
    if (alpha <= 0.01f) return
    for (i in 0 until 14) {
        val tNorm = i / 14f
        val r = (depth * 0.20f) * (1f - tNorm * 0.5f)
        val y = top + depth * (0.1f + tNorm * 0.7f)
        val jx = cx + sin(i * 1.7f) * depth * 0.12f
        drawCircle(Color(0xFF6B4A2E).copy(alpha = alpha * (0.6f + 0.4f * (1f - tNorm))), r, Offset(jx, y))
    }
}

/* ─────────────────────────── Side cards ────────────────────────── */

@Composable
private fun StepCard(phase: Int, round: Int) {
    val t = LL.tokens
    val (title, body) = when (phase) {
        0 -> "LIFT THEM OUT" to "Wet soil loosens the grip. Drag each plant up until its roots clear the ground."
        1 -> "WASH & LOOK" to "Rinse the mud off so the shape of the roots shows clearly. Then compare the two."
        2 -> "NAME WHAT YOU SEE" to "One plant has a single thick root; the other has a tuft of thin ones. Now they get their names."
        else -> "REPLANT" to "Tuck them back into the soil so the herbs keep on growing."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("STEP ${phase + 1} OF 4", color = t.ink500, size = 11.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            if (round == 1) {
                Spacer(Modifier.width(8.dp))
                LLText("· confirming the rule", color = t.ink500, size = 11.sp)
            }
        }
        LLText(title, color = t.ink50, size = 18.sp, weight = FontWeight.Bold)
        LLText(body, color = t.ink200, size = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun NameCard(spec: RootPlantSpec, headline: String, term: String, color: Color) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(Radius.md))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            LLText(spec.name, color = t.ink50, size = 14.sp, weight = FontWeight.Bold)
        }
        LLText(headline, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
        LLText(term, color = color, size = 15.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun CompareStrip() {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(Radius.md))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            LLText("Taproot", color = tapColor, size = 12.sp, weight = FontWeight.Bold)
            LLText("One main root, thin side roots.", color = t.ink400, size = 11.sp, lineHeight = 15.sp)
        }
        Box(Modifier.width(1.dp).height(34.dp).background(t.line))
        Column(Modifier.weight(1f)) {
            LLText("Fibrous", color = fibrousColor, size = 12.sp, weight = FontWeight.Bold)
            LLText("Many thin roots, no main one.", color = t.ink400, size = 11.sp, lineHeight = 15.sp)
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

@Composable
private fun DoneCard(sawSecondPair: Boolean, onReplay: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LLText("ROOT EXPLORER — DONE", color = t.ink500, size = 11.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            LLText(
                if (sawSecondPair) "You checked four plants — the rule held both times."
                else "You dug, washed, named, and replanted both plants.",
                color = t.ink50, size = 22.sp, weight = FontWeight.Bold,
                lineHeight = 28.sp, align = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LegendDot("One main root → taproot", tapColor)
                LegendDot("A tuft of thin roots → fibrous", fibrousColor)
            }
            Spacer(Modifier.height(22.dp))
            SecondaryButton(label = "Run again", onClick = onReplay)
        }
    }
}
