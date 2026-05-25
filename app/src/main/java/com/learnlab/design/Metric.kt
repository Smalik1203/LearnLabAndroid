package com.learnlab.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tiny "label = value" chip used in physics lab readouts. */
@Composable
fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            LLText(label, color = t.ink500, size = 13.sp, maxLines = 1)
            LLText(" ", color = t.ink500, size = 13.sp, maxLines = 1)
            LLText(value, color = t.ink50, size = 13.sp,
                weight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
