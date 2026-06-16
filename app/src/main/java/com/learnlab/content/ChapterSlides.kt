package com.learnlab.content

/**
 * Pre-experiment slide decks, generated from each chapter's textbook content.
 * One entry per authored chapter; [slidesFor] returns an empty list when a
 * chapter has no deck yet (the slideshow is then skipped).
 */
private val chapterSlides: Map<String, List<ChapterSlide>> = mapOf(
    // Grade 8 · Chapter 1 — "Exploring the Investigative World of Science"
    "ch05" to listOf(
        ChapterSlide.Title(
            kicker = "GRADE 8 · CHAPTER 1",
            title = "Exploring the Investigative World of Science",
            subtitle = "This year you don't just learn facts — you learn how to find them.",
            points = listOf(
                "Why is one side of a puri thinner than the other?",
                "More grains of sand on Earth's beaches, or more stars in our galaxy?",
                "Why has nature created such a vast variety of life?",
            ),
        ),
        ChapterSlide.Steps(
            kicker = "THE JOURNEY SO FAR",
            title = "From wonder to investigation",
            steps = listOf(
                "Grade 6 — science begins with wonder: \"Why?\" and \"How?\"",
                "Grade 7 — science evolves: every answer opens new questions",
                "Grade 8 — you become an investigator",
            ),
        ),
        ChapterSlide.Concept(
            kicker = "WHAT IS INVESTIGATION?",
            title = "Find facts — don't just learn them",
            body = "Investigation means asking **focused questions**, designing **experiments** to answer them, and using your **observations** to sharpen your understanding.",
            icon = SlideIcon.SEARCH,
        ),
        ChapterSlide.Steps(
            kicker = "THE INVESTIGATOR'S LOOP",
            title = "How science actually works",
            steps = listOf(
                "Observe — notice something with your senses",
                "Question — ask a focused \"what happens if…?\"",
                "Experiment — test it, changing one thing",
                "Explain — make sense of what you saw",
            ),
        ),
        ChapterSlide.Split(
            kicker = "STAY GROUNDED, AIM HIGH",
            title = "The root and the kite",
            leftTitle = "Root",
            leftBody = "Careful observation keeps you grounded in real evidence.",
            rightTitle = "Kite",
            rightBody = "Curiosity lets your ideas soar toward new horizons.",
        ),
        ChapterSlide.Chips(
            kicker = "THIS YEAR'S JOURNEY",
            title = "Where curiosity will take us",
            body = "From the tiniest microbe to the whole planet:",
            chips = listOf(
                "Microbes", "Health", "Electricity", "Forces", "Pressure & Cyclones",
                "Particles of Matter", "Elements & Mixtures", "Solutions",
                "Light & Mirrors", "Moon & Calendars", "Ecosystems", "Planet Earth",
            ),
        ),
        ChapterSlide.Concept(
            kicker = "INVESTIGATE ANYTHING",
            title = "Your kitchen is a laboratory",
            body = "Why does a **puri** puff up in hot oil — and why is one side thinner? You don't need a fancy lab, just **curiosity** and careful **observation**.",
            icon = SlideIcon.KITCHEN,
        ),
        ChapterSlide.Split(
            kicker = "DESIGN THE TEST",
            title = "What you change vs what you watch",
            leftTitle = "You control",
            leftBody = "Dough thickness · flour type (atta / maida) · oil temperature · how you drop it",
            rightTitle = "You observe",
            rightBody = "Does it puff? (yes / no) · how long it takes (seconds) · is one side thin?",
        ),
        ChapterSlide.Concept(
            kicker = "THE GOLDEN RULE",
            title = "Change one thing at a time",
            body = "To see what really matters, keep everything else the same and change **only one** thing. Testing the oil temperature? Use the **same** dough circles, dropped the **same** way.",
            icon = SlideIcon.BALANCE,
        ),
        ChapterSlide.Concept(
            kicker = "LIKE A REAL SCIENTIST",
            title = "Notice, note, and ask again",
            body = "Record everything you sense — splatter, smell, smoke. Each answer sparks new questions: fresh dough or stored? What if you prick a hole? **This is how all science is done.**",
            icon = SlideIcon.NOTE,
        ),
        ChapterSlide.Closing(
            title = "Happy investigating!",
            subtitle = "From a puffing puri to the shrinking Moon after purnima — let careful observation lead the way. Ready to experiment?",
        ),
    ),
)

fun slidesFor(chapterId: String): List<ChapterSlide> = chapterSlides[chapterId].orEmpty()

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
