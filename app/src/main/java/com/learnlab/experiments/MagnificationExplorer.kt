package com.learnlab.experiments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.experiments.ch06kit.ActivityCard
import com.learnlab.experiments.ch06kit.ContextCard
import com.learnlab.experiments.ch06kit.InfoCard
import com.learnlab.experiments.ch06kit.NoteBox
import com.learnlab.experiments.ch06kit.NoteTone
import com.learnlab.experiments.ch06kit.QuoteCard
import com.learnlab.experiments.ch06kit.SlideDeck
import com.learnlab.experiments.ch06kit.SnapSlider
import com.learnlab.experiments.ch06kit.TemplateA
import com.learnlab.experiments.ch06kit.TemplateB
import com.learnlab.experiments.ch06kit.Timeline
import com.learnlab.experiments.ch06kit.MicroscopeCircle
import com.learnlab.experiments.ch06kit.drawOnionField
import com.learnlab.experiments.ch06kit.drawTextAt
import com.learnlab.store.ExperimentControls
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Simulation 1 — Activity 2.1: the water-filled flask as a magnifying glass.
 * Opener → refraction concept (ray diagram) → interactive magnification viewer → history of the
 * microscope (Hooke & Leeuwenhoek).
 */

private enum class Subject(val label: String) { Text("Text on a page"), Ant("An ant"), Cells("Onion peel cells") }

private val TOOLS = listOf("Naked eye (1×)", "Water flask (~3×)", "Magnifying glass (~10×)", "Microscope (100–400×)")

@Composable
fun MagnificationExplorer(controls: ExperimentControls) {
    SlideDeck(
        controls = controls,
        slideCount = 4,
        eyebrow = "ACTIVITY 2.1 · THE FLASK AS A MAGNIFYING GLASS",
        topics = listOf("How lenses work", "Why lenses work", "Explore", "History of science"),
    ) { page ->
        when (page) {
            0 -> MagOpener()
            1 -> MagConcept()
            2 -> MagViewer()
            else -> MagHistory()
        }
    }
}

@Composable
private fun MagOpener() {
    TemplateA(
        badge = "SECTION 2.1 · 10 MIN",
        title = "How small a thing can your eyes actually see?",
        lead = "The human eye can only see objects above a certain size — so for a long time many tiny things " +
            "stayed unknown. Then people found that a curved piece of glass, shaped like a lentil seed (thick " +
            "in the middle, thin at the edge), could make small things look bigger. They called it a lens.",
    ) {
        QuoteCard(
            text = "Each new tool, from simple magnifying glasses to microscopes, helped humans see what their " +
                "eyes could not. The invention of the microscope opened a fascinating hidden world filled with " +
                "tiny living creatures.",
            attribution = "Curiosity, Grade 8, Chapter 2",
        )
        ContextCard(
            label = "The question",
            body = "In Activity 2.1 you place a water-filled round-bottom flask on an open book and look at the " +
                "letters through it. The letters appear larger. Why? And what does that have to do with how we " +
                "discovered the invisible living world?",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard("What you will discover",
                "Why a curved surface bends light, how different tools reach different magnifications, and what " +
                    "the microscope made possible.",
                Modifier.weight(1f))
            InfoCard("Key vocabulary",
                "Lens · Refraction · Magnification · Magnifying glass · Microscope",
                Modifier.weight(1f))
        }
    }
}

@Composable
private fun MagConcept() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    TemplateA(
        badge = "SECTION 2.1 · WHY IT WORKS",
        title = "Why does the flask make letters look bigger?",
        lead = "When light passes from one medium into another — say air into water — it bends. This bending is " +
            "called refraction. The curved surface of the water-filled flask bends the light rays from the text, " +
            "so by the time they reach your eye the letters look larger. The flask is acting like a lens.",
    ) {
        Box(
            Modifier.fillMaxWidth().height(190.dp)
                .clip(RoundedCornerShape(12.dp)).background(t.surface2).border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                val panelW = size.width / 3f
                drawRayPanel(tm, 0f, panelW, size.height, RayKind.Naked, t.ink400)
                drawRayPanel(tm, panelW, panelW, size.height, RayKind.Flask, t.ink400)
                drawRayPanel(tm, panelW * 2f, panelW, size.height, RayKind.Scope, t.ink400)
            }
        }
        LLText(
            "All three work by the same principle — bending light through a curved surface. Only the precision " +
                "and the power differ.",
            color = t.ink400, size = 12.sp, align = TextAlign.Center,
        )
        NoteBox(
            text = "The word \"lens\" comes from the Latin for lentil — early glass lenses were shaped exactly " +
                "like lentil seeds: round, thick in the middle, thin at the edges. A lentil in your kitchen is, " +
                "in fact, a lens.",
            tone = NoteTone.Info, label = "Real connection",
        )
    }
}

private enum class RayKind { Naked, Flask, Scope }

