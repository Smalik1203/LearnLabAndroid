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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.LearnLabFonts
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * A paragraph rendered like an editorial spread, not a wall of centred prose.
 *
 *  ┌──────────────────────────────────────────────┐
 *  │  • SECTION 2.1                       [glyph] │
 *  │                                              │
 *  │  ╔═╗                                         │
 *  │  ║W║hen you look closely, no two plants     │
 *  │  ╚═╝   seem exactly the same.               │
 *  │                                              │
 *  │  Stems are tall or short, soft or hard.     │
 *  │  Leaves have different shapes …             │
 *  └──────────────────────────────────────────────┘
 *
 * Strips {{token}} markers and shows them as inline keyword pills.
 */
@Composable
fun HeroParagraph(
    body: String,
    sectionNumber: String? = null,
    sectionTitle: String? = null,
    accent: Color = LL.tokens.accent500,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val (firstSentence, restOfBody) = splitFirstSentence(body)
    val keyTerms = extractKeyTerms(body)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp, vertical = 48.dp),
    ) {
        // Top label row
        if (sectionNumber != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(2.dp).width(28.dp)
                            .clip(RoundedCornerShape(Radius.pill))
                            .background(accent),
                    )
                    Spacer(Modifier.width(10.dp))
                    LLText(
                        "§ $sectionNumber" + (sectionTitle?.let { " · $it" } ?: ""),
                        color = accent, size = 12.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp,
                    )
                }
            }
        }

        // Body block, vertically centred
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1200.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // First sentence as a hero line
            HeroLine(text = stripTokens(firstSentence))
            if (restOfBody.isNotBlank()) {
                Spacer(Modifier.height(24.dp))
                LLText(
                    text = stripTokens(restOfBody),
                    color = t.ink200,
                    size = 22.sp,
                    lineHeight = 34.sp,
                )
            }
            // Inline key-terms strip (if any tokens like {{biodiversity}} were in body)
            if (keyTerms.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText("KEY", color = t.ink500, size = 11.sp,
                        weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    keyTerms.forEach { term -> KeywordPill(term, accent) }
                }
            }
        }
    }
}

@Composable
private fun HeroLine(text: String) {
    val t = LL.tokens
    if (text.isBlank()) return
    androidx.compose.material3.Text(
        text = text,
        color = t.ink50,
        fontSize = 32.sp,
        lineHeight = 46.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = LearnLabFonts.Display,
    )
}

@Composable
private fun KeywordPill(term: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(Radius.pill))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        LLText(term, color = accent, size = 14.sp, weight = FontWeight.Bold)
    }
}

/* ───────── helpers ───────── */

private fun splitFirstSentence(body: String): Pair<String, String> {
    val cleaned = body.trim()
    if (cleaned.isEmpty()) return "" to ""
    // First sentence ends at . ! ? ; followed by whitespace
    val endRegex = Regex("""[.!?;](\s|$)""")
    val match = endRegex.find(cleaned) ?: return cleaned to ""
    val cut = match.range.first + 1
    val first = cleaned.substring(0, cut).trim()
    val rest = cleaned.substring(cut).trim()
    return first to rest
}

/** Pulls tokens like `{{biodiversity}}` out, returning them in encounter order, deduped. */
private fun extractKeyTerms(body: String): List<String> {
    val rx = Regex("""\{\{([^}]+)\}\}""")
    return rx.findAll(body).map {
        it.groupValues[1].trim().split(" ").joinToString(" ") { w ->
            w.replaceFirstChar { c -> c.uppercase() }
        }
    }.distinct().toList()
}

private fun stripTokens(body: String): String =
    body.replace(Regex("""\{\{([^}]+)\}\}""")) { it.groupValues[1] }
