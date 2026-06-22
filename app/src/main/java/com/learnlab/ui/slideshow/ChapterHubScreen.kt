package com.learnlab.ui.slideshow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapters
import com.learnlab.content.TextbookItem
import com.learnlab.content.slidesFor
import com.learnlab.design.LL
import com.learnlab.design.LLText
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

    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(t.bg)) {
        TextbookNav(state, title = title, onBack = onBack, onHome = onHome)

        Box(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            ReadExperimentTabs(selected = tab, onSelect = { tab = it })
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
private fun ReadExperimentTabs(selected: Int, onSelect: (Int) -> Unit) {
    val t = LL.tokens
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        listOf("Read", "Experiments").forEachIndexed { i, label ->
            val active = i == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(i) }
                    .width(IntrinsicSize.Max)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                LLText(
                    label,
                    color = if (active) t.accent500 else t.ink400,
                    size = 15.sp,
                    weight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) t.accent500 else Color.Transparent),
                )
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
