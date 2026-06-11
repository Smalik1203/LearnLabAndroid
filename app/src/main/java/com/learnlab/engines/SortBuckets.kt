package com.learnlab.engines

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.learnlab.design.Radius
import com.learnlab.store.ExperimentControls

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
                LLText("TASK DESCRIPTION", color = t.accent700, size = 11.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(6.dp))
                LLText(prompt, color = t.ink50, size = 15.sp, lineHeight = 22.sp, weight = FontWeight.SemiBold)
            }
            Row {
                GhostButton(
                    label = "Reset Board",
                    onClick = {
                        items.forEach { placement[it.id] = POOL }
                        revealed = false
                        hoverZone = null
                        controls.onProgress(0f)
                    },
                )
                Spacer(Modifier.width(10.dp))
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

        // Pool Container
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

        // Buckets Row
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    val defaultBg = if (t.isDark) t.surface.copy(alpha = 0.2f) else t.surface.copy(alpha = 0.7f)
    val hoveredBg = if (t.isDark) t.accent50.copy(alpha = 0.15f) else t.accent50.copy(alpha = 0.4f)
    val bg by animateColorAsState(if (isHovered) hoveredBg else defaultBg, label = "pool-bg")

    val defaultBorder = t.lineStrong.copy(alpha = 0.2f)
    val hoveredBorder = t.accent500
    val border by animateColorAsState(if (isHovered) hoveredBorder else defaultBorder, label = "pool-b")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(Radius.md))
            .padding(16.dp)
            .onGloballyPositioned {
                val pos = it.positionInRoot()
                onZoneRect(Rect(pos.x, pos.y, pos.x + it.size.width, pos.y + it.size.height))
            },
    ) {
        LLText(
            "SPECIMENS TO CLASSIFY",
            color = t.ink500, size = 10.sp,
            weight = FontWeight.Bold, letterSpacing = 1.8.sp,
        )
        Spacer(Modifier.height(10.dp))
        if (items.isEmpty()) {
            LLText("All specimens classified! Tap Check Answers.", color = t.ink400, size = 13.sp, weight = FontWeight.Medium)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { items.forEach { chip(it) } }
        }
    }
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
    val defaultBg = if (t.isDark) t.surface.copy(alpha = 0.2f) else t.surface.copy(alpha = 0.7f)
    val hoveredBg = if (t.isDark) t.accent50.copy(alpha = 0.15f) else t.accent50.copy(alpha = 0.4f)
    val bg by animateColorAsState(if (isHovered) hoveredBg else defaultBg, label = "bucket-bg")

    val defaultBorder = t.lineStrong.copy(alpha = 0.2f)
    val hoveredBorder = t.accent500
    val border by animateColorAsState(if (isHovered) hoveredBorder else defaultBorder, label = "bucket-b")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(Radius.md))
            .padding(18.dp)
            .onGloballyPositioned {
                val pos = it.positionInRoot()
                onZoneRect(Rect(pos.x, pos.y, pos.x + it.size.width, pos.y + it.size.height))
            },
    ) {
        LLText(
            title.uppercase(),
            color = t.accent700, size = 11.sp,
            weight = FontWeight.Bold, letterSpacing = 1.6.sp,
        )
        Spacer(Modifier.height(4.dp))
        LLText(bucket.label, color = t.ink50, size = 18.sp, weight = FontWeight.ExtraBold)
        if (bucket.hint != null) {
            Spacer(Modifier.height(4.dp))
            LLText(bucket.hint, color = t.ink400, size = 12.sp, lineHeight = 16.sp, weight = FontWeight.Medium)
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
        revealed && !correct -> Color(0xFFEF4444) // Rose-500
        isDragging -> t.accent300
        else -> t.lineStrong.copy(alpha = 0.6f)
    }
    val bgColor = when {
        revealed && correct -> t.accent50.copy(alpha = 0.2f)
        revealed && !correct -> Color(0xFFEF4444).copy(alpha = 0.1f)
        isDragging -> t.surface2.copy(alpha = 0.8f)
        else -> t.surface2.copy(alpha = 0.4f)
    }
    val textColor = when {
        revealed && correct -> t.accent700
        revealed && !correct -> Color(0xFFEF4444)
        else -> t.ink200
    }

    // 3D Lift Scale Animation
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "dragScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 12f else 2f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "dragElevation"
    )

    Box(
        modifier = Modifier
            .offset { dragPx }
            .zIndex(if (isDragging) 100f else 0f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(elevation = elevation.dp, shape = RoundedCornerShape(Radius.sm), spotColor = if (isDragging) t.accent300 else Color.Black)
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
            .clip(RoundedCornerShape(Radius.sm))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(Radius.sm))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText(item.emoji, size = 16.sp, color = Color.Unspecified)
            Spacer(Modifier.width(8.dp))
            LLText(item.label, color = textColor, size = 14.sp, weight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ResultBar(correct: Int, total: Int) {
    val t = LL.tokens
    val ok = correct == total
    val bg = if (ok) t.accent50.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.1f)
    val border = if (ok) t.accent300 else Color(0xFFEF4444).copy(alpha = 0.3f)
    val accent = if (ok) t.accent700 else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(Radius.sm))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("$correct / $total", color = accent, size = 15.sp, weight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            LLText(
                if (ok) "perfectly sorted! Well done." else "correctly sorted. Tap Reset Board to retry.",
                color = t.ink200,
                size = 14.sp,
                weight = FontWeight.Medium
            )
        }
    }
    @Suppress("UNUSED_EXPRESSION") mutableStateListOf<String>()
}