private fun DrawScope.drawRayPanel(tm: TextMeasurer, x0: Float, w: Float, h: Float, kind: RayKind, ink: Color) {
    val cx = x0 + w / 2f
    val midY = h * 0.5f
    val objX = x0 + w * 0.16f
    val eyeX = x0 + w * 0.86f
    val objColor = Color(0xFF60A5FA)
    val rayColor = Color(0xFFF59E0B)
    // object arrow
    drawLine(objColor, Offset(objX, midY + 22f), Offset(objX, midY - 22f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(objColor, Offset(objX, midY - 22f), Offset(objX - 5f, midY - 14f), strokeWidth = 3f)
    drawLine(objColor, Offset(objX, midY - 22f), Offset(objX + 5f, midY - 14f), strokeWidth = 3f)
    // lens / flask in the middle
    when (kind) {
        RayKind.Naked -> { /* nothing */ }
        RayKind.Flask -> drawCircle(Color(0xFF38BDF8).copy(alpha = 0.30f), w * 0.13f, Offset(cx, midY)).also {
            drawCircle(Color(0xFF0EA5E9), w * 0.13f, Offset(cx, midY), style = Stroke(2f))
        }
        RayKind.Scope -> {
            // a biconvex lens
            val lw = w * 0.07f; val lh = h * 0.34f
            val p = Path().apply {
                moveTo(cx, midY - lh / 2f)
                quadraticTo(cx + lw, midY, cx, midY + lh / 2f)
                quadraticTo(cx - lw, midY, cx, midY - lh / 2f)
                close()
            }
            drawPath(p, Color(0xFF38BDF8).copy(alpha = 0.35f))
            drawPath(p, Color(0xFF0EA5E9), style = Stroke(2f))
        }
    }
    // rays from object top toward eye, bent at the optic
    val topY = midY - 18f
    if (kind == RayKind.Naked) {
        drawLine(rayColor, Offset(objX, topY), Offset(eyeX, midY - 6f), strokeWidth = 1.5f)
    } else {
        drawLine(rayColor, Offset(objX, topY), Offset(cx, midY - (if (kind == RayKind.Scope) 30f else 22f)), strokeWidth = 1.5f)
        drawLine(rayColor, Offset(cx, midY - (if (kind == RayKind.Scope) 30f else 22f)), Offset(eyeX, midY - 8f), strokeWidth = 1.5f)
        // magnified virtual arrow (dashed, larger)
        val mag = if (kind == RayKind.Scope) 1.9f else 1.4f
        drawLine(objColor.copy(alpha = 0.5f), Offset(objX, midY + 22f * mag), Offset(objX, midY - 22f * mag), strokeWidth = 2f)
    }
    // eye
    drawCircle(ink, 7f, Offset(eyeX, midY), style = Stroke(2f))
    drawCircle(ink, 2.5f, Offset(eyeX, midY))
    // caption
    val cap = when (kind) { RayKind.Naked -> "Naked eye"; RayKind.Flask -> "Water flask · refraction"; RayKind.Scope -> "Microscope lens" }
    drawTextAt(tm, cap, Offset(x0 + 6f, h - 16f), ink, 10.sp, FontWeight.SemiBold)
}

@Composable
private fun MagViewer() {
    val t = LL.tokens
    val tm = rememberTextMeasurer()
    var subject by remember { mutableStateOf(Subject.Text) }
    var zoom by remember { mutableStateOf(0) }

    TemplateB(
        left = {
            ActivityCard(
                code = "ACTIVITY 2.1 · INVESTIGATE",
                title = "The magnification explorer",
                description = "Drag the slider to change your viewing tool — from naked eye to flask to " +
                    "magnifying glass to microscope.",
                tags = listOf(
                    "🔍  Drag the slider to magnify",
                    "👁️  Watch detail appear at each level",
                    "🐜  Try the text, an ant, and onion cells",
                ),
            )
            NoteBox(
                text = "The flask and the magnifying glass use the same physics as the microscope. The " +
                    "microscope differs by precision — lenses ground to exact curves — and by combining several " +
                    "lenses to multiply the magnification.",
                tone = NoteTone.Info,
            )
        },
        right = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Subject.entries.forEach { s ->
                    SelectPill(s.label, s == subject, Modifier.weight(1f)) { subject = s }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                MicroscopeCircle(Modifier.fillMaxSize()) { c, r ->
                    drawSubject(tm, subject, zoom, c, r)
                }
            }
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(10.dp)).padding(10.dp),
            ) {
                LLText(TOOLS[zoom], color = t.accent700, size = 13.sp, weight = FontWeight.Bold)
            }
            SnapSlider("Viewing tool", TOOLS, zoom, { zoom = it })
        },
    )
}

@Composable
private fun SelectPill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) t.accent50 else t.surface2)
            .border(1.dp, if (selected) t.accent500 else t.line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = if (selected) t.accent700 else t.ink400, size = 11.sp,
            weight = FontWeight.SemiBold, align = TextAlign.Center)
    }
}

