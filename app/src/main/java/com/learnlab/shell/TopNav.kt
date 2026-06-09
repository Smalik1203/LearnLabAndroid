package com.learnlab.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import com.learnlab.design.LLText

/**
 * Top navigation bar mirroring the web: bold "LearnLab" wordmark on the left;
 * search + history circular icons, "Log in" (hairline pill) and "Sign Up"
 * (solid white pill) on the right. Auth/search/history are visual placeholders.
 */
@Composable
fun TopNav(
    onLogo: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onHistory: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
) {
    val t = LL.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LLText(
            "LearnLab",
            color = t.ink50,
            size = 22.sp,
            weight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onLogo),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIcon(Icons.Filled.Search, "Search", onSearch)
            Spacer(Modifier.width(12.dp))
            CircleIcon(Icons.Filled.History, "History", onHistory)
            Spacer(Modifier.width(18.dp))
            // Log in — transparent pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, t.lineStrong, RoundedCornerShape(999.dp))
                    .clickable(onClick = onLogin)
                    .padding(horizontal = 20.dp, vertical = 9.dp),
            ) {
                LLText("Log in", color = t.ink50, size = 14.sp, weight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(10.dp))
            // Sign Up — solid white pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable(onClick = onSignUp)
                    .padding(horizontal = 20.dp, vertical = 9.dp),
            ) {
                LLText("Sign Up", color = Color(0xFF0A0A0F), size = 14.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    val t = LL.tokens
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(t.surface2)
            .border(1.dp, t.line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = t.ink400, modifier = Modifier.size(18.dp))
    }
}
