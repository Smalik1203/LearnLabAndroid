package com.learnlab.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.Chapters
import com.learnlab.content.Experiment
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.AppState

/**
 * Collapsible left rail.
 *  - Expanded (280dp): chapter sections + experiment items.
 *  - Collapsed (40dp): a thin strip with a single chevron button to re-open.
 */
@Composable
fun ExperimentRail(state: AppState, modifier: Modifier = Modifier) {
    val t = LL.tokens
    val railOpen by state.railOpen
    val width by animateDpAsState(if (railOpen) 280.dp else 40.dp, label = "rail-w")

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(0.dp)),
    ) {
        if (railOpen) {
            ExpandedRail(state = state, onCollapse = { state.toggleRail() })
        } else {
            CollapsedRail(onExpand = { state.toggleRail() })
        }
    }
}

@Composable
private fun CollapsedRail(onExpand: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onExpand() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface2),
            contentAlignment = Alignment.Center,
        ) {
            LLText("›", color = t.ink400, size = 18.sp, weight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        // Vertical "EXPERIMENTS" hint
        LLText(
            "EXPERIMENTS",
            color = t.ink500, size = 9.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
            modifier = Modifier.rotate(-90f),
        )
    }
}

@Composable
private fun ExpandedRail(state: AppState, onCollapse: () -> Unit) {
    val t = LL.tokens
    val currentId = state.currentExperimentId.value
    val currentChapter = Chapters.firstOrNull { ch -> ch.experiments.any { it.id == currentId } }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            Chapters.forEach { ch ->
                put(ch.id, ch.id == (currentChapter?.id ?: Chapters.firstOrNull()?.id))
            }
        }
    }
    if (currentChapter != null && expanded[currentChapter.id] != true) {
        expanded[currentChapter.id] = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with collapse button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LLText(
                    "EXPERIMENTS",
                    color = t.ink500, size = 10.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
                )
                Spacer(Modifier.height(2.dp))
                LLText(
                    "${Chapters.sumOf { it.experiments.size }} across ${Chapters.size} chapters",
                    color = t.ink400, size = 11.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(t.surface2)
                    .clickable { onCollapse() },
                contentAlignment = Alignment.Center,
            ) {
                LLText("‹", color = t.ink400, size = 18.sp, weight = FontWeight.Bold)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Chapters.forEach { chapter ->
                item(key = "header-${chapter.id}") {
                    ChapterHeader(
                        chapter = chapter,
                        isOpen = expanded[chapter.id] == true,
                        hasActive = chapter.id == currentChapter?.id,
                        onToggle = { expanded[chapter.id] = expanded[chapter.id] != true },
                    )
                }
                if (expanded[chapter.id] == true) {
                    items(
                        count = chapter.experiments.size,
                        key = { i -> "exp-${chapter.experiments[i].id}" },
                    ) { i ->
                        val exp = chapter.experiments[i]
                        ExperimentItem(
                            experiment = exp,
                            active = currentId == exp.id,
                            onSelect = { state.select(exp.id) },
                        )
                    }
                    item(key = "spacer-${chapter.id}") { Spacer(Modifier.height(4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ChapterHeader(
    chapter: Chapter,
    isOpen: Boolean,
    hasActive: Boolean,
    onToggle: () -> Unit,
) {
    val t = LL.tokens
    val rot by animateFloatAsState(if (isOpen) 90f else 0f, label = "chev")
    val bg by animateColorAsState(
        if (hasActive) t.accent50 else androidx.compose.ui.graphics.Color.Transparent,
        label = "chh",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(14.dp).rotate(rot),
            contentAlignment = Alignment.Center,
        ) {
            LLText("▸", color = t.ink500, size = 11.sp, weight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            LLText(
                "CH ${chapter.number}",
                color = if (hasActive) t.accent700 else t.ink500,
                size = 9.sp,
                weight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
            )
            LLText(
                chapter.title,
                color = if (hasActive) t.accent700 else t.ink50,
                size = 12.sp,
                weight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LLText(
            "${chapter.experiments.size}",
            color = t.ink500, size = 11.sp,
        )
    }
}

@Composable
private fun ExperimentItem(experiment: Experiment, active: Boolean, onSelect: () -> Unit) {
    val t = LL.tokens
    val bg by animateColorAsState(if (active) t.accent50 else t.surface, label = "rail-bg")
    val border = if (active) t.accent500 else androidx.compose.ui.graphics.Color.Transparent
    val titleColor = if (active) t.accent700 else t.ink200

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 4.dp, top = 1.dp, bottom = 1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(
            experiment.title,
            color = titleColor, size = 12.sp,
            weight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
