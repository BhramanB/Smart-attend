package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.AppTextField
import com.attendancehr.app.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val current = remember { mutableStateOf("") }
    val newPass = remember { mutableStateOf("") }
    val confirm = remember { mutableStateOf("") }
    val show = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change password") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            AppTextField(
                label = "Current password",
                value = current.value,
                onValueChange = { current.value = it },
                leadingIcon = Icons.Outlined.Lock,
                visualTransformation = PasswordVisualTransformation(),
            )
            AppTextField(
                label = "New password",
                value = newPass.value,
                onValueChange = { newPass.value = it },
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
                label = "Confirm password",
                value = confirm.value,
                onValueChange = { confirm.value = it },
                leadingIcon = Icons.Outlined.Lock,
                visualTransformation = if (show.value) VisualTransformation.None else PasswordVisualTransformation(),
            )

            Spacer(Modifier.height(6.dp))
            PrimaryButton(text = "Update Password", onClick = { /* delegate to reset flow from profile */ })
            TextButton(onClick = onBack) { Text("Back", color = MaterialTheme.colorScheme.primary) }
        }
    }
}
