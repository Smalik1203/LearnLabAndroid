package com.learnlab.render

import android.content.res.AssetManager
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.Entity
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural geometry for the reference templates. We generate meshes in code
 * (not glTF) for the primitive shapes a physics sim needs — a sphere, a ground
 * grid, a trajectory trail — because they're tiny, have zero asset-decode cost,
 * and ship inside the APK with nothing to download. Offline-first by default.
 *
 * Everything here uses an UNLIT material: flat, single-colour shading with no
 * per-pixel lighting math. On an entry Mali/Adreno part that halves fragment
 * cost versus PBR, and flat high-chroma shapes actually read BETTER from the
 * back of a classroom than subtly-lit ones. Depth cues come from the grid and
 * the trail, not from shading.
 */
object MeshFactory {

    /**
     * Loads a compiled Filament material package (.filamat) from assets. The
     * source lives at assets/materials/unlit.mat and is compiled by `matc` at
     * build time (see app/build.gradle.kts). Throws if absent/incompatible so
     * the host shows an error state instead of a silent black screen.
     */
    fun loadUnlitMaterial(engine: Engine, assets: AssetManager, path: String): Material {
        val bytes = assets.open(path).use { it.readBytes() }
        val buf = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        buf.put(bytes).flip()
        return Material.Builder().payload(buf, buf.remaining()).build(engine)
    }

    fun instance(material: Material, r: Float, g: Float, b: Float, a: Float = 1f): MaterialInstance =
        material.createInstance().apply { setParameter("baseColor", r, g, b, a) }

    /**
     * Low-poly UV sphere. [rings] x [sectors] kept deliberately small — at
     * classroom distance a 16x12 sphere is indistinguishable from a 64x48 one
     * but costs a quarter of the vertices. Budget over beauty.
     */
    fun sphere(
        engine: Engine,
        @Entity entity: Int,
        mat: MaterialInstance,
        radius: Float,
        rings: Int = 12,
        sectors: Int = 16,
    ) {
        val vertCount = (rings + 1) * (sectors + 1)
        val pos = FloatArray(vertCount * 3)
        var p = 0
        for (i in 0..rings) {
            val phi = Math.PI * i / rings
            val y = cos(phi).toFloat()
            val rSin = sin(phi).toFloat()
            for (j in 0..sectors) {
                val theta = 2.0 * Math.PI * j / sectors
                pos[p++] = (radius * rSin * cos(theta)).toFloat()
                pos[p++] = (radius * y)
                pos[p++] = (radius * rSin * sin(theta)).toFloat()
            }
        }
        val idx = ShortArray(rings * sectors * 6)
        var k = 0
        for (i in 0 until rings) {
            for (j in 0 until sectors) {
                val a = (i * (sectors + 1) + j).toShort()
                val bIdx = ((i + 1) * (sectors + 1) + j).toShort()
                val c = (a + 1).toShort()
                val d = (bIdx + 1).toShort()
                idx[k++] = a; idx[k++] = bIdx; idx[k++] = c
                idx[k++] = c; idx[k++] = bIdx; idx[k++] = d
            }
        }
        buildRenderable(
            engine, entity, mat, pos, idx,
            RenderableManager.PrimitiveType.TRIANGLES,
            half = radius,
        )
    }

    /**
     * A flat reference grid on the XZ plane drawn as LINES — the cheapest
     * primitive, one draw call, and the spatial anchor that lets students read
     * 3D depth on a 2D projection.
     */
    fun grid(
        engine: Engine,
        @Entity entity: Int,
        mat: MaterialInstance,
        halfSize: Float,
        divisions: Int,
    ) {
        val lines = divisions + 1
        val pos = FloatArray(lines * 2 * 2 * 3)   // 2 axes * 2 endpoints * 3 comp
        var p = 0
        val step = (halfSize * 2f) / divisions
        for (i in 0 until lines) {
            val c = -halfSize + i * step
            // line parallel to X
            pos[p++] = -halfSize; pos[p++] = 0f; pos[p++] = c
            pos[p++] = halfSize;  pos[p++] = 0f; pos[p++] = c
            // line parallel to Z
            pos[p++] = c; pos[p++] = 0f; pos[p++] = -halfSize
            pos[p++] = c; pos[p++] = 0f; pos[p++] = halfSize
        }
        val idx = ShortArray(lines * 4) { it.toShort() }
        buildRenderable(
            engine, entity, mat, pos, idx,
            RenderableManager.PrimitiveType.LINES,
            half = halfSize,
        )
    }

