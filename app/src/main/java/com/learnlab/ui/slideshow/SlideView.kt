package com.learnlab.ui.slideshow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.ChapterSlide
import com.learnlab.content.SlideIcon
import com.learnlab.content.findExperiment
import com.learnlab.design.Inter
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SubjectBiology
import com.learnlab.design.SubjectChemistry
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics
import com.learnlab.design.gradHeadline
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storm
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.learnlab.content.FigureKind
import com.learnlab.content.InteractiveKind
import com.learnlab.design.LLSlider
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Renders one generated chapter slide. [index] drives the cycling accent colour. */
@Composable
fun SlideView(slide: ChapterSlide, index: Int, onRunExperiment: (String) -> Unit = {}) {
    val t = LL.tokens
    val accent = t.accent500  // single forest-green accent on warm paper

    Box(modifier = Modifier.fillMaxSize().background(t.bg)) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 26.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .shadow(10.dp, RoundedCornerShape(24.dp), clip = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(24.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 44.dp, vertical = 38.dp),
            ) {
                when (slide) {
                    is ChapterSlide.Title -> TitleSlide(slide, accent)
                    is ChapterSlide.Concept -> ConceptSlide(slide, accent)
                    is ChapterSlide.Steps -> StepsSlide(slide, accent)
                    is ChapterSlide.Activity -> ActivitySlide(slide, accent)
                    is ChapterSlide.Quote -> QuoteSlide(slide, accent)
                    is ChapterSlide.Split -> SplitSlide(slide, accent)
                    is ChapterSlide.Chips -> ChipsSlide(slide, accent)
                    is ChapterSlide.Table -> TableSlide(slide, accent)
                    is ChapterSlide.Figure -> FigureSlide(slide, accent)
                    is ChapterSlide.Interactive -> InteractiveSlide(slide, accent)
                    is ChapterSlide.SectionHeader -> SectionHeaderSlide(slide, accent)
                    is ChapterSlide.Scene -> SceneSlide(slide, accent)
                    is ChapterSlide.Closing -> ClosingSlide(slide, accent)
                    is ChapterSlide.Experiment -> ExperimentSlide(slide, accent, onRunExperiment)
                }
            }
        }
    }
}

