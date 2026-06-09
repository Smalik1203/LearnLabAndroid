package com.learnlab.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark-first, mirrors the web's `:root` palette (near-black + neon cyan).
val LearnLabDarkColorScheme = darkColorScheme(
    primary              = SubjectPhysics,        // #00D4FF
    onPrimary            = Color(0xFF0A0A0F),
    primaryContainer     = Color(0xFF12121A),
    onPrimaryContainer   = SubjectPhysics,
    secondary            = SubjectChemistry,      // #00FF88
    onSecondary          = Color(0xFF0A0A0F),
    tertiary             = SubjectMath,           // #A855F7
    onTertiary           = Color(0xFF0A0A0F),
    background           = Color(0xFF0A0A0F),
    onBackground         = Color(0xFFFFFFFF),
    surface              = Color(0xFF12121A),
    onSurface            = Color(0xFFFFFFFF),
    surfaceVariant       = Color(0xFF1A1A24),
    onSurfaceVariant     = Color(0xBFFFFFFF),
    error                = SubjectBiology,        // #FF6B6B
    onError              = Color(0xFF0A0A0F),
    outline              = Color(0x26FFFFFF),
    outlineVariant       = Color(0x14FFFFFF),
    scrim                = Color(0xCC000000),
)

// Light mode mirrors `[data-theme="light"]`.
val LearnLabLightColorScheme = lightColorScheme(
    primary              = SubjectPhysicsLt,      // #0099CC
    onPrimary            = Color.White,
    secondary            = SubjectChemistryLt,
    onSecondary          = Color.White,
    tertiary             = SubjectMathLt,
    background           = Color(0xFFF8F9FA),
    onBackground         = Color(0xFF1A1A2E),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1A1A2E),
    surfaceVariant       = Color(0xFFF1F2F4),
    onSurfaceVariant     = Color(0xFF4A4A6A),
    outline              = Color(0x26000000),
    error                = SubjectBiologyLt,
    onError              = Color.White,
)

@Composable
fun LearnLabTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isDark) LearnLabDarkColorScheme else LearnLabLightColorScheme,
        typography  = LearnLabTypography,
        content     = content,
    )
}
