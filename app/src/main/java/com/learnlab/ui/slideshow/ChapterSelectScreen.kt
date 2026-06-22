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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapter
import com.learnlab.content.textbookChapters
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.AppState

private const val COLS = 5

/**
 * Chapter picker for a grade: large rounded cards that stack and resize to fill the whole page
 * (no scrolling), matching the grade-selection screen's card design. Tapping opens the chapter hub.
 */
@Composable
fun ChapterSelectScreen(
    state: AppState,
    grade: Int,
    onOpenChapter: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val chapters = remember(grade) { textbookChapters(grade) }

    Column(Modifier.fillMaxSize().background(t.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.bg)
                .padding(horizontal = 28.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                LLText("Science · Grade $grade", color = t.ink50, size = 15.sp, weight = FontWeight.SemiBold)
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

        if (chapters.isEmpty()) {
            Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
                LLText("No chapters yet.", color = t.ink400, size = 15.sp)
            }
            return@Column
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column {
                LLText("Choose a chapter", color = t.ink50, size = 28.sp, weight = FontWeight.Bold)
                LLText(
                    "Pick a chapter to open its reading and experiments.",
                    color = t.ink400, size = 14.sp,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                chapters.chunked(COLS).forEach { rowChapters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        rowChapters.forEach { ch ->
                            ChapterCard(ch, Modifier.weight(1f).height(150.dp)) { onOpenChapter(ch.id) }
                        }
                        // Keep card widths uniform when the last row is short.
                        repeat(COLS - rowChapters.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(ch: Chapter, modifier: Modifier, onClick: () -> Unit) {
    val t = LL.tokens
    val accent = t.accent500
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(t.surface)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LLText("${ch.number}", color = accent, size = 28.sp, weight = FontWeight.ExtraBold)
            LLText(ch.title, color = t.ink400, size = 12.sp, lineHeight = 16.sp)
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
