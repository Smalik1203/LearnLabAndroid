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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.textbookChapters
import com.learnlab.design.DarkTokens
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.LocalTokens
import com.learnlab.design.PaperTokens
import com.learnlab.design.Serif
import com.learnlab.store.AppState

/** Themed list of a grade's textbook chapters; tap one to open its reader. */
@Composable
fun ChapterListScreen(
    state: AppState,
    grade: Int,
    onOpenChapter: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    CompositionLocalProvider(LocalTokens provides if (state.readerDark.value) DarkTokens else PaperTokens) {
        val t = LL.tokens
        val chapters = remember(grade) { textbookChapters(grade) }
        Column(Modifier.fillMaxSize().background(t.bg)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(t.surface).padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(t.surface2).border(1.dp, t.line, CircleShape).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = t.ink200, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(14.dp))
                    LLText("Science · Grade $grade", color = t.ink50, size = 20.sp, weight = FontWeight.Bold, fontFamily = Serif)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(t.surface2).border(1.dp, t.line, CircleShape).clickable { state.toggleReaderTheme() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (state.readerDark.value) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            "Toggle reading theme", tint = t.ink200, modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(t.surface2).border(1.dp, t.line, CircleShape).clickable(onClick = onHome),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.Home, "Home", tint = t.ink200, modifier = Modifier.size(18.dp)) }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 40.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.widthIn(max = 820.dp).fillMaxWidth()) {
                    LLText("In this textbook", color = t.ink400, size = 13.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(20.dp))
                    chapters.forEach { ch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(t.surface)
                                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                                .clickable { onOpenChapter(ch.id) }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(t.accent50),
                                contentAlignment = Alignment.Center,
                            ) { LLText("${ch.number}", color = t.accent500, size = 20.sp, weight = FontWeight.Bold, fontFamily = Serif) }
                            Spacer(Modifier.width(18.dp))
                            Column(Modifier.weight(1f)) {
                                LLText(ch.title, color = t.ink50, size = 19.sp, weight = FontWeight.Bold, fontFamily = Serif, lineHeight = 24.sp)
                                Spacer(Modifier.height(4.dp))
                                LLText(ch.description, color = t.ink400, size = 13.sp, lineHeight = 18.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = t.ink400, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
