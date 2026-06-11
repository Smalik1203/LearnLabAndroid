package com.learnlab.ui.grade

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.MeshBackground
import com.learnlab.design.Radius
import com.learnlab.design.bounceClickable
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

private data class GradeOption(
    val grade: Int,
    val label: String,
    val tagline: String,
    val available: Boolean,
)

private val GRADE_OPTIONS = listOf(
    GradeOption(1, "Class 1", "Sights, sounds, the world around us.", false),
    GradeOption(2, "Class 2", "Plants, animals, weather, machines.", false),
    GradeOption(3, "Class 3", "Living vs non-living, magnets, water.", false),
    GradeOption(4, "Class 4", "Food, shelter, transport, environment.", false),
    GradeOption(5, "Class 5", "Solids, liquids, gases, the body.", false),
    GradeOption(6, "Class 6", "The basics — observe, group, test.", true),
    GradeOption(7, "Class 7", "Acids & bases, weather, motion.", false),
    GradeOption(8, "Class 8", "Cells, magnetism, motors, light.", true),
    GradeOption(9, "Class 9", "Motion, atoms, sound, ecosystems.", true),
    GradeOption(10, "Class 10", "Reactions, electricity, evolution.", false),
)

@Composable
fun GradeSelectScreen(
    state: AppState,
    subject: String,
    onGradeSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val t = LL.tokens
    var animateTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopBar(
                state = state,
                title = "Back to Homepage",
                showBack = true,
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column {
                    LLText(
                        "Pick a Class Level",
                        color = t.ink50,
                        size = 28.sp,
                        weight = FontWeight.ExtraBold,
                    )
                    LLText(
                        "Choose the grade context for the labs you want to run.",
                        color = t.ink400,
                        size = 14.sp,
                        weight = FontWeight.Medium
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GRADE_OPTIONS.chunked(5).forEachIndexed { rowIndex, rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            rowOptions.forEachIndexed { colIndex, opt ->
                                val cardIndex = rowIndex * 5 + colIndex
                                GradeCard(
                                    option = opt,
                                    index = cardIndex,
                                    animateTrigger = animateTrigger,
                                    modifier = Modifier.weight(1f).height(150.dp),
                                    onClick = { if (opt.available) onGradeSelected(opt.grade) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeCard(
    option: GradeOption,
    index: Int,
    animateTrigger: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    val accent = if (option.available) t.accent500 else t.accent500.copy(alpha = 0.3f)
    val cardBg = if (option.available) t.surface.copy(alpha = 0.35f) else t.surface.copy(alpha = 0.12f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (option.available) 0.15f else 0.05f),
            Color.Transparent
        )
    )

    // Entry staggered animations
    val delay = index * 40
    val alpha by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(450, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "gradeAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (animateTrigger) 0f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "gradeY"
    )

    Box(
        modifier = modifier
            .graphicsLayer(alpha = alpha, translationY = translateY)
            .shadow(
                elevation = if (option.available) 10.dp else 2.dp,
                shape = RoundedCornerShape(Radius.lg),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(Radius.lg))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = if (option.available) accent.copy(alpha = 0.35f) else t.line.copy(alpha = 0.3f),
                shape = RoundedCornerShape(Radius.lg),
            )
            .bounceClickable(enabled = option.available) { onClick() }
            .padding(20.dp),
    ) {
        // Subtle back-lighting glow circle on active cards
        if (option.available) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = accent.copy(alpha = 0.03f),
                    center = Offset(20.dp.toPx(), 20.dp.toPx()),
                    radius = 45.dp.toPx()
                )
                drawCircle(
                    color = accent.copy(alpha = 0.04f),
                    center = Offset(20.dp.toPx(), 20.dp.toPx()),
                    radius = 30.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top — compact grade number
                LLText(
                    "${option.grade}",
                    color = if (option.available) accent else accent.copy(alpha = 0.5f),
                    size = 32.sp,
                    weight = FontWeight.Black,
                )

                if (option.available) {
                    // Active glowing status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .shadow(elevation = 6.dp, shape = CircleShape, spotColor = accent)
                    )
                }
            }
            // Tagline / Status description
            LLText(
                if (option.available) option.tagline else "Coming soon",
                color = if (option.available) t.ink400 else t.ink600,
                size = 12.sp,
                lineHeight = 16.sp,
                weight = FontWeight.Medium
            )
        }
    }
}
