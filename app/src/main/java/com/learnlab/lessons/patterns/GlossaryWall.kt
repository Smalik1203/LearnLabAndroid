package com.learnlab.lessons.patterns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * The end-of-chapter keyword cloud. Tap a chip → a floating definition card
 * appears in the centre. Tap empty space or another chip to dismiss/switch.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlossaryWall(
    title: String,
    terms: List<String>,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()
    var focused by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(focused) {
                detectTapGestures(onTap = { focused = null })
            }
            .padding(48.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().widthIn(max = 1500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(p.emerald.surfaceStrong.copy(alpha = 0.5f))
                    .padding(horizontal = 22.dp, vertical = 8.dp),
            ) {
                LLText(
                    title.uppercase(),
                    color = p.emerald.accent,
                    size = 18.sp,
                    weight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                )
            }
            Spacer(Modifier.height(32.dp))
            // Hint
            LLText(
                if (focused == null) "Tap any word for its meaning"
                else "Tap empty space to close",
                color = t.ink400,
                size = 13.sp,
                weight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(20.dp))
            // The wall
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                terms.forEach { term ->
                    Box(modifier = Modifier.padding(end = 12.dp, bottom = 0.dp)) {
                        GlossaryChip(
                            term = term,
                            focused = focused == term,
                            dimmed = focused != null && focused != term,
                            onTap = { focused = if (focused == term) null else term },
                        )
                    }
                }
            }
        }

        // Floating definition card overlay
        AnimatedVisibility(
            visible = focused != null,
            enter = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.92f),
            exit = fadeOut(tween(160)) + scaleOut(tween(180), targetScale = 0.96f),
            modifier = Modifier.align(Alignment.Center),
        ) {
            val term = focused
            if (term != null) GlossaryFloatingCard(term = term)
        }
    }
}

@Composable
private fun GlossaryChip(
    term: String,
    focused: Boolean,
    dimmed: Boolean,
    onTap: () -> Unit,
) {
    val t = LL.tokens
    val p = lessonPalette()
    val alpha = when {
        focused -> 1f
        dimmed -> 0.35f
        else -> 1f
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(
                if (focused) p.sky.accent
                else p.sky.surface.copy(alpha = alpha)
            )
            .border(
                if (focused) 2.dp else 1.dp,
                p.sky.border.copy(alpha = alpha),
                RoundedCornerShape(Radius.pill),
            )
            .pointerInput(focused) {
                detectTapGestures(onTap = { onTap() })
            }
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        LLText(
            term,
            color = if (focused) t.surface else t.ink50.copy(alpha = alpha),
            size = 20.sp,
            weight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GlossaryFloatingCard(term: String) {
    val t = LL.tokens
    val p = lessonPalette()
    val definition = definitionForTerm(term)
    val glyph = glyphForTermPublic(term)
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .clip(RoundedCornerShape(Radius.xl))
            .background(t.surface)
            .border(2.dp, p.sky.accent, RoundedCornerShape(Radius.xl))
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(p.sky.surfaceStrong),
            contentAlignment = Alignment.Center,
        ) {
            LLText(glyph, color = p.sky.accent, size = 38.sp)
        }
        Spacer(Modifier.height(18.dp))
        LLText(
            term,
            color = t.ink50, size = 38.sp,
            weight = FontWeight.ExtraBold,
            align = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        LLText(
            definition,
            color = t.ink200, size = 18.sp, lineHeight = 26.sp,
            align = TextAlign.Center,
        )
    }
}

/** Public re-export of the glyph lookup so other patterns can use the same vocabulary. */
fun glyphForTermPublic(term: String): String {
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
        "sacred grove" in key || "sacred groves" in key -> "🌲"
        "analyse" in key || "analyze" in key -> "🔬"
        "compare" in key -> "⚖"
        "create" in key -> "✦"
        "explore" in key -> "🧭"
        "group" in key -> "🗂"
        "observe" in key -> "👁"
        "record" in key -> "📓"
        "relate" in key -> "🔗"
        else -> "✦"
    }
}

/** Quick glossary keyed by term. Used to pop a floating card on tap. */
private fun definitionForTerm(term: String): String {
    val key = term.lowercase().trim()
    return when {
        "biodiversity" in key ->
            "The variety of plants and animals found in a particular place."
        "adaptation" in key ->
            "A special feature that helps a plant or animal survive in its surroundings."
        "habitat" in key ->
            "The natural surroundings in which a plant or animal lives."
        "terrestrial" in key ->
            "Living on land. Forests, deserts, grasslands, and mountains are terrestrial habitats."
        "aquatic" in key ->
            "Living in water. Ponds, lakes, rivers, and oceans are aquatic habitats."
        "amphibian" in key ->
            "An animal that can live both on land and in water. Frogs are an example."
        "monocot" in key ->
            "A plant whose seed has one cotyledon. Maize and wheat are monocots."
        "dicot" in key ->
            "A plant whose seed has two cotyledons. Chickpea and mango are dicots."
        "parallel venation" in key ->
            "Veins that run side by side along the length of a leaf."
        "reticulate venation" in key ->
            "A net-like pattern of veins on a leaf, branching from a central vein."
        "venation" in key ->
            "The pattern of veins on a leaf."
        "taproot" in key ->
            "A root system with one thick main root and smaller side roots branching off it."
        "fibrous" in key ->
            "A root system made of many thin roots of similar size, with no single main root."
        "cotyledon" in key ->
            "The food-storing leaf inside a seed. Dicots have two; monocots have one."
        "climber" in key ->
            "A plant with a weak stem that needs support to grow upward."
        "creeper" in key ->
            "A plant that spreads along the ground."
        "herb" in key ->
            "A short plant with a soft, green stem."
        "shrub" in key ->
            "A medium plant with several woody stems starting near the ground."
        "tree" in key ->
            "A tall plant with a single thick, woody trunk; branches start higher up."
        "sacred grove" in key || "sacred groves" in key ->
            "An untouched patch of forest, protected for generations by a local community."
        "analyse" in key || "analyze" in key ->
            "To look carefully at something to understand how its parts fit together."
        "compare" in key ->
            "To look at two or more things and see what is the same and what is different."
        "create" in key ->
            "To make something new."
        "explore" in key ->
            "To go and find out about something for yourself."
        "group" in key ->
            "To put things together that share a common feature."
        "observe" in key ->
            "To watch carefully, using all your senses."
        "record" in key ->
            "To write down or sketch what you see, so you don't forget."
        "relate" in key ->
            "To see how one thing is connected to another."
        else -> "A keyword from this chapter — definition coming soon."
    }
}
