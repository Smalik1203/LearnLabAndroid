package com.learnlab.sim.projectile

import android.content.res.AssetManager
import com.google.android.filament.EntityManager
import com.learnlab.render.MeshFactory
import com.learnlab.render.SimContext
import com.learnlab.render.SimInput
import com.learnlab.render.SimScene
import com.learnlab.render.TrailMesh
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reference template: projectile motion. Demonstrates the full scene contract
 * end-to-end — GL resource creation, allocation-free per-frame physics, discrete
 * commands from the UI, and progress reporting — while staying inside the frame
 * budget on entry hardware.
 *
 * Visuals are deliberately minimal and flat-shaded: a reference grid, one
 * projectile sphere, and a live predicted-trajectory trail that redraws as the
 * teacher changes angle/speed. That predictive arc is the teaching moment, and
 * it costs one constant-size line-strip draw call (see [TrailMesh]).
 */
class ProjectileSimScene(
    private val assets: AssetManager,
    private val config: ProjectileSimConfig,
) : SimScene {

    private var sphereEntity = 0
    private var sphereInstance = 0
    private var gridEntity = 0
    private lateinit var trail: TrailMesh

    // Live, mutable sim state — all primitives, mutated only on the render thread.
    private var angleDeg = config.initialAngleDeg
    private var speed = config.initialSpeedMs
    private var t = 0f
    private var launched = false
    private var completedOnce = false

    // Preallocated arc buffer reused every frame for the predicted trajectory.
    private val arc = FloatArray(ARC_SAMPLES * 3)

    override fun onSurfaceCreated(ctx: SimContext) {
        // May throw if the compiled material is missing -> host shows error state.
        val mat = MeshFactory.loadUnlitMaterial(ctx.engine, assets, MATERIAL_PATH)

        val em = EntityManager.get()

        gridEntity = em.create()
        MeshFactory.grid(
            ctx.engine, gridEntity,
            MeshFactory.instance(mat, 0.28f, 0.34f, 0.45f, 1f),  // dim slate
            halfSize = 10f, divisions = 20,
        )
        ctx.scene.addEntity(gridEntity)

        sphereEntity = em.create()
        MeshFactory.sphere(
            ctx.engine, sphereEntity,
            MeshFactory.instance(mat, 0.98f, 0.57f, 0.24f, 1f),  // orange == gravity
            radius = 0.45f,
        )
        // Give the sphere a transform component so we can move it each frame.
        ctx.transformManager.create(sphereEntity)
        sphereInstance = ctx.transformManager.getInstance(sphereEntity)
        ctx.scene.addEntity(sphereEntity)

        val trailEntity = em.create()
        trail = TrailMesh(
            ctx.engine, trailEntity,
            MeshFactory.instance(mat, 0.52f, 0.8f, 0.09f, 1f),   // lime == horizontal
            maxPoints = ARC_SAMPLES,
        )
        ctx.scene.addEntity(trail.entity)
    }

    override fun onConfigure(ctx: SimContext) {
        angleDeg = config.initialAngleDeg
        speed = config.initialSpeedMs
        resetFlight(ctx)
        rebuildArc(ctx)
    }

    override fun update(ctx: SimContext, dtSeconds: Float, input: SimInput) {
        if (launched) {
            t += dtSeconds
            val rad = Math.toRadians(angleDeg.toDouble())
            val vx = speed * cos(rad).toFloat()
            val vy = speed * sin(rad).toFloat()
            val x = vx * t
            val y = vy * t - 0.5f * config.gravity * t * t
            if (y <= 0f && t > 0f) {
                // Landed: pin to ground, stop, mark the activity progressed.
                ctx.setTranslationScale(sphereInstance, x, 0f, 0f, 1f)
                launched = false
                completedOnce = true
            } else {
                ctx.setTranslationScale(sphereInstance, x, y, 0f, 1f)
            }
        }
        // Redraw the predicted arc every frame so angle/speed edits show live.
        // Cheap: fills a preallocated array + one buffer upload, no allocation.
        rebuildArc(ctx)
    }

    override fun onCommand(ctx: SimContext, code: Int, a: Float, b: Float) {
        when (code) {
            ProjectileSimConfig.CMD_LAUNCH -> if (!launched) { t = 0f; launched = true }
            ProjectileSimConfig.CMD_RESET -> resetFlight(ctx)
            ProjectileSimConfig.CMD_SET_ANGLE ->
                angleDeg = a.coerceIn(config.minAngleDeg, config.maxAngleDeg)
            ProjectileSimConfig.CMD_SET_SPEED ->
                speed = a.coerceIn(config.minSpeedMs, config.maxSpeedMs)
        }
    }

    override fun onResized(ctx: SimContext, width: Int, height: Int) { /* aspect handled by rig */ }

    override fun progress(): Float = when {
        completedOnce -> 1f
        launched -> 0.5f
        else -> 0f
    }

    override fun onDestroy(ctx: SimContext) {
        // Entities are destroyed with the Engine by the render thread; nothing
        // scene-private to free here beyond what the Engine owns.
    }

    private fun resetFlight(ctx: SimContext) {
        launched = false
        t = 0f
        if (sphereInstance != 0) ctx.setTranslationScale(sphereInstance, 0f, 0f, 0f, 1f)
    }

    /** Samples the parabola into [arc] and uploads it to the trail. The number
     *  of samples is capped by the active quality tier's detail budget so a LOW
     *  device draws a coarser (still smooth-looking) arc. Allocation-free. */
    private fun rebuildArc(ctx: SimContext) {
        val samples = ARC_SAMPLES.coerceAtMost(ctx.quality().detailBudget)
        val rad = Math.toRadians(angleDeg.toDouble())
        val vx = speed * cos(rad).toFloat()
        val vy = speed * sin(rad).toFloat()
        // Total flight time from ground to ground: 2*vy/g.
        val flight = (2f * vy / config.gravity).coerceAtLeast(0.01f)
        var w = 0
        for (i in 0 until samples) {
            val ti = flight * i / (samples - 1)
            arc[w++] = vx * ti
            arc[w++] = (vy * ti - 0.5f * config.gravity * ti * ti).coerceAtLeast(0f)
            arc[w++] = 0f
        }
        trail.update(arc, samples)
    }

    private companion object {
        const val MATERIAL_PATH = "materials/unlit.filamat"
        const val ARC_SAMPLES = 48
    }
}
