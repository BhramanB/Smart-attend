package com.attendancehr.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.AppTextField
import com.attendancehr.app.ui.components.PrimaryButton

@Composable
fun ForgotPasswordScreen(
    onSendOtp: (email: String) -> Unit,
    onBack: () -> Unit,
) {
    val email = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Text(text = "Forgot password", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Enter your email to receive an OTP.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(10.dp))
        AppTextField(
            label = "Email",
            value = email.value,
            onValueChange = { email.value = it },
            leadingIcon = Icons.Outlined.Email,
        )

        PrimaryButton(
            text = "Send OTP",
            onClick = { onSendOtp(email.value) },
        )

        TextButton(onClick = onBack) {
            Text("Back", color = MaterialTheme.colorScheme.primary)
        }
    }
}

