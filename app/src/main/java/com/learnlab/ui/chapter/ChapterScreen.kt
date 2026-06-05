package com.learnlab.ui.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.learnlab.content.chapter.Chapter
import com.learnlab.content.chapter.ChapterLoader
import com.learnlab.content.chapter.ChapterPaths
import com.learnlab.content.chapter.Character
import com.learnlab.content.chapter.Slide
import com.learnlab.content.chapter.SlideOverride
import com.learnlab.data.sync.SlideOverridesRepository
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.ProvideTokens
import com.learnlab.lessons.slides.EditHistory
import com.learnlab.lessons.slides.OverrideSynthesizer
import com.learnlab.lessons.slides.SlidePlanner
import com.learnlab.lessons.slides.SlideStage
import com.learnlab.store.AppState

@Composable
fun ChapterScreen(
    state: AppState,
    chapterId: String,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
) {
    var isDark by remember { mutableStateOf(false) }
    var isEditing by remember(chapterId) { mutableStateOf(false) }
    ProvideTokens(isDark = isDark) {
        ChapterScreenContent(
            chapterId = chapterId,
            onBack = onBack,
            onOpenActivity = onOpenActivity,
            isDark = isDark,
            onToggleTheme = { isDark = !isDark },
            isEditing = isEditing,
            onToggleEdit = { isEditing = !isEditing },
        )
    }
}

