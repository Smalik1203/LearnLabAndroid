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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.ChapterSlide
import com.learnlab.content.SlideIcon
import com.learnlab.design.Inter
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.SubjectBiology
import com.learnlab.design.SubjectChemistry
import com.learnlab.design.SubjectMath
import com.learnlab.design.SubjectPhysics
import com.learnlab.design.gradHeadline

/** Renders one generated chapter slide. [index] drives the cycling accent colour. */
@Composable
fun SlideView(slide: ChapterSlide, index: Int) {
    val t = LL.tokens
    val accents = listOf(SubjectChemistry, SubjectPhysics, SubjectMath, SubjectBiology, t.accent500)
    val accent = accents[index % accents.size]

    val bg = Brush.linearGradient(
        0.0f to accent.copy(alpha = if (t.isDark) 0.16f else 0.09f).compositeOver(t.bg),
        0.55f to t.bg,
        1.0f to t.bg,
        start = Offset.Zero,
        end = Offset.Infinite,
    )

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        // Large translucent slide number — a unifying decorative flourish.
        LLText(
            "%02d".format(index + 1),
            color = accent.copy(alpha = 0.12f),
            size = 150.sp,
            weight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 36.dp, top = 8.dp),
        )

        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 64.dp, vertical = 44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth()) {
                when (slide) {
                    is ChapterSlide.Title -> TitleSlide(slide, accent)
                    is ChapterSlide.Concept -> ConceptSlide(slide, accent)
                    is ChapterSlide.Steps -> StepsSlide(slide, accent)
                    is ChapterSlide.Split -> SplitSlide(slide, accent)
                    is ChapterSlide.Chips -> ChipsSlide(slide, accent)
                    is ChapterSlide.Closing -> ClosingSlide(slide, accent)
                }
            }
        }
    }
}

@Composable
private fun Kicker(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        LLText(text, color = accent, size = 12.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun GradientTitle(text: String, size: Int, align: TextAlign? = null) {
    Text(
        text = text,
        style = TextStyle(brush = gradHeadline()),
        fontFamily = Inter,
        fontSize = size.sp,
        lineHeight = (size * 1.1f).sp,
        fontWeight = FontWeight.Bold,
        textAlign = align,
        modifier = Modifier.fillMaxWidth(),
    )
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
                            append(rest)
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
        LLText(body, color = t.ink200, size = 16.sp, lineHeight = 24.sp)
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

private fun iconFor(icon: SlideIcon): ImageVector = when (icon) {
    SlideIcon.QUESTION -> Icons.Filled.HelpOutline
    SlideIcon.SEARCH -> Icons.Filled.Search
    SlideIcon.LOOP -> Icons.Filled.Autorenew
    SlideIcon.BALANCE -> Icons.Filled.Balance
    SlideIcon.NOTE -> Icons.Filled.EditNote
    SlideIcon.SCIENCE -> Icons.Filled.Science
    SlideIcon.KITCHEN -> Icons.Filled.Restaurant
    SlideIcon.SPARK -> Icons.Filled.AutoAwesome
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
