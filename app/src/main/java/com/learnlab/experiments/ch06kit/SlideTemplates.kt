package com.learnlab.experiments.ch06kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText

/**
 * The three slide templates shared by every Ch.2 simulation, so all seven feel like one product.
 *
 *  Template A — full card, generous padding. Openers + concept slides.
 *  Template B — activity rail (left) + content panel (right). Interactive / activity slides.
 *  Template C — section header: badge + large title + preview box.
 */

@Composable
fun TemplateA(
    title: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    lead: String? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val t = LL.tokens
    val base = Modifier
        .fillMaxSize()
        .padding(horizontal = 18.dp, vertical = 6.dp)
    Column(
        modifier = if (scrollable) base.verticalScroll(rememberScrollState()) else base,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (badge != null) Badge(badge)
        LLText(title, color = t.ink50, size = 21.sp, weight = FontWeight.Bold, lineHeight = 27.sp)
        if (lead != null) {
            LLText(lead, color = t.ink200, size = 14.sp, lineHeight = 20.sp)
        }
        content()
    }
}

@Composable
fun TemplateB(
    modifier: Modifier = Modifier,
    leftWeight: Float = 2f,
    rightWeight: Float = 3f,
    left: @Composable ColumnScope.() -> Unit,
    right: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(leftWeight).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) { left() }
        Column(
            modifier = Modifier.weight(rightWeight).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) { right() }
    }
}

@Composable
fun TemplateC(
    badge: String,
    title: String,
    modifier: Modifier = Modifier,
    preview: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val t = LL.tokens
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Badge(badge)
        LLText(title, color = t.ink50, size = 30.sp, weight = FontWeight.Bold, lineHeight = 36.sp)
        if (preview != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface2)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { preview() }
        }
    }
}
