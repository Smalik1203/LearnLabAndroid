package com.learnlab.lessons.patterns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.learnlab.design.Radius

/**
 * A callout shown as a sequence of revealable points. The teacher taps the
 * slide to advance — each tap reveals the next point. A small chip in the
 * corner shows progress (e.g. "2 / 3").
 *
 * If the input body parses to just one sentence, falls back to the original
 * centred-callout look.
 */
@Composable
fun BuildUpReveal(
    title: String?,
    body: String,
    accentSurface: Color,
    accentBorder: Color,
    accentInk: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val points = splitIntoPoints(body)
    var revealed by remember(body) { mutableIntStateOf(if (points.size == 1) 1 else 0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (revealed < points.size) revealed++ else revealed = 0
            }
            .padding(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 1100.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor))
                    Spacer(Modifier.width(10.dp))
                    LLText(
                        title.uppercase(),
                        color = accentColor, size = 14.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp,
                    )
                }
                Spacer(Modifier.height(36.dp))
            }
            if (points.size <= 1) {
                // Single statement — just render centred big.
                LLText(
                    body, color = accentInk,
                    size = 36.sp, lineHeight = 52.sp,
                    weight = FontWeight.Medium,
                    align = androidx.compose.ui.text.style.TextAlign.Center,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    points.forEachIndexed { i, point ->
                        AnimatedVisibility(
                            visible = i < revealed,
                            enter = fadeIn(tween(280)) + slideInVertically(tween(320)) { it / 4 },
                        ) {
                            RevealPoint(
                                index = i + 1,
                                text = point,
                                accentSurface = accentSurface,
                                accentBorder = accentBorder,
                                accentInk = accentInk,
                                accentColor = accentColor,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
                // Hint / progress
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LLText(
                        if (revealed == 0) "TAP TO REVEAL"
                        else if (revealed < points.size) "TAP TO REVEAL NEXT"
                        else "TAP TO RESTART",
                        color = t.ink500, size = 12.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.pill))
                            .background(t.surface2)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        LLText("$revealed / ${points.size}",
                            color = t.ink400, size = 12.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevealPoint(
    index: Int,
    text: String,
    accentSurface: Color,
    accentBorder: Color,
    accentInk: Color,
    accentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(accentSurface)
            .border(1.dp, accentBorder, RoundedCornerShape(Radius.lg))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center,
        ) {
            LLText("$index", color = Color.White, size = 20.sp, weight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(20.dp))
        LLText(
            text, color = accentInk,
            size = 22.sp, lineHeight = 32.sp,
            weight = FontWeight.Medium,
        )
    }
}

private fun splitIntoPoints(body: String): List<String> {
    val cleaned = body.trim()
    if (cleaned.isEmpty()) return emptyList()
    // Split on sentence endings keeping the punctuation
    val parts = Regex("""(?<=[.!?])\s+""").split(cleaned)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return parts
}
