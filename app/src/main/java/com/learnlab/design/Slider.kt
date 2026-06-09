package com.learnlab.design

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact horizontal slider with a label and a live numeric readout.
 * Used by physics labs (length, angle, speed, height, etc.).
 *
 * When `info` is provided, a small "i" badge appears next to the label; tapping
 * it toggles a tooltip card with the info text.
 */
@Composable
fun LLSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    step: Float? = null,
    unit: String = "",
    enabled: Boolean = true,
    info: String? = null,
    valueFormat: (Float) -> String = { "%.2f".format(it) },
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    var showInfo by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LLText(label, color = t.ink200, size = 12.sp, weight = FontWeight.Medium)
                if (info != null) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(t.surface2)
                            .border(1.dp, t.lineStrong, CircleShape)
                            .clickable { showInfo = !showInfo },
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText("i", color = t.ink400, size = 10.sp,
                            weight = FontWeight.Bold)
                    }
                }
            }
            LLText("${valueFormat(value)} $unit", color = t.ink500, size = 12.sp)
        }

        if (info != null && showInfo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(8.dp))
                    .clickable { showInfo = false }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                LLText(info, color = t.ink200, size = 11.sp, lineHeight = 15.sp)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val steps = if (step != null && step > 0f) {
                val n = ((max - min) / step).toInt() - 1
                if (n > 0) n else 0
            } else 0
            Slider(
                value = value.coerceIn(min, max),
                onValueChange = onValueChange,
                valueRange = min..max,
                steps = steps,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = ExperimentAccent,
                    activeTrackColor = ExperimentAccent,
                    inactiveTrackColor = t.surface3,
                    activeTickColor = ExperimentAccent,
                    inactiveTickColor = t.ink500.copy(alpha = 0.5f),
                ),
            )
        }
    }
}