private fun DrawScope.drawSubject(tm: TextMeasurer, subject: Subject, zoom: Int, c: Offset, r: Float) {
    when (subject) {
        Subject.Text -> {
            if (zoom < 3) {
                val sz = (10 + zoom * 10).sp
                drawTextAt(tm, "abc", Offset(c.x - r * 0.5f, c.y - r * 0.2f), Color(0xFF1F2937), sz, FontWeight.Bold)
            } else {
                // paper fibres
                val col = Color(0xFFB45309).copy(alpha = 0.5f)
                for (i in 0 until 22) {
                    val a = i * 0.9f
                    val x = c.x - r + (i * (2 * r) / 22f)
                    drawLine(col, Offset(x, c.y - r * 0.6f + sin(a) * r * 0.3f),
                        Offset(x + r * 0.5f, c.y + r * 0.5f - cos(a) * r * 0.3f), strokeWidth = 1.4f)
                }
                drawTextAt(tm, "paper fibres", Offset(c.x - r * 0.45f, c.y + r * 0.55f), Color(0xFF6B7280), 10.sp)
            }
        }
        Subject.Ant -> drawAnt(c, r * (0.35f + zoom * 0.22f), detail = zoom)
        Subject.Cells -> {
            if (zoom < 3) {
                val msg = when (zoom) {
                    0 -> "invisible to naked eye"
                    1 -> "too small — need more"
                    else -> "not powerful enough"
                }
                drawTextAt(tm, msg, Offset(c.x - r * 0.55f, c.y - 6f), Color(0xFF9CA3AF), 11.sp, FontWeight.SemiBold)
            } else {
                // onion cells fill the field (the MicroscopeCircle already clips to the circle)
                drawOnionField()
            }
        }
    }
}

private fun DrawScope.drawAnt(c: Offset, s: Float, detail: Int) {
    val col = Color(0xFF3F2A1E)
    // three body segments
    val head = Offset(c.x - s * 1.4f, c.y)
    val thorax = Offset(c.x, c.y)
    val abdomen = Offset(c.x + s * 1.5f, c.y)
    drawCircle(col, s * 0.55f, head)
    drawCircle(col, s * 0.6f, thorax)
    drawOval(col, topLeft = Offset(abdomen.x - s * 0.9f, abdomen.y - s * 0.65f), size = Size(s * 1.8f, s * 1.3f))
    // legs (6) — more visible / jointed at higher detail
    val legCol = if (detail >= 2) Color(0xFF6B4A36) else col
    for (i in 0 until 3) {
        val lx = thorax.x + (i - 1) * s * 0.5f
        val up = if (detail >= 1) s * 1.3f else s * 1.0f
        drawLine(legCol, Offset(lx, thorax.y), Offset(lx - s * 0.6f, thorax.y - up), strokeWidth = if (detail >= 2) 2.5f else 1.5f, cap = StrokeCap.Round)
        drawLine(legCol, Offset(lx, thorax.y), Offset(lx - s * 0.6f, thorax.y + up), strokeWidth = if (detail >= 2) 2.5f else 1.5f, cap = StrokeCap.Round)
    }
    // antennae
    drawLine(col, head, Offset(head.x - s * 0.7f, head.y - s * 0.8f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    drawLine(col, head, Offset(head.x - s * 0.7f, head.y - s * 0.3f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    // hairs at highest detail
    if (detail >= 3) {
        for (i in 0 until 16) {
            val a = i * (2f * PI.toFloat() / 16f)
            val bx = abdomen.x + cos(a) * s * 0.9f
            val by = abdomen.y + sin(a) * s * 0.65f
            drawLine(Color(0xFF1F2937), Offset(bx, by), Offset(bx + cos(a) * s * 0.3f, by + sin(a) * s * 0.3f), strokeWidth = 1f)
        }
        // compound eye on head
        drawCircle(Color(0xFF111827), s * 0.18f, Offset(head.x - s * 0.2f, head.y - s * 0.1f))
    }
}

@Composable
private fun MagHistory() {
    val t = LL.tokens
    TemplateA(
        badge = "EVER HEARD OF… · HISTORY OF SCIENCE",
        title = "The man who first saw the invisible world",
        lead = "In 1665 Robert Hooke published Micrographia — detailed drawings of tiny things seen through a " +
            "tool we now call a microscope. His microscope magnified 200–300 times. Looking at a thin slice of " +
            "cork, he saw many small empty spaces like a honeycomb and called each one a cell — the first time the " +
            "word was used in science.",
    ) {
        LLText(
            "Around the same time, the Dutch scientist Antonie van Leeuwenhoek made better lenses and built more " +
                "useful microscopes. He was the first to clearly see and describe tiny living things like bacteria " +
                "and blood cells, and is known as the Father of Microbiology.",
            color = t.ink200, size = 13.sp, lineHeight = 19.sp,
        )
        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Timeline(
                listOf(
                    "1665" to "Hooke coins \"cell\"",
                    "1670s" to "Leeuwenhoek sees bacteria",
                    "1800s" to "Compound scopes reach 1000×",
                    "1930s" to "Electron microscope",
                    "Today" to "You observe cells",
                ),
            )
        }
        NoteBox(
            text = "Every time you prepare a slide and look into a microscope, you are doing what Hooke did in " +
                "1665. He called those empty spaces in cork \"cells\" because they reminded him of the small rooms " +
                "(cellula) where monks slept. That word has lasted 360 years.",
            tone = NoteTone.Info, label = "Conclusion",
        )
    }
}
