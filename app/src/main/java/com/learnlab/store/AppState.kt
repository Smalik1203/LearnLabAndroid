package com.learnlab.store

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.learnlab.content.AllExperiments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Mirrors src/store/appStore.ts: which experiment is selected, light/dark theme,
 * next/prev navigation. Single shared instance; persisted via DataStore.
 */

private val Context.dataStore by preferencesDataStore("learnlab")
private val KEY_THEME = stringPreferencesKey("theme")
private val KEY_LAST_EXP = stringPreferencesKey("last_experiment")
@Suppress("unused")
private val KEY_HYDRATED = booleanPreferencesKey("hydrated")

class AppState(private val appContext: Context, private val scope: CoroutineScope) {
    val currentExperimentId: MutableState<String?> = mutableStateOf(null)
    /** "light" or "dark". Defaults to dark to match the React app. */
    val theme: MutableState<String> = mutableStateOf("dark")
    val isDark: Boolean get() = theme.value == "dark"

    /** When false, the rail collapses to a thin chevron strip. */
    val railOpen: MutableState<Boolean> = mutableStateOf(true)
    fun toggleRail() { railOpen.value = !railOpen.value }

    init {
        scope.launch(Dispatchers.IO) {
            val prefs = appContext.dataStore.data.first()
            val savedTheme = prefs[KEY_THEME]
            if (savedTheme == "light" || savedTheme == "dark") {
                theme.value = savedTheme
            }
            // We intentionally do NOT restore the last experiment selection
            // on launch — same as the React app, which lands on Welcome.
        }
    }

    fun select(id: String?) { currentExperimentId.value = id }

    fun next() {
        val ids = AllExperiments.map { it.id }
        val i = ids.indexOf(currentExperimentId.value)
        if (i in 0 until ids.size - 1) currentExperimentId.value = ids[i + 1]
        else if (currentExperimentId.value == null && ids.isNotEmpty()) currentExperimentId.value = ids[0]
    }

    fun prev() {
        val ids = AllExperiments.map { it.id }
        val i = ids.indexOf(currentExperimentId.value)
        if (i > 0) currentExperimentId.value = ids[i - 1]
    }

    fun hasPrev(): Boolean {
        val ids = AllExperiments.map { it.id }
        val i = ids.indexOf(currentExperimentId.value)
        return i > 0
    }

    fun hasNext(): Boolean {
        val ids = AllExperiments.map { it.id }
        val i = ids.indexOf(currentExperimentId.value)
        return i >= 0 && i < ids.size - 1
    }

    fun toggleTheme() {
        theme.value = if (theme.value == "dark") "light" else "dark"
        persistTheme()
    }

    private fun persistTheme() {
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_THEME] = theme.value }
        }
    }

    @Suppress("unused")
    fun rememberLast() {
        val id = currentExperimentId.value ?: return
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_LAST_EXP] = id }
        }
    }
}
