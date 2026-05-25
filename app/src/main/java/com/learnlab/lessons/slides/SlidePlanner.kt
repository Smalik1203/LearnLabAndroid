package com.learnlab.lessons.slides

import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout

/**
 * Turns a Chapter's flat block list into a deck of slides. Designed for an IFP:
 * one slide = one teachable moment. No vertical scroll inside a slide.
 *
 * Rules, in priority order:
 *  1. The first slide is always a Cover. (Synthetic — owns no blocks.)
 *  2. SanskritShloka → its own slide.
 *  3. SectionHeader → SectionTitle slide.
 *  4. KnowScientist / SuccessStory / DoYouKnow / MoreToKnow → their own interlude slide.
 *  5. SideBySideCompare → Compare slide.
 *  6. FigureCollage → Collage slide.
 *  7. Figure with adjacent paragraph(s) before or after → TextWithFigure (up to one short para).
 *     Otherwise → FigureFocus.
 *  8. Activity → ActivityLaunch slide. Any tables it owns become extra TableSlides immediately after.
 *  9. Exercise → ExerciseSlide.
 * 10. KeywordCloud → KeywordCloud slide.
 * 11. Summary → SummaryGrid slide.
 * 12. LearningFurther → LearningFurther slide.
 * 13. Quotation → Closing slide if last, Callout otherwise.
 * 14. Callout → Callout slide (its own).
 * 15. KeyTerm → KeyTermCard slide (own slide; this gives the term emphasis).
 * 16. StoryFrame → Story slide.
 * 17. SpeechBubble runs (2+ consecutive) → Conversation slide. Lone bubble → Story slide.
 * 18. Paragraph → TextOnly slide. Long paragraphs are split into multiple
 *     TextOnly slides (planner doesn't actually split mid-sentence; it just
 *     emits one slide per paragraph today and lets the layout enforce a max
 *     character count by clipping. Authors split long ones at JSON level.)
 * 19. Final synthetic Closing slide if last block wasn't already a quote/summary.
 */
object SlidePlanner {

