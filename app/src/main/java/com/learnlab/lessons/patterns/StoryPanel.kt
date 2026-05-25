package com.learnlab.lessons.patterns

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Character
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius

/* ────────────── Single speaker panel ────────────── */

/**
 * Big single-speaker panel. Avatar dominates one side, bubble on the other.
 * Background is tinted with the character's accent color so consecutive
 * speakers feel visually distinct.
 */
@Composable
fun StoryPanelSingle(
    speaker: Character?,
    speakerIdFallback: String,
    body: String,
    sceneCaption: String? = null,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val accent = parseAccent(speaker?.accentColor) ?: t.accent500
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.linearGradient(
                colors = listOf(
                    accent.copy(alpha = 0.08f),
                    t.bg,
                ),
            ),
        ).padding(56.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            BigAvatar(speaker, speakerIdFallback, accent)
            Column(modifier = Modifier.weight(1f)) {
                if (sceneCaption != null) {
                    LLText("SCENE", color = t.ink500, size = 11.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    LLText(sceneCaption, color = t.ink400, size = 16.sp,
                        lineHeight = 24.sp, weight = FontWeight.Medium)
                    Spacer(Modifier.height(20.dp))
                }
                LLText(
                    (speaker?.displayName ?: speakerIdFallback).uppercase(),
                    color = accent, size = 16.sp,
                    weight = FontWeight.ExtraBold, letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(12.dp))
                ComicBubble(body = body, accent = accent, big = true)
            }
        }
    }
}

/* ────────────── Conversation (multi-speaker) ────────────── */

data class StoryLine(val speaker: Character?, val idFallback: String, val body: String)

/**
 * Multi-speaker conversation arranged as a comic strip. 2 lines: side by side.
 * 3+ lines: zig-zag (left/right/left).
 */
@Composable
fun StoryPanelConversation(
    lines: List<StoryLine>,
    sceneCaption: String? = null,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Box(modifier = modifier.fillMaxSize().padding(40.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (sceneCaption != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.height(2.dp).width(28.dp)
                        .clip(RoundedCornerShape(Radius.pill)).background(t.accent500))
                    Spacer(Modifier.width(10.dp))
                    LLText("SCENE · $sceneCaption",
                        color = t.accent500, size = 12.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(20.dp))
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                lines.forEachIndexed { i, line ->
                    val alignRight = i % 2 == 1
                    ConversationRow(line, alignRight)
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(line: StoryLine, alignRight: Boolean) {
    val t = LL.tokens
    val accent = parseAccent(line.speaker?.accentColor) ?: t.accent500
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!alignRight) {
            MediumAvatar(line.speaker, line.idFallback, accent)
            Spacer(Modifier.width(20.dp))
        }
        Column(
            modifier = Modifier.widthIn(max = 780.dp),
            horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start,
        ) {
            LLText(
                (line.speaker?.displayName ?: line.idFallback).uppercase(),
                color = accent, size = 13.sp,
                weight = FontWeight.ExtraBold, letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(6.dp))
            ComicBubble(body = line.body, accent = accent, big = false, pointRight = alignRight)
        }
        if (alignRight) {
            Spacer(Modifier.width(20.dp))
            MediumAvatar(line.speaker, line.idFallback, accent)
        }
    }
}

/* ────────────── Sub-elements ────────────── */

@Composable
private fun BigAvatar(ch: Character?, fallback: String, accent: Color) {
    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        accent.copy(alpha = 0.30f),
                        accent.copy(alpha = 0.12f),
                    )
                ),
            )
            .border(4.dp, accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LLText(
            (ch?.displayName ?: fallback).firstOrNull()?.uppercase() ?: "?",
            color = accent, size = 72.sp,
            weight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun MediumAvatar(ch: Character?, fallback: String, accent: Color) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.22f))
            .border(3.dp, accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LLText(
            (ch?.displayName ?: fallback).firstOrNull()?.uppercase() ?: "?",
            color = accent, size = 32.sp,
            weight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ComicBubble(body: String, accent: Color, big: Boolean, pointRight: Boolean = false) {
    val t = LL.tokens
    val shape = if (pointRight) {
        RoundedCornerShape(
            topStart = Radius.lg, topEnd = 4.dp,
            bottomEnd = Radius.lg, bottomStart = Radius.lg,
        )
    } else {
        RoundedCornerShape(
            topStart = 4.dp, topEnd = Radius.lg,
            bottomEnd = Radius.lg, bottomStart = Radius.lg,
        )
    }
    Box(
        modifier = Modifier
            .clip(shape)
            .background(t.surface)
            .border(2.dp, accent.copy(alpha = 0.4f), shape)
            .padding(if (big) 28.dp else 22.dp),
    ) {
        androidx.compose.material3.Text(
            text = "“$body”",
            color = t.ink50,
            fontSize = if (big) 28.sp else 20.sp,
            lineHeight = if (big) 40.sp else 30.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun parseAccent(hex: String?): Color? {
    if (hex == null) return null
    return try {
        val s = hex.removePrefix("#")
        val v = s.toLong(16)
        if (s.length == 6) Color((0xFF000000L or v).toInt()) else Color(v.toInt())
    } catch (_: Throwable) { null }
}
