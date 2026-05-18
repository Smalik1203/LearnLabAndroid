package com.learnlab.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact horizontal slider with a label and a live numeric readout.
 * Used by physics labs (length, angle, speed, height, etc.).
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
    valueFormat: (Float) -> String = { "%.2f".format(it) },
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LLText(label, color = t.ink200, size = 12.sp, weight = FontWeight.Medium)
            LLText("${valueFormat(value)} $unit", color = t.ink500, size = 12.sp)
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
                    thumbColor = t.accent600,
                    activeTrackColor = t.accent500,
                    inactiveTrackColor = t.surface3,
                ),
            )
        }
    }
}
