package com.learnlab.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring


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

    // Status — 50 and 700 are theme-aware so dark mode gets vivid fills + bright text
    val rose50: Color,
    val rose700: Color,
    val amber50: Color,
    val amber700: Color,
    // Constants — same contrast on both light and dark surfaces
    val rose300: Color = Color(0xFFFDA4AF),
    val rose600: Color = Color(0xFFE11D48),
    val amber400: Color = Color(0xFFFBBF24),

    // Motion-semantic colors — used by physics engines to give the same
    // concept the same color across diagram, equation, and explanation.
    // Same value on both themes; chosen for readability on dark surfaces.
    //   horizontal motion = green   (forward / range)
    //   vertical motion   = blue    (up-down / height)
    //   gravity           = orange  (the force acting)
    val motionHorizontal: Color = Color(0xFF84CC16), // lime-500 — distinct from brand emerald
    val motionVertical: Color   = Color(0xFF60A5FA), // blue-400
    val motionGravity: Color    = Color(0xFFFB923C), // orange-400
)

val LightTokens = LearnLabTokens(
    isDark   = false,
    bg       = Color(0xFFF8FAFC),   // Slate-50: crisp clean background
    bgDeep   = Color(0xFFFFFFFF),   // White
    surface  = Color(0xFFFFFFFF),   // White
    surface2 = Color(0xFFF1F5F9),   // Slate-100
    surface3 = Color(0xFFE2E8F0),   // Slate-200
    line     = Color(0xFFE2E8F0),   // Slate-200
    lineStrong = Color(0xFFCBD5E1), // Slate-300
    ink50    = Color(0xFF0F172A),   // Slate-900: high contrast text
    ink200   = Color(0xFF1E293B),   // Slate-800
    ink400   = Color(0xFF475569),   // Slate-600
    ink500   = Color(0xFF64748B),   // Slate-500
    ink600   = Color(0xFF94A3B8),   // Slate-400
    accent50 = Color(0xFFECFEFF),   // Cyan-50
    accent700= Color(0xFF0E7490),   // Cyan-700
    amber50  = Color(0xFFFEF3C7),   // Amber-50
    amber700 = Color(0xFFB45309),   // Amber-700
    rose50   = Color(0xFFFFF1F2),   // Rose-50
    rose700  = Color(0xFFBE123C),   // Rose-700
)

val DarkTokens = LearnLabTokens(
    isDark     = true,
    bg         = Color(0xFF030712),   // Slate-950: deep space ink black
    bgDeep     = Color(0xFF0F172A),   // Slate-900: deep surface base
    surface    = Color(0xFF1E293B),   // Slate-800: glass surface base
    surface2   = Color(0xFF334155),   // Slate-700
    surface3   = Color(0xFF475569),   // Slate-600
    line       = Color(0xFF334155),   // Slate-700
    lineStrong = Color(0xFF475569),   // Slate-600
    ink50      = Color(0xFFF8FAFC),   // Slate-50: bright text
    ink200     = Color(0xFFE2E8F0),   // Slate-200
    ink400     = Color(0xFF94A3B8),   // Slate-400
    ink500     = Color(0xFF64748B),   // Slate-500
    ink600     = Color(0xFF475569),   // Slate-600
    accent300  = Color(0xFF22D3EE),   // Cyan-300
    accent400  = Color(0xFF06B6D4),   // Cyan-500
    accent500  = Color(0xFF0891B2),   // Cyan-600
    accent600  = Color(0xFF0E7490),   // Cyan-700
    accent50   = Color(0xFF164E63),   // Cyan-900: glowing dark base
    accent700  = Color(0xFF22D3EE),   // Cyan-300: neon bright text
    amber50    = Color(0xFF78350F),   // Amber dark fill
    amber700   = Color(0xFFFCD34D),   // Amber bright text
    rose50     = Color(0xFF881337),   // Rose dark fill
    rose700    = Color(0xFFFDA4AF),   // Rose bright text
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

// Spring animations for tactile feel
object LLAnimation {
    val TactileSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val SmoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

// Reusable gradients - upgraded to neon/violet combinations
@Composable
fun gradHeadline(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFFD946EF)),
)

@Composable
fun gradCta(): Brush {
    return Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6)))
}

@Composable
fun gradLogo(): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
)

@Composable
fun gradProgress(): Brush {
    return Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF10B981)))
}

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

// Corner radii — slightly increased for premium rounded dashboard aesthetics
object Radius {
    val sm = 10.dp     // chips, small inputs
    val md = 16.dp    // buttons, list rows
    val lg = 24.dp    // cards
    val xl = 32.dp    // hero / stats cards
    val xxl = 40.dp   // marquee cards
    val pill = 999.dp // full pill / circle
}

// Icon sizes for visual rhythm
object IconSize {
    val xs = 14.dp    // inline glyph
    val sm = 18.dp    // small button icon
    val md = 20.dp    // standard
    val lg = 24.dp    // emphasised / nav
    val xl = 32.dp    // hero
}

