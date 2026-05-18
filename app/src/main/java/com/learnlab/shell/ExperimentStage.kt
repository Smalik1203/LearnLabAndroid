package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.findExperiment
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.ProgressBar
import com.learnlab.design.SecondaryButton
import com.learnlab.design.PrimaryButton
import com.learnlab.engines.experimentRegistry
import com.learnlab.store.AppState
import com.learnlab.store.ExperimentControls

@Composable
fun ExperimentStage(state: AppState, modifier: Modifier = Modifier) {
    val t = LL.tokens
    val id = state.currentExperimentId.value

    if (id == null) {
        Box(modifier = modifier.fillMaxSize()) { Welcome() }
        return
    }
    val experiment = findExperiment(id) ?: run {
        Box(modifier = modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
            LLText("Experiment not found.", color = t.ink400)
        }
        return
    }

    var progress by remember(id) { mutableStateOf(0f) }
    LaunchedEffect(id) { progress = 0f }
    val controls = remember(id) {
        ExperimentControls(
            onProgress = { progress = it.coerceIn(0f, 1f) },
            onComplete = { score -> progress = 1f; score?.let { /* future: persist */ } },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(t.bg),
    ) {
        // Stage header — compact two-line layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                LLText(
                    experiment.title,
                    color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                LLText(
                    "${experiment.source}  ·  ${experiment.outcome}",
                    color = t.ink400, size = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(value = progress, modifier = Modifier.width(120.dp))
                Spacer(Modifier.width(10.dp))
                SecondaryButton(label = "‹", onClick = { state.prev() }, enabled = state.hasPrev())
                Spacer(Modifier.width(6.dp))
                if (progress >= 1f && state.hasNext()) {
                    PrimaryButton(label = "Next ›", onClick = { state.next() }, enabled = state.hasNext())
                } else {
                    SecondaryButton(label = "Next ›", onClick = { state.next() }, enabled = state.hasNext())
                }
            }
        }

        // Procedure strip
        InstructionBanner(steps = experiment.steps)

        // Activity surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(t.bg),
        ) {
            val Component = experimentRegistry[experiment.id]
            if (Component != null) {
                Component(experiment, controls)
            } else {
                ComingSoon(source = experiment.source)
            }
        }
    }
}

@Composable
private fun ComingSoon(source: String) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(t.surface2),
                contentAlignment = Alignment.Center,
            ) { LLText("⏳", size = 24.sp, color = t.ink400) }
            Spacer(Modifier.height(16.dp))
            LLText(
                "This experiment is on the way.",
                color = t.ink50, size = 18.sp, weight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            LLText("From $source.", color = t.ink400, size = 14.sp)
        }
    }
}
