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

class AppState(private val appContext: Context, private val scope: CoroutineScope) {
    val theme: MutableState<String> = mutableStateOf("dark")
    val isDark: Boolean get() = theme.value == "dark"

    init {
        scope.launch(Dispatchers.IO) {
            val prefs = appContext.dataStore.data.first()
            val saved = prefs[KEY_THEME]
            if (saved == "light" || saved == "dark") theme.value = saved
        }
    }

    fun toggleTheme() {
        theme.value = if (theme.value == "dark") "light" else "dark"
        scope.launch(Dispatchers.IO) {
            appContext.dataStore.edit { it[KEY_THEME] = theme.value }
        }
    }
}
