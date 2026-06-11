package com.learnlab.lessons.figures.composeDraw

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.LearnLabFonts
import com.learnlab.design.Radius

/**
 * Renders four premium criterion cards: Flowers, Stem, Eating, and Place.
 * Aligns horizontally so that hotspot overlays at x=0.13, 0.38, 0.63, 0.87 land centered.
 */
@Composable
fun GroupingCriteriaPills(modifier: Modifier = Modifier) {
    val t = LL.tokens
    val cardBg = if (t.isDark) t.surface.copy(alpha = 0.5f) else t.surface.copy(alpha = 0.9f)
    
    val criteria = listOf(
        CriterionItem(
            title = "Flowers",
            subtitle = "Presence / Absence",
            emoji = "🌸",
            gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E)) // pink-rose
        ),
        CriterionItem(
            title = "Stem Type",
            subtitle = "Hard / Soft / Woody",
            emoji = "🪵",
            gradient = listOf(Color(0xFF10B981), Color(0xFF059669)) // emerald
        ),
        CriterionItem(
            title = "Eating Habits",
            subtitle = "Herbivore / Carnivore",
            emoji = "🍎",
            gradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706)) // amber
        ),
        CriterionItem(
            title = "Place They Live",
            subtitle = "Land / Water / Air",
            emoji = "🏠",
            gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)) // blue-indigo
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            criteria.forEach { item ->
                Column(
                    modifier = Modifier
                        .size(width = 180.dp, height = 240.dp)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(cardBg)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(item.gradient[0].copy(alpha = 0.7f), t.line.copy(alpha = 0.2f))
                            ),
                            shape = RoundedCornerShape(Radius.lg)
                        )
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(Radius.lg),
                            ambientColor = item.gradient[0].copy(alpha = 0.1f),
                            spotColor = item.gradient[0].copy(alpha = 0.2f)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(item.gradient[0].copy(alpha = 0.12f))
                            .border(1.dp, item.gradient[0].copy(alpha = 0.3f), RoundedCornerShape(Radius.md)),
                        contentAlignment = Alignment.Center
                    ) {
                        LLText(
                            text = item.emoji,
                            size = 36.sp
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    LLText(
                        text = item.title,
                        color = t.ink50,
                        size = 20.sp,
                        weight = FontWeight.Bold,
                        fontFamily = LearnLabFonts.Display,
                        align = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    LLText(
                        text = item.subtitle,
                        color = t.ink400,
                        size = 13.sp,
                        align = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class CriterionItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val gradient: List<Color>
)
