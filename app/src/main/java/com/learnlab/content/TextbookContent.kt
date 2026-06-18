package com.learnlab.content

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// One chapter's authored deck, as stored in assets/textbook/<chapterId>.json
@Serializable
data class ChapterDeck(
    val chapterId: String,
    val slides: List<ChapterSlide>,
)

// Loads the textbook slide decks from JSON bundled in assets/textbook/.
// Content is data, not code (CLAUDE.md rule 5): each chapter is a JSON file,
// read once at startup and cached. slidesFor returns an empty list for any
// chapter without a deck (the slideshow is then skipped).
object TextbookContent {
    private const val DIR = "textbook"

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }

    private var decks: Map<String, List<ChapterSlide>> = emptyMap()

    // Read every JSON file under assets/textbook/ into the cache. Idempotent.
    fun load(context: Context) {
        if (decks.isNotEmpty()) return
        val assets = context.assets
        val files = assets.list(DIR)?.filter { it.endsWith(".json") }.orEmpty()
        val loaded = HashMap<String, List<ChapterSlide>>(files.size)
        for (name in files) {
            try {
                val text = assets.open("$DIR/$name").bufferedReader().use { it.readText() }
                val deck = json.decodeFromString<ChapterDeck>(text)
                loaded[deck.chapterId] = deck.slides
            } catch (e: Exception) {
                // One malformed deck must never crash startup, so skip it.
                Log.e("TextbookContent", "Failed to load $DIR/$name", e)
            }
        }
        decks = loaded
    }

    fun slidesFor(chapterId: String): List<ChapterSlide> = decks[chapterId].orEmpty()
}
