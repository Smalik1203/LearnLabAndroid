package com.learnlab.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer as LayoutSpacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.LessonBlock
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.lessons.figures.LessonFigureRegistry

/* ────────────────────────── Heading ───────────────────────────── */

@Composable
fun HeadingBlockView(block: LessonBlock.Heading) {
    val t = LL.tokens
    val p = lessonPalette()
    val titleSize = when (block.level) { 1 -> 26.sp; 2 -> 20.sp; else -> 17.sp }
    val barHeight = when (block.level) { 1 -> 8.dp; 2 -> 5.dp; else -> 3.dp }
    val barWidth = when (block.level) { 1 -> 48.dp; 2 -> 36.dp; else -> 28.dp }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 10.dp)) {
        // Decorative gradient bar (violet → emerald) — louder for §1, subtler for §2
        Box(
            modifier = Modifier
                .width(barWidth).height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(Brush.horizontalGradient(listOf(p.violet.accent, p.emerald.accent))),
        )
        LayoutSpacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (block.number != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(p.violet.surfaceStrong.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    LLText(
                        block.number, color = p.violet.accent,
                        size = (titleSize.value - 4).sp, weight = FontWeight.Bold,
                    )
                }
                LayoutSpacer(Modifier.width(10.dp))
            }
            LLText(
                block.text, color = t.ink50,
                size = titleSize, weight = FontWeight.Bold,
                lineHeight = (titleSize.value * 1.25f).sp,
            )
        }
    }
}

/* ────────────────────────── Paragraph ─────────────────────────── */

@Composable
fun ParagraphBlockView(block: LessonBlock.Paragraph) {
    val t = LL.tokens
    Text(
        text = renderInlineBold(block.text, accentColor = t.ink50),
        color = t.ink200,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}

/** `**bold**` markers render in bold. Anything else stays plain. */
private fun renderInlineBold(text: String, accentColor: Color) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        val start = text.indexOf("**", i)
        if (start < 0) { append(text.substring(i)); break }
        append(text.substring(i, start))
        val end = text.indexOf("**", start + 2)
        if (end < 0) { append(text.substring(start)); break }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accentColor)) {
            append(text.substring(start + 2, end))
        }
        i = end + 2
    }
}

/* ────────────────────────── Key term ─────────────────────────── */

@Composable
fun KeyTermBlockView(block: LessonBlock.KeyTerm) {
    val t = LL.tokens
    val p = lessonPalette()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(p.violet.surface)
            .border(1.dp, p.violet.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        // Vertical accent stripe
        Box(
            modifier = Modifier
                .width(4.dp).height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(p.violet.accent),
        )
        LayoutSpacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LLText(
                block.term, color = p.violet.accent,
                size = 14.sp, weight = FontWeight.Bold,
            )
            LayoutSpacer(Modifier.height(4.dp))
            Text(
                renderInlineBold(block.definition, accentColor = t.ink50),
                color = p.violet.ink,
                fontSize = 13.sp, lineHeight = 19.sp,
            )
        }
    }
}

/* ────────────────────────── Quote (Sanskrit scroll) ──────────── */

@Composable
fun QuoteBlockView(block: LessonBlock.Quote) {
    val t = LL.tokens
    val p = lessonPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(p.amber.surface, p.amber.surface.copy(alpha = 0.75f)),
                ),
            )
            .border(1.dp, p.amber.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LLText("“", color = p.amber.accent, size = 36.sp, weight = FontWeight.Bold)
        if (block.original != null) {
            LLText(
                block.original, color = p.amber.accent,
                size = 17.sp, weight = FontWeight.SemiBold, lineHeight = 26.sp,
                align = TextAlign.Center,
            )
            LayoutSpacer(Modifier.height(10.dp))
        }
        LLText(
            block.text, color = p.amber.ink,
            size = 15.sp, lineHeight = 24.sp,
            align = TextAlign.Center,
        )
        LayoutSpacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(p.amber.surfaceStrong.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            LLText("— ${block.attribution}",
                color = p.amber.accent, size = 11.sp, weight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp)
        }
    }
}

/* ────────────────────────── Character speech ─────────────────── */

