package com.learnlab.design

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * On-device text-to-speech, offline. Wraps Android's [TextToSpeech] so any
 * engine can read its on-screen text aloud without touching the platform API
 * directly — see [SpeakControls] for the drop-in UI.
 *
 * Why this lives in core/design: it's a reusable UI capability in the same
 * category as [Slider] / [PrimaryButton] — every engine reaches it via the
 * `com.learnlab.design` import they already have. It needs an Android Context,
 * which it pulls from the composition (LocalContext), so callers pass only text.
 *
 * Pause/resume note: Android TTS cannot truly pause mid-utterance. We track
 * word boundaries via [UtteranceProgressListener.onRangeStart] and, on resume,
 * re-speak from the remaining text. That boundary tracking is also the
 * groundwork for word-level highlighting (a planned follow-up) — `spokenCharIndex`
 * is already exposed for it.
 */
class ReadAloudController internal constructor(
    private val tts: TextToSpeech,
) {
    /** True while an utterance is actively being spoken (not paused, not idle). */
    var isSpeaking by mutableStateOf(false)
        internal set

    /** True when narration has been paused and can be resumed. */
    var isPaused by mutableStateOf(false)
        internal set

    /**
     * Character offset (into [currentText]) of the word currently being
     * spoken, or -1 when idle. Exposed for the highlight follow-up; the
     * resume logic also uses it as the restart point.
     */
    var spokenCharIndex by mutableIntStateOf(-1)
        internal set

    /** The full text of the active utterance, for resume + future highlight. */
    var currentText by mutableStateOf("")
        internal set

    internal var ready = false

    /** Speak [text] from the beginning, replacing anything in progress. */
    fun restart(text: String) {
        if (!ready || text.isBlank()) return
        currentText = text
        speakFrom(0)
    }

    /**
     * If paused, resume from the last spoken word. If actively speaking,
     * pause (TTS stops; state remembers where to pick up). If idle, no-op.
     */
    fun togglePause() {
        if (!ready) return
        when {
            isSpeaking -> {
                // Snapshot the resume point before stopping clears it.
                tts.stop()
                isSpeaking = false
                isPaused = true
            }
            isPaused -> {
                val from = spokenCharIndex.coerceAtLeast(0)
                speakFrom(from)
            }
        }
    }

    /** Fully stop and clear state. Called on step change and on dispose. */
    fun stop() {
        if (ready) tts.stop()
        isSpeaking = false
        isPaused = false
        spokenCharIndex = -1
    }

    private fun speakFrom(charOffset: Int) {
        val remaining = currentText.substring(charOffset.coerceIn(0, currentText.length))
        if (remaining.isBlank()) {
            stop()
            return
        }
        // Stash the offset so onRangeStart can report indices relative to the
        // full text, not the sliced remainder.
        utteranceBaseOffset = charOffset
        isPaused = false
        isSpeaking = true
        spokenCharIndex = charOffset
        tts.speak(remaining, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    internal var utteranceBaseOffset = 0

    companion object {
        internal const val UTTERANCE_ID = "ll-read-aloud"
    }
}

/**
 * Remembers a [ReadAloudController] tied to the current composition. Initializes
 * TextToSpeech (prefers en-IN, falls back to default locale) and shuts it down
 * on dispose so the engine is never leaked.
 */
@Composable
fun rememberReadAloud(): ReadAloudController {
    val context = LocalContext.current
    val controller = remember {
        // tts is assigned synchronously below; the listener only fires after
        // init completes, by which point `ready` is true.
        lateinit var c: ReadAloudController
        val engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) c.ready = true
        }
        c = ReadAloudController(engine)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                // start is relative to the spoken slice; offset back to full text.
                c.spokenCharIndex = c.utteranceBaseOffset + start
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == ReadAloudController.UTTERANCE_ID) {
                    c.isSpeaking = false
                    c.isPaused = false
                    c.spokenCharIndex = -1
                }
            }

            @Deprecated("Required override", ReplaceWith(""))
            override fun onError(utteranceId: String?) {
                c.isSpeaking = false
                c.isPaused = false
            }
        })
        // en-IN reads NCERT terms more naturally than en-US; fall back silently
        // if the device doesn't ship it.
        val locale = Locale("en", "IN")
        engine.setLanguage(locale)

        // A touch slower than default and neutral pitch — the stock rate
        // sounds rushed and even more robotic when read to a class.
        engine.setSpeechRate(0.92f)
        engine.setPitch(1.0f)

        // Default voice is often the lowest-quality one the engine ships.
        // Pick the highest-quality voice for this language that doesn't
        // require the network (offline-first), preferring non-"network"
        // voices with the best declared quality.
        runCatching {
            engine.voices
                ?.filter { v ->
                    v.locale.language == locale.language &&
                        !v.isNetworkConnectionRequired &&
                        !v.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                }
                ?.maxByOrNull { it.quality }
                ?.let { engine.voice = it }
        }
        c
    }

    DisposableEffect(Unit) {
        onDispose { controller.stop() }
    }

    return controller
}

/**
 * Drop-in read-aloud control for any engine. Renders a Restart button and a
 * Pause/Resume button wired to [controller]. Pass the [text] the engine is
 * currently showing; Restart speaks it from the top, Pause/Resume toggles.
 *
 * The engine is responsible for calling `controller.stop()` when the on-screen
 * text changes (e.g. step navigation) — this control does not watch for that.
 */
@Composable
fun SpeakControls(
    text: String,
    controller: ReadAloudController,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeakIconButton(
            label = if (controller.isSpeaking || controller.isPaused) "↻ Restart" else "▶ Read aloud",
            primary = !controller.isSpeaking && !controller.isPaused,
            onClick = { controller.restart(text) },
        )
        if (controller.isSpeaking || controller.isPaused) {
            Box(Modifier.padding(start = 8.dp)) {
                SpeakIconButton(
                    label = if (controller.isPaused) "▶ Resume" else "⏸ Pause",
                    primary = false,
                    onClick = { controller.togglePause() },
                )
            }
        }
    }
}

/** Small pill button matching the design system, used by [SpeakControls]. */
@Composable
private fun SpeakIconButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (primary) t.accent50 else t.surface2)
            .border(1.dp, if (primary) t.accent300 else t.lineStrong, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(
            label,
            color = if (primary) t.accent700 else t.ink200,
            size = 12.sp,
            weight = FontWeight.SemiBold,
        )
    }
}
