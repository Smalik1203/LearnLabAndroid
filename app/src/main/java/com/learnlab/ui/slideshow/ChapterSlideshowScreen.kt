package com.learnlab.ui.slideshow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapters
import com.learnlab.content.slidesFor
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.SecondaryButton
import com.learnlab.store.AppState
import kotlinx.coroutines.launch

/**
 * Pre-experiment deck for a chapter: a swipeable set of generated, designed
 * slides built from the chapter's textbook content (see [slidesFor]), shown
 * before the experiments.
 */
@Composable
fun ChapterSlideshowScreen(
    state: AppState,
    chapterId: String,
    onStartExperiments: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val chapter = remember(chapterId) { Chapters.firstOrNull { it.id == chapterId } }
    val slides = remember(chapterId) { slidesFor(chapterId) }

    // No deck authored → nothing to show; go straight to the experiments.
    if (slides.isEmpty()) {
        LaunchedEffect(chapterId) { onStartExperiments() }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        SlideshowNav(
            state = state,
            title = chapter?.title ?: "Chapter",
            onBack = onBack,
            onHome = onHome,
            onSkip = onStartExperiments,
        )
        SlideshowBody(slideCount = slides.size, onStartExperiments = onStartExperiments) { index ->
            SlideView(slides[index], index)
        }
    }
}

@Composable
private fun ColumnScope.SlideshowBody(
    slideCount: Int,
    onStartExperiments: () -> Unit,
    slide: @Composable (Int) -> Unit,
) {
    val t = LL.tokens
    val pagerState = rememberPagerState(pageCount = { slideCount })
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage
    val isLast = page == slideCount - 1

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().weight(1f).background(t.bg),
    ) { index ->
        slide(index)
    }

    Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.surface)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressBar(value = (page + 1).toFloat() / slideCount, modifier = Modifier.width(140.dp))
            Spacer(Modifier.width(12.dp))
            LLText("${page + 1} / $slideCount", color = t.ink400, size = 13.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SecondaryButton(
                label = "‹ Previous",
                onClick = { scope.launch { pagerState.animateScrollToPage(page - 1) } },
                enabled = page > 0,
            )
            Spacer(Modifier.width(10.dp))
            if (isLast) {
                PrimaryButton(label = "Start experiments ›", onClick = onStartExperiments)
            } else {
                PrimaryButton(
                    label = "Next ›",
                    onClick = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                )
            }
        }
    }
}

/** Top nav: Back to Curriculum + chapter title (left), Skip + Home + theme (right). */
@Composable
private fun SlideshowNav(
    state: AppState,
    title: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSkip: () -> Unit,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.bg)
            .padding(horizontal = 28.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Curriculum",
                    tint = t.ink200,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                LLText("Back to Curriculum", color = t.ink200, size = 14.sp, weight = FontWeight.Medium)
            }
            Spacer(Modifier.width(16.dp))
            LLText(
                title,
                color = t.ink50,
                size = 15.sp,
                weight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            GhostButton(label = "Skip to experiments", onClick = onSkip)
            Spacer(Modifier.width(10.dp))
            CircleIconButton(Icons.Filled.Home, "Home", onHome)
            Spacer(Modifier.width(10.dp))
            CircleIconButton(
                if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                "Toggle theme",
            ) { state.toggleTheme() }
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(t.surface2)
            .border(1.dp, t.line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = t.ink400, modifier = Modifier.size(16.dp))
    }
}
