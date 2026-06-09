package com.learnlab.design

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.learnlab.android.R

/**
 * Inter — the typeface used by the LearnLab web app. Bundled as static TTFs in res/font
 * (offline-first: no Google Fonts network fetch). Weights 300–800 mirror the web's
 * `Inter:wght@300;400;500;600;700;800` load.
 */
val Inter = FontFamily(
    Font(R.font.inter_light,     FontWeight.Light),      // 300
    Font(R.font.inter_regular,   FontWeight.Normal),     // 400
    Font(R.font.inter_medium,    FontWeight.Medium),     // 500
    Font(R.font.inter_semibold,  FontWeight.SemiBold),   // 600
    Font(R.font.inter_bold,      FontWeight.Bold),       // 700
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),  // 800
)
