package com.learnlab.lessons.slides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.Hotspot
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.figures.composeDraw.ComposeFigureRegistry
import com.learnlab.lessons.lessonPalette
import com.learnlab.lessons.patterns.BuildUpReveal
import com.learnlab.lessons.patterns.BuildUpTable
import com.learnlab.lessons.patterns.CompareStage
import com.learnlab.lessons.patterns.DefinitionHero
import com.learnlab.lessons.patterns.GlossaryWall
import com.learnlab.lessons.patterns.HeroParagraph
import com.learnlab.lessons.patterns.InterludeKind
import com.learnlab.lessons.patterns.SpotlightInterlude
import com.learnlab.lessons.patterns.StoryLine
import com.learnlab.lessons.patterns.StoryPanelConversation
import com.learnlab.lessons.patterns.StoryPanelSingle

/* ───────────────────────── Dispatcher ───────────────────────── */

@Composable
fun SlideContent(
    slide: Slide,
    chapter: Chapter,
    cast: Map<String, Character>,
    onOpenActivity: (String) -> Unit,
) {
    when (slide.layout) {
        SlideLayout.Cover -> CoverSlide(chapter)
        SlideLayout.Shloka -> ShlokaSlide(slide.blocks[0] as ChapterBlock.SanskritShloka)
        SlideLayout.SectionTitle -> SectionTitleSlide(slide.blocks[0] as ChapterBlock.SectionHeader)
        SlideLayout.TextOnly -> TextOnlySlide(slide.blocks[0] as ChapterBlock.Paragraph, slide)
        SlideLayout.TextWithFigure -> TextWithFigureSlide(slide.blocks)
        SlideLayout.FigureFocus -> FigureFocusSlide(slide.blocks[0])
        SlideLayout.Compare -> CompareSlide(slide.blocks[0] as ChapterBlock.SideBySideCompare)
        SlideLayout.KeyTermCard -> KeyTermSlide(slide.blocks[0] as ChapterBlock.KeyTerm)
        SlideLayout.Story -> StorySlide(slide.blocks, cast)
        SlideLayout.Conversation -> ConversationSlide(slide.blocks, cast)
        SlideLayout.ScientistInterlude -> ScientistSlide(slide.blocks[0] as ChapterBlock.KnowScientist)
        SlideLayout.SuccessStoryInterlude -> SuccessStorySlide(slide.blocks[0] as ChapterBlock.SuccessStory)
        SlideLayout.DidYouKnowInterlude -> DidYouKnowSlide(slide.blocks[0])
        SlideLayout.Callout -> CalloutSlide(slide.blocks[0])
        SlideLayout.ActivityLaunch -> ActivityLaunchSlide(slide.blocks[0] as ChapterBlock.Activity, onOpenActivity)
        SlideLayout.TableSlide -> TableSlide(slide.blocks[0] as ChapterBlock.TableBlock)
        SlideLayout.Collage -> CollageSlide(slide.blocks[0] as ChapterBlock.FigureCollage)
        SlideLayout.KeywordCloud -> KeywordCloudSlide(slide.blocks[0] as ChapterBlock.KeywordCloud)
        SlideLayout.SummaryGrid -> SummaryGridSlide(slide.blocks[0] as ChapterBlock.Summary)
        SlideLayout.ExerciseSlide -> ExerciseSlide(slide.blocks[0] as ChapterBlock.Exercise)
        SlideLayout.LearningFurther -> LearningFurtherSlide(slide.blocks[0] as ChapterBlock.LearningFurther)
        SlideLayout.Closing -> ClosingSlide(slide.blocks[0] as ChapterBlock.Quotation)
    }
}

/* ───────────────────────── Cover ───────────────────────── */

