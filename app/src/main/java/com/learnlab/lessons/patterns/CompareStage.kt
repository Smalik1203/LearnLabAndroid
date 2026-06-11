package com.learnlab.lessons.patterns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette
import com.learnlab.lessons.slides.loadFigureBitmap
import com.learnlab.lessons.slides.PlaceholderImage

/**
 * Side-by-side compare with a build-up synthesis row at the bottom.
 *
 *  ┌────────────────────┬────────────────────┐
 *  │                    │                    │
 *  │      [image L]     │      [image R]     │
 *  │       LABEL L      │       LABEL R      │
 *  │       blurb L      │       blurb R      │
 *  └────────────────────┴────────────────────┘
 *  CAPTION
 *  ▸ Feature 1     ▸ Feature 2     ▸ Feature 3
 *  (revealed one at a time as teacher taps anywhere)
 */
@Composable
fun CompareStage(
    caption: String?,
    leftImage: String,
    leftLabel: String,
    leftBlurb: String?,
    rightImage: String,
    rightLabel: String,
    rightBlurb: String?,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()

    // Auto-derive synthesis chips: split each blurb on ". " or ", " up to 2 pieces.
    // Replace with author-provided in future once JSON has `synthesis: [...]`.
    val syntheses = buildList {
        leftBlurb?.let { add(SynthChip(it, leftLabel, p.amber.accent)) }
        rightBlurb?.let { add(SynthChip(it, rightLabel, p.sky.accent)) }
    }
    var revealed by remember(caption) { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (revealed < syntheses.size) revealed++ else revealed = 0
            }
            .padding(28.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                CompareCell(
                    label = leftLabel, image = leftImage,
                    accent = p.amber.accent, modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                CompareCell(
                    label = rightLabel, image = rightImage,
                    accent = p.sky.accent, modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            if (caption != null) {
                Spacer(Modifier.height(14.dp))
                LLText(
                    caption, color = t.ink400, size = 14.sp,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (syntheses.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    syntheses.forEachIndexed { i, chip ->
                        AnimatedVisibility(
                            visible = i < revealed,
                            enter = fadeIn(tween(240)) + slideInVertically(tween(280)) { it / 4 },
                            modifier = Modifier.weight(1f),
                        ) {
                            SynthesisChip(chip)
                        }
                        if (i >= revealed) Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    LLText(
                        if (revealed == 0) "TAP TO REVEAL"
                        else if (revealed < syntheses.size) "TAP TO REVEAL NEXT"
                        else "TAP TO RESTART",
                        color = t.ink500, size = 11.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareCell(
    label: String,
    image: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface2)
            .border(2.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(Radius.lg))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(Radius.md)),
            contentAlignment = Alignment.Center,
        ) {
            val bmp = loadFigureBitmap(image)
            if (bmp != null) {
                Image(bitmap = bmp, contentDescription = label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize())
            } else {
                PlaceholderImage(label = label, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(accent.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            LLText(label.uppercase(), color = accent,
                size = 14.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
    }
}

private data class SynthChip(val body: String, val side: String, val accent: Color)

@Composable
private fun SynthesisChip(chip: SynthChip) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(chip.accent.copy(alpha = 0.12f))
            .border(1.dp, chip.accent.copy(alpha = 0.5f), RoundedCornerShape(Radius.md))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp).clip(CircleShape).background(chip.accent),
            contentAlignment = Alignment.Center,
        ) {
            LLText(chip.side.first().uppercase(),
                color = Color.White, size = 14.sp, weight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(12.dp))
        LLText(chip.body, color = t.ink50, size = 14.sp, lineHeight = 20.sp,
            weight = FontWeight.Medium)
    }
}
