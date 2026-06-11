package com.learnlab.ui.lesson

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.findExperiment
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.MeshBackground
import com.learnlab.design.PrimaryButton
import com.learnlab.design.ProgressBar
import com.learnlab.design.Radius
import com.learnlab.design.SecondaryButton
import com.learnlab.design.Spacing
import com.learnlab.design.bounceClickable
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
        Box(modifier = Modifier.fillMaxSize()) {
            MeshBackground()
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopBar(state = state, title = "Not Found", showBack = true, onBack = onBack)
                LLText("Experiment not found.", color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold)
            }
        }
        return
    }

    var progress by remember(experimentId) { mutableStateOf(0f) }
    var activeStep by remember(experimentId) { mutableStateOf(0) }
    LaunchedEffect(experimentId) { progress = 0f }
    val controls = remember(experimentId) {
        ExperimentControls(
            onProgress = { progress = it.coerceIn(0f, 1f) },
            onComplete = { _ -> progress = 1f },
            onStep = { activeStep = it },
        )
    }

    val headerBg = if (t.isDark) t.surface.copy(alpha = 0.45f) else t.surface.copy(alpha = 0.85f)
    val headerBorder = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (t.isDark) 0.15f else 0.4f), Color.Transparent)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                state = state,
                title = "Back",
                showBack = true,
                onBack = onBack,
                onHomeClick = onHome,
            )

            // Stage header: Floating glassmorphic dashboard panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(headerBg)
                        .border(1.dp, headerBorder, RoundedCornerShape(Radius.md))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LLText(
                        experiment.title,
                        color = t.ink50,
                        size = 20.sp,
                        weight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 16.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProgressBar(value = progress, modifier = Modifier.width(140.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecondaryButton(
                                label = "‹ Prev", 
                                onClick = { onPrev?.invoke() }, 
                                enabled = onPrev != null
                            )
                            if (progress >= 1f && onNext != null) {
                                PrimaryButton(label = "Next Lab ›", onClick = { onNext.invoke() })
                            } else {
                                SecondaryButton(
                                    label = "Next Lab ›", 
                                    onClick = { onNext?.invoke() }, 
                                    enabled = onNext != null
                                )
                            }
                        }
                    }
                }
            }

            InstructionBanner(steps = experiment.steps, activeStep = activeStep)

            // Experiment Workspace Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(t.bgDeep.copy(alpha = if (t.isDark) 0.45f else 0.85f))
                    .border(
                        1.dp, 
                        if (t.isDark) t.lineStrong.copy(alpha = 0.2f) else t.lineStrong.copy(alpha = 0.5f), 
                        RoundedCornerShape(Radius.lg)
                    ),
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
}

@Composable
private fun ComingSoon(source: String) {
    val t = LL.tokens
    val glowColor = t.accent500
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")
    
    // Slow bouncing rotation for hourglass
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hourglassBounce"
    )

    Box(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(t.surface2.copy(alpha = 0.3f))
                    .border(1.dp, t.line.copy(alpha = 0.4f), RoundedCornerShape(Radius.pill))
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(Radius.pill), spotColor = glowColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassEmpty,
                    contentDescription = null,
                    tint = t.accent700,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer(rotationZ = rotation),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LLText(
                    "This virtual lab module is on the way",
                    color = t.ink50,
                    size = 16.sp,
                    weight = FontWeight.Bold,
                )
                LLText(
                    "NCERT Curriculum reference: $source", 
                    color = t.ink400, 
                    size = 13.sp,
                    weight = FontWeight.Medium
                )
            }
        }
    }
}
