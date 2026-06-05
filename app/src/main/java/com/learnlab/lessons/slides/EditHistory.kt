package com.learnlab.lessons.slides

import androidx.compose.runtime.Stable
import com.learnlab.content.chapter.SlideOverride

/**
 * Undo/redo stack for the slide editor.
 *
 * Tracks the whole [localOverrides] map (a snapshot per "edit step") rather
 * than per-element diffs. Memory cost: one Map per step; we cap at 50 steps.
 *
 * What counts as a step:
 *  - the end of a drag (one push per drag end, not per drag tick)
 *  - resize handle release
 *  - rotate handle release
 *  - delete / add / z-order op
 *
 * The editor calls [pushIfDifferent] when a drag/resize/rotate ends. Live
 * drag ticks update the live state directly; only the *committed* value
 * lands in history.
 */
@Stable
class EditHistory {
    private val past = ArrayDeque<Map<String, SlideOverride>>()
    private val future = ArrayDeque<Map<String, SlideOverride>>()
    private var current: Map<String, SlideOverride> = emptyMap()

    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()

    /** Replace the current state without recording history (e.g. initial load). */
    fun reset(state: Map<String, SlideOverride>) {
        past.clear()
        future.clear()
        current = state
    }

    /** Push the previous [current] onto the past stack and adopt [next] as current.
     *  No-op if [next] equals [current]. Clears the redo stack. */
    fun pushIfDifferent(next: Map<String, SlideOverride>) {
        if (next == current) return
        past.addLast(current)
        if (past.size > MAX_DEPTH) past.removeFirst()
        future.clear()
        current = next
    }

    /** Returns the previous state, or null if none. */
    fun undo(): Map<String, SlideOverride>? {
        if (past.isEmpty()) return null
        future.addLast(current)
        current = past.removeLast()
        return current
    }

    /** Returns the next state, or null if none. */
    fun redo(): Map<String, SlideOverride>? {
        if (future.isEmpty()) return null
        past.addLast(current)
        current = future.removeLast()
        return current
    }

    companion object {
        const val MAX_DEPTH = 50
    }
}
