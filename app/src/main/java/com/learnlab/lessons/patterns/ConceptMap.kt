package com.learnlab.lessons.patterns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.Radius

/* ───────────── Data model ───────────── */

data class ConceptHub(
    val title: String,
    val subtitle: String? = null,
)

data class ConceptBranch(
    val id: String,
    val label: String,
    val accent: Color,
    val glyph: String,
    val description: String? = null,
    val children: List<ConceptLeaf> = emptyList(),
)

data class ConceptLeaf(
    val label: String,
    val blurb: String? = null,
    val glyph: String? = null,
)

/**
 * A concept map: central hub with N branches radiating out.
 *
 * Focus state is hoisted — pass [focusedId] in, [onFocusChange] out. The map
 * itself does NOT render detail content; the *caller* (slide layout) is
 * responsible for showing branch details somewhere off-map.
 *
 * Tapping anywhere outside a branch (including the background and the hub)
 * clears focus.
 */
@Composable
fun ConceptMap(
    hub: ConceptHub,
    branches: List<ConceptBranch>,
    focusedId: String?,
    onFocusChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Tap anywhere on the background dismisses focus.
            .pointerInput(focusedId) {
                detectTapGestures(onTap = { onFocusChange(null) })
            }
            .padding(20.dp),
    ) {
        val w = maxWidth
        val h = maxHeight
        val cx = w / 2f
        val cy = h / 2f

        // Sizing — favour readability over density.
        val hubDiameter = (kotlin.math.min(w.value, h.value) * 0.34f).dp.coerceIn(140.dp, 240.dp)
        val branchDiameter = (kotlin.math.min(w.value, h.value) * 0.22f).dp.coerceIn(96.dp, 150.dp)
        val orbitRx = (w / 2f) - branchDiameter / 2f - 8.dp
        val orbitRy = (h / 2f) - branchDiameter / 2f - 8.dp

        val n = branches.size
        val angles = (0 until n).map { idx ->
            -Math.PI / 2 + 2.0 * Math.PI * idx / n
        }
        val nodePositions = angles.map { a ->
            Offset(
                x = (cx.value + orbitRx.value * kotlin.math.cos(a).toFloat()),
                y = (cy.value + orbitRy.value * kotlin.math.sin(a).toFloat()),
            )
        }

        // Canvas: connecting lines (drawn first, behind nodes)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Decorative orbit
            drawOval(
                color = Color.White.copy(alpha = 0.04f),
                topLeft = Offset((cx - orbitRx).toPx(), (cy - orbitRy).toPx()),
                size = Size((orbitRx * 2).toPx(), (orbitRy * 2).toPx()),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))),
            )
            nodePositions.forEachIndexed { i, p ->
                val accent = branches[i].accent
                val isFocused = focusedId == branches[i].id
                val isDimmed = focusedId != null && !isFocused
                val alpha = when {
                    isFocused -> 1f
                    isDimmed -> 0.18f
                    else -> 0.55f
                }
                drawLine(
                    color = accent.copy(alpha = alpha),
                    start = Offset(cx.toPx(), cy.toPx()),
                    end = Offset(p.x.dp.toPx(), p.y.dp.toPx()),
                    strokeWidth = if (isFocused) 5.5f else 3f,
                    cap = StrokeCap.Round,
                )
            }
        }

        // Hub (centred, big enough for the title)
        Box(
            modifier = Modifier
                .offset(x = cx - hubDiameter / 2f, y = cy - hubDiameter / 2f)
                .size(hubDiameter),
        ) {
            HubNode(hub, hubDiameter, isDimmed = focusedId != null)
        }

        // Branch nodes
        branches.forEachIndexed { i, branch ->
            val pos = nodePositions[i]
            val isFocused = focusedId == branch.id
            val isDimmed = focusedId != null && !isFocused
            Box(
                modifier = Modifier
                    .offset(x = pos.x.dp - branchDiameter / 2f, y = pos.y.dp - branchDiameter / 2f)
                    .size(branchDiameter),
            ) {
                BranchNode(
                    branch = branch,
                    isFocused = isFocused,
                    isDimmed = isDimmed,
                    onTap = { onFocusChange(if (isFocused) null else branch.id) },
                )
            }
        }
    }
}

