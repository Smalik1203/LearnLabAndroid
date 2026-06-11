package com.learnlab.experiments

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Cell Explorer — Grade 8 cell biology.
 *
 * Five modes behind a segmented control:
 *   Explore  — pseudo-3D rotatable cell, click any organelle for info
 *   Shapes   — gallery of common cell shapes (RBC, neuron, plant cell, amoeba)
 *   Label    — drag labels onto a head-on cell diagram
 *   Compare  — plant vs animal cells side-by-side with difference highlights
 *   Table    — organelle presence/function comparison table
 */

// ────────────────────────────────────────────────────────────────────
// Data model
// ────────────────────────────────────────────────────────────────────

private enum class Mode(val label: String) {
    Explore("Explore"),
    Detail("Detail"),
    Shapes("Shapes"),
    Compare("Compare"),
    Table("Table"),
}

private enum class CellType { Plant, Animal }

private enum class AnimKind { Pulse, AtpSparks, LightRays, DnaHelix, ProteinChain, VesiclePinch, FluidLevel, Membrane }

private data class Vec3(val x: Float, val y: Float, val z: Float)

private data class OrganelleKind(
    val id: String,
    val name: String,
    val color: Color,
    val radiusUnit: Float,            // 0..1, fraction of cellR
    val shape: OrganelleShape,
    val role: String,                 // one-liner
    val presentInPlant: Boolean,
    val presentInAnimal: Boolean,
    val animKind: AnimKind,
    val detail: String,               // 3–4 line description for Detail mode
)

private enum class OrganelleShape { Circle, Oval, Disk, Squiggle, RoundedRect, PairedRods }

private data class OrganelleInstance(val kindId: String, val pos: Vec3)

private val KINDS: List<OrganelleKind> = listOf(
    OrganelleKind(
        id = "nucleus", name = "Nucleus", color = Color(0xFF7C3AED),
        radiusUnit = 0.22f, shape = OrganelleShape.Circle,
        role = "Control centre — stores DNA, directs all cell activity.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.DnaHelix,
        detail = "The nucleus holds the cell's DNA, tightly packed as chromatin around a darker nucleolus. " +
            "A double membrane (the nuclear envelope) studded with pores controls what enters and leaves. " +
            "Inside, the nucleolus assembles ribosomes that the rest of the cell uses to make proteins.",
    ),
    OrganelleKind(
        id = "mitochondrion", name = "Mitochondrion", color = Color(0xFFEF4444),
        radiusUnit = 0.11f, shape = OrganelleShape.Oval,
        role = "Power-house — burns sugar with oxygen to make ATP energy.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.AtpSparks,
        detail = "Mitochondria release energy from glucose using oxygen — a process called aerobic respiration. " +
            "The inner membrane folds into cristae, packing in lots of surface area where ATP (the cell's energy currency) is made. " +
            "Active cells like muscle have many mitochondria; resting cells have fewer.",
    ),
    OrganelleKind(
        id = "chloroplast", name = "Chloroplast", color = Color(0xFF10B981),
        radiusUnit = 0.12f, shape = OrganelleShape.Oval,
        role = "Solar kitchen — uses sunlight to make glucose (photosynthesis).",
        presentInPlant = true, presentInAnimal = false,
        animKind = AnimKind.LightRays,
        detail = "Chloroplasts capture sunlight using the green pigment chlorophyll, then turn carbon dioxide and water into glucose. " +
            "Inside, stacks of disc-like thylakoids (grana) are linked by stroma lamellae. " +
            "Only plant cells and some algae have them — animals must eat plants to get the energy plants made here.",
    ),
    OrganelleKind(
        id = "ribosome", name = "Ribosome", color = Color(0xFFFCD34D),
        radiusUnit = 0.04f, shape = OrganelleShape.Circle,
        role = "Protein factory — reads mRNA and links amino acids into proteins.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.ProteinChain,
        detail = "Ribosomes are tiny two-part machines that read messenger RNA and join amino acids in the right order to build proteins. " +
            "Some float free in the cytoplasm; others stick to the rough ER. " +
            "They are the only organelle found in every kind of cell, from bacteria up.",
    ),
    OrganelleKind(
        id = "er", name = "Endoplasmic reticulum", color = Color(0xFF60A5FA),
        radiusUnit = 0.18f, shape = OrganelleShape.Squiggle,
        role = "Transport network — moves proteins and lipids around the cell.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.VesiclePinch,
        detail = "The endoplasmic reticulum is a network of folded membranes that runs from the nucleus through the cytoplasm. " +
            "The rough ER is studded with ribosomes and makes proteins for export; the smooth ER builds lipids and detoxifies. " +
            "Finished molecules bud off in vesicles bound for the Golgi.",
    ),
    OrganelleKind(
        id = "golgi", name = "Golgi apparatus", color = Color(0xFFF59E0B),
        radiusUnit = 0.10f, shape = OrganelleShape.Disk,
        role = "Packaging plant — modifies and ships proteins in vesicles.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.VesiclePinch,
        detail = "The Golgi apparatus is a stack of flattened sacs (cisternae). " +
            "Proteins arrive at the cis face from the ER, get sorted, tagged, and modified across the stack, " +
            "then leave from the trans face in vesicles bound for the membrane, lysosomes, or secretion.",
    ),
    OrganelleKind(
        id = "vacuole", name = "Vacuole", color = Color(0xFFA7F3D0),
        radiusUnit = 0.30f, shape = OrganelleShape.Circle,
        role = "Storage tank — holds water, food, and waste. Big in plant cells.",
        presentInPlant = true, presentInAnimal = true,
        animKind = AnimKind.FluidLevel,
        detail = "Vacuoles are fluid-filled sacs bounded by a single membrane called the tonoplast. " +
            "In plant cells one huge central vacuole holds water and salts, giving the cell its turgor (pressure that keeps the plant upright). " +
            "Animal cells have many tiny vacuoles for short-term storage.",
    ),
    OrganelleKind(
        id = "centriole", name = "Centriole", color = Color(0xFFE879F9),
        radiusUnit = 0.06f, shape = OrganelleShape.PairedRods,
        role = "Cell-division helper — organises spindle fibres when cells divide.",
        presentInPlant = false, presentInAnimal = true,
        animKind = AnimKind.Pulse,
        detail = "Centrioles are a pair of short cylinders set at right angles, each built from nine triplets of microtubules. " +
            "When an animal cell divides they organise the spindle fibres that pull chromosomes apart. " +
            "Plant cells manage cell division without them.",
    ),
    OrganelleKind(
        id = "lysosome", name = "Lysosome", color = Color(0xFFFB923C),
        radiusUnit = 0.05f, shape = OrganelleShape.Circle,
        role = "Digestion sac — recycles old cell parts and breaks down waste.",
        presentInPlant = false, presentInAnimal = true,
        animKind = AnimKind.Membrane,
        detail = "Lysosomes are membrane sacs packed with digestive enzymes. " +
            "They break down worn-out organelles, food particles, and invading bacteria — recycling the parts. " +
            "Sometimes called the cell's 'suicide bags', because if they burst they can dissolve the cell itself.",
    ),
    OrganelleKind(
        id = "cell-wall", name = "Cell wall", color = Color(0xFF065F46),
        radiusUnit = 0f, shape = OrganelleShape.RoundedRect,
        role = "Rigid outer jacket — gives plant cells their fixed shape and protection.",
        presentInPlant = true, presentInAnimal = false,
        animKind = AnimKind.Pulse,
        detail = "The cell wall sits just outside the cell membrane of plant cells. " +
            "It is made mostly of cellulose fibres laid down in cross-crossing layers, giving the cell a fixed shape and protection. " +
            "Between neighbouring walls a thin middle lamella glues cells together.",
    ),
)

private fun kindOf(id: String): OrganelleKind = KINDS.first { it.id == id }

// Plant cell layout (cell wall and large central vacuole; chloroplasts present)
private val PLANT_LAYOUT: List<OrganelleInstance> = listOf(
    OrganelleInstance("vacuole",      Vec3( 0.05f,  0.05f,  0.10f)),
    OrganelleInstance("nucleus",      Vec3(-0.55f, -0.05f, -0.25f)),
    OrganelleInstance("mitochondrion",Vec3( 0.55f, -0.40f,  0.30f)),
    OrganelleInstance("mitochondrion",Vec3(-0.30f,  0.55f, -0.10f)),
    OrganelleInstance("chloroplast",  Vec3( 0.40f,  0.45f, -0.30f)),
    OrganelleInstance("chloroplast",  Vec3(-0.50f,  0.40f,  0.40f)),
    OrganelleInstance("chloroplast",  Vec3( 0.65f, -0.05f, -0.40f)),
    OrganelleInstance("ribosome",     Vec3(-0.20f, -0.50f,  0.55f)),
    OrganelleInstance("ribosome",     Vec3( 0.15f, -0.60f, -0.30f)),
    OrganelleInstance("ribosome",     Vec3(-0.65f,  0.20f,  0.20f)),
    OrganelleInstance("er",           Vec3( 0.30f, -0.20f,  0.10f)),
    OrganelleInstance("golgi",        Vec3(-0.45f, -0.45f,  0.25f)),
)

// Animal cell layout (no cell wall, no chloroplasts; centrioles + lysosomes)
private val ANIMAL_LAYOUT: List<OrganelleInstance> = listOf(
    OrganelleInstance("nucleus",      Vec3( 0.00f,  0.00f, -0.10f)),
    OrganelleInstance("mitochondrion",Vec3( 0.55f, -0.35f,  0.30f)),
    OrganelleInstance("mitochondrion",Vec3(-0.50f,  0.45f, -0.20f)),
    OrganelleInstance("mitochondrion",Vec3( 0.40f,  0.55f,  0.15f)),
    OrganelleInstance("ribosome",     Vec3(-0.30f, -0.55f,  0.40f)),
    OrganelleInstance("ribosome",     Vec3( 0.25f, -0.60f, -0.15f)),
    OrganelleInstance("ribosome",     Vec3(-0.60f,  0.10f,  0.30f)),
    OrganelleInstance("er",           Vec3( 0.35f, -0.10f,  0.20f)),
    OrganelleInstance("golgi",        Vec3(-0.50f, -0.30f,  0.25f)),
    OrganelleInstance("vacuole",      Vec3( 0.55f,  0.40f, -0.30f)),
    OrganelleInstance("centriole",    Vec3(-0.25f,  0.30f,  0.55f)),
    OrganelleInstance("lysosome",     Vec3(-0.05f,  0.55f,  0.30f)),
    OrganelleInstance("lysosome",     Vec3( 0.60f,  0.10f,  0.10f)),
)

