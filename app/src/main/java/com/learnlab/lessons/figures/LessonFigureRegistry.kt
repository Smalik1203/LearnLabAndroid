package com.learnlab.lessons.figures

import androidx.compose.runtime.Composable

/**
 * id → Canvas-drawn figure composable. Figures are referenced from
 * [com.learnlab.content.LessonBlock.Figure] by their id.
 */
object LessonFigureRegistry {
    private val figures: Map<String, @Composable () -> Unit> = mapOf(
        "fig-3-7-iodine-bench" to { IodineBenchFigure() },
    )

    operator fun get(id: String): (@Composable () -> Unit)? = figures[id]
}
