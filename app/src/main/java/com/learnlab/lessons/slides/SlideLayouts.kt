package com.learnlab.lessons.slides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterBlock
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.Hotspot
import com.learnlab.content.chapter.OverrideElement
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideLayout
import com.learnlab.content.chapter.SlideOverride
import com.learnlab.data.sync.SlideAssetsRepository
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius
import com.learnlab.lessons.figures.composeDraw.ComposeFigureRegistry
import com.learnlab.lessons.LessonPalette
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
    isEditing: Boolean = false,
    onOverrideEdited: (slideId: String, override: SlideOverride) -> Unit = { _, _ -> },
    onOverrideCommitted: (slideId: String, override: SlideOverride) -> Unit = { _, _ -> },
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
        SlideLayout.SectionIntro -> SectionIntroSlide(
            slide.blocks[0] as ChapterBlock.SectionHeader,
            slide.blocks[1] as ChapterBlock.Paragraph,
        )
        SlideLayout.StoryWithContext -> StoryWithContextSlide(slide.blocks, cast)
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
        SlideLayout.FreeForm -> {
            val ov = slide.override
            if (ov != null) {
                FreeFormSlide(
                    override = ov,
                    isEditing = isEditing,
                    chapterId = chapter.id,
                    slideId = slide.id,
                    onEdited = { updated -> onOverrideEdited(slide.id, updated) },
                    onCommitted = { updated -> onOverrideCommitted(slide.id, updated) },
                )
            }
        }
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
                color = t.ink50, size = 28.sp, lineHeight = 42.sp,
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
                    LLText(block.caption, color = t.ink400, size = 22.sp, align = TextAlign.Center,
                        lineHeight = 30.sp, modifier = Modifier.fillMaxWidth())
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
        LLText(label, color = t.ink50, size = 32.sp, weight = FontWeight.Bold)
        if (blurb != null) {
            Spacer(Modifier.height(10.dp))
            LLText(blurb, color = t.ink400, size = 22.sp, lineHeight = 32.sp)
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
            LLText(b.title, color = t.ink50, size = 48.sp, weight = FontWeight.ExtraBold,
                align = TextAlign.Center, lineHeight = 56.sp)
            if (b.intro != null) {
                Spacer(Modifier.height(28.dp))
                LLText(b.intro, color = t.ink200, size = 26.sp, lineHeight = 38.sp,
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
                            .widthIn(min = 340.dp, max = 460.dp)
                            .clip(RoundedCornerShape(Radius.md))
                            .background(p.amber.surface)
                            .border(1.dp, p.amber.border, RoundedCornerShape(Radius.md))
                            .padding(22.dp),
                    ) {
                        LLText(pt, color = p.amber.ink, size = 20.sp, lineHeight = 30.sp)
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
            LLText(b.prompt, color = t.ink50, size = 28.sp, lineHeight = 40.sp,
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

/* ───────────────────────── Section intro (header + first paragraph) ───────────────────────── */

@Composable
private fun SectionIntroSlide(header: ChapterBlock.SectionHeader, para: ChapterBlock.Paragraph) {
    val t = LL.tokens
    val p = lessonPalette()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 72.dp, vertical = 72.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(72.dp),
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .height(6.dp).width(64.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(Brush.horizontalGradient(listOf(p.emerald.accent, p.sky.accent))),
            )
            Spacer(Modifier.height(16.dp))
            LLText("SECTION", color = p.emerald.accent, size = 12.sp,
                weight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(20.dp))
            if (header.number != null) {
                LLText(header.number, color = p.emerald.accent, size = 72.sp,
                    weight = FontWeight.ExtraBold, lineHeight = 72.sp)
                Spacer(Modifier.height(12.dp))
            }
            LLText(header.title, color = t.ink50, size = 34.sp,
                weight = FontWeight.Bold, lineHeight = 42.sp)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .height(1.dp).fillMaxWidth(0.3f)
                    .background(t.line),
            )
        }
        Column(
            modifier = Modifier.weight(1.4f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            LLText(
                text = para.body.replace(Regex("""\{\{([^}]+)\}\}""")) { it.groupValues[1] },
                color = t.ink50,
                size = 26.sp,
                lineHeight = 40.sp,
                weight = FontWeight.Medium,
            )
        }
    }
}

/* ───────────────────────── Paragraph + lone speech bubble ───────────────────────── */

@Composable
private fun StoryWithContextSlide(blocks: List<ChapterBlock>, cast: Map<String, Character>) {
    val t = LL.tokens
    val p = lessonPalette()
    val para = blocks.firstOrNull { it is ChapterBlock.Paragraph } as? ChapterBlock.Paragraph ?: return
    val bubble = blocks.firstOrNull { it is ChapterBlock.SpeechBubble } as? ChapterBlock.SpeechBubble ?: return
    val ch = cast[bubble.speakerId]

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Cream card holding the whole composition. Soft shadow, no border.
        // Fills the slide area so we don't leave dead bands above and below.
        // Card layout: paragraph on the left, portrait+bubble stacked on the
        // right (portrait on top, bubble cleanly below it).
        Row(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), clip = false)
                .clip(RoundedCornerShape(20.dp))
                .background(t.surface)
                .padding(40.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Paragraph — left column.
            Column(modifier = Modifier.weight(1.4f)) {
                LLText(
                    text = para.body.replace(Regex("""\{\{([^}]+)\}\}""")) { it.groupValues[1] },
                    color = t.ink50,
                    size = 26.sp,
                    lineHeight = 40.sp,
                )
            }
            // Right column: portrait on top, bubble below.
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                val bmp = ch?.avatar?.let { loadFigureBitmap(it) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = ch?.displayName ?: bubble.speakerId,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        val accent = parseHexColor(ch?.accentColor, t.accent500)
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f))
                                .border(3.dp, accent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            LLText(
                                (ch?.displayName ?: bubble.speakerId).firstOrNull()?.uppercase() ?: "?",
                                color = accent, size = 80.sp, weight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.sky.surface)
                        .border(1.dp, p.sky.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    ItalicText(
                        text = "“${bubble.body}”",
                        color = t.ink50,
                        size = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
        }
    }
}

/* ───────────────────────── Free-form (author-edited) ───────────────────────── */

/**
 * Renders an author-edited slide. Every element is absolutely positioned
 * using fractional coordinates (0..1) of the slide area, so the same
 * override looks identical across screen sizes.
 *
 * No edit affordances here — those live in a separate overlay added later.
 */
@Composable
private fun FreeFormSlide(
    override: SlideOverride,
    isEditing: Boolean,
    chapterId: String,
    slideId: String,
    onEdited: (SlideOverride) -> Unit,
    onCommitted: (SlideOverride) -> Unit,
) {
    val t = LL.tokens
    val bg = override.backgroundHex?.let { parseHexColor(it, t.surface) } ?: t.surface

    // Local mutable copy so drags feel immediate; we report up on each change.
    // Reset whenever the incoming override (or its element set) changes so we
    // stay in sync after a remote refetch or a slide change.
    val elementsState = remember(override) {
        mutableStateListOf<OverrideElement>().apply { addAll(override.elements) }
    }
    var selectedId by remember(override) { mutableStateOf<String?>(null) }

    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Tracks the element we last asked to swap. The launcher writes back to
    // it when the picker returns. Stored as id (string) so it survives the
    // launcher's separate lifecycle.
    var pendingSwapElementId by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val target = pendingSwapElementId ?: return@rememberLauncherForActivityResult
        pendingSwapElementId = null
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val url = SlideAssetsRepository.uploadImage(ctx, uri, chapterId, slideId)
            if (url != null) {
                val idx = elementsState.indexOfFirst { it.id == target }
                if (idx >= 0) {
                    val current = elementsState[idx]
                    if (current is OverrideElement.Image) {
                        elementsState[idx] = current.copy(asset = url)
                        val updated = override.copy(elements = elementsState.toList())
                        onEdited(updated); onCommitted(updated)
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(bg),
        // No parent-level tap handler: a sibling detectTapGestures here
        // races the element's own tap and was clearing selectedId
        // immediately after a child selected. To deselect, tap pencil again
        // (exits edit mode) or tap the same element a second time (future
        // toggle). Without this fix, tapping an element appeared to do
        // nothing.
    ) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight
        val density = LocalDensity.current
        // Pre-compute pixel sizes once so dragging math is cheap.
        val parentWidthPx = with(density) { parentWidth.toPx() }
        val parentHeightPx = with(density) { parentHeight.toPx() }

        // Track which (elementId, asset) pairs we've already auto-fitted so
        // we don't re-fire the height adjustment forever.
        val fittedAssets = remember(override) { mutableStateListOf<String>() }
        elementsState.forEachIndexed { idx, el ->
            FreeFormElement(
                el = el,
                parentWidth = parentWidth,
                parentHeight = parentHeight,
                parentWidthPx = parentWidthPx,
                parentHeightPx = parentHeightPx,
                isEditing = isEditing,
                isSelected = isEditing && selectedId == el.id,
                onSelect = { selectedId = el.id },
                onMove = { dxFrac, dyFrac ->
                    val current = elementsState[idx]
                    val updated = current.withPosition(current.x + dxFrac, current.y + dyFrac)
                    elementsState[idx] = updated
                    onEdited(override.copy(elements = elementsState.toList()))
                },
                onMoveEnd = { onCommitted(override.copy(elements = elementsState.toList())) },
                onAspectKnown = { ratio ->
                    // For image elements with contentScale=fit, shrink the
                    // box's height so it wraps the image instead of being
                    // a tall empty bounding rect. Width is preserved; new
                    // height = width × (slide-aspect) ÷ image-aspect.
                    val current = elementsState[idx]
                    if (current is OverrideElement.Image && current.contentScale.lowercase() == "fit") {
                        val key = "${current.id}:${current.asset}"
                        if (key !in fittedAssets && parentWidthPx > 0f && parentHeightPx > 0f) {
                            val pxWidth = current.width * parentWidthPx
                            val pxHeight = pxWidth / ratio
                            val newHeightFrac = (pxHeight / parentHeightPx).coerceIn(0.05f, 1f)
                            // Only adjust if the new height is meaningfully
                            // different (>5% change) — avoids tiny jitters.
                            if (kotlin.math.abs(newHeightFrac - current.height) > 0.02f) {
                                elementsState[idx] = current.copy(height = newHeightFrac)
                                val updated = override.copy(elements = elementsState.toList())
                                onEdited(updated); onCommitted(updated)
                            }
                            fittedAssets.add(key)
                        }
                    }
                },
            )
        }
        if (isEditing) {
            // Card outline. drawn behind handles so handles stay clickable.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, t.accent500, RoundedCornerShape(20.dp)),
            )
            // Add-element palette, pinned right edge of the card.
            ElementPalette(
                parentWidth = parentWidth,
                parentHeight = parentHeight,
                onAddText = {
                    val newEl = OverrideElement.Text(
                        id = "text-${System.currentTimeMillis()}",
                        x = 0.35f, y = 0.40f, width = 0.30f, height = 0.20f,
                        body = "New text",
                        sizeSp = 24f,
                    )
                    elementsState.add(newEl)
                    selectedId = newEl.id
                    val updated = override.copy(elements = elementsState.toList())
                    onEdited(updated); onCommitted(updated)
                },
                onAddBubble = {
                    val newEl = OverrideElement.Bubble(
                        id = "bubble-${System.currentTimeMillis()}",
                        x = 0.35f, y = 0.40f, width = 0.30f, height = 0.18f,
                        body = "New quote",
                        sizeSp = 16f,
                        hue = "sky",
                    )
                    elementsState.add(newEl)
                    selectedId = newEl.id
                    val updated = override.copy(elements = elementsState.toList())
                    onEdited(updated); onCommitted(updated)
                },
                onAddImage = {
                    val newEl = OverrideElement.Image(
                        id = "image-${System.currentTimeMillis()}",
                        x = 0.35f, y = 0.30f, width = 0.30f, height = 0.40f,
                        // Default to a known-present asset until step 7 wires
                        // up the file picker. The author swaps it then.
                        asset = "cast/dadi-leela.png",
                        contentScale = "fit",
                    )
                    elementsState.add(newEl)
                    selectedId = newEl.id
                    val updated = override.copy(elements = elementsState.toList())
                    onEdited(updated); onCommitted(updated)
                },
            )
            // Selection handles for the currently selected element.
            val selectedIdx = elementsState.indexOfFirst { it.id == selectedId }
            if (selectedIdx >= 0) {
                val selectedEl = elementsState[selectedIdx]
                SelectionToolbar(
                    el = selectedEl,
                    parentWidth = parentWidth,
                    parentHeight = parentHeight,
                    canMoveForward = selectedIdx < elementsState.lastIndex,
                    canMoveBackward = selectedIdx > 0,
                    onDelete = {
                        elementsState.removeAt(selectedIdx)
                        selectedId = null
                        val updatedOverride = override.copy(elements = elementsState.toList())
                        onEdited(updatedOverride)
                        onCommitted(updatedOverride)
                    },
                    onForward = {
                        val moving = elementsState.removeAt(selectedIdx)
                        elementsState.add(selectedIdx + 1, moving)
                        val updatedOverride = override.copy(elements = elementsState.toList())
                        onEdited(updatedOverride)
                        onCommitted(updatedOverride)
                    },
                    onBackward = {
                        val moving = elementsState.removeAt(selectedIdx)
                        elementsState.add(selectedIdx - 1, moving)
                        val updatedOverride = override.copy(elements = elementsState.toList())
                        onEdited(updatedOverride)
                        onCommitted(updatedOverride)
                    },
                    onSwapImage = if (selectedEl is OverrideElement.Image) {
                        {
                            pendingSwapElementId = selectedEl.id
                            imagePicker.launch("image/*")
                        }
                    } else null,
                )
                ResizeHandles(
                    el = elementsState[selectedIdx],
                    parentWidth = parentWidth,
                    parentHeight = parentHeight,
                    parentWidthPx = parentWidthPx,
                    parentHeightPx = parentHeightPx,
                    onResize = { newX, newY, newW, newH ->
                        val current = elementsState[selectedIdx]
                        val updated = current.withGeometry(newX, newY, newW, newH)
                        elementsState[selectedIdx] = updated
                        onEdited(override.copy(elements = elementsState.toList()))
                    },
                    onResizeEnd = { onCommitted(override.copy(elements = elementsState.toList())) },
                )
                RotateHandle(
                    el = elementsState[selectedIdx],
                    parentWidth = parentWidth,
                    parentHeight = parentHeight,
                    parentWidthPx = parentWidthPx,
                    parentHeightPx = parentHeightPx,
                    onRotate = { newDegrees ->
                        val current = elementsState[selectedIdx]
                        val updated = current.withRotation(newDegrees)
                        elementsState[selectedIdx] = updated
                        onEdited(override.copy(elements = elementsState.toList()))
                    },
                    onRotateEnd = { onCommitted(override.copy(elements = elementsState.toList())) },
                )
            }
        }
    }
}

/**
 * Vertical add-element palette pinned to the right edge of the card. Only
 * visible in edit mode. Each button creates a new element centred on the
 * slide, then auto-selects it for further editing.
 */
@Composable
private fun ElementPalette(
    parentWidth: androidx.compose.ui.unit.Dp,
    parentHeight: androidx.compose.ui.unit.Dp,
    onAddText: () -> Unit,
    onAddBubble: () -> Unit,
    onAddImage: () -> Unit,
) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .padding(start = (parentWidth - 56.dp).coerceAtLeast(0.dp), top = 76.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(24.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PaletteBtn(Icons.Filled.TextFields, "Add text", onAddText)
        PaletteBtn(Icons.Filled.ChatBubble, "Add speech bubble", onAddBubble)
        PaletteBtn(Icons.Filled.AddPhotoAlternate, "Add image", onAddImage)
    }
}

@Composable
private fun PaletteBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(t.surface2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = t.ink200, modifier = Modifier.size(22.dp))
    }
}