@Composable
private fun HubNode(hub: ConceptHub, diameter: Dp, isDimmed: Boolean) {
    val t = LL.tokens
    val alpha by animateFloatAsState(if (isDimmed) 0.55f else 1f, tween(220), label = "hubAlpha")
    Box(
        modifier = Modifier
            .size(diameter)
            .clip(CircleShape)
            .background(t.surface.copy(alpha = alpha))
            .border(3.dp, t.accent500.copy(alpha = alpha), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp),
        ) {
            LLText(
                text = hub.title,
                color = t.ink50.copy(alpha = alpha),
                size = 22.sp,
                weight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                maxLines = 2,
            )
            if (hub.subtitle != null) {
                Spacer(Modifier.height(4.dp))
                LLText(
                    hub.subtitle,
                    color = t.ink400.copy(alpha = alpha),
                    size = 12.sp,
                    weight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun BranchNode(
    branch: ConceptBranch,
    isFocused: Boolean,
    isDimmed: Boolean,
    onTap: () -> Unit,
) {
    val t = LL.tokens
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.10f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "branchScale",
    )
    val alpha by animateFloatAsState(
        if (isDimmed) 0.35f else 1f, tween(220), label = "branchAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                if (isFocused) branch.accent.copy(alpha = alpha)
                else t.surface.copy(alpha = alpha)
            )
            .border(
                width = if (isFocused) 4.dp else 2.dp,
                color = branch.accent.copy(alpha = alpha),
                shape = CircleShape,
            )
            .pointerInput(isFocused) {
                detectTapGestures(onTap = { onTap() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LLText(
                branch.glyph,
                color = if (isFocused) Color.White else branch.accent.copy(alpha = alpha),
                size = 30.sp,
            )
            Spacer(Modifier.height(2.dp))
            LLText(
                branch.label,
                color = (if (isFocused) Color.White else t.ink50).copy(alpha = alpha),
                size = 15.sp,
                weight = FontWeight.Bold,
            )
        }
    }
}

/* ───────── Detail pane the slide layout renders elsewhere ───────── */

/**
 * The branch-detail card. Render this in the *side pane*, not over the map.
 * Slides in from the right when [branch] changes; out when it becomes null.
 */
@Composable
fun ConceptBranchDetailPane(
    branch: ConceptBranch?,
    intro: String,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    Box(modifier = modifier.fillMaxSize()) {
        // Intro text — visible only when nothing focused
        AnimatedVisibility(
            visible = branch == null,
            enter = fadeIn(tween(240)),
            exit = fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                LLText(
                    intro,
                    color = t.ink50,
                    size = 22.sp,
                    lineHeight = 34.sp,
                )
                Spacer(Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp).clip(CircleShape)
                            .background(t.surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        LLText("→", color = t.ink400, size = 16.sp, weight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    LLText("Tap any branch to learn more",
                        color = t.ink400, size = 14.sp, weight = FontWeight.Medium)
                }
            }
        }

        // Branch detail — visible when something focused
        AnimatedVisibility(
            visible = branch != null,
            enter = fadeIn(tween(240)) + slideInHorizontally(tween(280)) { it / 6 },
            exit = fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { it / 6 },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (branch != null) BranchDetailContent(branch)
        }
    }
}

@Composable
private fun BranchDetailContent(branch: ConceptBranch) {
    val t = LL.tokens
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        // Pill header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp).clip(CircleShape)
                    .background(branch.accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                LLText(branch.glyph, color = branch.accent, size = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            LLText(
                branch.label.uppercase(),
                color = branch.accent,
                size = 18.sp,
                weight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
            )
        }
        if (branch.description != null) {
            Spacer(Modifier.height(20.dp))
            LLText(
                branch.description,
                color = t.ink50,
                size = 22.sp,
                lineHeight = 32.sp,
                weight = FontWeight.Medium,
            )
        }
        if (branch.children.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                branch.children.forEach { leaf ->
                    LeafChip(leaf = leaf, accent = branch.accent)
                }
            }
        }
    }
}

@Composable
private fun LeafChip(leaf: ConceptLeaf, accent: Color) {
    val t = LL.tokens
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(Radius.md))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (leaf.glyph != null) {
            LLText(leaf.glyph, color = accent, size = 26.sp)
            Spacer(Modifier.height(6.dp))
        }
        LLText(leaf.label, color = t.ink50, size = 16.sp, weight = FontWeight.Bold)
        if (leaf.blurb != null) {
            Spacer(Modifier.height(2.dp))
            LLText(leaf.blurb, color = t.ink400, size = 12.sp)
        }
    }
}
