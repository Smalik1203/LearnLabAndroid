package com.learnlab.experiments.ch06kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText

/** Small uppercase pill. Defaults to the accent tint; pass [tint] for category colours. */
@Composable
fun Badge(text: String, modifier: Modifier = Modifier, tint: Color? = null) {
    val t = LL.tokens
    val fg = tint ?: t.accent700
    val bg = if (tint != null) tint.copy(alpha = 0.15f) else t.accent50
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        LLText(text.uppercase(), color = fg, size = 10.sp,
            weight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

/**
 * The dark "activity card" shown in the left rail of Template B: an activity code badge, a title,
 * an optional description, and a list of emoji-prefixed tags (e.g. "🔍  Drag the slider").
 */
@Composable
fun ActivityCard(
    code: String,
    title: String,
    tags: List<String>,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.bgDeep)
            .border(1.dp, t.lineStrong, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Badge(code)
        LLText(title, color = t.ink50, size = 16.sp, weight = FontWeight.Bold, lineHeight = 21.sp)
        if (description != null) {
            LLText(description, color = t.ink200, size = 12.sp, lineHeight = 17.sp)
        }
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            tags.forEach { tag ->
                LLText(tag, color = t.ink400, size = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

/** Tinted quotation callout with optional attribution. */
@Composable
fun QuoteCard(text: String, modifier: Modifier = Modifier, attribution: String? = null) {
    val t = LL.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.accent50)
            .padding(start = 0.dp),
    ) {
        Box(Modifier.width(4.dp).height(if (attribution != null) 76.dp else 60.dp).background(t.accent500))
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LLText("“$text”", color = t.ink200, size = 13.sp, lineHeight = 19.sp)
            if (attribution != null) {
                LLText("— $attribution", color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

/** Neutral labelled context card (surface-2 fill). */
@Composable
fun ContextCard(label: String, body: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(label.uppercase(), color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
        LLText(body, color = t.ink200, size = 13.sp, lineHeight = 19.sp)
    }
}

/** Titled info card on the surface fill — used for "what you'll discover" style panels. */
@Composable
fun InfoCard(title: String, body: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(title, color = t.ink50, size = 14.sp, weight = FontWeight.Bold)
        LLText(body, color = t.ink400, size = 12.sp, lineHeight = 17.sp)
    }
}

enum class NoteTone { Info, Warn, Alert }

/** A coloured pullout box. Info = accent (teal), Warn = amber, Alert = rose. */
@Composable
fun NoteBox(text: String, modifier: Modifier = Modifier, tone: NoteTone = NoteTone.Info, label: String? = null) {
    val t = LL.tokens
    val (bg, fg) = when (tone) {
        NoteTone.Info -> t.accent50 to t.accent700
        NoteTone.Warn -> t.amber50 to t.amber700
        NoteTone.Alert -> t.rose50 to t.rose700
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (label != null) {
            LLText(label.uppercase(), color = fg, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp)
        }
        LLText(text, color = t.ink200, size = 12.sp, lineHeight = 17.sp)
    }
}

/**
 * A predict-then-reveal table cell: starts as a tinted blank ("tap to reveal") and shows [answer]
 * on tap. Used by the comparison/observation tables — keeps the kiosk keyboard-free while still
 * asking students to think before they peek.
 */
@Composable
fun RevealCell(answer: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    var shown by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (shown) t.surface else t.surface2)
            .border(1.dp, if (shown) t.accent500.copy(alpha = 0.55f) else t.line, RoundedCornerShape(8.dp))
            .clickable { shown = !shown }
            .padding(horizontal = 9.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(
            if (shown) answer else "tap to reveal",
            color = if (shown) t.ink50 else t.ink500,
            size = 11.sp, lineHeight = 15.sp,
            weight = if (shown) FontWeight.Medium else FontWeight.Normal,
            align = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** A tap-to-expand question/answer card for analysis questions. */
@Composable
fun RevealCard(question: String, answer: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    var shown by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .clickable { shown = !shown }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            LLText(question, color = t.ink50, size = 13.sp, weight = FontWeight.SemiBold,
                lineHeight = 18.sp, modifier = Modifier.weight(1f).padding(end = 8.dp))
            LLText(if (shown) "−" else "+", color = t.accent700, size = 18.sp, weight = FontWeight.Bold)
        }
        if (shown) {
            LLText(answer, color = t.ink200, size = 12.sp, lineHeight = 18.sp)
        }
    }
}
