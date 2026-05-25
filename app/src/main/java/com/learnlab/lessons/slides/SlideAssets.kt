package com.learnlab.lessons.slides

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/** Loads "figures/<path>" from APK assets. Returns null silently if missing. */
@Composable
fun loadFigureBitmap(path: String?): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    val ctx = LocalContext.current
    var bmp by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bmp = try {
            ctx.assets.open("figures/$path").use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Throwable) { null }
    }
    return bmp
}

/** Hex like "#10B981" → Color, with a safe fallback. */
fun parseHexColor(hex: String?, fallback: Color = Color(0xFF10B981)): Color {
    if (hex == null) return fallback
    return try {
        val s = hex.removePrefix("#")
        val v = s.toLong(16)
        if (s.length == 6) Color((0xFF000000L or v).toInt()) else Color(v.toInt())
    } catch (_: Throwable) { fallback }
}
