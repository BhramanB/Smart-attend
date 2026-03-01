package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.attendancehr.app.data.model.AttendanceRecord
import com.attendancehr.app.ui.components.GlassCard
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.components.SectionHeader
import com.attendancehr.app.ui.components.StatCard
import com.attendancehr.app.ui.theme.Blue600
import com.attendancehr.app.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeDashboardScreen(
    vm: AppViewModel,
    onOpenNotifications: () -> Unit,
    onOpenCameraAttendance: () -> Unit,
) {
    val user by vm.currentUser.collectAsState()
    val today by vm.todayAttendance.collectAsState()
    val history by vm.attendanceHistory.collectAsState()
    val leaveBalances by vm.leaveBalances.collectAsState()
    val notifications by vm.notifications.collectAsState()

    val zone = remember { ZoneId.systemDefault() }
    val nowTick = remember { mutableLongStateOf(0L) }

    LaunchedEffect(today?.checkInAt, today?.checkOutAt) {
        while (today?.checkInAt != null && today?.checkOutAt == null) {
            nowTick.longValue = System.currentTimeMillis()
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(bottom = 92.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val displayId = user?.managedClassroomId ?: user?.classroomId
                    if (displayId != null) {
                        Text(
                            text = "CLASS ID: $displayId",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Hello, ${user?.name?.split(" ")?.firstOrNull() ?: "there"}",
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = LocalDate.now().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onOpenNotifications) {
                    Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null)
                }
            }
        }

        item {
            TodayAttendanceCard(
                today = today,
                runningDuration = computeRunningDuration(today, zone),
                onCheckIn = onOpenCameraAttendance,
                onCheckOut = { vm.checkOut() },
            )
        }

        item {
            val monthStart = LocalDate.now().withDayOfMonth(1)
            val monthRecords = history.filter { it.date >= monthStart }
            val presentDays = monthRecords.count { it.checkInAt != null }
            val workingDays = LocalDate.now().dayOfMonth.coerceAtLeast(1)
            val attendancePct = ((presentDays.toFloat() / workingDays.toFloat()) * 100f).coerceIn(0f, 100f)

            StatsRow(
                attendancePct = attendancePct,
                presentDays = presentDays,
                absentDays = (workingDays - presentDays).coerceAtLeast(0),
                leaveRemaining = leaveBalances.sumOf { it.remainingDays },
            )
        }

        item {
            SectionHeader(title = "Recent activity")
        }

        items(notifications.take(6), key = { it.id }) { n ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = n.title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(text = n.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TodayAttendanceCard(
    today: AttendanceRecord?,
    runningDuration: Duration,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Today", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                Text(
                    text = when {
                        today?.checkInAt == null -> "Not checked-in"
                        today.checkOutAt == null -> "Checked-in"
                        else -> "Checked-out"
                    },
                    color = Blue600,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (today?.checkInAt != null) {
                val t = formatDuration(if (today.checkOutAt == null) runningDuration else (today.totalDuration ?: Duration.ZERO))
                Text(text = t, style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Working hours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Tap Check-In to start your workday.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PrimaryButton(
                text = if (today?.checkInAt == null) "Check-In (Camera + GPS)" else if (today.checkOutAt == null) "Check-Out" else "Completed",
                enabled = today?.checkOutAt == null,
                onClick = { if (today?.checkInAt == null) onCheckIn() else onCheckOut() },
            )
        }
    }
}

@Composable
private fun StatsRow(attendancePct: Float, presentDays: Int, absentDays: Int, leaveRemaining: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard(title = "Attendance %", value = "${attendancePct.toInt()}%", modifier = Modifier.weight(1f))
        StatCard(title = "Present", value = presentDays.toString(), modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard(title = "Absent", value = absentDays.toString(), modifier = Modifier.weight(1f))
        StatCard(title = "Leave left", value = leaveRemaining.toString(), modifier = Modifier.weight(1f))
    }
}

private fun computeRunningDuration(today: AttendanceRecord?, zone: ZoneId): Duration {
    val inAt = today?.checkInAt ?: return Duration.ZERO
    val outAt = today.checkOutAt
    val end = outAt ?: Instant.now()
    return Duration.between(inAt, end).coerceAtLeast(Duration.ZERO)
}

private fun formatDuration(d: Duration): String {
    val h = d.toHours()
    val m = (d.toMinutes() % 60)
    val s = (d.seconds % 60)
    return "%02d:%02d:%02d".format(h, m, s)
}
