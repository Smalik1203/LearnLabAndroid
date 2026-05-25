package com.learnlab.content.chapter

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads chapters and cast from JSON in assets. JSON files are copied into
 * assets/ from content-json/ by Gradle (see app/build.gradle.kts sourceSets).
 *
 * Caches in memory after first read — chapters are immutable per app run.
 */
object ChapterLoader {

    private val json = Json {
        ignoreUnknownKeys = true       // pilot files include $schema, _notes etc.
        isLenient = true
        classDiscriminator = "type"
    }

    private val chapterCache = mutableMapOf<String, Chapter>()
    private var castCache: CastFile? = null

    fun loadChapter(context: Context, path: String): Chapter {
        chapterCache[path]?.let { return it }
        val text = context.assets.open(path).bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(Chapter.serializer(), text)
        chapterCache[path] = parsed
        return parsed
    }

    fun loadCast(context: Context): CastFile {
        castCache?.let { return it }
        val text = context.assets.open("cast.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(CastFile.serializer(), text)
        castCache = parsed
        return parsed
    }

    fun characterById(context: Context, id: String): Character? =
        loadCast(context).characters.firstOrNull { it.id == id }
}

/** Known chapter paths in assets — extend as more chapters are authored. */
object ChapterPaths {
    const val G6_SCIENCE_CH02 = "grade-6/science/ch02-diversity-living-world.json"
}
