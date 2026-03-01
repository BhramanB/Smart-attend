package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendancehr.app.data.model.User
import com.attendancehr.app.data.model.UserRole
import com.attendancehr.app.ui.components.GlassCard
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.components.SectionHeader
import com.attendancehr.app.ui.components.StatCard
import com.attendancehr.app.ui.viewmodel.AppViewModel

@Composable
fun AdminDashboardScreen(vm: AppViewModel) {
    val classroom by vm.managedClassroom.collectAsState()
    val students by vm.classStudents.collectAsState()
    val attendance by vm.todayClassAttendance.collectAsState()
    val clipboard = LocalClipboardManager.current

    val presentUids = attendance.map { it.userId }.toSet()
    val presentCount = students.count { it.id in presentUids }
    val absentCount = students.size - presentCount

    LazyColumn(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Admin Dashboard", style = MaterialTheme.typography.headlineLarge)
        }

        if (classroom == null) {
            item {
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Create a Classroom", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(text = "Create a classroom to manage students and track their attendance.")
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(text = "Create Classroom", onClick = { vm.createClassroom("My New Class") })
                    }
                }
            }
        } else {
            item {
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Class Code", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { clipboard.setText(AnnotatedString(classroom!!.code)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(text = classroom!!.code, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Students join using this code", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = "Present", value = presentCount.toString(), modifier = Modifier.weight(1f))
                    StatCard(title = "Absent", value = absentCount.toString(), modifier = Modifier.weight(1f))
                }
            }

            item {
                SectionHeader(title = "Students & Staff")
            }

            items(students) { user ->
                UserListItem(user = user, isPresent = user.id in presentUids)
            }
        }
    }
}

@Composable
fun UserListItem(user: User, isPresent: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (user.role == UserRole.Student) Icons.Outlined.Person else Icons.Outlined.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = "${user.role} • ${user.idNumber}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (isPresent) "Present" else "Absent",
                color = if (isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
