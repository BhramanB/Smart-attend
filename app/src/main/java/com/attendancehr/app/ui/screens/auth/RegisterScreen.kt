package com.attendancehr.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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

data class RegisterForm(
    val name: String,
    val email: String,
    val mobile: String,
    val organization: String,
    val department: String,
    val idNumber: String,
    val password: String,
    val confirmPassword: String,
)

@Composable
fun RegisterScreen(
    onRegister: (RegisterForm) -> Unit,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val mobile = remember { mutableStateOf("") }
    val org = remember { mutableStateOf("") }
    val dept = remember { mutableStateOf("") }
    val idNo = remember { mutableStateOf("") }
    val pass = remember { mutableStateOf("") }
    val confirm = remember { mutableStateOf("") }
    val showPass = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(6.dp))
        Text(text = "Create account", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "Register once. Verify OTP. Start tracking attendance.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        AppTextField("Name", name.value, { name.value = it }, leadingIcon = Icons.Outlined.Person)
        AppTextField("Email", email.value, { email.value = it }, leadingIcon = Icons.Outlined.Email)
        AppTextField("Mobile Number", mobile.value, { mobile.value = it }, leadingIcon = Icons.Outlined.Phone)
        AppTextField("College/Company Name", org.value, { org.value = it }, leadingIcon = Icons.Outlined.Business)
        AppTextField("Department", dept.value, { dept.value = it }, leadingIcon = Icons.Outlined.Badge)
        AppTextField("ID Number", idNo.value, { idNo.value = it }, leadingIcon = Icons.Outlined.Badge)

        AppTextField(
            label = "Password",
            value = pass.value,
            onValueChange = { pass.value = it },
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = {
                IconButton(onClick = { showPass.value = !showPass.value }) {
                    Icon(
                        imageVector = if (showPass.value) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                    )
                }
            },
            visualTransformation = if (showPass.value) VisualTransformation.None else PasswordVisualTransformation(),
        )
        AppTextField(
            label = "Confirm Password",
            value = confirm.value,
            onValueChange = { confirm.value = it },
            leadingIcon = Icons.Outlined.Lock,
            visualTransformation = if (showPass.value) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Spacer(Modifier.height(6.dp))
        PrimaryButton(
            text = "Register",
            onClick = {
                onRegister(
                    RegisterForm(
                        name = name.value,
                        email = email.value,
                        mobile = mobile.value,
                        organization = org.value,
                        department = dept.value,
                        idNumber = idNo.value,
                        password = pass.value,
                        confirmPassword = confirm.value,
                    )
                )
            },
        )

        TextButton(onClick = onBack) {
            Text(text = "Back to Login", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(10.dp))
    }
}

