package com.learnlab.ui.slideshow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
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
import com.learnlab.content.Chapters
import com.learnlab.content.TextbookItem
import com.learnlab.content.slidesFor
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PillTabBar
import com.learnlab.design.PrimaryButton
import com.learnlab.store.AppState

/**
 * One chapter, with reading and experiments kept separate: a "Read" tab (the chapter's slide
 * deck, reading only) and an "Experiments" tab (the chapter's runnable experiments). Reached
 * from the chapter grid.
 */
@Composable
fun ChapterHubScreen(
    state: AppState,
    chapterId: String,
    onRunExperiment: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val chapter = remember(chapterId) { Chapters.firstOrNull { it.id == chapterId } }
    val title = chapter?.title ?: "Chapter"
    val experiments = chapter?.experiments ?: emptyList()
    val readingItems = remember(chapterId, title) {
        slidesFor(chapterId).map { TextbookItem(title, it) }
    }
    val readingPager = rememberPagerState(pageCount = { readingItems.size })

    var tab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(t.bg)) {
        TextbookNav(state, title = title, onBack = onBack, onHome = onHome)

        Box(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            PillTabBar(
                tabs = listOf("Read", "Experiments"),
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
            )
        }

        if (tab == 0) {
            if (readingItems.isEmpty()) {
                EmptyHub("No reading content for this chapter yet.")
            } else {
                TextbookDeck(
                    items = readingItems,
                    pagerState = readingPager,
                    onRunExperiment = onRunExperiment,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        } else {
            if (experiments.isEmpty()) {
                EmptyHub("No experiments for this chapter yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 40.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(experiments, key = { it.id }) { exp ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 900.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(t.surface)
                                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                                .padding(22.dp),
                        ) {
                            LLText("EXPERIMENT", color = t.accent500, size = 11.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(8.dp))
                            LLText(exp.title, color = t.ink50, size = 20.sp, weight = FontWeight.Bold, lineHeight = 26.sp)
                            if (exp.blurb.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                LLText(exp.blurb, color = t.ink200, size = 14.sp, lineHeight = 20.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(label = "Run experiment ›", onClick = { onRunExperiment(exp.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHub(message: String) {
    val t = LL.tokens
    Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
        LLText(message, color = t.ink400, size = 15.sp)
    }
}
