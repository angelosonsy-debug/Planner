package com.plannermvp.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus
import com.plannermvp.app.ui.theme.AccentGreen

/**
 * Shared TaskRow — compact, RTL-aware.
 *
 * Release 1.0 changes:
 * - Recurring rule shown as compact horizontal chips (not vertical text)
 * - Completion sound hook (onSoundPlay) called only on PENDING→COMPLETED transition
 * - Overflow menu for edit/postpone/delete instead of always-visible buttons
 * - Muted appearance for completed tasks
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task:         TaskEntity,
    onToggle:     () -> Unit,
    onPostpone:   () -> Unit,
    onDelete:     () -> Unit,
    onEdit:       () -> Unit = {},
    onSoundPlay:  () -> Unit = {}   // called only when task transitions PENDING→COMPLETED
) {
    val isCompleted  = task.status == TaskStatus.COMPLETED
    var confirmDelete by remember { mutableStateOf(false) }
    var menuExpanded  by remember { mutableStateOf(false) }

    Surface(
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier       = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick     = onEdit,
                onLongClick = { menuExpanded = true }
            )
    ) {
        Row(
            modifier          = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox — sound fires only on PENDING → COMPLETED
            Checkbox(
                checked         = isCompleted,
                onCheckedChange = {
                    if (!isCompleted) onSoundPlay()   // only going into completed
                    onToggle()
                },
                modifier = Modifier.size(20.dp),
                colors   = CheckboxDefaults.colors(
                    checkedColor   = AccentGreen,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                // Title
                Text(
                    text           = task.title,
                    style          = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    color          = if (isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines       = 2,
                    overflow       = TextOverflow.Ellipsis
                )

                // Metadata row — date · time · duration
                val metaParts = buildList {
                    task.date?.let { add(it) }
                    task.startTime?.let { add(it) }
                    task.durationMinutes?.let { add("${it}د") }
                }.joinToString(" · ")

                if (metaParts.isNotEmpty()) {
                    Text(
                        text  = metaParts,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Recurrence — compact horizontal (NOT vertical)
                task.recurringRule?.let { rule ->
                    RecurrenceChip(rule = rule)
                }
            }

            // Priority dot
            PriorityDot(priority = task.priority)

            // Overflow menu
            Box {
                IconButton(
                    onClick  = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.MoreVert,
                        contentDescription = "خيارات",
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.action_edit)) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.action_postpone)) },
                        onClick = { menuExpanded = false; onPostpone() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text  = stringResource(R.string.action_delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = { menuExpanded = false; confirmDelete = true }
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title   = { Text(stringResource(R.string.task_delete_confirm_title)) },
            text    = { Text(stringResource(R.string.task_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

// ─── Recurrence chip ──────────────────────────────────────────────────────────
// Shows rule as compact horizontal text instead of long vertical block

@Composable
private fun RecurrenceChip(rule: String) {
    val label = when (rule.lowercase()) {
        "daily"   -> "🔁 يومي"
        "weekly"  -> "🔁 أسبوعي"
        "monthly" -> "🔁 شهري"
        else      -> "🔁 $rule"
    }
    Text(
        text  = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

// ─── Priority dot (small, not background fill) ────────────────────────────────

@Composable
private fun PriorityDot(priority: com.plannermvp.app.data.local.TaskPriority) {
    val color = when (priority) {
        com.plannermvp.app.data.local.TaskPriority.HIGH   -> com.plannermvp.app.ui.theme.PriorityHighDot
        com.plannermvp.app.data.local.TaskPriority.MEDIUM -> com.plannermvp.app.ui.theme.PriorityMediumDot
        com.plannermvp.app.data.local.TaskPriority.LOW    -> com.plannermvp.app.ui.theme.PriorityLowDot
    }
    androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp).padding(2.dp)) {
        drawCircle(color = color)
    }
}
