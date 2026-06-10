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

    // Status — 50 and 700 are theme-aware so dark mode gets vivid fills + bright text
    val rose50: Color,
    val rose700: Color,
    val amber50: Color,
    val amber700: Color,
    // Constants — same contrast on both light and dark surfaces
    val rose300: Color = Color(0xFFFDA4AF),
    val rose600: Color = Color(0xFFE11D48),
    val amber400: Color = Color(0xFFFBBF24),
)

// Light mode mirrors `[data-theme="light"]` in the web's styles.css.
val LightTokens = LearnLabTokens(
    isDark   = false,
    bg       = Color(0xFFF8F9FA),   // --bg-primary
    bgDeep   = Color(0xFFEEF0F3),   // --bg-darker
    surface  = Color(0xFFFFFFFF),   // --bg-secondary
    surface2 = Color(0xFFF1F2F4),   // card / hover
    surface3 = Color(0xFFE4E6EA),
    line     = Color(0x14000000),   // --border-subtle  rgba(0,0,0,0.08)
    lineStrong = Color(0x26000000), // --border-glow    rgba(0,0,0,0.15)
    ink50    = Color(0xFF1A1A2E),   // --text-primary
    ink200   = Color(0xFF4A4A6A),   // --text-secondary
    ink400   = Color(0xFF8888A8),   // --text-muted
    ink500   = Color(0xFF9A9AB5),
    ink600   = Color(0xFFB5B5C8),
    accent300 = Color(0xFF2DD4BF),
    accent400 = Color(0xFF14B8A6),
    accent500 = Color(0xFF0D9488),  // muted teal-green (light)
    accent600 = Color(0xFF0F766E),
    accent50  = Color(0xFFCCFBF1),  // light teal tint behind accent700 text
    accent700 = Color(0xFF0F766E),
    amber50  = Color(0xFFFEF3C7),
    amber700 = Color(0xFFB45309),
    rose50   = Color(0xFFFFE4E6),
    rose700  = Color(0xFFBE123C),
)

// Dark mode (default) mirrors `:root` in the web's styles.css — near-black + neon.
val DarkTokens = LearnLabTokens(
    isDark     = true,
    bg         = Color(0xFF0A0A0F),   // --bg-primary
    bgDeep     = Color(0xFF050508),   // --bg-darker
    surface    = Color(0xFF12121A),   // --bg-secondary / --bg-card-solid
    surface2   = Color(0xFF1A1A24),   // card hover
    surface3   = Color(0xFF24242F),
    line       = Color(0x14FFFFFF),   // --border-subtle  rgba(255,255,255,0.08)
    lineStrong = Color(0x26FFFFFF),   // --border-glow    rgba(255,255,255,0.15)
    ink50      = Color(0xFFFFFFFF),   // --text-primary
    ink200     = Color(0xBFFFFFFF),   // --text-secondary rgba(255,255,255,0.75)
    ink400     = Color(0x73FFFFFF),   // --text-muted     rgba(255,255,255,0.45)
    ink500     = Color(0x59FFFFFF),
    ink600     = Color(0x40FFFFFF),
    accent300  = Color(0xFF5EEAD4),   // light teal
    accent400  = Color(0xFF2DD4BF),
    accent500  = Color(0xFF2DD4BF),   // teal-green (primary interactive)
    accent600  = Color(0xFF0D9488),   // darker teal for white-text fills
    accent50   = Color(0x332DD4BF),   // translucent teal fill on dark surface
    accent700  = Color(0xFF5EEAD4),   // bright teal text on accent50
    amber50    = Color(0x33FBBF24),
    amber700   = Color(0xFFFBBF24),
    rose50     = Color(0x33FF6B6B),
    rose700    = Color(0xFFFF6B6B),
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

// Reusable gradients — mirror the web's gradient-text / CTA / progress treatments.
// Hero/headline text: physics → chemistry → mathematics (cyan → lime → purple).
@Composable
fun gradHeadline(): Brush = Brush.linearGradient(
    colors = listOf(SubjectPhysics, SubjectChemistry, SubjectMath),
)

// CTA buttons & active tab: teal → green, matching the web "Begin learning" pill.
val CtaTeal = Color(0xFF22D3EE)
val CtaGreen = Color(0xFF34D399)

@Composable
fun gradCta(): Brush = Brush.linearGradient(
    colors = listOf(CtaTeal, CtaGreen),
)

@Composable
fun gradLogo(): Brush = Brush.linearGradient(
    colors = listOf(CtaTeal, CtaGreen),
)

// Progress / level indicators use the unified experiment accent (blue).
@Composable
fun gradProgress(): Brush = Brush.horizontalGradient(
    colors = listOf(ExperimentAccent, Color(0xFF34D399)),
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
