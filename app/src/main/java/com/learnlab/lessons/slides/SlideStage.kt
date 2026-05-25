package com.learnlab.lessons.slides

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout
import com.learnlab.design.IconSize
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.design.Spacing

/**
 * Full-screen IFP-friendly slide stage. Top strip = title + counter + outline button.
 * Middle = the slide content. Bottom strip = back / next big buttons + thumbnail rail.
 * Arrow keys also page (for clicker / keyboard).
 */
@Composable
fun SlideStage(
    chapter: Chapter,
    slides: List<Slide>,
    cast: Map<String, Character>,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val t = LL.tokens
    var index by remember(chapter.id) { mutableIntStateOf(0) }
    var outlineOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun goNext() { if (index < slides.lastIndex) index++ }
    fun goPrev() { if (index > 0) index-- }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) false
                else when (e.key) {
                    Key.DirectionRight, Key.PageDown, Key.Spacebar -> { goNext(); true }
                    Key.DirectionLeft, Key.PageUp -> { goPrev(); true }
                    Key.Escape -> { if (outlineOpen) { outlineOpen = false; true } else false }
                    else -> false
                }
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopStrip(
                chapter = chapter,
                currentSlide = slides[index],
                index = index,
                total = slides.size,
                onBack = onBack,
                onOutline = { outlineOpen = !outlineOpen },
            )
            // Slide area
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                AnimatedContent(
                    targetState = index,
                    label = "slide",
                    transitionSpec = {
                        val forward = targetState > initialState
                        (slideInHorizontally(tween(280)) { if (forward) it else -it } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(180)))
                    },
                ) { i ->
                    SlideContent(
                        slide = slides[i],
                        chapter = chapter,
                        cast = cast,
                        onOpenActivity = onOpenActivity,
                    )
                }
            }
            BottomStrip(
                slides = slides,
                index = index,
                onPrev = { goPrev() },
                onNext = { goNext() },
                onJump = { index = it },
            )
        }
        if (outlineOpen) {
            OutlineDrawer(
                chapter = chapter,
                slides = slides,
                currentIndex = index,
                onJump = { index = it; outlineOpen = false },
                onClose = { outlineOpen = false },
            )
        }
    }
}

/* ───────────────────────── Top strip ───────────────────────── */

@Composable
private fun TopStrip(
    chapter: Chapter,
    currentSlide: Slide,
    index: Int,
    total: Int,
    onBack: () -> Unit,
    onOutline: () -> Unit,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(0.dp))
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBtn(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = "Close",
            onClick = onBack,
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            LLText(
                "CH ${chapter.chapter.number} · ${chapter.chapter.title}",
                color = t.ink50, size = 14.sp, weight = FontWeight.Bold, maxLines = 1,
            )
            if (currentSlide.sectionNumber != null && currentSlide.sectionTitle != null) {
                LLText(
                    "${currentSlide.sectionNumber}  ${currentSlide.sectionTitle}",
                    color = t.ink500, size = 11.sp, maxLines = 1,
                )
            }
        }
        // Slide counter
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(t.surface2)
                .padding(horizontal = Spacing.md, vertical = 6.dp),
        ) {
            LLText("${index + 1} / $total", color = t.ink400, size = 12.sp, weight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(Spacing.sm))
        IconBtn(icon = Icons.Filled.GridView, label = "Outline", onClick = onOutline)
    }
}

@Composable
private fun IconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(t.surface2)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = t.ink200, modifier = Modifier.size(IconSize.md))
    }
}

/* ───────────────────────── Bottom strip ───────────────────────── */

@Composable
private fun BottomStrip(
    slides: List<Slide>,
    index: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onJump: (Int) -> Unit,
) {
    val t = LL.tokens
    val railState = rememberLazyListState()
    LaunchedEffect(index) {
        val target = (index - 2).coerceAtLeast(0)
        railState.animateScrollToItem(target)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(0.dp))
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Prev
        BigArrow(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = "Previous",
            enabled = index > 0,
            onClick = onPrev,
        )
        Spacer(Modifier.width(Spacing.md))
        // Thumbnail rail
        LazyRow(
            state = railState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(slides.size) { i ->
                ThumbnailDot(slides[i], i, isCurrent = i == index, onJump = { onJump(i) })
            }
        }
        Spacer(Modifier.width(Spacing.md))
        BigArrow(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            label = "Next",
            enabled = index < slides.lastIndex,
            onClick = onNext,
            primary = true,
        )
    }
}

@Composable
private fun BigArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    primary: Boolean = false,
) {
    val t = LL.tokens
    val bg = when {
        !enabled -> t.surface2.copy(alpha = 0.5f)
        primary -> t.accent500
        else -> t.surface2
    }
    val fg = if (!enabled) t.ink500 else if (primary) t.surface else t.ink50
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 48.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(IconSize.lg))
    }
}

@Composable
private fun ThumbnailDot(slide: Slide, i: Int, isCurrent: Boolean, onJump: () -> Unit) {
    val t = LL.tokens
    val accent = t.accent500
    val isMarker = slide.layout == SlideLayout.SectionTitle || slide.layout == SlideLayout.Cover
    val width = if (isMarker) 22.dp else 16.dp
    val bg = when {
        isCurrent -> accent
        isMarker -> t.ink400
        else -> t.surface3
    }
    Box(
        modifier = Modifier
            .size(width = width, height = 16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .clickable { onJump() },
    )
}

/* ───────────────────────── Outline drawer ───────────────────────── */

@Composable
private fun OutlineDrawer(
    chapter: Chapter,
    slides: List<Slide>,
    currentIndex: Int,
    onJump: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val t = LL.tokens
    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClose),
    )
    // Drawer
    Row(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(440.dp)
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(0.dp))
                .padding(Spacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    LLText("OUTLINE", color = t.ink500, size = 11.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    LLText(chapter.chapter.title, color = t.ink50, size = 16.sp, weight = FontWeight.Bold, maxLines = 1)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(t.surface2)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close outline",
                        tint = t.ink200, modifier = Modifier.size(IconSize.md))
                }
            }
            Spacer(Modifier.height(Spacing.md))
            // Only show section + cover entries so the list isn't 40 items long
            val markers = slides.mapIndexedNotNull { idx, s ->
                if (s.layout == SlideLayout.Cover || s.layout == SlideLayout.SectionTitle) idx to s else null
            }
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(markers) { (idx, s) ->
                    val isCurrent = idx == currentIndex || (currentIndex > idx && markers.indexOfFirst { it.first > currentIndex } - 1 == markers.indexOfFirst { it.first == idx })
                    OutlineRow(idx = idx, slide = s, isCurrent = isCurrent, onJump = { onJump(idx) })
                }
            }
        }
    }
}

@Composable
private fun OutlineRow(idx: Int, slide: Slide, isCurrent: Boolean, onJump: () -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (isCurrent) t.surface2 else t.surface)
            .clickable { onJump() }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (slide.layout == SlideLayout.Cover) {
            LLText("Cover", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
        } else {
            LLText(slide.sectionNumber ?: "·",
                color = t.accent500, size = 13.sp, weight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            LLText(slide.sectionTitle ?: "(untitled)",
                color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
        }
    }
}
