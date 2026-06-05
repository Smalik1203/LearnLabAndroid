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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Resolves an image path to an ImageBitmap, supporting three forms:
 *  - "https://…"          remote (e.g. Supabase Storage URLs from the editor)
 *  - "file://…" or "/…"   absolute local file path
 *  - anything else        treated as "figures/<path>" inside APK assets
 *
 * Returns null silently if the image can't be loaded. The renderer falls
 * back to a placeholder so a broken asset doesn't break the slide.
 */
@Composable
fun loadFigureBitmap(path: String?): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    val ctx = LocalContext.current
    var bmp by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bmp = withContext(Dispatchers.IO) {
            try {
                when {
                    path.startsWith("http://") || path.startsWith("https://") -> {
                        URL(path).openStream().use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                    }
                    path.startsWith("file://") -> {
                        val real = path.removePrefix("file://")
                        java.io.FileInputStream(real).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                    }
                    path.startsWith("/") -> {
                        java.io.FileInputStream(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                    }
                    else -> {
                        ctx.assets.open("figures/$path").use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                    }
                }
            } catch (_: Throwable) { null }
        }
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
