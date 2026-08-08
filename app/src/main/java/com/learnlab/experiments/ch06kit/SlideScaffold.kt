package com.learnlab.experiments.ch06kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls

/**
 * The internal pager that lives inside an experiment's content area (above the shell's own
 * between-experiment Prev/Next bar). Renders the shared outer card, an eyebrow + slide counter,
 * the current slide, and an internal Back/Next row with dot indicators.
 *
 * Centralises progress reporting so every paged Ch.2 simulation behaves identically:
 * progress = page / (slideCount-1), complete on the last slide.
 */
@Composable
fun SlideDeck(
    controls: ExperimentControls,
    slideCount: Int,
    eyebrow: String,
    modifier: Modifier = Modifier,
    topics: List<String>? = null,
    completeOnLastSlide: Boolean = true,
    slide: @Composable (index: Int) -> Unit,
) {
    val t = LL.tokens
    var page by remember { mutableStateOf(0) }

    LaunchedEffect(page, slideCount) {
        val denom = (slideCount - 1).coerceAtLeast(1).toFloat()
        controls.onProgress((page / denom).coerceIn(0f, 1f))
        if (completeOnLastSlide && page >= slideCount - 1) controls.onComplete(1f)
    }

    Box(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LLText(eyebrow, color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                val topic = topics?.getOrNull(page)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(t.surface2)
                        .border(1.dp, t.line, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    LLText(
                        "Slide ${page + 1} / $slideCount" + (if (topic != null) "  ·  $topic" else ""),
                        color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold,
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                slide(page)
            }

            SlideNavRow(page = page, count = slideCount) { page = it.coerceIn(0, slideCount - 1) }
        }
    }
}

@Composable
private fun SlideNavRow(page: Int, count: Int, onGo: (Int) -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SecondaryButton(label = "‹ Back", onClick = { onGo(page - 1) }, enabled = page > 0)
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0 until count) {
                val active = i == page
                Box(
                    modifier = Modifier
                        .size(if (active) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (active) t.accent500 else t.surface3),
                )
            }
        }
        PrimaryButton(label = "Next ›", onClick = { onGo(page + 1) }, enabled = page < count - 1)
    }
    Spacer(Modifier.width(0.dp))
}
