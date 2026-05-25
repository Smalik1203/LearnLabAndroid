package com.learnlab.lessons.figures.composeDraw

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.learnlab.lessons.patterns.ConceptBranch
import com.learnlab.lessons.patterns.ConceptBranchDetailPane
import com.learnlab.lessons.patterns.ConceptHub
import com.learnlab.lessons.patterns.ConceptLeaf
import com.learnlab.lessons.patterns.ConceptMap

/**
 * Full-bleed "How do we group plants?" slide. Concept map on the left, intro
 * paragraph (or focused branch detail) on the right. Focus state is shared
 * between the two halves so tapping a branch swaps the right pane.
 */
@Composable
fun PlantGroupingConceptMap(modifier: Modifier = Modifier) {
    val branches = remember { plantGroupingBranches() }
    var focusedId by remember { mutableStateOf<String?>(null) }
    val focusedBranch = branches.firstOrNull { it.id == focusedId }

    // Outer scrim: tapping ANY empty area (left or right side) dismisses focus.
    // Branch nodes and leaf chips have their own pointerInput which consumes
    // events first, so they aren't affected.
    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(focusedId) {
                detectTapGestures(onTap = { focusedId = null })
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1.1f).fillMaxHeight()) {
            ConceptMap(
                hub = ConceptHub(title = "GROUP", subtitle = "PLANTS"),
                branches = branches,
                focusedId = focusedId,
                onFocusChange = { focusedId = it },
            )
        }
        Box(modifier = Modifier.weight(0.9f).fillMaxHeight().padding(end = 8.dp)) {
            ConceptBranchDetailPane(
                branch = focusedBranch,
                intro = "Plants differ in many ways. Four features — stem, leaves, roots, seeds — give us almost everything we need to group any plant we meet.",
            )
        }
    }
}

private fun plantGroupingBranches(): List<ConceptBranch> = listOf(
    ConceptBranch(
        id = "stem",
        label = "Stem",
        accent = Color(0xFF10B981),
        glyph = "🌿",
        description = "How tall is it? Is the stem soft and green, or hard and woody?",
        children = listOf(
            ConceptLeaf("Herb", "soft, short", "🌱"),
            ConceptLeaf("Shrub", "woody, medium", "🌿"),
            ConceptLeaf("Tree", "woody, tall", "🌳"),
        ),
    ),
    ConceptBranch(
        id = "leaves",
        label = "Leaves",
        accent = Color(0xFF06B6D4),
        glyph = "🍃",
        description = "What pattern do the veins make on the leaf?",
        children = listOf(
            ConceptLeaf("Reticulate", "net-like"),
            ConceptLeaf("Parallel", "side by side"),
        ),
    ),
    ConceptBranch(
        id = "roots",
        label = "Roots",
        accent = Color(0xFFF59E0B),
        glyph = "⚓",
        description = "Below the soil — one thick main root, or many thin ones?",
        children = listOf(
            ConceptLeaf("Taproot", "one main"),
            ConceptLeaf("Fibrous", "many thin"),
        ),
    ),
    ConceptBranch(
        id = "seeds",
        label = "Seeds",
        accent = Color(0xFFA855F7),
        glyph = "🌰",
        description = "Inside the seed — one cotyledon or two?",
        children = listOf(
            ConceptLeaf("Monocot", "one"),
            ConceptLeaf("Dicot", "two"),
        ),
    ),
)