@Composable
private fun ChapterScreenContent(
    chapterId: String,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
) {
    val t = LL.tokens
    val ctx = LocalContext.current
    var chapter by remember(chapterId) { mutableStateOf<Chapter?>(null) }
    var cast by remember { mutableStateOf<List<Character>>(emptyList()) }
    var error by remember(chapterId) { mutableStateOf<String?>(null) }
    // Overrides fetched from Supabase. Re-fetched on chapter open.
    var remoteOverrides by remember(chapterId) { mutableStateOf<Map<String, SlideOverride>>(emptyMap()) }
    // Overrides synthesized locally when the author taps pencil on an
    // auto-layout slide. These live in memory until step 9 (persistence)
    // wires them back to Supabase.
    var localOverrides by remember(chapterId) { mutableStateOf<Map<String, SlideOverride>>(emptyMap()) }
    // Undo/redo stack — committed (gesture-end) snapshots of localOverrides.
    val history = remember(chapterId) { EditHistory() }
    var canUndo by remember(chapterId) { mutableStateOf(false) }
    var canRedo by remember(chapterId) { mutableStateOf(false) }
    fun refreshHistoryFlags() {
        canUndo = history.canUndo
        canRedo = history.canRedo
    }

    // Debounced save-to-Supabase. One Job per slide ID; rapid commits replace
    // the pending job so we send one network call per ~500ms quiet period
    // instead of one per drag-end. Burst of resize handles → one save.
    val saveScope = rememberCoroutineScope()
    val pendingSaves = remember(chapterId) { mutableMapOf<String, Job>() }
    fun scheduleSave(slideId: String, override: SlideOverride) {
        pendingSaves[slideId]?.cancel()
        pendingSaves[slideId] = saveScope.launch {
            delay(500L)
            SlideOverridesRepository.save(chapterId, slideId, override)
        }
    }

    LaunchedEffect(chapterId) {
        try {
            val path = chapterPathFor(chapterId)
                ?: throw IllegalArgumentException("No chapter path mapped for id '$chapterId'")
            val loaded = ChapterLoader.loadChapter(ctx, path)
            chapter = loaded
            cast = ChapterLoader.loadCast(ctx).characters
            remoteOverrides = SlideOverridesRepository.fetchForChapter(chapterId)
        } catch (e: Throwable) {
            error = e.message ?: e::class.simpleName
        }
    }

    // Re-plan only when the set of overridden slide IDs changes — the heavy
    // walk of every block in the chapter happens once per "new slide gets an
    // override", not once per drag tick. Live edits then mutate the override
    // field of the already-planned slides in-place, which is O(1).
    val localKeys = localOverrides.keys
    val remoteKeys = remoteOverrides.keys
    val baseSlides: List<Slide> = remember(chapter, remoteKeys, localKeys) {
        val ch = chapter ?: return@remember emptyList<Slide>()
        // Use placeholder empty overrides so the planner flips layouts for
        // all the right IDs; we'll fill the real elements below.
        val placeholderMap: Map<String, SlideOverride> =
            (remoteKeys + localKeys).associateWith { com.learnlab.content.chapter.SlideOverride() }
        SlidePlanner.plan(ch, placeholderMap)
    }
    // Fast pass: for slides marked FreeForm, attach the current override
    // (local wins over remote). Runs on every recomposition but is O(slides).
    val slides: List<Slide> = remember(baseSlides, remoteOverrides, localOverrides) {
        baseSlides.map { slide ->
            val ov = localOverrides[slide.id] ?: remoteOverrides[slide.id]
            if (ov != null) slide.copy(override = ov) else slide
        }
    }

    val pal = com.learnlab.lessons.lessonPalette()
    Box(modifier = Modifier.fillMaxSize().background(pal.pageBgBrush)) {
        when {
            error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LLText("Couldn't load chapter", color = t.ink50, size = 18.sp)
                LLText(error ?: "", color = t.ink400, size = 12.sp)
            }
            chapter == null || slides.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LLText("Preparing slides…", color = t.ink500, size = 14.sp)
            }
            else -> {
                val castMap = cast.associateBy { it.id }
                SlideStage(
                    chapter = chapter!!,
                    slides = slides,
                    cast = castMap,
                    onBack = onBack,
                    onOpenActivity = onOpenActivity,
                    isDark = isDark,
                    onToggleTheme = onToggleTheme,
                    isEditing = isEditing,
                    onToggleEdit = onToggleEdit,
                    onRequestSynthesizeOverride = { slide ->
                        // Only synthesize once per slide. Subsequent edits
                        // mutate the existing override (steps 4-9).
                        if (slide.id !in localOverrides && slide.id !in remoteOverrides) {
                            val synth = OverrideSynthesizer.synthesize(slide, castMap)
                            if (synth != null) {
                                localOverrides = localOverrides + (slide.id to synth)
                            }
                        }
                    },
                    onOverrideEdited = { slideId, updated ->
                        // Live edit — every drag tick updates localOverrides
                        // so the screen follows the finger. NOT recorded in
                        // history; only the committed state at gesture-end is.
                        localOverrides = localOverrides + (slideId to updated)
                    },
                    onOverrideCommitted = { slideId, updated ->
                        val next = localOverrides + (slideId to updated)
                        history.pushIfDifferent(next)
                        localOverrides = next
                        refreshHistoryFlags()
                        scheduleSave(slideId, updated)
                    },
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = {
                        history.undo()?.let { restored ->
                            // Diff against current to find which slides
                            // changed, then schedule saves for each.
                            (localOverrides.keys + restored.keys).forEach { id ->
                                val before = localOverrides[id]
                                val after = restored[id]
                                if (before != after && after != null) {
                                    scheduleSave(id, after)
                                }
                                // If a slide was undone back to "no override",
                                // we don't currently delete the remote row.
                                // That's a future polish.
                            }
                            localOverrides = restored
                        }
                        refreshHistoryFlags()
                    },
                    onRedo = {
                        history.redo()?.let { restored ->
                            (localOverrides.keys + restored.keys).forEach { id ->
                                val before = localOverrides[id]
                                val after = restored[id]
                                if (before != after && after != null) {
                                    scheduleSave(id, after)
                                }
                            }
                            localOverrides = restored
                        }
                        refreshHistoryFlags()
                    },
                )
            }
        }
    }
}

private fun chapterPathFor(chapterId: String): String? = when (chapterId) {
    "g6-sci-ch02" -> ChapterPaths.G6_SCIENCE_CH02
    else -> null
}
