package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.store.ExperimentControls
import kotlin.math.abs

/**
 * Scientific Method Detective. A pen has stopped writing. Inspect each part
 * (cap, body, refill, tip), pick a hypothesis from 4, apply a fix from 4.
 * If the fix matches the actual hidden fault, the pen writes again.
 */

private enum class Fault { INK_EMPTY, TIP_DRIED, TIP_BLOCKED, REFILL_LEAK }
private enum class Fix   { REFILL_INK, SOAK_TIP, CLEAN_TIP, REPLACE_REFILL }

private data class FaultInfo(
    val hypothesis: String,
    val cap: String, val refill: String, val tip: String, val body: String,
    val correctFix: Fix,
)

private val FAULT_INFO: Map<Fault, FaultInfo> = mapOf(
    Fault.INK_EMPTY to FaultInfo(
        hypothesis = "The ink has finished.",
        cap = "Cap looks normal.",
        refill = "Refill chamber is dry. Almost no ink left.",
        tip = "Tip is clean but pale — no ink coming through.",
        body = "Body looks fine.",
        correctFix = Fix.REFILL_INK,
    ),
    Fault.TIP_DRIED to FaultInfo(
        hypothesis = "The ink in the tip has dried up.",
        cap = "Cap was loose — left off the pen.",
        refill = "Refill still has ink.",
        tip = "Tip has a crust of dried ink. Won't deliver new ink.",
        body = "Body looks fine.",
        correctFix = Fix.SOAK_TIP,
    ),
    Fault.TIP_BLOCKED to FaultInfo(
        hypothesis = "The tip is blocked by paper fibres.",
        cap = "Cap looks normal.",
        refill = "Refill is full.",
        tip = "Tiny paper fibres stuck in the ball of the tip.",
        body = "Body looks fine.",
        correctFix = Fix.CLEAN_TIP,
    ),
    Fault.REFILL_LEAK to FaultInfo(
        hypothesis = "The refill is cracked and leaking.",
        cap = "Cap stained with ink inside.",
        refill = "Hairline crack in the refill plastic. Ink leaking out the side.",
        tip = "Tip looks clean but the pen body is sticky.",
        body = "Inside of the body has dried ink streaks.",
        correctFix = Fix.REPLACE_REFILL,
    ),
)

private fun randomFault(): Fault = Fault.entries.random()

@Composable
fun ScientificMethodDetective(controls: ExperimentControls) {
    val t = LL.tokens
    var fault by remember { mutableStateOf(randomFault()) }
    var inspCap by remember { mutableStateOf(false) }
    var inspRefill by remember { mutableStateOf(false) }
    var inspTip by remember { mutableStateOf(false) }
    var inspBody by remember { mutableStateOf(false) }
    var hypothesis by remember { mutableStateOf<Fault?>(null) }
    var appliedFix by remember { mutableStateOf<Fix?>(null) }

    val info = FAULT_INFO.getValue(fault)
    val inspectedCount by remember {
        derivedStateOf { listOf(inspCap, inspRefill, inspTip, inspBody).count { it } }
    }
    val solved = appliedFix == info.correctFix

    LaunchedEffect(inspectedCount, hypothesis, appliedFix, solved) {
        var p = 0f
        p += (inspectedCount / 4f) * 0.25f
        if (hypothesis != null) p += 0.25f
        if (appliedFix != null) p += 0.25f
        if (solved) p = 1f
        controls.onProgress(p)
        if (solved) controls.onComplete(if (hypothesis == fault) 1f else 0.5f)
    }

    fun reset() {
        fault = randomFault()
        inspCap = false; inspRefill = false; inspTip = false; inspBody = false
        hypothesis = null; appliedFix = null
        controls.onProgress(0f)
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Left: pen + observation log
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    LLText("THE PEN WORKBENCH", color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(4.dp))
                    LLText(
                        "Tap each part to inspect it. Then form a hypothesis, then try a fix.",
                        color = t.ink400, size = 13.sp,
                    )
                }
                GhostButton(label = "New mystery", onClick = { reset() })
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                PenCanvas(
                    inspCap = inspCap, inspRefill = inspRefill,
                    inspTip = inspTip, inspBody = inspBody,
                    fault = fault, writing = solved,
                    onInspect = { part ->
                        when (part) {
                            "cap" -> inspCap = true
                            "refill" -> inspRefill = true
                            "tip" -> inspTip = true
                            "body" -> inspBody = true
                        }
                    },
                )
            }

            LLText(
                if (solved) "Pen writes — fix successful." else "Tap each part of the pen to inspect it. Observation comes first.",
                color = t.ink500, size = 12.sp,
            )

            // Observation log
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LLText("OBSERVATION LOG", color = t.ink500, size = 10.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                LogRow("Cap", inspCap, info.cap)
                LogRow("Refill", inspRefill, info.refill)
                LogRow("Tip", inspTip, info.tip)
                LogRow("Body", inspBody, info.body)
            }
        }

        // Right: steps + result
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepCard(n = 1, title = "Observe", done = inspectedCount > 0) {
                LLText("$inspectedCount/4 parts inspected. Tap each part of the pen.",
                    color = t.ink400, size = 13.sp)
            }
            StepCard(n = 2, title = "Hypothesis", done = hypothesis != null, dim = inspectedCount == 0) {
                LLText("What do you think is wrong?", color = t.ink400, size = 13.sp)
                Spacer(Modifier.height(8.dp))
                Fault.entries.forEach { f ->
                    HypothesisChoice(
                        label = FAULT_INFO.getValue(f).hypothesis,
                        active = hypothesis == f,
                        enabled = inspectedCount > 0,
                        onClick = { hypothesis = f },
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
            StepCard(n = 3, title = "Test (apply a fix)", done = appliedFix != null, dim = hypothesis == null) {
                LLText("Which fix do you try?", color = t.ink400, size = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FixButton("Add new ink", Fix.REFILL_INK, appliedFix, hypothesis != null) { appliedFix = it }
                    FixButton("Soak the tip", Fix.SOAK_TIP, appliedFix, hypothesis != null) { appliedFix = it }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FixButton("Clean the tip", Fix.CLEAN_TIP, appliedFix, hypothesis != null) { appliedFix = it }
                    FixButton("Replace refill", Fix.REPLACE_REFILL, appliedFix, hypothesis != null) { appliedFix = it }
                }
            }
            ResultCard(appliedFix, solved, hypothesis, fault, info, ::reset)
        }
    }
}

