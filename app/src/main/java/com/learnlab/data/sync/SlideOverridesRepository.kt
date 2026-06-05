package com.learnlab.data.sync

import android.util.Log
import com.learnlab.content.chapter.SlideOverride
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Reads slide layout overrides from Supabase. Write path lands later (task #9).
 *
 * Today's contract: give me all overrides for a chapter, keyed by slide ID.
 * Anonymous rows (user_id is null) are the shared chapter layout. Per-user
 * rows will override the anonymous one once auth lands.
 *
 * Failure mode is deliberate: on any error (no network, RLS denied, parse
 * failure) we return an empty map. The renderer then falls back to the
 * planner's auto-layout. The app never blocks on the network.
 */
object SlideOverridesRepository {

    private const val TAG = "SlideOverrides"

    /** Permissive JSON parser — tolerate extra fields the editor might add later. */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Serializable
    private data class Row(
        val slide_id: String,
        val overrides: kotlinx.serialization.json.JsonElement,
    )

    /**
     * Upserts a single slide override. Anonymous (user_id = null) for now;
     * once auth lands we'll pass the user ID and the row will land under
     * that user's slot via the unique index.
     *
     * Failure is logged and swallowed — the caller's in-memory state still
     * holds the edit, so the next save attempt will pick it up.
     */
    suspend fun save(chapterId: String, slideId: String, override: SlideOverride) {
        try {
            val overridesJson: JsonObject = json.encodeToJsonElement(SlideOverride.serializer(), override) as JsonObject
            val row = buildJsonObject {
                put("chapter_id", chapterId)
                put("slide_id", slideId)
                put("overrides", overridesJson)
            }
            SupabaseAccess.client
                .from("slide_overrides")
                .upsert(row) {
                    onConflict = "chapter_id, slide_id, user_id"
                    ignoreDuplicates = false
                }
            Log.i(TAG, "Saved override $chapterId/$slideId")
        } catch (t: Throwable) {
            Log.e(TAG, "Save failed for $chapterId/$slideId: ${t.message}", t)
        }
    }

    suspend fun fetchForChapter(chapterId: String): Map<String, SlideOverride> {
        return try {
            val rows = SupabaseAccess.client
                .from("slide_overrides")
                .select(Columns.list("slide_id", "overrides")) {
                    filter { eq("chapter_id", chapterId) }
                }
                .decodeList<Row>()

            rows.associate { row ->
                val parsed = json.decodeFromJsonElement(SlideOverride.serializer(), row.overrides)
                row.slide_id to parsed
            }.also {
                Log.i(TAG, "Loaded ${it.size} overrides for chapter $chapterId")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Couldn't load overrides for $chapterId, falling back to auto-layout: ${t.message}")
            emptyMap()
        }
    }
}
