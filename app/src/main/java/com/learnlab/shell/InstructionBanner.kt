package com.learnlab.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText

/**
 * Active-step stepper. Renders one numbered circle per step; the active step's
 * circle expands into a pill that shows the step text. Tapping any closed
 * circle activates it (others collapse). Step 1 is open by default.
 */
@Composable
fun InstructionBanner(steps: List<String>) {
    val t = LL.tokens
    var activeStep by remember(steps) { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LLText(
            "PROCEDURE",
            color = t.ink400, size = 12.sp,
            weight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
        )
        Box(Modifier.width(14.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { i, step ->
                StepperPill(
                    number = i + 1,
                    text = step,
                    open = i == activeStep,
                    onClick = { activeStep = i },
                )
            }
        }
    }
}

@Composable
private fun StepperPill(
    number: Int,
    text: String,
    open: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (open) t.accent50 else Color.Transparent)
            .border(
                1.dp,
                if (open) t.accent300 else t.lineStrong,
                RoundedCornerShape(20.dp),
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (open) t.accent500 else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            LLText(
                "$number",
                color = if (open) Color.White else t.ink400,
                size = 11.sp,
                weight = FontWeight.Bold,
            )
        }
        AnimatedVisibility(visible = open) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(8.dp))
                LLText(
                    text,
                    color = t.ink50,
                    size = 13.sp,
                    weight = FontWeight.Medium,
                    lineHeight = 16.sp,
                )
                Box(Modifier.width(10.dp))
            }
        }
    }
}
