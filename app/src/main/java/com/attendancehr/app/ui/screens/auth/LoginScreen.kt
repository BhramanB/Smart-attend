package com.attendancehr.app.ui.screens.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.attendancehr.app.ui.components.AppTextField
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.components.SecondaryButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoogleLogin: (idToken: String) -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        Text(text = "Welcome back", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Sign in to continue to your dashboard.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(14.dp))
        AppTextField(
            label = "Email",
            value = email.value,
            onValueChange = { email.value = it },
            leadingIcon = Icons.Outlined.Email,
        )
        AppTextField(
            label = "Password",
            value = password.value,
            onValueChange = { password.value = it },
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { showPassword.value = !showPassword.value }) {
                    Icon(
                        imageVector = if (showPassword.value) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        )

        TextButton(onClick = onForgotPassword, modifier = Modifier.padding(top = 2.dp)) {
            Text(text = "Forgot Password?", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(4.dp))
        PrimaryButton(
            text = "Login",
            onClick = { onLogin(email.value, password.value) },
        )
        SecondaryButton(
            text = "Continue with Google",
            leadingIcon = Icons.Outlined.AccountCircle,
            onClick = {
                scope.launch {
                    val idToken = triggerGoogleSignIn(context)
                    if (idToken != null) {
                        onGoogleLogin(idToken)
                    }
                }
            },
        )

        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onRegister) {
            Text(
                text = "Don't have an account? Register",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private suspend fun triggerGoogleSignIn(context: Context): String? {
    val credentialManager = CredentialManager.create(context)
    
    // NOTE: You must replace this with your actual Web Client ID from Firebase Console
    val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            credential.idToken
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
