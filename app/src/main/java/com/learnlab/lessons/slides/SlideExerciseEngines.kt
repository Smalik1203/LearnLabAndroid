package com.learnlab.lessons.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.lessonPalette
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ExerciseEngineHost(b: ChapterBlock.Exercise) {
    val t = LL.tokens
    when (b.engine) {
        "venn" -> VennEngine(b.config)
        "flowchart" -> FlowchartEngine(b.config)
        "imageCompare" -> ImageCompareEngine(b.config)
        "mcq" -> McqEngine(b.config)
        "freeText" -> FreeTextEngine(b.config)
        else -> LLText("[unsupported engine: ${b.engine}]", color = t.ink500, size = 14.sp)
    }
}

/* ───────────────────────── Venn (3 regions horizontal) ───────────────────────── */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VennEngine(config: JsonObject) {
    val t = LL.tokens
    val p = lessonPalette()
    val regions = config["regions"]?.jsonArray ?: return
    val items = config["items"]?.jsonArray ?: return

    val placement = remember { mutableStateMapOf<String, String>() }
    var selectedRegion by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1.4f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            regions.forEach { r ->
                val obj = r.jsonObject
                val id = obj["id"]!!.jsonPrimitive.content
                val label = obj["label"]!!.jsonPrimitive.content
                val isSelected = selectedRegion == id
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(if (isSelected) p.sky.surfaceStrong.copy(alpha = 0.5f) else p.sky.surface)
                        .border(if (isSelected) 3.dp else 1.dp, p.sky.accent, RoundedCornerShape(Radius.lg))
                        .clickable { selectedRegion = id }
                        .padding(20.dp),
                ) {
                    LLText(label, color = p.sky.accent, size = 18.sp, weight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    val assigned = placement.filterValues { it == id }.keys
                    if (assigned.isEmpty()) {
                        LLText("Tap items below to drop in", color = p.sky.ink.copy(alpha = 0.5f), size = 14.sp)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            assigned.forEach { itemId ->
                                val itemLabel = items.firstNotNullOfOrNull { ji ->
                                    val jo = ji.jsonObject
                                    if (jo["id"]?.jsonPrimitive?.content == itemId)
                                        jo["label"]?.jsonPrimitive?.content else null
                                } ?: itemId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.pill))
                                        .background(p.sky.surfaceStrong)
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                ) {
                                    LLText(itemLabel, color = p.sky.accent, size = 13.sp, weight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        LLText("Items — tap a region, then tap each item to drop it in",
            color = t.ink400, size = 13.sp, weight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { it ->
                val jo = it.jsonObject
                val itemId = jo["id"]!!.jsonPrimitive.content
                val itemLabel = jo["label"]!!.jsonPrimitive.content
                val correct = jo["correct"]?.jsonPrimitive?.content
                val assignedTo = placement[itemId]
                val isCorrect = assignedTo != null && assignedTo == correct
                val bgColor = when {
                    assignedTo == null -> t.surface2
                    isCorrect -> p.emerald.surface
                    else -> p.rose.surface
                }
                val borderColor = when {
                    assignedTo == null -> t.line
                    isCorrect -> p.emerald.border
                    else -> p.rose.border
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(Radius.pill))
                        .clickable(enabled = selectedRegion != null || assignedTo != null) {
                            if (assignedTo != null) placement.remove(itemId)
                            else selectedRegion?.let { placement[itemId] = it }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    LLText(itemLabel, color = t.ink50, size = 15.sp, weight = FontWeight.Medium)
                }
            }
        }
    }
}

/* ───────────────────────── Flowchart ───────────────────────── */

@Composable
private fun FlowchartEngine(config: JsonObject) {
    val t = LL.tokens
    val p = lessonPalette()
    val nodes = config["nodes"]?.jsonArray ?: return
    val answers = config["answers"]?.jsonObject

    var revealed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            nodes.forEachIndexed { i, n ->
                val obj = n.jsonObject
                val id = obj["id"]!!.jsonPrimitive.content
                val label = obj["label"]!!.jsonPrimitive.content
                val type = obj["type"]?.jsonPrimitive?.content ?: "decision"
                FlowchartNode(
                    label = label, type = type, isAnswer = type == "answer",
                    revealed = revealed,
                    answerText = if (revealed) answers?.get(id)?.jsonPrimitive?.content else null,
                )
                if (i < nodes.size - 1) {
                    Box(modifier = Modifier.height(32.dp).width(3.dp).background(t.line))
                }
            }
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(p.sky.accent)
                    .clickable { revealed = !revealed }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            ) {
                LLText(if (revealed) "Hide answers" else "Reveal A and B",
                    color = t.surface, size = 18.sp, weight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FlowchartNode(label: String, type: String, isAnswer: Boolean, revealed: Boolean, answerText: String?) {
    val t = LL.tokens
    val p = lessonPalette()
    val (bg, border, ink) = when {
        isAnswer && revealed -> Triple(p.emerald.surface, p.emerald.border, p.emerald.ink)
        type == "start" -> Triple(p.violet.surface, p.violet.border, p.violet.ink)
        else -> Triple(t.surface2, t.line, t.ink50)
    }
    Column(
        modifier = Modifier
            .clip(if (isAnswer) CircleShape else RoundedCornerShape(Radius.md))
            .background(bg)
            .border(2.dp, border, if (isAnswer) CircleShape else RoundedCornerShape(Radius.md))
            .padding(horizontal = 28.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LLText(label, color = ink, size = 18.sp, weight = FontWeight.SemiBold)
        if (isAnswer && revealed && answerText != null) {
            Spacer(Modifier.height(6.dp))
            LLText(answerText, color = p.emerald.ink, size = 13.sp, lineHeight = 18.sp)
        }
    }
}

/* ───────────────────────── Image compare ───────────────────────── */

@Composable
private fun ImageCompareEngine(config: JsonObject) {
    val t = LL.tokens
    val p = lessonPalette()
    val leftLabel = config["leftLabel"]?.jsonPrimitive?.content ?: "Left"
    val rightLabel = config["rightLabel"]?.jsonPrimitive?.content ?: "Right"
    val leftImage = config["leftImage"]?.jsonPrimitive?.content ?: ""
    val rightImage = config["rightImage"]?.jsonPrimitive?.content ?: ""
    val fields = config["promptFields"]?.jsonArray ?: return

    val notes = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CompareCellLarge(leftLabel, leftImage, Modifier.weight(1f).fillMaxHeight())
            CompareCellLarge(rightLabel, rightImage, Modifier.weight(1f).fillMaxHeight())
        }
        Spacer(Modifier.height(20.dp))
        LLText("Compare these features (tap to mark observed):",
            color = t.ink400, size = 15.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        fields.forEach { f ->
            val key = f.jsonPrimitive.content
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                LLText("• $key", color = p.sky.accent, size = 16.sp, weight = FontWeight.SemiBold,
                    modifier = Modifier.width(220.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(t.surface2)
                        .clickable { notes[key] = if (notes[key] == "noted") "" else "noted" }
                        .padding(14.dp),
                ) {
                    LLText(
                        if (notes[key] == "noted") "✓ noted — write the comparison in your notebook"
                        else "tap to mark observed",
                        color = if (notes[key] == "noted") p.emerald.accent else t.ink500,
                        size = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareCellLarge(label: String, asset: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(16.dp),
    ) {
        val bmp = loadFigureBitmap(asset)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(Radius.md)),
            contentAlignment = Alignment.Center,
        ) {
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp, contentDescription = label,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PlaceholderImage(label = label, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.height(12.dp))
        LLText(label, color = t.ink50, size = 18.sp, weight = FontWeight.Bold)
    }
}

/* ───────────────────────── MCQ ───────────────────────── */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun McqEngine(config: JsonObject) {
    val t = LL.tokens
    val p = lessonPalette()
    val rows = config["rows"]?.jsonArray ?: return
    val selections = remember { mutableStateMapOf<String, Boolean>() }
    var checked by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        rows.forEach { rNode ->
            val r = rNode.jsonObject
            val group = r["group"]!!.jsonPrimitive.content
            val seed = r["seed"]?.jsonPrimitive?.content ?: ""
            val root = r["root"]?.jsonPrimitive?.content ?: ""
            val options = r["options"]?.jsonArray ?: return@forEach
            val correct = r["correct"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet() ?: emptySet()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(t.surface2)
                    .padding(16.dp),
            ) {
                LLText("Group $group  ·  $seed  ·  $root",
                    color = p.sky.accent, size = 16.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { o ->
                        val opt = o.jsonPrimitive.content
                        val key = "$group:$opt"
                        val isSelected = selections[key] == true
                        val isCorrect = correct.contains(opt)
                        val borderColor = when {
                            !checked -> if (isSelected) p.sky.accent else t.line
                            isSelected && isCorrect -> p.emerald.accent
                            isSelected && !isCorrect -> p.rose.accent
                            !isSelected && isCorrect -> p.emerald.border
                            else -> t.line
                        }
                        val bg = when {
                            !checked -> if (isSelected) p.sky.surface else t.surface
                            isSelected && isCorrect -> p.emerald.surface
                            isSelected && !isCorrect -> p.rose.surface
                            else -> t.surface
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.pill))
                                .background(bg)
                                .border(2.dp, borderColor, RoundedCornerShape(Radius.pill))
                                .clickable { selections[key] = !isSelected }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            LLText(opt, color = t.ink50, size = 16.sp, weight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(p.sky.accent)
                .clickable { checked = !checked }
                .padding(horizontal = 28.dp, vertical = 12.dp),
        ) {
            LLText(if (checked) "Reset" else "Check answers", color = t.surface,
                size = 16.sp, weight = FontWeight.Bold)
        }
    }
}

/* ───────────────────────── Free text ───────────────────────── */

@Composable
private fun FreeTextEngine(config: JsonObject) {
    val t = LL.tokens
    val p = lessonPalette()
    val hints = config["suggestedAnswerHints"]?.jsonArray
    var showHints by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .clip(RoundedCornerShape(Radius.md))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(Radius.md))
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            LLText("Discuss in class. Students write their answers in their notebooks.",
                color = t.ink500, size = 18.sp, lineHeight = 26.sp)
        }
        if (hints != null) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(p.amber.accent)
                    .clickable { showHints = !showHints }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                LLText(if (showHints) "Hide teacher hint" else "Show teacher hint",
                    color = t.surface, size = 15.sp, weight = FontWeight.Bold)
            }
            if (showHints) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(p.amber.surface)
                        .border(1.dp, p.amber.border, RoundedCornerShape(Radius.md))
                        .padding(20.dp),
                ) {
                    hints.forEach { h ->
                        LLText("• ${h.jsonPrimitive.content}",
                            color = p.amber.ink, size = 16.sp, lineHeight = 24.sp,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
