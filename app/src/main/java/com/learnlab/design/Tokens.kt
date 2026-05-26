package com.learnlab.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Mirrors src/design/tokens.css exactly. Two palettes (light + dark) with
 * the same names as the Tailwind config, so any class like `bg-surface-2`,
 * `text-ink-400`, `border-accent-500` translates directly.
 */
@Immutable
data class LearnLabTokens(
    val isDark: Boolean,

    // Surfaces
    val bg: Color,
    val bgDeep: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val line: Color,
    val lineStrong: Color,

    // Ink
    val ink50: Color,
    val ink200: Color,
    val ink400: Color,
    val ink500: Color,
    val ink600: Color,

    // Brand emerald (constant across themes)
    val accent300: Color = Color(0xFF6EE7B7),
    val accent400: Color = Color(0xFF34D399),
    val accent500: Color = Color(0xFF10B981),
    val accent600: Color = Color(0xFF059669),

    // Theme-aware accent ends
    val accent50: Color,
    val accent700: Color,

    // Status (kept constant — they're already chosen for contrast on both bgs)
    val rose50: Color = Color(0xFFFFF1F2),
    val rose300: Color = Color(0xFFFDA4AF),
    val rose600: Color = Color(0xFFE11D48),
    val rose700: Color = Color(0xFFBE123C),
    val amber50: Color = Color(0xFFFFFBEB),
    val amber400: Color = Color(0xFFFBBF24),
    val amber700: Color = Color(0xFFB45309),
)

val LightTokens = LearnLabTokens(
    isDark   = false,
    bg       = Color(0xFFFAFAFA),   // zinc-50
    bgDeep   = Color(0xFFFFFFFF),   // white
    surface  = Color(0xFFFFFFFF),   // white
    surface2 = Color(0xFFF4F4F5),   // zinc-100
    surface3 = Color(0xFFE4E4E7),   // zinc-200
    line     = Color(0xFFE4E4E7),   // zinc-200
    lineStrong = Color(0xFFD4D4D8), // zinc-300
    ink50    = Color(0xFF18181B),   // zinc-900
    ink200   = Color(0xFF27272A),   // zinc-800
    ink400   = Color(0xFF52525B),   // zinc-600
    ink500   = Color(0xFF71717A),   // zinc-500
    ink600   = Color(0xFFA1A1AA),   // zinc-400
    accent50 = Color(0xFFECFDF5),   // emerald-50 (kept — matches accent palette)
    accent700= Color(0xFF047857),   // emerald-700
)

val DarkTokens = LearnLabTokens(
    isDark     = true,
    bg         = Color(0xFF18181B),   // zinc-900
    bgDeep     = Color(0xFF09090B),   // zinc-950
    surface    = Color(0xFF27272A),   // zinc-800
    surface2   = Color(0xFF3F3F46),   // zinc-700
    surface3   = Color(0xFF52525B),   // zinc-600
    line       = Color(0xFF3F3F46),   // zinc-700
    lineStrong = Color(0xFF52525B),   // zinc-600
    ink50      = Color(0xFFFAFAFA),   // zinc-50
    ink200     = Color(0xFFE4E4E7),   // zinc-200
    ink400     = Color(0xFFA1A1AA),   // zinc-400
    ink500     = Color(0xFF71717A),   // zinc-500
    ink600     = Color(0xFF52525B),   // zinc-600
    accent300  = Color(0xFF6EE7B7),   // emerald-300
    accent400  = Color(0xFF34D399),   // emerald-400
    accent500  = Color(0xFF10B981),   // emerald-500
    accent600  = Color(0xFF059669),   // emerald-600
    accent50   = Color(0xFF0E2A20),   // dim emerald tint over zinc surface
    accent700  = Color(0xFF34D399),   // emerald-400 (for text on accent50)
)

val LocalTokens = compositionLocalOf { LightTokens }

object LL {
    val tokens: LearnLabTokens
        @Composable get() = LocalTokens.current
}

@Composable
fun ProvideTokens(isDark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTokens provides if (isDark) DarkTokens else LightTokens) {
        content()
    }
}

// Reusable gradients
@Composable
fun gradHeadline(): Brush = Brush.linearGradient(
    colors = listOf(CyanBright, NavyLight, Color(0xFF7C3AED)),
)

@Composable
fun gradCta(): Brush {
    val t = LL.tokens
    return Brush.linearGradient(listOf(t.accent500, t.accent400, t.accent300))
}

@Composable
fun gradLogo(): Brush = Brush.linearGradient(
    colors = listOf(NavyMid, CyanBright),
)

@Composable
fun gradProgress(): Brush {
    val t = LL.tokens
    return Brush.horizontalGradient(listOf(t.accent500, t.accent400))
}

// Common spacings
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
