package com.learnlab.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learnlab.content.findExperiment
import com.learnlab.design.Card
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.RecentEntry

/**
 * History popup: the 5 most recently opened experiments, with a "View more →"
 * link (bottom-right) into the full history page.
 */
@Composable
fun HistoryDialog(
    recents: List<RecentEntry>,
    onDismiss: () -> Unit,
    onOpenExperiment: (String) -> Unit,
    onViewMore: () -> Unit,
) {
    val t = LL.tokens
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText("Recent experiments", color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = t.ink400, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (recents.isEmpty()) {
                    LLText("No experiments opened yet.", color = t.ink500, size = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recents.take(5).forEach { entry ->
                            val exp = findExperiment(entry.id)
                            HistoryRow(
                                title = exp?.title ?: entry.id,
                                source = exp?.source,
                                onClick = { onOpenExperiment(entry.id) },
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onViewMore)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            LLText("View more →", color = t.accent600, size = 14.sp, weight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(title: String, source: String?, onClick: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        LLText(title, color = t.ink50, size = 15.sp, weight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (source != null) {
            Spacer(Modifier.height(2.dp))
            LLText(source, color = t.ink400, size = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
