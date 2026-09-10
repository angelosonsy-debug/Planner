package com.plannermvp.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.ui.viewmodel.ProjectWithProgress
import com.plannermvp.app.ui.viewmodel.ProjectsViewModel

@Composable
fun ProjectsScreen(viewModel: ProjectsViewModel = viewModel(factory = ProjectsViewModel.Factory)) {
    val projects      by viewModel.projects.collectAsState()
    val pendingDelete by viewModel.pendingDelete.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_project))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text     = stringResource(R.string.projects_title),
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            if (projects.isEmpty()) {
                EmptyState(
                    message  = stringResource(R.string.projects_empty),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.project.id }) { entry ->
                        ProjectCard(
                            entry     = entry,
                            onDelete  = { viewModel.requestDelete(entry.project) }
                        )
                    }
                }
            }
        }
    }

    // Release 1.0: delete confirmation dialog
    pendingDelete?.let { project ->
        DeleteProjectDialog(
            projectName = project.name,
            onConfirm   = { viewModel.confirmDelete() },
            onDismiss   = { viewModel.cancelDelete() }
        )
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, deadline ->
                viewModel.addProject(name, deadline)
                showAddDialog = false
            }
        )
    }
}

// ─── Project card ─────────────────────────────────────────────────────────────

@Composable
private fun ProjectCard(
    entry:    ProjectWithProgress,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = entry.project.name,
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(
                        onClick  = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.MoreVert,
                            contentDescription = "خيارات المشروع",
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded          = menuExpanded,
                        onDismissRequest  = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text  = "حذف",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector        = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { entry.progress.percent / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text  = stringResource(
                    R.string.project_progress_format,
                    entry.progress.completed,
                    entry.progress.total,
                    entry.progress.percent
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            entry.project.deadline?.let { deadline ->
                Text(
                    text  = stringResource(R.string.project_deadline_display_format, deadline),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ─── Delete confirmation dialog ───────────────────────────────────────────────

@Composable
private fun DeleteProjectDialog(
    projectName: String,
    onConfirm:   () -> Unit,
    onDismiss:   () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حذف المشروع؟") },
        text = {
            Column {
                Text("سيتم حذف مشروع \"$projectName\".")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = "المهام المرتبطة بهذا المشروع ستُحفظ كما هي، لكن تصنيف المشروع سيُزال منها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("حذف المشروع") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ─── Add project dialog ───────────────────────────────────────────────────────

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, deadline: String?) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_project)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text(stringResource(R.string.project_name_label)) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = deadline,
                    onValueChange = { deadline = it },
                    label         = { Text(stringResource(R.string.project_deadline_label)) },
                    placeholder   = { Text("YYYY-MM-DD") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { onConfirm(name, deadline.ifBlank { null }) },
                enabled  = name.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
