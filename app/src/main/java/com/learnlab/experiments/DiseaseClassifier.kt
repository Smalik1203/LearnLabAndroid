package com.learnlab.experiments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLSlider
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.design.SecondaryButton
import com.learnlab.store.ExperimentControls
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

// ───────────────────────── data model ─────────────────────────

private enum class Kind { Communicable, NonCommunicable }

private data class DiseaseCard(
    val id: String,
    val name: String,
    val emoji: String,
    val kind: Kind,
    val agent: String,
    val transmission: String,
    val prevention: String,
)

private val DISEASES: List<DiseaseCard> = listOf(
    DiseaseCard("flu", "Common cold / Flu", "🤧", Kind.Communicable,
        "Virus", "Air — cough and sneeze droplets",
        "Wash hands, cover mouth, wear a mask"),
    DiseaseCard("chickenpox", "Chickenpox", "🟢", Kind.Communicable,
        "Virus (Varicella zoster)", "Air and direct contact",
        "Vaccination; isolate the patient"),
    DiseaseCard("tb", "Tuberculosis (TB)", "🫁", Kind.Communicable,
        "Bacteria (Mycobacterium tuberculosis)", "Air — droplet nuclei",
        "BCG vaccine; avoid close contact"),
    DiseaseCard("cholera", "Cholera", "💧", Kind.Communicable,
        "Bacteria (Vibrio cholerae)", "Contaminated water and food",
        "Boiled water, sanitation, vaccine"),
    DiseaseCard("typhoid", "Typhoid", "🍽️", Kind.Communicable,
        "Bacteria (Salmonella typhi)", "Contaminated water and food",
        "Safe food, hygiene, typhoid vaccine"),
    DiseaseCard("hepa", "Hepatitis A", "🟡", Kind.Communicable,
        "Virus (Hepatitis A virus)", "Contaminated water and food",
        "Boiled water, sanitation, vaccine"),
    DiseaseCard("malaria", "Malaria", "🦟", Kind.Communicable,
        "Protozoa (Plasmodium)", "Female Anopheles mosquito (vector)",
        "Mosquito nets, repellents, drain still water"),
    DiseaseCard("dengue", "Dengue", "🦠", Kind.Communicable,
        "Virus (Dengue virus)", "Aedes mosquito (vector)",
        "Drain still water, nets, repellents"),
    DiseaseCard("covid", "COVID-19", "😷", Kind.Communicable,
        "Virus (SARS-CoV-2)", "Air — droplets and aerosols",
        "Mask, vaccination, ventilation"),

    DiseaseCard("diabetes", "Diabetes", "🩸", Kind.NonCommunicable,
        "Lifestyle and genetics", "Does not spread between people",
        "Balanced diet, regular exercise"),
    DiseaseCard("hypert", "Hypertension", "❤️", Kind.NonCommunicable,
        "Lifestyle, salt, stress", "Does not spread between people",
        "Low salt, exercise, manage stress"),
    DiseaseCard("asthma", "Asthma", "🌬️", Kind.NonCommunicable,
        "Environmental triggers", "Does not spread between people",
        "Avoid triggers, prescribed inhaler"),
    DiseaseCard("obesity", "Obesity", "⚖️", Kind.NonCommunicable,
        "Diet and low activity", "Does not spread between people",
        "Balanced diet, regular exercise"),
    DiseaseCard("cancer", "Cancer", "🎗️", Kind.NonCommunicable,
        "Multifactorial (genetics + lifestyle)", "Does not spread between people",
        "Avoid tobacco, screening, healthy lifestyle"),
)

private enum class Bucket { Pool, Communicable, NonCommunicable }

// ───────────────────────── root composable ─────────────────────────

@Composable
fun DiseaseClassifier(controls: ExperimentControls) {
    val t = LL.tokens
    var page by remember { mutableStateOf(0) }
    var sortedCount by remember { mutableStateOf(0) }
    var simRan by remember { mutableStateOf(false) }

    LaunchedEffect(page, sortedCount, simRan) {
        val pageFraction = when (page) {
            0 -> (sortedCount / DISEASES.size.toFloat()) * 0.5f
            else -> 0.5f + if (simRan) 0.5f else 0f
        }
        controls.onProgress(pageFraction.coerceIn(0f, 1f))
        if (page == 1 && simRan) controls.onComplete(1f)
    }

    Box(Modifier.fillMaxSize().padding(20.dp)) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeaderStrip(page, sortedCount)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    0 -> ClassifyAndLearnPage(onSortedCountChange = { sortedCount = it })
                    else -> SimulatePage(onSimComplete = { simRan = true })
                }
            }
            PageNavRow(page, 2) { page = it }
        }
    }
}

