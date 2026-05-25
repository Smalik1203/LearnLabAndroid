package com.learnlab.lessons.patterns

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * Big centred term, glyph-as-hero (gently pulsing), then the definition.
 *
 * The glyph is taken from a small lookup keyed by term; falls back to "✦" if
 * no match. Authors can override in future once the JSON KeyTerm gets a glyph
 * field.
 */
@Composable
fun DefinitionHero(
    term: String,
    definition: String,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()
    val glyph = glyphForTerm(term)

    val infinite = rememberInfiniteTransition(label = "hero")
    val pulse by infinite.animateFloat(
        initialValue = 1.0f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1800), repeatMode = RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier = modifier.fillMaxSize().padding(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Section-style label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(p.indigo.accent))
                Spacer(Modifier.width(8.dp))
                LLText(
                    "KEY TERM",
                    color = p.indigo.accent, size = 13.sp,
                    weight = FontWeight.Bold, letterSpacing = 2.sp,
                )
            }
            Spacer(Modifier.height(28.dp))

            // Hero glyph
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                p.indigo.surfaceStrong,
                                p.indigo.surface.copy(alpha = 0.6f),
                            )
                        )
                    )
                    .border(3.dp, p.indigo.accent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                LLText(glyph, color = p.indigo.accent, size = 64.sp)
            }

            Spacer(Modifier.height(36.dp))

            // The term itself
            LLText(
                term,
                color = t.ink50,
                size = 64.sp, lineHeight = 72.sp,
                weight = FontWeight.ExtraBold,
                align = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            // Definition
            Box(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(p.indigo.surface)
                    .border(1.dp, p.indigo.border, RoundedCornerShape(Radius.lg))
                    .padding(24.dp),
            ) {
                LLText(
                    definition,
                    color = p.indigo.ink, size = 22.sp, lineHeight = 32.sp,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    weight = FontWeight.Medium,
                )
            }
        }
    }
}

/** Small library of glyphs keyed by term (case-insensitive). */
private fun glyphForTerm(term: String): String {
    val key = term.lowercase().trim()
    return when {
        "biodiversity" in key -> "🌍"
        "classification" in key || "grouping" in key -> "🗂"
        "venation" in key -> "🍃"
        "reticulate" in key -> "🕸"
        "parallel" in key -> "🎋"
        "taproot" in key -> "🌱"
        "fibrous" in key -> "🌾"
        "cotyledon" in key -> "🌰"
        "climber" in key -> "🌿"
        "creeper" in key -> "🍃"
        "herb" in key -> "🌱"
        "shrub" in key -> "🌿"
        "tree" in key -> "🌳"
        "habitat" in key -> "🏞"
        "terrestrial" in key -> "🏕"
        "aquatic" in key -> "🌊"
        "amphibian" in key -> "🐸"
        "adaptation" in key -> "🐪"
        "monocot" in key -> "🌽"
        "dicot" in key -> "🫘"
        else -> "✦"
    }
}
