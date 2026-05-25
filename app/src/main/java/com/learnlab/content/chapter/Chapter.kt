package com.learnlab.content.chapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class Chapter(
    val id: String,
    val schemaVersion: Int = 1,
    val grade: Int,
    val subject: String,
    val chapter: ChapterMeta,
    val estimatedMinutes: Int = 0,
    val learningOutcomes: List<String> = emptyList(),
    val blocks: List<ChapterBlock>,
)

@Serializable
data class ChapterMeta(
    val number: Int,
    val title: String,
    val subtitle: String? = null,
    val ncertReference: String? = null,
)

/* ─────────────────── Block union ─────────────────── */

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class ChapterBlock {

    @Serializable @SerialName("sectionHeader")
    data class SectionHeader(
        val number: String? = null,
        val title: String,
        val anchor: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("paragraph")
    data class Paragraph(val body: String) : ChapterBlock()

    @Serializable @SerialName("keyTerm")
    data class KeyTerm(val term: String, val definition: String) : ChapterBlock()

    @Serializable @SerialName("quotation")
    data class Quotation(val body: String, val attribution: String? = null) : ChapterBlock()

    @Serializable @SerialName("callout")
    data class Callout(
        val tone: String = "info",   // info | warning | success | fact
        val title: String? = null,
        val body: String,
    ) : ChapterBlock()

    @Serializable @SerialName("sanskritShloka")
    data class SanskritShloka(
        val devanagari: String,
        val transliteration: String? = null,
        val translation: String,
        val attribution: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("storyFrame")
    data class StoryFrame(
        val scene: String,
        val characters: List<String> = emptyList(),
        val imageRef: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("speechBubble")
    data class SpeechBubble(
        val speakerId: String,
        val body: String,
    ) : ChapterBlock()

    @Serializable @SerialName("figure")
    data class Figure(
        val id: String,
        val assetType: String,           // bitmap | svg | composeDraw
        val asset: String,
        val caption: String? = null,
        val altText: String? = null,
        val hotspots: List<Hotspot> = emptyList(),
        val capabilities: FigureCapabilities = FigureCapabilities(),
    ) : ChapterBlock()

    @Serializable @SerialName("figureCollage")
    data class FigureCollage(
        val id: String,
        val asset: String,
        val caption: String? = null,
        val altText: String? = null,
        val circles: List<CollageCircle> = emptyList(),
    ) : ChapterBlock()

    @Serializable @SerialName("imageWithCallout")
    data class ImageWithCallout(
        val id: String,
        val asset: String,
        val caption: String,
        val side: String = "center",     // left | center | right
    ) : ChapterBlock()

    @Serializable @SerialName("sideBySideCompare")
    data class SideBySideCompare(
        val id: String,
        val caption: String? = null,
        val leftImage: String,
        val leftLabel: String,
        val leftBlurb: String? = null,
        val rightImage: String,
        val rightLabel: String,
        val rightBlurb: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("activity")
    data class Activity(
        val experimentId: String? = null,
        val ncertReference: String? = null,
        val title: String,
        val intro: String? = null,
        val placement: String = "inline",   // inline | fullscreen
        val estimatedMinutes: Int = 0,
        val kind: String = "hybrid",        // fieldwork | classroom | hybrid
        val tables: List<TableSpec> = emptyList(),
    ) : ChapterBlock()

    @Serializable @SerialName("tableBlock")
    data class TableBlock(
        val caption: String? = null,
        val headers: List<String>,
        val rows: List<List<String>>,
    ) : ChapterBlock()

    @Serializable @SerialName("workedExample")
    data class WorkedExample(
        val title: String,
        val steps: List<String>,
    ) : ChapterBlock()

    @Serializable @SerialName("knowScientist")
    data class KnowScientist(
        val id: String,
        val name: String,
        val dates: String? = null,
        val portraitAsset: String? = null,
        val biography: String,
        val fields: List<String> = emptyList(),
    ) : ChapterBlock()

    @Serializable @SerialName("successStory")
    data class SuccessStory(
        val id: String,
        val title: String,
        val imageAsset: String? = null,
        val body: String,
        val highlight: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("doYouKnow")
    data class DoYouKnow(
        val id: String,
        val title: String,
        val body: String,
        val images: List<CaptionedImage> = emptyList(),
    ) : ChapterBlock()

    @Serializable @SerialName("moreToKnow")
    data class MoreToKnow(
        val id: String,
        val title: String,
        val body: String,
        val image: String? = null,
        val imageCaption: String? = null,
    ) : ChapterBlock()

    @Serializable @SerialName("keywordCloud")
    data class KeywordCloud(
        val title: String = "Keywords",
        val terms: List<String>,
    ) : ChapterBlock()

    @Serializable @SerialName("summary")
    data class Summary(
        val title: String = "Summary",
        val points: List<String>,
    ) : ChapterBlock()

    @Serializable @SerialName("exercise")
    data class Exercise(
        val id: String,
        val number: String,
        val engine: String,                 // venn | flowchart | imageCompare | mcq | freeText
        val prompt: String,
        val config: kotlinx.serialization.json.JsonObject,
    ) : ChapterBlock()

    @Serializable @SerialName("learningFurther")
    data class LearningFurther(
        val title: String = "Learning further",
        val projects: List<ProjectItem>,
    ) : ChapterBlock()
}

/* ─────────────────── Shared sub-types ─────────────────── */

@Serializable
data class Hotspot(
    val id: String,
    val x: Float,
    val y: Float,
    val label: String,
    val blurb: String? = null,
)

@Serializable
data class CollageCircle(
    val id: String,
    val x: Float,
    val y: Float,
    val label: String,
    val moves: String? = null,
    val parts: String? = null,
)

@Serializable
data class FigureCapabilities(
    val zoom: Boolean = false,
    val hotspots: Boolean = true,
    val quiz: Boolean = false,
    val animation: Boolean = false,
    val teacherMode: Boolean = false,
    val expandFullscreen: Boolean = false,
    val compareMode: CompareMode? = null,
)

@Serializable
data class CompareMode(val leftId: String, val rightId: String)

@Serializable
data class TableSpec(
    val caption: String? = null,
    val headers: List<String>,
    val exampleRows: List<List<String>> = emptyList(),
)

@Serializable
data class CaptionedImage(val asset: String, val caption: String? = null)

@Serializable
data class ProjectItem(val id: String, val body: String)
