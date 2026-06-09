package com.learnlab.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learnlab.content.Subject
import com.learnlab.content.gradesFor
import com.learnlab.content.topicsFor
import com.learnlab.design.LL
import com.learnlab.design.LLText

/**
 * Web subject → topics modal: pick a Grade, then a Topic, then jump straight
 * into that experiment.
 */
@Composable
fun SubjectTopicsModal(
    subject: Subject,
    onDismiss: () -> Unit,
    onTopic: (String) -> Unit,
) {
    val t = LL.tokens
    val grades = remember(subject) { gradesFor(subject) }
    var grade by remember(subject) { mutableIntStateOf(grades.firstOrNull() ?: -1) }
    val topics = remember(subject, grade) { if (grade >= 0) topicsFor(subject, grade) else emptyList() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(20.dp))
                .padding(28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(subject.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    LLText(subject.displayName, color = t.ink50, size = 24.sp, weight = FontWeight.Bold)
                }
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
            Spacer(Modifier.height(4.dp))
            LLText("Choose a topic to explore", color = t.ink400, size = 15.sp)

            Spacer(Modifier.height(24.dp))
            LLText("Grade", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (grades.isEmpty()) {
                Dropdown(label = "Coming soon", enabled = false, items = emptyList<String>(), onSelect = {})
            } else {
                Dropdown(
                    label = if (grade >= 0) "Grade $grade" else "Select the Grade",
                    enabled = true,
                    items = grades.map { "Grade $it" },
                    onSelect = { idx -> grade = grades[idx] },
                )
            }

            Spacer(Modifier.height(20.dp))
            LLText("Topics", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Dropdown(
                label = "Select a topic",
                enabled = topics.isNotEmpty(),
                items = topics.map { it.title },
                onSelect = { idx -> onTopic(topics[idx].id) },
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun <T> Dropdown(
    label: String,
    enabled: Boolean,
    items: List<T>,
    onSelect: (Int) -> Unit,
) {
    val t = LL.tokens
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .clickable(enabled = enabled) { open = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText(label, color = if (enabled) t.ink50 else t.ink500, size = 15.sp, weight = FontWeight.Medium)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = t.ink400, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            items.forEachIndexed { idx, item ->
                DropdownMenuItem(
                    text = { LLText(item.toString(), color = t.ink50, size = 15.sp) },
                    onClick = { open = false; onSelect(idx) },
                )
            }
        }
    }
}
