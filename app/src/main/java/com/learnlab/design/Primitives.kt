package com.learnlab.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Primitives mirror the LearnLab web components: pill CTA with cyan→lime gradient,
 * glassmorphism cards, subject chips, the Labs/Tutorials pill tab bar, and the
 * 3-step level stepper. Shared by all screens and experiments.
 */

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val t = LL.tokens
    val bgBrush: Brush = if (enabled) gradCta() else Brush.linearGradient(listOf(t.surface3, t.surface3))
    // Web CTA uses dark text (--bg-primary) on the bright gradient.
    val textColor = if (enabled) Color(0xFF0A0A0F) else t.ink500

    Box(
        modifier = modifier
            .then(if (enabled) Modifier.shadow(18.dp, RoundedCornerShape(999.dp), spotColor = CtaTeal, ambientColor = CtaTeal) else Modifier)
            .clip(RoundedCornerShape(999.dp))
            .background(bgBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(8.dp)) }
            LLText(label, color = textColor, size = 14.sp, weight = FontWeight.SemiBold)
            if (trailing != null) { Box(Modifier.width(8.dp)); trailing() }
        }
    }
}

@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, t.lineStrong, RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(8.dp)) }
            LLText(
                label,
                color = if (enabled) t.ink50 else t.ink600,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            if (trailing != null) { Box(Modifier.width(8.dp)); trailing() }
        }
    }
}

/** Tertiary action — a subtle filled pill, same shape/height as Primary/Secondary. */
@Composable
fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = t.ink200, size = 14.sp, weight = FontWeight.SemiBold)
    }
}

/** Glassmorphism card: card-overlay fill, subtle border, lg radius, soft shadow. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(20.dp))
            .padding(padding),
    ) { content() }
}

@Composable
fun ChapterBadge(number: Int) {
    val t = LL.tokens
    LLText(
        "CHAPTER $number",
        color = t.ink400,
        size = 10.sp,
        weight = FontWeight.SemiBold,
        letterSpacing = 1.8.sp,
    )
}

@Composable
fun NumberPip(n: Int) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(t.accent50)
            .border(1.dp, t.accent500, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LLText("$n", color = t.accent700, size = 11.sp, weight = FontWeight.Bold)
    }
}

@Composable
fun ProgressBar(value: Float, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(t.surface2),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(value.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(gradProgress()),
        )
    }
}

/** Subject-colored uppercase pill chip (web `.subject-tag` / `.concept-tag`). */
@Composable
fun SubjectTag(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        LLText(label.uppercase(), color = color, size = 11.sp, weight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    }
}

/** Web pill tab bar with a sliding gradient indicator behind the active tab. */
@Composable
fun PillTabBar(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabledTabs: List<Boolean> = tabs.map { true },
) {
    val t = LL.tokens
    val n = tabs.size.coerceAtLeast(1)
    // Cleaner, near-uniform teal→green pill (drops the cyan-blue end) + single-tone glow.
    val pillStart = Color(0xFF2DD4BF)
    val pillEnd = Color(0xFF34D399)
    // Smoother, slightly longer ease-in-out glide (symmetric cubic-bezier(0.42, 0, 0.58, 1)).
    val target by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = tween(durationMillis = 480, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)),
        label = "pillTab",
    )
    // No clip on the track, so the active pill's glow can bleed outside the bar.
    BoxWithConstraints(
        modifier = modifier
            .height(46.dp)
            .background(t.surface2, RoundedCornerShape(999.dp))
            .border(1.dp, t.line, RoundedCornerShape(999.dp))
            .padding(4.dp),
    ) {
        val cellW = maxWidth / n
        // Sliding indicator, one cell wide, with a soft teal→green glow. Offset via the
        // layout-phase lambda so the animation doesn't recompose PillTabBar each frame.
        Box(
            modifier = Modifier
                .offset { IntOffset((cellW.toPx() * target).roundToInt(), 0) }
                .width(cellW)
                .fillMaxHeight()
                .shadow(14.dp, RoundedCornerShape(999.dp), clip = false, spotColor = pillStart, ambientColor = pillStart)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(listOf(pillStart, pillEnd))),
        )
        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { i, label ->
                val on = enabledTabs.getOrElse(i) { true }
                val isSel = i == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = on,
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    val labelColor by animateColorAsState(
                        targetValue = when {
                            isSel -> Color(0xFF0A0A0F)
                            !on -> t.ink600
                            else -> t.ink400
                        },
                        animationSpec = tween(durationMillis = 420),
                        label = "tabLabel",
                    )
                    LLText(
                        label,
                        color = labelColor,
                        size = 13.sp,
                        weight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** Web experiment "level" progress: numbered circles joined by connectors. */
@Composable
fun LevelStepper(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            val active = i <= current
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(
                        if (active) Modifier.background(gradProgress())
                        else Modifier.background(t.surface2).border(2.dp, t.line, CircleShape),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                LLText(
                    "${i + 1}",
                    color = if (active) Color.White else t.ink400,
                    size = 14.sp,
                    weight = FontWeight.Bold,
                )
            }
            if (i < total - 1) {
                Box(
                    Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(if (i < current) gradProgress() else Brush.linearGradient(listOf(t.line, t.line))),
                )
            }
        }
    }
}
