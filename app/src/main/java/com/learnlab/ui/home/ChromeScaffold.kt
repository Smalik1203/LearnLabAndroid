package com.learnlab.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.learnlab.shell.PageChrome
import com.learnlab.store.AppState
import com.learnlab.ui.auth.AuthDialog
import com.learnlab.ui.auth.AuthMode

private enum class ChromeDialog { NONE, SEARCH, HISTORY, LOGIN, SIGN_UP }

/**
 * Wraps [PageChrome] and hosts the top-bar's search / history / login / sign-up
 * popups. Kept in the ui layer so shell never has to depend on screen code.
 */
@Composable
fun ChromeScaffold(
    state: AppState,
    onLogo: () -> Unit,
    onOpenExperiment: (String) -> Unit,
    onViewAllHistory: () -> Unit,
    content: @Composable () -> Unit,
) {
    var dialog by remember { mutableStateOf(ChromeDialog.NONE) }

    PageChrome(
        state = state,
        onLogo = onLogo,
        onSearch = { dialog = ChromeDialog.SEARCH },
        onHistory = { dialog = ChromeDialog.HISTORY },
        onLogin = { dialog = ChromeDialog.LOGIN },
        onSignUp = { dialog = ChromeDialog.SIGN_UP },
        content = content,
    )

    when (dialog) {
        ChromeDialog.NONE -> Unit
        ChromeDialog.SEARCH -> SearchDialog(
            onDismiss = { dialog = ChromeDialog.NONE },
            onOpenExperiment = { dialog = ChromeDialog.NONE; onOpenExperiment(it) },
        )
        ChromeDialog.HISTORY -> HistoryDialog(
            recents = state.recents.value,
            onDismiss = { dialog = ChromeDialog.NONE },
            onOpenExperiment = { dialog = ChromeDialog.NONE; onOpenExperiment(it) },
            onViewMore = { dialog = ChromeDialog.NONE; onViewAllHistory() },
        )
        ChromeDialog.LOGIN -> AuthDialog(
            mode = AuthMode.LOGIN,
            onDismiss = { dialog = ChromeDialog.NONE },
            onSubmit = { name -> state.setDisplayName(name); dialog = ChromeDialog.NONE },
        )
        ChromeDialog.SIGN_UP -> AuthDialog(
            mode = AuthMode.SIGN_UP,
            onDismiss = { dialog = ChromeDialog.NONE },
            onSubmit = { name -> state.setDisplayName(name); dialog = ChromeDialog.NONE },
        )
    }
}
