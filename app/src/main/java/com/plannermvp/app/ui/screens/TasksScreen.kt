package com.plannermvp.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.plannermvp.app.ui.viewmodel.TasksViewModel

private val REPEAT_OPTIONS = listOf(null, "daily", "weekly", "monthly")

@Composable
fun TasksScreen(viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory)) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = stringResource(R.string.tasks_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            if (tasks.isEmpty()) {
                EmptyState(
                    message = stringResource(R.string.tasks_empty),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggle = { viewModel.toggleComplete(task) },
                            onPostpone = { viewModel.postponeToTomorrow(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onEdit = { editingTask = task }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            titleRes = R.string.add_task,
            initialTitle = "",
            initialDate = "",
            initialStartTime = "",
            initialDuration = "",
            initialPriority = TaskPriority.MEDIUM,
            initialRepeat = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date, startTime, duration, priority, repeat ->
                viewModel.addTask(title, date, priority, startTime, duration, repeat)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskFormDialog(
            titleRes = R.string.edit_task_title,
            initialTitle = task.title,
            initialDate = task.date.orEmpty(),
            initialStartTime = task.startTime.orEmpty(),
            initialDuration = task.durationMinutes?.toString().orEmpty(),
            initialPriority = task.priority,
            initialRepeat = task.recurringRule,
            onDismiss = { editingTask = null },
            onConfirm = { title, date, startTime, duration, priority, repeat ->
                viewModel.updateTaskDetails(task, title, date, startTime, duration, priority, repeat)
                editingTask = null
            }
        )
    }
}

@Composable
private fun TaskFormDialog(
    titleRes: Int,
    initialTitle: String,
    initialDate: String,
    initialStartTime: String,
    initialDuration: String,
    initialPriority: TaskPriority,
    initialRepeat: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        date: String?,
        startTime: String?,
        durationMinutes: Int?,
        priority: TaskPriority,
        recurringRule: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var duration by remember { mutableStateOf(initialDuration) }
    var priority by remember { mutableStateOf(initialPriority) }
    var repeat by remember { mutableStateOf(initialRepeat) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(R.string.task_date_label)) },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text(stringResource(R.string.task_start_time_label)) },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { new -> if (new.all { it.isDigit() }) duration = new },
                    label = { Text(stringResource(R.string.task_duration_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(stringResource(R.string.task_priority_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { option ->
                        val labelRes = when (option) {
                            TaskPriority.HIGH -> R.string.priority_high
                            TaskPriority.MEDIUM -> R.string.priority_medium
                            TaskPriority.LOW -> R.string.priority_low
                        }
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }

                Text(stringResource(R.string.task_repeat_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    REPEAT_OPTIONS.forEach { option ->
                        val labelRes = when (option) {
                            null -> R.string.repeat_none
                            "daily" -> R.string.repeat_daily
                            "weekly" -> R.string.repeat_weekly
                            else -> R.string.repeat_monthly
                        }
                        FilterChip(
                            selected = repeat == option,
                            onClick = { repeat = option },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        title,
                        date.ifBlank { null },
                        startTime.ifBlank { null },
                        duration.toIntOrNull(),
                        priority,
                        repeat
                    )
                },
                enabled = title.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
