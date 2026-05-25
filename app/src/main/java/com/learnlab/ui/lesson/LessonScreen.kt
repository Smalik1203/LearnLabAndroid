package com.learnlab.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.Spacing
import com.learnlab.engines.experimentRegistry
import com.learnlab.shell.InstructionBanner
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState
import com.learnlab.store.ExperimentControls

@Composable
fun LessonScreen(
    state: AppState,
    experimentId: String,
    onBack: () -> Unit,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
) {
    val t = LL.tokens
    val experiment = findExperiment(experimentId)

    if (experiment == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(t.bg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(state = state, title = "Not Found", showBack = true, onBack = onBack)
            LLText("Experiment not found.", color = MaterialTheme.colorScheme.onBackground, size = 14.sp)
        }
        return
    }

    var progress by remember(experimentId) { mutableStateOf(0f) }
    LaunchedEffect(experimentId) { progress = 0f }
    val controls = remember(experimentId) {
        ExperimentControls(
            onProgress = { progress = it.coerceIn(0f, 1f) },
            onComplete = { _ -> progress = 1f },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        TopBar(
            state = state,
            title = experiment.title,
            showBack = true,
            onBack = onBack,
        )

        // Stage header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = Spacing.md)) {
                LLText(
                    experiment.outcome,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LLText(
                    experiment.source,
                    color = MaterialTheme.colorScheme.outline,
                    size = 11.sp,
                    weight = FontWeight.Normal,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(value = progress, modifier = Modifier.width(120.dp))
                Spacer(Modifier.width(Spacing.md))
                SecondaryButton(label = "‹", onClick = { onPrev?.invoke() }, enabled = onPrev != null)
                Spacer(Modifier.width(Spacing.xs + 2.dp))
                if (progress >= 1f && onNext != null) {
                    PrimaryButton(label = "Next ›", onClick = { onNext.invoke() })
                } else {
                    SecondaryButton(label = "Next ›", onClick = { onNext?.invoke() }, enabled = onNext != null)
                }
            }
        }

        InstructionBanner(steps = experiment.steps)

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
        modifier = Modifier.fillMaxSize().padding(Spacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(t.surface2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassEmpty,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(Spacing.lg))
            LLText(
                "This experiment is on the way.",
                color = MaterialTheme.colorScheme.onSurface,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            LLText("From $source.", color = MaterialTheme.colorScheme.onSurfaceVariant, size = 12.sp)
        }
    }
}
