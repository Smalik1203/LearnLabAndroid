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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    isDark: Boolean = false,
    onToggleTheme: () -> Unit = {},
    isEditing: Boolean = false,
    onToggleEdit: () -> Unit = {},
    /** Called when edit mode starts on an auto-layout slide; parent should
     *  build a SlideOverride from the slide and store it locally. */
    onRequestSynthesizeOverride: (Slide) -> Unit = {},
    /** Called whenever an element on a FreeForm slide is moved/resized/etc.
     *  Parent updates its localOverrides map; persistence is step 9. */
    onOverrideEdited: (slideId: String, override: com.learnlab.content.chapter.SlideOverride) -> Unit = { _, _ -> },
    /** Called when a gesture (drag/resize/rotate) ends; parent records this
     *  as one undo step. */
    onOverrideCommitted: (slideId: String, override: com.learnlab.content.chapter.SlideOverride) -> Unit = { _, _ -> },
    /** Undo/redo plumbing — parent owns the history stack. */
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
) {
    var index by remember(chapter.id) { mutableIntStateOf(0) }
    var outlineOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // When edit mode is on AND the visible slide has no override, ask the
    // parent to synthesize one from the auto-layout. Re-runs when the user
    // pages to a fresh slide while still editing.
    LaunchedEffect(isEditing, index, slides.size) {
        if (isEditing && index in slides.indices) {
            val slide = slides[index]
            if (slide.layout != SlideLayout.FreeForm) {
                onRequestSynthesizeOverride(slide)
            }
        }
    }

    fun goNext() { if (index < slides.lastIndex) index++ }
    fun goPrev() { if (index > 0) index-- }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                isDark = isDark,
                onToggleTheme = onToggleTheme,
                isEditing = isEditing,
                onToggleEdit = onToggleEdit,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
            )
            // Slide area — horizontal swipe pages when not editing.
            val swipeThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }
            var dragAccum by remember { mutableStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(
                        if (!isEditing) Modifier.pointerInput(slides.size) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragAccum = 0f },
                                onDragEnd = {
                                    when {
                                        dragAccum <= -swipeThresholdPx -> goNext()
                                        dragAccum >= swipeThresholdPx -> goPrev()
                                    }
                                    dragAccum = 0f
                                },
                                onDragCancel = { dragAccum = 0f },
                                onHorizontalDrag = { _, delta -> dragAccum += delta },
                            )
                        } else Modifier,
                    ),
            ) {
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
                        isEditing = isEditing,
                        onOverrideEdited = onOverrideEdited,
                        onOverrideCommitted = onOverrideCommitted,
                    )
                }
            }
            BottomStrip(
                chapter = chapter,
                slides = slides,
                index = index,
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
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
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
        if (isEditing) {
            Spacer(Modifier.width(Spacing.sm))
            IconBtn(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "Undo",
                onClick = onUndo,
                enabled = canUndo,
            )
            Spacer(Modifier.width(Spacing.sm))
            IconBtn(
                icon = Icons.AutoMirrored.Filled.Redo,
                label = "Redo",
                onClick = onRedo,
                enabled = canRedo,
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        IconBtn(
            icon = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
            label = if (isEditing) "Done editing" else "Edit slide",
            onClick = onToggleEdit,
            highlighted = isEditing,
        )
        Spacer(Modifier.width(Spacing.sm))
        IconBtn(
            icon = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            label = if (isDark) "Switch to light mode" else "Switch to dark mode",
            onClick = onToggleTheme,
        )
        Spacer(Modifier.width(Spacing.sm))
        IconBtn(icon = Icons.Filled.GridView, label = "Outline", onClick = onOutline)
    }
}

@Composable
private fun IconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
    enabled: Boolean = true,
) {
    val t = LL.tokens
    val bg = when {
        !enabled -> t.surface2.copy(alpha = 0.4f)
        highlighted -> t.accent500
        else -> t.surface2
    }
    val tint = when {
        !enabled -> t.ink500
        highlighted -> t.surface
        else -> t.ink200
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(IconSize.md))
    }
}

/* ───────────────────────── Bottom strip ───────────────────────── */

@Composable
private fun BottomStrip(
    chapter: Chapter,
    slides: List<Slide>,
    index: Int,
    onJump: (Int) -> Unit,
) {
    val t = LL.tokens
    val railState = rememberLazyListState()
    LaunchedEffect(index) {
        val target = (index - 3).coerceAtLeast(0)
        railState.animateScrollToItem(target)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pagination dot rail — takes most of the width
        LazyRow(
            state = railState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(slides.size) { i ->
                ThumbnailDot(slides[i], i, isCurrent = i == index, onJump = { onJump(i) })
            }
        }
        Spacer(Modifier.width(Spacing.md))
        // Centre label
        LLText(
            "CH ${chapter.chapter.number} - ${chapter.chapter.title}",
            color = t.ink400,
            size = 13.sp,
            weight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Spacer(Modifier.width(Spacing.md))
        // Right counter pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(t.surface2)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            LLText(
                "${index + 1}/${slides.size}",
                color = t.ink400, size = 12.sp, weight = FontWeight.SemiBold,
            )
        }
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
