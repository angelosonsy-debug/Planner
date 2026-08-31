package com.plannermvp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus

/**
 * Shared task row — used by the Tasks tab and the Today tab alike.
 * Tapping the title opens editing (Tasks tab only for now — Today keeps
 * onEdit as a no-op so tapping there does nothing, rather than half-wiring
 * an edit flow that writes through a different ViewModel).
 *
 * Delete asks for confirmation first (Phase 12: an irreversible action
 * had none before this) — kept local to this composable so every place
 * TaskRow is used gets it automatically, not just one screen.
 */
@Composable
fun TaskRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onPostpone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    var confirmingDelete by remember { mutableStateOf(false) }

    ListItem(
        leadingContent = {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = { onToggle() }
            )
        },
        headlineContent = {
            Text(
                text = task.title,
                textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null,
                modifier = Modifier.clickable(onClick = onEdit)
            )
        },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val scheduleText = listOfNotNull(
                    task.date ?: stringResource(R.string.no_date),
                    task.startTime,
                    task.durationMinutes?.let { "${it}m" }
                ).joinToString(" · ")
                Text(scheduleText)
                if (task.recurringRule != null) {
                    Text("\u21BB ${task.recurringRule}")
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PriorityPill(task.priority)
                TextButton(onClick = onPostpone) { Text(stringResource(R.string.action_postpone)) }
                IconButton(onClick = { confirmingDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    )

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.task_delete_confirm_title)) },
            text = { Text(stringResource(R.string.task_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}
