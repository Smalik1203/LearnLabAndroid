package com.learnlab.ui.slideshow

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
import com.learnlab.content.textbookDeck
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.SecondaryButton
import com.learnlab.store.AppState
import kotlinx.coroutines.launch

/**
 * The continuous "textbook" for a grade: one swipeable deck of every authored
 * chapter's slides (in order), with each chapter's experiments embedded as inline
 * slides. No chapter/topic selection — you just flip through.
 */
@Composable
fun TextbookScreen(
    state: AppState,
    grade: Int,
    onRunExperiment: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val items = remember(grade) { textbookDeck(grade) }

    if (items.isEmpty()) {
        Column(Modifier.fillMaxSize().background(t.bg)) {
            TextbookNav(state, title = "Grade $grade", onBack = onBack, onHome = onHome)
            Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
                LLText("No textbook content yet.", color = t.ink400, size = 15.sp)
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage.coerceIn(0, items.size - 1)

    Column(Modifier.fillMaxSize().background(t.bg)) {
        TextbookNav(state, title = items[page].chapterTitle, onBack = onBack, onHome = onHome)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f).background(t.bg),
        ) { i ->
            SlideView(items[i].slide, i, onRunExperiment = onRunExperiment)
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
                ProgressBar(value = (page + 1).toFloat() / items.size, modifier = Modifier.width(140.dp))
                Spacer(Modifier.width(12.dp))
                LLText("${page + 1} / ${items.size}", color = t.ink400, size = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SecondaryButton(
                    label = "‹ Previous",
                    onClick = { scope.launch { pagerState.animateScrollToPage(page - 1) } },
                    enabled = page > 0,
                )
                Spacer(Modifier.width(10.dp))
                PrimaryButton(
                    label = "Next ›",
                    onClick = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
                    enabled = page < items.size - 1,
                )
            }
        }
    }
}

/** Top nav: Back + current chapter title (left), Home + theme (right). */
@Composable
private fun TextbookNav(
    state: AppState,
    title: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
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
                    contentDescription = "Back",
                    tint = t.ink200,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                LLText("Back", color = t.ink200, size = 14.sp, weight = FontWeight.Medium)
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
