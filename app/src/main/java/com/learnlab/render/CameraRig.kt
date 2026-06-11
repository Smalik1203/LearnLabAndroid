package com.learnlab.render

import com.google.android.filament.Camera
import kotlin.math.cos
import kotlin.math.sin

/**
 * Orbit camera shared by every simulation. The teacher drags to rotate, pinches
 * to zoom; the framework owns this so individual scenes never reimplement camera
 * control (and never get it subtly wrong).
 *
 * Spherical coordinates around a fixed target. All state is primitive fields;
 * [apply] does a handful of trig and one allocation-free [Camera.lookAt]. No
 * objects created per frame.
 */
class CameraRig(
    private val targetX: Float = 0f,
    private val targetY: Float = 1f,
    private val targetZ: Float = 0f,
) {
    private var yaw = 0.6f          // radians, around Y
    private var pitch = 0.5f        // radians, above horizon
    private var distance = 9f

    // Smoothing toward target values so a flung drag eases instead of snapping —
    // cheap critically-damped lerp, no springs/animators (those allocate).
    private var yawTarget = yaw
    private var pitchTarget = pitch
    private var distTarget = distance

    fun applyInput(input: SimInput) {
        yawTarget -= input.dragYaw
        pitchTarget = (pitchTarget - input.dragPitch).coerceIn(MIN_PITCH, MAX_PITCH)
        if (input.pinchScale != 1f) {
            distTarget = (distTarget / input.pinchScale).coerceIn(MIN_DIST, MAX_DIST)
        }
    }

    /** Advance smoothing and push the view matrix to the camera. Allocation-free. */
    fun apply(camera: Camera) {
        yaw += (yawTarget - yaw) * SMOOTH
        pitch += (pitchTarget - pitch) * SMOOTH
        distance += (distTarget - distance) * SMOOTH

        val cosP = cos(pitch)
        val eyeX = targetX + distance * cosP * sin(yaw)
        val eyeY = targetY + distance * sin(pitch)
        val eyeZ = targetZ + distance * cosP * cos(yaw)

        // lookAt takes doubles by value — no array allocation.
        camera.lookAt(
            eyeX.toDouble(), eyeY.toDouble(), eyeZ.toDouble(),
            targetX.toDouble(), targetY.toDouble(), targetZ.toDouble(),
            0.0, 1.0, 0.0,
        )
    }

    fun setProjection(camera: Camera, width: Int, height: Int) {
        val aspect = if (height == 0) 1.0 else width.toDouble() / height.toDouble()
        // 45° vertical FOV reads naturally for a tabletop sim; near/far kept tight
        // so the depth buffer has precision on a 16-bit-depth entry GPU.
        camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL)
    }

    private companion object {
        const val MIN_PITCH = 0.15f
        const val MAX_PITCH = 1.45f
        const val MIN_DIST = 3f
        const val MAX_DIST = 22f
        const val SMOOTH = 0.18f
    }
}