@Composable
private fun LogRow(part: String, revealed: Boolean, text: String) {
    val t = LL.tokens
    Row {
        LLText(part, color = t.ink200, size = 12.sp,
            weight = FontWeight.SemiBold, modifier = Modifier.width(60.dp))
        LLText(
            if (revealed) text else "— not inspected",
            color = if (revealed) t.ink200 else t.ink600,
            size = 12.sp, lineHeight = 16.sp,
        )
    }
}

@Composable
private fun StepCard(
    n: Int, title: String, done: Boolean, dim: Boolean = false,
    content: @Composable () -> Unit,
) {
    val t = LL.tokens
    val border = if (done) t.accent300 else t.line
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape)
                    .background(if (done) t.accent600 else t.surface3),
                contentAlignment = Alignment.Center,
            ) {
                if (done) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                } else {
                    LLText("$n", color = t.ink400, size = 11.sp, weight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(8.dp))
            LLText(title, color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Column { content() }
    }
    if (dim) {
        Spacer(Modifier.height(0.dp))
    }
}

@Composable
private fun HypothesisChoice(label: String, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    val bg = if (active) t.accent50 else t.surface
    val border = if (active) t.accent500 else t.line
    val fg = if (active) t.accent700 else t.ink200
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) { LLText(label, color = fg, size = 13.sp) }
}

@Composable
private fun FixButton(label: String, id: Fix, current: Fix?, enabled: Boolean, onClick: (Fix) -> Unit) {
    val t = LL.tokens
    val active = current == id
    val bg = if (active) t.accent50 else t.surface
    val border = if (active) t.accent500 else t.line
    val fg = if (active) t.accent700 else t.ink200
    Box(
        modifier = Modifier
            .width(165.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick(id) }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) { LLText(label, color = fg, size = 12.sp, weight = FontWeight.Medium) }
}

@Composable
private fun ResultCard(
    appliedFix: Fix?, solved: Boolean, hypothesis: Fault?, fault: Fault,
    info: FaultInfo, onReset: () -> Unit,
) {
    val t = LL.tokens
    when {
        appliedFix == null -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) { LLText("Step 4 — Analyse: shows the result once you apply a fix.",
            color = t.ink500, size = 13.sp) }
        solved -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(t.accent50)
                .border(1.dp, t.accent500, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            LLText("RESOLVED", color = t.accent700, size = 11.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(4.dp))
            LLText(
                if (hypothesis == fault)
                    "The pen writes. Hypothesis was correct. Actual fault: ${info.hypothesis}"
                else
                    "The pen writes. Hypothesis was incorrect, but the fix happened to work. Actual fault: ${info.hypothesis}",
                color = t.ink200, size = 13.sp, lineHeight = 18.sp,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(label = "Run a new case", onClick = onReset, modifier = Modifier.fillMaxWidth())
        }
        else -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(t.rose50)
                .border(1.dp, t.rose300, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            LLText("PEN STILL DOES NOT WRITE", color = t.rose700, size = 11.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(4.dp))
            LLText(
                "That fix did not resolve the fault. Revise the hypothesis and try a different fix. " +
                    "In the scientific method, a wrong guess is informative — not a failure.",
                color = t.ink200, size = 13.sp, lineHeight = 18.sp,
            )
        }
    }
}

/* ───────────────────────── pen canvas ───────────────────────── */

