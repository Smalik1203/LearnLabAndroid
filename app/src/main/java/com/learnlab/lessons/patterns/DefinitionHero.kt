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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette
import com.learnlab.design.LearnLabFonts
import kotlinx.coroutines.delay

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

    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnim = true
    }

    val entryAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "entryAlpha"
    )

    val entryScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.9f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "entryScale"
    )

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
            modifier = Modifier
                .widthIn(max = 1000.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    alpha = entryAlpha,
                    scaleX = entryScale,
                    scaleY = entryScale
                ),
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

@Composable
fun MultiDefinitionHero(
    terms: List<com.learnlab.content.chapter.ChapterBlock.KeyTerm>,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 1300.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Section-style label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(p.indigo.accent))
                Spacer(Modifier.width(8.dp))
                LLText(
                    "KEY TERMS",
                    color = p.indigo.accent, size = 13.sp,
                    weight = FontWeight.Bold, letterSpacing = 2.sp,
                )
            }
            Spacer(Modifier.height(24.dp))

            // Grid of definition cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(terms.size) { index ->
                    val item = terms[index]
                    KeyTermGridCard(item = item, index = index, palette = p, tokens = t)
                }
            }
        }
    }
}

@Composable
private fun KeyTermGridCard(
    item: com.learnlab.content.chapter.ChapterBlock.KeyTerm,
    index: Int,
    palette: com.learnlab.lessons.LessonPalette,
    tokens: com.learnlab.design.LearnLabTokens,
) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        animate = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (animate) 1f else 0.88f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val translationY by animateFloatAsState(
        targetValue = if (animate) 0f else 30f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "translationY"
    )

    val glyph = glyphForTerm(item.term)
    
    Column(
        modifier = Modifier
            .graphicsLayer(
                alpha = alpha,
                scaleX = scale,
                scaleY = scale,
                translationY = translationY
            )
            .clip(RoundedCornerShape(Radius.md))
            .background(tokens.surface2.copy(alpha = 0.5f))
            .border(
                1.dp, 
                Brush.verticalGradient(
                    listOf(palette.indigo.accent.copy(alpha = 0.4f), tokens.line.copy(alpha = 0.1f))
                ),
                RoundedCornerShape(Radius.md)
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(palette.indigo.surfaceStrong)
                    .border(1.dp, palette.indigo.accent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LLText(glyph, color = palette.indigo.accent, size = 20.sp)
            }
            LLText(
                text = item.term,
                color = tokens.ink50,
                size = 18.sp,
                weight = FontWeight.Bold,
                fontFamily = LearnLabFonts.Display
            )
        }
        Spacer(Modifier.height(12.dp))
        LLText(
            text = item.definition,
            color = tokens.ink200,
            size = 14.sp,
            lineHeight = 20.sp
        )
    }
}
