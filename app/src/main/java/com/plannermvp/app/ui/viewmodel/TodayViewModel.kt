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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayViewModel(
    private val repository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: TaskReminderScheduler,
    private val appContext: Context
) : ViewModel() {

    val summary: StateFlow<TodaySummary> = repository
        .observeByDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .map { buildTodaySummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodaySummary())

    /** Same reminder-sync logic as TasksViewModel — kept in sync since both mutate the same tasks. */
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
