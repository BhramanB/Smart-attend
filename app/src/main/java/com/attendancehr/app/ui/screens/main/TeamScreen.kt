package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.theme.Danger
import com.attendancehr.app.ui.theme.Success

private data class TeamMember(
    val name: String,
    val subtitle: String,
    val isPresent: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(onBack: () -> Unit) {
    val q = remember { mutableStateOf("") }
    val items = remember {
        listOf(
            TeamMember("Aditi Sharma", "CSE • 3rd Year", true),
            TeamMember("Rahul Patil", "ECE • 2nd Year", false),
            TeamMember("Sana Khan", "HR • Admin", true),
            TeamMember("Vikram Singh", "Mechanical • 4th Year", true),
        )
    }

    val filtered = items.filter {
        q.value.isBlank() || it.name.contains(q.value, ignoreCase = true) || it.subtitle.contains(q.value, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team / Students") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
            )
        },
    ) { inner ->
        Column(modifier = Modifier.padding(inner)) {
            OutlinedTextField(
                value = q.value,
                onValueChange = { q.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = MaterialTheme.shapes.medium,
            )

            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filtered, key = { it.name }) { m ->
                    MemberCard(m)
                }
            }
        }
    }
}

@Composable
private fun MemberCard(m: TeamMember) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = m.name.take(1), style = MaterialTheme.typography.headlineSmall)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = m.name, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = m.subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val dotColor = if (m.isPresent) Success else Danger
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                Text(
                    text = if (m.isPresent) "Present" else "Absent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dotColor,
                )
            }
        }
    }
}
