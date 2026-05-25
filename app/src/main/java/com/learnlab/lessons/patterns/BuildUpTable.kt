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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette

/**
 * A table where rows build in one at a time as the teacher taps.
 *
 * Header is always visible. Rows fade+slide in. A "reveal next" hint appears
 * at the bottom; tapping the slide advances. After all rows are revealed,
 * tapping resets.
 */
@Composable
fun BuildUpTable(
    caption: String?,
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()
    var revealed by remember(caption) { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (revealed < rows.size) revealed++ else revealed = 0
            }
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 1300.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (caption != null) {
                LLText(
                    caption,
                    color = p.violet.accent,
                    size = 16.sp,
                    weight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    align = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(20.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.lg))
                    .border(1.dp, t.line, RoundedCornerShape(Radius.lg)),
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(t.surface)
                        .padding(18.dp),
                ) {
                    headers.forEach { h ->
                        LLText(h, color = t.ink50, size = 16.sp,
                            weight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))
                // Rows
                rows.forEachIndexed { idx, row ->
                    AnimatedVisibility(
                        visible = idx < revealed,
                        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 3 },
                    ) {
                        val bg = if (idx % 2 == 0) t.bg else t.surface2
                        // Last row = synthesis row: emerald tint
                        val isSynth = idx == rows.lastIndex && rows.size > 2
                        val rowBg = if (isSynth) p.emerald.surface else bg
                        val rowInk = if (isSynth) p.emerald.ink else t.ink200
                        val rowWeight = if (isSynth) FontWeight.SemiBold else FontWeight.Normal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(16.dp),
                        ) {
                            row.forEach { cell ->
                                LLText(
                                    cell, color = rowInk,
                                    size = 16.sp, lineHeight = 22.sp,
                                    weight = rowWeight,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LLText(
                    when {
                        revealed == 0 -> "TAP TO REVEAL FIRST ROW"
                        revealed < rows.size -> "TAP TO REVEAL NEXT ROW"
                        else -> "TAP TO RESTART"
                    },
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
                    LLText("$revealed / ${rows.size}",
                        color = t.ink400, size = 12.sp, weight = FontWeight.SemiBold)
                }
            }
        }
    }
}