    fun plan(chapter: Chapter): List<Slide> {
        val out = mutableListOf<Slide>()
        var sectionNum: String? = null
        var sectionTitle: String? = null

        // 1. Cover
        out += Slide(
            id = "cover",
            layout = SlideLayout.Cover,
            blocks = emptyList(),
        )

        val blocks = chapter.blocks
        var i = 0
        var seq = 0
        fun mintId(tag: String): String = "${tag}-${seq++}"

        while (i < blocks.size) {
            val b = blocks[i]
            when (b) {
                is ChapterBlock.SanskritShloka -> {
                    out += Slide(mintId("shloka"), SlideLayout.Shloka, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SectionHeader -> {
                    sectionNum = b.number
                    sectionTitle = b.title
                    val next = blocks.getOrNull(i + 1)
                    if (next is ChapterBlock.Paragraph && next.body.length < 600) {
                        out += Slide(
                            id = mintId("section-intro"),
                            layout = SlideLayout.SectionIntro,
                            blocks = listOf(b, next),
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        i += 2
                    } else {
                        out += Slide(mintId("section"), SlideLayout.SectionTitle, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                    }
                }
                is ChapterBlock.KnowScientist -> {
                    out += Slide(mintId("scientist"), SlideLayout.ScientistInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SuccessStory -> {
                    out += Slide(mintId("success"), SlideLayout.SuccessStoryInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.DoYouKnow, is ChapterBlock.MoreToKnow -> {
                    out += Slide(mintId("did-you-know"), SlideLayout.DidYouKnowInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SideBySideCompare -> {
                    // Optionally pull in a preceding paragraph or one trailing paragraph as caption
                    out += Slide(mintId("compare"), SlideLayout.Compare, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.FigureCollage -> {
                    out += Slide(mintId("collage"), SlideLayout.Collage, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Figure -> {
                    // composeDraw figures own their full slide — they render
                    // their own internal layout (map + detail pane etc) so we
                    // never want to squeeze them into half a TextWithFigure.
                    val isComposeDraw = b.assetType == "composeDraw"
                    if (isComposeDraw) {
                        out += Slide(mintId("figure"), SlideLayout.FigureFocus, listOf(b),
                            sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                        continue
                    }
                    // Bitmap/SVG figures: look back, then forward, for a short
                    // paragraph to pair with as TextWithFigure.
                    val prevSlide = out.lastOrNull()
                    val prevWasShortText = prevSlide != null
                        && prevSlide.layout == SlideLayout.TextOnly
                        && prevSlide.blocks.size == 1
                        && (prevSlide.blocks[0] as? ChapterBlock.Paragraph)?.body?.length?.let { it < 320 } == true
                    if (prevWasShortText) {
                        val merged = Slide(
                            id = mintId("text-fig"),
                            layout = SlideLayout.TextWithFigure,
                            blocks = listOf(prevSlide!!.blocks[0], b),
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        out[out.lastIndex] = merged
                    } else {
                        val next = blocks.getOrNull(i + 1)
                        if (next is ChapterBlock.Paragraph && next.body.length < 320) {
                            out += Slide(mintId("text-fig"), SlideLayout.TextWithFigure, listOf(b, next), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                            i += 2
                            continue
                        }
                        out += Slide(mintId("figure"), SlideLayout.FigureFocus, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    }
                    i++
                }
                is ChapterBlock.Activity -> {
                    val singleSmallTable = b.tables.singleOrNull()?.takeIf { it.exampleRows.size <= 2 }
                    if (singleSmallTable != null) {
                        val tableBlock = ChapterBlock.TableBlock(
                            caption = singleSmallTable.caption,
                            headers = singleSmallTable.headers,
                            rows = singleSmallTable.exampleRows,
                        )
                        out += Slide(
                            id = mintId("activity-with-table"),
                            layout = SlideLayout.ActivityWithTable,
                            blocks = listOf(b, tableBlock),
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                    } else {
                        out += Slide(mintId("activity"), SlideLayout.ActivityLaunch, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        b.tables.forEach { spec ->
                            out += Slide(
                                id = mintId("activity-table"),
                                layout = SlideLayout.TableSlide,
                                blocks = listOf(ChapterBlock.TableBlock(
                                    caption = spec.caption,
                                    headers = spec.headers,
                                    rows = spec.exampleRows,
                                )),
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                        }
                    }
                    i++
                }
                is ChapterBlock.Exercise -> {
                    out += Slide(mintId("exercise"), SlideLayout.ExerciseSlide, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.KeywordCloud -> {
                    out += Slide(mintId("kw"), SlideLayout.KeywordCloud, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Summary -> {
                    out += Slide(mintId("summary"), SlideLayout.SummaryGrid, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.LearningFurther -> {
                    out += Slide(mintId("learn-further"), SlideLayout.LearningFurther, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Quotation -> {
                    val isLast = i == blocks.lastIndex
                    out += Slide(
                        id = mintId("quote"),
                        layout = if (isLast) SlideLayout.Closing else SlideLayout.Callout,
                        blocks = listOf(b),
                        sectionNumber = sectionNum,
                        sectionTitle = sectionTitle,
                    )
                    i++
                }
                is ChapterBlock.Callout -> {
                    out += Slide(mintId("callout"), SlideLayout.Callout, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.KeyTerm -> {
                    out += Slide(mintId("term"), SlideLayout.KeyTermCard, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.StoryFrame -> {
                    out += Slide(mintId("story"), SlideLayout.Story, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SpeechBubble -> {
                    // Collect a run of consecutive bubbles
                    val run = mutableListOf<ChapterBlock>(b)
                    var j = i + 1
                    while (j < blocks.size && blocks[j] is ChapterBlock.SpeechBubble) {
                        run += blocks[j]
                        j++
                    }
                    if (run.size >= 2) {
                        out += Slide(mintId("conv"), SlideLayout.Conversation, run, sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    } else {
                        out += Slide(mintId("story-line"), SlideLayout.Story, run, sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    }
                    i = j
                }
                is ChapterBlock.Paragraph -> {
                    // Pull in 1..N KeyTerms that follow this paragraph into one slide.
                    val terms = mutableListOf<ChapterBlock>()
                    var k = i + 1
                    while (k < blocks.size && blocks[k] is ChapterBlock.KeyTerm && terms.size < 3) {
                        terms += blocks[k]
                        k++
                    }
                    if (terms.isNotEmpty()) {
                        out += Slide(
                            id = mintId("text-defs"),
                            layout = SlideLayout.TextWithDefinitions,
                            blocks = listOf(b) + terms,
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        i = k
                    } else {
                        // Pull in a single adjacent SpeechBubble as context.
                        val nextBubble = blocks.getOrNull(i + 1) as? ChapterBlock.SpeechBubble
                        val nextNextIsBubble = blocks.getOrNull(i + 2) is ChapterBlock.SpeechBubble
                        if (nextBubble != null && !nextNextIsBubble) {
                            out += Slide(
                                id = mintId("story-context"),
                                layout = SlideLayout.StoryWithContext,
                                blocks = listOf(b, nextBubble),
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                            i += 2
                        } else {
                            out += Slide(mintId("text"), SlideLayout.TextOnly, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                            i++
                        }
                    }
                }
                is ChapterBlock.TableBlock -> {
                    out += Slide(mintId("table"), SlideLayout.TableSlide, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.WorkedExample -> {
                    // Treat as a Callout-style slide for now (own design later)
                    out += Slide(mintId("worked"), SlideLayout.Callout, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.ImageWithCallout -> {
                    out += Slide(mintId("image"), SlideLayout.FigureFocus, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
            }
        }

        return out
    }
}
