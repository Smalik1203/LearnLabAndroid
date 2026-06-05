package com.learnlab.data.sync

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonElement

/**
 * One-shot reachability check for the Supabase backend.
 *
 * Called from MainActivity on first launch. Reads zero or more rows from
 * slide_overrides and logs the count. If the SDK can't talk to the project
 * — wrong URL, wrong key, no network, RLS misconfigured — it logs the
 * exception.
 *
 * Delete (or hide behind BuildConfig.DEBUG) once the editor's real sync
 * code is in. For now this is the only thing proving the wiring works.
 */
object SupabaseSmokeTest {

    private const val TAG = "SupabaseSmokeTest"

    suspend fun ping() {
        try {
            val rows = SupabaseAccess.client
                .from("slide_overrides")
                .select()
                .decodeList<JsonElement>()
            Log.i(TAG, "Reached Supabase. slide_overrides has ${rows.size} rows.")
        } catch (t: Throwable) {
            Log.e(TAG, "Supabase ping failed: ${t.message}", t)
        }
    }
}
