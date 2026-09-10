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
        viewModelScope.launch { repository.setTaskRemindersEnabled(enabled) }
    }

    fun setOverdueDigestEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setOverdueDigestEnabled(enabled)
            if (enabled) overdueDigestScheduler.enable() else overdueDigestScheduler.disable()
        }
    }

    /** Called from SettingsScreen which passes the existing time string. */
    fun setDailyReviewReminder(enabled: Boolean, time: String) {
        val parts  = time.split(":")
        val hour   = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        viewModelScope.launch {
            repository.setDailyReviewReminder(enabled, time)
            if (enabled) dailyReviewReminderScheduler.enable(hour, minute)
            else dailyReviewReminderScheduler.disable()
        }
    }

    /** Release 1.0: toggle completion sound. */
    fun setCompletionSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setCompletionSoundEnabled(enabled) }
    }

    /** Release 1.0: change theme (light/dark/system). */
    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                SettingsViewModel(
                    app.settingsRepository,
                    app.overdueDigestScheduler,
                    app.dailyReviewReminderScheduler
                )
            }
        }
    }
}
