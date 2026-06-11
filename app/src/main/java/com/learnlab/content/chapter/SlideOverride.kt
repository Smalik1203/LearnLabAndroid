package com.learnlab.content.chapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A per-slide layout override authored in the in-app editor.
 *
 * When a slide has an override, the renderer ignores the auto-layout the
 * SlidePlanner would have chosen for the underlying blocks and instead
 * draws a free-form canvas of [OverrideElement]s at the positions saved
 * by the editor.
 *
 * Coordinates are fractions of the slide area (0..1) so the same override
 * renders identically across screen sizes and orientations.
 */
@Serializable
data class SlideOverride(
    val elements: List<OverrideElement> = emptyList(),
    /** Optional card background tint; null = use the chapter palette default. */
    val backgroundHex: String? = null,
)

/**
 * One placed element on a free-form slide.
 *
 * Position + size are fractions of the parent slide area. Rotation is in
 * degrees (clockwise). Editor toolbars write these fields; the renderer
 * reads them.
 */
@Serializable
sealed class OverrideElement {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
    abstract val rotation: Float

    @Serializable
    @SerialName("text")
    data class Text(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val rotation: Float = 0f,
        val body: String,
        val sizeSp: Float = 22f,
        val colorHex: String? = null,
        val weight: String = "normal", // normal | medium | semibold | bold | extrabold
        val italic: Boolean = false,
        val align: String = "start",   // start | center | end
    ) : OverrideElement()

    @Serializable
    @SerialName("image")
    data class Image(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val rotation: Float = 0f,
        /** Asset path relative to assets/figures/ — e.g. "cast/dadi-leela.png". */
        val asset: String,
        val contentScale: String = "fit", // fit | crop | fillWidth | fillHeight
    ) : OverrideElement()

    @Serializable
    @SerialName("bubble")
    data class Bubble(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val rotation: Float = 0f,
        val body: String,
        val sizeSp: Float = 16f,
        /** Optional speaker label shown above the bubble (uppercase). */
        val speakerLabel: String? = null,
        /** Palette hue: sky | amber | emerald | rose | violet | indigo. */
        val hue: String = "sky",
    ) : OverrideElement()

    /** Returns a copy of this element with new x/y (clamped to 0..1). */
    fun withPosition(newX: Float, newY: Float): OverrideElement {
        val cx = newX.coerceIn(0f, 1f - width.coerceAtMost(1f))
        val cy = newY.coerceIn(0f, 1f - height.coerceAtMost(1f))
        return when (this) {
            is Text -> copy(x = cx, y = cy)
            is Image -> copy(x = cx, y = cy)
            is Bubble -> copy(x = cx, y = cy)
        }
    }

    /** Returns a copy with a new rotation in degrees. */
    fun withRotation(newRotation: Float): OverrideElement = when (this) {
        is Text -> copy(rotation = newRotation)
        is Image -> copy(rotation = newRotation)
        is Bubble -> copy(rotation = newRotation)
    }

    /** Returns a copy with a new geometry (x, y, width, height). All clamped so
     *  the element stays inside 0..1 and has a minimum size of 0.02. */
    fun withGeometry(newX: Float, newY: Float, newWidth: Float, newHeight: Float): OverrideElement {
        val w = newWidth.coerceIn(MIN_DIM, 1f)
        val h = newHeight.coerceIn(MIN_DIM, 1f)
        val cx = newX.coerceIn(0f, 1f - w)
        val cy = newY.coerceIn(0f, 1f - h)
        return when (this) {
            is Text -> copy(x = cx, y = cy, width = w, height = h)
            is Image -> copy(x = cx, y = cy, width = w, height = h)
            is Bubble -> copy(x = cx, y = cy, width = w, height = h)
        }
    }

    companion object {
        /** Minimum size for any element, in slide fractions. */
        const val MIN_DIM: Float = 0.02f
    }
}
