package com.learnlab.lessons.slides

import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout
import com.learnlab.content.chapter.SlideOverride
import java.security.MessageDigest

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

    /**
     * Plans the deck and, for any slide whose ID matches a key in
     * [overrides], swaps its layout to [SlideLayout.FreeForm] carrying the
     * authored elements. Slides without an override fall through to the
     * normal auto-layout.
     */
    fun plan(chapter: Chapter, overrides: Map<String, SlideOverride> = emptyMap()): List<Slide> {
        val autoSlides = planAuto(chapter)
        if (overrides.isEmpty()) return autoSlides
        val applied = mutableListOf<String>()
        val result = autoSlides.map { slide ->
            val ov = overrides[slide.id]
            if (ov == null) {
                slide
            } else {
                applied += slide.id
                slide.copy(layout = SlideLayout.FreeForm, override = ov)
            }
        }
        android.util.Log.i(
            "SlidePlanner",
            "Overrides requested: ${overrides.keys}. " +
                "Planner slide IDs: ${autoSlides.take(10).map { it.id }}... " +
                "Applied: $applied",
        )
        return result
    }

    private fun planAuto(chapter: Chapter): List<Slide> {
        val out = mutableListOf<Slide>()
        var sectionNum: String? = null
        var sectionTitle: String? = null

        val keyTermsBySection = mutableMapOf<String?, MutableList<ChapterBlock.KeyTerm>>()
        val emittedKeyTermsForSection = mutableSetOf<String?>()
        var currentSec: String? = null
        chapter.blocks.forEach { b ->
            if (b is ChapterBlock.SectionHeader) {
                currentSec = b.number
            }
            if (b is ChapterBlock.KeyTerm) {
                keyTermsBySection.getOrPut(currentSec) { mutableListOf() }.add(b)
            }
        }

        // 1. Cover
        out += Slide(
            id = "cover",
            layout = SlideLayout.Cover,
            blocks = emptyList(),
        )

        val blocks = chapter.blocks
        var i = 0
        // Counter only used to disambiguate when two slides have identical
        // fingerprints (e.g. two empty SectionTitle slides in a row).
        // Otherwise the ID is purely content-derived and stable across edits
        // elsewhere in the chapter.
        val seenFingerprints = mutableMapOf<String, Int>()
        fun mintId(tag: String, vararg fingerprintBlocks: ChapterBlock): String {
            val fp = stableFingerprint(tag, fingerprintBlocks.toList())
            val n = seenFingerprints.getOrDefault(fp, 0)
            seenFingerprints[fp] = n + 1
            return if (n == 0) fp else "${fp}-d${n}"
        }

        while (i < blocks.size) {
            val b = blocks[i]
            when (b) {
                is ChapterBlock.SanskritShloka -> {
                    out += Slide(mintId("shloka", b), SlideLayout.Shloka, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SectionHeader -> {
                    sectionNum = b.number
                    sectionTitle = b.title
                    i++
                }
                is ChapterBlock.KnowScientist -> {
                    out += Slide(mintId("scientist", b), SlideLayout.ScientistInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SuccessStory -> {
                    out += Slide(mintId("success", b), SlideLayout.SuccessStoryInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.DoYouKnow, is ChapterBlock.MoreToKnow -> {
                    out += Slide(mintId("did-you-know", b), SlideLayout.DidYouKnowInterlude, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SideBySideCompare -> {
                    out += Slide(mintId("compare", b), SlideLayout.Compare, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.FigureCollage -> {
                    out += Slide(mintId("collage", b), SlideLayout.Collage, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Figure -> {
                    val isComposeDraw = b.assetType == "composeDraw"
                    if (isComposeDraw) {
                        out += Slide(mintId("figure", b), SlideLayout.FigureFocus, listOf(b),
                            sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                    } else {
                        // Look ahead for paragraph(s) to merge
                        val paragraphGroup = mutableListOf<ChapterBlock.Paragraph>()
                        var totalLength = 0
                        var j = i + 1
                        while (j < blocks.size) {
                            val nextB = blocks[j]
                            if (nextB is ChapterBlock.Paragraph) {
                                val hasNextBubble = j + 1 < blocks.size && blocks[j + 1] is ChapterBlock.SpeechBubble
                                val hasNextNextBubble = j + 2 < blocks.size && blocks[j + 2] is ChapterBlock.SpeechBubble
                                if (hasNextBubble && !hasNextNextBubble) break

                                if (paragraphGroup.isNotEmpty() && totalLength + nextB.body.length > 900) break

                                paragraphGroup.add(nextB)
                                totalLength += nextB.body.length
                                j++
                            } else {
                                break
                            }
                        }
                        if (paragraphGroup.isNotEmpty()) {
                            val mergedBlocks = listOf(b) + paragraphGroup
                            out += Slide(
                                id = mintId("text-fig", *mergedBlocks.toTypedArray()),
                                layout = SlideLayout.TextWithFigure,
                                blocks = mergedBlocks,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                            i = j
                        } else {
                            out += Slide(mintId("figure", b), SlideLayout.FigureFocus, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                            i++
                        }
                    }
                }
                is ChapterBlock.Activity -> {
                    out += Slide(mintId("activity", b), SlideLayout.ActivityLaunch, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Exercise -> {
                    out += Slide(mintId("exercise", b), SlideLayout.ExerciseSlide, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.KeywordCloud -> {
                    out += Slide(mintId("kw", b), SlideLayout.KeywordCloud, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Summary -> {
                    out += Slide(mintId("summary", b), SlideLayout.SummaryGrid, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.LearningFurther -> {
                    out += Slide(mintId("learn-further", b), SlideLayout.LearningFurther, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.Quotation -> {
                    val isLast = i == blocks.lastIndex
                    if (isLast) {
                        out += Slide(mintId("closing", b), SlideLayout.Closing, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                    } else {
                        // Look ahead for paragraph(s) to merge
                        val paragraphGroup = mutableListOf<ChapterBlock.Paragraph>()
                        var totalLength = 0
                        var j = i + 1
                        while (j < blocks.size) {
                            val nextB = blocks[j]
                            if (nextB is ChapterBlock.Paragraph) {
                                val hasNextBubble = j + 1 < blocks.size && blocks[j + 1] is ChapterBlock.SpeechBubble
                                val hasNextNextBubble = j + 2 < blocks.size && blocks[j + 2] is ChapterBlock.SpeechBubble
                                if (hasNextBubble && !hasNextNextBubble) break

                                if (paragraphGroup.isNotEmpty() && totalLength + nextB.body.length > 900) break

                                paragraphGroup.add(nextB)
                                totalLength += nextB.body.length
                                j++
                            } else {
                                break
                            }
                        }
                        if (paragraphGroup.isNotEmpty()) {
                            val mergedBlocks = listOf(b) + paragraphGroup
                            out += Slide(
                                id = mintId("text-callout", *mergedBlocks.toTypedArray()),
                                layout = SlideLayout.Callout,
                                blocks = mergedBlocks,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                            i = j
                        } else {
                            out += Slide(mintId("quote", b), SlideLayout.Callout, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                            i++
                        }
                    }
                }
                is ChapterBlock.Callout -> {
                    // Look ahead for paragraph(s) to merge
                    val paragraphGroup = mutableListOf<ChapterBlock.Paragraph>()
                    var totalLength = 0
                    var j = i + 1
                    while (j < blocks.size) {
                        val nextB = blocks[j]
                        if (nextB is ChapterBlock.Paragraph) {
                            val hasNextBubble = j + 1 < blocks.size && blocks[j + 1] is ChapterBlock.SpeechBubble
                            val hasNextNextBubble = j + 2 < blocks.size && blocks[j + 2] is ChapterBlock.SpeechBubble
                            if (hasNextBubble && !hasNextNextBubble) break

                            if (paragraphGroup.isNotEmpty() && totalLength + nextB.body.length > 900) break

                            paragraphGroup.add(nextB)
                            totalLength += nextB.body.length
                            j++
                        } else {
                            break
                        }
                    }
                    if (paragraphGroup.isNotEmpty()) {
                        val mergedBlocks = listOf(b) + paragraphGroup
                        out += Slide(
                            id = mintId("text-callout", *mergedBlocks.toTypedArray()),
                            layout = SlideLayout.Callout,
                            blocks = mergedBlocks,
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        i = j
                    } else {
                        out += Slide(mintId("callout", b), SlideLayout.Callout, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                    }
                }
                is ChapterBlock.KeyTerm -> {
                    if (sectionNum !in emittedKeyTermsForSection) {
                        emittedKeyTermsForSection.add(sectionNum)
                        val terms = keyTermsBySection[sectionNum] ?: emptyList()
                        if (terms.isNotEmpty()) {
                            out += Slide(
                                id = mintId("terms-sec", *terms.toTypedArray()),
                                layout = SlideLayout.KeyTermCard,
                                blocks = terms,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle
                            )
                        }
                    }
                    i++
                }
                is ChapterBlock.StoryFrame -> {
                    out += Slide(mintId("story", b), SlideLayout.Story, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.SpeechBubble -> {
                    val run = mutableListOf<ChapterBlock>(b)
                    var j = i + 1
                    while (j < blocks.size && blocks[j] is ChapterBlock.SpeechBubble) {
                        run += blocks[j]
                        j++
                    }
                    if (run.size >= 2) {
                        out += Slide(mintId("conv", *run.toTypedArray()), SlideLayout.Conversation, run, sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    } else {
                        out += Slide(mintId("story-line", b), SlideLayout.Story, run, sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    }
                    i = j
                }
                is ChapterBlock.Paragraph -> {
                    val nextBubble = blocks.getOrNull(i + 1) as? ChapterBlock.SpeechBubble
                    val nextNextIsBubble = blocks.getOrNull(i + 2) is ChapterBlock.SpeechBubble
                    if (nextBubble != null && !nextNextIsBubble) {
                        out += Slide(
                            id = mintId("story-context", b, nextBubble),
                            layout = SlideLayout.StoryWithContext,
                            blocks = listOf(b, nextBubble),
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        i += 2
                    } else {
                        // Scan all consecutive paragraphs starting at i
                        val paragraphGroup = mutableListOf<ChapterBlock.Paragraph>()
                        var totalLength = 0
                        var j = i
                        while (j < blocks.size) {
                            val nextB = blocks[j]
                            if (nextB is ChapterBlock.Paragraph) {
                                val hasNextBubble = j + 1 < blocks.size && blocks[j + 1] is ChapterBlock.SpeechBubble
                                val hasNextNextBubble = j + 2 < blocks.size && blocks[j + 2] is ChapterBlock.SpeechBubble
                                if (hasNextBubble && !hasNextNextBubble) break

                                if (paragraphGroup.isNotEmpty() && totalLength + nextB.body.length > 900) break

                                paragraphGroup.add(nextB)
                                totalLength += nextB.body.length
                                j++
                            } else {
                                break
                            }
                        }

                        // Check if the block right after the paragraph group is a Figure, Callout, or WorkedExample
                        val nextBlock = blocks.getOrNull(j)
                        if (nextBlock is ChapterBlock.Figure && nextBlock.assetType != "composeDraw" && totalLength < 900) {
                            val mergedBlocks = paragraphGroup + nextBlock
                            out += Slide(
                                id = mintId("text-fig", *mergedBlocks.toTypedArray()),
                                layout = SlideLayout.TextWithFigure,
                                blocks = mergedBlocks,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                            i = j + 1
                        } else if ((nextBlock is ChapterBlock.Callout || nextBlock is ChapterBlock.WorkedExample) && totalLength < 900) {
                            val mergedBlocks = paragraphGroup + nextBlock
                            out += Slide(
                                id = mintId("text-callout", *mergedBlocks.toTypedArray()),
                                layout = SlideLayout.Callout,
                                blocks = mergedBlocks,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle,
                            )
                            i = j + 1
                        } else {
                            // Emit them as a single consolidated TextOnly slide!
                            out += Slide(
                                id = mintId("text", *paragraphGroup.toTypedArray()),
                                layout = SlideLayout.TextOnly,
                                blocks = paragraphGroup,
                                sectionNumber = sectionNum,
                                sectionTitle = sectionTitle
                            )
                            i = j
                        }
                    }
                }
                is ChapterBlock.TableBlock -> {
                    out += Slide(mintId("table", b), SlideLayout.TableSlide, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
                is ChapterBlock.WorkedExample -> {
                    // Look ahead for paragraph(s) to merge
                    val paragraphGroup = mutableListOf<ChapterBlock.Paragraph>()
                    var totalLength = 0
                    var j = i + 1
                    while (j < blocks.size) {
                        val nextB = blocks[j]
                        if (nextB is ChapterBlock.Paragraph) {
                            val hasNextBubble = j + 1 < blocks.size && blocks[j + 1] is ChapterBlock.SpeechBubble
                            val hasNextNextBubble = j + 2 < blocks.size && blocks[j + 2] is ChapterBlock.SpeechBubble
                            if (hasNextBubble && !hasNextNextBubble) break

                            if (paragraphGroup.isNotEmpty() && totalLength + nextB.body.length > 900) break

                            paragraphGroup.add(nextB)
                            totalLength += nextB.body.length
                            j++
                        } else {
                            break
                        }
                    }
                    if (paragraphGroup.isNotEmpty()) {
                        val mergedBlocks = listOf(b) + paragraphGroup
                        out += Slide(
                            id = mintId("text-callout", *mergedBlocks.toTypedArray()),
                            layout = SlideLayout.Callout,
                            blocks = mergedBlocks,
                            sectionNumber = sectionNum,
                            sectionTitle = sectionTitle,
                        )
                        i = j
                    } else {
                        out += Slide(mintId("worked", b), SlideLayout.Callout, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                        i++
                    }
                }
                is ChapterBlock.ImageWithCallout -> {
                    out += Slide(mintId("image", b), SlideLayout.FigureFocus, listOf(b), sectionNumber = sectionNum, sectionTitle = sectionTitle)
                    i++
                }
            }
        }

        return out
    }

    /**
     * Returns a stable ID derived from a tag + a fingerprint of the blocks
     * the slide represents. The same blocks always produce the same ID,
     * regardless of where they appear in the chapter — so overrides saved
     * for "the Leela dadi quote slide" survive future content insertions
     * earlier in the chapter.
     *
     * Format: `<tag>-<hash>` where hash is the first 12 hex chars of SHA-1
     * over `tag + "|" + fingerprintOf(block1) + "|" + ...`. 12 hex chars =
     * 48 bits of entropy = ~2.8e14 possible IDs; collision risk inside one
     * chapter is effectively zero.
     */
    private fun stableFingerprint(tag: String, blocks: List<ChapterBlock>): String {
        val payload = buildString {
            append(tag)
            blocks.forEach { append('|'); append(fingerprintOf(it)) }
        }
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(payload.toByteArray(Charsets.UTF_8))
        val hex = bytes.joinToString("") { "%02x".format(it) }.take(12)
        return "$tag-$hex"
    }

    /**
     * One-line content fingerprint per block type. Pick the field most
     * likely to stay stable when the author tweaks prose:
     *  - ID fields (ncertReference, key term name) when present;
     *  - first ~80 chars of body text otherwise.
     *
     * Trade: if you rewrite a paragraph wholesale, its slide ID changes
     * and its overrides re-attach to the new content. That's acceptable —
     * a wholesale rewrite is a different slide.
     */
    private fun fingerprintOf(block: ChapterBlock): String = when (block) {
        is ChapterBlock.SanskritShloka -> "shloka:" + block.devanagari.take(80)
        is ChapterBlock.SectionHeader -> "section:${block.number}:${block.title}"
        is ChapterBlock.Paragraph -> "para:" + block.body.take(80)
        is ChapterBlock.SpeechBubble -> "bubble:${block.speakerId}:" + block.body.take(60)
        is ChapterBlock.KeyTerm -> "term:${block.term}"
        is ChapterBlock.Activity -> "activity:${block.ncertReference ?: block.title}"
        is ChapterBlock.Exercise -> "ex:${block.id}"
        is ChapterBlock.Figure -> "fig:${block.id}"
        is ChapterBlock.FigureCollage -> "collage:${block.id}"
        is ChapterBlock.ImageWithCallout -> "img:${block.id}"
        is ChapterBlock.SideBySideCompare -> "compare:${block.id}"
        is ChapterBlock.Callout -> "callout:${block.title ?: block.body.take(60)}"
        is ChapterBlock.KnowScientist -> "scientist:${block.id}"
        is ChapterBlock.SuccessStory -> "success:${block.id}"
        is ChapterBlock.DoYouKnow -> "dyk:${block.id}"
        is ChapterBlock.MoreToKnow -> "mtk:${block.id}"
        is ChapterBlock.Summary -> "summary:${block.title}"
        is ChapterBlock.KeywordCloud -> "kw:${block.title}"
        is ChapterBlock.LearningFurther -> "lf:${block.title}"
        is ChapterBlock.Quotation -> "quote:" + block.body.take(60)
        is ChapterBlock.StoryFrame -> "story:" + block.scene.take(60)
        is ChapterBlock.TableBlock -> "table:" + (block.caption ?: block.headers.joinToString(","))
        is ChapterBlock.WorkedExample -> "worked:${block.title}"
    }
}
