package com.learnlab.design

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.learnlab.android.R

/**
 * App-wide typography. Inter for body, Inter Display for big headings.
 * Both bundled in res/font/ so the app is fully offline-capable (rule §7).
 */
object LearnLabFonts {

    /** Body / UI text. Inter at multiple weights. */
    val Body: FontFamily = FontFamily(
        Font(R.font.inter_regular,  FontWeight.Normal),
        Font(R.font.inter_medium,   FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold,     FontWeight.Bold),
    )

    /** Display / headings. Inter Display is optically tuned for large sizes. */
    val Display: FontFamily = FontFamily(
        Font(R.font.inter_display_bold,       FontWeight.Bold),
        Font(R.font.inter_display_extrabold,  FontWeight.ExtraBold),
        Font(R.font.inter_display_extrabold,  FontWeight.Black),
    )
}
