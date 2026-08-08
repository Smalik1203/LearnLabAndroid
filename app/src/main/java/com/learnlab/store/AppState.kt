package com.learnlab.store

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("learnlab")
private val KEY_THEME = stringPreferencesKey("theme")
private val KEY_RECENTS = stringPreferencesKey("recents")
private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
private val KEY_READER_DARK = stringPreferencesKey("reader_dark")

/** One opened-experiment record for the History feature. */
data class RecentEntry(val id: String, val openedAt: Long)

private const val MAX_RECENTS = 100

class AppState(private val appContext: Context, private val scope: CoroutineScope) {
    val theme: MutableState<String> = mutableStateOf("light")
    val isDark: Boolean get() = theme.value == "dark"

    /** Opened experiments, newest first. Persisted locally via DataStore. */
    val recents: MutableState<List<RecentEntry>> = mutableStateOf(emptyList())

    /** Display name captured by the (mock) login/sign-up form, if any. */
    val displayName: MutableState<String?> = mutableStateOf(null)

    /** Textbook reader theme: dark by default, toggled to the "paper" light theme. */
    val readerDark: MutableState<Boolean> = mutableStateOf(true)

    init {
        scope.launch(Dispatchers.IO) {
            val prefs = appContext.dataStore.data.first()
            val saved = prefs[KEY_THEME]
            if (saved == "light" || saved == "dark") theme.value = saved
            prefs[KEY_READER_DARK]?.let { readerDark.value = it != "false" }
            recents.value = decodeRecents(prefs[KEY_RECENTS])
            displayName.value = prefs[KEY_DISPLAY_NAME]?.takeIf { it.isNotBlank() }
        }
    }

    fun toggleTheme() {
        theme.value = if (theme.value == "dark") "light" else "dark"
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_THEME] = theme.value }
        }
    }

    fun toggleReaderTheme() {
        readerDark.value = !readerDark.value
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_READER_DARK] = readerDark.value.toString() }
        }
    }

    /** Record that [experimentId] was just opened — moves it to the front, deduped. */
    fun recordRecent(experimentId: String) {
        val updated = (listOf(RecentEntry(experimentId, System.currentTimeMillis())) +
            recents.value.filter { it.id != experimentId }).take(MAX_RECENTS)
        recents.value = updated
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_RECENTS] = encodeRecents(updated) }
        }
    }

    fun setDisplayName(name: String) {
        val clean = name.trim()
        displayName.value = clean.takeIf { it.isNotBlank() }
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_DISPLAY_NAME] = clean }
        }
    }
}

// Stored as "id|epochMillis;id|epochMillis;…" — experiment ids are kebab-case so
// they never contain the '|' or ';' delimiters.
private fun encodeRecents(entries: List<RecentEntry>): String =
    entries.joinToString(";") { "${it.id}|${it.openedAt}" }

private fun decodeRecents(raw: String?): List<RecentEntry> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.split(";").mapNotNull { token ->
        val parts = token.split("|")
        val id = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val ts = parts.getOrNull(1)?.toLongOrNull() ?: return@mapNotNull null
        RecentEntry(id, ts)
    }
}
