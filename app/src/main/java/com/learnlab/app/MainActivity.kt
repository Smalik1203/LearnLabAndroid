package com.learnlab.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.learnlab.design.DarkTokens
import com.learnlab.design.LearnLabTheme
import com.learnlab.design.LightTokens
import com.learnlab.design.LocalFontScale
import com.learnlab.design.LocalTokens
import com.learnlab.store.AppState
import com.learnlab.ui.navigation.LearnLabNavGraph
import kotlinx.coroutines.MainScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { LearnLabApp() }
    }
}

@Composable
private fun LearnLabApp() {
    val ctx      = LocalContext.current
    val scope    = remember { MainScope() }
    val state    = remember { AppState(ctx.applicationContext, scope) }
    val isDark   by state.theme
    val fontScale by state.fontSizeScale
    val keepOn   by state.keepScreenOn
    val fullscreen by state.fullscreen
    val tokens   = if (isDark == "dark") DarkTokens else LightTokens
    val activity = ctx as? android.app.Activity

    // Sync status bar appearance with theme
    LaunchedEffect(isDark) {
        if (activity != null) {
            val ctrl = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            ctrl.isAppearanceLightStatusBars    = isDark != "dark"
            ctrl.isAppearanceLightNavigationBars = isDark != "dark"
        }
    }

    // Fullscreen toggle
    LaunchedEffect(fullscreen) {
        if (activity != null) {
            val ctrl = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            if (fullscreen) {
                ctrl.hide(WindowInsetsCompat.Type.systemBars())
                ctrl.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                ctrl.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Keep screen on
    LaunchedEffect(keepOn) {
        activity?.window?.let { w ->
            if (keepOn) w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else        w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val navController = rememberNavController()

    LearnLabTheme(isDark = isDark == "dark", fontScale = fontScale) {
        CompositionLocalProvider(
            LocalTokens   provides tokens,
            LocalFontScale provides fontScale,
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                LearnLabNavGraph(navController = navController, state = state)
            }
        }
    }
}
