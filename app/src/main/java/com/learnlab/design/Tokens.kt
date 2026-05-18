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
    isDark   = true,
    bg       = Color(0xFF0A0C14),
    bgDeep   = Color(0xFF050608),
    surface  = Color(0xFF11141B),
    surface2 = Color(0xFF161A23),
    surface3 = Color(0xFF1F2530),
    line     = Color(0xFF1F2530),
    lineStrong = Color(0xFF2A323F),
    ink50    = Color(0xFFF8FAFC),
    ink200   = Color(0xFFE2E8F0),
    ink400   = Color(0xFF94A3B8),
    ink500   = Color(0xFF64748B),
    ink600   = Color(0xFF475569),
    accent50 = Color(0xFF064E3B),
    accent700= Color(0xFF6EE7B7),
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

// Reusable gradients (mirror tailwind.config.js `backgroundImage`)
@Composable
fun gradHeadline(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF0891B2), Color(0xFF7C3AED)),
)

@Composable
fun gradCta(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF14B8A6)),
)

@Composable
fun gradLogo(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF14B8A6)),
)

@Composable
fun gradProgress(): Brush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF14B8A6)),
)

// Common spacings
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
