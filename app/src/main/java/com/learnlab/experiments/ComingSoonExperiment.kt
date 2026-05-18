package com.learnlab.experiments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Experiment
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.NumberPip

/**
 * Visible activity surface for experiments where the React app has a
 * bespoke simulation that hasn't been ported yet. Keeps the shell honest
 * (steps strip + progress bar still work) and tells the teacher exactly
 * what's coming.
 */
@Composable
fun ComingSoonExperiment(experiment: Experiment) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(20.dp))
                .padding(28.dp),
        ) {
            LLText(
                experiment.source.uppercase(),
                color = t.ink500, size = 11.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            Spacer(Modifier.height(8.dp))
            LLText(
                experiment.title,
                color = t.ink50, size = 22.sp, weight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            LLText(experiment.outcome, color = t.ink400, size = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(24.dp))
            LLText(
                "WHAT THE STUDENT WILL DO",
                color = t.ink500, size = 10.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp,
            )
            Spacer(Modifier.height(10.dp))
            experiment.steps.forEachIndexed { i, step ->
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    NumberPip(i + 1)
                    LLText(step, color = t.ink200, size = 14.sp, lineHeight = 20.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(16.dp),
            ) {
                LLText(
                    "This experiment exists in the web version. Native build is in progress — " +
                        "the shell, navigation, and procedure strip are already running, so dropping in the " +
                        "Compose implementation is a self-contained next step.",
                    color = t.ink400, size = 13.sp, lineHeight = 18.sp, align = TextAlign.Start,
                )
            }
        }
    }
}
