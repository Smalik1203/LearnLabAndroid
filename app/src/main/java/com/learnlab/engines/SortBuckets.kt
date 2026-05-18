package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.learnlab.design.GhostButton
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.store.ExperimentControls

/**
 * Native port of src/runtime/engines/SortBuckets.tsx.
 *
 * Long-press on a chip to pick it up (avoids palm-rejection misfires on IFP
 * touch panels), drag over a bucket to highlight it, release to drop. Reset
 * sends everything back to the pool. Check answers reveals correct/wrong tint.
 */

data class SortItem(val id: String, val label: String, val emoji: String, val bucketId: String)
data class SortBucket(val id: String, val label: String, val hint: String? = null)

private const val POOL = "__pool__"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortBuckets(
    prompt: String,
    bucketsTitle: String = "Sort into",
    items: List<SortItem>,
    buckets: List<SortBucket>,
    controls: ExperimentControls,
) {
    val t = LL.tokens
    val placement: SnapshotStateMap<String, String> = remember(items) {
        mutableStateMapOf<String, String>().apply { items.forEach { put(it.id, POOL) } }
    }
    val zoneRects = remember { mutableStateMapOf<String, Rect>() }
    var hoverZone by remember { mutableStateOf<String?>(null) }
    var revealed by remember { mutableStateOf(false) }

    val placedCount by remember { derivedStateOf { placement.count { it.value != POOL } } }
    val correctCount by remember {
        derivedStateOf { items.count { placement[it.id] == it.bucketId } }
    }

    LaunchedEffect(placedCount) {
        controls.onProgress(placedCount / items.size.toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) {
                LLText("TASK", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(6.dp))
                LLText(prompt, color = t.ink400, size = 14.sp, lineHeight = 20.sp)
            }
            Row {
                GhostButton(
                    label = "Reset",
                    onClick = {
                        items.forEach { placement[it.id] = POOL }
                        revealed = false
                        hoverZone = null
                        controls.onProgress(0f)
                    },
                )
                Spacer(Modifier.width(8.dp))
                PrimaryButton(
                    label = "Check answers",
                    enabled = placedCount == items.size,
                    onClick = {
                        revealed = true
                        controls.onComplete(correctCount / items.size.toFloat())
                    },
                )
            }
        }

        // Pool
        Pool(
            items = items.filter { placement[it.id] == POOL },
            zoneId = POOL,
            isHovered = hoverZone == POOL,
            onZoneRect = { zoneRects[POOL] = it },
            chip = { item ->
                ChipDraggable(
                    item = item,
                    revealed = revealed,
                    correct = placement[item.id] == item.bucketId,
                    onZonePoint = { hoverZone = pickZone(zoneRects, it, buckets) },
                    onDropAt = { p ->
                        val z = pickZone(zoneRects, p, buckets) ?: POOL
                        placement[item.id] = z
                        hoverZone = null
                    },
                )
            },
        )

        // Buckets
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            buckets.forEach { bucket ->
                BucketBox(
                    bucket = bucket,
                    title = bucketsTitle,
                    items = items.filter { placement[it.id] == bucket.id },
                    isHovered = hoverZone == bucket.id,
                    onZoneRect = { zoneRects[bucket.id] = it },
                    chip = { item ->
                        ChipDraggable(
                            item = item,
                            revealed = revealed,
                            correct = placement[item.id] == item.bucketId,
                            onZonePoint = { hoverZone = pickZone(zoneRects, it, buckets) },
                            onDropAt = { p ->
                                val z = pickZone(zoneRects, p, buckets) ?: POOL
                                placement[item.id] = z
                                hoverZone = null
                            },
                        )
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }

        if (revealed) {
            ResultBar(correct = correctCount, total = items.size)
        }
    }
}

private fun pickZone(rects: Map<String, Rect>, p: Offset, buckets: List<SortBucket>): String? {
    buckets.forEach { b -> rects[b.id]?.let { if (it.contains(p)) return b.id } }
    rects[POOL]?.let { if (it.contains(p)) return POOL }
    return null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Pool(
    items: List<SortItem>,
    zoneId: String,
    isHovered: Boolean,
    onZoneRect: (Rect) -> Unit,
    chip: @Composable (SortItem) -> Unit,
) {
    val t = LL.tokens
    val bg by animateColorAsState(if (isHovered) t.accent50 else t.surface, label = "pool-bg")
    val border = if (isHovered) t.accent500 else t.line
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .onGloballyPositioned {
                val pos = it.positionInRoot()
                onZoneRect(Rect(pos.x, pos.y, pos.x + it.size.width, pos.y + it.size.height))
            },
    ) {
        LLText(
            "SPECIMENS TO CLASSIFY",
            color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
        )
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            LLText("All items placed. Hit Check answers.", color = t.ink500, size = 12.sp)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { items.forEach { chip(it) } }
        }
    }
    // suppress lint
    @Suppress("UNUSED_EXPRESSION") zoneId
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BucketBox(
    bucket: SortBucket,
    title: String,
    items: List<SortItem>,
    isHovered: Boolean,
    onZoneRect: (Rect) -> Unit,
    chip: @Composable (SortItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val bg by animateColorAsState(if (isHovered) t.accent50 else t.surface, label = "bucket-bg")
    val border = if (isHovered) t.accent500 else t.line
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .onGloballyPositioned {
                val pos = it.positionInRoot()
                onZoneRect(Rect(pos.x, pos.y, pos.x + it.size.width, pos.y + it.size.height))
            },
    ) {
        LLText(
            title.uppercase(),
            color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
        )
        Spacer(Modifier.height(4.dp))
        LLText(bucket.label, color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold)
        if (bucket.hint != null) {
            Spacer(Modifier.height(4.dp))
            LLText(bucket.hint, color = t.ink400, size = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { items.forEach { chip(it) } }
        }
    }
}

@Composable
private fun ChipDraggable(
    item: SortItem,
    revealed: Boolean,
    correct: Boolean,
    onZonePoint: (Offset) -> Unit,
    onDropAt: (Offset) -> Unit,
) {
    val t = LL.tokens
    var posInRoot by remember { mutableStateOf(Offset.Zero) }
    var dragPx by remember { mutableStateOf(IntOffset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val borderColor = when {
        revealed && correct -> t.accent500
        revealed && !correct -> t.rose300
        else -> t.lineStrong
    }
    val bgColor = when {
        revealed && correct -> t.accent50
        revealed && !correct -> t.rose50
        else -> t.surface3
    }
    val textColor = when {
        revealed && correct -> t.accent700
        revealed && !correct -> t.rose700
        else -> t.ink200
    }

    Box(
        modifier = Modifier
            .offset { dragPx }
            .zIndex(if (isDragging) 100f else 0f)
            .onGloballyPositioned { posInRoot = it.positionInRoot() }
            .pointerInput(item.id, revealed) {
                if (revealed) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { isDragging = true },
                    onDrag = { change, drag ->
                        change.consume()
                        dragPx = IntOffset(dragPx.x + drag.x.toInt(), dragPx.y + drag.y.toInt())
                        val centre = Offset(
                            posInRoot.x + dragPx.x + size.width / 2f,
                            posInRoot.y + dragPx.y + size.height / 2f,
                        )
                        onZonePoint(centre)
                    },
                    onDragEnd = {
                        val centre = Offset(
                            posInRoot.x + dragPx.x + size.width / 2f,
                            posInRoot.y + dragPx.y + size.height / 2f,
                        )
                        onDropAt(centre)
                        dragPx = IntOffset.Zero
                        isDragging = false
                    },
                    onDragCancel = {
                        dragPx = IntOffset.Zero
                        isDragging = false
                    },
                )
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText(item.emoji, size = 16.sp, color = Color.Unspecified)
            Spacer(Modifier.width(6.dp))
            LLText(item.label, color = textColor, size = 14.sp, weight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ResultBar(correct: Int, total: Int) {
    val t = LL.tokens
    val ok = correct == total
    val bg = if (ok) t.accent50 else t.surface2
    val accent = if (ok) t.accent700 else t.ink200
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("$correct / $total", color = accent, size = 14.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            LLText("correctly sorted. Tap Reset to try again.", color = t.ink400, size = 14.sp)
        }
    }
    // referenced so list-only edits don't drop them
    @Suppress("UNUSED_EXPRESSION") mutableStateListOf<String>()
}
