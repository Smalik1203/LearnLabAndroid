package com.learnlab.experiments.ch06kit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.ExperimentAccent
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import kotlin.math.roundToInt

/** A discrete slider that snaps between named positions and shows the current label as its readout. */
@Composable
fun SnapSlider(
    title: String,
    labels: List<String>,
    index: Int,
    onIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LLSlider(
        label = title,
        value = index.toFloat(),
        onValueChange = { onIndex(it.roundToInt().coerceIn(0, labels.lastIndex)) },
        min = 0f,
        max = (labels.size - 1).coerceAtLeast(1).toFloat(),
        step = 1f,
        valueFormat = { labels.getOrElse(it.roundToInt()) { "" } },
        modifier = modifier,
    )
}

/** A horizontal dated timeline: each item is (year, caption). */
@Composable
fun Timeline(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEachIndexed { i, (year, caption) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.fillMaxWidth().height(18.dp)) {
                    Canvas(Modifier.fillMaxWidth().height(18.dp)) {
                        val midY = size.height / 2f
                        val first = i == 0
                        val last = i == items.lastIndex
                        val x0 = if (first) size.width / 2f else 0f
                        val x1 = if (last) size.width / 2f else size.width
                        drawLine(t.line, Offset(x0, midY), Offset(x1, midY), strokeWidth = 2f)
                        drawCircle(ExperimentAccent, 6f, Offset(size.width / 2f, midY))
                    }
                }
                LLText(year, color = t.ink50, size = 12.sp, weight = FontWeight.Bold)
                LLText(caption, color = t.ink400, size = 10.sp, lineHeight = 13.sp, align = TextAlign.Center)
            }
        }
    }
}
