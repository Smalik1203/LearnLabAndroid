package com.learnlab.content

/** Decorative icon for a [ChapterSlide.Concept] — mapped to a Material icon by the renderer. */
enum class SlideIcon { QUESTION, SEARCH, LOOP, BALANCE, NOTE, SCIENCE, KITCHEN, SPARK }

/**
 * A single slide in a chapter's pre-experiment deck. The deck is generated from
 * the chapter's textbook content (not the raw PDF) and rendered natively.
 */
sealed interface ChapterSlide {
    data class Title(
        val kicker: String,
        val title: String,
        val subtitle: String,
        val points: List<String> = emptyList(),
    ) : ChapterSlide

    /** [body] may contain **bold** spans. */
    data class Concept(
        val kicker: String,
        val title: String,
        val body: String,
        val icon: SlideIcon,
    ) : ChapterSlide

    /** Each step is "Lead — detail"; the lead word is emphasised by the renderer. */
    data class Steps(
        val kicker: String,
        val title: String,
        val steps: List<String>,
    ) : ChapterSlide

    data class Split(
        val kicker: String,
        val title: String,
        val leftTitle: String,
        val leftBody: String,
        val rightTitle: String,
        val rightBody: String,
    ) : ChapterSlide

    data class Chips(
        val kicker: String,
        val title: String,
        val body: String,
        val chips: List<String>,
    ) : ChapterSlide

    data class Closing(
        val title: String,
        val subtitle: String,
    ) : ChapterSlide
}
