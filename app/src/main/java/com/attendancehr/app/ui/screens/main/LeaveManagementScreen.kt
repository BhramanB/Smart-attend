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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.attendancehr.app.data.model.LeaveBalance
import com.attendancehr.app.data.model.LeaveRequest
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.components.SectionHeader
import com.attendancehr.app.ui.components.StatusBadge
import com.attendancehr.app.ui.viewmodel.AppViewModel

@Composable
fun LeaveManagementScreen(
    vm: AppViewModel,
    onApply: () -> Unit,
) {
    val balances by vm.leaveBalances.collectAsState()
    val history by vm.leaveHistory.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(bottom = 92.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(title = "Leave management")
        }

        item {
            LeaveBalanceRow(balances = balances)
        }

        item {
            PrimaryButton(text = "Apply Leave", onClick = onApply)
        }

        item {
            SectionHeader(title = "Leave history")
        }

        items(history, key = { it.id }) { req ->
            LeaveHistoryItem(req = req)
        }
    }
}

@Composable
private fun LeaveBalanceRow(balances: List<LeaveBalance>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        balances.take(3).forEach { b ->
            Card(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = b.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${b.remainingDays}/${b.totalDays}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaveHistoryItem(req: LeaveRequest) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "${req.type.name} leave", style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${req.startDate} → ${req.endDate}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(status = req.status)
        }
    }
}