// Label placement for the persistent organelle labels around each layout.
// anchorDx / anchorDy = offset from the organelle centre, in units of organelle radius (r).
// labelX / labelY    = label end position, in units of cellR (relative to cell centre).
private data class LabelPlacement(
    val anchorDx: Float, val anchorDy: Float,
    val labelX: Float, val labelY: Float,
)

private val PLANT_LABELS: Map<String, LabelPlacement> = mapOf(
    "nucleus"       to LabelPlacement(-1.0f,  0.0f, -1.60f, -0.10f),
    "mitochondrion" to LabelPlacement( 1.3f,  0.0f,  1.55f, -0.55f),
    "chloroplast"   to LabelPlacement( 1.3f,  0.0f,  1.55f,  0.55f),
    "ribosome"      to LabelPlacement( 0.0f, -1.5f,  0.30f, -1.50f),
    "er"            to LabelPlacement( 1.5f,  0.0f,  1.55f, -0.18f),
    "golgi"         to LabelPlacement( 0.0f, -1.5f, -1.55f, -0.65f),
    "vacuole"       to LabelPlacement( 0.0f,  1.0f,  0.25f,  1.55f),
)

private val ANIMAL_LABELS: Map<String, LabelPlacement> = mapOf(
    "nucleus"       to LabelPlacement(-1.0f,  0.0f, -1.60f, -0.10f),
    "mitochondrion" to LabelPlacement( 1.3f,  0.0f,  1.55f, -0.55f),
    "ribosome"      to LabelPlacement( 0.0f, -1.5f,  0.20f, -1.50f),
    "er"            to LabelPlacement( 1.5f,  0.0f,  1.55f,  0.20f),
    "golgi"         to LabelPlacement( 0.0f, -1.5f, -1.55f, -0.50f),
    "vacuole"       to LabelPlacement( 1.3f,  0.0f,  1.55f,  0.60f),
    "centriole"     to LabelPlacement( 0.0f,  1.3f, -1.55f,  0.55f),
    "lysosome"      to LabelPlacement( 0.0f,  1.2f, -0.30f,  1.55f),
)

// ────────────────────────────────────────────────────────────────────
// Top-level shell
// ────────────────────────────────────────────────────────────────────

@Composable
fun CellExplorer(controls: ExperimentControls) {
    val t = LL.tokens
    var mode by remember { mutableStateOf(Mode.Explore) }
    var visited by remember { mutableStateOf(setOf(Mode.Explore)) }
    var showInfoPopover by remember { mutableStateOf(false) }

    LaunchedEffect(visited) {
        val p = (visited.size / Mode.entries.size.toFloat()).coerceAtMost(1f)
        controls.onProgress(p)
        if (visited.size == Mode.entries.size) {
            controls.onComplete(1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp)),
        ) {
            // ── Title bar ──
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LLText("CELL EXPLORER",
                        color = t.ink500, size = 11.sp,
                        weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                    LLText(
                        when (mode) {
                            Mode.Explore -> "Tap any organelle to see its job. Switch between plant and animal."
                            Mode.Detail -> "The full labelled cell. Tap any label for more about that organelle."
                            Mode.Shapes -> "Tap a card to see why this shape fits its function."
                            Mode.Compare -> "Plant vs animal — toggle a difference to highlight it."
                            Mode.Table -> "Plant / Animal organelle checklist."
                        },
                        color = t.ink400, size = 12.sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(t.accent50)
                        .border(1.dp, t.accent500, RoundedCornerShape(999.dp))
                        .clickable { showInfoPopover = true },
                    contentAlignment = Alignment.Center,
                ) {
                    LLText("i", color = t.accent700, size = 14.sp,
                        weight = FontWeight.Bold)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // ── Mode tabs ──
            ModeTabs(current = mode, onSelect = { newMode ->
                mode = newMode
                visited = visited + newMode
            })
            Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))

            // ── Body ──
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(t.surface2)) {
                when (mode) {
                    Mode.Explore -> ExploreMode()
                    Mode.Detail -> DetailMode()
                    Mode.Shapes -> ShapesMode()
                    Mode.Compare -> CompareMode()
                    Mode.Table -> TableMode()
                }
            }
        }

        if (showInfoPopover) {
            WhatIsACellPopover(onDismiss = { showInfoPopover = false })
        }
    }
}

@Composable
private fun ModeTabs(current: Mode, onSelect: (Mode) -> Unit) {
    val t = LL.tokens
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Mode.entries.forEach { m ->
            val selected = m == current
            val bg = if (selected) t.accent50 else t.surface2
            val fg = if (selected) t.accent700 else t.ink400
            val border = if (selected) t.accent500 else t.line
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(999.dp))
                    .clickable { onSelect(m) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                LLText(m.label, color = fg, size = 13.sp,
                    weight = FontWeight.SemiBold)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Mode 1: Explore — pseudo-3D rotatable cell + click-reveal
// ────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreMode() {
    val t = LL.tokens
    var cellType by remember { mutableStateOf(CellType.Plant) }
    var selectedKindId by remember { mutableStateOf<String?>(null) }
    var pulse by remember { mutableStateOf(0f) }

    LaunchedEffect(selectedKindId) {
        pulse = 0f
        if (selectedKindId == null) return@LaunchedEffect
        var lastNanos = 0L
        var t0 = 0f
        while (selectedKindId != null) {
            withFrameNanos { now ->
                if (lastNanos == 0L) lastNanos = now
                val dt = ((now - lastNanos) / 1_000_000_000f)
                lastNanos = now
                t0 += dt
                pulse = ((sin(t0 * 4f) + 1f) / 2f)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Scene
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .pointerInput(cellType) {
                    detectTapGestures { tap ->
                        selectedKindId = hitTestCell(
                            tap = tap,
                            canvasW = size.width.toFloat(),
                            canvasH = size.height.toFloat(),
                            cellType = cellType,
                        )
                    }
                },
        ) {
            CellSceneCanvas(
                cellType = cellType,
                selectedKindId = selectedKindId,
                pulse = pulse,
                showLabels = true,
            )
        }

        // Right rail
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText("CELL TYPE", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(CellType.Plant to "Plant", CellType.Animal to "Animal").forEach { (ct, label) ->
                        val sel = cellType == ct
                        val bg = if (sel) t.accent600 else t.surface2
                        val fg = if (sel) Color.White else t.ink200
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .clickable {
                                    cellType = ct
                                    selectedKindId = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) { LLText(label, color = fg, size = 13.sp, weight = FontWeight.SemiBold) }
                    }
                }
            }
            // Info panel
            OrganelleInfoPanel(selectedKindId = selectedKindId)
        }
    }
}

@Composable
private fun OrganelleInfoPanel(selectedKindId: String?) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText("ORGANELLE", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        if (selectedKindId == null) {
            LLText("Tap any organelle in the cell to see its job.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            return@Column
        }
        val k = kindOf(selectedKindId)
        LLText(k.name, color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
        LLText(k.role, color = t.ink200, size = 12.sp, lineHeight = 16.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface2)
                .border(1.dp, t.line, RoundedCornerShape(8.dp)),
        ) {
            MiniAnimation(k.animKind, k.color)
        }
        LLText(
            "Present in: ${if (k.presentInPlant) "Plant" else "—"}${if (k.presentInPlant && k.presentInAnimal) " · " else ""}${if (k.presentInAnimal) "Animal" else if (!k.presentInPlant) "" else ""}".let {
                if (k.presentInPlant && k.presentInAnimal) "Present in: Plant · Animal"
                else if (k.presentInPlant) "Present in: Plant only"
                else "Present in: Animal only"
            },
            color = t.ink500, size = 11.sp,
        )
    }
}

