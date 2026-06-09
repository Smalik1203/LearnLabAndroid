package com.learnlab.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// All styles use Inter (the web typeface). Sizes follow the web's proportions
// (hero ~44–80px, body ~16px) rather than the old smart-board ≥32sp scale —
// a deliberate deviation from CLAUDE.md §8 to match the web design, per request.
val LearnLabTypography = Typography(
    // Display — hero / landing headline (gradient text)
    displayLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 62.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,      fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp),
    displaySmall  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,      fontSize = 34.sp, lineHeight = 42.sp),

    // Headline — section + page titles
    headlineLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,     fontSize = 30.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,     fontSize = 26.sp, lineHeight = 34.sp),
    headlineSmall  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),

    // Title — card titles, modal titles
    titleLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),

    // Body — web body is ~1rem; line-height 1.5–1.6
    bodyLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp, letterSpacing = 0.3.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = 0.3.sp),
    bodySmall  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),

    // Label — badges, chips, buttons, metadata
    labelLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)
