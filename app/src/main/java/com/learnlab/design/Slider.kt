package com.learnlab.design

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact horizontal slider with a label and a live numeric readout.
 * Used by physics labs (length, angle, speed, height, etc.).
 *
 * When `info` is provided, a small "i" badge appears next to the label; tapping
 * it toggles a tooltip card with the info text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LLSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    step: Float? = null,
    unit: String = "",
    enabled: Boolean = true,
    info: String? = null,
    valueFormat: (Float) -> String = { "%.2f".format(it) },
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    var showInfo by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LLText(label, color = t.ink200, size = 12.sp, weight = FontWeight.Medium)
                if (info != null) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(t.surface2)
                            .border(1.dp, t.lineStrong, CircleShape)
                            .clickable { showInfo = !showInfo },
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText("i", color = t.ink400, size = 10.sp,
                            weight = FontWeight.Bold)
                    }
                }
            }
            LLText("${valueFormat(value)} $unit", color = t.ink500, size = 12.sp)
        }

        if (info != null && showInfo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(8.dp))
                    .clickable { showInfo = false }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                LLText(info, color = t.ink200, size = 11.sp, lineHeight = 15.sp)
            }
        }

        // Thumb grows and a soft glow fades in while the bar is being dragged.
        val interaction = remember { MutableInteractionSource() }
        val dragged by interaction.collectIsDraggedAsState()
        val pressed by interaction.collectIsPressedAsState()
        val active = dragged || pressed
        val thumbSize by animateDpAsState(
            targetValue = if (active) 20.dp else 18.dp,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            label = "thumbSize",
        )
        val glowAlpha by animateFloatAsState(
            targetValue = if (active) 0.55f else 0f,
            animationSpec = tween(180),
            label = "thumbGlow",
        )
        // Halo widens as it fades in, mirroring the web's 8px→16px glow.
        val glowSize by animateDpAsState(
            targetValue = if (active) 28.dp else 16.dp,
            animationSpec = tween(180),
            label = "thumbGlowSize",
        )

        Box(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val steps = if (step != null && step > 0f) {
                val n = ((max - min) / step).toInt() - 1
                if (n > 0) n else 0
            } else 0
            Slider(
                value = value.coerceIn(min, max),
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                interactionSource = interaction,
                steps = steps,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = ExperimentAccent,
                    activeTrackColor = ExperimentAccent,
                    inactiveTrackColor = t.surface3,
                    // No per-step tick dots — they read as microscopic noise. Snapping
                    // (the `steps` param) is preserved; the value readout shows the exact value.
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                thumb = {
                    // Root clipped to a circle so any press/hover state-layer stays round
                    // (no square box around the dragger).
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(glowSize)
                                .graphicsLayer { alpha = glowAlpha }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(ExperimentAccent.copy(alpha = 0.6f), Color.Transparent),
                                    ),
                                ),
                        )
                        // Light dragger with a teal ring — crisp on the dark track.
                        Box(
                            modifier = Modifier
                                .size(thumbSize)
                                .clip(CircleShape)
                                .background(if (enabled) Color(0xFFE8EBF0) else t.surface3)
                                .border(2.dp, if (enabled) ExperimentAccent else t.lineStrong, CircleShape),
                        )
                    }
                },
            )
        }
    }
}