@Composable
private fun MiniAnimation(kind: AnimKind, color: Color) {
    val t = LL.tokens
    var time by remember(kind) { mutableStateOf(0f) }
    LaunchedEffect(kind) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) lastNanos = now
                val dt = ((now - lastNanos) / 1_000_000_000f)
                lastNanos = now
                time += dt
            }
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val cx = w / 2f; val cy = h / 2f
        when (kind) {
            AnimKind.AtpSparks -> {
                // central oval (mito) with sparks pulsing outward
                drawOval(color,
                    topLeft = Offset(cx - 40f, cy - 18f),
                    size = Size(80f, 36f))
                for (i in 0 until 6) {
                    val phase = (time * 1.5f + i * 0.4f) % 1f
                    val ang = i * (PI.toFloat() / 3f)
                    val r = 30f + phase * 60f
                    val alpha = (1f - phase).coerceIn(0f, 1f)
                    drawCircle(
                        Color(0xFFFCD34D).copy(alpha = alpha),
                        radius = 3f,
                        center = Offset(cx + cos(ang) * r, cy + sin(ang) * r),
                    )
                }
            }
            AnimKind.LightRays -> {
                // Sun rays coming down, glucose dots emerging
                for (i in 0 until 5) {
                    val x = w * (0.2f + i * 0.15f)
                    val phase = (time + i * 0.2f) % 1f
                    drawLine(Color(0xFFFCD34D).copy(alpha = 0.5f),
                        Offset(x, 6f), Offset(x, 6f + phase * (cy - 14f)),
                        strokeWidth = 1.5f)
                }
                drawOval(color,
                    topLeft = Offset(cx - 36f, cy - 14f),
                    size = Size(72f, 28f))
                for (i in 0 until 4) {
                    val phase = (time * 0.8f + i * 0.25f) % 1f
                    drawCircle(Color(0xFFA7F3D0).copy(alpha = 1f - phase),
                        radius = 3f,
                        center = Offset(cx + (i - 1.5f) * 18f, cy + 18f + phase * 30f))
                }
            }
            AnimKind.DnaHelix -> {
                drawCircle(color.copy(alpha = 0.6f), 32f, Offset(cx, cy))
                drawCircle(Color.White, 32f, Offset(cx, cy), style = Stroke(1.5f))
                val turns = 4
                for (i in 0..30) {
                    val tt = i / 30f
                    val phase = time * 1.2f + tt * turns * 2f * PI.toFloat()
                    val x1 = cx + cos(phase) * 16f
                    val x2 = cx - cos(phase) * 16f
                    val y = cy - 24f + tt * 48f
                    drawCircle(Color(0xFFE9D5FF), 2f, Offset(x1, y))
                    drawCircle(Color(0xFFC4B5FD), 2f, Offset(x2, y))
                }
            }
            AnimKind.ProteinChain -> {
                drawCircle(color, 14f, Offset(cx - 30f, cy))
                val len = ((time * 0.9f) % 1f) * 80f
                var x = cx - 16f
                val r = 5f
                while (x < cx - 16f + len) {
                    drawCircle(Color(0xFF7DD3FC), r, Offset(x, cy))
                    x += r * 2.2f
                }
            }
            AnimKind.VesiclePinch -> {
                // ER squiggle on left, vesicle pinching off to right
                val path = Path().apply {
                    moveTo(8f, cy - 24f)
                    cubicTo(40f, cy - 50f, 60f, cy + 10f, 90f, cy - 12f)
                    cubicTo(120f, cy - 32f, 140f, cy + 18f, w - 80f, cy)
                }
                drawPath(path, color, style = Stroke(3f, cap = StrokeCap.Round))
                val phase = (time * 0.7f) % 1f
                val vx = w - 80f + phase * 60f
                drawCircle(color.copy(alpha = 1f - phase), 8f, Offset(vx, cy))
            }
            AnimKind.FluidLevel -> {
                val rectX = w * 0.2f; val rectW = w * 0.6f
                val rectY = h * 0.2f; val rectH = h * 0.7f
                drawRect(t.surface2.copy(alpha = 0.6f),
                    topLeft = Offset(rectX, rectY),
                    size = Size(rectW, rectH))
                drawRect(color.copy(alpha = 0.7f), style = Stroke(1.5f),
                    topLeft = Offset(rectX, rectY),
                    size = Size(rectW, rectH))
                val fill = (sin(time * 0.8f) + 1f) / 2f
                drawRect(color.copy(alpha = 0.7f),
                    topLeft = Offset(rectX, rectY + rectH * (1f - fill)),
                    size = Size(rectW, rectH * fill))
            }
            AnimKind.Membrane -> {
                drawLine(color,
                    Offset(10f, cy - 6f), Offset(w - 10f, cy - 6f), strokeWidth = 3f)
                drawLine(color,
                    Offset(10f, cy + 6f), Offset(w - 10f, cy + 6f), strokeWidth = 3f)
                val phase = (time * 0.6f) % 1f
                drawCircle(Color(0xFF60A5FA),
                    5f, Offset(8f + phase * (w - 16f), cy))
            }
            AnimKind.Pulse -> {
                val r = 12f + (sin(time * 3f) + 1f) * 6f
                drawCircle(color, r, Offset(cx, cy))
            }
        }
    }
}

// Project a Vec3 to screen Offset — flat NCERT-style.
// pos.z is preserved only as a static layering hint (back-to-front draw order).
private fun project(
    pos: Vec3,
    cx: Float, cy: Float,
    cellR: Float,
): ProjectedPoint {
    return ProjectedPoint(
        x = cx + pos.x * cellR,
        y = cy + pos.y * cellR,
        zRot = pos.z,
        scale = 1f,
    )
}

private data class ProjectedPoint(val x: Float, val y: Float, val zRot: Float, val scale: Float)

@Composable
private fun CellSceneCanvas(
    cellType: CellType,
    selectedKindId: String?,
    pulse: Float,
    showLabels: Boolean,
) {
    val t = LL.tokens
    val textMeasurer = rememberTextMeasurer()
    val inkLabel = t.ink400
    val instances = if (cellType == CellType.Plant) PLANT_LAYOUT else ANIMAL_LAYOUT
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val cx = w / 2f; val cy = h / 2f
        val cellR = min(w, h) * 0.35f

        // Cell wall + membrane + cytoplasm (shared renderer)
        drawCellOutline(cx, cy, cellR, isPlant = cellType == CellType.Plant)

        // Outer chrome labels (cell wall + membrane)
        if (showLabels) {
            if (cellType == CellType.Plant) {
                drawLabel(textMeasurer, "Cell wall",
                    anchor = Offset(cx - cellR * 1.18f, cy - cellR * 0.9f),
                    labelEnd = Offset(cx - cellR * 1.45f, cy - cellR * 1.1f),
                    color = Color(0xFF065F46))
            }
            drawLabel(textMeasurer, "Cell membrane",
                anchor = Offset(cx - cellR * 0.71f, cy + cellR * 0.71f),
                labelEnd = Offset(cx - cellR * 1.15f, cy + cellR * 1.0f),
                color = Color(0xFFEC4899))
        }

        // Organelles, sorted back to front by static z
        val projected = instances.map { it to project(it.pos, cx, cy, cellR) }
            .sortedBy { it.second.zRot }
        for ((inst, p) in projected) {
            val k = kindOf(inst.kindId)
            val r = cellR * k.radiusUnit
            val isSelected = selectedKindId == k.id
            if (isSelected) {
                drawCircle(
                    color = k.color.copy(alpha = 0.35f + 0.4f * pulse),
                    radius = r * (1.6f + 0.2f * pulse),
                    center = Offset(p.x, p.y),
                )
            }
            drawOrganelle(k, Offset(p.x, p.y), r)
        }

        // Persistent organelle labels (only the first instance of each kind is labelled,
        // so we don't repeat "Mitochondrion" three times).
        if (showLabels) {
            val labelTable = if (cellType == CellType.Plant) PLANT_LABELS else ANIMAL_LABELS
            val labelled = mutableSetOf<String>()
            for (inst in instances) {
                if (inst.kindId in labelled) continue
                val place = labelTable[inst.kindId] ?: continue
                val k = kindOf(inst.kindId)
                val p = project(inst.pos, cx, cy, cellR)
                val r = cellR * k.radiusUnit
                val anchor = Offset(p.x + place.anchorDx * r, p.y + place.anchorDy * r)
                val labelEnd = Offset(cx + place.labelX * cellR, cy + place.labelY * cellR)
                drawLabel(textMeasurer, k.name, anchor, labelEnd, color = inkLabel)
                labelled += inst.kindId
            }
        }
    }
}

private fun DrawScope.drawCellOutline(
    cx: Float, cy: Float, cellR: Float,
    isPlant: Boolean,
    wallStrokeColor: Color = Color(0xFF065F46),
    wallStrokeWidth: Float = 3f,
) {
    // ── Plant cell wall — cellulose hatched outer band with middle lamella ──
    if (isPlant) {
        val outer = cellR * 1.20f
        val left = cx - outer
        val top = cy - outer
        val side = outer * 2f
        // wall fill wash
        drawRect(Color(0xFF065F46).copy(alpha = 0.18f),
            topLeft = Offset(left, top), size = Size(side, side))
        // outer stroke (cellulose boundary)
        drawRect(wallStrokeColor,
            topLeft = Offset(left, top), size = Size(side, side),
            style = Stroke(wallStrokeWidth))
        // middle lamella — thin faint stripe between wall and membrane
        val lamInset = 10f
        drawRect(Color(0xFFCBD5E1).copy(alpha = 0.85f),
            topLeft = Offset(left + lamInset, top + lamInset),
            size = Size(side - lamInset * 2f, side - lamInset * 2f),
            style = Stroke(2f))
        // inner stroke (wall→membrane gap)
        val inset = 4f
        drawRect(wallStrokeColor.copy(alpha = 0.55f),
            topLeft = Offset(left + inset, top + inset),
            size = Size(side - inset * 2f, side - inset * 2f),
            style = Stroke(1f))
        // cellulose hatch — short cross-hatched fibres around the perimeter
        val hatchCol = Color(0xFF065F46).copy(alpha = 0.50f)
        val hatchStep = 16f
        val hatchInset = 8f
        // top edge
        var x = left + hatchInset + 8f
        while (x < left + side - hatchInset - 8f) {
            drawLine(hatchCol,
                Offset(x, top + hatchInset),
                Offset(x + 10f, top + hatchInset + 10f),
                strokeWidth = 1f)
            x += hatchStep
        }
        // bottom edge
        x = left + hatchInset + 8f
        while (x < left + side - hatchInset - 8f) {
            drawLine(hatchCol,
                Offset(x, top + side - hatchInset),
                Offset(x + 10f, top + side - hatchInset - 10f),
                strokeWidth = 1f)
            x += hatchStep
        }
        // left edge
        var yy = top + hatchInset + 8f
        while (yy < top + side - hatchInset - 8f) {
            drawLine(hatchCol,
                Offset(left + hatchInset, yy),
                Offset(left + hatchInset + 10f, yy + 10f),
                strokeWidth = 1f)
            yy += hatchStep
        }
        // right edge
        yy = top + hatchInset + 8f
        while (yy < top + side - hatchInset - 8f) {
            drawLine(hatchCol,
                Offset(left + side - hatchInset, yy),
                Offset(left + side - hatchInset - 10f, yy + 10f),
                strokeWidth = 1f)
            yy += hatchStep
        }
    }

    // ── Cell membrane — circular bilayer ──
    val memTL = Offset(cx - cellR, cy - cellR)
    val memSize = Size(cellR * 2f, cellR * 2f)
    // cytoplasm wash (radial gradient)
    val cytoBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFBCFE8).copy(alpha = 0.30f),
            Color(0xFFFBCFE8).copy(alpha = 0.12f),
        ),
        center = Offset(cx, cy), radius = cellR,
    )
    drawOval(cytoBrush, topLeft = memTL, size = memSize)
    // outer bilayer stroke
    drawOval(Color(0xFFEC4899), topLeft = memTL, size = memSize, style = Stroke(2.5f))
    // inner bilayer stroke (phospholipid bilayer)
    drawOval(Color(0xFFEC4899).copy(alpha = 0.55f),
        topLeft = Offset(memTL.x + 4f, memTL.y + 4f),
        size = Size(memSize.width - 8f, memSize.height - 8f),
        style = Stroke(1f))
    // embedded membrane proteins (ion channels)
    val proteinCount = 10
    val proteinCol = Color(0xFFC026D3).copy(alpha = 0.90f)
    for (i in 0 until proteinCount) {
        val a = i * (2f * PI.toFloat() / proteinCount) + 0.15f
        val px = cx + cos(a) * cellR
        val py = cy + sin(a) * cellR
        // small "tube" through the bilayer
        drawCircle(proteinCol, 3.5f, Offset(px, py))
        drawCircle(Color(0xFFFBCFE8), 1.4f, Offset(px, py))
    }
}

