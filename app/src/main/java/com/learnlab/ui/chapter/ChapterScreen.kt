package com.learnlab.ui.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterLoader
import com.learnlab.content.chapter.ChapterPaths
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.Slide
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.lessons.slides.SlidePlanner
import com.learnlab.lessons.slides.SlideStage
import com.learnlab.store.AppState

@Composable
fun ChapterScreen(
    state: AppState,
    chapterId: String,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    val t = LL.tokens
    val ctx = LocalContext.current
    var chapter by remember(chapterId) { mutableStateOf<Chapter?>(null) }
    var slides by remember(chapterId) { mutableStateOf<List<Slide>>(emptyList()) }
    var cast by remember { mutableStateOf<List<Character>>(emptyList()) }
    var error by remember(chapterId) { mutableStateOf<String?>(null) }

    LaunchedEffect(chapterId) {
        try {
            val path = chapterPathFor(chapterId)
                ?: throw IllegalArgumentException("No chapter path mapped for id '$chapterId'")
            val loaded = ChapterLoader.loadChapter(ctx, path)
            chapter = loaded
            slides = SlidePlanner.plan(loaded)
            cast = ChapterLoader.loadCast(ctx).characters
        } catch (e: Throwable) {
            error = e.message ?: e::class.simpleName
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(t.bg)) {
        when {
            error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LLText("Couldn't load chapter", color = t.ink50, size = 18.sp)
                LLText(error ?: "", color = t.ink400, size = 12.sp)
            }
            chapter == null || slides.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LLText("Preparing slides…", color = t.ink500, size = 14.sp)
            }
            else -> SlideStage(
                chapter = chapter!!,
                slides = slides,
                cast = cast.associateBy { it.id },
                onBack = onBack,
                onOpenActivity = onOpenActivity,
            )
        }
    }
}

private fun chapterPathFor(chapterId: String): String? = when (chapterId) {
    "g6-sci-ch02" -> ChapterPaths.G6_SCIENCE_CH02
    else -> null
}
