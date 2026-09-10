package com.plannermvp.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.domain.importing.DatePlausibility
import com.plannermvp.app.domain.importing.ImportIssueLevel
import com.plannermvp.app.domain.importing.ValidatedImportItem
import com.plannermvp.app.ui.theme.AccentGreen
import com.plannermvp.app.ui.theme.AccentRed
import com.plannermvp.app.ui.theme.AccentAmber
import com.plannermvp.app.ui.viewmodel.ImportUiState
import com.plannermvp.app.ui.viewmodel.ImportViewModel

private const val TXT_EXAMPLE = """PROJECT: React

TASK: Learn useEffect
DATE: 2026-09-10
TIME: 19:00
DURATION: 60m
PRIORITY: high

TASK: Practice hooks
DATE: 2026-09-11
PRIORITY: medium"""

private const val CSV_EXAMPLE =
    "project,title,date,time,duration,priority\n" +
    "React,Learn useEffect,2026-09-10,19:00,60,high\n" +
    "React,Practice hooks,2026-09-11,,45,medium"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: ImportViewModel = viewModel(factory = ImportViewModel.Factory)) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showHelp by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            viewModel.onFileContentPicked(uri.lastPathSegment, text)
        } catch (e: Exception) {
            viewModel.onFileReadFailed(context.getString(R.string.import_read_error))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title row with Help button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = stringResource(R.string.import_title),
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showHelp = true }) {
                Icon(Icons.Outlined.Info, contentDescription = "مساعدة")
            }
        }

        when (val current = state) {
            is ImportUiState.Idle -> IdleContent(
                onPickFile  = { filePicker.launch(arrayOf("*/*")) },
                onPasteText = { text -> viewModel.onFileContentPicked(null, text) }
            )
            is ImportUiState.Error -> ErrorContent(current.message, viewModel::reset)
            is ImportUiState.Preview -> PreviewContent(
                state     = current,
                onToggle  = viewModel::toggleSelected,
                onCancel  = viewModel::reset,
                onConfirm = viewModel::confirmImport
            )
            is ImportUiState.Done -> DoneContent(
                imported   = current.result.imported,
                duplicates = current.result.skippedDuplicates,
                errors     = current.result.skippedErrors,
                onDone     = viewModel::reset
            )
        }
    }

    if (showHelp) {
        FormatHelpSheet(onDismiss = { showHelp = false })
    }
}

// ── Idle: File picker + Paste ─────────────────────────────────────────────────

@Composable
private fun IdleContent(onPickFile: () -> Unit, onPasteText: (String) -> Unit) {
    var inputMode by remember { mutableStateOf(0) }  // 0=file, 1=paste
    var pasteText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.import_intro), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Mode tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = inputMode == 0, onClick = { inputMode = 0 },
                label = { Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.FileOpen, null, modifier = Modifier.padding(end = 2.dp))
                    Text("ملف") } })
            FilterChip(selected = inputMode == 1, onClick = { inputMode = 1 },
                label = { Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.ContentPaste, null, modifier = Modifier.padding(end = 2.dp))
                    Text("لصق نص") } })
        }

        if (inputMode == 0) {
            OutlinedButton(onClick = onPickFile, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.import_select_file))
            }
        } else {
            OutlinedTextField(
                value         = pasteText,
                onValueChange = { pasteText = it },
                label         = { Text("الصق خطتك هنا (TXT أو CSV)") },
                modifier      = Modifier.fillMaxWidth().height(220.dp),
                textStyle     = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
            Button(
                onClick  = { if (pasteText.isNotBlank()) onPasteText(pasteText) },
                enabled  = pasteText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("معاينة الخطة") }
        }
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
            Text(message, modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer)
        }
        Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

// ── Done ──────────────────────────────────────────────────────────────────────

@Composable
private fun DoneContent(imported: Int, duplicates: Int, errors: Int, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
            Text(stringResource(R.string.import_result_format, imported, duplicates, errors),
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Button(onClick = onDone) { Text(stringResource(R.string.action_done)) }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Composable
private fun PreviewContent(
    state: ImportUiState.Preview,
    onToggle: (Int) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val noProjectLabel = stringResource(R.string.import_no_project)
    val groups = linkedMapOf<String, MutableList<Int>>()
    state.items.forEachIndexed { index, item ->
        val key = item.raw.project?.trim()?.takeIf { it.isNotBlank() } ?: noProjectLabel
        groups.getOrPut(key) { mutableListOf() }.add(index)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Anomaly banner
        val analysis = state.dateAnalysis
        if (analysis != null && analysis.anomalyDetected) {
            Surface(
                color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text     = "⚠️ ${analysis.suspicious} من ${analysis.total} مهمة بتواريخ قديمة جدًا — راجع التواريخ قبل الاستيراد.",
                    modifier = Modifier.padding(10.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            groups.forEach { (project, indices) ->
                item {
                    Text(project, style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
                items(indices, key = { it }) { index ->
                    ImportItemRow(state.items[index], state.selected[index]) { onToggle(index) }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            val selectedCount = state.selected.count { it }
            Button(
                onClick  = onConfirm,
                enabled  = selectedCount > 0,
                modifier = Modifier.weight(1f)
            ) { Text("استيراد $selectedCount مهمة") }
        }
    }
}

// ── Import item row ───────────────────────────────────────────────────────────

@Composable
private fun ImportItemRow(item: ValidatedImportItem, checked: Boolean, onToggle: () -> Unit) {
    val icon = when {
        item.hasError   -> "❌"
        item.hasWarning -> "⚠️"
        else            -> "✅"
    }
    val iconColor = when {
        item.hasError   -> AccentRed
        item.hasWarning -> AccentAmber
        else            -> AccentGreen
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = !item.hasError)

        Column(modifier = Modifier.weight(1f)) {
            Text("$icon ${item.raw.task}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)

            // Date with plausibility color
            val dateColor = when (item.datePlausibility) {
                DatePlausibility.VERY_OLD, DatePlausibility.OLD_PAST -> AccentAmber
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val metaParts = listOfNotNull(item.resolvedDate,
                item.raw.duration, item.resolvedPriority?.name?.lowercase()).joinToString(" · ")
            if (metaParts.isNotEmpty()) {
                Text(metaParts, style = MaterialTheme.typography.labelMedium, color = dateColor)
            }

            item.issues.forEach { issue ->
                val color = if (issue.level == ImportIssueLevel.ERROR)
                    MaterialTheme.colorScheme.error else AccentAmber
                Text(issue.message, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

// ── Format help bottom sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatHelpSheet(onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Text("كيف أكتب خطة الاستيراد؟",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Text("مثال TXT:", style = MaterialTheme.typography.labelLarge)
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Text(TXT_EXAMPLE, modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            }
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(TXT_EXAMPLE)) },
                modifier = Modifier.fillMaxWidth()) {
                Text("نسخ مثال TXT")
            }

            Spacer(Modifier.height(4.dp))
            Text("مثال CSV:", style = MaterialTheme.typography.labelLarge)
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Text(CSV_EXAMPLE, modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            }
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(CSV_EXAMPLE)) },
                modifier = Modifier.fillMaxWidth()) {
                Text("نسخ مثال CSV")
            }

            Spacer(Modifier.height(4.dp))
            Text("الحقول المدعومة: PROJECT، TASK، DATE (YYYY-MM-DD)، TIME (HH:MM)، DURATION (60m)، PRIORITY (high/medium/low).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
