package com.learnlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.learnlab.design.DarkTokens
import com.learnlab.design.LightTokens
import com.learnlab.design.LocalTokens
import com.learnlab.shell.Shell
import com.learnlab.store.AppState
import kotlinx.coroutines.MainScope
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insets = WindowInsetsControllerCompat(window, window.decorView)
        insets.hide(WindowInsetsCompat.Type.systemBars())
        insets.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent { LearnLabApp() }
    }
}

@Composable
private fun LearnLabApp() {
    val ctx = LocalContext.current
    val scope = remember { MainScope() }
    val state = remember { AppState(ctx.applicationContext, scope) }
    val themeName by state.theme
    val isDark = themeName == "dark"

    val tokens = if (isDark) DarkTokens else LightTokens
    val mat3 = if (isDark) {
        darkColorScheme(
            primary = tokens.accent500,
            onPrimary = Color.White,
            background = tokens.bg,
            surface = tokens.surface,
            onSurface = tokens.ink50,
            onBackground = tokens.ink50,
        )
    } else {
        lightColorScheme(
            primary = tokens.accent600,
            onPrimary = Color.White,
            background = tokens.bg,
            surface = tokens.surface,
            onSurface = tokens.ink50,
            onBackground = tokens.ink50,
        )
    }

    // Keep status bar icon color in sync when theme toggles at runtime.
    val activity = ctx as? android.app.Activity
    LaunchedEffect(isDark) {
        if (activity != null) {
            val ctrl = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            ctrl.isAppearanceLightStatusBars = !isDark
            ctrl.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalTokens provides tokens) {
        MaterialTheme(colorScheme = mat3) {
            Surface(
                modifier = Modifier.fillMaxSize().background(tokens.bg),
                color = tokens.bg,
            ) { Shell(state) }
        }
    }
}
