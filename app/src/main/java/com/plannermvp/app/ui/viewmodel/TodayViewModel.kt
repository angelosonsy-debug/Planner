package com.plannermvp.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.core.notifications.TaskReminderScheduler
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.repository.SettingsRepository
import com.plannermvp.app.data.repository.TaskRepository
import com.plannermvp.app.domain.TodaySummary
import com.plannermvp.app.domain.buildTodaySummary
import com.plannermvp.app.domain.tasks.taskReminderInstant
import com.plannermvp.app.widget.TodayWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class TodayViewModel(
    private val repository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: TaskReminderScheduler,
    private val appContext: Context
) : ViewModel() {

    // Release 1.0: explicit local timezone to avoid off-by-one day bug
    private val todayString: String = LocalDate.now(ZoneId.systemDefault()).toString()

    val summary: StateFlow<TodaySummary> = repository
        .observeByDate(todayString)
        .map { buildTodaySummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodaySummary())

    // Collapsible completed section
    private val _completedExpanded = MutableStateFlow(false)
    val completedExpanded: StateFlow<Boolean> = _completedExpanded.asStateFlow()

    fun toggleCompletedSection() {
        _completedExpanded.value = !_completedExpanded.value
    }

    // Completion sound enabled state
    val soundEnabled: StateFlow<Boolean> = settingsRepository.observe()
        .map { it.completionSoundEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private suspend fun syncReminder(task: TaskEntity) {
        val instant = taskReminderInstant(task.date, task.startTime)
        if (instant != null && settingsRepository.get().taskRemindersEnabled) {
            reminderScheduler.scheduleAt(task.id, instant)
        } else {
            reminderScheduler.cancel(task.id)
        }
    }

    private suspend fun refreshWidget() = TodayWidgetUpdater.refresh(appContext)

    fun toggleComplete(task: TaskEntity) {
        viewModelScope.launch {
            val result = repository.toggleComplete(task)
            reminderScheduler.cancel(result.updated.id)
            result.followUp?.let { syncReminder(it) }
            refreshWidget()
        }
    }

    fun postponeToTomorrow(task: TaskEntity) {
        viewModelScope.launch {
            val updated = repository.postponeToTomorrow(task)
            syncReminder(updated)
            refreshWidget()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            reminderScheduler.cancel(task.id)
            repository.deleteTask(task)
            refreshWidget()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                TodayViewModel(app.taskRepository, app.settingsRepository, app.taskReminderScheduler, app)
            }
        }
    }
}