private fun DrawScope.drawOrganelle(k: OrganelleKind, center: Offset, r: Float) {
    when (k.id) {
        "nucleus" -> drawNucleus(center, r, k.color)
        "mitochondrion" -> drawMitochondrion(center, r, k.color)
        "chloroplast" -> drawChloroplast(center, r, k.color)
        "ribosome" -> drawRibosome(center, r, k.color)
        "er" -> drawER(center, r, k.color)
        "golgi" -> drawGolgi(center, r, k.color)
        "vacuole" -> drawVacuole(center, r, k.color)
        "centriole" -> drawCentriole(center, r, k.color)
        "lysosome" -> drawLysosome(center, r, k.color)
        "cell-wall" -> { /* drawn at cell-outline level */ }
        else -> drawCircle(k.color, r, center)
    }
}

private fun DrawScope.drawNucleus(c: Offset, r: Float, color: Color) {
    // base sphere with soft gradient (nucleoplasm)
    val brush = Brush.radialGradient(
        colors = listOf(
            color.copy(alpha = 0.50f),
            color.copy(alpha = 0.80f),
            color.copy(red = color.red * 0.55f, green = color.green * 0.55f, blue = color.blue * 0.55f),
        ),
        center = Offset(c.x - r * 0.25f, c.y - r * 0.25f),
        radius = r * 1.3f,
    )
    drawCircle(brush, r, c)
    // heterochromatin ring (slightly darker near the envelope)
    drawCircle(
        Color(0xFF4C1D95).copy(alpha = 0.25f),
        r * 0.95f, c, style = Stroke(r * 0.10f),
    )
    // nuclear envelope — outer + inner (double membrane)
    drawCircle(Color(0xFF4C1D95), r * 1.04f, c, style = Stroke(1.6f))
    drawCircle(Color(0xFF4C1D95).copy(alpha = 0.55f), r * 0.96f, c, style = Stroke(1f))
    // nuclear pores — 10 small breaks/dots around the envelope
    if (r > 8f) {
        val poreCount = 10
        val poreCol = Color(0xFFFAF5FF)
        val poreEdge = Color(0xFF4C1D95)
        for (i in 0 until poreCount) {
            val a = i * (2f * PI.toFloat() / poreCount) + 0.15f
            val px = c.x + cos(a) * r * 1.00f
            val py = c.y + sin(a) * r * 1.00f
            drawCircle(poreCol, 2.4f, Offset(px, py))
            drawCircle(poreEdge, 2.4f, Offset(px, py), style = Stroke(0.8f))
        }
    }
    // chromatin (denser — 8 curves radiating from centre)
    val chroma = Color(0xFFE9D5FF).copy(alpha = 0.75f)
    val r6 = r * 0.62f
    val strands = 8
    for (i in 0 until strands) {
        val a = i * (2f * PI.toFloat() / strands) + 0.3f
        val p1 = Offset(c.x + cos(a) * r * 0.18f, c.y + sin(a) * r * 0.18f)
        val p2 = Offset(c.x + cos(a) * r6, c.y + sin(a) * r6)
        val mid = Offset((p1.x + p2.x) / 2f + cos(a + 1.5f) * r * 0.14f,
            (p1.y + p2.y) / 2f + sin(a + 1.5f) * r * 0.14f)
        val path = Path().apply {
            moveTo(p1.x, p1.y); quadraticTo(mid.x, mid.y, p2.x, p2.y)
        }
        drawPath(path, chroma, style = Stroke(1.4f, cap = StrokeCap.Round))
    }
    // nucleolus — prominent darker yellow blob, off-centre
    val nlc = Offset(c.x + r * 0.18f, c.y - r * 0.06f)
    drawCircle(Color(0xFFCA8A04), r * 0.26f, nlc)
    drawCircle(Color(0xFFFCD34D), r * 0.21f, nlc)
    drawCircle(Color(0xFF92400E).copy(alpha = 0.55f), r * 0.08f,
        Offset(nlc.x + r * 0.03f, nlc.y))
}

private fun DrawScope.drawMitochondrion(c: Offset, r: Float, color: Color) {
    val w = r * 2.8f
    val h = r * 1.5f
    val tl = Offset(c.x - w / 2f, c.y - h / 2f)
    // outer membrane (gradient)
    val outer = Brush.linearGradient(
        colors = listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.75f)),
        start = Offset(tl.x, tl.y), end = Offset(tl.x + w, tl.y + h),
    )
    drawOval(outer, topLeft = tl, size = Size(w, h))
    // matrix (inner space — darker)
    val matrixTL = Offset(c.x - w * 0.43f, c.y - h * 0.37f)
    val matrixSize = Size(w * 0.86f, h * 0.74f)
    val matrixBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)),
        center = c, radius = w * 0.45f,
    )
    drawOval(matrixBrush, topLeft = matrixTL, size = matrixSize)

    // Cristae — many continuous finger-like invaginations of the inner membrane.
    if (r > 6f) {
        val cristaCol = Color(0xFFFCA5A5).copy(alpha = 0.95f)
        val cristaCount = 10
        val matrixLeft = matrixTL.x
        val matrixTop = matrixTL.y
        val matrixW = matrixSize.width
        val matrixH = matrixSize.height
        // Alternate cristae from top and bottom edges, like a fan-fold pattern.
        for (i in 0 until cristaCount) {
            val tFrac = (i + 0.5f) / cristaCount.toFloat()
            val xMid = matrixLeft + matrixW * tFrac
            val fromTop = i % 2 == 0
            val yEdge = if (fromTop) matrixTop + 2f else matrixTop + matrixH - 2f
            val yTip = if (fromTop) matrixTop + matrixH * 0.78f else matrixTop + matrixH * 0.22f
            val controlOffset = matrixW * 0.045f
            val path = Path().apply {
                moveTo(xMid - controlOffset, yEdge)
                quadraticTo(xMid, yTip, xMid + controlOffset, yEdge)
            }
            drawPath(path, cristaCol, style = Stroke(1.4f, cap = StrokeCap.Round))
        }
        // Tiny granules in the matrix (mitochondrial ribosomes / DNA)
        val granuleCol = Color(0xFFFEF3C7).copy(alpha = 0.7f)
        listOf(
            Offset(c.x - w * 0.18f, c.y + h * 0.08f),
            Offset(c.x + w * 0.12f, c.y - h * 0.12f),
            Offset(c.x + w * 0.30f, c.y + h * 0.18f),
        ).forEach { drawCircle(granuleCol, r * 0.06f, it) }
    }
    // double-membrane outline (outer + inner trace)
    drawOval(Color(0xFF7F1D1D), topLeft = tl, size = Size(w, h), style = Stroke(1.4f))
    drawOval(Color(0xFFFCA5A5).copy(alpha = 0.55f),
        topLeft = Offset(tl.x + 3f, tl.y + 3f),
        size = Size(w - 6f, h - 6f), style = Stroke(0.8f))
}

private fun DrawScope.drawChloroplast(c: Offset, r: Float, color: Color) {
    val w = r * 2.8f
    val h = r * 1.6f
    val tl = Offset(c.x - w / 2f, c.y - h / 2f)
    // stroma fill (light green inside)
    val stroma = Brush.radialGradient(
        colors = listOf(Color(0xFFA7F3D0), color.copy(alpha = 0.85f), Color(0xFF047857)),
        center = c, radius = w * 0.55f,
    )
    drawOval(stroma, topLeft = tl, size = Size(w, h))
    // grana stacks: 5 stacks, each 5 thylakoid discs, linked by stroma lamellae
    if (r > 6f) {
        val granaDark = Color(0xFF064E3B)
        val granaLight = Color(0xFF065F46)
        val granaCount = 5
        val discsPerStack = 5
        val firstStackX = c.x - w * 0.32f
        val stackSpacing = w * 0.16f
        val discW = r * 0.32f
        val discH = r * 0.08f
        val stackTop = c.y - r * 0.45f
        val centerYsOfStacks = FloatArray(granaCount) { c.y }
        for (g in 0 until granaCount) {
            val cxg = firstStackX + g * stackSpacing
            // jitter stack vertical position slightly for organic feel
            val yJitter = if (g % 2 == 0) -r * 0.06f else r * 0.06f
            val sTop = stackTop + yJitter
            for (i in 0 until discsPerStack) {
                val y0 = sTop + i * (discH + 1.5f)
                drawOval(if (i % 2 == 0) granaDark else granaLight,
                    topLeft = Offset(cxg - discW / 2f, y0),
                    size = Size(discW, discH))
            }
            centerYsOfStacks[g] = sTop + (discsPerStack * (discH + 1.5f)) / 2f
        }
        // Stroma lamellae — connecting lines between adjacent grana
        val lamella = Color(0xFF064E3B).copy(alpha = 0.60f)
        for (g in 0 until granaCount - 1) {
            val x1 = firstStackX + g * stackSpacing + discW / 2f
            val x2 = firstStackX + (g + 1) * stackSpacing - discW / 2f
            val y = (centerYsOfStacks[g] + centerYsOfStacks[g + 1]) / 2f
            drawLine(lamella, Offset(x1, y), Offset(x2, y), strokeWidth = 1.2f)
        }
    }
    // double envelope outline (outer + inner)
    drawOval(Color(0xFF064E3B), topLeft = tl, size = Size(w, h), style = Stroke(1.4f))
    drawOval(Color(0xFF065F46).copy(alpha = 0.55f),
        topLeft = Offset(tl.x + 3f, tl.y + 3f),
        size = Size(w - 6f, h - 6f), style = Stroke(0.8f))
}

