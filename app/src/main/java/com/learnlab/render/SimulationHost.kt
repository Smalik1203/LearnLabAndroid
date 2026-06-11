package com.learnlab.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SimHud
import com.learnlab.design.SimType

/** Handle the overlay uses to talk to the running scene. */
class SimHostHandle internal constructor(
    private val viewRef: () -> SimSurfaceView?,
) {
    fun command(code: Int, a: Float = 0f, b: Float = 0f) {
        viewRef()?.command(code, a, b)
    }
}

private sealed interface HostState {
    data object Loading : HostState
    data object Ready : HostState
    data class Failed(val error: Throwable) : HostState
}

/**
 * Hosts a [SimScene] inside Compose. Owns the [SimSurfaceView] lifecycle and
 * renders honest, non-happy-path states:
 *  - LOADING  while the Engine spins up and the scene builds its GL resources.
 *  - READY    once the first frame's progress lands (proves the loop is alive).
 *  - FAILED   if the scene throws (e.g. a material .filamat is missing on a
 *             device with a broken driver) — with a retry that rebuilds it.
 *  - EMPTY    when [empty] is true (no valid config for this activity).
 *
 * The simulation is the hero: the surface fills the whole box, chrome floats on
 * top via [overlay] over a bottom gradient so it recedes into the scene.
 */
@Composable
fun SimulationHost(
    sceneFactory: () -> SimScene,
    tier: DeviceTier,
    modifier: Modifier = Modifier,
    empty: Boolean = false,
    onProgress: (Float) -> Unit = {},
    overlay: @Composable (SimHostHandle) -> Unit = {},
) {
    if (empty) { SimEmpty(modifier); return }

    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember { mutableStateOf<HostState>(HostState.Loading) }
    // Bump to force a fresh SurfaceView/Engine after a failure (retry).
    var attempt by remember { mutableStateOf(0) }
    var viewHolder by remember { mutableStateOf<SimSurfaceView?>(null) }
    val handle = remember { SimHostHandle { viewHolder } }

    Box(modifier.fillMaxSize().background(Color(0xFF030712))) {
        key(attempt) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SimSurfaceView(
                        context = ctx,
                        scene = sceneFactory(),
                        tier = tier,
                        onProgress = { p ->
                            if (state is HostState.Loading) state = HostState.Ready
                            onProgress(p)
                        },
                        onError = { t -> state = HostState.Failed(t) },
                    ).also { viewHolder = it }
                },
            )
        }

        // Lifecycle: pause the render loop in the background to stop draining the
        // battery and heating the SoC; resume on return. Release on dispose frees
        // the Filament Engine (a hard leak otherwise).
        DisposableEffect(lifecycleOwner, attempt) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> viewHolder?.onLifecyclePause()
                    Lifecycle.Event.ON_RESUME -> viewHolder?.onLifecycleResume()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                viewHolder?.release()
                viewHolder = null
            }
        }

        when (val s = state) {
            is HostState.Loading -> SimLoading()
            is HostState.Failed -> SimError(s.error) { state = HostState.Loading; attempt++ }
            is HostState.Ready -> {
                // Bottom scrim + overlay only once we're actually rendering.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                0.62f to SimHud.dockGradientTop,
                                1f to SimHud.dockGradientBottom,
                            ),
                        ),
                )
                overlay(handle)
            }
        }
    }
}

@Composable
private fun SimLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = SimHud.horizontal, strokeWidth = 4.dp)
            LLText(
                "Preparing simulation…",
                color = SimHud.onScrimDim,
                size = SimType.hudLabel,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}

@Composable
private fun SimError(error: Throwable, onRetry: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(SimHud.scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            LLText(
                "This simulation couldn't start",
                color = SimHud.onScrim,
                size = SimType.calloutTitle,
                weight = SimType.displayWeight,
                align = TextAlign.Center,
            )
            LLText(
                // Surface the real reason — a black screen teaches the teacher
                // nothing. Common case here: a missing/incompatible material.
                error.message ?: error.javaClass.simpleName,
                color = SimHud.onScrimDim,
                size = 18.sp,
                align = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            PrimaryButton(label = "Try again", onClick = onRetry)
        }
    }
}

@Composable
private fun SimEmpty(modifier: Modifier) {
    Box(
        modifier.fillMaxSize().background(LL.tokens.bg),
        contentAlignment = Alignment.Center,
    ) {
        LLText(
            "No simulation is configured for this activity yet.",
            color = LL.tokens.ink400,
            size = SimType.hudLabel,
            align = TextAlign.Center,
            modifier = Modifier.padding(40.dp),
        )
    }
}
