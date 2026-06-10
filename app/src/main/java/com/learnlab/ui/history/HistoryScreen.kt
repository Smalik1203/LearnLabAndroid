package com.learnlab.ui.history

import android.text.format.DateUtils
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.findExperiment
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

/** Complete history of opened experiments, newest first. */
@Composable
fun HistoryScreen(
    state: AppState,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onOpenExperiment: (String) -> Unit,
) {
    val t = LL.tokens
    val recents by state.recents

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        TopBar(state = state, title = "History", showBack = true, onBack = onBack, onHomeClick = onHome)

        if (recents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LLText("No experiments opened yet.", color = t.ink500, size = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Column {
                        LLText("Your experiment history", color = t.ink50, size = 24.sp, weight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                items(recents, key = { it.id }) { entry ->
                    val exp = findExperiment(entry.id)
                    HistoryCard(
                        title = exp?.title ?: entry.id,
                        source = exp?.source,
                        whenText = DateUtils.getRelativeTimeSpanString(
                            entry.openedAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                        ).toString(),
                        onClick = { onOpenExperiment(entry.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(title: String, source: String?, whenText: String, onClick: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        LLText(title, color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText(
                source ?: "",
                color = t.ink400, size = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            LLText(whenText, color = t.ink500, size = 12.sp, weight = FontWeight.Medium)
        }
    }
}