    private fun buildRenderable(
        engine: Engine,
        @Entity entity: Int,
        mat: MaterialInstance,
        pos: FloatArray,
        idx: ShortArray,
        type: RenderableManager.PrimitiveType,
        half: Float,
    ) {
        val vCount = pos.size / 3
        val vb = VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(vCount)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
                VertexBuffer.AttributeType.FLOAT3, 0, 12)
            .build(engine)
        vb.setBufferAt(engine, 0, pos.toDirectFloatBuffer())

        val ib = IndexBuffer.Builder()
            .indexCount(idx.size)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)
        ib.setBuffer(engine, idx.toDirectShortBuffer())

        RenderableManager.Builder(1)
            .boundingBox(Box(0f, 0f, 0f, half, half, half))
            .geometry(0, type, vb, ib)
            .material(0, mat)
            .culling(true)         // let Filament frustum-cull this renderable
            .castShadows(false)
            .receiveShadows(false)
            .build(engine, entity)
    }
}

/**
 * A fixed-capacity trajectory trail. The whole point: it animates every frame
 * but NEVER reallocates a buffer. We allocate [maxPoints] up front and, on each
 * update, fill in the live points and collapse the unused tail onto the last
 * live point — those become zero-length line segments the GPU draws for almost
 * nothing, so the draw call and the vertex buffer stay a constant size forever.
 */
class TrailMesh(
    private val engine: Engine,
    @Entity val entity: Int,
    mat: MaterialInstance,
    private val maxPoints: Int,
) {
    private val pos = FloatArray(maxPoints * 3)
    private val direct = ByteBuffer
        .allocateDirect(maxPoints * 3 * 4)
        .order(ByteOrder.nativeOrder())
    private val vb: VertexBuffer = VertexBuffer.Builder()
        .bufferCount(1)
        .vertexCount(maxPoints)
        .attribute(VertexBuffer.VertexAttribute.POSITION, 0,
            VertexBuffer.AttributeType.FLOAT3, 0, 12)
        .build(engine)

    init {
        val idx = ShortArray(maxPoints) { it.toShort() }
        val ib = IndexBuffer.Builder()
            .indexCount(maxPoints)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)
        ib.setBuffer(engine, idx.toDirectShortBuffer())
        RenderableManager.Builder(1)
            .boundingBox(Box(0f, 0f, 0f, 50f, 50f, 50f))
            .geometry(0, RenderableManager.PrimitiveType.LINE_STRIP, vb, ib)
            .material(0, mat)
            .culling(false)        // trail can extend past its initial bounds
            .castShadows(false)
            .build(engine, entity)
    }

    /** Allocation-free per-frame update. [count] live points from [src] (xyz). */
    fun update(src: FloatArray, count: Int) {
        val n = count.coerceIn(0, maxPoints)
        var w = 0
        for (i in 0 until n) {
            pos[w++] = src[i * 3]; pos[w++] = src[i * 3 + 1]; pos[w++] = src[i * 3 + 2]
        }
        // Collapse the tail onto the last live point (degenerate segments).
        val lx: Float; val ly: Float; val lz: Float
        if (n > 0) { lx = pos[(n - 1) * 3]; ly = pos[(n - 1) * 3 + 1]; lz = pos[(n - 1) * 3 + 2] }
        else { lx = 0f; ly = 0f; lz = 0f }
        for (i in n until maxPoints) { pos[w++] = lx; pos[w++] = ly; pos[w++] = lz }

        direct.clear()
        direct.asFloatBuffer().put(pos)
        direct.position(0)
        vb.setBufferAt(engine, 0, direct)
    }
}

private fun FloatArray.toDirectFloatBuffer(): java.nio.FloatBuffer {
    val bb = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder())
    return bb.asFloatBuffer().put(this).apply { position(0) }
}

private fun ShortArray.toDirectShortBuffer(): java.nio.ShortBuffer {
    val bb = ByteBuffer.allocateDirect(size * 2).order(ByteOrder.nativeOrder())
    return bb.asShortBuffer().put(this).apply { position(0) }
}
