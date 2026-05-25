package com.learnlab.lessons.patterns

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.LessonHue
import com.learnlab.lessons.lessonPalette
import com.learnlab.lessons.slides.loadFigureBitmap

enum class InterludeKind { Scientist, SuccessStory, DidYouKnow, MoreToKnow }

/**
 * One pattern, four faces. Full-bleed coloured interlude — distinct from the
 * main reading flow so it reads as a pause / interesting aside.
 */
@Composable
fun SpotlightInterlude(
    kind: InterludeKind,
    title: String,
    body: String,
    heroImage: String? = null,
    heroLabel: String? = null,
    eyebrow: String? = null,
    galleryImages: List<Pair<String, String?>> = emptyList(),
    highlight: String? = null,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val p = lessonPalette()
    val hue: LessonHue = when (kind) {
        InterludeKind.Scientist -> p.emerald
        InterludeKind.SuccessStory -> p.amber
        InterludeKind.DidYouKnow -> p.sky
        InterludeKind.MoreToKnow -> p.violet
    }
    val labelDefault = when (kind) {
        InterludeKind.Scientist -> "KNOW A SCIENTIST"
        InterludeKind.SuccessStory -> "SUCCESS STORY"
        InterludeKind.DidYouKnow -> "DO YOU KNOW"
        InterludeKind.MoreToKnow -> "MORE TO KNOW"
    }
    val glyph = when (kind) {
        InterludeKind.Scientist -> "🔬"
        InterludeKind.SuccessStory -> "✓"
        InterludeKind.DidYouKnow -> "?"
        InterludeKind.MoreToKnow -> "!"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(hue.surface, t.bg),
                )
            )
            .padding(48.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 1500.dp)) {
            // Eyebrow row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(hue.surfaceStrong),
                    contentAlignment = Alignment.Center,
                ) {
                    LLText(glyph, color = hue.accent, size = 22.sp, weight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                LLText(
                    eyebrow ?: labelDefault,
                    color = hue.accent, size = 13.sp,
                    weight = FontWeight.ExtraBold, letterSpacing = 2.5.sp,
                )
            }
            Spacer(Modifier.height(18.dp))

            // Title
            LLText(
                title,
                color = t.ink50,
                size = 48.sp, lineHeight = 56.sp,
                weight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(28.dp))

            // Body + hero image row
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    LLText(
                        body, color = t.ink200,
                        size = 19.sp, lineHeight = 30.sp,
                    )
                    if (highlight != null) {
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .background(hue.surfaceStrong.copy(alpha = 0.6f))
                                .padding(20.dp),
                        ) {
                            LLText(
                                highlight, color = hue.ink,
                                size = 20.sp, lineHeight = 28.sp,
                                weight = FontWeight.Bold,
                                align = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                if (heroImage != null) {
                    Column(
                        modifier = Modifier.weight(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(t.surface2)
                                .border(2.dp, hue.accent.copy(alpha = 0.5f), RoundedCornerShape(Radius.lg)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val bmp = loadFigureBitmap(heroImage)
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp, contentDescription = heroLabel,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                        .clip(RoundedCornerShape(Radius.lg)),
                                )
                            } else {
                                LLText("[ image ]", color = t.ink500, size = 14.sp)
                            }
                        }
                        if (heroLabel != null) {
                            Spacer(Modifier.height(10.dp))
                            LLText(heroLabel, color = t.ink400, size = 14.sp,
                                weight = FontWeight.Medium,
                                align = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Gallery row (for DoYouKnow with multiple images)
            if (galleryImages.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    galleryImages.forEach { (asset, caption) ->
                        Column(modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(140.dp)
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(t.surface2),
                                contentAlignment = Alignment.Center,
                            ) {
                                val bmp = loadFigureBitmap(asset)
                                if (bmp != null) {
                                    Image(bitmap = bmp, contentDescription = caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                            .clip(RoundedCornerShape(Radius.md)))
                                } else {
                                    LLText("[ ${caption ?: "image"} ]",
                                        color = t.ink500, size = 12.sp)
                                }
                            }
                            if (caption != null) {
                                Spacer(Modifier.height(6.dp))
                                LLText(caption, color = t.ink400,
                                    size = 13.sp, weight = FontWeight.Medium,
                                    align = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
