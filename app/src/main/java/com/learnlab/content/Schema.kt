package com.learnlab.content

/**
 * Mirrors src/content/schema.ts exactly.
 */
data class Experiment(
    val id: String,
    val chapterId: String,
    val title: String,
    val blurb: String,
    val outcome: String,
    val source: String,
    val steps: List<String>,
    val ready: Boolean = true,
)

data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val experiments: List<Experiment>,
    val grade: Int = 6,
    val comingSoon: Boolean = false,
)
