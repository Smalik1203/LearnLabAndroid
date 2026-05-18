package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.learnlab.design.LL
import com.learnlab.store.AppState

/**
 * Top-level layout: TopBar on top (fixed height), then a row with
 * the ExperimentRail (fixed width) and the ExperimentStage (flex).
 * Mirrors src/shell/Shell.tsx.
 */
@Composable
fun Shell(state: AppState) {
    val t = LL.tokens
    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        TopBar(state)
        Row(modifier = Modifier.fillMaxSize()) {
            ExperimentRail(state)
            ExperimentStage(state, modifier = Modifier.weight(1f))
        }
    }
}
