package com.learnlab.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learnlab.content.LessonBlock

/**
 * Renders a list of [LessonBlock]s as a scrollable, magazine-style page.
 * Page is painted with [LessonPalette.pageBgBrush] (warm cream in light,
 * deep slate in dark). Content width capped at 760dp for readability on
 * big IFP screens.
 */
@Composable
fun LessonRenderer(blocks: List<LessonBlock>, modifier: Modifier = Modifier) {
    val p = lessonPalette()
    Box(modifier = modifier.fillMaxSize().background(p.pageBgBrush)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(blocks) { block ->
                Column(modifier = Modifier.widthIn(max = 760.dp)) {
                    LessonBlockView(block)
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun LessonBlockView(block: LessonBlock) {
    when (block) {
        is LessonBlock.Heading      -> HeadingBlockView(block)
        is LessonBlock.Paragraph    -> ParagraphBlockView(block)
        is LessonBlock.KeyTerm      -> KeyTermBlockView(block)
        is LessonBlock.Quote        -> QuoteBlockView(block)
        is LessonBlock.CharacterSay -> CharacterSayBlockView(block)
        is LessonBlock.Callout      -> CalloutBlockView(block)
        is LessonBlock.Question     -> QuestionBlockView(block)
        is LessonBlock.Figure       -> FigureBlockView(block)
        is LessonBlock.CaseStudy    -> CaseStudyBlockView(block)
        is LessonBlock.Spacer       -> Spacer(Modifier.height(block.sizeDp.dp))
    }
}
