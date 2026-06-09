package com.learnlab.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.Chapters
import com.learnlab.content.ChapterBlock
import com.learnlab.content.findExperiment
import com.learnlab.design.ExperimentAccent
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.PrimaryButton
import com.learnlab.shell.TopBar
import com.learnlab.store.AppState

@Composable
fun ChapterReaderScreen(
    state: AppState,
    chapterId: String,
    onExperimentSelected: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    val t = LL.tokens
    val chapter = remember(chapterId) {
        Chapters.firstOrNull { it.id == chapterId }
    }

    if (chapter == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(t.bg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(state = state, title = "Not Found", showBack = true, onBack = onBack)
            Text("Chapter not found.", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        TopBar(
            state = state,
            title = "Back to Curriculum",
            showBack = true,
            onBack = onBack,
            onHomeClick = onHome,
        )

        // Chapter Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.surface)
                .padding(horizontal = 40.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = t.accent500.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    LLText(
                        text = "${chapter.number}",
                        color = t.accent500,
                        size = 20.sp,
                        weight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                LLText(
                    text = "CHAPTER ${chapter.number}",
                    color = t.accent500,
                    size = 11.sp,
                    weight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                LLText(
                    text = chapter.title,
                    color = t.ink50,
                    size = 28.sp,
                    weight = FontWeight.Bold
                )
            }
        }

        // Reading Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(t.bg),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(chapter.blocks) { block ->
                ChapterBlockItem(
                    block = block,
                    onExperimentSelected = onExperimentSelected
                )
            }
            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ChapterBlockItem(
    block: ChapterBlock,
    onExperimentSelected: (String) -> Unit,
) {
    val t = LL.tokens
    when (block) {
        is ChapterBlock.Heading -> {
            val size = when (block.level) {
                1 -> 24.sp
                2 -> 20.sp
                else -> 16.sp
            }
            val weight = if (block.level == 1) FontWeight.Bold else FontWeight.SemiBold
            val topPadding = if (block.level == 1) 16.dp else 8.dp

            Column(modifier = Modifier.padding(top = topPadding, bottom = 4.dp)) {
                LLText(
                    text = block.text,
                    color = t.ink50,
                    size = size,
                    weight = weight
                )
                if (block.level == 1) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .background(t.accent500)
                    )
                }
            }
        }

        is ChapterBlock.Paragraph -> {
            val annotated = remember(block.text, t.ink50) {
                parseMarkdownBold(block.text, t.ink50)
            }
            Text(
                text = annotated,
                fontSize = 16.sp,
                color = t.ink200,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        is ChapterBlock.ActivityRef -> {
            val experiment = remember(block.experimentId) {
                findExperiment(block.experimentId)
            }
            if (experiment != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, t.accent500.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = t.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = t.accent500.copy(alpha = 0.15f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    LLText("🧪", size = 24.sp)
                                }
                            }
                            Spacer(Modifier.width(20.dp))
                            Column {
                                LLText(
                                    text = "ACTIVITY ${experiment.source.substringAfter("Activity ").substringBefore(",")}",
                                    color = t.accent500,
                                    size = 11.sp,
                                    weight = FontWeight.Bold,
                                    letterSpacing = 1.6.sp
                                )
                                LLText(
                                    text = experiment.title,
                                    color = t.ink50,
                                    size = 18.sp,
                                    weight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                LLText(
                                    text = experiment.blurb,
                                    color = t.ink400,
                                    size = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(20.dp))
                        Button(
                            onClick = { onExperimentSelected(experiment.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExperimentAccent,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Run Simulation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        is ChapterBlock.Sidebar -> {
            val (bgColor, borderColor, titleColor, icon) = when (block.type) {
                ChapterBlock.SidebarType.CURIOUS_STUDENT -> Quadruple(
                    t.amber50, t.amber700.copy(alpha = 0.4f), t.amber700, "💡"
                )
                ChapterBlock.SidebarType.THINK_LIKE_A_SCIENTIST -> Quadruple(
                    t.accent50, t.accent700.copy(alpha = 0.4f), t.accent700, "🧠"
                )
                ChapterBlock.SidebarType.BE_A_SCIENTIST -> Quadruple(
                    t.rose50, t.rose700.copy(alpha = 0.4f), t.rose700, "🌟"
                )
                ChapterBlock.SidebarType.STEP_FURTHER -> Quadruple(
                    t.surface2, t.lineStrong, t.ink50, "🚀"
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LLText(icon, size = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        LLText(
                            text = block.title,
                            color = titleColor,
                            size = 14.sp,
                            weight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LLText(
                        text = block.content,
                        color = t.ink200,
                        size = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }

        is ChapterBlock.Figure -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, t.line, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = t.surface2)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (block.illustrationId != null) {
                        IllustrationRenderer(
                            id = block.illustrationId,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(160.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    LLText(
                        text = block.label,
                        color = t.ink50,
                        size = 13.sp,
                        weight = FontWeight.Bold
                    )
                    LLText(
                        text = block.caption,
                        color = t.ink400,
                        size = 13.sp,
                        align = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IllustrationRenderer(id: String, modifier: Modifier) {
    val t = LL.tokens
    if (id == "scientific_method_cycle") {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) * 0.35f

            // Nodes
            val nodes = listOf(
                Offset(cx, cy - r) to "Observe",
                Offset(cx + r, cy) to "Hypothesis",
                Offset(cx, cy + r) to "Test",
                Offset(cx - r, cy) to "Analyse"
            )

            // Draw circular connecting paths
            drawCircle(
                color = t.line,
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(2f, cap = StrokeCap.Round)
            )

            // Draw nodes
            nodes.forEach { (pos, label) ->
                drawCircle(
                    color = t.surface,
                    radius = 32f,
                    center = pos
                )
                drawCircle(
                    color = t.accent500,
                    radius = 32f,
                    center = pos,
                    style = Stroke(2f)
                )
            }
        }
        // Overlay Composable text boxes on top of canvas for high-quality text rendering
        Box(modifier = modifier) {
            val labels = listOf("Observe", "Hypothesis", "Test", "Analyse")
            val alignments = listOf(
                Alignment.TopCenter,
                Alignment.CenterEnd,
                Alignment.BottomCenter,
                Alignment.CenterStart
            )
            labels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (index == 0) 4.dp else 0.dp,
                            bottom = if (index == 2) 4.dp else 0.dp,
                            start = if (index == 3) 4.dp else 0.dp,
                            end = if (index == 1) 4.dp else 0.dp
                        ),
                    contentAlignment = alignments[index]
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = t.surface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, t.line, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = t.ink200,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Fallback placeholder illustration
        Box(
            modifier = modifier
                .background(t.surface)
                .border(1.dp, t.line, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            LLText("[Illustration: $id]", color = t.ink500, size = 13.sp)
        }
    }
}

private fun parseMarkdownBold(text: String, boldColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor))
                append(parts[i])
                pop()
            } else {
                append(parts[i])
            }
        }
    }
}

private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
