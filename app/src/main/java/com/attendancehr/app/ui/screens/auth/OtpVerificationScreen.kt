package com.attendancehr.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.PrimaryButton

@Composable
fun OtpVerificationScreen(
    title: String,
    subtitle: String,
    email: String,
    onResend: () -> Unit,
    onVerify: (otp: String) -> Unit,
) {
    val digits = remember { mutableStateListOf("", "", "", "") }
    val focus = remember { List(4) { FocusRequester() } }

    LaunchedEffect(Unit) {
        focus.first().requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (email.isNotBlank()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(4) { idx ->
                OutlinedTextField(
                    value = digits[idx],
                    onValueChange = { raw ->
                        val v = raw.filter { it.isDigit() }.take(1)
                        digits[idx] = v
                        if (v.isNotEmpty() && idx < 3) focus[idx + 1].requestFocus()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .size(56.dp)
                        .focusRequester(focus[idx]),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = if (idx == 3) ImeAction.Done else ImeAction.Next,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    ),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onResend) {
            Text(text = "Resend OTP", color = MaterialTheme.colorScheme.primary)
        }

        PrimaryButton(
            text = "Verify",
            onClick = { onVerify(digits.joinToString("")) },
        )
    }
}

