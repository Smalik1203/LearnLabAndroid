package com.learnlab.content

/**
 * Theory + figures + character dialogue that wraps an experiment.
 * Mirrors the NCERT textbook flow: prose, embedded figures, "More to know!"
 * callouts, character speech bubbles (Medu, Mishti, grandma, Dr Poshita),
 * sticky-note questions, etc.
 *
 * An [Experiment] now has an optional [LessonContent]. The stage shows three
 * tabs — Read · Do · Reflect — when content is present.
 */
data class LessonContent(
    /** Shown in the Read tab — sets up the experiment. */
    val read: List<LessonBlock>,
    /** Shown in the Reflect tab — the explanation that follows in the book. */
    val reflect: List<LessonBlock> = emptyList(),
)

sealed class LessonBlock {
    /** Section heading like "3.3 How to Test Different Components of Food?". */
    data class Heading(val text: String, val level: Int = 1, val number: String? = null) : LessonBlock()

    /** Body paragraph. Inline **bold** markers (`**word**`) get rendered emphasised. */
    data class Paragraph(val text: String) : LessonBlock()

    /** Glossary-style "this word means…" call-out. */
    data class KeyTerm(val term: String, val definition: String) : LessonBlock()

    /** Yellow scroll quote box (Sanskrit / wise saying). */
    data class Quote(val text: String, val original: String? = null, val attribution: String) : LessonBlock()

    /**
     * Character speech bubble. [who] is the character name, [side] decides
     * which side of the screen the bubble appears on (so two characters can
     * "converse" in alternating bubbles).
     */
    data class CharacterSay(
        val who: String,
        val text: String,
        val side: Side = Side.LEFT,
        val avatar: Avatar = Avatar.STUDENT_GIRL,
    ) : LessonBlock() {
        enum class Side { LEFT, RIGHT }
        enum class Avatar { STUDENT_GIRL, STUDENT_BOY, GRANDMA, TEACHER, SCIENTIST }
    }

    /** Yellow "More to know!" or red "Precautions" coloured callout. */
    data class Callout(
        val title: String,
        val body: String,
        val tone: Tone = Tone.INFO,
    ) : LessonBlock() {
        enum class Tone { INFO, WARNING, SUCCESS, FACT }
    }

    /** Sticky-note style "?" question. The student doesn't answer here — it's rhetorical. */
    data class Question(val prompt: String) : LessonBlock()

    /** A figure rendered by a registered [figureId]. The caption sits under it. */
    data class Figure(val figureId: String, val caption: String) : LessonBlock()

    /**
     * "Case 1 / Case 2" style narrative box. Used heavily in the deficiency
     * sections of Ch 3.
     */
    data class CaseStudy(val number: Int, val title: String, val body: String) : LessonBlock()

    /** Vertical spacing nudge between dense blocks. */
    data class Spacer(val sizeDp: Int = 12) : LessonBlock()
}
