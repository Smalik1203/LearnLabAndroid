package com.learnlab.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learnlab.design.Card
import com.learnlab.design.LL
import com.learnlab.design.LLText
import com.learnlab.design.LLTextField
import com.learnlab.design.PrimaryButton

enum class AuthMode { LOGIN, SIGN_UP }

/**
 * Mock login / sign-up form. UI-only: validates that fields are filled, hands a
 * display name back to the caller, and closes. No auth backend (deferred).
 */
@Composable
fun AuthDialog(
    mode: AuthMode,
    onDismiss: () -> Unit,
    onSubmit: (displayName: String) -> Unit,
) {
    val t = LL.tokens
    val isSignUp = mode == AuthMode.SIGN_UP
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), padding = 28.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LLText(
                        if (isSignUp) "Create your account" else "Welcome back",
                        color = t.ink50, size = 22.sp, weight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = t.ink400, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                LLText(
                    if (isSignUp) "Sign up to save your progress." else "Log in to continue.",
                    color = t.ink400, size = 14.sp,
                )

                Spacer(Modifier.height(20.dp))
                if (isSignUp) {
                    LLTextField(value = name, onValueChange = { name = it; error = null }, placeholder = "Full name")
                    Spacer(Modifier.height(12.dp))
                }
                LLTextField(
                    value = email, onValueChange = { email = it; error = null },
                    placeholder = "Email", keyboardType = KeyboardType.Email,
                )
                Spacer(Modifier.height(12.dp))
                LLTextField(
                    value = password, onValueChange = { password = it; error = null },
                    placeholder = "Password", keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                )

                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    LLText(error!!, color = t.amber700, size = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    label = if (isSignUp) "Sign up" else "Log in",
                    onClick = {
                        val missingName = isSignUp && name.isBlank()
                        if (missingName || email.isBlank() || password.isBlank()) {
                            error = "Please fill in all fields."
                        } else {
                            onSubmit(if (isSignUp) name.trim() else email.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
