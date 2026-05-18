package com.learnlab.store

/**
 * Mirrors `ExperimentControls` from the React runtime: every experiment
 * receives a small callback bag so the shell can drive progress + completion.
 */
data class ExperimentControls(
    val onProgress: (Float) -> Unit,
    val onComplete: (Float?) -> Unit,
)
