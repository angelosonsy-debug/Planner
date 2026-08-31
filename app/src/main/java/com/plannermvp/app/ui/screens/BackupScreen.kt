package com.plannermvp.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.domain.backup.BackupIssueLevel
import com.plannermvp.app.ui.viewmodel.BackupUiState
import com.plannermvp.app.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupScreen(viewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory)) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = viewModel.buildExportJson()
            val success = try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                true
            } catch (e: Exception) {
                false
            }
            viewModel.onExportResult(success)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: ""
                viewModel.onBackupFilePicked(text)
            } catch (e: Exception) {
                viewModel.onBackupFilePicked("") // yields a clear "invalid file" message, not a crash
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.backup_export_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.backup_export_desc), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { exportLauncher.launch("planner-backup-${LocalDate.now()}.json") }) {
                    Text(stringResource(R.string.backup_export_button))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.backup_restore_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.backup_restore_desc), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                    Text(stringResource(R.string.backup_restore_button))
                }
            }
        }

        when (val current = state) {
            is BackupUiState.ExportResult -> {
                Text(
                    if (current.success) stringResource(R.string.backup_export_success)
                    else stringResource(R.string.backup_export_failure),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is BackupUiState.RestoreError -> {
                Text(current.message, style = MaterialTheme.typography.bodyMedium)
            }
            is BackupUiState.RestoreSuccess -> {
                Text(stringResource(R.string.backup_restore_success), style = MaterialTheme.typography.bodyMedium)
            }
            else -> Unit
        }
    }

    val previewState = state as? BackupUiState.RestorePreview
    if (previewState != null) {
        RestoreConfirmationDialog(
            summary = previewState.data.summary,
            warnings = previewState.issues.filter { it.level == BackupIssueLevel.WARNING },
            onConfirm = viewModel::confirmRestore,
            onCancel = viewModel::cancelRestore
        )
    }
}

@Composable
private fun RestoreConfirmationDialog(
    summary: com.plannermvp.app.domain.backup.BackupSummary,
    warnings: List<com.plannermvp.app.domain.backup.BackupIssue>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.backup_restore_confirm_message))
                HorizontalDivider()
                Text(
                    stringResource(
                        R.string.backup_restore_confirm_summary,
                        summary.projects, summary.tasks, summary.habits, summary.habitCheckIns
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                warnings.forEach { warning ->
                    Text(
                        stringResource(R.string.backup_restore_warning_prefix) + warning.message,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.backup_restore_confirm_button)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
