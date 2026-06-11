package com.learnlab.lessons.slides

import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Slide

/**
 * Flattens a slide's blocks into a single read-aloud string, in reading order,
 * for [com.learnlab.design.SpeakControls]. Pulls the prose a teacher/student
 * would actually want narrated — headings, paragraphs, definitions, quotes,
 * speech, callouts, questions, captions, summaries — and skips the parts that
 * don't read well aloud (raw table grids, keyword tag-clouds, asset ids).
 *
 * NCERT note: Sanskrit shlokas read their English translation, not the
 * Devanagari/transliteration, which TTS can't pronounce.
 */
fun Slide.spokenText(): String =
    blocks.mapNotNull { it.spokenText() }.filter { it.isNotBlank() }.joinToString(". ")

private fun ChapterBlock.spokenText(): String? = when (this) {
    is ChapterBlock.SectionHeader -> listOfNotNull(number, title).joinToString(" ")
    is ChapterBlock.Paragraph -> body
    is ChapterBlock.KeyTerm -> "$term. $definition"
    is ChapterBlock.Quotation -> body
    is ChapterBlock.Callout -> listOfNotNull(title, body).joinToString(". ")
    is ChapterBlock.SanskritShloka -> translation
    is ChapterBlock.StoryFrame -> scene
    is ChapterBlock.SpeechBubble -> body
    is ChapterBlock.Figure -> caption
    is ChapterBlock.FigureCollage -> caption
    is ChapterBlock.ImageWithCallout -> caption
    is ChapterBlock.SideBySideCompare -> listOfNotNull(caption, leftBlurb, rightBlurb).joinToString(". ")
    is ChapterBlock.Activity -> listOfNotNull(title, intro).joinToString(". ")
    is ChapterBlock.WorkedExample -> (listOf(title) + steps).joinToString(". ")
    is ChapterBlock.KnowScientist -> "$name. $biography"
    is ChapterBlock.SuccessStory -> listOfNotNull(title, body).joinToString(". ")
    is ChapterBlock.DoYouKnow -> "$title. $body"
    is ChapterBlock.MoreToKnow -> "$title. $body"
    is ChapterBlock.Summary -> (listOf(title) + points).joinToString(". ")
    is ChapterBlock.Exercise -> prompt
    is ChapterBlock.LearningFurther -> (listOf(title) + projects.map { it.body }).joinToString(". ")
    // Skipped — no prose that reads well aloud.
    is ChapterBlock.TableBlock,
    is ChapterBlock.KeywordCloud,
    -> null
}
