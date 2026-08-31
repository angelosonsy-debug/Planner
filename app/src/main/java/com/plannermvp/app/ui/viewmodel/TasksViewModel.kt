package com.plannermvp.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.core.notifications.TaskReminderScheduler
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.repository.SettingsRepository
import com.plannermvp.app.data.repository.TaskRepository
import com.plannermvp.app.domain.tasks.taskReminderInstant
import com.plannermvp.app.widget.TodayWidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: TaskReminderScheduler,
    private val appContext: Context
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Reschedules (or cancels) the reminder for [task] based on its current date/time and Settings. */
    private suspend fun syncReminder(task: TaskEntity) {
        val instant = taskReminderInstant(task.date, task.startTime)
        if (instant != null && settingsRepository.get().taskRemindersEnabled) {
            reminderScheduler.scheduleAt(task.id, instant)
        } else {
            reminderScheduler.cancel(task.id)
        }
    }

    private suspend fun refreshWidget() = TodayWidgetUpdater.refresh(appContext)

    fun addTask(
        title: String,
        date: String?,
        priority: TaskPriority,
        startTime: String? = null,
        durationMinutes: Int? = null,
        recurringRule: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val created = repository.createTask(
                title = title,
                date = date,
                priority = priority,
                startTime = startTime,
                durationMinutes = durationMinutes,
                recurringRule = recurringRule
            )
            syncReminder(created)
            refreshWidget()
        }
    }

    fun updateTaskDetails(
        task: TaskEntity,
        title: String,
        date: String?,
        startTime: String?,
        durationMinutes: Int?,
        priority: TaskPriority,
        recurringRule: String?
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val updated = repository.updateTaskDetails(task, title, date, startTime, durationMinutes, priority, recurringRule)
            syncReminder(updated)
            refreshWidget()
        }
    }

    fun toggleComplete(task: TaskEntity) {
        viewModelScope.launch {
            val result = repository.toggleComplete(task)
            reminderScheduler.cancel(result.updated.id) // a completed/reopened task never needs its old reminder
            result.followUp?.let { syncReminder(it) } // the next occurrence gets its own, if it has a time
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
                TasksViewModel(app.taskRepository, app.settingsRepository, app.taskReminderScheduler, app)
            }
        }
    }
}
