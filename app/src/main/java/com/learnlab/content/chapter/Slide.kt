package com.learnlab.content.chapter

/**
 * A Slide is one full-screen unit shown on the IFP. The deck for a chapter is
 * produced by [com.learnlab.lessons.slides.SlidePlanner] from the chapter's
 * blocks, or — if the chapter JSON includes a manual `slides` array — by the
 * author directly. Either way the runtime sees a flat list of [Slide].
 */
data class Slide(
    val id: String,
    val layout: SlideLayout,
    /** The blocks that this slide owns. Layouts know how to read them. */
    val blocks: List<com.learnlab.content.chapter.ChapterBlock>,
    /** Short context for the teacher — printed in speaker-notes mode (future). */
    val speakerNotes: String? = null,
    /** Optional section anchor for outline / jump targets. */
    val sectionNumber: String? = null,
    val sectionTitle: String? = null,
    /**
     * Author-edited free-form layout. When non-null the renderer ignores
     * [layout]/[blocks] and draws the elements absolutely positioned.
     */
    val override: SlideOverride? = null,
)

enum class SlideLayout {
    /** Chapter cover. */
    Cover,
    /** Opening Sanskrit shloka card. */
    Shloka,
    /** Big section number + title with a one-line subtitle. */
    SectionTitle,
    /** One block of body text, centred, no figure. */
    TextOnly,
    /** A figure dominating one half, text the other. Planner chooses side. */
    TextWithFigure,
    /** A single figure dominating the slide, caption + hotspots. */
    FigureFocus,
    /** Side-by-side compare (camel vs camel, fish vs goat, leaf vs leaf). */
    Compare,
    /** A single key term, large and centred for emphasis. */
    KeyTermCard,
    /** Section header + the first paragraph of that section, one slide. */
    SectionIntro,
    /** Paragraph + a single adjacent speech bubble, one slide. */
    StoryWithContext,
    /** One character + one speech bubble, large. */
    Story,
    /** Two-or-more characters arranged spatially with their bubbles. */
    Conversation,
    /** Janaki Ammal / Salim Ali — portrait + bio interlude. */
    ScientistInterlude,
    /** Save Silent Valley success-story interlude. */
    SuccessStoryInterlude,
    /** Sacred groves / Do-you-know interlude. */
    DidYouKnowInterlude,
    /** "Big idea" / closing-quote callout slide. */
    Callout,
    /** The activity launch slide — title + intro + big "Open activity" CTA. */
    ActivityLaunch,
    /** A standalone table (Activity 2.1's Table 2.1 / 2.2). */
    TableSlide,
    /** A figure collage with multiple circled regions (Fig 2.7 — animal diversity). */
    Collage,
    /** End-of-chapter keyword tag-cloud (clickable terms). */
    KeywordCloud,
    /** Summary "Key Points" laid out as a grid of pill cards. */
    SummaryGrid,
    /** A single exercise — Venn / flowchart / image-compare / MCQ / free text. */
    ExerciseSlide,
    /** Learning-further projects grid. */
    LearningFurther,
    /** Closing quote / final slide. */
    Closing,
    /**
     * Free-form, author-edited layout. The slide's [Slide.override] holds
     * the elements; auto-layout fields are ignored.
     */
    FreeForm,
}
