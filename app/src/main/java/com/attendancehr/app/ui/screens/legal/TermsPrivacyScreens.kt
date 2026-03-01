package com.attendancehr.app.ui.screens.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(onBack: () -> Unit) {
    LegalScreen(
        title = "Terms & Conditions",
        body = loremTerms,
        onBack = onBack,
    )
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalScreen(
        title = "Privacy Policy",
        body = loremPrivacy,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalScreen(
    title: String,
    body: String,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private val loremTerms = """
Welcome to the Attendance HR mobile application.

By using this app you agree that:
- Attendance data is recorded for compliance and analytics.
- Your account is for your personal use only.
- Admins in your organization can view and export aggregated data.

For a production rollout, replace this copy with your organization-specific legal terms.
""".trimIndent()

private val loremPrivacy = """
We value your privacy.

This demo implementation stores all data locally in memory only.
In a real deployment:
- User profiles and attendance events are stored in a secure backend.
- Passwords are always stored as cryptographic hashes.
- Access is controlled using role-based permissions.

Consult your legal team to provide a complete privacy policy before going live.
""".trimIndent()
