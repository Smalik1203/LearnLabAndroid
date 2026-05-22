package com.learnlab.store

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("learnlab")
private val KEY_THEME        = stringPreferencesKey("theme")
private val KEY_FONT_SCALE   = floatPreferencesKey("font_scale")
private val KEY_KEEP_SCREEN  = booleanPreferencesKey("keep_screen_on")
private val KEY_FULLSCREEN   = booleanPreferencesKey("fullscreen")

class AppState(private val appContext: Context, private val scope: CoroutineScope) {
    val theme: MutableState<String>        = mutableStateOf("dark")
    val fontSizeScale: MutableState<Float> = mutableFloatStateOf(1f)
    val keepScreenOn: MutableState<Boolean> = mutableStateOf(false)
    val fullscreen: MutableState<Boolean>   = mutableStateOf(true)

    val isDark: Boolean get() = theme.value == "dark"

    init {
        scope.launch(Dispatchers.IO) {
            val prefs = appContext.dataStore.data.first()
            prefs[KEY_THEME]?.let       { if (it == "light" || it == "dark") theme.value = it }
            prefs[KEY_FONT_SCALE]?.let  { fontSizeScale.value = it }
            prefs[KEY_KEEP_SCREEN]?.let { keepScreenOn.value = it }
            prefs[KEY_FULLSCREEN]?.let  { fullscreen.value = it }
        }
    }

    fun toggleTheme() {
        theme.value = if (theme.value == "dark") "light" else "dark"
        persist { it[KEY_THEME] = theme.value }
    }

    fun setFontScale(scale: Float) {
        fontSizeScale.value = scale
        persist { it[KEY_FONT_SCALE] = scale }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        keepScreenOn.value = enabled
        persist { it[KEY_KEEP_SCREEN] = enabled }
    }

    fun setFullscreen(enabled: Boolean) {
        fullscreen.value = enabled
        persist { it[KEY_FULLSCREEN] = enabled }
    }

    private fun persist(block: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        scope.launch(Dispatchers.IO) { appContext.dataStore.edit { block(it) } }
    }
}
