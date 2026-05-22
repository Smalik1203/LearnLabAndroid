package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.NumberPip

// Single fixed-height strip — steps scroll horizontally, never wrap vertically.
@Composable
fun InstructionBanner(steps: List<String>) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(
            "PROCEDURE",
            color = t.ink500, size = 10.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
        )
        Box(Modifier.width(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberPip(i + 1)
                    Box(Modifier.width(6.dp))
                    LLText(step, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
