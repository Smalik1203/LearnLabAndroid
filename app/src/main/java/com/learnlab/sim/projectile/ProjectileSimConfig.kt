package com.learnlab.sim.projectile

import kotlinx.serialization.Serializable

/**
 * Data config for the projectile-motion reference template, parsed from the
 * activity JSON's `config` block (schema §5). Pure data — no behaviour — so the
 * same engine renders any projectile activity by changing these numbers.
 *
 * Ties to the existing `projectile-challenge` content on this branch.
 */
@Serializable
data class ProjectileSimConfig(
    val initialSpeedMs: Float = 12f,
    val initialAngleDeg: Float = 45f,
    val gravity: Float = 9.8f,
    val minSpeedMs: Float = 2f,
    val maxSpeedMs: Float = 25f,
    val minAngleDeg: Float = 5f,
    val maxAngleDeg: Float = 85f,
) {
    companion object {
        /** Command codes shared between the Compose overlay and the scene.
         *  Plain ints so nothing is allocated crossing the thread boundary. */
        const val CMD_LAUNCH = 1
        const val CMD_RESET = 2
        const val CMD_SET_ANGLE = 3   // a = degrees
        const val CMD_SET_SPEED = 4   // a = m/s
    }
}