@Composable
private fun HeaderStrip(page: Int, sortedCount: Int) {
    val t = LL.tokens
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            val title = if (page == 0) "PART 1 · CLASSIFY AND LEARN" else "PART 2 · WATCH AN OUTBREAK"
            LLText(title, color = t.ink50, size = 12.sp,
                weight = FontWeight.Bold, letterSpacing = 1.6.sp)
            LLText("Grade 8 · Chapter 3 — Health: The Ultimate Treasure · §3.4",
                color = t.ink500, size = 10.sp)
        }
        if (page == 0) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                LLText(
                    "$sortedCount of ${DISEASES.size} sorted",
                    color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PageNavRow(page: Int, total: Int, onPage: (Int) -> Unit) {
    val t = LL.tokens
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val canPrev = page > 0
        val canNext = page < total - 1
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (canPrev) t.accent500 else t.surface2)
                .border(1.dp, if (canPrev) t.accent500 else t.line, RoundedCornerShape(8.dp))
                .then(if (canPrev) Modifier.clickable { onPage(page - 1) } else Modifier)
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            LLText("←", color = if (canPrev) Color.White else t.ink500, size = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                Box(
                    Modifier
                        .size(if (i == page) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (i == page) t.accent500 else t.line),
                )
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (canNext) t.accent500 else t.surface2)
                .border(1.dp, if (canNext) t.accent500 else t.line, RoundedCornerShape(8.dp))
                .then(if (canNext) Modifier.clickable { onPage(page + 1) } else Modifier)
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            LLText("→", color = if (canNext) Color.White else t.ink500, size = 14.sp)
        }
    }
}

// ───────────────────────── Page 1 — Classify & Learn ─────────────────────────

