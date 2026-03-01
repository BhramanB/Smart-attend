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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.attendancehr.app.data.model.AttendanceRecord
import com.attendancehr.app.ui.theme.Blue600
import com.attendancehr.app.ui.theme.Danger
import com.attendancehr.app.ui.theme.Success
import com.attendancehr.app.ui.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    vm: AppViewModel,
    onBack: (() -> Unit)?,
) {
    val history by vm.attendanceHistory.collectAsState()
    val selectedMonth = remember { mutableStateOf(YearMonth.now()) }

    val monthRecords = history.filter { YearMonth.from(it.date) == selectedMonth.value }
    val byDate = monthRecords.associateBy { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance history") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) }
                    }
                },
                actions = {
                    IconButton(onClick = { selectedMonth.value = selectedMonth.value.minusMonths(1) }) {
                        Icon(Icons.Outlined.ChevronLeft, null)
                    }
                    Text(
                        text = selectedMonth.value.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { selectedMonth.value = selectedMonth.value.plusMonths(1) }) {
                        Icon(Icons.Outlined.ChevronRight, null)
                    }
                },
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CalendarMonthGrid(
                    month = selectedMonth.value,
                    recordsByDate = byDate,
                )
            }

            item {
                Text(
                    text = "Daily records",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            items(monthRecords.sortedByDescending { it.date }, key = { it.id }) { rec ->
                AttendanceRecordCard(rec)
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    recordsByDate: Map<LocalDate, AttendanceRecord>,
) {
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDow = first.dayOfWeek
    val offset = ((firstDow.value % 7) + 7) % 7 // Monday=1..Sunday=7 -> align Mon start

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            val totalCells = ((offset + daysInMonth + 6) / 7) * 7
            var day = 1 - offset
            repeat(totalCells / 7) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) {
                        val date = if (day in 1..daysInMonth) month.atDay(day) else null
                        val rec = if (date != null) recordsByDate[date] else null
                        CalendarCell(day = date?.dayOfMonth, record = rec, modifier = Modifier.weight(1f))
                        day++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(day: Int?, record: AttendanceRecord?, modifier: Modifier = Modifier) {
    val isPresent = record?.checkInAt != null
    val dot = when {
        day == null -> null
        record == null -> null
        isPresent -> Success
        else -> Danger
    }

    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day?.toString() ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = if (day == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurface,
        )
        if (dot != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(6.dp)
                    .background(dot.copy(alpha = 0.95f), CircleShape),
            )
        }
    }
}

@Composable
private fun AttendanceRecordCard(rec: AttendanceRecord) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = rec.date.toString(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                val status = if (rec.checkInAt != null) "Present" else "Absent"
                Text(text = status, color = if (rec.checkInAt != null) Success else Danger)
            }
            val inTime = rec.checkInAt?.toString()?.substringAfter("T")?.substringBefore(".") ?: "--:--"
            val outTime = rec.checkOutAt?.toString()?.substringAfter("T")?.substringBefore(".") ?: "--:--"
            Text(
                text = "In: $inTime  •  Out: $outTime",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val total = rec.totalDuration?.toHours()?.toString() ?: "-"
            Text(
                text = "Total: ${formatDuration(rec.totalDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Blue600,
            )
        }
    }
}

private fun formatDuration(d: java.time.Duration?): String {
    if (d == null) return "--"
    val h = d.toHours()
    val m = (d.toMinutes() % 60)
    return "%02dh %02dm".format(h, m)
}
