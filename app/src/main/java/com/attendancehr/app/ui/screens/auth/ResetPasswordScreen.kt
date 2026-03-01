package com.attendancehr.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.AppTextField
import com.attendancehr.app.ui.components.PrimaryButton

@Composable
fun ResetPasswordScreen(
    onUpdate: (newPassword: String) -> Unit,
    onBack: () -> Unit,
) {
    val pass = remember { mutableStateOf("") }
    val confirm = remember { mutableStateOf("") }
    val show = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Text(text = "Reset password", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Choose a new password for your account.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(10.dp))
        AppTextField(
            label = "New Password",
            value = pass.value,
            onValueChange = { pass.value = it },
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { show.value = !show.value }) {
                    Icon(
                        imageVector = if (show.value) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                    )
                }
            },
            visualTransformation = if (show.value) VisualTransformation.None else PasswordVisualTransformation(),
        )
        AppTextField(
            label = "Confirm Password",
            value = confirm.value,
            onValueChange = { confirm.value = it },
            leadingIcon = Icons.Outlined.Lock,
            visualTransformation = if (show.value) VisualTransformation.None else PasswordVisualTransformation(),
        )

        PrimaryButton(
            text = "Update Password",
            onClick = { if (pass.value == confirm.value) onUpdate(pass.value) },
        )

        TextButton(onClick = onBack) {
            Text("Back", color = MaterialTheme.colorScheme.primary)
        }
    }
}