@Composable
private fun Kicker(text: String, accent: Color) {
    LLText(text, color = accent, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
}

@Composable
private fun GradientTitle(text: String, size: Int, align: TextAlign? = null) {
    val t = LL.tokens
    Text(
        text = text,
        color = t.ink50,
        fontFamily = Inter,
        fontSize = size.sp,
        lineHeight = (size * 1.1f).sp,
        fontWeight = FontWeight.Bold,
        textAlign = align,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SectionHeaderSlide(s: ChapterSlide.SectionHeader, accent: Color) {
    val t = LL.tokens
    Column {
        LLText("SECTION ${s.number}", color = accent, size = 13.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(14.dp))
        LLText(s.title, color = t.ink50, size = 40.sp, weight = FontWeight.Bold, lineHeight = 46.sp)
        if (s.intro.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(text = slideBold(s.intro, accent), color = t.ink200, fontSize = 19.sp, lineHeight = 30.sp)
        }
    }
}

@Composable
private fun SceneSlide(s: ChapterSlide.Scene, accent: Color) {
    val t = LL.tokens
    val lav = Color(0xFF7C6BD6)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(lav.copy(alpha = if (t.isDark) 0.16f else 0.10f))
            .border(1.dp, lav.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(30.dp),
    ) {
        Column {
            LLText(s.kicker.ifBlank { "SCENE" }, color = lav, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(14.dp))
            Text(text = slideBold(s.body, lav), color = t.ink200, fontSize = 21.sp, lineHeight = 33.sp)
        }
    }
}

@Composable
private fun TitleSlide(s: ChapterSlide.Title, accent: Color) {
    val t = LL.tokens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(20.dp))
        GradientTitle(s.title, size = 40, align = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LLText(s.subtitle, color = t.ink200, size = 18.sp, lineHeight = 26.sp, align = TextAlign.Center)
        if (s.points.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                s.points.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(26.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) { LLText("?", color = accent, size = 15.sp, weight = FontWeight.Bold) }
                        Spacer(Modifier.width(14.dp))
                        LLText(p, color = t.ink200, size = 16.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConceptSlide(s: ChapterSlide.Concept, accent: Color) {
    val t = LL.tokens
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Kicker(s.kicker, accent)
            Spacer(Modifier.height(18.dp))
            LLText(s.title, color = t.ink50, size = 34.sp, weight = FontWeight.Bold, lineHeight = 40.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = slideBold(s.body, accent),
                color = t.ink200,
                fontFamily = Inter,
                fontSize = 18.sp,
                lineHeight = 28.sp,
            )
        }
        Spacer(Modifier.width(48.dp))
        Box(
            modifier = Modifier.size(150.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconFor(s.icon), contentDescription = null, tint = accent, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun StepsSlide(s: ChapterSlide.Steps, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            s.steps.forEachIndexed { i, step ->
                val lead = step.substringBefore(" — ", "")
                val rest = if (" — " in step) step.substringAfter(" — ") else step
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) { LLText("${i + 1}", color = accent, size = 18.sp, weight = FontWeight.Bold) }
                    Spacer(Modifier.width(18.dp))
                    Text(
                        text = buildAnnotatedString {
                            if (lead.isNotEmpty()) {
                                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = t.ink50))
                                append(lead); pop()
                                append("  —  ")
                            }
                            append(slideBold(rest, t.ink50))
                        },
                        color = t.ink200,
                        fontFamily = Inter,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivitySlide(s: ChapterSlide.Activity, accent: Color) {
    val t = LL.tokens
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        // Left: the teacher's framing — what we explore and why.
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(accent)
                .padding(26.dp),
        ) {
            LLText(s.kicker, color = Color.White.copy(alpha = 0.85f), size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(14.dp))
            LLText(s.title, color = Color.White, size = 28.sp, weight = FontWeight.Bold, lineHeight = 34.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = slideBold(s.purpose, Color.White),
                color = Color.White.copy(alpha = 0.92f),
                fontFamily = Inter,
                fontSize = 17.sp,
                lineHeight = 26.sp,
            )
        }
        // Right: what to do, then what to notice (the learning).
        Column(modifier = Modifier.weight(1f)) {
            LLText("WHAT TO DO", color = accent, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                s.steps.forEachIndexed { i, step ->
                    val lead = step.substringBefore(" — ", "")
                    val rest = if (" — " in step) step.substringAfter(" — ") else step
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) { LLText("${i + 1}", color = accent, size = 14.sp, weight = FontWeight.Bold) }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = buildAnnotatedString {
                                if (lead.isNotEmpty()) {
                                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = t.ink50))
                                    append(lead); pop()
                                    append("  —  ")
                                }
                                append(slideBold(rest, t.ink50))
                            },
                            color = t.ink200,
                            fontFamily = Inter,
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = if (t.isDark) 0.12f else 0.10f))
                    .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                    .padding(18.dp),
            ) {
                Column {
                    LLText("WHAT TO NOTICE", color = accent, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = slideBold(s.observe, accent),
                        color = t.ink200,
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteSlide(s: ChapterSlide.Quote, accent: Color) {
    val t = LL.tokens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(22.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 780.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(accent.copy(alpha = if (t.isDark) 0.10f else 0.08f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .padding(horizontal = 36.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (s.original.isNotBlank()) {
                LLText(s.original, color = accent, size = 26.sp, weight = FontWeight.Bold, align = TextAlign.Center, lineHeight = 38.sp)
                Spacer(Modifier.height(14.dp))
            }
            if (s.transliteration.isNotBlank()) {
                Text(
                    text = s.transliteration,
                    color = t.ink400,
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
            }
            LLText(s.translation, color = t.ink200, size = 18.sp, align = TextAlign.Center, lineHeight = 28.sp)
            Spacer(Modifier.height(18.dp))
            LLText(s.attribution, color = accent, size = 14.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
        }
    }
}

@Composable
private fun SplitSlide(s: ChapterSlide.Split, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            SplitPanel(s.leftTitle, s.leftBody, accent, Modifier.weight(1f))
            SplitPanel(s.rightTitle, s.rightBody, accent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SplitPanel(title: String, body: String, accent: Color, modifier: Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = if (t.isDark) 0.12f else 0.08f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(22.dp),
    ) {
        LLText(title, color = accent, size = 19.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            text = slideBold(body, accent),
            color = t.ink200,
            fontFamily = Inter,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsSlide(s: ChapterSlide.Chips, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        LLText(s.body, color = t.ink400, size = 16.sp)
        Spacer(Modifier.height(24.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            s.chips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = 0.10f))
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                ) { LLText(chip, color = t.ink50, size = 15.sp, weight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
private fun ClosingSlide(s: ChapterSlide.Closing, accent: Color) {
    val t = LL.tokens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(36.dp)) }
        Spacer(Modifier.height(24.dp))
        GradientTitle(s.title, size = 42, align = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LLText(s.subtitle, color = t.ink200, size = 18.sp, lineHeight = 27.sp, align = TextAlign.Center)
    }
}

@Composable
private fun ExperimentSlide(s: ChapterSlide.Experiment, accent: Color, onRun: (String) -> Unit) {
    val t = LL.tokens
    val exp = findExperiment(s.experimentId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Kicker("EXPERIMENT", accent)
            Spacer(Modifier.height(18.dp))
            LLText(
                exp?.title ?: "Experiment",
                color = t.ink50, size = 34.sp, weight = FontWeight.Bold, lineHeight = 40.sp,
            )
            if (exp?.blurb != null) {
                Spacer(Modifier.height(14.dp))
                LLText(exp.blurb, color = t.ink200, size = 18.sp, lineHeight = 27.sp)
            }
            Spacer(Modifier.height(28.dp))
            PrimaryButton(label = "Run experiment ›", onClick = { onRun(s.experimentId) })
        }
        Spacer(Modifier.width(48.dp))
        Box(
            modifier = Modifier.size(150.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Science, contentDescription = null, tint = accent, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun TableSlide(s: ChapterSlide.Table, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(16.dp)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accent.copy(alpha = 0.16f))
                    .padding(vertical = 14.dp, horizontal = 18.dp),
            ) {
                s.headers.forEach { h ->
                    LLText(
                        h, color = accent, size = 16.sp, weight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    )
                }
            }
            s.rows.forEachIndexed { i, row ->
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (i % 2 == 1) t.surface2.copy(alpha = if (t.isDark) 0.4f else 0.6f)
                            else Color.Transparent,
                        )
                        .padding(vertical = 13.dp, horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEachIndexed { j, cell ->
                        LLText(
                            cell,
                            color = if (j == 0) t.ink50 else t.ink200,
                            size = 15.sp,
                            weight = if (j == 0) FontWeight.SemiBold else FontWeight.Normal,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FigureSlide(s: ChapterSlide.Figure, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface2.copy(alpha = if (t.isDark) 0.45f else 0.7f))
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (s.kind) {
                FigureKind.PARTICLE_STATES -> ParticleStatesFigure(accent)
                FigureKind.PARTICLE_GROUPING -> ParticleGroupingFigure(accent)
                FigureKind.FORCE_ARROWS -> ForceArrowsFigure(accent, s.labels)
                FigureKind.LIGHT_REFLECTION -> LightReflectionFigure(accent)
                FigureKind.MOON_PHASES -> MoonPhasesFigure(accent)
                FigureKind.GOLDILOCKS -> GoldilocksFigure(accent, s.labels)
                FigureKind.ENERGY_PYRAMID -> EnergyPyramidFigure(accent, s.labels)
            }
        }
        if (s.caption.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            LLText(s.caption, color = t.ink400, size = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ParticleStatesFigure(accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        ParticleCell("Solid", "Packed, fixed", accent, Modifier.weight(1f)) { r ->
            val n = 4
            for (i in 0 until n) for (j in 0 until n) {
                drawCircle(accent, r, Offset(size.width * (i + 0.5f) / n, size.height * (j + 0.5f) / n))
            }
        }
        ParticleCell("Liquid", "Touching, mobile", accent, Modifier.weight(1f)) { r ->
            listOf(
                0.22f to 0.25f, 0.5f to 0.2f, 0.78f to 0.3f,
                0.3f to 0.5f, 0.62f to 0.52f, 0.85f to 0.6f,
                0.2f to 0.74f, 0.48f to 0.78f, 0.72f to 0.82f,
            ).forEach { (fx, fy) -> drawCircle(accent, r, Offset(size.width * fx, size.height * fy)) }
        }
        ParticleCell("Gas", "Far apart, free", accent, Modifier.weight(1f)) { r ->
            listOf(0.2f to 0.25f, 0.7f to 0.18f, 0.45f to 0.5f, 0.82f to 0.62f, 0.25f to 0.8f)
                .forEach { (fx, fy) -> drawCircle(accent, r, Offset(size.width * fx, size.height * fy)) }
        }
    }
}

@Composable
private fun ParticleGroupingFigure(accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        ParticleCell("Element", "One kind of atom", accent, Modifier.weight(1f)) { r ->
            listOf(0.25f to 0.3f, 0.6f to 0.25f, 0.8f to 0.55f, 0.3f to 0.62f, 0.55f to 0.72f)
                .forEach { (fx, fy) -> drawCircle(accent, r, Offset(size.width * fx, size.height * fy)) }
        }
        ParticleCell("Compound", "Atoms bonded", accent, Modifier.weight(1f)) { r ->
            listOf(0.26f to 0.3f, 0.58f to 0.46f, 0.32f to 0.7f).forEach { (fx, fy) ->
                val c1 = Offset(size.width * fx, size.height * fy)
                val c2 = Offset(c1.x + r * 2.2f, c1.y)
                drawLine(accent.copy(alpha = 0.6f), c1, c2, strokeWidth = r * 0.6f)
                drawCircle(accent, r, c1)
                drawCircle(SubjectPhysics, r, c2)
            }
        }
        ParticleCell("Mixture", "Different, unbonded", accent, Modifier.weight(1f)) { r ->
            drawCircle(accent, r, Offset(size.width * 0.25f, size.height * 0.3f))
            drawCircle(SubjectPhysics, r, Offset(size.width * 0.62f, size.height * 0.25f))
            drawCircle(SubjectChemistry, r * 0.85f, Offset(size.width * 0.8f, size.height * 0.55f))
            drawCircle(accent, r, Offset(size.width * 0.32f, size.height * 0.68f))
            drawCircle(SubjectPhysics, r * 0.85f, Offset(size.width * 0.6f, size.height * 0.74f))
        }
    }
}

@Composable
private fun ParticleCell(
    name: String,
    sub: String,
    accent: Color,
    modifier: Modifier,
    draw: DrawScope.(Float) -> Unit,
) {
    val t = LL.tokens
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(t.bg.copy(alpha = 0.5f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        ) {
            Canvas(Modifier.fillMaxSize().padding(14.dp)) { draw(7.dp.toPx()) }
        }
        Spacer(Modifier.height(12.dp))
        LLText(name, color = accent, size = 17.sp, weight = FontWeight.Bold)
        LLText(sub, color = t.ink400, size = 13.sp)
    }
}

@Composable
private fun ForceArrowsFigure(accent: Color, labels: List<String>) {
    val t = LL.tokens
    val applied = labels.getOrElse(0) { "Applied force" }
    val friction = labels.getOrElse(1) { "Friction" }
    val weight = labels.getOrElse(2) { "Weight" }
    val support = labels.getOrElse(3) { "Support" }
    Box(Modifier.fillMaxWidth().height(220.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.48f
            val bw = (110.dp.toPx()).coerceAtMost(w * 0.22f)
            val bh = bw * 0.78f
            val groundY = cy + bh / 2
            drawLine(t.ink400, Offset(w * 0.12f, groundY), Offset(w * 0.88f, groundY), strokeWidth = 2.dp.toPx())
            val left = cx - bw / 2
            val tl = Offset(left, cy - bh / 2)
            val box = Size(bw, bh)
            val radius = CornerRadius(8.dp.toPx())
            drawRoundRect(accent.copy(alpha = 0.22f), tl, box, radius)
            drawRoundRect(accent, tl, box, radius, style = Stroke(width = 2.dp.toPx()))
            val sw = 3.dp.toPx()
            drawArrow(Offset(left - w * 0.18f, cy), Offset(left - 4.dp.toPx(), cy), accent, sw)
            drawArrow(Offset(cx, groundY - 6.dp.toPx()), Offset(cx - w * 0.16f, groundY - 6.dp.toPx()), t.rose700, sw)
            drawArrow(Offset(cx, cy), Offset(cx, groundY + h * 0.18f), t.ink200, sw)
            drawArrow(Offset(cx, cy - bh / 2), Offset(cx, cy - bh / 2 - h * 0.18f), SubjectPhysics, sw)
        }
        LLText(applied, color = accent, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterStart))
        LLText(support, color = SubjectPhysics, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopCenter))
        LLText(weight, color = t.ink200, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.BottomCenter))
        LLText(friction, color = t.rose700, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.BottomStart))
    }
}

@Composable
private fun LightReflectionFigure(accent: Color) {
    val t = LL.tokens
    Box(Modifier.fillMaxWidth().height(230.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val mirrorY = h * 0.8f
            val p = Offset(w * 0.5f, mirrorY)
            drawLine(t.ink200, Offset(w * 0.18f, mirrorY), Offset(w * 0.82f, mirrorY), strokeWidth = 3.dp.toPx())
            var x = w * 0.22f
            while (x < w * 0.82f) {
                drawLine(t.ink400, Offset(x, mirrorY), Offset(x - 10.dp.toPx(), mirrorY + 12.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                x += 16.dp.toPx()
            }
            drawLine(
                t.ink400, p, Offset(p.x, h * 0.08f), strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )
            drawArrow(Offset(w * 0.18f, h * 0.12f), p, accent, 3.dp.toPx())
            drawArrow(p, Offset(w * 0.82f, h * 0.12f), SubjectPhysics, 3.dp.toPx())
        }
        LLText("Incident ray", color = accent, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopStart))
        LLText("Reflected ray", color = SubjectPhysics, size = 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopEnd))
        LLText("Normal", color = t.ink400, size = 12.sp, modifier = Modifier.align(Alignment.TopCenter))
        LLText("Mirror", color = t.ink200, size = 12.sp, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun MoonPhasesFigure(accent: Color) {
    val t = LL.tokens
    val names = listOf(
        "New", "Waxing\ncrescent", "First\nquarter", "Waxing\ngibbous",
        "Full", "Waning\ngibbous", "Last\nquarter", "Waning\ncrescent",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        names.forEachIndexed { i, nm ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.size(56.dp)) {
                    val r = size.minDimension / 2 - 2.dp.toPx()
                    drawMoonPhase(center, r, i / 8f, lit = accent, dark = t.surface3)
                    drawCircle(t.ink400, r, center, style = Stroke(width = 1.dp.toPx()))
                }
                Spacer(Modifier.height(8.dp))
                LLText(nm, color = t.ink400, size = 11.sp, align = TextAlign.Center, lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun GoldilocksFigure(accent: Color, labels: List<String>) {
    val t = LL.tokens
    val l0 = labels.getOrElse(0) { "Too hot" }
    val l1 = labels.getOrElse(1) { "Just right" }
    val l2 = labels.getOrElse(2) { "Too cold" }
    val sun = Color(0xFFFFB020)
    Box(Modifier.fillMaxWidth().height(220.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cy = h * 0.46f
            val sc = Offset(w * 0.06f, cy)
            drawCircle(sun.copy(alpha = 0.25f), 34.dp.toPx(), sc)
            drawCircle(sun, 22.dp.toPx(), sc)
            drawLine(t.line, Offset(w * 0.12f, cy), Offset(w * 0.98f, cy), strokeWidth = 1.5.dp.toPx())
            val zx = w * 0.5f
            val zhw = w * 0.13f
            drawRect(SubjectBiology.copy(alpha = 0.18f), topLeft = Offset(zx - zhw, h * 0.12f), size = Size(zhw * 2, h * 0.66f))
            drawCircle(t.rose700, 9.dp.toPx(), Offset(w * 0.17f, cy))
            drawCircle(SubjectPhysics, 11.dp.toPx(), Offset(zx, cy))
            drawCircle(t.ink400, 9.dp.toPx(), Offset(w * 0.82f, cy))
        }
        LLText("Habitable zone", color = SubjectBiology, size = 12.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopCenter))
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LLText(l0, color = t.rose700, size = 13.sp, weight = FontWeight.SemiBold)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LLText(l1, color = SubjectPhysics, size = 13.sp, weight = FontWeight.SemiBold)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LLText(l2, color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EnergyPyramidFigure(accent: Color, labels: List<String>) {
    val t = LL.tokens
    val tiers = if (labels.size >= 4) labels.take(4)
    else listOf("Top predators", "Carnivores", "Herbivores", "Producers (plants)")
    val colors = listOf(t.rose700, SubjectChemistry, accent, SubjectBiology)
    Column(Modifier.fillMaxWidth().height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tiers.forEachIndexed { i, label ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f + i * 0.2f)
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors[i].copy(alpha = if (t.isDark) 0.3f else 0.22f))
                    .border(1.dp, colors[i].copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                LLText(label, color = t.ink50, size = 15.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
            }
        }
    }
}

private fun DrawScope.drawArrow(start: Offset, end: Offset, color: Color, sw: Float) {
    drawLine(color, start, end, strokeWidth = sw, cap = StrokeCap.Round)
    val angle = atan2(end.y - start.y, end.x - start.x)
    val headLen = 11.dp.toPx()
    val spread = Math.toRadians(150.0).toFloat()
    drawLine(color, end, Offset(end.x + headLen * cos(angle + spread), end.y + headLen * sin(angle + spread)), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, end, Offset(end.x + headLen * cos(angle - spread), end.y + headLen * sin(angle - spread)), strokeWidth = sw, cap = StrokeCap.Round)
}

private fun DrawScope.drawMoonPhase(c: Offset, r: Float, phase: Float, lit: Color, dark: Color) {
    drawCircle(dark, r, c)
    val startAngle = if (phase < 0.5f) -90f else 90f
    drawArc(lit, startAngle, 180f, useCenter = true, topLeft = Offset(c.x - r, c.y - r), size = Size(2 * r, 2 * r))
    val x = cos(phase * 2f * PI.toFloat())
    val ew = r * abs(x)
    drawOval(if (x < 0f) lit else dark, topLeft = Offset(c.x - ew, c.y - r), size = Size(2 * ew, 2 * r))
}

@Composable
private fun InteractiveSlide(s: ChapterSlide.Interactive, accent: Color) {
    val t = LL.tokens
    Column {
        Kicker(s.kicker, accent)
        Spacer(Modifier.height(16.dp))
        LLText(s.title, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        when (s.kind) {
            InteractiveKind.KINETIC_MATTER -> KineticMatterFigure(accent)
            InteractiveKind.MOON_ORBIT -> MoonOrbitFigure(accent)
            InteractiveKind.FORCE_BLOCK -> ForceBlockFigure(accent)
            InteractiveKind.DENSITY_FLOAT -> DensityFloatFigure(accent)
        }
        if (s.caption.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            LLText(s.caption, color = t.ink400, size = 15.sp, lineHeight = 22.sp)
        }
    }
}

/** Shared bordered frame for an interactive model's canvas, matching FigureSlide. */
@Composable
private fun ModelBox(height: Dp = 240.dp, content: @Composable BoxScope.() -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface2.copy(alpha = if (t.isDark) 0.45f else 0.7f))
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun KineticMatterFigure(accent: Color) {
    val t = LL.tokens
    var temp by remember { mutableStateOf(0.15f) }
    var clock by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now -> clock = (now - start) / 1_000_000_000f }
        }
    }
    val state = when {
        temp < 0.34f -> "Solid"
        temp < 0.67f -> "Liquid"
        else -> "Gas"
    }
    Column {
        ModelBox(height = 230.dp) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val r = 8.dp.toPx()
                val spread = 0.42f + temp * 0.5f
                val amp = size.minDimension * (0.012f + temp * 0.13f)
                val speed = 1.6f + temp * 9f
                val n = 4
                for (i in 0 until n) for (j in 0 until n) {
                    val homeX = cx + ((i - 1.5f) / 1.5f) * (w / 2f - r * 2) * spread
                    val homeY = cy + ((j - 1.5f) / 1.5f) * (h / 2f - r * 2) * spread
                    val idx = i * n + j
                    val px = homeX + amp * sin(clock * speed + idx * 1.7f)
                    val py = homeY + amp * cos(clock * speed * 1.13f + idx * 2.3f)
                    drawCircle(accent, r, Offset(px.coerceIn(r, w - r), py.coerceIn(r, h - r)))
                }
            }
            LLText(
                state, color = accent, size = 16.sp, weight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        Spacer(Modifier.height(18.dp))
        LLSlider(
            label = "Temperature",
            value = temp,
            onValueChange = { temp = it },
            min = 0f, max = 1f,
            valueFormat = { state },
        )
    }
}

@Composable
private fun MoonOrbitFigure(accent: Color) {
    val t = LL.tokens
    var month by remember { mutableStateOf(0.5f) }
    val sun = Color(0xFFFFB020)
    Column {
        ModelBox(height = 240.dp) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val earth = Offset(w * 0.55f, h * 0.5f)
                        val orbitR = size.minDimension * 0.32f
                        drawCircle(t.line, orbitR, earth, style = Stroke(width = 1.dp.toPx()))
                        drawCircle(SubjectPhysics, 10.dp.toPx(), earth)
                        val a = PI.toFloat() - 2f * PI.toFloat() * month
                        val moon = Offset(earth.x + orbitR * cos(a), earth.y - orbitR * sin(a))
                        val mr = 7.dp.toPx()
                        drawCircle(t.surface3, mr, moon)
                        drawArc(
                            accent, 90f, 180f, useCenter = true,
                            topLeft = Offset(moon.x - mr, moon.y - mr), size = Size(2 * mr, 2 * mr),
                        )
                    }
                    LLText(
                        "☀ Sunlight →", color = sun, size = 11.sp, weight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.size(92.dp)) {
                            val r = size.minDimension / 2 - 2.dp.toPx()
                            drawMoonPhase(center, r, month % 1f, lit = accent, dark = t.surface3)
                            drawCircle(t.ink400, r, center, style = Stroke(width = 1.dp.toPx()))
                        }
                        Spacer(Modifier.height(10.dp))
                        LLText(moonPhaseName(month), color = t.ink200, size = 13.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        LLSlider(
            label = "Lunar month",
            value = month,
            onValueChange = { month = it },
            min = 0f, max = 0.999f,
            valueFormat = { "Day ${(it * 29.5f).roundToInt()}" },
        )
    }
}

@Composable
private fun ForceBlockFigure(accent: Color) {
    val t = LL.tokens
    var force by remember { mutableStateOf(0.3f) }
    var rough by remember { mutableStateOf(false) }
    val threshold = if (rough) 0.6f else 0.28f
    val moves = force > threshold
    val shift by animateFloatAsState(if (moves) 0.28f else 0f, label = "blockShift")
    Column {
        ModelBox(height = 210.dp) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val groundY = h * 0.7f
                drawLine(t.ink400, Offset(w * 0.05f, groundY), Offset(w * 0.95f, groundY), strokeWidth = 2.dp.toPx())
                if (rough) {
                    var x = w * 0.07f
                    while (x < w * 0.93f) {
                        drawLine(t.ink500, Offset(x, groundY), Offset(x + 8.dp.toPx(), groundY + 9.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                        x += 14.dp.toPx()
                    }
                }
                val bw = 80.dp.toPx()
                val bh = bw * 0.7f
                val left = w * 0.22f + shift * w * 0.5f
                val cy = groundY - bh / 2
                val radius = CornerRadius(8.dp.toPx())
                drawRoundRect(accent.copy(alpha = 0.22f), Offset(left, groundY - bh), Size(bw, bh), radius)
                drawRoundRect(accent, Offset(left, groundY - bh), Size(bw, bh), radius, style = Stroke(2.dp.toPx()))
                val sw = 3.dp.toPx()
                val fLen = 22.dp.toPx() + force * 70.dp.toPx()
                drawArrow(Offset(left - fLen, cy), Offset(left - 6.dp.toPx(), cy), accent, sw)
                val frLen = 16.dp.toPx() + threshold * 55.dp.toPx()
                drawArrow(Offset(left + bw * 0.5f, groundY - 5.dp.toPx()), Offset(left + bw * 0.5f - frLen, groundY - 5.dp.toPx()), t.rose700, sw)
            }
            LLText("Push", color = accent, size = 12.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterStart))
            LLText("Friction", color = t.rose700, size = 12.sp, weight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.BottomStart))
            LLText(
                if (moves) "Moves →" else "Stays still",
                color = if (moves) accent else t.ink400, size = 15.sp, weight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                LLSlider(
                    label = "Applied force",
                    value = force,
                    onValueChange = { force = it },
                    min = 0f, max = 1f,
                    valueFormat = { "%.0f%%".format(it * 100) },
                )
            }
            Spacer(Modifier.width(16.dp))
            SurfaceToggle(rough, { rough = it }, accent)
        }
    }
}

@Composable
private fun SurfaceToggle(rough: Boolean, onChange: (Boolean) -> Unit, accent: Color) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(999.dp))
            .padding(3.dp),
    ) {
        listOf(false to "Smooth", true to "Rough").forEach { (v, lbl) ->
            val sel = v == rough
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (sel) accent.copy(alpha = 0.18f) else Color.Transparent)
                    .clickable { onChange(v) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                LLText(lbl, color = if (sel) accent else t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DensityFloatFigure(accent: Color) {
    val t = LL.tokens
    var density by remember { mutableStateOf(0.6f) }
    val water = 1.0f
    val state = when {
        density < water - 0.03f -> "Floats"
        density > water + 0.03f -> "Sinks"
        else -> "Neutral"
    }
    val targetSub = if (density < water) (density / water).coerceIn(0.12f, 0.95f) else 1f
    val sub by animateFloatAsState(targetSub, label = "sub")
    val sink by animateFloatAsState(if (density > water + 0.03f) 1f else 0f, label = "sink")
    val waterBlue = Color(0xFF3B82F6)
    Column {
        ModelBox(height = 240.dp) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val bx = w * 0.3f
                val bw = w * 0.4f
                val top = h * 0.08f
                val bot = h * 0.92f
                val waterTop = h * 0.4f
                drawLine(t.ink400, Offset(bx, top), Offset(bx, bot), strokeWidth = 2.dp.toPx())
                drawLine(t.ink400, Offset(bx + bw, top), Offset(bx + bw, bot), strokeWidth = 2.dp.toPx())
                drawLine(t.ink400, Offset(bx, bot), Offset(bx + bw, bot), strokeWidth = 2.dp.toPx())
                drawRect(waterBlue.copy(alpha = 0.25f), topLeft = Offset(bx, waterTop), size = Size(bw, bot - waterTop))
                drawLine(waterBlue, Offset(bx, waterTop), Offset(bx + bw, waterTop), strokeWidth = 1.5.dp.toPx())
                val os = w * 0.13f
                val ox = bx + bw / 2 - os / 2
                val floatTopY = waterTop - os * (1f - sub)
                val sinkTopY = bot - os - 4.dp.toPx()
                val oy = floatTopY + sink * (sinkTopY - floatTopY)
                drawRoundRect(accent.copy(alpha = 0.85f), Offset(ox, oy), Size(os, os), CornerRadius(6.dp.toPx()))
            }
            LLText(
                state, color = if (state == "Sinks") t.rose700 else accent, size = 16.sp, weight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            LLText("Water = 1.0 g/cm³", color = t.ink400, size = 11.sp, modifier = Modifier.align(Alignment.BottomStart))
        }
        Spacer(Modifier.height(18.dp))
        LLSlider(
            label = "Object density",
            value = density,
            onValueChange = { density = it },
            min = 0.2f, max = 2.0f,
            unit = "g/cm³",
            valueFormat = { "%.1f".format(it) },
        )
    }
}

private fun moonPhaseName(p: Float): String {
    val names = listOf(
        "New Moon", "Waxing crescent", "First quarter", "Waxing gibbous",
        "Full Moon", "Waning gibbous", "Last quarter", "Waning crescent",
    )
    return names[((p % 1f) * 8f).roundToInt() % 8]
}

private fun iconFor(icon: SlideIcon): ImageVector = when (icon) {
    SlideIcon.QUESTION -> Icons.Filled.HelpOutline
    SlideIcon.SEARCH -> Icons.Filled.Search
    SlideIcon.LOOP -> Icons.Filled.Autorenew
    SlideIcon.BALANCE -> Icons.Filled.Balance
    SlideIcon.NOTE -> Icons.Filled.EditNote
    SlideIcon.SCIENCE -> Icons.Filled.Science
    SlideIcon.KITCHEN -> Icons.Filled.Restaurant
    SlideIcon.SPARK -> Icons.Filled.AutoAwesome
    SlideIcon.MICROBE -> Icons.Filled.Biotech
    SlideIcon.VIRUS -> Icons.Filled.Coronavirus
    SlideIcon.HEART -> Icons.Filled.MonitorHeart
    SlideIcon.BOLT -> Icons.Filled.Bolt
    SlideIcon.WIND -> Icons.Filled.Air
    SlideIcon.PARTICLE -> Icons.Filled.Grain
    SlideIcon.BUBBLE -> Icons.Filled.BubbleChart
    SlideIcon.LIGHT -> Icons.Filled.Lightbulb
    SlideIcon.CLOCK -> Icons.Filled.Schedule
    SlideIcon.LEAF -> Icons.Filled.Eco
    SlideIcon.GLOBE -> Icons.Filled.Public
    SlideIcon.WAVE -> Icons.Filled.Waves
    SlideIcon.HEAT -> Icons.Filled.Thermostat
    SlideIcon.SUN -> Icons.Filled.WbSunny
    SlideIcon.FIRE -> Icons.Filled.LocalFireDepartment
    SlideIcon.VACCINE -> Icons.Filled.Vaccines
    SlideIcon.EYE -> Icons.Filled.Visibility
    SlideIcon.MEASURE -> Icons.Filled.Straighten
    SlideIcon.STORM -> Icons.Filled.Storm
    SlideIcon.FORCE -> Icons.Filled.FitnessCenter
}

private fun slideBold(text: String, boldColor: Color): AnnotatedString =
    buildAnnotatedString {
        text.split("**").forEachIndexed { i, part ->
            if (i % 2 == 1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor))
                append(part); pop()
            } else append(part)
        }
    }
