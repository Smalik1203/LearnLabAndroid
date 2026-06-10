package com.learnlab.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.findExperiment
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
    LaunchedEffect(experimentId) {
        progress = 0f
        state.recordRecent(experimentId)
    }
    val controls = remember(experimentId) {
        ExperimentControls(
            onProgress = { progress = it.coerceIn(0f, 1f) },
            onComplete = { _ -> progress = 1f },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        ExperimentNav(
            state = state,
            onBack = onBack,
            onHome = onHome,
        )

        // Stage header: title (left) + progress bar (right). Kept compact so the
        // experiment stage gets the most vertical room.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .padding(horizontal = 20.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LLText(
                experiment.title,
                color = t.ink50,
                size = 17.sp,
                weight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            ProgressBar(value = progress, modifier = Modifier.width(140.dp))
        }

        InstructionBanner(steps = experiment.steps)

        // Fit-to-window: lay the experiment out at a generous design height and uniformly
        // scale it down to the available height (top-left anchored) so its fixed cards never
        // clip or scroll.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(t.bg)
                .clipToBounds(),
        ) {
            val Component = experimentRegistry[experiment.id]
            if (Component != null) {
                FitToWindow(designHeight = 820.dp, modifier = Modifier.fillMaxSize()) {
                    Component(experiment, controls)
                }
            } else {
                ComingSoon(source = experiment.source)
            }
        }

        // Bottom staging bar: Previous (outline) + Next (teal-green), bottom-right.
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            SecondaryButton(
                label = "‹ Previous",
                onClick = { onPrev?.invoke() },
                enabled = onPrev != null,
            )
            Spacer(Modifier.width(10.dp))
            PrimaryButton(
                label = "Next ›",
                onClick = { onNext?.invoke() },
                enabled = onNext != null,
            )
        }
    }
}

@Composable
private fun ExperimentNav(
    state: AppState,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(t.bg)
            .padding(horizontal = 28.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left — Back to Lab
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
            LLText("Back to Lab", color = t.ink200, size = 14.sp, weight = FontWeight.Medium)
        }

        // Right — home + theme toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(Icons.Filled.Home, "Home", onHome)
            Spacer(Modifier.width(10.dp))
            CircleIconButton(
                if (state.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                "Toggle theme",
            ) { state.toggleTheme() }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(t.surface2)
            .border(1.dp, t.line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = t.ink400, modifier = Modifier.size(16.dp))
    }
}

/**
 * Lays [content] out at [designHeight] (so its internal fixed-height cards get full room),
 * then uniformly scales it down — anchored top-left — to exactly fit the available height.
 * Width is measured at availW/scale so it fills after scaling. Guarantees the experiment fits
 * in one window with no clipping and no scrolling. Pointer input is routed through the layer
 * transform, so drags/taps still land correctly.
 */
@Composable
private fun FitToWindow(
    designHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val availW = constraints.maxWidth
        val availH = if (constraints.hasBoundedHeight) constraints.maxHeight else designHeight.roundToPx()
        val designPx = designHeight.roundToPx()
        val scale = if (availH >= designPx) 1f else availH.toFloat() / designPx.toFloat()
        val contentH = if (scale >= 1f) availH else designPx
        val contentW = (availW / scale).toInt()
        val placeable = measurables.first().measure(Constraints.fixed(contentW, contentH))
        layout(availW, availH) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
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
