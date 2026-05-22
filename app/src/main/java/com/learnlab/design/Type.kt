package com.learnlab.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun scaleTypography(factor: Float): Typography {
    if (factor == 1f) return LearnLabTypography
    fun TextStyle.s() = copy(
        fontSize   = (fontSize.value   * factor).sp,
        lineHeight = (lineHeight.value * factor).sp,
    )
    return LearnLabTypography.copy(
        displayLarge   = LearnLabTypography.displayLarge.s(),
        displayMedium  = LearnLabTypography.displayMedium.s(),
        displaySmall   = LearnLabTypography.displaySmall.s(),
        headlineLarge  = LearnLabTypography.headlineLarge.s(),
        headlineMedium = LearnLabTypography.headlineMedium.s(),
        headlineSmall  = LearnLabTypography.headlineSmall.s(),
        titleLarge     = LearnLabTypography.titleLarge.s(),
        titleMedium    = LearnLabTypography.titleMedium.s(),
        titleSmall     = LearnLabTypography.titleSmall.s(),
        bodyLarge      = LearnLabTypography.bodyLarge.s(),
        bodyMedium     = LearnLabTypography.bodyMedium.s(),
        bodySmall      = LearnLabTypography.bodySmall.s(),
        labelLarge     = LearnLabTypography.labelLarge.s(),
        labelMedium    = LearnLabTypography.labelMedium.s(),
        labelSmall     = LearnLabTypography.labelSmall.s(),
    )
}

val LearnLabTypography = Typography(
    // Display — chapter/lesson titles on curriculum browser
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 72.sp, lineHeight = 80.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 56.sp, lineHeight = 64.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 52.sp),

    // Headline — slide headings in lesson player
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 40.sp, lineHeight = 48.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 32.sp, lineHeight = 40.sp),

    // Title — card titles, section headers
    titleLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 24.sp, lineHeight = 32.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 20.sp, lineHeight = 28.sp),

    // Body — PRD requires body ≥ 32 sp for smart-board readability
    bodyLarge  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 44.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 38.sp),
    bodySmall  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),

    // Label — badges, chips, metadata
    labelLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
)
