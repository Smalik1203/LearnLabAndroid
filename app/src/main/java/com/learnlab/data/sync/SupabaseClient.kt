package com.learnlab.data.sync

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Single Supabase client for the whole app.
 *
 * The slide editor saves layout overrides through this client; later, the
 * progress + events tables will too (per CLAUDE.md §7). We avoid DI here
 * because we don't have Hilt yet and the client is genuinely process-wide
 * state — one TCP pool per app makes sense.
 *
 * The publishable key is safe to ship in the APK: it only grants whatever
 * Row Level Security allows, which is currently "anonymous read+write to
 * slide_overrides only." When auth lands we tighten RLS and the same key
 * starts giving callers nothing without a JWT.
 */
object SupabaseAccess {

    // Project ccjgpqkonnonkbjevkdk (LearnLab, ap-south-1 / Mumbai).
    private const val URL = "https://ccjgpqkonnonkbjevkdk.supabase.co"
    private const val PUBLISHABLE_KEY = "sb_publishable_nLymHqoXKycOtO_J9ldcHw_G2zxJ_JO"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = URL,
            supabaseKey = PUBLISHABLE_KEY,
        ) {
            install(Postgrest)
            install(Storage)
        }
    }
}
