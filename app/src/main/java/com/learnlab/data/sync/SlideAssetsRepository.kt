package com.learnlab.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Uploads images picked by the in-app editor to Supabase Storage and
 * returns the public URL the renderer can load with [loadFigureBitmap].
 *
 * Bucket: "slide-assets" (created in migration). Public read.
 * Path scheme: "<chapterId>/<slideId>/<random>.<ext>" so assets from one
 * slide don't collide and a chapter's images can be pruned by prefix.
 */
object SlideAssetsRepository {

    private const val TAG = "SlideAssets"
    private const val BUCKET = "slide-assets"

    /**
     * Reads the image at [uri] and uploads it. Returns the public URL on
     * success, null on failure. Safe to call from a coroutine on any
     * dispatcher; the IO is moved to Dispatchers.IO internally.
     */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        chapterId: String,
        slideId: String,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            val ext = guessExtension(context, uri)
            val key = "$chapterId/$slideId/${UUID.randomUUID()}.$ext"
            val bucket = SupabaseAccess.client.storage.from(BUCKET)
            bucket.upload(key, bytes) { upsert = false }
            val url = bucket.publicUrl(key)
            Log.i(TAG, "Uploaded $key → $url")
            url
        } catch (t: Throwable) {
            Log.e(TAG, "Image upload failed: ${t.message}", t)
            null
        }
    }

    private fun guessExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri) ?: ""
        return when {
            mime.contains("png") -> "png"
            mime.contains("jpeg") || mime.contains("jpg") -> "jpg"
            mime.contains("webp") -> "webp"
            mime.contains("gif") -> "gif"
            else -> "png"
        }
    }
}
