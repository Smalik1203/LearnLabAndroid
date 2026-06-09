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
    val blocks: List<ChapterBlock> = emptyList(),
)

sealed interface ChapterBlock {
    data class Heading(val text: String, val level: Int) : ChapterBlock
    data class Paragraph(val text: String) : ChapterBlock
    data class ActivityRef(val experimentId: String) : ChapterBlock
    
    enum class SidebarType {
        CURIOUS_STUDENT,
        THINK_LIKE_A_SCIENTIST,
        BE_A_SCIENTIST,
        STEP_FURTHER
    }
    
    data class Sidebar(
        val type: SidebarType,
        val title: String,
        val content: String
    ) : ChapterBlock
    
    data class Figure(
        val label: String,
        val caption: String,
        val illustrationId: String? = null
    ) : ChapterBlock
}
