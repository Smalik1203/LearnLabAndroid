package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.content.AllExperiments
import com.learnlab.design.LL
import com.learnlab.design.LLText

@Composable
fun Welcome() {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(horizontal = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.surface)
                    .border(1.dp, t.line, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    tint = t.ink400,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            LLText(
                "No experiment selected",
                color = t.ink50, size = 18.sp, weight = FontWeight.SemiBold,
                align = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            LLText(
                "Pick one of the ${AllExperiments.size} experiments from the rail on the left to begin.",
                color = t.ink400, size = 14.sp,
                align = TextAlign.Center, lineHeight = 20.sp,
            )
            Spacer(Modifier.height(20.dp))
            LLText(
                "CHAPTER 1 · CHAPTER 2 · CHAPTER 3 · CHAPTER 4",
                color = t.ink500, size = 11.sp,
                letterSpacing = 1.8.sp, weight = FontWeight.SemiBold,
                align = TextAlign.Center,
            )
        }
    }
}