@Composable
private fun CoverSlide(chapter: Chapter) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(p.emerald.surface, t.surface2))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 900.dp).padding(48.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(p.emerald.accent)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                LLText("CHAPTER ${chapter.chapter.number}", color = t.surface,
                    size = 16.sp, weight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(40.dp))
            LLText(
                chapter.chapter.title,
                color = t.ink50, size = 64.sp, weight = FontWeight.ExtraBold,
                lineHeight = 72.sp, align = TextAlign.Center,
            )
            if (chapter.chapter.subtitle != null) {
                Spacer(Modifier.height(20.dp))
                LLText(
                    chapter.chapter.subtitle, color = t.ink400, size = 22.sp,
                    lineHeight = 30.sp, align = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(48.dp))
            if (chapter.estimatedMinutes > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(p.emerald.surfaceStrong.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    LLText("~ ${chapter.estimatedMinutes} minutes",
                        color = p.emerald.accent, size = 15.sp, weight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/* ───────────────────────── Shloka ───────────────────────── */

@Composable
private fun ShlokaSlide(b: ChapterBlock.SanskritShloka) {
    val p = lessonPalette()
    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .clip(RoundedCornerShape(Radius.xl))
                .background(p.amber.surface)
                .border(1.dp, p.amber.border, RoundedCornerShape(Radius.xl))
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LLText(b.devanagari, color = p.amber.ink, size = 32.sp,
                lineHeight = 48.sp, align = TextAlign.Center, weight = FontWeight.Medium)
            if (b.transliteration != null) {
                Spacer(Modifier.height(16.dp))
                LLText(b.transliteration, color = p.amber.ink.copy(alpha = 0.7f),
                    size = 16.sp, lineHeight = 22.sp, align = TextAlign.Center)
            }
            Spacer(Modifier.height(28.dp))
            LLText(b.translation, color = p.amber.ink, size = 22.sp,
                lineHeight = 32.sp, align = TextAlign.Center)
            if (b.attribution != null) {
                Spacer(Modifier.height(20.dp))
                LLText(b.attribution, color = p.amber.accent, size = 14.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

/* ───────────────────────── Section title ───────────────────────── */

@Composable
private fun SectionTitleSlide(b: ChapterBlock.SectionHeader) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(modifier = Modifier.fillMaxSize().padding(64.dp), contentAlignment = Alignment.CenterStart) {
        Column(modifier = Modifier.widthIn(max = 1100.dp)) {
            Box(
                modifier = Modifier
                    .height(8.dp).width(72.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(Brush.horizontalGradient(listOf(p.emerald.accent, p.sky.accent))),
            )
            Spacer(Modifier.height(28.dp))
            if (b.number != null) {
                LLText(b.number, color = p.emerald.accent, size = 80.sp, weight = FontWeight.ExtraBold, lineHeight = 80.sp)
                Spacer(Modifier.height(8.dp))
            }
            LLText(b.title, color = t.ink50, size = 52.sp, weight = FontWeight.Bold, lineHeight = 60.sp)
        }
    }
}

/* ───────────────────────── Text only ───────────────────────── */

@Composable
private fun TextOnlySlide(b: ChapterBlock.Paragraph, slide: com.learnlab.content.chapter.Slide) {
    HeroParagraph(
        body = b.body,
        sectionNumber = slide.sectionNumber,
        sectionTitle = slide.sectionTitle,
    )
}

/* ───────────────────────── Text with figure ───────────────────────── */

@Composable
private fun TextWithFigureSlide(blocks: List<ChapterBlock>) {
    val t = LL.tokens
    val figure = blocks.firstOrNull { it is ChapterBlock.Figure } as? ChapterBlock.Figure
    val paragraph = blocks.firstOrNull { it is ChapterBlock.Paragraph } as? ChapterBlock.Paragraph
    if (figure == null || paragraph == null) return

    Row(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        Box(modifier = Modifier.weight(1.1f).fillMaxHeight(0.85f)) {
            FigureBlock(figure, captionSize = 14.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            LLText(
                text = paragraph.body.replace(Regex("""\{\{([^}]+)\}\}""")) { it.groupValues[1] },
                color = t.ink50, size = 22.sp, lineHeight = 34.sp,
            )
        }
    }
}

/* ───────────────────────── Figure focus ───────────────────────── */

@Composable
private fun FigureFocusSlide(block: ChapterBlock) {
    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        when (block) {
            is ChapterBlock.Figure -> FigureBlock(block, captionSize = 18.sp)
            is ChapterBlock.ImageWithCallout -> {
                val t = LL.tokens
                Column(
                    modifier = Modifier.widthIn(max = 1100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val bmp = loadFigureBitmap(block.asset)
                    if (bmp != null) {
                        Image(bitmap = bmp, contentDescription = block.caption,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.78f)
                                .clip(RoundedCornerShape(Radius.lg)))
                    } else {
                        PlaceholderImage(label = block.id, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.78f))
                    }
                    Spacer(Modifier.height(20.dp))
                    LLText(block.caption, color = t.ink400, size = 16.sp, align = TextAlign.Center,
                        lineHeight = 22.sp, modifier = Modifier.fillMaxWidth())
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun FigureBlock(b: ChapterBlock.Figure, captionSize: androidx.compose.ui.unit.TextUnit) {
    val t = LL.tokens
    var selectedHotspot by remember(b.id) { mutableStateOf<Hotspot?>(null) }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val isComposeDraw = b.assetType == "composeDraw" && ComposeFigureRegistry.has(b.asset)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(Radius.lg)),
        ) {
            if (isComposeDraw) {
                // Compose-drawn interactive diagram — owns its own interactivity.
                // We do NOT overlay hotspots; the diagram has its own tap targets.
                ComposeFigureRegistry.render(b.asset, modifier = Modifier.fillMaxSize())
            } else {
                val asset = loadFigureBitmap(b.asset)
                if (asset != null) {
                    Image(bitmap = asset, contentDescription = b.altText,
                        contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                } else {
                    PlaceholderImage(label = "${b.assetType} • ${b.asset}", modifier = Modifier.fillMaxSize())
                }
                if (b.capabilities.hotspots) {
                    b.hotspots.forEach { hs ->
                        HotspotDot(
                            hs = hs,
                            boxWidth = maxWidth,
                            boxHeight = maxHeight,
                            selected = selectedHotspot?.id == hs.id,
                            onTap = { selectedHotspot = if (selectedHotspot?.id == hs.id) null else hs },
                        )
                    }
                }
            }
        }
        if (b.caption != null) {
            Spacer(Modifier.height(12.dp))
            LLText(b.caption, color = t.ink400, size = captionSize, align = TextAlign.Center,
                lineHeight = (captionSize.value * 1.4f).sp, modifier = Modifier.fillMaxWidth())
        }
        if (!isComposeDraw && selectedHotspot != null) {
            Spacer(Modifier.height(12.dp))
            HotspotDetail(selectedHotspot!!)
        }
    }
}

@Composable
private fun HotspotDot(
    hs: Hotspot,
    boxWidth: androidx.compose.ui.unit.Dp,
    boxHeight: androidx.compose.ui.unit.Dp,
    selected: Boolean,
    onTap: () -> Unit,
) {
    val t = LL.tokens
    val xDp = boxWidth * hs.x
    val yDp = boxHeight * hs.y
    Box(
        modifier = Modifier
            .padding(start = xDp - 22.dp, top = yDp - 22.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(if (selected) t.accent500 else t.accent500.copy(alpha = 0.78f))
            .border(3.dp, Color.White, CircleShape)
            .clickable { onTap() },
        contentAlignment = Alignment.Center,
    ) {
        LLText(hs.label.firstOrNull()?.uppercase() ?: "·", color = Color.White, size = 18.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun HotspotDetail(hs: Hotspot) {
    val p = lessonPalette()
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(p.sky.surface)
            .border(1.dp, p.sky.border, RoundedCornerShape(Radius.md))
            .padding(20.dp),
    ) {
        LLText(hs.label, color = p.sky.accent, size = 20.sp, weight = FontWeight.Bold)
        if (hs.blurb != null) {
            Spacer(Modifier.height(6.dp))
            LLText(hs.blurb, color = p.sky.ink, size = 16.sp, lineHeight = 24.sp)
        }
    }
}

/* ───────────────────────── Compare (left/right) ───────────────────────── */

@Composable
private fun CompareSlide(b: ChapterBlock.SideBySideCompare) {
    CompareStage(
        caption = b.caption,
        leftImage = b.leftImage,
        leftLabel = b.leftLabel,
        leftBlurb = b.leftBlurb,
        rightImage = b.rightImage,
        rightLabel = b.rightLabel,
        rightBlurb = b.rightBlurb,
    )
}

@Composable
private fun CompareCell(label: String, blurb: String?, asset: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(Radius.lg))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(Radius.lg))
            .padding(20.dp),
    ) {
        val bmp = loadFigureBitmap(asset)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(Radius.md)),
            contentAlignment = Alignment.Center,
        ) {
            if (bmp != null) {
                Image(bitmap = bmp, contentDescription = label, contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize())
            } else {
                PlaceholderImage(label = label, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.height(16.dp))
        LLText(label, color = t.ink50, size = 24.sp, weight = FontWeight.Bold)
        if (blurb != null) {
            Spacer(Modifier.height(6.dp))
            LLText(blurb, color = t.ink400, size = 16.sp, lineHeight = 24.sp)
        }
    }
}

/* ───────────────────────── Key term card ───────────────────────── */

@Composable
private fun KeyTermSlide(b: ChapterBlock.KeyTerm) {
    DefinitionHero(term = b.term, definition = b.definition)
}

/* ───────────────────────── Story (single bubble) ───────────────────────── */

@Composable
private fun StorySlide(blocks: List<ChapterBlock>, cast: Map<String, Character>) {
    val t = LL.tokens
    val storyFrame = blocks.firstOrNull { it is ChapterBlock.StoryFrame } as? ChapterBlock.StoryFrame
    val bubble = blocks.firstOrNull { it is ChapterBlock.SpeechBubble } as? ChapterBlock.SpeechBubble

    when {
        bubble != null -> StoryPanelSingle(
            speaker = cast[bubble.speakerId],
            speakerIdFallback = bubble.speakerId,
            body = bubble.body,
        )
        storyFrame != null -> {
            // Keep the original scene-establishing layout for a pure StoryFrame
            // (no speaker). Render scene + character chips.
            Box(modifier = Modifier.fillMaxSize().padding(56.dp), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 1100.dp)) {
                    LLText("SCENE", color = t.ink500, size = 14.sp,
                        weight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(16.dp))
                    LLText(storyFrame.scene, color = t.ink50, size = 28.sp,
                        lineHeight = 40.sp, weight = FontWeight.Medium)
                    if (storyFrame.characters.isNotEmpty()) {
                        Spacer(Modifier.height(32.dp))
                        StaffRow(storyFrame.characters, cast)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StaffRow(ids: List<String>, cast: Map<String, Character>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ids.forEach { id ->
            val ch = cast[id]
            CharacterBadge(ch?.displayName ?: id, ch?.accentColor)
        }
    }
}

@Composable
private fun CharacterBadge(label: String, accentHex: String?) {
    val t = LL.tokens
    val accent = parseHexColor(accentHex, t.accent500)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(accent.copy(alpha = 0.2f))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(10.dp))
        LLText(label, color = t.ink50, size = 16.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun BigBubble(b: ChapterBlock.SpeechBubble, cast: Map<String, Character>) {
    val t = LL.tokens
    val ch = cast[b.speakerId]
    val accent = parseHexColor(ch?.accentColor, t.accent500)
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.widthIn(max = 1100.dp)) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.22f))
                .border(3.dp, accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            LLText((ch?.displayName ?: b.speakerId).firstOrNull()?.uppercase() ?: "?",
                color = accent, size = 40.sp, weight = FontWeight.Bold)
        }
        Spacer(Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            LLText((ch?.displayName ?: b.speakerId).uppercase(), color = accent,
                size = 16.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = Radius.lg,
                        bottomEnd = Radius.lg, bottomStart = Radius.lg))
                    .background(t.surface2)
                    .padding(28.dp),
            ) {
                ItalicText("“${b.body}”", color = t.ink50, size = 28.sp, lineHeight = 40.sp)
            }
        }
    }
}

@Composable
private fun ItalicText(text: String, color: Color, size: androidx.compose.ui.unit.TextUnit, lineHeight: androidx.compose.ui.unit.TextUnit) {
    androidx.compose.material3.Text(
        text = text, color = color, fontSize = size, lineHeight = lineHeight, fontStyle = FontStyle.Italic,
    )
}

/* ───────────────────────── Conversation (multiple bubbles) ───────────────────────── */

@Composable
private fun ConversationSlide(blocks: List<ChapterBlock>, cast: Map<String, Character>) {
    val lines = blocks.filterIsInstance<ChapterBlock.SpeechBubble>().map { b ->
        StoryLine(speaker = cast[b.speakerId], idFallback = b.speakerId, body = b.body)
    }
    StoryPanelConversation(lines = lines)
}

@Composable
private fun ConversationBubble(b: ChapterBlock.SpeechBubble, cast: Map<String, Character>, alignRight: Boolean) {
    val t = LL.tokens
    val ch = cast[b.speakerId]
    val accent = parseHexColor(ch?.accentColor, t.accent500)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!alignRight) Avatar(ch, accent, b.speakerId)
        if (!alignRight) Spacer(Modifier.width(16.dp))
        Column(
            horizontalAlignment = if (alignRight) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 760.dp),
        ) {
            LLText((ch?.displayName ?: b.speakerId).uppercase(), color = accent,
                size = 13.sp, weight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(
                        if (alignRight)
                            RoundedCornerShape(topStart = Radius.lg, topEnd = 4.dp,
                                bottomEnd = Radius.lg, bottomStart = Radius.lg)
                        else
                            RoundedCornerShape(topStart = 4.dp, topEnd = Radius.lg,
                                bottomEnd = Radius.lg, bottomStart = Radius.lg)
                    )
                    .background(t.surface2)
                    .padding(22.dp),
            ) {
                ItalicText("“${b.body}”", color = t.ink50, size = 22.sp, lineHeight = 32.sp)
            }
        }
        if (alignRight) Spacer(Modifier.width(16.dp))
        if (alignRight) Avatar(ch, accent, b.speakerId)
    }
}

@Composable
private fun Avatar(ch: Character?, accent: Color, speakerId: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.22f))
            .border(3.dp, accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LLText((ch?.displayName ?: speakerId).firstOrNull()?.uppercase() ?: "?",
            color = accent, size = 26.sp, weight = FontWeight.Bold)
    }
}

/* ───────────────────────── Scientist interlude ───────────────────────── */

@Composable
private fun ScientistSlide(b: ChapterBlock.KnowScientist) {
    val sub = listOfNotNull(b.dates, b.fields.takeIf { it.isNotEmpty() }?.joinToString(" · "))
        .joinToString(" · ")
    SpotlightInterlude(
        kind = InterludeKind.Scientist,
        title = b.name,
        body = b.biography,
        heroImage = b.portraitAsset,
        heroLabel = sub.ifBlank { null },
    )
}

/* ───────────────────────── Success story interlude ───────────────────────── */

@Composable
private fun SuccessStorySlide(b: ChapterBlock.SuccessStory) {
    SpotlightInterlude(
        kind = InterludeKind.SuccessStory,
        title = b.title,
        body = b.body,
        heroImage = b.imageAsset,
        highlight = b.highlight,
    )
}

/* ───────────────────────── DidYouKnow / MoreToKnow ───────────────────────── */

@Composable
private fun DidYouKnowSlide(block: ChapterBlock) {
    when (block) {
        is ChapterBlock.DoYouKnow -> SpotlightInterlude(
            kind = InterludeKind.DidYouKnow,
            title = block.title,
            body = block.body,
            galleryImages = block.images.map { it.asset to it.caption },
        )
        is ChapterBlock.MoreToKnow -> SpotlightInterlude(
            kind = InterludeKind.MoreToKnow,
            title = block.title,
            body = block.body,
            heroImage = block.image,
            heroLabel = block.imageCaption,
        )
        else -> Unit
    }
}

/* ───────────────────────── Callout / WorkedExample ───────────────────────── */

@Composable
private fun CalloutSlide(block: ChapterBlock) {
    val t = LL.tokens
    val p = lessonPalette()
    when (block) {
        is ChapterBlock.Callout -> {
            val hue = when (block.tone) {
                "warning" -> p.rose
                "success" -> p.emerald
                "fact" -> p.amber
                else -> p.sky
            }
            BuildUpReveal(
                title = block.title,
                body = block.body,
                accentSurface = hue.surface,
                accentBorder = hue.border,
                accentInk = hue.ink,
                accentColor = hue.accent,
            )
        }
        is ChapterBlock.WorkedExample -> {
            Box(modifier = Modifier.fillMaxSize().padding(56.dp), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 1100.dp)) {
                    LLText(block.title, color = p.violet.accent, size = 28.sp,
                        weight = FontWeight.Bold, lineHeight = 36.sp)
                    Spacer(Modifier.height(28.dp))
                    block.steps.forEachIndexed { i, step ->
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .background(p.violet.surfaceStrong),
                                contentAlignment = Alignment.Center,
                            ) {
                                LLText("${i + 1}", color = p.violet.accent,
                                    size = 18.sp, weight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(20.dp))
                            LLText(step, color = t.ink50, size = 20.sp, lineHeight = 30.sp,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        is ChapterBlock.Quotation -> {
            ClosingSlide(block)
        }
        else -> {}
    }
}

/* ───────────────────────── Activity launch ───────────────────────── */

@Composable
private fun ActivityLaunchSlide(b: ChapterBlock.Activity, onOpen: (String) -> Unit) {
    val t = LL.tokens
    val p = lessonPalette()
    val interactive = b.experimentId != null
    Box(modifier = Modifier.fillMaxSize().padding(56.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .clip(RoundedCornerShape(Radius.xl))
                .background(p.emerald.surface)
                .border(1.dp, p.emerald.border, RoundedCornerShape(Radius.xl))
                .padding(56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(p.emerald.surfaceStrong),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Science, contentDescription = null,
                        tint = p.emerald.accent, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(20.dp))
                if (b.ncertReference != null) {
                    LLText(b.ncertReference.uppercase(), color = p.emerald.accent,
                        size = 16.sp, weight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            LLText(b.title, color = t.ink50, size = 40.sp, weight = FontWeight.ExtraBold,
                align = TextAlign.Center, lineHeight = 48.sp)
            if (b.intro != null) {
                Spacer(Modifier.height(24.dp))
                LLText(b.intro, color = t.ink200, size = 20.sp, lineHeight = 30.sp,
                    align = TextAlign.Center)
            }
            if (b.estimatedMinutes > 0) {
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(p.emerald.surfaceStrong.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    LLText("~ ${b.estimatedMinutes} min", color = p.emerald.accent,
                        size = 14.sp, weight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(36.dp))
            if (interactive) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.md))
                        .background(p.emerald.accent)
                        .clickable { onOpen(b.experimentId!!) }
                        .padding(horizontal = 36.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null,
                        tint = t.surface, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    LLText("Open activity", color = t.surface, size = 20.sp, weight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                        tint = t.surface, modifier = Modifier.size(20.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(p.emerald.surfaceStrong)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    LLText("${b.kind.uppercase()} — teacher-led",
                        color = p.emerald.accent, size = 14.sp, weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }
        }
    }
}

/* ───────────────────────── Table slide ───────────────────────── */

@Composable
private fun TableSlide(b: ChapterBlock.TableBlock) {
    BuildUpTable(caption = b.caption, headers = b.headers, rows = b.rows)
}

/* ───────────────────────── Collage (Fig 2.7 animals) ───────────────────────── */

@Composable
private fun CollageSlide(b: ChapterBlock.FigureCollage) {
    val t = LL.tokens
    var selected by remember(b.id) { mutableStateOf<com.learnlab.content.chapter.CollageCircle?>(null) }
    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 1200.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (selected != null) 0.62f else 0.82f)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(Radius.lg)),
            ) {
                val bmp = loadFigureBitmap(b.asset)
                if (bmp != null) {
                    Image(bitmap = bmp, contentDescription = b.altText,
                        contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                } else {
                    PlaceholderImage(label = "scene collage", modifier = Modifier.fillMaxSize())
                }
                b.circles.forEach { c ->
                    val xDp = maxWidth * c.x
                    val yDp = maxHeight * c.y
                    Box(
                        modifier = Modifier
                            .padding(start = xDp - 26.dp, top = yDp - 26.dp)
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(t.accent500.copy(alpha = 0.35f))
                            .border(3.dp, t.accent500, CircleShape)
                            .clickable { selected = if (selected?.id == c.id) null else c },
                    )
                }
            }
            if (b.caption != null) {
                Spacer(Modifier.height(14.dp))
                LLText(b.caption, color = t.ink400, size = 16.sp, align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
            if (selected != null) {
                val c = selected!!
                val p = lessonPalette()
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(p.emerald.surface)
                        .border(1.dp, p.emerald.border, RoundedCornerShape(Radius.md))
                        .padding(20.dp),
                ) {
                    LLText(c.label, color = p.emerald.accent, size = 22.sp, weight = FontWeight.Bold)
                    if (c.moves != null) {
                        Spacer(Modifier.height(4.dp))
                        LLText("Moves: ${c.moves}", color = p.emerald.ink, size = 17.sp)
                    }
                    if (c.parts != null) {
                        LLText("Body parts: ${c.parts}", color = p.emerald.ink, size = 17.sp)
                    }
                }
            }
        }
    }
}

/* ───────────────────────── Keyword cloud ───────────────────────── */

@Composable
private fun KeywordCloudSlide(b: ChapterBlock.KeywordCloud) {
    GlossaryWall(title = b.title, terms = b.terms)
}

/* ───────────────────────── Summary grid ───────────────────────── */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGridSlide(b: ChapterBlock.Summary) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 1300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LLText(b.title, color = p.amber.accent, size = 26.sp,
                weight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(28.dp))
            // 3-column grid via FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                b.points.forEach { pt ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 380.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(p.amber.surface)
                            .border(1.dp, p.amber.border, RoundedCornerShape(Radius.md))
                            .padding(18.dp),
                    ) {
                        LLText(pt, color = p.amber.ink, size = 15.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

/* ───────────────────────── Exercise slide ───────────────────────── */

@Composable
private fun ExerciseSlide(b: ChapterBlock.Exercise) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(modifier = Modifier.fillMaxSize().padding(40.dp)) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 1300.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(p.sky.surfaceStrong),
                    contentAlignment = Alignment.Center,
                ) {
                    LLText(b.number, color = p.sky.accent, size = 22.sp, weight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                LLText("EXERCISE ${b.number}", color = p.sky.accent, size = 14.sp,
                    weight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(20.dp))
            LLText(b.prompt, color = t.ink50, size = 22.sp, lineHeight = 32.sp,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(28.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                ExerciseEngineHost(b)
            }
        }
    }
}

/* ───────────────────────── Learning further ───────────────────────── */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LearningFurtherSlide(b: ChapterBlock.LearningFurther) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 1300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LLText(b.title, color = p.violet.accent, size = 32.sp, weight = FontWeight.Bold)
            Spacer(Modifier.height(28.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                b.projects.forEachIndexed { i, prj ->
                    Column(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 400.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(p.violet.surface)
                            .border(1.dp, p.violet.border, RoundedCornerShape(Radius.md))
                            .padding(20.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape)
                                    .background(p.violet.surfaceStrong),
                                contentAlignment = Alignment.Center,
                            ) {
                                LLText("${i + 1}", color = p.violet.accent,
                                    size = 14.sp, weight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            LLText("PROJECT", color = p.violet.accent, size = 11.sp,
                                weight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        LLText(prj.body, color = p.violet.ink, size = 15.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

/* ───────────────────────── Closing ───────────────────────── */

@Composable
private fun ClosingSlide(b: ChapterBlock.Quotation) {
    val t = LL.tokens
    val p = lessonPalette()
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(p.emerald.surface, t.surface2)))
            .padding(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.widthIn(max = 1100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LLText("“${b.body}”", color = t.ink50, size = 36.sp, lineHeight = 52.sp,
                align = TextAlign.Center, weight = FontWeight.Medium)
            if (b.attribution != null) {
                Spacer(Modifier.height(28.dp))
                LLText(b.attribution, color = p.emerald.accent, size = 18.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

/* ───────────────────────── Placeholder image ───────────────────────── */

@Composable
fun PlaceholderImage(label: String, modifier: Modifier = Modifier) {
    val t = LL.tokens
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(t.surface3),
        contentAlignment = Alignment.Center,
    ) {
        LLText("[ $label ]", color = t.ink500, size = 14.sp, weight = FontWeight.Medium)
    }
}
