package com.learnlab.experiments

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls

/**
 * Seed Dissection (NCERT Activity 2.8 — "Let us compare").
 * Phase 0 Soak — both seeds drink water and swell over three days.
 * Phase 1 Split chickpea — peel the coat, pry it open: two cotyledons.
 * Phase 2 Try maize — the same pull won't halve it: one thin cotyledon.
 * Phase 3 Name & connect — cotyledon → dicot vs monocot, then the chapter's
 * payoff linking cotyledon count to venation and root type.
 */

// Dicot family (chickpea) uses the chapter's violet; monocot (maize) the sky.
private val dicotColor = Color(0xFFA855F7)
private val monocotColor = Color(0xFF0EA5E9)
private val seedFill = Color(0xFFF6D88A)
private val seedFillDeep = Color(0xFFEAB24E)
private val embryoColor = Color(0xFF6FB46B)

private enum class SeedPhase { Soak, SplitChickpea, TryMaize, Connect }

@Composable
fun SeedDissection(controls: ExperimentControls) {
    val t = LL.tokens
    var phase by remember { mutableStateOf(SeedPhase.Soak) }

    // onStep 0 = soak, 1 = split chickpea, 2 = try maize, 3 = name & connect.
    LaunchedEffect(phase) {
        controls.onStep(
            when (phase) {
                SeedPhase.Soak -> 0
                SeedPhase.SplitChickpea -> 1
                SeedPhase.TryMaize -> 2
                SeedPhase.Connect -> 3
            },
        )
        controls.onProgress(
            when (phase) {
                SeedPhase.Soak -> 0.15f
                SeedPhase.SplitChickpea -> 0.45f
                SeedPhase.TryMaize -> 0.7f
                SeedPhase.Connect -> 0.9f
            },
        )
        if (phase == SeedPhase.Connect) controls.onComplete(1f)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(t.bg).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (phase) {
            SeedPhase.Soak -> SoakPhase(
                modifier = Modifier.weight(1f),
                onDone = { phase = SeedPhase.SplitChickpea },
            )
            SeedPhase.SplitChickpea -> SplitChickpeaPhase(
                modifier = Modifier.weight(1f),
                onDone = { phase = SeedPhase.TryMaize },
            )
            SeedPhase.TryMaize -> TryMaizePhase(
                modifier = Modifier.weight(1f),
                onDone = { phase = SeedPhase.Connect },
            )
            SeedPhase.Connect -> ConnectPhase(
                modifier = Modifier.weight(1f),
                onReplay = { phase = SeedPhase.Soak },
            )
        }
    }
}

/* ─────────────── Shared card frame ─────────────── */