private fun DrawScope.drawRibosome(c: Offset, r: Float, color: Color) {
    // larger 60S
    drawCircle(color, r, c)
    // smaller 40S cap on top-right
    drawCircle(color.copy(alpha = 0.7f), r * 0.65f, Offset(c.x + r * 0.4f, c.y - r * 0.4f))
    drawCircle(Color(0xFFB45309).copy(alpha = 0.6f), r, c, style = Stroke(0.8f))
}

private fun DrawScope.drawER(c: Offset, r: Float, color: Color) {
    // Rough ER — 3 stacked parallel curving cisternae with ribosomes on the surfaces.
    val span = r * 1.7f
    val cisternaCount = 3
    val gap = r * 0.30f
    val baseY = c.y - gap * (cisternaCount - 1) / 2f
    val membraneCol = color.copy(alpha = 0.95f)
    val membraneStrokeW = r * 0.10f
    for (i in 0 until cisternaCount) {
        val yLine = baseY + i * gap
        val path = Path().apply {
            moveTo(c.x - span, yLine + r * 0.05f * (i - 1f))
            cubicTo(
                c.x - span * 0.35f, yLine - r * 0.25f,
                c.x + span * 0.25f, yLine + r * 0.30f,
                c.x + span, yLine - r * 0.10f,
            )
        }
        drawPath(path, membraneCol, style = Stroke(membraneStrokeW, cap = StrokeCap.Round))
    }
    // Ribosomes — small yellow dots dotted along the membranes.
    if (r > 10f) {
        val ribCol = Color(0xFFFCD34D)
        val ribStroke = Color(0xFFB45309).copy(alpha = 0.5f)
        for (i in 0 until cisternaCount) {
            val yLine = baseY + i * gap
            // ribosomes above and below the membrane
            for (j in 0 until 7) {
                val tFrac = j / 7f
                val px = c.x - span + tFrac * (2 * span)
                // approximate y on the curve using a sin-shape
                val curveOffset = sin(tFrac * PI.toFloat() * 1.4f) * r * 0.20f * (1f - i * 0.15f)
                val side = if (j % 2 == 0) -1f else 1f
                val py = yLine + curveOffset + side * (membraneStrokeW / 2f + r * 0.06f)
                drawCircle(ribCol, r * 0.07f, Offset(px, py))
                drawCircle(ribStroke, r * 0.07f, Offset(px, py), style = Stroke(0.6f))
            }
        }
    }
}

private fun DrawScope.drawSmoothER(c: Offset, r: Float, color: Color) {
    // A tangled tubular network — winding, no ribosomes.
    val net = color.copy(alpha = 0.80f)
    val stroke = r * 0.08f
    val path = Path().apply {
        moveTo(c.x - r * 1.2f, c.y)
        cubicTo(
            c.x - r * 0.6f, c.y - r * 0.8f,
            c.x - r * 0.2f, c.y + r * 0.8f,
            c.x + r * 0.4f, c.y - r * 0.2f,
        )
        cubicTo(
            c.x + r * 0.9f, c.y - r * 0.6f,
            c.x + r * 1.2f, c.y + r * 0.5f,
            c.x + r * 0.5f, c.y + r * 0.8f,
        )
        cubicTo(
            c.x, c.y + r * 1.1f,
            c.x - r * 0.6f, c.y + r * 0.4f,
            c.x - r * 1.0f, c.y - r * 0.4f,
        )
    }
    drawPath(path, net, style = Stroke(stroke, cap = StrokeCap.Round))
}

private fun DrawScope.drawGolgi(c: Offset, r: Float, color: Color) {
    // Stack of 6 curved cisternae; widest at the cis face (top), narrowest at trans (bottom).
    val sacCount = 6
    val sacWMax = r * 2.6f
    val sacWMin = r * 1.7f
    val sacH = r * 0.30f
    val gap = r * 0.22f
    val stackTop = c.y - (sacCount - 1) * gap / 2f - sacH * 0.3f
    for (i in 0 until sacCount) {
        val tFrac = i / (sacCount - 1f)
        val sacW = sacWMax + (sacWMin - sacWMax) * tFrac
        val xJitter = if (i % 2 == 0) -r * 0.06f else r * 0.06f
        val y = stackTop + i * gap
        val sacCol = color.copy(alpha = 0.95f - i * 0.06f)
        // filled stroke band
        drawArc(sacCol,
            startAngle = 180f, sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(c.x - sacW / 2f + xJitter, y - sacH * 0.6f),
            size = Size(sacW, sacH * 1.2f),
            style = Stroke(2f, cap = StrokeCap.Round))
    }
    // cis / trans labels (only if the icon is big enough for the text to make sense)
    if (r > 16f) {
        // We can't draw text in DrawScope without a textMeasurer here.
        // Mark cis (top) and trans (bottom) with small coloured pips instead.
        val cisCol = Color(0xFF60A5FA)
        val transCol = Color(0xFFEC4899)
        drawCircle(cisCol, r * 0.10f, Offset(c.x - sacWMax / 2f - r * 0.18f, stackTop))
        drawCircle(transCol, r * 0.10f,
            Offset(c.x - sacWMin / 2f - r * 0.18f, stackTop + (sacCount - 1) * gap))
    }
    // Vesicles budding off the trans face (bottom-right side).
    if (r > 6f) {
        val vCol = color.copy(alpha = 0.92f)
        val vEdge = Color(0xFF92400E).copy(alpha = 0.55f)
        val transY = stackTop + (sacCount - 1) * gap
        val budRight = c.x + sacWMin / 2f
        val buds = listOf(
            Triple(Offset(budRight + r * 0.10f, transY + r * 0.20f), r * 0.16f, 0),
            Triple(Offset(budRight + r * 0.32f, transY + r * 0.42f), r * 0.13f, 0),
            Triple(Offset(budRight + r * 0.55f, transY + r * 0.62f), r * 0.10f, 0),
            Triple(Offset(c.x - sacWMin / 2f - r * 0.30f, transY + r * 0.45f), r * 0.12f, 0),
        )
        buds.forEach { (pt, rad, _) ->
            drawCircle(vCol, rad, pt)
            drawCircle(vEdge, rad, pt, style = Stroke(0.8f))
        }
    }
}