@Composable
fun CharacterSayBlockView(block: LessonBlock.CharacterSay) {
    val t = LL.tokens
    val p = lessonPalette()
    val onRight = block.side == LessonBlock.CharacterSay.Side.RIGHT
    val hue = when (block.avatar) {
        LessonBlock.CharacterSay.Avatar.STUDENT_GIRL -> p.emerald
        LessonBlock.CharacterSay.Avatar.STUDENT_BOY -> p.violet
        LessonBlock.CharacterSay.Avatar.GRANDMA -> p.rose
        LessonBlock.CharacterSay.Avatar.TEACHER -> p.sky
        LessonBlock.CharacterSay.Avatar.SCIENTIST -> p.amber
    }
    val avatarEmoji = when (block.avatar) {
        LessonBlock.CharacterSay.Avatar.STUDENT_GIRL -> "👧"
        LessonBlock.CharacterSay.Avatar.STUDENT_BOY -> "👦"
        LessonBlock.CharacterSay.Avatar.GRANDMA -> "👵"
        LessonBlock.CharacterSay.Avatar.TEACHER -> "🧑‍🏫"
        LessonBlock.CharacterSay.Avatar.SCIENTIST -> "🧑‍🔬"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = if (onRight) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!onRight) Avatar(avatarEmoji, hue.surfaceStrong)
        if (!onRight) LayoutSpacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .shadow(3.dp, RoundedCornerShape(
                    topStart = if (onRight) 16.dp else 4.dp,
                    topEnd = if (onRight) 4.dp else 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp,
                ), clip = false)
                .clip(RoundedCornerShape(
                    topStart = if (onRight) 16.dp else 4.dp,
                    topEnd = if (onRight) 4.dp else 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp,
                ))
                .background(hue.surface)
                .border(1.dp, hue.border, RoundedCornerShape(
                    topStart = if (onRight) 16.dp else 4.dp,
                    topEnd = if (onRight) 4.dp else 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp,
                ))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            LLText(block.who.uppercase(), color = hue.accent, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp)
            LayoutSpacer(Modifier.height(4.dp))
            LLText(block.text, color = hue.ink, size = 14.sp, lineHeight = 20.sp)
        }
        if (onRight) LayoutSpacer(Modifier.width(10.dp))
        if (onRight) Avatar(avatarEmoji, hue.surfaceStrong)
    }
}

@Composable
private fun Avatar(emoji: String, ring: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(2.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(ring.copy(alpha = 0.35f), ring.copy(alpha = 0.1f)),
                ),
            )
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = 22.sp) }
}

/* ────────────────────────── Callout ───────────────────────────── */

@Composable
fun CalloutBlockView(block: LessonBlock.Callout) {
    val p = lessonPalette()
    val (hue, icon) = when (block.tone) {
        LessonBlock.Callout.Tone.INFO -> p.indigo to "ℹ️"
        LessonBlock.Callout.Tone.WARNING -> p.rose to "⚠️"
        LessonBlock.Callout.Tone.SUCCESS -> p.emerald to "✓"
        LessonBlock.Callout.Tone.FACT -> p.amber to "💡"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(hue.surface, hue.surface.copy(alpha = 0.8f)),
                ),
            )
            .border(1.dp, hue.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(hue.surfaceStrong, hue.surfaceStrong.copy(alpha = 0.7f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) { Text(icon, fontSize = 18.sp) }
        LayoutSpacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            LLText(block.title.uppercase(), color = hue.accent, size = 11.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            LayoutSpacer(Modifier.height(6.dp))
            LLText(block.body, color = hue.ink, size = 14.sp, lineHeight = 20.sp)
        }
    }
}

/* ────────────────────────── Question sticky ─────────────────── */

@Composable
fun QuestionBlockView(block: LessonBlock.Question) {
    val p = lessonPalette()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(p.violet.surface)
            .border(1.dp, p.violet.border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(p.violet.accent, p.violet.accent.copy(alpha = 0.7f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            LLText("?", color = Color.White, size = 22.sp, weight = FontWeight.Bold)
        }
        LayoutSpacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            LLText("THINK", color = p.violet.accent, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            LayoutSpacer(Modifier.height(4.dp))
            LLText(block.prompt, color = p.violet.ink, size = 15.sp,
                lineHeight = 22.sp, weight = FontWeight.Medium)
        }
    }
}

/* ────────────────────────── Figure ────────────────────────────── */

@Composable
fun FigureBlockView(block: LessonBlock.Figure) {
    val t = LL.tokens
    val renderer = LessonFigureRegistry[block.figureId]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(t.surface, t.surface2),
                ),
            )
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (renderer != null) {
            renderer()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(t.surface3),
                contentAlignment = Alignment.Center,
            ) { LLText("[Figure: ${block.figureId}]", color = t.ink500, size = 12.sp) }
        }
        LayoutSpacer(Modifier.height(12.dp))
        // Decorative divider
        Box(
            modifier = Modifier
                .width(40.dp).height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(t.line),
        )
        LayoutSpacer(Modifier.height(8.dp))
        LLText(block.caption, color = t.ink400, size = 12.sp,
            align = TextAlign.Center, lineHeight = 17.sp)
    }
}

/* ────────────────────────── Case study ──────────────────────── */

@Composable
fun CaseStudyBlockView(block: LessonBlock.CaseStudy) {
    val p = lessonPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(5.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(p.amber.surface, p.amber.surface.copy(alpha = 0.8f))),
            )
            .border(1.dp, p.amber.border, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(p.amber.accent, p.amber.accent.copy(alpha = 0.75f)),
                        ),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                LLText("CASE ${block.number}", color = Color.White, size = 11.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            }
            LayoutSpacer(Modifier.width(10.dp))
            LLText(block.title, color = p.amber.accent,
                size = 15.sp, weight = FontWeight.SemiBold)
        }
        LayoutSpacer(Modifier.height(10.dp))
        LLText(block.body, color = p.amber.ink, size = 14.sp, lineHeight = 21.sp)
    }
}
