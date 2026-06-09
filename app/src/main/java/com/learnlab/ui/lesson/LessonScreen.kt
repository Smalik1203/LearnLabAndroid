package com.learnlab.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.findExperiment
import com.learnlab.content.subjectOf
import com.learnlab.design.LL
import com.learnlab.design.LLText
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
    onHome: () -> Unit,
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
        ExperimentNav(
            state = state,
            subject = subjectOf(experimentId),
            onBack = onBack,
        )

        // Stage header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LLText(
                experiment.title,
                color = t.ink50,
                size = 24.sp,
                weight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
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
private fun ExperimentNav(
    state: AppState,
    subject: com.learnlab.content.Subject?,
    onBack: () -> Unit,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.bg)
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("LearnLab", color = t.ink50, size = 18.sp, weight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(width = 1.dp, height = 22.dp).background(t.line))
            Spacer(Modifier.width(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Lab",
                    tint = t.ink200,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                LLText("Back to Lab", color = t.ink200, size = 15.sp, weight = FontWeight.Medium)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (subject != null) {
                com.learnlab.design.SubjectTag(subject.displayName, subject.color)
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(t.surface2)
                    .border(1.dp, t.line, androidx.compose.foundation.shape.CircleShape)
                    .clickable { state.toggleTheme() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = t.ink400,
                    modifier = Modifier.size(16.dp),
                )
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
