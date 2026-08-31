package com.plannermvp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val (reviewHour, reviewMinute) = remember(settings.dailyReviewReminderTime) {
        val parts = settings.dailyReviewReminderTime.split(":")
        (parts.getOrNull(0)?.toIntOrNull() ?: 20) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.settings_notifications_section),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_task_reminders)) },
            supportingContent = { Text(stringResource(R.string.settings_task_reminders_desc)) },
            trailingContent = {
                Switch(
                    checked = settings.taskRemindersEnabled,
                    onCheckedChange = { viewModel.setTaskRemindersEnabled(it) }
                )
            }
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_overdue_digest)) },
            supportingContent = { Text(stringResource(R.string.settings_overdue_digest_desc)) },
            trailingContent = {
                Switch(
                    checked = settings.overdueDigestEnabled,
                    onCheckedChange = { viewModel.setOverdueDigestEnabled(it) }
                )
            }
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_daily_review)) },
            supportingContent = { Text(stringResource(R.string.settings_daily_review_desc)) },
            trailingContent = {
                Switch(
                    checked = settings.dailyReviewReminderEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setDailyReviewReminder(enabled, reviewHour, reviewMinute)
                    }
                )
            }
        )
        if (settings.dailyReviewReminderEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.settings_review_time_label))
                SettingsStepper(value = reviewHour, range = 0..23) { newHour ->
                    viewModel.setDailyReviewReminder(true, newHour, reviewMinute)
                }
                Text(":")
                SettingsStepper(value = reviewMinute, range = 0..59) { newMinute ->
                    viewModel.setDailyReviewReminder(true, reviewHour, newMinute)
                }
            }
        }
        HorizontalDivider()

        Text(
            stringResource(R.string.settings_backup_section),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_backup_restore)) },
            supportingContent = { Text(stringResource(R.string.settings_backup_restore_desc)) },
            modifier = Modifier.clickable(onClick = onBackupClick)
        )
        HorizontalDivider()
    }
}

@Composable
private fun SettingsStepper(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (value - 1 >= range.first) onChange(value - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.stepper_decrease))
        }
        Text("%02d".format(value), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { if (value + 1 <= range.last) onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.stepper_increase))
        }
    }
}
