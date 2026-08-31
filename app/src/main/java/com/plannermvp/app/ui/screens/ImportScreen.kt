package com.plannermvp.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.domain.importing.ImportIssueLevel
import com.plannermvp.app.domain.importing.ValidatedImportItem
import com.plannermvp.app.ui.viewmodel.ImportUiState
import com.plannermvp.app.ui.viewmodel.ImportViewModel

@Composable
fun ImportScreen(viewModel: ImportViewModel = viewModel(factory = ImportViewModel.Factory)) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: ""
            viewModel.onFileContentPicked(uri.lastPathSegment, text)
        } catch (e: Exception) {
            viewModel.onFileReadFailed(context.getString(R.string.import_read_error))
        }
    }

    when (val current = state) {
        is ImportUiState.Idle -> IdleContent(
            onPickFile = { filePicker.launch(arrayOf("*/*")) }
        )
        is ImportUiState.Error -> ErrorContent(message = current.message, onRetry = viewModel::reset)
        is ImportUiState.Preview -> PreviewContent(
            state = current,
            onToggle = viewModel::toggleSelected,
            onCancel = viewModel::reset,
            onConfirm = viewModel::confirmImport
        )
        is ImportUiState.Done -> DoneContent(
            imported = current.result.imported,
            duplicates = current.result.skippedDuplicates,
            errors = current.result.skippedErrors,
            onDone = viewModel::reset
        )
    }
}

@Composable
private fun IdleContent(onPickFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.import_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.import_intro), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onPickFile) { Text(stringResource(R.string.import_select_file)) }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun DoneContent(imported: Int, duplicates: Int, errors: Int, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.import_result_format, imported, duplicates, errors),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onDone) { Text(stringResource(R.string.action_done)) }
    }
}

@Composable
private fun PreviewContent(
    state: ImportUiState.Preview,
    onToggle: (Int) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val noProjectLabel = stringResource(R.string.import_no_project)
    // group indices by project, preserving first-seen order
    val groups = linkedMapOf<String, MutableList<Int>>()
    state.items.forEachIndexed { index, item ->
        val key = item.raw.project?.trim()?.takeIf { it.isNotBlank() } ?: noProjectLabel
        groups.getOrPut(key) { mutableListOf() }.add(index)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            groups.forEach { (project, indices) ->
                item { SectionHeader(project) }
                items(indices, key = { it }) { index ->
                    ImportItemRow(
                        item = state.items[index],
                        checked = state.selected[index],
                        onToggle = { onToggle(index) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = onConfirm,
                enabled = state.selected.any { it },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.action_import)) }
        }
    }
}

@Composable
private fun ImportItemRow(item: ValidatedImportItem, checked: Boolean, onToggle: () -> Unit) {
    val statusSymbol = when {
        item.hasError -> "\u274C"
        item.hasWarning -> "\u26A0\uFE0F"
        else -> "\u2705"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = !item.hasError)
        Column(modifier = Modifier.weight(1f)) {
            Text("$statusSymbol ${item.raw.task}", style = MaterialTheme.typography.bodyLarge)
            val subtitle = listOfNotNull(
                item.resolvedDate,
                item.raw.duration,
                item.resolvedPriority.name
            ).joinToString(" · ")
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            item.issues.forEach { issue ->
                val issueLabel = if (issue.level == ImportIssueLevel.ERROR) {
                    stringResource(R.string.import_status_error)
                } else {
                    stringResource(R.string.import_status_warning)
                }
                Text("$issueLabel: ${issue.message}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
