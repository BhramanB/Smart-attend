package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.attendancehr.app.data.model.LeaveType
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.viewmodel.AppViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    val zone = remember { ZoneId.systemDefault() }

    val type = remember { mutableStateOf(LeaveType.Casual) }
    val startDate = remember { mutableStateOf(LocalDate.now()) }
    val endDate = remember { mutableStateOf(LocalDate.now()) }
    val reason = remember { mutableStateOf("") }

    val showStartPicker = remember { mutableStateOf(false) }
    val showEndPicker = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply Leave") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeaveTypeDropdown(
                selected = type.value,
                onSelected = { type.value = it },
            )

            DateField(
                label = "Start date",
                value = startDate.value.toString(),
                onClick = { showStartPicker.value = true },
            )
            DateField(
                label = "End date",
                value = endDate.value.toString(),
                onClick = { showEndPicker.value = true },
            )

            OutlinedTextField(
                value = reason.value,
                onValueChange = { reason.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reason") },
                leadingIcon = { Icon(Icons.Outlined.Description, null) },
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(Modifier.height(6.dp))
            PrimaryButton(
                text = "Submit",
                onClick = {
                    vm.applyLeave(
                        type = type.value,
                        startDate = startDate.value,
                        endDate = endDate.value,
                        reason = reason.value,
                        onSuccess = onBack,
                    )
                },
            )
        }
    }

    if (showStartPicker.value) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate.value.toEpochMillis(zone))
        DatePickerDialog(
            onDismissRequest = { showStartPicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { startDate.value = it.toLocalDate(zone) }
                        showStartPicker.value = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker.value = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    if (showEndPicker.value) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate.value.toEpochMillis(zone))
        DatePickerDialog(
            onDismissRequest = { showEndPicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { endDate.value = it.toLocalDate(zone) }
                        showEndPicker.value = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker.value = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveTypeDropdown(
    selected: LeaveType,
    onSelected: (LeaveType) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Leave type") },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            LeaveType.entries.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.name) },
                    onClick = {
                        onSelected(t)
                        expanded.value = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DateField(label: String, value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        trailingIcon = {
            TextButton(onClick = onClick) { Text("Pick") }
        },
    )
}

private fun LocalDate.toEpochMillis(zone: ZoneId): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

private fun Long.toLocalDate(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
