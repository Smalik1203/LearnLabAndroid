package com.learnlab.ui.slideshow

import android.speech.tts.TextToSpeech
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
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.ChapterSlide
import com.learnlab.content.findExperiment
import com.learnlab.content.textbookDeck
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.SecondaryButton
import com.learnlab.store.AppState
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * The continuous "textbook" for a grade: one swipeable deck of every authored
 * chapter's slides (in order), with each chapter's experiments embedded as inline
 * slides. No chapter/topic selection — you just flip through. A Read-aloud button
 * speaks the current slide via the platform TextToSpeech engine.
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

    // Read-aloud: a single TextToSpeech instance, released with the screen.
    val ctx = LocalContext.current
    var speaking by remember { mutableStateOf(false) }
    val tts = remember {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) engine?.language = Locale.UK
        }
        engine
    }
    DisposableEffect(Unit) { onDispose { tts?.stop(); tts?.shutdown() } }

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
                ReadAloudButton(
                    speaking = speaking,
                    onClick = {
                        if (speaking) {
                            tts?.stop(); speaking = false
                        } else {
                            val text = slideSpeech(items[page].slide)
                            if (text.isNotBlank()) {
                                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "slide")
                                speaking = true
                            }
                        }
                    },
                )
                Spacer(Modifier.width(12.dp))
                SecondaryButton(
                    label = "‹ Previous",
                    onClick = { speaking = false; tts?.stop(); scope.launch { pagerState.animateScrollToPage(page - 1) } },
                    enabled = page > 0,
                )
                Spacer(Modifier.width(10.dp))
                PrimaryButton(
                    label = "Next ›",
                    onClick = { speaking = false; tts?.stop(); scope.launch { pagerState.animateScrollToPage(page + 1) } },
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
private fun ReadAloudButton(speaking: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (speaking) t.accent500 else t.surface2)
            .border(1.dp, t.accent500.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (speaking) Icons.Filled.Stop else Icons.Filled.RecordVoiceOver,
            contentDescription = "Read aloud",
            tint = if (speaking) Color.White else t.accent500,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(7.dp))
        LLText(
            if (speaking) "Stop" else "Read aloud",
            color = if (speaking) Color.White else t.accent500,
            size = 13.sp,
            weight = FontWeight.SemiBold,
        )
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

/** Plain text of a slide, for the Read-aloud button. */
private fun slideSpeech(s: ChapterSlide): String = when (s) {
    is ChapterSlide.Title -> s.title + ". " + s.subtitle + " " + s.points.joinToString(" ")
    is ChapterSlide.Concept -> s.title + ". " + s.body.clean()
    is ChapterSlide.Steps -> s.title + ". " + s.steps.joinToString(". ") { it.replace(" — ", ": ") }
    is ChapterSlide.Activity -> s.title + ". " + s.purpose.clean() + " " +
        s.steps.joinToString(". ") { it.replace(" — ", ": ") } + ". " + s.observe.clean()
    is ChapterSlide.Quote -> s.translation.clean() + " " + s.attribution
    is ChapterSlide.Split -> s.title + ". " + s.leftTitle + ": " + s.leftBody.clean() + ". " + s.rightTitle + ": " + s.rightBody.clean()
    is ChapterSlide.Chips -> s.title + ". " + s.body + " " + s.chips.joinToString(", ")
    is ChapterSlide.Table -> s.title + ". " + s.rows.joinToString(". ") { it.joinToString(", ") }
    is ChapterSlide.Figure -> s.title + ". " + s.caption.clean()
    is ChapterSlide.Interactive -> s.title + ". " + s.caption.clean()
    is ChapterSlide.SectionHeader -> "Section ${s.number}. ${s.title}. " + s.intro.clean()
    is ChapterSlide.Scene -> s.body.clean()
    is ChapterSlide.Closing -> s.title + ". " + s.subtitle
    is ChapterSlide.Experiment -> findExperiment(s.experimentId)?.let { it.title + ". " + it.blurb } ?: ""
}

private fun String.clean(): String = replace("**", "")
