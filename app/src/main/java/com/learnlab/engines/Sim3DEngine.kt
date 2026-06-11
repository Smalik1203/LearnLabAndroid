package com.learnlab.engines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learnlab.design.LLText
import com.learnlab.design.bounceClickable
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.design.SimHud
import com.learnlab.design.SimMetrics
import com.learnlab.design.SimType
import com.learnlab.render.DeviceTier
import com.learnlab.render.SimHostHandle
import com.learnlab.render.SimulationHost
import com.learnlab.sim.projectile.ProjectileSimConfig
import com.learnlab.sim.projectile.ProjectileSimScene
import com.learnlab.store.ExperimentControls

/**
 * Bridges a 3D [com.learnlab.render.SimScene] into LearnLab's existing engine
 * registry contract ([ExperimentComposable]). The shell stays unchanged: it
 * still maps an activity id to a Composable that takes (Experiment, controls).
 *
 * NOTE on config: the frozen `Experiment` schema has no typed `config` blob yet
 * (§5 forbids changing it without team review), so this uses the template's
 * defaults. When the schema gains a config field, parse it here and pass the
 * decoded [ProjectileSimConfig] in — the scene already takes one.
 */
@Composable
fun ProjectileSim3D(controls: ExperimentControls) {
    val context = LocalContext.current
    val tier = remember { DeviceTier.detect(context) }
    val config = remember { ProjectileSimConfig() }

    SimulationHost(
        sceneFactory = { ProjectileSimScene(context.applicationContext.assets, config) },
        tier = tier,
        onProgress = { p ->
            controls.onProgress(p)
            if (p >= 1f) controls.onComplete(p)
        },
        overlay = { handle -> ProjectileControls(handle, config) },
    )
}

/**
 * The recessed control dock. Floats over the bottom scrim the host draws, so the
 * simulation stays the hero. Big hit targets ([SimMetrics.controlTouch]) and
 * large readouts ([SimType.readout]) keep it usable standing up and legible from
 * the back of the room.
 */
@Composable
private fun ProjectileControls(handle: SimHostHandle, config: ProjectileSimConfig) {
    var angle by remember { mutableFloatStateOf(config.initialAngleDeg) }
    var speed by remember { mutableFloatStateOf(config.initialSpeedMs) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(SimMetrics.dockPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Stepper(
                    label = "ANGLE",
                    value = "${angle.toInt()}°",
                    tint = SimHud.vertical,
                    onMinus = {
                        angle = (angle - 5f).coerceAtLeast(config.minAngleDeg)
                        handle.command(ProjectileSimConfig.CMD_SET_ANGLE, angle)
                    },
                    onPlus = {
                        angle = (angle + 5f).coerceAtMost(config.maxAngleDeg)
                        handle.command(ProjectileSimConfig.CMD_SET_ANGLE, angle)
                    },
                )
                Stepper(
                    label = "SPEED",
                    value = "${speed.toInt()} m/s",
                    tint = SimHud.horizontal,
                    onMinus = {
                        speed = (speed - 1f).coerceAtLeast(config.minSpeedMs)
                        handle.command(ProjectileSimConfig.CMD_SET_SPEED, speed)
                    },
                    onPlus = {
                        speed = (speed + 1f).coerceAtMost(config.maxSpeedMs)
                        handle.command(ProjectileSimConfig.CMD_SET_SPEED, speed)
                    },
                )
            }

            Row(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(SimMetrics.controlGap),
            ) {
                SecondaryButton(
                    label = "Reset",
                    onClick = { handle.command(ProjectileSimConfig.CMD_RESET) },
                )
                PrimaryButton(
                    label = "Launch",
                    onClick = { handle.command(ProjectileSimConfig.CMD_LAUNCH) },
                )
            }
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LLText(label, color = SimHud.onScrimDim, size = SimType.hudLabel, weight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton("–", onMinus)
            LLText(
                value,
                color = tint,
                size = SimType.readout,
                weight = SimType.readoutWeight,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
            StepButton("+", onPlus)
        }
    }
}

@Composable
private fun StepButton(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(SimMetrics.controlTouch)
            .clip(CircleShape)
            .background(SimHud.scrimSoft)
            .bounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        LLText(glyph, color = SimHud.onScrim, size = SimType.readout, weight = FontWeight.Bold)
    }
}
