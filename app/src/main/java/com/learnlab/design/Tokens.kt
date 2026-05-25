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
    bg       = Color(0xFFF8FAFC),
    bgDeep   = Color(0xFFFFFFFF),
    surface  = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1F5F9),
    surface3 = Color(0xFFE2E8F0),
    line     = Color(0xFFE2E8F0),
    lineStrong = Color(0xFFCBD5E1),
    ink50    = Color(0xFF0F172A),
    ink200   = Color(0xFF1E293B),
    ink400   = Color(0xFF475569),
    ink500   = Color(0xFF64748B),
    ink600   = Color(0xFF94A3B8),
    accent50 = Color(0xFFECFDF5),
    accent700= Color(0xFF047857),
)

val DarkTokens = LearnLabTokens(
    isDark     = true,
    bg         = NavyDeep,
    bgDeep     = Color(0xFF050D1A),
    surface    = SurfaceDark,
    surface2   = SurfaceMid,
    surface3   = SurfaceCard,
    line       = SurfaceMid,
    lineStrong = SurfaceElevated,
    ink50      = OnSurfaceHigh,
    ink200     = OnSurfaceHigh,
    ink400     = OnSurfaceMed,
    ink500     = OnSurfaceLow,
    ink600     = Color(0xFF4A6A8A),
    accent300  = CyanSoft,
    accent400  = CyanMid,
    accent500  = CyanBright,
    accent600  = Color(0xFF0099BB),
    accent50   = Color(0xFF0A2040),
    accent700  = CyanBright,
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
fun gradCta(): Brush = Brush.linearGradient(
    colors = listOf(CyanBright, CyanMid, Color(0xFF4ECDC4)),
)

@Composable
fun gradLogo(): Brush = Brush.linearGradient(
    colors = listOf(NavyMid, CyanBright),
)

@Composable
fun gradProgress(): Brush = Brush.horizontalGradient(
    colors = listOf(CyanBright, CyanMid),
)

// Common spacings
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
}

// Corner radii — keep the scale small on purpose
object Radius {
    val sm = 8.dp     // chips, small inputs
    val md = 12.dp    // buttons, list rows
    val lg = 16.dp    // cards
    val xl = 20.dp    // hero / stats cards
    val xxl = 24.dp   // marquee cards
    val pill = 999.dp // full pill / circle
}

// Icon sizes for visual rhythm — pick one, don't invent new ones
object IconSize {
    val xs = 14.dp    // inline glyph
    val sm = 18.dp    // small button icon
    val md = 20.dp    // standard
    val lg = 24.dp    // emphasised / nav
    val xl = 32.dp    // hero
}