private fun DrawScope.drawVacuole(c: Offset, r: Float, color: Color) {
    // Cell sap (radial gradient from clear centre to greener edge)
    val brush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFECFDF5).copy(alpha = 0.55f),
            color.copy(alpha = 0.40f),
            color.copy(alpha = 0.20f),
        ),
        center = c, radius = r,
    )
    drawCircle(brush, r, c)
    // Tonoplast — single membrane, slightly thicker than other organelle outlines
    drawCircle(Color(0xFF047857).copy(alpha = 0.70f), r, c, style = Stroke(1.6f))
    // Inner highlight to suggest fluid
    if (r > 18f) {
        val highlight = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(c.x - r * 0.3f, c.y - r * 0.3f),
            radius = r * 0.5f,
        )
        drawCircle(highlight, r * 0.6f, Offset(c.x - r * 0.3f, c.y - r * 0.3f))
        // Few faint drift lines
        val drift = Color(0xFF10B981).copy(alpha = 0.18f)
        for (i in 0 until 3) {
            val yy = c.y + (i - 1) * r * 0.25f + r * 0.05f
            val path = Path().apply {
                moveTo(c.x - r * 0.55f, yy)
                quadraticTo(c.x, yy - r * 0.15f, c.x + r * 0.55f, yy)
            }
            drawPath(path, drift, style = Stroke(1f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawCentriole(c: Offset, r: Float, color: Color) {
    val rodW = r * 0.8f
    val rodH = r * 1.8f
    // vertical cylinder (left)
    val vRodTL = Offset(c.x - rodW - r * 0.25f, c.y - rodH / 2f)
    drawRect(color, topLeft = vRodTL, size = Size(rodW, rodH))
    // horizontal cylinder (right)
    val hRodTL = Offset(c.x + r * 0.25f, c.y - rodW / 2f)
    drawRect(color, topLeft = hRodTL, size = Size(rodH, rodW))

    // End caps with 9-triplet hint — small radial ticks at each cylinder end.
    if (r > 6f) {
        val tickCol = Color(0xFFF5D0FE)
        // Vertical cylinder top + bottom end caps
        val vEndTop = Offset(vRodTL.x + rodW / 2f, vRodTL.y)
        val vEndBot = Offset(vRodTL.x + rodW / 2f, vRodTL.y + rodH)
        // small ellipse at each end to suggest the open end of the cylinder
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(vEndTop.x - rodW / 2f, vEndTop.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(vEndTop.x - rodW / 2f, vEndTop.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f), style = Stroke(0.8f))
        // 9 small ticks around the top opening
        for (i in 0 until 9) {
            val a = i * (2f * PI.toFloat() / 9f) + 0.2f
            val tickX = vEndTop.x + cos(a) * rodW * 0.42f
            val tickY = vEndTop.y + sin(a) * rodW * 0.15f
            drawCircle(tickCol, 1.2f, Offset(tickX, tickY))
        }
        // Bottom opening
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(vEndBot.x - rodW / 2f, vEndBot.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(vEndBot.x - rodW / 2f, vEndBot.y - rodW * 0.15f),
            size = Size(rodW, rodW * 0.30f), style = Stroke(0.8f))

        // Horizontal cylinder left + right end caps
        val hEndL = Offset(hRodTL.x, hRodTL.y + rodW / 2f)
        val hEndR = Offset(hRodTL.x + rodH, hRodTL.y + rodW / 2f)
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(hEndL.x - rodW * 0.15f, hEndL.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(hEndL.x - rodW * 0.15f, hEndL.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW), style = Stroke(0.8f))
        // 9 ticks around the right opening
        for (i in 0 until 9) {
            val a = i * (2f * PI.toFloat() / 9f) + 0.2f
            val tickX = hEndR.x + cos(a) * rodW * 0.15f
            val tickY = hEndR.y + sin(a) * rodW * 0.42f
            drawCircle(tickCol, 1.2f, Offset(tickX, tickY))
        }
        drawOval(color.copy(alpha = 0.85f),
            topLeft = Offset(hEndR.x - rodW * 0.15f, hEndR.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW))
        drawOval(Color(0xFF86198F),
            topLeft = Offset(hEndR.x - rodW * 0.15f, hEndR.y - rodW / 2f),
            size = Size(rodW * 0.30f, rodW), style = Stroke(0.8f))

        // Longitudinal striations on the cylinder bodies
        val str = Color(0xFFF5D0FE).copy(alpha = 0.75f)
        for (i in 1..2) {
            val xx = vRodTL.x + rodW * (i / 3f)
            drawLine(str,
                Offset(xx, vRodTL.y + 2f),
                Offset(xx, vRodTL.y + rodH - 2f),
                strokeWidth = 0.6f)
            val yy = hRodTL.y + rodW * (i / 3f)
            drawLine(str,
                Offset(hRodTL.x + 2f, yy),
                Offset(hRodTL.x + rodH - 2f, yy),
                strokeWidth = 0.6f)
        }
    }
    // outlines
    drawRect(Color(0xFF86198F), topLeft = vRodTL, size = Size(rodW, rodH), style = Stroke(0.8f))
    drawRect(Color(0xFF86198F), topLeft = hRodTL, size = Size(rodH, rodW), style = Stroke(0.8f))
}

private fun DrawScope.drawLysosome(c: Offset, r: Float, color: Color) {
    // Slightly irregular outline (lobed) drawn as path
    val brush = Brush.radialGradient(
        colors = listOf(color.copy(alpha = 0.95f), color, Color(0xFFC2410C)),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.3f,
    )
    val outline = Path().apply {
        val sides = 10
        for (i in 0 until sides) {
            val a = i * (2f * PI.toFloat() / sides)
            val rad = r * (1f + (if (i % 2 == 0) 0.05f else -0.05f))
            val x = c.x + cos(a) * rad
            val y = c.y + sin(a) * rad
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(outline, brush)
    drawPath(outline, Color(0xFF9A3412).copy(alpha = 0.75f), style = Stroke(1f))

    // Enzyme speckle cluster — denser, jittered cluster of small dark dots
    if (r > 5f) {
        val speck = Color(0xFF7C2D12).copy(alpha = 0.80f)
        val speckLight = Color(0xFFFEF3C7).copy(alpha = 0.6f)
        val dots = listOf(
            Offset(c.x - r * 0.30f, c.y - r * 0.10f) to r * 0.10f,
            Offset(c.x + r * 0.20f, c.y - r * 0.32f) to r * 0.09f,
            Offset(c.x + r * 0.35f, c.y + r * 0.10f) to r * 0.11f,
            Offset(c.x - r * 0.05f, c.y + r * 0.35f) to r * 0.09f,
            Offset(c.x - r * 0.40f, c.y + r * 0.18f) to r * 0.08f,
            Offset(c.x + r * 0.05f, c.y + r * 0.05f) to r * 0.07f,
            Offset(c.x - r * 0.15f, c.y - r * 0.30f) to r * 0.08f,
            Offset(c.x + r * 0.40f, c.y - r * 0.05f) to r * 0.07f,
        )
        dots.forEach { (pt, rad) ->
            drawCircle(speck, rad, pt)
            drawCircle(speckLight, rad * 0.45f, Offset(pt.x - rad * 0.3f, pt.y - rad * 0.3f))
        }
    }
}

private fun hitTestCell(
    tap: Offset,
    canvasW: Float,
    canvasH: Float,
    cellType: CellType,
): String? {
    val cx = canvasW / 2f
    val cy = canvasH / 2f
    val cellR = min(canvasW, canvasH) * 0.35f
    val instances = if (cellType == CellType.Plant) PLANT_LAYOUT else ANIMAL_LAYOUT
    val projected = instances.map { it to project(it.pos, cx, cy, cellR) }
        .sortedByDescending { it.second.zRot }
    for ((inst, p) in projected) {
        val k = kindOf(inst.kindId)
        val r = cellR * k.radiusUnit
        val hitR = r * 1.3f
        val d = Offset(tap.x - p.x, tap.y - p.y).getDistance()
        if (d < hitR) return k.id
    }
    return null
}

// ────────────────────────────────────────────────────────────────────
// Mode 2: Shapes — cell type gallery
// ────────────────────────────────────────────────────────────────────

private data class CellShape(
    val id: String, val name: String, val tagline: String, val detail: String,
    val drawer: DrawScope.(center: Offset, r: Float) -> Unit,
    val keyParts: List<String> = emptyList(),
)

private val CELL_SHAPES: List<CellShape> = listOf(
    CellShape(
        id = "rbc", name = "Red Blood Cell", tagline = "Biconcave disc",
        detail = "Flexible, no nucleus, biconcave so it has max surface area for picking up oxygen — and squeezes through tiny capillaries.",
        keyParts = listOf(
            "Biconcave disc shape",
            "No nucleus (more room for haemoglobin)",
            "Flexible — bends through tiny capillaries",
        ),
        drawer = { c, r ->
            // Three biconcave cells in a cluster
            val cells = listOf(
                Triple(Offset(c.x - r * 0.55f, c.y + r * 0.05f), r * 0.55f, 0f),
                Triple(Offset(c.x + r * 0.45f, c.y - r * 0.35f), r * 0.70f, 0.4f),
                Triple(Offset(c.x + r * 0.20f, c.y + r * 0.50f), r * 0.65f, -0.3f),
            )
            for ((pos, rad, rot) in cells) {
                // Outer disc with red radial gradient
                val outer = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFCA5A5),
                        Color(0xFFEF4444),
                        Color(0xFFB91C1C),
                    ),
                    center = Offset(pos.x - rad * 0.2f, pos.y - rad * 0.2f),
                    radius = rad * 1.3f,
                )
                drawOval(outer,
                    topLeft = Offset(pos.x - rad, pos.y - rad * (0.85f - rot * 0.05f)),
                    size = Size(rad * 2f, rad * (1.7f - rot * 0.1f)))
                // Inner dimple (darker center)
                drawOval(Color(0xFFB91C1C).copy(alpha = 0.55f),
                    topLeft = Offset(pos.x - rad * 0.5f, pos.y - rad * 0.3f),
                    size = Size(rad * 1.0f, rad * 0.6f))
                // Highlight glint top-left
                drawOval(Color(0xFFFEE2E2).copy(alpha = 0.55f),
                    topLeft = Offset(pos.x - rad * 0.85f, pos.y - rad * 0.7f),
                    size = Size(rad * 0.7f, rad * 0.25f))
            }
        },
    ),
    CellShape(
        id = "neuron", name = "Nerve Cell (Neuron)", tagline = "Long branching",
        detail = "Long axon carries electrical signals from one place to another in the body. The branches receive signals from other neurons.",
        keyParts = listOf(
            "Soma (cell body) — holds the nucleus",
            "Dendrites — receive incoming signals",
            "Axon — long fibre that carries the signal",
            "Myelin sheath — insulates the axon",
            "Axon terminals — pass the signal on",
        ),
        drawer = { c, r ->
            val somaC = Offset(c.x - r * 0.4f, c.y)
            val somaR = r * 0.45f
            // dendrite branches (5 main, each with 2-3 sub-branches)
            val dendCol = Color(0xFFEC4899).copy(alpha = 0.85f)
            for (i in 0 until 6) {
                val a = (i * 2 * PI.toFloat() / 6f) + PI.toFloat() * 1.05f   // skip the right side (axon side)
                if (a > PI.toFloat() * 0.55f && a < PI.toFloat() * 1.45f) continue  // gap on right
                val start = Offset(somaC.x + cos(a) * somaR * 0.9f, somaC.y + sin(a) * somaR * 0.9f)
                val mid = Offset(somaC.x + cos(a) * somaR * 1.6f, somaC.y + sin(a) * somaR * 1.6f)
                drawLine(dendCol, start, mid, strokeWidth = 3.5f, cap = StrokeCap.Round)
                // sub-branches
                for (b in 0 until 3) {
                    val branchA = a + (b - 1) * 0.4f
                    val tip = Offset(
                        mid.x + cos(branchA) * somaR * 0.7f,
                        mid.y + sin(branchA) * somaR * 0.7f,
                    )
                    drawLine(dendCol, mid, tip, strokeWidth = 2f, cap = StrokeCap.Round)
                    // tiny terminal
                    drawCircle(dendCol, 1.5f, tip)
                }
            }
            // soma (radial gradient)
            val somaBrush = Brush.radialGradient(
                colors = listOf(Color(0xFFFBCFE8), Color(0xFFEC4899), Color(0xFFBE185D)),
                center = Offset(somaC.x - somaR * 0.3f, somaC.y - somaR * 0.3f),
                radius = somaR * 1.2f,
            )
            drawCircle(somaBrush, somaR, somaC)
            // nucleus inside soma
            drawCircle(Color(0xFF9D174D).copy(alpha = 0.7f), somaR * 0.35f, somaC)
            drawCircle(Color(0xFFFCD34D), somaR * 0.10f, Offset(somaC.x + somaR * 0.05f, somaC.y))
            // axon — segmented myelin sheath (5 segments)
            val axonY = c.y
            val axonStartX = somaC.x + somaR * 0.9f
            val segCount = 5
            val segLen = (c.x + r * 1.2f - axonStartX) / segCount
            val myelin = Color(0xFFFCD34D)
            val myelinEdge = Color(0xFFB45309)
            for (i in 0 until segCount) {
                val x0 = axonStartX + i * segLen + segLen * 0.05f
                val x1 = axonStartX + (i + 1) * segLen - segLen * 0.05f
                drawOval(myelin,
                    topLeft = Offset(x0, axonY - r * 0.18f),
                    size = Size(x1 - x0, r * 0.36f))
                drawOval(myelinEdge,
                    topLeft = Offset(x0, axonY - r * 0.18f),
                    size = Size(x1 - x0, r * 0.36f),
                    style = Stroke(1f))
            }
            // axon terminals (3 small branches at the end)
            val termX = axonStartX + segCount * segLen
            for (b in 0 until 3) {
                val ay = axonY + (b - 1) * r * 0.18f
                drawLine(dendCol,
                    Offset(termX, axonY),
                    Offset(termX + r * 0.25f, ay),
                    strokeWidth = 2f, cap = StrokeCap.Round)
                drawCircle(dendCol, 2f, Offset(termX + r * 0.25f, ay))
            }
        },
    ),
    CellShape(
        id = "plant-leaf", name = "Plant Leaf Cell", tagline = "Rectangular brick",
        detail = "Boxy, rigid cell wall — packs neatly side by side so leaves can capture lots of sunlight per area.",
        keyParts = listOf(
            "Cell wall — fixed rectangular shape",
            "Chloroplasts — capture sunlight",
            "Large central vacuole — keeps the cell turgid",
            "Nucleus — pushed to one side by the vacuole",
        ),
        drawer = { c, r ->
            // Leaf silhouette (left side)
            val leafC = Offset(c.x - r * 0.85f, c.y)
            val leafR = r * 0.55f
            val leafBrush = Brush.radialGradient(
                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF15803D)),
                center = Offset(leafC.x - leafR * 0.3f, leafC.y - leafR * 0.3f),
                radius = leafR * 1.3f,
            )
            val leafPath = Path().apply {
                moveTo(leafC.x, leafC.y + leafR * 0.9f)
                cubicTo(
                    leafC.x - leafR * 1.1f, leafC.y + leafR * 0.4f,
                    leafC.x - leafR * 1.1f, leafC.y - leafR * 0.5f,
                    leafC.x - leafR * 0.1f, leafC.y - leafR * 0.95f,
                )
                cubicTo(
                    leafC.x + leafR * 0.5f, leafC.y - leafR,
                    leafC.x + leafR * 0.9f, leafC.y - leafR * 0.6f,
                    leafC.x + leafR * 0.2f, leafC.y + leafR * 0.9f,
                )
                close()
            }
            drawPath(leafPath, leafBrush)
            drawPath(leafPath, Color(0xFF14532D), style = Stroke(1.5f))
            // central vein
            drawLine(Color(0xFF14532D),
                Offset(leafC.x + leafR * 0.15f, leafC.y + leafR * 0.85f),
                Offset(leafC.x - leafR * 0.05f, leafC.y - leafR * 0.85f),
                strokeWidth = 1.2f)
            // Zoom trapezoid lines from leaf to cell
            val zoomCol = Color(0xFF065F46).copy(alpha = 0.35f)
            val cellLeft = c.x + r * 0.15f
            val cellTop = c.y - r * 0.65f
            val cellSize = r * 1.15f
            drawLine(zoomCol,
                Offset(leafC.x + leafR * 0.3f, leafC.y - leafR * 0.4f),
                Offset(cellLeft, cellTop), strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
            drawLine(zoomCol,
                Offset(leafC.x + leafR * 0.3f, leafC.y + leafR * 0.3f),
                Offset(cellLeft, cellTop + cellSize), strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
            // Cell square — cell wall + membrane + interior
            drawRect(Color(0xFFA7F3D0).copy(alpha = 0.75f),
                topLeft = Offset(cellLeft, cellTop), size = Size(cellSize, cellSize))
            drawRect(Color(0xFF065F46),
                topLeft = Offset(cellLeft, cellTop), size = Size(cellSize, cellSize),
                style = Stroke(2f))
            // central vacuole
            drawOval(Color(0xFFA5F3FC).copy(alpha = 0.6f),
                topLeft = Offset(cellLeft + cellSize * 0.18f, cellTop + cellSize * 0.18f),
                size = Size(cellSize * 0.64f, cellSize * 0.64f))
            // chloroplasts (5 ovals with grana dots) packed around vacuole
            val chloroCol = Color(0xFF15803D)
            val chloroPos = listOf(
                Offset(cellLeft + cellSize * 0.18f, cellTop + cellSize * 0.10f),
                Offset(cellLeft + cellSize * 0.78f, cellTop + cellSize * 0.18f),
                Offset(cellLeft + cellSize * 0.85f, cellTop + cellSize * 0.65f),
                Offset(cellLeft + cellSize * 0.15f, cellTop + cellSize * 0.75f),
                Offset(cellLeft + cellSize * 0.50f, cellTop + cellSize * 0.85f),
            )
            for (p in chloroPos) {
                drawOval(chloroCol,
                    topLeft = Offset(p.x - cellSize * 0.10f, p.y - cellSize * 0.05f),
                    size = Size(cellSize * 0.20f, cellSize * 0.10f))
                // grana dot
                drawCircle(Color(0xFF064E3B), cellSize * 0.018f, p)
            }
            // small nucleus
            drawCircle(Color(0xFF7C3AED),
                cellSize * 0.07f,
                Offset(cellLeft + cellSize * 0.50f, cellTop + cellSize * 0.50f))
        },
    ),
    CellShape(
        id = "amoeba", name = "Amoeba", tagline = "Irregular / changing",
        detail = "Has no fixed shape — pushes out pseudopodia (false-feet) to crawl and engulf food. Single-celled organism.",
        keyParts = listOf(
            "Pseudopodia — temporary 'false feet' for moving and feeding",
            "Nucleus — control centre",
            "Contractile vacuole — pumps out excess water",
            "Food vacuole — holds engulfed food being digested",
        ),
        drawer = { c, r ->
            // Irregular blob with multiple pseudopodia (4 sides)
            val path = Path().apply {
                moveTo(c.x + r, c.y - r * 0.1f)
                cubicTo(c.x + r * 1.3f, c.y - r * 0.7f,
                    c.x + r * 0.4f, c.y - r * 1.3f,
                    c.x + r * 0.1f, c.y - r * 0.9f)
                cubicTo(c.x - r * 0.2f, c.y - r * 1.3f,
                    c.x - r * 0.9f, c.y - r * 1.05f,
                    c.x - r * 0.95f, c.y - r * 0.45f)
                cubicTo(c.x - r * 1.4f, c.y - r * 0.2f,
                    c.x - r * 1.4f, c.y + r * 0.5f,
                    c.x - r * 0.7f, c.y + r * 0.7f)
                cubicTo(c.x - r * 0.85f, c.y + r * 1.3f,
                    c.x + r * 0.2f, c.y + r * 1.3f,
                    c.x + r * 0.4f, c.y + r * 0.8f)
                cubicTo(c.x + r * 1.2f, c.y + r * 1.1f,
                    c.x + r * 1.45f, c.y + r * 0.3f,
                    c.x + r, c.y - r * 0.1f)
                close()
            }
            // cytoplasm gradient fill
            val cytoBrush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF7DD3FC)),
                center = Offset(c.x - r * 0.1f, c.y - r * 0.1f),
                radius = r * 1.4f,
            )
            drawPath(path, cytoBrush)
            drawPath(path, Color(0xFF0369A1), style = Stroke(2f))
            // nucleus (purple sphere + small dot)
            val nucleusC = Offset(c.x - r * 0.1f, c.y - r * 0.15f)
            val nBrush = Brush.radialGradient(
                colors = listOf(Color(0xFFE9D5FF), Color(0xFF7C3AED)),
                center = Offset(nucleusC.x - r * 0.05f, nucleusC.y - r * 0.05f),
                radius = r * 0.32f,
            )
            drawCircle(nBrush, r * 0.22f, nucleusC)
            drawCircle(Color(0xFF6D28D9), r * 0.22f, nucleusC, style = Stroke(1f))
            drawCircle(Color(0xFFFCD34D), r * 0.05f,
                Offset(nucleusC.x + r * 0.04f, nucleusC.y))
            // contractile vacuoles (cyan circles)
            drawCircle(Color(0xFFA5F3FC).copy(alpha = 0.85f), r * 0.12f,
                Offset(c.x + r * 0.4f, c.y - r * 0.35f))
            drawCircle(Color(0xFF0EA5E9), r * 0.12f,
                Offset(c.x + r * 0.4f, c.y - r * 0.35f), style = Stroke(1f))
            // food vacuoles with speckle
            val foodC = Offset(c.x + r * 0.35f, c.y + r * 0.30f)
            drawCircle(Color(0xFFFEF3C7), r * 0.15f, foodC)
            drawCircle(Color(0xFFCA8A04), r * 0.15f, foodC, style = Stroke(1f))
            drawCircle(Color(0xFF92400E), r * 0.025f, Offset(foodC.x - r * 0.04f, foodC.y))
            drawCircle(Color(0xFF92400E), r * 0.025f, Offset(foodC.x + r * 0.05f, foodC.y - r * 0.03f))
            drawCircle(Color(0xFF92400E), r * 0.025f, Offset(foodC.x + r * 0.02f, foodC.y + r * 0.05f))
            // a small food particle in another vacuole
            drawCircle(Color(0xFFFEF3C7), r * 0.10f,
                Offset(c.x - r * 0.5f, c.y + r * 0.2f))
            drawCircle(Color(0xFFCA8A04), r * 0.10f,
                Offset(c.x - r * 0.5f, c.y + r * 0.2f), style = Stroke(1f))
        },
    ),
)

