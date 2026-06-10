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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learnlab.content.AllExperiments
import com.learnlab.design.Card
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.LLTextField

/**
 * Search modal: type a keyword, see matching experiments (title/blurb), tap one
 * to open it. Reuses the shared Card frame + LLTextField input.
 */
@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onOpenExperiment: (String) -> Unit,
) {
    val t = LL.tokens
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        val q = query.trim()
        if (q.isBlank()) emptyList()
        else AllExperiments.filter {
            it.title.contains(q, ignoreCase = true) || it.blurb.contains(q, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText("Search experiments", color = t.ink50, size = 20.sp, weight = FontWeight.Bold)
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
                LLTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search by topic or keyword…",
                    leadingIcon = Icons.Filled.Search,
                )
                Spacer(Modifier.height(14.dp))
                when {
                    query.isBlank() ->
                        LLText("Type to find an experiment.", color = t.ink500, size = 14.sp)
                    results.isEmpty() ->
                        LLText("No experiments match “${query.trim()}”.", color = t.ink500, size = 14.sp)
                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(results, key = { it.id }) { exp ->
                            ResultRow(
                                title = exp.title,
                                blurb = exp.blurb,
                                onClick = { onOpenExperiment(exp.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(title: String, blurb: String, onClick: () -> Unit) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        LLText(title, color = t.ink50, size = 15.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        LLText(blurb, color = t.ink400, size = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
