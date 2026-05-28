package com.learnlab.ui.grade

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg),
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
                .padding(horizontal = 40.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                LLText(
                    "Pick a grade",
                    color = t.ink50, size = 28.sp,
                    weight = FontWeight.Bold,
                )
                LLText(
                    "Choose the class level for the labs you want to explore.",
                    color = t.ink400, size = 14.sp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GRADE_OPTIONS.chunked(5).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        rowOptions.forEach { opt ->
                            GradeCard(
                                option = opt,
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

@Composable
private fun GradeCard(
    option: GradeOption,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val t = LL.tokens
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "gradeElevation",
    )
    val accent = if (option.available) t.accent500 else t.accent500.copy(alpha = 0.35f)
    val cardBg = if (option.available) t.surface else t.surface.copy(alpha = 0.55f)
    val titleColor = if (option.available) t.ink50 else t.ink500
    val taglineColor = if (option.available) t.ink400 else t.ink500
    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(
                width = if (option.available) 1.dp else 1.dp,
                color = if (option.available) accent.copy(alpha = 0.4f) else t.line,
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(enabled = option.available) { pressed = true; onClick() }
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top — compact grade number
            LLText(
                "${option.grade}",
                color = if (option.available) accent else accent.copy(alpha = 0.6f),
                size = 28.sp, weight = FontWeight.ExtraBold,
            )
            // Tagline
            LLText(
                if (option.available) option.tagline else "Coming soon",
                color = taglineColor,
                size = 12.sp, lineHeight = 16.sp,
            )
        }
    }
}