@Composable
private fun PenCanvas(
    inspCap: Boolean, inspRefill: Boolean, inspTip: Boolean, inspBody: Boolean,
    fault: Fault, writing: Boolean, onInspect: (String) -> Unit,
) {
    // Pen drawn in a 600×200 virtual coord space, hit-tested on tap.
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .heightIn(min = 200.dp, max = 240.dp)
            .pointerInput(Unit) {
                detectTapsScaled(virtualW = 600f, virtualH = 200f) { vx, vy ->
                    val hit = hitPart(vx, vy)
                    if (hit != null) onInspect(hit)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sx = size.width / 600f
            val sy = size.height / 200f
            // Cap
            run {
                val color = if (inspCap) Color(0xFFBFDBFE) else Color(0xFFDBEAFE)
                drawRoundedRect(
                    color, Offset(20f * sx, 80f * sy),
                    Size(80f * sx, 40f * sy), corner = 8f * sx,
                    stroke = Stroke(2f), strokeColor = Color(0xFF60A5FA),
                )
                drawRoundedRect(
                    Color(0xFF3B82F6), Offset(92f * sx, 78f * sy),
                    Size(6f * sx, 44f * sy), corner = 2f * sx,
                )
            }
            // Body
            run {
                val color = if (inspBody) Color(0xFFFEF3C7) else Color(0xFFFDE68A)
                drawRoundedRect(
                    color, Offset(105f * sx, 85f * sy),
                    Size(350f * sx, 30f * sy), corner = 6f * sx,
                    stroke = Stroke(2f), strokeColor = Color(0xFFF59E0B),
                )
            }
            // Refill (visible when body inspected; coloured when refill inspected)
            if (inspBody) {
                drawRoundedRect(
                    Color(0xFFFDE68A), Offset(115f * sx, 92f * sy),
                    Size(335f * sx, 16f * sy), corner = 4f * sx,
                )
            }
            if (inspRefill) {
                val refillColor = when (fault) {
                    Fault.INK_EMPTY -> Color(0xFFE5E7EB)
                    Fault.REFILL_LEAK -> Color(0xFFCBD5E1)
                    else -> Color(0xFF1E40AF)
                }
                drawRoundedRect(
                    refillColor, Offset(115f * sx, 93f * sy),
                    Size(335f * sx, 14f * sy), corner = 4f * sx,
                    stroke = Stroke(1.5f), strokeColor = Color(0xFF1E40AF),
                )
            }
            // Tip
            run {
                val tipColor = if (inspTip) when (fault) {
                    Fault.TIP_DRIED -> Color(0xFF7C2D12)
                    Fault.TIP_BLOCKED -> Color(0xFFFEF3C7)
                    else -> Color(0xFF1E40AF)
                } else Color(0xFF94A3B8)
                val p = Path().apply {
                    moveTo(455f * sx, 82f * sy)
                    lineTo(540f * sx, 100f * sy)
                    lineTo(455f * sx, 118f * sy)
                    close()
                }
                drawPath(p, tipColor)
                drawPath(p, Color(0xFF475569), style = Stroke(2f))
                val ballColor = if (writing) Color(0xFF1E40AF)
                    else if (inspTip) Color(0xFF0F172A) else Color(0xFF475569)
                drawCircle(ballColor, radius = 4f * sx, center = Offset(545f * sx, 100f * sy))
            }
            // Writing trail
            if (writing) {
                val p = Path().apply {
                    moveTo(555f * sx, 110f * sy)
                    quadraticBezierTo(575f * sx, 105f * sy, 595f * sx, 115f * sy)
                }
                drawPath(p, Color(0xFF1E40AF),
                    style = Stroke(2.5f, cap = StrokeCap.Round))
            }
        }
    }
}

// ── Geometry helpers ──

private fun hitPart(vx: Float, vy: Float): String? {
    // Match the 600×200 layout used in PenCanvas
    if (Rect(20f, 80f, 100f, 120f).contains(Offset(vx, vy))) return "cap"
    if (Rect(455f, 80f, 545f, 120f).contains(Offset(vx, vy))) return "tip"
    // Refill is inside the body — prefer refill if click is in upper band, else body
    if (Rect(115f, 92f, 450f, 108f).contains(Offset(vx, vy))) return "refill"
    if (Rect(105f, 85f, 455f, 115f).contains(Offset(vx, vy))) return "body"
    return null
}

/**
 * Pointer scope helper: detect tap, scale from physical pixels to virtual
 * coords, call back with the virtual (vx, vy).
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapsScaled(
    virtualW: Float, virtualH: Float,
    onTap: (vx: Float, vy: Float) -> Unit,
) {
    detectTapGestures(
        onTap = { offset: androidx.compose.ui.geometry.Offset ->
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            if (w > 0f && h > 0f) {
                onTap(offset.x * virtualW / w, offset.y * virtualH / h)
            }
        },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedRect(
    color: Color, topLeft: Offset, size: Size, corner: Float,
    stroke: Stroke? = null, strokeColor: Color? = null,
) {
    val cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
    drawRoundRect(color = color, topLeft = topLeft, size = size, cornerRadius = cornerRadius)
    if (stroke != null && strokeColor != null) {
        drawRoundRect(color = strokeColor, topLeft = topLeft, size = size,
            cornerRadius = cornerRadius, style = stroke)
    }
}

@Suppress("unused")
private fun keepAbs() = abs(0)
