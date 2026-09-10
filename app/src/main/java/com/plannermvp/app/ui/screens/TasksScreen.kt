package com.plannermvp.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.ui.util.CompletionSoundPlayer
import com.plannermvp.app.ui.viewmodel.TaskFilter
import com.plannermvp.app.ui.viewmodel.TasksViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val REPEAT_OPTIONS = listOf(null, "daily", "weekly", "monthly")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory)) {
    val tasks        by viewModel.tasks.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask   by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text     = stringResource(R.string.tasks_title),
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── Filter chips ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == activeFilter,
                        onClick  = { viewModel.setFilter(filter) },
                        label    = { Text(filter.arabicLabel, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (tasks.isEmpty()) {
                EmptyState(
                    message  = activeFilter.emptyMessage,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task        = task,
                            onToggle    = { viewModel.toggleComplete(task) },
                            onPostpone  = { viewModel.postponeToTomorrow(task) },
                            onDelete    = { viewModel.deleteTask(task) },
                            onEdit      = { editingTask = task },
                            onSoundPlay = { if (soundEnabled) CompletionSoundPlayer.play() }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        TaskFormDialog(
            titleRes       = R.string.add_task,
            initialTitle   = "",
            initialDate    = LocalDate.now(ZoneId.systemDefault()).toString(),
            initialTime    = "",
            initialDuration = "",
            initialPriority = TaskPriority.MEDIUM,
            initialRepeat  = null,
            onDismiss      = { showAddDialog = false },
            onConfirm      = { title, date, time, durationStr, priority, repeat ->
                viewModel.addTask(title, date, priority, time, durationStr?.toIntOrNull(), repeat)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    editingTask?.let { task ->
        TaskFormDialog(
            titleRes        = R.string.edit_task_title,
            initialTitle    = task.title,
            initialDate     = task.date.orEmpty(),
            initialTime     = task.startTime.orEmpty(),
            initialDuration = task.durationMinutes?.toString().orEmpty(),
            initialPriority = task.priority,
            initialRepeat   = task.recurringRule,
            onDismiss       = { editingTask = null },
            onConfirm       = { title, date, time, durationStr, priority, repeat ->
                viewModel.updateTaskDetails(task, title, date, time, durationStr?.toIntOrNull(), priority, repeat)
                editingTask = null
            }
        )
    }
}

// ─── Task form dialog with DatePicker + TimePicker ────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormDialog(
    titleRes: Int,
    initialTitle: String,
    initialDate: String,
    initialTime: String,
    initialDuration: String,
    initialPriority: TaskPriority,
    initialRepeat: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        date: String?,
        startTime: String?,
        durationStr: String?,
        priority: TaskPriority,
        recurringRule: String?
    ) -> Unit
) {
    var title    by remember { mutableStateOf(initialTitle) }
    var date     by remember { mutableStateOf(initialDate) }
    var time     by remember { mutableStateOf(initialTime) }
    var duration by remember { mutableStateOf(initialDuration) }
    var priority by remember { mutableStateOf(initialPriority) }
    var repeat   by remember { mutableStateOf(initialRepeat) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ar"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Title
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text(stringResource(R.string.task_title_label)) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                // Date — tapping opens DatePicker dialog
                val dateDisplay = date.ifBlank { null }
                    ?.let { runCatching { LocalDate.parse(it).format(dateFmt) }.getOrElse { it } }
                    ?: stringResource(R.string.task_date_label)

                OutlinedButton(
                    onClick  = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📅  $dateDisplay")
                }

                // Time — tapping opens TimePicker dialog
                val timeDisplay = time.ifBlank { stringResource(R.string.task_start_time_label) }
                OutlinedButton(
                    onClick  = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⏰  $timeDisplay")
                }

                // Duration (numeric, optional)
                OutlinedTextField(
                    value         = duration,
                    onValueChange = { new -> if (new.all { it.isDigit() }) duration = new },
                    label         = { Text(stringResource(R.string.task_duration_label)) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                // Priority chips
                Text(stringResource(R.string.task_priority_label), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { opt ->
                        val labelRes = when (opt) {
                            TaskPriority.HIGH   -> R.string.priority_high
                            TaskPriority.MEDIUM -> R.string.priority_medium
                            TaskPriority.LOW    -> R.string.priority_low
                        }
                        FilterChip(
                            selected = priority == opt,
                            onClick  = { priority = opt },
                            label    = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Repeat chips
                Text(stringResource(R.string.task_repeat_label), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    REPEAT_OPTIONS.forEach { opt ->
                        val labelRes = when (opt) {
                            null      -> R.string.repeat_none
                            "daily"   -> R.string.repeat_daily
                            "weekly"  -> R.string.repeat_weekly
                            else      -> R.string.repeat_monthly
                        }
                        FilterChip(
                            selected = repeat == opt,
                            onClick  = { repeat = opt },
                            label    = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, date.ifBlank { null }, time.ifBlank { null },
                        duration.ifBlank { null }, priority, repeat)
                },
                enabled = title.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )

    // DatePicker dialog
    if (showDatePicker) {
        val initialMs = remember(date) {
            runCatching {
                LocalDate.parse(date)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant().toEpochMilli()
            }.getOrElse { System.currentTimeMillis() }
        }
        val dpState = rememberDatePickerState(initialSelectedDateMillis = initialMs)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        date = Instant.ofEpochMilli(ms)
                            .atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = dpState, showModeToggle = true)
        }
    }

    // TimePicker dialog
    if (showTimePicker) {
        val initHour   = time.split(":").getOrNull(0)?.toIntOrNull() ?: 9
        val initMinute = time.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        val tpState = rememberTimePickerState(
            initialHour   = initHour,
            initialMinute = initMinute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title   = { Text(stringResource(R.string.task_start_time_label)) },
            text    = { TimeInput(state = tpState) },
            confirmButton = {
                TextButton(onClick = {
                    time = "%02d:%02d".format(tpState.hour, tpState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
