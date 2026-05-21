package com.learnlab.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material3 buttons are aggressively opinionated. These tiny primitives
 * mirror the React components exactly: same paddings, same colors, same
 * border-radius, same disabled treatment.
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
    val textColor = if (enabled) Color.White else t.ink500

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(6.dp)) }
            LLText(
                label,
                color = textColor,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            if (trailing != null) { Box(Modifier.width(6.dp)); trailing() }
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
            .background(t.surface)
            .border(1.dp, t.lineStrong, RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { leading(); Box(Modifier.width(6.dp)) }
            LLText(
                label,
                color = if (enabled) t.ink200 else t.ink600,
                size = 14.sp,
                weight = FontWeight.SemiBold,
            )
            if (trailing != null) { Box(Modifier.width(6.dp)); trailing() }
        }
    }
}

@Composable
fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .border(1.dp, t.lineStrong, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = t.ink200, size = 13.sp, weight = FontWeight.Medium)
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val t = LL.tokens
    Surface(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = t.surface,
        tonalElevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

@Composable
fun ChapterBadge(number: Int) {
    val t = LL.tokens
    LLText(
        "CHAPTER $number",
        color = t.ink500,
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
            .border(1.dp, t.accent300, CircleShape),
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
            .background(t.surface3),
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
