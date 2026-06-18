package com.learnlab.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Decorative icon for a [ChapterSlide.Concept] — mapped to a Material icon by the renderer. */
@Serializable
enum class SlideIcon {
    QUESTION, SEARCH, LOOP, BALANCE, NOTE, SCIENCE, KITCHEN, SPARK,
    // Topic icons for the grade-8 chapters.
    MICROBE, VIRUS, HEART, BOLT, WIND, PARTICLE, BUBBLE, LIGHT, CLOCK,
    LEAF, GLOBE, WAVE, HEAT, SUN, FIRE, VACCINE, EYE, MEASURE, STORM, FORCE,
}

/** A slider-driven interactive 2D model rendered on a Canvas (theme-aware). */
@Serializable
enum class InteractiveKind {
    KINETIC_MATTER,  // temperature slider → particles speed up, solid → liquid → gas
    MOON_ORBIT,      // lunar-month slider → orbit + the phase Earth sees
    FORCE_BLOCK,     // applied-force slider + surface → block slides or stays
    DENSITY_FLOAT,   // density slider → object floats, suspends, or sinks
}

/** Schematic diagram a [ChapterSlide.Figure] draws on a Canvas (theme-aware). */
@Serializable
enum class FigureKind {
    PARTICLE_STATES,    // solid / liquid / gas arrangement
    PARTICLE_GROUPING,  // element / compound / mixture
    FORCE_ARROWS,       // forces acting on a block
    LIGHT_REFLECTION,   // a ray reflecting off a plane mirror
    MOON_PHASES,        // the eight phases of the Moon
    GOLDILOCKS,         // Earth in the Sun's habitable zone
    ENERGY_PYRAMID,     // trophic levels in an ecosystem
}

/**
 * A single slide in a chapter's pre-experiment deck. The deck is generated from
 * the chapter's textbook content (not the raw PDF) and rendered natively.
 *
 * Authored as JSON in `assets/textbook/<chapterId>.json`; the `"type"` field is
 * the polymorphic discriminator (see [TextbookContent]).
 */
@Serializable
sealed interface ChapterSlide {
    @Serializable
    @SerialName("title")
    data class Title(
        val kicker: String,
        val title: String,
        val subtitle: String,
        val points: List<String> = emptyList(),
    ) : ChapterSlide

    /** [body] may contain **bold** spans. */
    @Serializable
    @SerialName("concept")
    data class Concept(
        val kicker: String,
        val title: String,
        val body: String,
        val icon: SlideIcon,
    ) : ChapterSlide

    /** Each step is "Lead — detail"; the lead word is emphasised by the renderer. */
    @Serializable
    @SerialName("steps")
    data class Steps(
        val kicker: String,
        val title: String,
        val steps: List<String>,
    ) : ChapterSlide

    /**
     * A teacher-framed activity: [purpose] says what we explore and why, [steps]
     * is what to do, [observe] is what to notice and the concept it teaches.
     * Each step may be "Lead — detail". [body] fields may contain **bold** spans.
     */
    @Serializable
    @SerialName("activity")
    data class Activity(
        val kicker: String,
        val title: String,
        val purpose: String,
        val steps: List<String>,
        val observe: String,
    ) : ChapterSlide

    /** A heritage / wise-saying card. [original] and [transliteration] are optional. */
    @Serializable
    @SerialName("quote")
    data class Quote(
        val kicker: String,
        val translation: String,
        val attribution: String,
        val original: String = "",
        val transliteration: String = "",
    ) : ChapterSlide

    @Serializable
    @SerialName("split")
    data class Split(
        val kicker: String,
        val title: String,
        val leftTitle: String,
        val leftBody: String,
        val rightTitle: String,
        val rightBody: String,
    ) : ChapterSlide

    @Serializable
    @SerialName("chips")
    data class Chips(
        val kicker: String,
        val title: String,
        val body: String,
        val chips: List<String>,
    ) : ChapterSlide

    /** A comparison / property table. [rows] cells align to [headers] columns. */
    @Serializable
    @SerialName("table")
    data class Table(
        val kicker: String,
        val title: String,
        val headers: List<String>,
        val rows: List<List<String>>,
    ) : ChapterSlide

    /** A schematic diagram drawn natively. [labels] feed the kinds that use them. */
    @Serializable
    @SerialName("figure")
    data class Figure(
        val kicker: String,
        val title: String,
        val caption: String,
        val kind: FigureKind,
        val labels: List<String> = emptyList(),
    ) : ChapterSlide

    /** A slider-driven interactive 2D model drawn natively. */
    @Serializable
    @SerialName("interactive")
    data class Interactive(
        val kicker: String,
        val title: String,
        val caption: String,
        val kind: InteractiveKind,
    ) : ChapterSlide

    /** A section divider; also the anchor for the reader's "In this chapter" TOC. */
    @Serializable
    @SerialName("section")
    data class SectionHeader(
        val number: String,
        val title: String,
        val intro: String = "",
    ) : ChapterSlide

    /** A short narrative "scene" that motivates a topic (story hook). */
    @Serializable
    @SerialName("scene")
    data class Scene(
        val kicker: String,
        val body: String,
    ) : ChapterSlide

    @Serializable
    @SerialName("closing")
    data class Closing(
        val title: String,
        val subtitle: String,
    ) : ChapterSlide

    /** Inline experiment within the textbook flow; launches the experiment's lesson. */
    @Serializable
    @SerialName("experiment")
    data class Experiment(val experimentId: String) : ChapterSlide
}