@Composable
private fun ClassifyAndLearnPage(onSortedCountChange: (Int) -> Unit) {
    val t = LL.tokens
    val placement = remember {
        mutableStateListOf<Bucket>().apply { repeat(DISEASES.size) { add(Bucket.Pool) } }
    }
    var focused by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var wrongFlash by remember { mutableStateOf<Bucket?>(null) }

    val sortedCount = placement.count { it != Bucket.Pool }
    LaunchedEffect(sortedCount) { onSortedCountChange(sortedCount) }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(1500)
            feedback = null
        }
    }
    LaunchedEffect(wrongFlash) {
        if (wrongFlash != null) {
            delay(800)
            wrongFlash = null
        }
    }

    fun tryPlace(target: Bucket) {
        val f = focused ?: return
        val correct = (DISEASES[f].kind == Kind.Communicable && target == Bucket.Communicable) ||
            (DISEASES[f].kind == Kind.NonCommunicable && target == Bucket.NonCommunicable)
        if (correct) {
            placement[f] = target
            val label = if (target == Bucket.Communicable) "Communicable" else "Non-communicable"
            feedback = "✓ Correct! ${DISEASES[f].name} — $label."
            focused = null
        } else {
            wrongFlash = target
            feedback = "✗ Not quite — try the other bucket."
        }
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Feedback strip (always present so layout doesn't jump)
        FeedbackStrip(message = feedback, sortedCount = sortedCount, focusedIdx = focused)

        // Main row: bucket | bucket | detail
        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BucketColumn(
                step = 1,
                title = "Communicable",
                subtitle = "Caused by pathogens; spreads",
                wrongFlash = wrongFlash == Bucket.Communicable,
                bucket = Bucket.Communicable,
                placement = placement,
                focused = focused,
                expectedKind = Kind.Communicable,
                onChipTap = { focused = it },
                onBucketTap = { tryPlace(Bucket.Communicable) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            BucketColumn(
                step = 2,
                title = "Non-communicable",
                subtitle = "Lifestyle, diet, or environment",
                wrongFlash = wrongFlash == Bucket.NonCommunicable,
                bucket = Bucket.NonCommunicable,
                placement = placement,
                focused = focused,
                expectedKind = Kind.NonCommunicable,
                onChipTap = { focused = it },
                onBucketTap = { tryPlace(Bucket.NonCommunicable) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            DetailColumn(
                focused = focused?.let { DISEASES[it] },
                onPlaceComm = { tryPlace(Bucket.Communicable) },
                onPlaceNonComm = { tryPlace(Bucket.NonCommunicable) },
                modifier = Modifier.weight(1.1f).fillMaxHeight(),
            )
        }

        // Bottom: unsorted pool
        PoolStrip(
            placement = placement,
            focused = focused,
            onTap = { focused = it },
        )
    }
}

@Composable
private fun FeedbackStrip(message: String?, sortedCount: Int, focusedIdx: Int?) {
    val t = LL.tokens
    val allDone = sortedCount == DISEASES.size
    val text: String
    val color: Color
    when {
        message != null -> {
            text = message
            color = if (message.startsWith("✗")) t.rose700 else t.accent700
        }
        allDone -> {
            text = "All ${DISEASES.size} sorted — continue to the outbreak →"
            color = t.accent700
        }
        focusedIdx != null -> {
            text = "Now tap a bucket to classify ${DISEASES[focusedIdx].name}."
            color = t.ink200
        }
        else -> {
            text = "Tap any disease below to see its details and start classifying."
            color = t.ink200
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        LLText(text, color = color, size = 13.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun BucketColumn(
    step: Int,
    title: String,
    subtitle: String,
    wrongFlash: Boolean,
    bucket: Bucket,
    placement: List<Bucket>,
    focused: Int?,
    expectedKind: Kind,
    onChipTap: (Int) -> Unit,
    onBucketTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val accent = if (expectedKind == Kind.Communicable) t.amber700 else t.accent700
    val bg = if (expectedKind == Kind.Communicable) t.amber50 else t.accent50
    val expectedTotal = DISEASES.count { it.kind == expectedKind }
    val placedNow = placement.indices.count { placement[it] == bucket }
    val sortedIndices = placement.indices.filter { placement[it] == bucket }
    val borderColor = when {
        wrongFlash -> t.rose700
        focused != null -> accent
        else -> t.lineStrong
    }
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (wrongFlash) t.rose50 else bg)
            .border(if (wrongFlash) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = focused != null) { onBucketTap() }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                LLText("$step", color = Color.White, size = 12.sp, weight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                LLText(title.uppercase(), color = accent, size = 12.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.4.sp)
                LLText(subtitle, color = t.ink400, size = 11.sp)
            }
            LLText("$placedNow / $expectedTotal", color = accent, size = 13.sp,
                weight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = 0.25f)))
        // Sorted chips (vertical list)
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (sortedIndices.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                LLText(
                    if (focused != null) "Tap here to drop the focused card."
                    else "(empty — drop sorted diseases here)",
                    color = t.ink400, size = 12.sp,
                )
            } else {
                sortedIndices.forEach { i ->
                    SortedChip(disease = DISEASES[i], accent = accent,
                        isFocused = focused == i, onClick = { onChipTap(i) })
                }
            }
        }
    }
}

@Composable
private fun SortedChip(disease: DiseaseCard, accent: Color, isFocused: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFocused) accent.copy(alpha = 0.20f) else t.surface)
            .border(if (isFocused) 2.dp else 1.dp, if (isFocused) accent else accent.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(disease.emoji, size = 14.sp, color = Color.Unspecified)
        LLText(disease.name, color = t.ink50, size = 12.sp,
            weight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
        LLText("✓", color = accent, size = 12.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun DetailColumn(
    focused: DiseaseCard?,
    onPlaceComm: () -> Unit,
    onPlaceNonComm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A3A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LLText("DETAILS", color = Color.White.copy(alpha = 0.70f), size = 10.sp,
            weight = FontWeight.Bold, letterSpacing = 1.4.sp)
        if (focused == null) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LLText(
                    "Tap any disease below or already-sorted to see its causative agent, " +
                        "how it spreads, and how to prevent it.",
                    color = Color.White.copy(alpha = 0.80f), size = 13.sp, lineHeight = 18.sp,
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LLText(focused.emoji, size = 28.sp, color = Color.Unspecified)
                Column(Modifier.weight(1f)) {
                    LLText(focused.name, color = Color.White, size = 16.sp, weight = FontWeight.Bold)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.20f)))
            DarkDetailRow("Causative agent", focused.agent)
            DarkDetailRow("How it spreads", focused.transmission)
            DarkDetailRow("Prevention", focused.prevention)
            Spacer(Modifier.weight(1f))
            // Two big action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailActionButton(
                    label = "→ Communicable",
                    // Hardcoded amber-700: stays dark in both themes for white-on-amber readability.
                    // t.amber700 now flips to bright amber-300 in dark mode, which would blow out
                    // against white text on a button background.
                    color = Color(0xFFB45309),
                    onClick = onPlaceComm,
                    modifier = Modifier.weight(1f),
                )
                DetailActionButton(
                    label = "→ Non-communicable",
                    color = t.accent500,
                    onClick = onPlaceNonComm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DarkDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LLText(label.uppercase(), color = Color.White.copy(alpha = 0.70f), size = 10.sp,
            weight = FontWeight.Bold, letterSpacing = 1.2.sp)
        LLText(value, color = Color.White, size = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun DetailActionButton(label: String, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        LLText(label, color = Color.White, size = 11.sp, weight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoolStrip(placement: List<Bucket>, focused: Int?, onTap: (Int) -> Unit) {
    val t = LL.tokens
    val poolIndices = placement.indices.filter { placement[it] == Bucket.Pool }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .border(1.dp, t.line, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("UNSORTED", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f))
            LLText("${poolIndices.size} left", color = t.ink400, size = 11.sp)
        }
        if (poolIndices.isEmpty()) {
            LLText(
                "Pool is empty — every disease has been classified.",
                color = t.ink500, size = 12.sp,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                poolIndices.forEach { i ->
                    PoolChip(
                        disease = DISEASES[i],
                        isFocused = focused == i,
                        onClick = { onTap(i) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PoolChip(disease: DiseaseCard, isFocused: Boolean, onClick: () -> Unit) {
    val t = LL.tokens
    // Note: pool chips intentionally do NOT show kind colour — students must
    // classify them, not be given the answer.
    val border = if (isFocused) t.accent500 else t.lineStrong
    val bg = if (isFocused) t.accent50 else t.surface
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LLText(disease.emoji, size = 14.sp, color = Color.Unspecified)
        LLText(
            disease.name,
            color = if (isFocused) t.accent700 else t.ink50,
            size = 12.sp,
            weight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

// ───────────────────────── Page 2 — Simulate ─────────────────────────

private enum class CellState { Susceptible, Infected, Recovered }

private const val GRID_COLS = 6
private const val GRID_ROWS = 5
private const val TOTAL_STUDENTS = GRID_COLS * GRID_ROWS  // 30
private const val SEED_INDEX = 14                          // row 2, col 2
private const val SIM_DAYS = 14
private const val BASE_P = 0.30f
private const val MASK_EFF = 0.55f
private const val VENT_EFF = 0.45f
private const val HYG_EFF = 0.30f
private const val VAX_EFF = 0.80f
private const val RECOVERY_DAYS = 5

@Composable
private fun SimulatePage(onSimComplete: () -> Unit) {
    val t = LL.tokens
    var mask by remember { mutableStateOf(0.5f) }
    var ventilation by remember { mutableStateOf(0.5f) }
    var vaccination by remember { mutableStateOf(0.5f) }
    var hygiene by remember { mutableStateOf(0.5f) }
    var students by remember { mutableStateOf(initialStudents()) }
    var infectedSince by remember { mutableStateOf(initialInfectedSince()) }
    var day by remember { mutableStateOf(0) }
    val dailyInfected = remember { mutableStateListOf<Int>() }
    var running by remember { mutableStateOf(false) }
    var runId by remember { mutableStateOf(0) }

    LaunchedEffect(runId) {
        if (runId == 0) return@LaunchedEffect
        running = true
        dailyInfected.clear()
        dailyInfected.add(students.count { it == CellState.Infected })
        for (d in 1..SIM_DAYS) {
            delay(350)
            val (newStates, newSince) = stepOneDay(
                students, infectedSince, d,
                mask, ventilation, vaccination, hygiene,
            )
            students = newStates
            infectedSince = newSince
            day = d
            dailyInfected.add(students.count { it == CellState.Infected })
        }
        running = false
        onSimComplete()
    }

    fun reset() {
        running = false
        runId = 0
        day = 0
        students = initialStudents()
        infectedSince = initialInfectedSince()
        dailyInfected.clear()
    }

    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Left: classroom grid + curve
        Column(
            Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Classroom grid
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                ClassroomGrid(students = students, day = day)
            }
            // Epidemic curve
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                EpidemicCurve(dailyInfected, day, students)
            }
        }
        // Right: sliders + buttons + readout
        Column(
            Modifier.width(280.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LLText(
                    "PREVENTION MEASURES",
                    color = t.ink500, size = 10.sp,
                    weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                )
                LLSlider("Mask wearing", mask, { mask = it }, min = 0f, max = 1f, unit = "")
                LLSlider("Ventilation", ventilation, { ventilation = it }, min = 0f, max = 1f, unit = "")
                LLSlider("Vaccination", vaccination, { vaccination = it }, min = 0f, max = 1f, unit = "")
                LLSlider("Hand hygiene", hygiene, { hygiene = it }, min = 0f, max = 1f, unit = "")
            }
            PrimaryButton(
                label = if (running) "Running… day $day / $SIM_DAYS" else "Run 14 days →",
                onClick = { runId++ },
                enabled = !running,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                label = "Reset classroom",
                onClick = { reset() },
                modifier = Modifier.fillMaxWidth(),
            )
            ReadoutCard(students, dailyInfected, day)
        }
    }
}

@Composable
private fun ClassroomGrid(students: List<CellState>, day: Int) {
    val t = LL.tokens
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("CLASSROOM · DAY $day", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f))
            LegendDot(Color(0xFF10B981), "Healthy")
            Spacer(Modifier.width(8.dp))
            LegendDot(Color(0xFFE11D48), "Infected")
            Spacer(Modifier.width(8.dp))
            LegendDot(Color(0xFF6366F1), "Recovered")
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                for (r in 0 until GRID_ROWS) {
                    Row(
                        Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (c in 0 until GRID_COLS) {
                            val idx = r * GRID_COLS + c
                            StudentDot(state = students[idx], modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentDot(state: CellState, modifier: Modifier) {
    val color = when (state) {
        CellState.Susceptible -> Color(0xFF10B981)
        CellState.Infected -> Color(0xFFE11D48)
        CellState.Recovered -> Color(0xFF6366F1)
    }
    val scale by animateFloatAsState(
        targetValue = if (state == CellState.Infected) 1.15f else 1f,
        animationSpec = tween(300),
        label = "dot-scale",
    )
    Box(
        modifier.padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(28.dp * scale)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val t = LL.tokens
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        LLText(label, color = t.ink400, size = 10.sp)
    }
}

@Composable
private fun EpidemicCurve(
    dailyInfected: List<Int>,
    day: Int,
    students: List<CellState>,
) {
    val t = LL.tokens
    val peak = dailyInfected.maxOrNull() ?: 0
    val peakDay = dailyInfected.indexOf(peak).coerceAtLeast(0)
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LLText("EPIDEMIC CURVE", color = t.ink500, size = 10.sp,
                weight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f))
            if (dailyInfected.isNotEmpty()) {
                LLText("Peak: $peak on day $peakDay",
                    color = t.ink400, size = 11.sp, weight = FontWeight.SemiBold)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawEpidemicCurve(dailyInfected, t.line, t.accent500, t.ink500)
            }
        }
    }
    @Suppress("UNUSED_PARAMETER")
    val unused = students
    @Suppress("UNUSED_PARAMETER")
    val unusedDay = day
}

private fun DrawScope.drawEpidemicCurve(
    daily: List<Int>,
    gridColor: Color,
    lineColor: Color,
    axisColor: Color,
) {
    val w = size.width
    val h = size.height
    val padLeft = 24f
    val padBottom = 18f
    val padTop = 6f
    val padRight = 6f
    val plotW = w - padLeft - padRight
    val plotH = h - padTop - padBottom
    drawLine(axisColor, Offset(padLeft, h - padBottom),
        Offset(w - padRight, h - padBottom), strokeWidth = 1f)
    drawLine(axisColor, Offset(padLeft, padTop),
        Offset(padLeft, h - padBottom), strokeWidth = 1f)
    for (yval in listOf(10, 20, 30)) {
        val y = h - padBottom - (yval / 30f) * plotH
        drawLine(gridColor.copy(alpha = 0.4f),
            Offset(padLeft, y), Offset(w - padRight, y), strokeWidth = 0.6f)
    }
    if (daily.size < 2) return
    val maxDays = SIM_DAYS.toFloat()
    val path = Path().apply {
        daily.forEachIndexed { i, infected ->
            val x = padLeft + (i / maxDays) * plotW
            val y = h - padBottom - (infected / 30f) * plotH
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }
    drawPath(path, lineColor, style = Stroke(2.5f, cap = StrokeCap.Round))
    daily.forEachIndexed { i, infected ->
        val x = padLeft + (i / maxDays) * plotW
        val y = h - padBottom - (infected / 30f) * plotH
        drawCircle(lineColor, 3f, Offset(x, y))
    }
}

@Composable
private fun ReadoutCard(students: List<CellState>, daily: List<Int>, day: Int) {
    val t = LL.tokens
    val infected = students.count { it == CellState.Infected }
    val recovered = students.count { it == CellState.Recovered }
    val healthy = students.count { it == CellState.Susceptible }
    val totalEverInfected = recovered + infected
    val peak = daily.maxOrNull() ?: 0
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A3A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LLText("OUTBREAK READOUT", color = Color.White.copy(alpha = 0.55f),
            size = 10.sp, weight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(2.dp))
        StatLine("Day", "$day / $SIM_DAYS")
        StatLine("Healthy", "$healthy")
        StatLine("Infected now", "$infected")
        StatLine("Recovered", "$recovered")
        StatLine("Ever infected", "$totalEverInfected / $TOTAL_STUDENTS")
        StatLine("Peak", "$peak")
    }
    @Suppress("UNUSED_VARIABLE") val u = t
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LLText(label, color = Color.White.copy(alpha = 0.65f), size = 11.sp,
            modifier = Modifier.weight(1f))
        LLText(value, color = Color.White, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

// ───────────────────────── sim model ─────────────────────────

private fun initialStudents(): List<CellState> =
    List(TOTAL_STUDENTS) { i -> if (i == SEED_INDEX) CellState.Infected else CellState.Susceptible }

private fun initialInfectedSince(): List<Int> =
    List(TOTAL_STUDENTS) { i -> if (i == SEED_INDEX) 0 else -1 }

private fun neighbours8(index: Int): List<Int> {
    val r = index / GRID_COLS
    val c = index % GRID_COLS
    val out = ArrayList<Int>(8)
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val nr = r + dr
        val nc = c + dc
        if (nr in 0 until GRID_ROWS && nc in 0 until GRID_COLS) {
            out.add(nr * GRID_COLS + nc)
        }
    }
    return out
}

private fun stepOneDay(
    states: List<CellState>,
    infectedSince: List<Int>,
    today: Int,
    mask: Float,
    ventilation: Float,
    vaccination: Float,
    hygiene: Float,
): Pair<List<CellState>, List<Int>> {
    val newStates = states.toMutableList()
    val newSince = infectedSince.toMutableList()

    for (i in states.indices) {
        if (states[i] == CellState.Infected) {
            val since = infectedSince[i]
            if (since >= 0 && today - since >= RECOVERY_DAYS) {
                newStates[i] = CellState.Recovered
            }
        }
    }

    val pPerNeighbour = BASE_P *
        (1f - mask * MASK_EFF) *
        (1f - ventilation * VENT_EFF) *
        (1f - hygiene * HYG_EFF)
    val pPersonal = (pPerNeighbour * (1f - vaccination * VAX_EFF)).coerceIn(0f, 1f)

    for (i in states.indices) {
        if (states[i] != CellState.Susceptible) continue
        val infectedNeighbours = neighbours8(i).count { states[it] == CellState.Infected }
        if (infectedNeighbours == 0) continue
        val pSafe = (1f - pPersonal).pow(infectedNeighbours.toFloat())
        if (Random.nextFloat() > pSafe) {
            newStates[i] = CellState.Infected
            newSince[i] = today
        }
    }
    return newStates to newSince
}

@Suppress("unused")
private val unusedMax = max(0, 0)
