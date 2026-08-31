package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.core.notifications.DailyReviewReminderScheduler
import com.plannermvp.app.core.notifications.OverdueDigestScheduler
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val overdueDigestScheduler: OverdueDigestScheduler,
    private val dailyReviewReminderScheduler: DailyReviewReminderScheduler
) : ViewModel() {

    val settings: StateFlow<SettingsEntity> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsEntity())

    fun setTaskRemindersEnabled(enabled: Boolean) {
        // Existing per-task schedules are only re-applied the next time each task is
        // created/edited (see TasksViewModel.syncReminder) — turning this off doesn't
        // retroactively cancel every already-scheduled reminder in one pass.
        viewModelScope.launch { repository.setTaskRemindersEnabled(enabled) }
    }

    fun setOverdueDigestEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setOverdueDigestEnabled(enabled)
            if (enabled) overdueDigestScheduler.enable() else overdueDigestScheduler.disable()
        }
    }

    fun setDailyReviewReminder(enabled: Boolean, hour: Int, minute: Int) {
        val time = "%02d:%02d".format(hour, minute)
        viewModelScope.launch {
            repository.setDailyReviewReminder(enabled, time)
            if (enabled) dailyReviewReminderScheduler.enable(hour, minute) else dailyReviewReminderScheduler.disable()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                SettingsViewModel(app.settingsRepository, app.overdueDigestScheduler, app.dailyReviewReminderScheduler)
            }
        }
    }
}
