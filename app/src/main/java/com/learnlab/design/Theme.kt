package com.learnlab.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LearnLabDarkColorScheme = darkColorScheme(
    primary              = CyanBright,
    onPrimary            = NavyDeep,
    primaryContainer     = NavyMid,
    onPrimaryContainer   = CyanSoft,
    secondary            = AmberBright,
    onSecondary          = NavyDeep,
    secondaryContainer   = Color(0xFF3D2800),
    onSecondaryContainer = AmberSoft,
    tertiary             = EmeraldBright,
    onTertiary           = NavyDeep,
    tertiaryContainer    = Color(0xFF00391A),
    onTertiaryContainer  = EmeraldSoft,
    background           = NavyDeep,
    onBackground         = OnSurfaceHigh,
    surface              = SurfaceDark,
    onSurface            = OnSurfaceHigh,
    surfaceVariant       = SurfaceMid,
    onSurfaceVariant     = OnSurfaceMed,
    error                = ErrorRed,
    onError              = NavyDeep,
    outline              = OnSurfaceLow,
    outlineVariant       = SurfaceElevated,
    scrim                = Color(0xCC000000),
)

val LearnLabLightColorScheme = lightColorScheme(
    primary              = NavyMid,
    onPrimary            = Color.White,
    primaryContainer     = CyanSoft,
    onPrimaryContainer   = NavyDeep,
    secondary            = AmberMid,
    onSecondary          = NavyDeep,
    background           = Color(0xFFF0F4FF),
    onBackground         = NavyDeep,
    surface              = Color.White,
    onSurface            = NavyDeep,
    surfaceVariant       = Color(0xFFEDF1FF),
    onSurfaceVariant     = NavyMid,
    outline              = NavyLight,
    error                = ErrorRed,
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