@Composable
private fun StageCard(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(22.dp),
    ) {
        LLText(eyebrow, color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Spacer(Modifier.height(4.dp))
        LLText(title, color = t.ink50, size = 22.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/* ─────────────── Phase 0 — Soak ─────────────── */

@Composable
private fun SoakPhase(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val t = LL.tokens
    var days by remember { mutableFloatStateOf(0f) }
    val soaked = days >= 2.5f
    // 0 days → dry & wrinkled, 3 days → plump. Drives swell in the canvas.
    val swell = (days / 3f).coerceIn(0f, 1f)

    StageCard("STEP 1 · SOAK", "Leave both seeds in water for two to three days", modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.weight(1.4f).fillMaxHeight()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(t.surface2),
            ) {
                Canvas(Modifier.fillMaxSize().padding(16.dp)) { drawWaterBowl(swell) }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LLText(
                    "Drag the days forward. The seeds drink water and swell — the hard coat softens, so it will peel away easily.",
                    color = t.ink400, size = 14.sp, lineHeight = 20.sp,
                )
                LLSlider(
                    "Days in water", days, { days = it }, 0f, 3f, 0.5f, "d",
                    valueFormat = { "%.1f".format(it) },
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(if (soaked) dicotColor.copy(alpha = 0.10f) else t.surface2)
                        .padding(14.dp),
                ) {
                    LLText(
                        if (soaked) "Plump and soft — ready to open up."
                        else "Still firm. Give them the full soak.",
                        color = if (soaked) dicotColor else t.ink500,
                        size = 13.sp, weight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    label = "Open the chickpea",
                    onClick = onDone,
                    enabled = soaked,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ─────────────── Phase 1 — Split the chickpea ─────────────── */

@Composable
private fun SplitChickpeaPhase(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val t = LL.tokens
    var peel by remember { mutableFloatStateOf(0f) }      // 0 coat on → 1 coat off
    var split by remember { mutableFloatStateOf(0f) }     // 0 closed → 1 halves apart
    val peeled = peel >= 0.99f
    val opened = split >= 0.9f

    val peelAnim = remember { Animatable(0f) }
    LaunchedEffect(peel) { peelAnim.animateTo(peel, tween(500)) }

    StageCard("STEP 2 · SPLIT THE CHICKPEA", "Peel the coat, then pry it apart", modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.weight(1.4f).fillMaxHeight()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(t.surface2),
            ) {
                Canvas(Modifier.fillMaxSize().padding(16.dp)) {
                    drawChickpea(peelAnim.value, split)
                }
                if (opened) {
                    CountBadge("2", dicotColor, Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LLText(
                    if (!peeled)
                        "First lift off the thin seed coat."
                    else if (!opened)
                        "Now pull the seed apart. It splits cleanly down the middle."
                    else
                        "Two equal halves — each is a cotyledon. Have the class count them out loud.",
                    color = t.ink400, size = 14.sp, lineHeight = 20.sp,
                )
                SecondaryButton(
                    label = if (peeled) "Coat removed" else "Peel off the coat",
                    onClick = { peel = 1f },
                    enabled = !peeled,
                    modifier = Modifier.fillMaxWidth(),
                )
                LLSlider(
                    "Pry it open", split, { if (peeled) split = it }, 0f, 1f, null, "",
                    enabled = peeled,
                    valueFormat = { if (it < 0.05f) "closed" else "%.0f%%".format(it * 100) },
                )
                if (opened) {
                    LabelTag("Each half = 1 cotyledon", dicotColor)
                }
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    label = "Try the maize grain",
                    onClick = onDone,
                    enabled = opened,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ─────────────── Phase 2 — Try the maize ─────────────── */

@Composable
private fun TryMaizePhase(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val t = LL.tokens
    var pull by remember { mutableFloatStateOf(0f) }      // resists splitting
    val tried = pull >= 0.9f

    StageCard("STEP 3 · TRY THE MAIZE", "Same pull — does the grain split in two?", modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.weight(1.4f).fillMaxHeight()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(t.surface2),
            ) {
                Canvas(Modifier.fillMaxSize().padding(16.dp)) { drawMaize(pull) }
                if (tried) {
                    CountBadge("1", monocotColor, Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LLText(
                    if (!tried)
                        "Pull on the maize grain the same way you opened the chickpea."
                    else
                        "It won't fall into two. A cut through it shows just one thin cotyledon beside the baby plant. Count: one.",
                    color = t.ink400, size = 14.sp, lineHeight = 20.sp,
                )
                LLSlider(
                    "Pull it apart", pull, { pull = it }, 0f, 1f, null, "",
                    valueFormat = { if (it < 0.05f) "whole" else "%.0f%%".format(it * 100) },
                )
                if (tried) {
                    LabelTag("Only 1 cotyledon", monocotColor)
                }
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    label = "Name what we found",
                    onClick = onDone,
                    enabled = tried,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/* ─────────────── Phase 3 — Name & connect ─────────────── */

@Composable
private fun ConnectPhase(modifier: Modifier = Modifier, onReplay: () -> Unit) {
    val t = LL.tokens
    StageCard("STEP 4 · NAME & CONNECT", "Cotyledons tell two families apart", modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LLText(
                "A cotyledon is the seed's own food-packed half. Counting them sorts every flowering plant into two families — and that count lines up with the leaf and root clues from earlier activities.",
                color = t.ink400, size = 14.sp, lineHeight = 20.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FamilyCard(
                    modifier = Modifier.weight(1f),
                    color = dicotColor,
                    family = "Dicot",
                    anchor = "Chickpea (chana)",
                    cotyledons = "Two cotyledons",
                    venation = "Reticulate venation",
                    root = "Taproot",
                )
                FamilyCard(
                    modifier = Modifier.weight(1f),
                    color = monocotColor,
                    family = "Monocot",
                    anchor = "Maize",
                    cotyledons = "One cotyledon",
                    venation = "Parallel venation",
                    root = "Fibrous roots",
                )
            }
            SecondaryButton(
                label = "Run it again",
                onClick = onReplay,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FamilyCard(
    modifier: Modifier,
    color: Color,
    family: String,
    anchor: String,
    cotyledons: String,
    venation: String,
    root: String,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.md))
            .background(color.copy(alpha = 0.08f))
            .border(2.dp, color.copy(alpha = 0.5f), RoundedCornerShape(Radius.md))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(10.dp))
            LLText(family, color = color, size = 22.sp, weight = FontWeight.Bold)
        }
        LLText(anchor, color = t.ink200, size = 14.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        ConnectRow(cotyledons, color)
        ConnectRow(venation, color)
        ConnectRow(root, color)
    }
}

@Composable
private fun ConnectRow(text: String, color: Color) {
    val t = LL.tokens
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        LLText(text, color = t.ink200, size = 15.sp, weight = FontWeight.Medium)
    }
}

/* ─────────────── Small shared bits ─────────────── */

@Composable
private fun CountBadge(n: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        LLText(n, color = Color.White, size = 28.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun LabelTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(Radius.pill))
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        LLText(text, color = color, size = 13.sp, weight = FontWeight.Bold)
    }
}

/* ─────────────── Canvas drawing ─────────────── */

private fun DrawScope.drawWaterBowl(swell: Float) {
    val w = size.width
    val h = size.height
    val bowlTop = h * 0.34f
    // Bowl
    val bowl = Path().apply {
        moveTo(w * 0.12f, bowlTop)
        cubicTo(w * 0.12f, h * 0.96f, w * 0.88f, h * 0.96f, w * 0.88f, bowlTop)
        close()
    }
    drawPath(bowl, color = Color(0xFFCDE7F5))
    drawPath(bowl, color = Color(0xFF7FB6D4), style = Stroke(width = 3f))
    // Water line
    drawLine(
        Color(0xFF9AD0EC), Offset(w * 0.16f, bowlTop + 6f), Offset(w * 0.84f, bowlTop + 6f),
        strokeWidth = 4f, cap = StrokeCap.Round,
    )

    // Seeds resting in the water — swell with soak. Left chickpea, right maize.
    val baseY = h * 0.66f
    val cpR = lerp(20f, 34f, swell)
    drawSeedBlob(Offset(w * 0.36f, baseY), cpR, swell, round = true)
    val mzR = lerp(16f, 26f, swell)
    drawSeedBlob(Offset(w * 0.62f, baseY), mzR, swell, round = false)
}

private fun DrawScope.drawSeedBlob(c: Offset, r: Float, swell: Float, round: Boolean) {
    // Dry seeds are wrinkled (rough edge); soaked ones are smooth and plump.
    val fill = lerpColor(seedFillDeep, seedFill, swell)
    if (round) {
        drawCircle(fill, radius = r, center = c)
        drawCircle(Color(0xFFB8862E), radius = r, center = c, style = Stroke(width = 2.5f))
    } else {
        val path = Path().apply {
            moveTo(c.x, c.y - r)
            cubicTo(c.x + r * 0.95f, c.y - r, c.x + r * 0.95f, c.y + r, c.x, c.y + r)
            cubicTo(c.x - r * 0.95f, c.y + r, c.x - r * 0.95f, c.y - r, c.x, c.y - r)
            close()
        }
        drawPath(path, color = fill)
        drawPath(path, color = Color(0xFFB8862E), style = Stroke(width = 2.5f))
    }
    // Wrinkle hint fades as it swells
    val wrinkle = (1f - swell) * 0.6f
    if (wrinkle > 0.02f) {
        drawLine(Color(0xFFB8862E).copy(alpha = wrinkle),
            Offset(c.x - r * 0.4f, c.y), Offset(c.x + r * 0.4f, c.y), strokeWidth = 1.5f)
    }
}

private fun DrawScope.drawChickpea(peel: Float, split: Float) {
    val w = size.width
    val cx = w / 2f
    val cy = size.height * 0.52f
    val r = (size.minDimension * 0.30f)
    val gap = split * r * 1.15f

    // Seed coat fades and slides aside as it is peeled off.
    if (peel < 0.99f) {
        val coatAlpha = 1f - peel
        val shift = peel * r * 1.6f
        drawCircle(Color(0xFFC68A3A).copy(alpha = coatAlpha * 0.9f),
            radius = r * 1.06f, center = Offset(cx + shift, cy))
        drawCircle(Color(0xFF8A5A1E).copy(alpha = coatAlpha),
            radius = r * 1.06f, center = Offset(cx + shift, cy), style = Stroke(width = 2f))
    }

    // Two cotyledon halves: left moves left, right moves right.
    drawCotyledonHalf(Offset(cx - gap, cy), r, left = true)
    drawCotyledonHalf(Offset(cx + gap, cy), r, left = false)

    // Embryo (baby plant) sits at the hinge — visible once the halves part.
    if (split > 0.15f) {
        val a = ((split - 0.15f) / 0.85f).coerceIn(0f, 1f)
        drawEmbryo(Offset(cx, cy), r * 0.5f, a)
    }
}

private fun DrawScope.drawCotyledonHalf(c: Offset, r: Float, left: Boolean) {
    val sign = if (left) -1f else 1f
    // Flat side faces the centre, rounded back faces out.
    val path = Path().apply {
        moveTo(c.x, c.y - r)
        cubicTo(
            c.x + sign * r * 1.05f, c.y - r,
            c.x + sign * r * 1.05f, c.y + r,
            c.x, c.y + r,
        )
        lineTo(c.x, c.y - r)
        close()
    }
    drawPath(path, color = seedFill)
    drawPath(path, color = Color(0xFFD9A93F), style = Stroke(width = 2.5f))
    // Inner shading curve gives the half some depth.
    val inner = Path().apply {
        moveTo(c.x, c.y - r * 0.78f)
        cubicTo(
            c.x + sign * r * 0.55f, c.y - r * 0.55f,
            c.x + sign * r * 0.55f, c.y + r * 0.55f,
            c.x, c.y + r * 0.78f,
        )
    }
    drawPath(inner, color = seedFillDeep.copy(alpha = 0.6f), style = Stroke(width = 2f))
}

private fun DrawScope.drawEmbryo(c: Offset, h: Float, alpha: Float) {
    // Tiny shoot + root: the baby plant tucked between the cotyledons.
    val col = embryoColor.copy(alpha = alpha)
    drawLine(col, Offset(c.x, c.y + h), Offset(c.x, c.y - h),
        strokeWidth = 4f, cap = StrokeCap.Round)
    // Two leaf tips at the top
    drawLine(col, Offset(c.x, c.y - h), Offset(c.x - h * 0.5f, c.y - h * 1.4f),
        strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(col, Offset(c.x, c.y - h), Offset(c.x + h * 0.5f, c.y - h * 1.4f),
        strokeWidth = 3f, cap = StrokeCap.Round)
    // Root tip
    drawLine(col, Offset(c.x, c.y + h), Offset(c.x + h * 0.3f, c.y + h * 1.4f),
        strokeWidth = 3f, cap = StrokeCap.Round)
}

private fun DrawScope.drawMaize(pull: Float) {
    val w = size.width
    val cx = w / 2f
    val cy = size.height * 0.52f
    val r = size.minDimension * 0.30f
    // It resists: the two faint nudges spread only a little then the cutaway reveals.
    val nudge = pull * r * 0.18f

    // Maize grain — broad tooth shape, single solid body (does NOT halve).
    val grain = Path().apply {
        moveTo(cx, cy - r * 1.1f)
        cubicTo(cx + r * 1.0f - nudge, cy - r, cx + r * 0.95f, cy + r * 0.9f, cx + r * 0.35f, cy + r * 1.15f)
        lineTo(cx - r * 0.35f, cy + r * 1.15f)
        cubicTo(cx - r * 0.95f, cy + r * 0.9f, cx - r * 1.0f + nudge, cy - r, cx, cy - r * 1.1f)
        close()
    }
    drawPath(grain, color = seedFill)
    drawPath(grain, color = Color(0xFFD9A93F), style = Stroke(width = 2.5f))

    // Cutaway: once pulled, reveal the single thin cotyledon + embryo inside.
    if (pull > 0.4f) {
        val a = ((pull - 0.4f) / 0.6f).coerceIn(0f, 1f)
        // Single thin cotyledon — one vertical slab beside the embryo.
        val slab = Path().apply {
            moveTo(cx - r * 0.18f, cy - r * 0.8f)
            lineTo(cx + r * 0.18f, cy - r * 0.8f)
            lineTo(cx + r * 0.18f, cy + r * 0.85f)
            lineTo(cx - r * 0.18f, cy + r * 0.85f)
            close()
        }
        drawPath(slab, color = monocotColor.copy(alpha = 0.18f * a))
        drawPath(slab, color = monocotColor.copy(alpha = 0.7f * a), style = Stroke(width = 2f))
        // Embryo to one side
        drawEmbryo(Offset(cx + r * 0.45f, cy), r * 0.42f, a)
    }
}

/* ─────────────── tiny math helpers ─────────────── */

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun lerpColor(a: Color, b: Color, t: Float) = Color(
    red = lerp(a.red, b.red, t),
    green = lerp(a.green, b.green, t),
    blue = lerp(a.blue, b.blue, t),
    alpha = lerp(a.alpha, b.alpha, t),
)
