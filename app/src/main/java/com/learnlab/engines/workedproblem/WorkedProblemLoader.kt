package com.learnlab.engines.workedproblem

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads a WorkedProblemConfig from JSON in assets.
 *
 * Asset paths follow the same convention as ChapterLoader: Gradle copies
 * `content-json/` into `app/src/main/assets/` at build time (see
 * `app/build.gradle.kts` sourceSets), so a JSON at
 *     content-json/grade-9/science/ch04/projectile-challenge.json
 * is reachable at
 *     context.assets.open("grade-9/science/ch04/projectile-challenge.json")
 *
 * Cached per-app-run; configs are immutable.
 */
object WorkedProblemLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cache = mutableMapOf<String, WorkedProblemConfig>()

    fun load(context: Context, assetPath: String): WorkedProblemConfig {
        cache[assetPath]?.let { return it }
        val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(WorkedProblemConfig.serializer(), text)
        cache[assetPath] = parsed
        return parsed
    }
}

/** Known worked-problem asset paths. Extend as more are authored. */
object WorkedProblemPaths {
    const val G9_SCI_CH04_PROJECTILE_CHALLENGE =
        "grade-9/science/ch04/projectile-challenge.json"
}