@Composable
private fun ShapesMode() {
    val t = LL.tokens
    var expanded by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 2x2 grid of cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()) {
            for (rowIdx in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    for (colIdx in 0 until 2) {
                        val i = rowIdx * 2 + colIdx
                        val shape = CELL_SHAPES[i]
                        val isExpanded = expanded == shape.id
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(t.surface)
                                .border(
                                    if (isExpanded) 2.dp else 1.dp,
                                    if (isExpanded) t.accent500 else t.line,
                                    RoundedCornerShape(16.dp),
                                )
                                .clickable {
                                    expanded = if (isExpanded) null else shape.id
                                }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(t.surface2),
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val r = min(size.width, size.height) * 0.32f
                                        shape.drawer(this, Offset(cx, cy), r)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    LLText(shape.name, color = t.ink50,
                                        size = 14.sp, weight = FontWeight.Bold)
                                    LLText(shape.tagline, color = t.ink400,
                                        size = 11.sp)
                                }
                            }
                            if (isExpanded) {
                                LLText(shape.detail, color = t.ink200,
                                    size = 12.sp, lineHeight = 16.sp)
                                if (shape.keyParts.isNotEmpty()) {
                                    Spacer(Modifier.height(2.dp))
                                    LLText("LOOK FOR", color = t.ink500, size = 10.sp,
                                        weight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                                    shape.keyParts.forEach { part ->
                                        LLText("• $part", color = t.ink400,
                                            size = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ────────────────────────────────────────────────────────────────────
// Mode 4: Compare — plant vs animal split-screen
// ────────────────────────────────────────────────────────────────────

@Composable
private fun CompareMode() {
    val t = LL.tokens
    var hCellWall by remember { mutableStateOf(true) }
    var hChloroplast by remember { mutableStateOf(true) }
    var hVacuole by remember { mutableStateOf(true) }
    var hCentriole by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompareCellPanel(
                title = "Plant cell",
                instances = PLANT_LAYOUT,
                isPlant = true,
                hCellWall = hCellWall, hChloroplast = hChloroplast,
                hVacuole = hVacuole, hCentriole = hCentriole,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            CompareCellPanel(
                title = "Animal cell",
                instances = ANIMAL_LAYOUT,
                isPlant = false,
                hCellWall = hCellWall, hChloroplast = hChloroplast,
                hVacuole = hVacuole, hCentriole = hCentriole,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleChip("Cell wall", hCellWall) { hCellWall = !hCellWall }
                ToggleChip("Chloroplasts", hChloroplast) { hChloroplast = !hChloroplast }
                ToggleChip("Large vacuole", hVacuole) { hVacuole = !hVacuole }
                ToggleChip("Centrioles", hCentriole) { hCentriole = !hCentriole }
            }
            LLText(
                "Plant cells have a cell wall, chloroplasts, and a large central vacuole. " +
                    "Animal cells have centrioles and many small vacuoles.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun ToggleChip(label: String, on: Boolean, onToggle: () -> Unit) {
    val t = LL.tokens
    val bg = if (on) t.accent50 else t.surface2
    val border = if (on) t.accent500 else t.line
    val fg = if (on) t.accent700 else t.ink500
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) { LLText(label, color = fg, size = 12.sp, weight = FontWeight.SemiBold) }
}

@Composable
private fun CompareCellPanel(
    title: String,
    instances: List<OrganelleInstance>,
    isPlant: Boolean,
    hCellWall: Boolean,
    hChloroplast: Boolean,
    hVacuole: Boolean,
    hCentriole: Boolean,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val textMeasurer = rememberTextMeasurer()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText(title.uppercase(), color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                val cx = w / 2f; val cy = h / 2f
                val cellR = min(w, h) * 0.34f
                drawCellOutline(
                    cx, cy, cellR,
                    isPlant = isPlant,
                    wallStrokeColor = if (hCellWall) Color(0xFFFCD34D) else Color(0xFF065F46),
                    wallStrokeWidth = if (hCellWall) 4f else 3f,
                )
                // Organelles
                for (inst in instances) {
                    val k = kindOf(inst.kindId)
                    val p = project(inst.pos, cx, cy, cellR)
                    val r = cellR * k.radiusUnit
                    val isHighlighted = (k.id == "chloroplast" && hChloroplast) ||
                        (k.id == "vacuole" && hVacuole) ||
                        (k.id == "centriole" && hCentriole)
                    if (isHighlighted) {
                        drawCircle(Color(0xFFFCD34D).copy(alpha = 0.45f),
                            radius = r * 1.7f,
                            center = Offset(p.x, p.y))
                    }
                    drawOrganelle(k, Offset(p.x, p.y), r)
                }
                // Persistent labels
                val labelTable = if (isPlant) PLANT_LABELS else ANIMAL_LABELS
                val labelled = mutableSetOf<String>()
                for (inst in instances) {
                    if (inst.kindId in labelled) continue
                    val place = labelTable[inst.kindId] ?: continue
                    val k = kindOf(inst.kindId)
                    val p = project(inst.pos, cx, cy, cellR)
                    val r = cellR * k.radiusUnit
                    val anchor = Offset(p.x + place.anchorDx * r, p.y + place.anchorDy * r)
                    val labelEnd = Offset(cx + place.labelX * cellR, cy + place.labelY * cellR)
                    drawLabel(textMeasurer, k.name, anchor, labelEnd,
                        color = Color(0xFF52525B))
                    labelled += inst.kindId
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Mode: Detail — large fully-labelled cell diagram (NCERT-style)
// ────────────────────────────────────────────────────────────────────

@Composable
private fun DetailMode() {
    val t = LL.tokens
    var cellType by remember { mutableStateOf(CellType.Plant) }
    var selectedKindId by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Diagram surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(12.dp))
                .pointerInput(cellType) {
                    detectTapGestures { tap ->
                        selectedKindId = hitTestCell(
                            tap = tap,
                            canvasW = size.width.toFloat(),
                            canvasH = size.height.toFloat(),
                            cellType = cellType,
                        )
                    }
                },
        ) {
            CellSceneCanvas(
                cellType = cellType,
                selectedKindId = selectedKindId,
                pulse = 0.6f,
                showLabels = true,
            )
        }

        // Right rail — type toggle + long detail
        Column(
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Plant / Animal toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText("CELL TYPE", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(CellType.Plant to "Plant", CellType.Animal to "Animal")
                        .forEach { (ct, label) ->
                            val sel = cellType == ct
                            val bg = if (sel) t.accent600 else t.surface2
                            val fg = if (sel) Color.White else t.ink200
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bg)
                                    .clickable {
                                        cellType = ct
                                        selectedKindId = null
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                LLText(label, color = fg, size = 13.sp,
                                    weight = FontWeight.SemiBold)
                            }
                        }
                }
            }
            // Detail panel
            DetailInfoPanel(selectedKindId = selectedKindId, cellType = cellType)
        }
    }
}

@Composable
private fun DetailInfoPanel(selectedKindId: String?, cellType: CellType) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface)
            .border(1.dp, t.line, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LLText("ORGANELLE", color = t.ink500, size = 11.sp,
            weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
        if (selectedKindId == null) {
            LLText("Tap any organelle in the cell to read about it.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(4.dp))
            LLText(
                if (cellType == CellType.Plant)
                    "Plant cell — note the cell wall, large central vacuole, and chloroplasts. " +
                        "Animal cells lack all three."
                else
                    "Animal cell — note the centrioles, many small vacuoles, and lysosomes. " +
                        "Plant cells don't have these.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp,
            )
            return@Column
        }
        val k = kindOf(selectedKindId)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(k.color),
            )
            LLText(k.name, color = t.ink50, size = 17.sp, weight = FontWeight.Bold)
        }
        LLText(k.role, color = t.ink200, size = 13.sp, lineHeight = 18.sp,
            weight = FontWeight.SemiBold)
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        LLText(k.detail, color = t.ink400, size = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(2.dp))
        LLText(
            when {
                k.presentInPlant && k.presentInAnimal -> "Present in: Plant · Animal"
                k.presentInPlant -> "Present in: Plant only"
                else -> "Present in: Animal only"
            },
            color = t.ink500, size = 11.sp,
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Mode 5: Table — organelle comparison
// ────────────────────────────────────────────────────────────────────

@Composable
private fun TableMode() {
    val t = LL.tokens
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(12.dp)),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.surface2)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                LLText("ORGANELLE", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
                    modifier = Modifier.weight(2f))
                LLText("PLANT", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
                    modifier = Modifier.width(70.dp))
                LLText("ANIMAL", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
                    modifier = Modifier.width(80.dp))
                LLText("FUNCTION", color = t.ink500, size = 11.sp,
                    weight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
                    modifier = Modifier.weight(5f))
            }
            // Rows
            KINDS.forEachIndexed { i, k ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (i % 2 == 0) t.surface else t.surface2.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(k.name, color = t.ink50, size = 13.sp,
                        weight = FontWeight.SemiBold,
                        modifier = Modifier.weight(2f))
                    LLText(if (k.presentInPlant) "✓" else "✗",
                        color = if (k.presentInPlant) Color(0xFF10B981) else t.ink600,
                        size = 16.sp, weight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp))
                    LLText(if (k.presentInAnimal) "✓" else "✗",
                        color = if (k.presentInAnimal) Color(0xFF10B981) else t.ink600,
                        size = 16.sp, weight = FontWeight.Bold,
                        modifier = Modifier.width(80.dp))
                    LLText(k.role, color = t.ink200, size = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(5f))
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Info popover
// ────────────────────────────────────────────────────────────────────

@Composable
private fun WhatIsACellPopover(onDismiss: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.lineStrong, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .clickable(enabled = false, onClick = {}),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LLText("WHAT IS A CELL?",
                color = t.accent700, size = 12.sp,
                weight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            LLText(
                "A cell is the basic structural and functional unit of all living things. " +
                    "Every plant, animal, and microbe is made of one or many cells working together.",
                color = t.ink200, size = 14.sp, lineHeight = 20.sp,
            )
            LLText("• Plant cells: cell wall, chloroplasts, large central vacuole.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            LLText("• Animal cells: centrioles, lysosomes, many small vacuoles. No cell wall.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            LLText("• Both share: nucleus, mitochondria, ribosomes, ER, Golgi, cell membrane.",
                color = t.ink400, size = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                SecondaryButton("Got it", onClick = onDismiss)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Helpers (text in Canvas)
// ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawTextAt(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = size, fontWeight = weight),
    )
    drawText(textLayoutResult = layout, topLeft = topLeft)
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    anchor: Offset,
    labelEnd: Offset,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
) {
    drawLine(color.copy(alpha = 0.70f), anchor, labelEnd, strokeWidth = 1f)
    drawCircle(color, 2.2f, anchor)
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = color, fontSize = fontSize, fontWeight = FontWeight.SemiBold),
    )
    val pad = 4f
    val textTopLeft = if (labelEnd.x >= anchor.x) {
        Offset(labelEnd.x + pad, labelEnd.y - layout.size.height / 2f)
    } else {
        Offset(labelEnd.x - pad - layout.size.width, labelEnd.y - layout.size.height / 2f)
    }
    drawText(textLayoutResult = layout, topLeft = textTopLeft)
}
