package com.learnlab.lessons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.learnlab.design.LL

/**
 * Theme-aware palette for the Read/Reflect pages. Every accent surface,
 * border and text colour has a light + dark variant so the textbook-style
 * pastel boxes (yellow scroll, purple sticky, green callout) stay readable
 * in both themes instead of washing out on the dark surface.
 *
 * Hue groups:
 *  - Violet  → quotes, sticky-note questions, section accents
 *  - Amber   → scroll quotes, "More to know!" callouts, case studies
 *  - Emerald → success/conclusion callouts, student-girl bubble
 *  - Sky     → info callouts, teacher bubble
 *  - Rose    → warning callouts, grandma bubble
 *  - Indigo  → info callouts (alt)
 */
data class LessonHue(
    val surface: Color,        // card background
    val surfaceStrong: Color,  // bolder accent (icon backplate, badge bg)
    val border: Color,         // 1.dp stroke
    val ink: Color,            // primary text on the surface
    val accent: Color,         // headings, "CASE 1" label text
)

class LessonPalette(isDark: Boolean) {
    val violet  = if (!isDark) LessonHue(Color(0xFFEDE9FE), Color(0xFFC4B5FD), Color(0xFFC4B5FD), Color(0xFF1E1B4B), Color(0xFF5B21B6))
                  else        LessonHue(Color(0xFF2E1065), Color(0xFF6D28D9), Color(0xFF6D28D9), Color(0xFFEDE9FE), Color(0xFFC4B5FD))

    val amber   = if (!isDark) LessonHue(Color(0xFFFEF3C7), Color(0xFFFCD34D), Color(0xFFFCD34D), Color(0xFF422006), Color(0xFF92400E))
                  else        LessonHue(Color(0xFF3F2A05), Color(0xFFB45309), Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFFFBBF24))

    val emerald = if (!isDark) LessonHue(Color(0xFFDCFCE7), Color(0xFF86EFAC), Color(0xFF86EFAC), Color(0xFF052E16), Color(0xFF166534))
                  else        LessonHue(Color(0xFF052E1B), Color(0xFF15803D), Color(0xFF15803D), Color(0xFFDCFCE7), Color(0xFF6EE7B7))

    val sky     = if (!isDark) LessonHue(Color(0xFFE0F2FE), Color(0xFF7DD3FC), Color(0xFF7DD3FC), Color(0xFF082F49), Color(0xFF0369A1))
                  else        LessonHue(Color(0xFF082F49), Color(0xFF0369A1), Color(0xFF0369A1), Color(0xFFE0F2FE), Color(0xFF7DD3FC))

    val rose    = if (!isDark) LessonHue(Color(0xFFFFE4E6), Color(0xFFFCA5A5), Color(0xFFFCA5A5), Color(0xFF4C0519), Color(0xFFBE123C))
                  else        LessonHue(Color(0xFF3F0617), Color(0xFFB91C1C), Color(0xFFB91C1C), Color(0xFFFFE4E6), Color(0xFFFB7185))

    val indigo  = if (!isDark) LessonHue(Color(0xFFE0E7FF), Color(0xFFA5B4FC), Color(0xFFA5B4FC), Color(0xFF1E1B4B), Color(0xFF4338CA))
                  else        LessonHue(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF4338CA), Color(0xFFE0E7FF), Color(0xFFA5B4FC))

    val pageBgBrush: Brush =
        if (!isDark)
            Brush.verticalGradient(listOf(Color(0xFFFBFAF6), Color(0xFFF6F3EC)))
        else
            Brush.verticalGradient(listOf(Color(0xFF0B0F1A), Color(0xFF0A0C14)))
}

@Composable
fun lessonPalette(): LessonPalette = LessonPalette(LL.tokens.isDark)
