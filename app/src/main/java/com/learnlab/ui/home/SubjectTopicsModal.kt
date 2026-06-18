package com.learnlab.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.learnlab.content.Subject
import com.learnlab.content.gradesFor
import com.learnlab.content.topicsFor
import com.learnlab.design.Card
import com.learnlab.design.LL
import com.learnlab.design.LLText

/**
 * Subject → grade modal: pick a Grade, then open that grade's continuous textbook.
 */
@Composable
fun SubjectTopicsModal(
    subject: Subject,
    onDismiss: () -> Unit,
    onTopic: (String) -> Unit,
    onOpenTextbook: (Int) -> Unit,
) {
    val t = LL.tokens
    val available = remember(subject) { gradesFor(subject).toSet() }
    // Nothing pre-selected — the user must pick a grade first (topics stay disabled until then).
    var grade by remember(subject) { mutableIntStateOf(-1) }
    val topics = remember(subject, grade) { if (grade >= 0) topicsFor(subject, grade) else emptyList() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Full-screen window so we control the panel width; the Card is capped and
        // centered so neither it nor its dropdowns reach the screen edges.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Reuse the shared Card frame so the picker matches every other surface
        // (same radius, border, fill, and soft elevation).
        Card(modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(), padding = 28.dp) {
          Column {
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
            LLText("Pick a grade, then jump to a topic or open its textbook", color = t.ink400, size = 15.sp)

            Spacer(Modifier.height(24.dp))
            LLText("Grade", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            GradeDropdown(
                selected = grade,
                available = available,
                onSelect = { grade = it },
            )

            Spacer(Modifier.height(20.dp))
            LLText("Topics", color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Dropdown(
                label = "Select a topic",
                enabled = topics.isNotEmpty(),
                items = topics.map { it.title },
                onSelect = { idx -> onTopic(topics[idx].id) },
            )

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = grade >= 0) { onOpenTextbook(grade) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                LLText(
                    if (grade >= 0) "Open Grade $grade textbook →" else "Pick a grade to open the textbook",
                    color = if (grade >= 0) t.accent500 else t.ink500,
                    size = 14.sp, weight = FontWeight.SemiBold,
                )
            }
          }
        }
        }
    }
}

/**
 * Grade picker that always lists 1–10; grades without any experiment for this
 * subject are shown but disabled ("Coming soon") so they can't be selected.
 */
@Composable
private fun GradeDropdown(
    selected: Int,
    available: Set<Int>,
    onSelect: (Int) -> Unit,
) {
    val t = LL.tokens
    var open by remember { mutableStateOf(false) }
    val enabled = available.isNotEmpty()
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidth = it.width }
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .clickable(enabled = enabled) { open = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LLText(
                if (selected >= 0) "Grade $selected" else "Select the Grade",
                color = if (enabled) t.ink50 else t.ink500,
                size = 15.sp,
                weight = FontWeight.Medium,
            )
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = t.ink400, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier
                .width(with(density) { anchorWidth.toDp() })
                .heightIn(max = 200.dp),
            offset = DpOffset(0.dp, 8.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = t.surface2,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, t.line),
        ) {
            (1..10).forEach { g ->
                val on = g in available
                DropdownMenuItem(
                    enabled = on,
                    onClick = { open = false; onSelect(g) },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LLText(
                                "Grade $g",
                                color = if (on) t.ink50 else t.ink600,
                                size = 15.sp,
                                weight = if (g == selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            if (!on) {
                                Spacer(Modifier.width(16.dp))
                                LLText("Coming soon", color = t.ink600, size = 11.sp)
                            }
                        }
                    },
                )
            }
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
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidth = it.width }
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
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier
                .width(with(density) { anchorWidth.toDp() })
                .heightIn(max = 200.dp),
            offset = DpOffset(0.dp, 8.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = t.surface2,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, t.line),
        ) {
            items.forEachIndexed { idx, item ->
                DropdownMenuItem(
                    text = { LLText(item.toString(), color = t.ink50, size = 15.sp) },
                    onClick = { open = false; onSelect(idx) },
                )
            }
        }
    }
}
