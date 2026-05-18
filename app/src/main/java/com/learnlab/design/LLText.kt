package com.learnlab.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Thin wrapper around Material3 `Text` so we don't repeat font config everywhere.
 * Matches the web app's `font-family: Inter, system-ui, sans-serif` defaults
 * plus per-call sizes/weights.
 */
@Composable
fun LLText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LL.tokens.ink200,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        lineHeight = if (lineHeight == TextUnit.Unspecified) size * 1.4f else lineHeight,
        letterSpacing = letterSpacing,
        textAlign = align,
        maxLines = maxLines,
        overflow = overflow,
    )
}