/**
 * Floating action toolbar that appears at the top-right of the card when
 * an element is selected. Holds Delete + z-order controls. Anchored to the
 * card (not the element) so it doesn't fight with the rotate handle.
 */
@Composable
private fun SelectionToolbar(
    el: OverrideElement,
    parentWidth: androidx.compose.ui.unit.Dp,
    parentHeight: androidx.compose.ui.unit.Dp,
    canMoveForward: Boolean,
    canMoveBackward: Boolean,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    onSwapImage: (() -> Unit)? = null,
) {
    val t = LL.tokens
    // Width grows with number of buttons so the toolbar stays roughly
    // visually centred at the top-right.
    val buttonCount = 3 + (if (onSwapImage != null) 1 else 0)
    val width = (buttonCount * 44 + 24).dp
    Row(
        modifier = Modifier
            .padding(start = (parentWidth - width).coerceAtLeast(0.dp), top = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(t.surface)
            .border(1.dp, t.accent500, RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onSwapImage != null) {
            ToolbarBtn(Icons.Filled.AddPhotoAlternate, "Swap image", onSwapImage)
        }
        ToolbarBtn(Icons.Filled.FlipToBack, "Send backward", onBackward, enabled = canMoveBackward)
        ToolbarBtn(Icons.Filled.FlipToFront, "Bring forward", onForward, enabled = canMoveForward)
        ToolbarBtn(Icons.Filled.Delete, "Delete", onDelete, destructive = true)
    }
}

@Composable
private fun ToolbarBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val t = LL.tokens
    val tint = when {
        !enabled -> t.ink500
        destructive -> Color(0xFFE11D48)
        else -> t.ink200
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/**
 * Eight resize handles around the selected element (4 corners, 4 edges).
 * Each handle drags in screen pixels; the math converts to slide fractions
 * and emits a new (x, y, width, height) for the element.
 *
 *   nw ── n ── ne
 *   |          |
 *   w          e
 *   |          |
 *   sw ── s ── se
 */
@Composable
private fun ResizeHandles(
    el: OverrideElement,
    parentWidth: androidx.compose.ui.unit.Dp,
    parentHeight: androidx.compose.ui.unit.Dp,
    parentWidthPx: Float,
    parentHeightPx: Float,
    onResize: (x: Float, y: Float, width: Float, height: Float) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val handleSize = 18.dp
    val halfHandle = handleSize / 2
    val xDp = parentWidth * el.x
    val yDp = parentHeight * el.y
    val wDp = parentWidth * el.width
    val hDp = parentHeight * el.height

    data class HandleSpec(
        val offsetX: androidx.compose.ui.unit.Dp,
        val offsetY: androidx.compose.ui.unit.Dp,
        val dragX: Boolean,  // does this handle move the element's x edge?
        val dragY: Boolean,  // does it move the y edge?
        val dragW: Int,      // -1 left-edge (x moves), +1 right-edge (only width)
        val dragH: Int,      // -1 top-edge, +1 bottom-edge
    )

    val handles = listOf(
        // Corners
        HandleSpec(xDp - halfHandle, yDp - halfHandle, true,  true,  -1, -1), // NW
        HandleSpec(xDp + wDp - halfHandle, yDp - halfHandle, false, true,  +1, -1), // NE
        HandleSpec(xDp - halfHandle, yDp + hDp - halfHandle, true,  false, -1, +1), // SW
        HandleSpec(xDp + wDp - halfHandle, yDp + hDp - halfHandle, false, false, +1, +1), // SE
        // Edge midpoints
        HandleSpec(xDp + wDp / 2 - halfHandle, yDp - halfHandle, false, true,  0, -1), // N
        HandleSpec(xDp + wDp / 2 - halfHandle, yDp + hDp - halfHandle, false, false, 0, +1), // S
        HandleSpec(xDp - halfHandle, yDp + hDp / 2 - halfHandle, true,  false, -1, 0), // W
        HandleSpec(xDp + wDp - halfHandle, yDp + hDp / 2 - halfHandle, false, false, +1, 0), // E
    )

    handles.forEachIndexed { i, h ->
        ResizeHandle(
            elementId = el.id,
            offsetX = h.offsetX,
            offsetY = h.offsetY,
            size = handleSize,
            // Capture starting geometry on drag-start so each tick applies
            // cumulative delta against the START, not the live (mid-resize)
            // values. Without this, dragging a corner is a no-op because the
            // element's x/y/w/h have already shifted by the time the next
            // tick reads them.
            startX = el.x,
            startY = el.y,
            startW = el.width,
            startH = el.height,
            dragW = h.dragW,
            dragH = h.dragH,
            parentWidthPx = parentWidthPx,
            parentHeightPx = parentHeightPx,
            onResize = onResize,
            onDragEnd = onResizeEnd,
        )
    }
}

/**
 * Single rotation handle, positioned 28dp above the element's top-centre.
 * Drag in a circle around the element's centre to rotate. Snaps to common
 * angles when within 4 degrees.
 */
@Composable
private fun RotateHandle(
    el: OverrideElement,
    parentWidth: androidx.compose.ui.unit.Dp,
    parentHeight: androidx.compose.ui.unit.Dp,
    parentWidthPx: Float,
    parentHeightPx: Float,
    onRotate: (degrees: Float) -> Unit,
    onRotateEnd: () -> Unit,
) {
    val t = LL.tokens
    val handleSize = 22.dp
    val halfHandle = handleSize / 2
    val arm = 28.dp
    val xDp = parentWidth * el.x
    val yDp = parentHeight * el.y
    val wDp = parentWidth * el.width

    val handleCx = xDp + wDp / 2
    val handleCy = yDp - arm

    // Centre of the element in card pixels (used for atan2).
    val centerXPx = (el.x + el.width / 2f) * parentWidthPx
    val centerYPx = (el.y + el.height / 2f) * parentHeightPx

    // Pointer position is reported in coordinates of the handle. We need
    // absolute card-space coords, so track our running screen-space position.
    // Initial pointer (in card px) when drag starts = centre of handle.
    val handleStartCx = with(LocalDensity.current) { handleCx.toPx() } + with(LocalDensity.current) { halfHandle.toPx() }
    val handleStartCy = with(LocalDensity.current) { handleCy.toPx() } + with(LocalDensity.current) { halfHandle.toPx() }

    val safeStart = (handleCx - halfHandle).coerceAtLeast(0.dp)
    val safeTop = (handleCy - halfHandle).coerceAtLeast(0.dp)
    Box(
        modifier = Modifier
            .offset(x = safeStart, y = safeTop)
            .size(handleSize)
            .clip(CircleShape)
            .background(t.surface)
            .border(2.dp, t.accent500, CircleShape)
            .pointerInput(el.id) {
                var cursorX = handleStartCx
                var cursorY = handleStartCy
                detectDragGestures(
                    onDragStart = {
                        cursorX = handleStartCx
                        cursorY = handleStartCy
                    },
                    onDragEnd = { onRotateEnd() },
                    onDragCancel = { onRotateEnd() },
                    onDrag = { change, drag ->
                        change.consume()
                        cursorX += drag.x
                        cursorY += drag.y
                        val ang = Math.toDegrees(
                            kotlin.math.atan2(
                                (cursorY - centerYPx).toDouble(),
                                (cursorX - centerXPx).toDouble(),
                            )
                        ).toFloat()
                        // atan2 returns angle from +X axis. The handle starts
                        // *above* the element (negative Y), so the "natural"
                        // zero needs a +90° offset to match our rotation
                        // convention (clockwise from up = 0).
                        var deg = ang + 90f
                        // Normalise to [-180, 180].
                        while (deg > 180f) deg -= 360f
                        while (deg < -180f) deg += 360f
                        // Snap to common angles within 4°.
                        val snaps = listOf(-180f, -135f, -90f, -45f, 0f, 45f, 90f, 135f, 180f)
                        val snapped = snaps.firstOrNull { kotlin.math.abs(it - deg) < 4f } ?: deg
                        onRotate(snapped)
                    },
                )
            },
    )
}

@Composable
private fun ResizeHandle(
    elementId: String,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    size: androidx.compose.ui.unit.Dp,
    startX: Float,
    startY: Float,
    startW: Float,
    startH: Float,
    dragW: Int,
    dragH: Int,
    parentWidthPx: Float,
    parentHeightPx: Float,
    onResize: (x: Float, y: Float, width: Float, height: Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val t = LL.tokens
    val safeStart = offsetX.coerceAtLeast(0.dp)
    val safeTop = offsetY.coerceAtLeast(0.dp)
    // Use offset() not padding(): padding affects layout and the Box's
    // hit-test region; offset moves the rendered position without
    // claiming a larger layout area. Critical for handles to sit on top
    // of the element underneath and actually receive touches.
    Box(
        modifier = Modifier
            .offset(x = safeStart, y = safeTop)
            .size(size)
            .clip(CircleShape)
            .background(t.surface)
            .border(2.dp, t.accent500, CircleShape)
            // Key by element ID so the gesture handler rebuilds when a new
            // element is selected. Without this, the captured `startX` etc.
            // stay frozen at the values from the first composition and
            // resize does nothing.
            // IMPORTANT: key only on elementId, not on geometry. Keying on
            // start{X,Y,W,H} would restart the pointer handler mid-drag
            // (because the element's geometry IS changing as we resize),
            // resetting the accumulator and freezing the gesture. The
            // start* params are captured from the first composition of
            // this handle for the current selected element — exactly what
            // we want to anchor against.
            .pointerInput(elementId) {
                var dxAccum = 0f
                var dyAccum = 0f
                // Snapshot start geometry at the time the gesture begins.
                // The composable's start{X,Y,W,H} params will be stale by
                // the time onDrag fires (because we're updating the
                // element's geometry on each tick), so we capture once.
                var sx = 0f
                var sy = 0f
                var sw = 0f
                var sh = 0f
                detectDragGestures(
                    onDragStart = {
                        dxAccum = 0f
                        dyAccum = 0f
                        sx = startX
                        sy = startY
                        sw = startW
                        sh = startH
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                    onDrag = { change, drag ->
                        change.consume()
                        dxAccum += drag.x
                        dyAccum += drag.y
                        val dxFrac = if (parentWidthPx > 0f) dxAccum / parentWidthPx else 0f
                        val dyFrac = if (parentHeightPx > 0f) dyAccum / parentHeightPx else 0f
                        var newX = sx
                        var newY = sy
                        var newW = sw
                        var newH = sh
                        when (dragW) {
                            -1 -> { newX = sx + dxFrac; newW = sw - dxFrac }
                            +1 -> { newW = sw + dxFrac }
                        }
                        when (dragH) {
                            -1 -> { newY = sy + dyFrac; newH = sh - dyFrac }
                            +1 -> { newH = sh + dyFrac }
                        }
                        onResize(newX, newY, newW, newH)
                    },
                )
            },
    )
}

@Composable
private fun FreeFormElement(
    el: OverrideElement,
    parentWidth: androidx.compose.ui.unit.Dp,
    parentHeight: androidx.compose.ui.unit.Dp,
    parentWidthPx: Float,
    parentHeightPx: Float,
    isEditing: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMove: (dxFrac: Float, dyFrac: Float) -> Unit,
    onMoveEnd: () -> Unit,
    /** Image elements only: report natural aspect (W/H) when bitmap loads. */
    onAspectKnown: (widthOverHeight: Float) -> Unit = {},
) {
    val t = LL.tokens
    val xDp = parentWidth * el.x.coerceIn(0f, 1f)
    val yDp = parentHeight * el.y.coerceIn(0f, 1f)
    val wDp = parentWidth * el.width.coerceIn(0.01f, 1f)
    val hDp = parentHeight * el.height.coerceIn(0.01f, 1f)

    val borderModifier = when {
        isSelected -> Modifier.border(3.dp, t.accent500, RoundedCornerShape(6.dp))
        isEditing -> Modifier.border(1.5.dp, t.accent500.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
        else -> Modifier
    }

    val interactionModifier = if (isEditing) {
        Modifier
            .pointerInput(el.id) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(el.id, parentWidthPx, parentHeightPx) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDragEnd = { onMoveEnd() },
                    onDragCancel = { onMoveEnd() },
                    onDrag = { change, drag ->
                        change.consume()
                        val dxFrac = if (parentWidthPx > 0f) drag.x / parentWidthPx else 0f
                        val dyFrac = if (parentHeightPx > 0f) drag.y / parentHeightPx else 0f
                        onMove(dxFrac, dyFrac)
                    },
                )
            }
    } else Modifier

    // Place via Modifier.offset (moves both rendering and hit-test for the
    // child's own bounds), then size to wDp × hDp so the hit region is just
    // the visible element. Parent is BoxWithConstraints — siblings stack;
    // later index renders on top and gets hit priority.
    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(width = wDp, height = hDp)
            .then(interactionModifier)
            .graphicsLayer { rotationZ = el.rotation }
            .then(borderModifier),
    ) {
        when (el) {
            is OverrideElement.Text -> TextElementBody(el)
            is OverrideElement.Image -> ImageElementBody(el, onAspectKnown = onAspectKnown)
            is OverrideElement.Bubble -> BubbleElementBody(el)
        }
    }
}

@Composable
private fun TextElementBody(el: OverrideElement.Text) {
    val t = LL.tokens
    val color = el.colorHex?.let { parseHexColor(it, t.ink50) } ?: t.ink50
    val weight = when (el.weight.lowercase()) {
        "medium" -> FontWeight.Medium
        "semibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "extrabold" -> FontWeight.ExtraBold
        else -> FontWeight.Normal
    }
    val align = when (el.align.lowercase()) {
        "center" -> TextAlign.Center
        "end" -> TextAlign.End
        else -> TextAlign.Start
    }
    androidx.compose.material3.Text(
        text = el.body,
        color = color,
        fontSize = el.sizeSp.sp,
        lineHeight = (el.sizeSp * 1.5f).sp,
        fontWeight = weight,
        fontStyle = if (el.italic) FontStyle.Italic else FontStyle.Normal,
        textAlign = align,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun ImageElementBody(
    el: OverrideElement.Image,
    onAspectKnown: (widthOverHeight: Float) -> Unit = {},
) {
    val bmp = loadFigureBitmap(el.asset)
    val scale = when (el.contentScale.lowercase()) {
        "crop" -> ContentScale.Crop
        "fillwidth" -> ContentScale.FillWidth
        "fillheight" -> ContentScale.FillHeight
        else -> ContentScale.Fit
    }
    // Report the natural aspect ratio once per asset so the parent can
    // shrink the bounding box to wrap the image instead of leaving big
    // empty bands of unused box around a small image.
    LaunchedEffect(el.asset, bmp) {
        if (bmp != null) {
            val w = bmp.width.toFloat()
            val h = bmp.height.toFloat()
            if (w > 0 && h > 0) onAspectKnown(w / h)
        }
    }
    // The Image / placeholder is wrapped in a Box that uses matchParentSize
    // and is NOT focusable. The outer FreeFormElement's pointerInput needs
    // to receive taps; an Image with fillMaxSize alone can claim hit
    // testing in some Compose configurations.
    Box(modifier = Modifier.fillMaxSize()) {
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = scale,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            PlaceholderImage(label = el.asset, modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun BubbleElementBody(el: OverrideElement.Bubble) {
    val t = LL.tokens
    val p = lessonPalette()
    val hue = when (el.hue.lowercase()) {
        "amber" -> p.amber
        "emerald" -> p.emerald
        "rose" -> p.rose
        "violet" -> p.violet
        "indigo" -> p.indigo
        else -> p.sky
    }
    Column(modifier = Modifier.fillMaxSize()) {
        if (el.speakerLabel != null) {
            LLText(
                el.speakerLabel.uppercase(),
                color = hue.accent,
                size = 13.sp,
                weight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(hue.surface)
                .border(1.dp, hue.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            ItalicText(
                text = "“${el.body}”",
                color = t.ink50,
                size = el.sizeSp.sp,
                lineHeight = (el.sizeSp * 1.45f).sp,
            )
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
