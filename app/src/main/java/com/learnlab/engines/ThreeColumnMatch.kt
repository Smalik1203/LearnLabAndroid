package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.store.ExperimentControls
import kotlinx.coroutines.delay

/**
 * Native port of src/runtime/engines/ThreeColumnMatch.tsx.
 *
 * Three columns, one tile per row. Pick A, then B, then C. If all three are
 * the same row id, lock it in (green). If any mismatch, brief red flash, reset
 * selection. Notes for locked rows show in a panel under the columns.
 */

data class MatchTile(val label: String, val emoji: String? = null)
data class MatchRow(
    val id: String,
    val a: MatchTile,
    val b: MatchTile,
    val c: MatchTile,
    val note: String,
)

@Composable
fun ThreeColumnMatch(
    prompt: String,
    columnTitles: Triple<String, String, String>,
    rows: List<MatchRow>,
    controls: ExperimentControls,
) {
    val t = LL.tokens
    val completed = remember { mutableStateListOf<String>() }
    var aId by remember { mutableStateOf<String?>(null) }
    var bId by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var errorTick by remember { mutableStateOf(0) }

    LaunchedEffect(errorTick) {
        if (errorMsg != null) {
            delay(1200)
            errorMsg = null
        }
    }

    LaunchedEffect(completed.size) {
        controls.onProgress(completed.size / rows.size.toFloat())
        if (completed.size == rows.size) controls.onComplete(1f)
    }

    fun pickA(id: String) {
        if (completed.contains(id)) return
        aId = id
        bId = null
        errorMsg = null
    }
    fun pickB(id: String) {
        val a = aId ?: return
        if (a != id) {
            errorMsg = "Those don't match. Try again."
            errorTick++
            aId = null; bId = null
            return
        }
        bId = id
    }
    fun pickC(id: String) {
        val a = aId; val b = bId
        if (a == null || b == null) return
        if (a != id) {
            errorMsg = "Chain broken. Try again."
            errorTick++
            aId = null; bId = null
            return
        }
        completed.add(id)
        aId = null; bId = null
    }
    fun reset() {
        completed.clear()
        aId = null; bId = null; errorMsg = null
        controls.onProgress(0f)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) {
                LLText("BUILD THE CHAIN", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(6.dp))
                LLText(prompt, color = t.ink400, size = 14.sp, lineHeight = 20.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LLText("${completed.size}/${rows.size} linked", color = t.ink500, size = 12.sp)
                Spacer(Modifier.width(8.dp))
                GhostButton(label = "Reset", onClick = { reset() })
            }
        }

        // 3 columns
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MatchColumn(
                title = columnTitles.first,
                rows = rows,
                tileFor = { it.a },
                isDone = { completed.contains(it.id) },
                isActive = { aId == it.id },
                isDim = { aId != null && aId != it.id },
                onClick = { pickA(it.id) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            MatchColumn(
                title = columnTitles.second,
                rows = rows,
                tileFor = { it.b },
                isDone = { completed.contains(it.id) },
                isActive = { bId == it.id },
                isDim = { aId == null || (bId != null && bId != it.id) },
                onClick = { pickB(it.id) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            MatchColumn(
                title = columnTitles.third,
                rows = rows,
                tileFor = { it.c },
                isDone = { completed.contains(it.id) },
                isActive = { false },
                isDim = { bId == null },
                onClick = { pickC(it.id) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        // Status / error / hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val msg = errorMsg
            when {
                msg != null -> LLText(msg, color = t.rose700, size = 14.sp, weight = FontWeight.SemiBold)
                aId != null && bId == null -> LLText(
                    "Now pick the ${columnTitles.second} for this one.",
                    color = t.ink400, size = 14.sp,
                )
                bId != null -> LLText(
                    "Pick the matching ${columnTitles.third} to lock the chain.",
                    color = t.ink400, size = 14.sp,
                )
                else -> LLText("Start by tapping a ${columnTitles.first}.",
                    color = t.ink400, size = 14.sp)
            }
        }

        // Revealed notes for locked chains
        if (completed.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rows.filter { completed.contains(it.id) }.forEach { r ->
                    Row {
                        LLText(
                            "${r.a.label} → ${r.b.label}: ",
                            color = t.ink200, size = 12.sp, weight = FontWeight.SemiBold,
                        )
                        LLText(r.note, color = t.ink400, size = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchColumn(
    title: String,
    rows: List<MatchRow>,
    tileFor: (MatchRow) -> MatchTile,
    isDone: (MatchRow) -> Boolean,
    isActive: (MatchRow) -> Boolean,
    isDim: (MatchRow) -> Boolean,
    onClick: (MatchRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(title.uppercase(), color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows, key = { it.id }) { row ->
                MatchTileButton(
                    tile = tileFor(row),
                    done = isDone(row),
                    active = isActive(row),
                    dim = isDim(row) && !isDone(row),
                    onClick = { onClick(row) },
                )
            }
        }
    }
}

@Composable
private fun MatchTileButton(
    tile: MatchTile,
    done: Boolean,
    active: Boolean,
    dim: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val borderColor by animateColorAsState(
        targetValue = when {
            done -> t.accent500
            active -> t.accent500
            else -> t.line
        },
        label = "match-b",
    )
    val bg = when {
        done -> t.accent50
        active -> t.accent50
        else -> t.surface2
    }
    val fg = when {
        done -> t.accent700
        active -> t.ink50
        dim -> t.ink500
        else -> t.ink200
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .alpha(if (dim) 0.55f else 1f)
            .clickable(enabled = !done) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (tile.emoji != null) {
            LLText(tile.emoji, size = 16.sp, color = androidx.compose.ui.graphics.Color.Unspecified)
            Spacer(Modifier.width(8.dp))
        }
        LLText(tile.label, color = fg, size = 13.sp, weight = FontWeight.Medium,
            modifier = Modifier.weight(1f), lineHeight = 17.sp)
        if (done) {
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Matched",
                tint = t.accent700,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
