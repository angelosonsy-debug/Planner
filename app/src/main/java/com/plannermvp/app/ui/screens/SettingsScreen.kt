package com.plannermvp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    onBackupClick: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text     = stringResource(R.string.settings_title),
            style    = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        // ── Appearance ─────────────────────────────────────────────────────
        SettingsSectionHeader("المظهر")
        NavRow(
            icon     = Icons.Outlined.Palette,
            title    = "السمة",
            subtitle = when (settings.themeMode) {
                "light"  -> "فاتح"
                "dark"   -> "داكن"
                else     -> "تلقائي حسب الجهاز"
            },
            onClick  = { showThemeDialog = true }
        )

        // ── Task Experience ────────────────────────────────────────────────
        SettingsSectionHeader("تجربة المهام")
        SwitchRow(
            icon     = Icons.Outlined.MusicNote,
            title    = stringResource(R.string.settings_completion_sound),
            subtitle = stringResource(R.string.settings_completion_sound_desc),
            checked  = settings.completionSoundEnabled,
            onToggle = { viewModel.setCompletionSoundEnabled(it) }
        )

        // ── Navigation ─────────────────────────────────────────────────────
        SettingsSectionHeader("التنقل")
        NavRow(
            icon     = Icons.Outlined.GridView,
            title    = "تخصيص التبويبات الرئيسية",
            subtitle = "اختر ما يظهر في شريط التنقل",
            onClick  = { /* navigated from NavHost */ }
        )

        // ── Notifications ──────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.settings_notifications_section))
        SwitchRow(
            icon     = Icons.Outlined.Notifications,
            title    = stringResource(R.string.settings_task_reminders),
            subtitle = stringResource(R.string.settings_task_reminders_desc),
            checked  = settings.taskRemindersEnabled,
            onToggle = { viewModel.setTaskRemindersEnabled(it) }
        )
        SwitchRow(
            icon     = Icons.Outlined.Warning,
            title    = stringResource(R.string.settings_overdue_digest),
            subtitle = stringResource(R.string.settings_overdue_digest_desc),
            checked  = settings.overdueDigestEnabled,
            onToggle = { viewModel.setOverdueDigestEnabled(it) }
        )
        SwitchRow(
            icon     = Icons.Outlined.EventNote,
            title    = stringResource(R.string.settings_daily_review),
            subtitle = stringResource(R.string.settings_daily_review_desc),
            checked  = settings.dailyReviewReminderEnabled,
            onToggle = { viewModel.setDailyReviewReminder(it, settings.dailyReviewReminderTime) }
        )

        // ── Data ───────────────────────────────────────────────────────────
        SettingsSectionHeader(stringResource(R.string.settings_backup_section))
        NavRow(
            icon     = Icons.Outlined.Backup,
            title    = stringResource(R.string.settings_backup_restore),
            subtitle = stringResource(R.string.settings_backup_restore_desc),
            onClick  = onBackupClick
        )

        Spacer(Modifier.height(32.dp))
    }

    // Theme picker dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("اختر السمة") },
            text = {
                Column {
                    listOf("light" to "فاتح", "dark" to "داكن", "system" to "تلقائي حسب الجهاز")
                        .forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.themeMode == mode,
                                    onClick  = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                )
                                Text(label, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SwitchRow(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
