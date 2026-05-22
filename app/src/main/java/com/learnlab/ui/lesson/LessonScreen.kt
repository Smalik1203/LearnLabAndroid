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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.learnlab.content.findExperiment
import com.learnlab.design.LL
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.SecondaryButton
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
            Text("Experiment not found.", color = MaterialTheme.colorScheme.onBackground)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    experiment.outcome,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    experiment.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Normal,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(value = progress, modifier = Modifier.width(120.dp))
                Spacer(Modifier.width(10.dp))
                SecondaryButton(label = "‹", onClick = { onPrev?.invoke() }, enabled = onPrev != null)
                Spacer(Modifier.width(6.dp))
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
                .weight(1f)
                .clipToBounds()
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
        modifier = Modifier.fillMaxSize().padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(t.surface2),
                contentAlignment = Alignment.Center,
            ) {
                Text("⏳", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.size(16.dp))
            Text(
                "This experiment is on the way.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text("From $source.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
