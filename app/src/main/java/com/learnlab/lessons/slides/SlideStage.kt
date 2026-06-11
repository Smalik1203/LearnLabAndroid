package com.learnlab.lessons.slides

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.learnlab.design.LLAnimation
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.design.Spacing
import com.learnlab.design.SpeakControls
import com.learnlab.design.bounceClickable
import com.learnlab.design.rememberReadAloud
import com.learnlab.lessons.lessonPalette

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

    val readAloud = rememberReadAloud()
    // Paging to another slide stops narration so we never read the previous
    // slide while a new one is on screen.
    LaunchedEffect(index, chapter.id) { readAloud.stop() }

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
            val t = LL.tokens
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
                    modifier = Modifier.fillMaxSize()
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

                // Stylized corner page counter overlay
                if (!isEditing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 48.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        androidx.compose.material3.Text(
                            text = "${index + 1}",
                            color = t.amber700,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = com.learnlab.design.LearnLabFonts.Display
                        )
                        androidx.compose.material3.Text(
                            text = " / ${slides.size}",
                            color = t.ink400,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp),
                            fontFamily = com.learnlab.design.LearnLabFonts.Display
                        )
                    }
                }
            }
            BottomStrip(
                chapter = chapter,
                slides = slides,
                index = index,
                onJump = { index = it },
                spokenText = slides[index].spokenText(),
                readAloud = readAloud,
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
    val barBg = if (t.isDark) t.surface.copy(alpha = 0.45f) else t.surface.copy(alpha = 0.85f)
    val barBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(barBg)
                .border(1.dp, barBorder, RoundedCornerShape(Radius.md))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBtn(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Close",
                onClick = onBack,
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                LLText(
                    "CH ${chapter.chapter.number} · ${chapter.chapter.title}",
                    color = t.ink50, size = 15.sp, weight = FontWeight.Bold, maxLines = 1,
                )
                if (currentSlide.sectionNumber != null && currentSlide.sectionTitle != null) {
                    LLText(
                        "${currentSlide.sectionNumber}  ${currentSlide.sectionTitle}",
                        color = t.ink500, size = 12.sp, maxLines = 1, weight = FontWeight.Medium
                    )
                }
            }



            if (isEditing) {
                IconBtn(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    onClick = onUndo,
                    enabled = canUndo,
                )
                Spacer(Modifier.width(8.dp))
                IconBtn(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    label = "Redo",
                    onClick = onRedo,
                    enabled = canRedo,
                )
                Spacer(Modifier.width(8.dp))
            }

            IconBtn(
                icon = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                label = if (isEditing) "Done editing" else "Edit slide",
                onClick = onToggleEdit,
                highlighted = isEditing,
            )
            Spacer(Modifier.width(8.dp))
            IconBtn(
                icon = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                label = if (isDark) "Switch to light mode" else "Switch to dark mode",
                onClick = onToggleTheme,
            )
            Spacer(Modifier.width(8.dp))
            IconBtn(icon = Icons.Filled.GridView, label = "Outline", onClick = onOutline)
        }
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
        !enabled -> t.surface2.copy(alpha = 0.15f)
        highlighted -> t.accent500
        else -> t.surface2.copy(alpha = 0.4f)
    }
    val tint = when {
        !enabled -> t.ink600
        highlighted -> Color.White
        else -> t.ink200
    }
    val borderStroke = if (highlighted) {
        BorderStroke(1.dp, t.accent300)
    } else {
        BorderStroke(1.dp, t.line.copy(alpha = 0.4f))
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg)
            .border(borderStroke, CircleShape)
            .bounceClickable(enabled = enabled, onClick = onClick),
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
    spokenText: String,
    readAloud: com.learnlab.design.ReadAloudController,
) {
    val t = LL.tokens
    val p = lessonPalette()

    val barBg = if (t.isDark) t.surface.copy(alpha = 0.45f) else t.surface.copy(alpha = 0.85f)
    val barBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(barBg)
                .border(1.dp, barBorder, RoundedCornerShape(Radius.md))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (spokenText.isNotBlank()) {
                SpeakControls(text = spokenText, controller = readAloud)
                Spacer(Modifier.width(16.dp))
            }

            // Left Navigation Button: Previous
            val prevEnabled = index > 0
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.md))
                    .background(if (prevEnabled) t.surface2.copy(alpha = 0.4f) else Color.Transparent)
                    .border(
                        BorderStroke(
                            1.dp, 
                            if (prevEnabled) t.line.copy(alpha = 0.4f) else t.line.copy(alpha = 0.1f)
                        ), 
                        RoundedCornerShape(Radius.md)
                    )
                    .then(
                        if (prevEnabled) Modifier.bounceClickable { onJump(index - 1) }
                        else Modifier
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = if (prevEnabled) t.ink200 else t.ink600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    LLText(
                        "Previous",
                        color = if (prevEnabled) t.ink200 else t.ink600,
                        size = 14.sp,
                        weight = FontWeight.Bold
                    )
                }
            }

            // Center Pagination Timeline Rail
            TimelineRail(
                slides = slides,
                currentIndex = index,
                onJump = onJump,
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
            )

            // Right Navigation Button: Next
            val nextEnabled = index < slides.lastIndex
            val nextBg = if (nextEnabled) t.amber700 else t.surface2.copy(alpha = 0.15f)
            val nextTint = if (nextEnabled) Color.White else t.ink600
            val nextBorder = if (nextEnabled) BorderStroke(1.dp, t.amber700) else BorderStroke(1.dp, t.line.copy(alpha = 0.1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.md))
                    .background(nextBg)
                    .border(nextBorder, RoundedCornerShape(Radius.md))
                    .then(
                        if (nextEnabled) Modifier.bounceClickable { onJump(index + 1) }
                        else Modifier
                    )
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LLText(
                        "Next",
                        color = nextTint,
                        size = 14.sp,
                        weight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = nextTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRail(
    slides: List<Slide>,
    currentIndex: Int,
    onJump: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = LL.tokens
    val p = lessonPalette()
    
    // Identify section positions dynamically
    val sectionPositions = remember(slides) {
        val map = mutableMapOf<Int, String>()
        var lastSec: String? = null
        slides.forEachIndexed { idx, slide ->
            val sec = slide.sectionNumber
            if (sec != null && sec != lastSec) {
                val count = map.size + 1
                val formatted = if (count < 10) "0$count" else "$count"
                map[idx] = formatted
                lastSec = sec
            }
        }
        map
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        slides.forEachIndexed { i, slide ->
            val secLabel = sectionPositions[i]
            if (secLabel != null) {
                LLText(
                    text = secLabel,
                    color = if (i <= currentIndex) t.amber700 else t.ink500,
                    size = 12.sp,
                    weight = FontWeight.Black,
                    modifier = Modifier
                        .bounceClickable { onJump(i) }
                        .padding(horizontal = 6.dp)
                )
                Spacer(Modifier.width(4.dp))
            }

            val isCurrent = i == currentIndex
            val isBefore = i < currentIndex
            
            val targetWidth = if (isCurrent) 28.dp else 12.dp
            val targetHeight = if (isCurrent) 8.dp else 4.dp
            val width by animateDpAsState(
                targetValue = targetWidth,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "lineWidth"
            )
            val height by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "lineHeight"
            )

            val color = when {
                isCurrent -> t.amber700
                isBefore -> t.amber700.copy(alpha = 0.5f)
                slide.layout == SlideLayout.ActivityLaunch -> p.emerald.accent.copy(alpha = 0.7f)
                else -> t.surface3.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .size(width = width, height = height)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(color)
                    .then(
                        if (isCurrent) Modifier.shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(Radius.pill),
                            ambientColor = t.amber700.copy(alpha = 0.4f),
                            spotColor = t.amber700.copy(alpha = 0.8f)
                        ) else Modifier
                    )
                    .bounceClickable { onJump(i) }
            )

            if (i < slides.lastIndex) {
                Spacer(Modifier.width(6.dp))
            }
        }

        Spacer(Modifier.width(8.dp))
        val finalSlideIsActivity = slides.lastOrNull()?.layout == SlideLayout.ActivityLaunch
        val sparkColor = if (currentIndex == slides.lastIndex) p.emerald.accent else t.ink500
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(sparkColor.copy(alpha = 0.15f))
                .border(BorderStroke(1.dp, sparkColor.copy(alpha = 0.4f)), CircleShape)
                .bounceClickable { onJump(slides.lastIndex) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (finalSlideIsActivity) Icons.Filled.Science else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = sparkColor,
                modifier = Modifier.size(10.dp)
            )
        }
    }
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
    val drawerBg = if (t.isDark) t.surface.copy(alpha = 0.65f) else t.surface.copy(alpha = 0.9f)
    val drawerBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose),
    )
    // Drawer
    Row(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(440.dp)
                .background(drawerBg)
                .border(1.dp, drawerBorder, RoundedCornerShape(0.dp))
                .shadow(elevation = 24.dp)
                .padding(Spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LLText("OUTLINE", color = t.accent700, size = 11.sp, weight = FontWeight.Bold, letterSpacing = 1.6.sp)
                    LLText(chapter.chapter.title, color = t.ink50, size = 18.sp, weight = FontWeight.ExtraBold, maxLines = 1)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(t.surface2.copy(alpha = 0.4f))
                        .border(1.dp, t.line.copy(alpha = 0.3f), CircleShape)
                        .bounceClickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close, 
                        contentDescription = "Close outline",
                        tint = t.ink200, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(Spacing.xl))
            // Only show section start slides + cover entries so the list isn't 40 items long
            val markers = remember(slides) {
                val list = mutableListOf<Pair<Int, Slide>>()
                val coverIdx = slides.indexOfFirst { it.layout == SlideLayout.Cover }
                if (coverIdx >= 0) {
                    list.add(coverIdx to slides[coverIdx])
                }
                var lastSec: String? = null
                slides.forEachIndexed { idx, s ->
                    val sec = s.sectionNumber
                    if (sec != null && sec != lastSec) {
                        list.add(idx to s)
                        lastSec = sec
                    }
                }
                list
            }
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
    val rowBg = if (isCurrent) t.accent50.copy(alpha = 0.15f) else Color.Transparent
    val borderStroke = if (isCurrent) {
        BorderStroke(1.dp, t.accent300)
    } else {
        BorderStroke(1.dp, Color.Transparent)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(rowBg)
            .border(borderStroke, RoundedCornerShape(Radius.sm))
            .bounceClickable { onJump() }
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (slide.layout == SlideLayout.Cover) {
            LLText("Cover / Title slide", color = if (isCurrent) t.accent700 else t.ink50, size = 14.sp, weight = FontWeight.Bold)
        } else {
            LLText(
                slide.sectionNumber ?: "·",
                color = t.accent700, 
                size = 13.sp, 
                weight = FontWeight.Black, 
                modifier = Modifier.width(44.dp)
            )
            LLText(
                slide.sectionTitle ?: "(untitled)",
                color = if (isCurrent) t.ink50 else t.ink200, 
                size = 14.sp, 
                weight = FontWeight.SemiBold
            )
        }
    }
}
