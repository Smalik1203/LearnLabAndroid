package com.learnlab.lessons.slides

import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.OverrideElement
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout
import com.learnlab.content.chapter.SlideOverride

/**
 * Turns an auto-layout slide into a free-form override the editor can
 * actually edit. Called when the author taps pencil on a slide that has
 * no override yet.
 *
 * The synthesized layout doesn't try to match the auto-layout pixel-for-
 * pixel — it places elements in reasonable default positions that the
 * author can then drag/resize/restyle. Think of it as "first draft you
 * can edit", not "perfect reproduction".
 */
object OverrideSynthesizer {

    fun synthesize(slide: Slide, cast: Map<String, Character>): SlideOverride? {
        val elements = when (slide.layout) {
            SlideLayout.TextOnly -> synthesizeTextOnly(slide.blocks)
            SlideLayout.StoryWithContext -> synthesizeStoryWithContext(slide.blocks, cast)
            SlideLayout.KeyTermCard -> synthesizeKeyTerm(slide.blocks)
            SlideLayout.Callout -> synthesizeCallout(slide.blocks)
            SlideLayout.SectionIntro -> synthesizeSectionIntro(slide.blocks)
            SlideLayout.Story -> synthesizeStory(slide.blocks, cast)
            else -> synthesizeFallback(slide)
        }
        return if (elements.isEmpty()) null else SlideOverride(elements = elements)
    }

    /* ─────────── per-layout templates ─────────── */

    private fun synthesizeTextOnly(blocks: List<ChapterBlock>): List<OverrideElement> {
        val paras = blocks.filterIsInstance<ChapterBlock.Paragraph>()
        if (paras.isEmpty()) return emptyList()
        return listOf(
            OverrideElement.Text(
                id = "para",
                x = 0.06f, y = 0.10f, width = 0.88f, height = 0.80f,
                body = paras.joinToString("\n\n") { stripTokens(it.body) },
                sizeSp = 28f,
                weight = "normal",
            ),
        )
    }

    private fun synthesizeStoryWithContext(
        blocks: List<ChapterBlock>,
        cast: Map<String, Character>,
    ): List<OverrideElement> {
        val para = blocks.firstOrNull { it is ChapterBlock.Paragraph } as? ChapterBlock.Paragraph
        val bubble = blocks.firstOrNull { it is ChapterBlock.SpeechBubble } as? ChapterBlock.SpeechBubble
        val character = bubble?.speakerId?.let { cast[it] }
        val out = mutableListOf<OverrideElement>()
        if (para != null) {
            out += OverrideElement.Text(
                id = "para",
                x = 0.05f, y = 0.08f, width = 0.55f, height = 0.84f,
                body = stripTokens(para.body),
                sizeSp = 24f,
            )
        }
        if (character?.avatar != null) {
            out += OverrideElement.Image(
                id = "portrait",
                x = 0.62f, y = 0.04f, width = 0.34f, height = 0.78f,
                asset = character.avatar,
                contentScale = "fit",
            )
        }
        if (bubble != null) {
            out += OverrideElement.Bubble(
                id = "quote",
                x = 0.62f, y = 0.80f, width = 0.34f, height = 0.16f,
                body = bubble.body,
                speakerLabel = character?.displayName,
                sizeSp = 14f,
                hue = "sky",
            )
        }
        return out
    }

    private fun synthesizeKeyTerm(blocks: List<ChapterBlock>): List<OverrideElement> {
        val term = blocks.firstOrNull { it is ChapterBlock.KeyTerm } as? ChapterBlock.KeyTerm
            ?: return emptyList()
        return listOf(
            OverrideElement.Text(
                id = "term",
                x = 0.1f, y = 0.20f, width = 0.8f, height = 0.20f,
                body = term.term,
                sizeSp = 56f, weight = "extrabold", align = "center",
            ),
            OverrideElement.Text(
                id = "definition",
                x = 0.1f, y = 0.48f, width = 0.8f, height = 0.40f,
                body = term.definition,
                sizeSp = 26f, align = "center",
            ),
        )
    }

    private fun synthesizeCallout(blocks: List<ChapterBlock>): List<OverrideElement> {
        val callout = blocks.firstOrNull { it is ChapterBlock.Callout } as? ChapterBlock.Callout
            ?: return emptyList()
        val out = mutableListOf<OverrideElement>()
        if (callout.title != null) {
            out += OverrideElement.Text(
                id = "title",
                x = 0.08f, y = 0.15f, width = 0.84f, height = 0.15f,
                body = callout.title,
                sizeSp = 36f, weight = "bold",
            )
        }
        out += OverrideElement.Text(
            id = "body",
            x = 0.08f, y = if (callout.title != null) 0.35f else 0.20f,
            width = 0.84f, height = if (callout.title != null) 0.55f else 0.70f,
            body = callout.body,
            sizeSp = 26f,
        )
        return out
    }

    private fun synthesizeSectionIntro(blocks: List<ChapterBlock>): List<OverrideElement> {
        val header = blocks.firstOrNull { it is ChapterBlock.SectionHeader } as? ChapterBlock.SectionHeader
        val para = blocks.firstOrNull { it is ChapterBlock.Paragraph } as? ChapterBlock.Paragraph
        val out = mutableListOf<OverrideElement>()
        if (header?.number != null) {
            out += OverrideElement.Text(
                id = "number",
                x = 0.06f, y = 0.18f, width = 0.4f, height = 0.18f,
                body = header.number,
                sizeSp = 84f, weight = "extrabold",
            )
        }
        if (header != null) {
            out += OverrideElement.Text(
                id = "title",
                x = 0.06f, y = 0.40f, width = 0.4f, height = 0.30f,
                body = header.title,
                sizeSp = 36f, weight = "bold",
            )
        }
        if (para != null) {
            out += OverrideElement.Text(
                id = "intro",
                x = 0.50f, y = 0.18f, width = 0.44f, height = 0.66f,
                body = stripTokens(para.body),
                sizeSp = 24f,
            )
        }
        return out
    }

    private fun synthesizeStory(
        blocks: List<ChapterBlock>,
        cast: Map<String, Character>,
    ): List<OverrideElement> {
        val bubble = blocks.firstOrNull { it is ChapterBlock.SpeechBubble } as? ChapterBlock.SpeechBubble
            ?: return emptyList()
        val character = cast[bubble.speakerId]
        val out = mutableListOf<OverrideElement>()
        if (character?.avatar != null) {
            out += OverrideElement.Image(
                id = "portrait",
                x = 0.10f, y = 0.10f, width = 0.30f, height = 0.80f,
                asset = character.avatar,
                contentScale = "fit",
            )
        }
        out += OverrideElement.Bubble(
            id = "quote",
            x = 0.45f, y = 0.30f, width = 0.45f, height = 0.40f,
            body = bubble.body,
            speakerLabel = character?.displayName,
            sizeSp = 22f,
            hue = "sky",
        )
        return out
    }

    /** Generic fallback: one text element with a hint and the slide ID. */
    private fun synthesizeFallback(slide: Slide): List<OverrideElement> {
        return listOf(
            OverrideElement.Text(
                id = "placeholder",
                x = 0.1f, y = 0.4f, width = 0.8f, height = 0.2f,
                body = "Slide ${slide.id} — no template yet. Drag this text or use the palette to add elements.",
                sizeSp = 22f, align = "center", colorHex = "#94A3B8",
            ),
        )
    }

    private fun stripTokens(body: String): String =
        body.replace(Regex("""\{\{([^}]+)\}\}""")) { it.groupValues[1] }
}
