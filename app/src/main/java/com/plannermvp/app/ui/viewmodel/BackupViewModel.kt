package com.plannermvp.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.core.notifications.HabitReminderScheduler
import com.plannermvp.app.core.notifications.TaskReminderScheduler
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.repository.BackupRepository
import com.plannermvp.app.domain.backup.BackupData
import com.plannermvp.app.domain.backup.BackupIssue
import com.plannermvp.app.domain.backup.BackupIssueLevel
import com.plannermvp.app.domain.backup.BackupParseException
import com.plannermvp.app.domain.backup.BackupSerializer
import com.plannermvp.app.domain.backup.BackupValidator
import com.plannermvp.app.domain.tasks.taskReminderInstant
import com.plannermvp.app.widget.TodayWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data class ExportResult(val success: Boolean) : BackupUiState
    data class RestorePreview(val data: BackupData, val issues: List<BackupIssue>) : BackupUiState
    data class RestoreError(val message: String) : BackupUiState
    data object RestoreSuccess : BackupUiState
}

/**
 * File I/O (picking a location, reading/writing bytes) stays in the
 * Composable, same split as ImportViewModel: this ViewModel only ever
 * sees/returns plain strings, never a Uri or ContentResolver.
 */
class BackupViewModel(
    private val repository: BackupRepository,
    private val taskReminderScheduler: TaskReminderScheduler,
    private val habitReminderScheduler: HabitReminderScheduler,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    /** Called by the Composable right before writing the result to a picked file. */
    suspend fun buildExportJson(): String = BackupSerializer.toJson(repository.exportData())

    fun onExportResult(success: Boolean) {
        _uiState.value = BackupUiState.ExportResult(success)
    }

    /** Called by the Composable after reading the picked file's text. Never restores immediately —
     *  always stops at a preview so the person can see what they're about to replace (Section 32). */
    fun onBackupFilePicked(content: String) {
        val data = try {
            BackupSerializer.fromJson(content)
        } catch (e: BackupParseException) {
            _uiState.value = BackupUiState.RestoreError(e.message ?: "This file isn't a valid backup.")
            return
        }

        val issues = BackupValidator.validate(data)
        if (BackupValidator.hasBlockingError(issues)) {
            val message = issues.first { it.level == BackupIssueLevel.ERROR }.message
            _uiState.value = BackupUiState.RestoreError(message)
            return
        }

        _uiState.value = BackupUiState.RestorePreview(data, issues)
    }

    fun confirmRestore() {
        val state = _uiState.value as? BackupUiState.RestorePreview ?: return
        viewModelScope.launch {
            repository.restoreData(state.data)
            rescheduleReminders(state.data)
            TodayWidgetUpdater.refresh(appContext)
            _uiState.value = BackupUiState.RestoreSuccess
        }
    }

    /**
     * A restore wipes and re-inserts every table, but WorkManager jobs are
     * a separate system the restore doesn't touch — without this, a
     * restored task with a future start time, or a restored habit with a
     * reminder, would silently never notify (Phase 12 reliability review).
     * Stale jobs for tasks/habits that *aren't* in the backup are already
     * self-cleaning (see TaskReminderWorker/HabitReminderWorker), so
     * nothing needs cancelling here — only the restored data's own
     * reminders need re-scheduling.
     */
    private fun rescheduleReminders(data: BackupData) {
        data.tasks.forEach { task -> scheduleTaskReminderIfNeeded(task) }
        data.habits.forEach { habit -> scheduleHabitReminderIfNeeded(habit) }
    }

    private fun scheduleTaskReminderIfNeeded(task: TaskEntity) {
        val instant = taskReminderInstant(task.date, task.startTime) ?: return
        taskReminderScheduler.scheduleAt(task.id, instant)
    }

    private fun scheduleHabitReminderIfNeeded(habit: HabitEntity) {
        val time = habit.reminderTime ?: return
        if (!habit.active) return
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return
        habitReminderScheduler.scheduleDaily(habit.id, hour, minute)
    }

    fun cancelRestore() {
        _uiState.value = BackupUiState.Idle
    }

    fun dismiss() {
        _uiState.value = BackupUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                BackupViewModel(app.backupRepository, app.taskReminderScheduler, app.habitReminderScheduler, app)
            }
        }
    }
}
