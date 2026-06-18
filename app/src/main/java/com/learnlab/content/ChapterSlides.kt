package com.learnlab.content

/**
 * The continuous grade "textbook" is assembled from per-chapter slide decks that
 * are authored as JSON in `assets/textbook/` and loaded by [TextbookContent].
 * (Content is data, not code — CLAUDE.md §5.)
 */

fun slidesFor(chapterId: String): List<ChapterSlide> = TextbookContent.slidesFor(chapterId)

/** Grade-8 (etc.) chapters that have a textbook, in order — for the chapter list. */
fun textbookChapters(grade: Int): List<Chapter> =
    Chapters.filter { it.grade == grade && !it.comingSoon && slidesFor(it.id).isNotEmpty() }
        .sortedBy { it.number }

/**
 * One chapter's deck. If the JSON already places experiment slides inline, use it
 * as authored; otherwise append the chapter's experiments at the end (fallback for
 * chapters not yet converted to inline placement).
 */
fun chapterDeck(chapterId: String): List<ChapterSlide> {
    val slides = slidesFor(chapterId)
    if (slides.isEmpty()) return emptyList()
    if (slides.any { it is ChapterSlide.Experiment }) return slides
    val ch = Chapters.firstOrNull { it.id == chapterId } ?: return slides
    return slides + ch.experiments.map { ChapterSlide.Experiment(it.id) }
}

/** A "In this chapter" entry: a section anchor and the slide index it starts at. */
data class TocEntry(val number: String, val title: String, val slideIndex: Int)

/** TOC for a chapter, derived from its SectionHeader slides. */
fun tocFor(chapterId: String): List<TocEntry> =
    chapterDeck(chapterId).mapIndexedNotNull { i, s ->
        if (s is ChapterSlide.SectionHeader) TocEntry(s.number, s.title, i) else null
    }

/** One entry in the continuous grade "textbook": a slide plus the chapter it belongs to. */
data class TextbookItem(val chapterTitle: String, val slide: ChapterSlide)

/**
 * The continuous textbook for a grade: every chapter that has authored slides (in
 * chapter order), each chapter's reading slides followed by an inline experiment
 * slide per experiment. Empty for grades with no authored content yet.
 */
fun textbookDeck(grade: Int): List<TextbookItem> =
    Chapters.filter { it.grade == grade && !it.comingSoon }
        .sortedBy { it.number }
        .flatMap { ch ->
            val slides = slidesFor(ch.id)
            if (slides.isEmpty()) emptyList()
            else (slides + ch.experiments.map { ChapterSlide.Experiment(it.id) })
                .map { TextbookItem(ch.title, it) }
        }
