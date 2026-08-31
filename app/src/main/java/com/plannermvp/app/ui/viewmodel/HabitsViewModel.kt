package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.core.notifications.HabitReminderScheduler
import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitFrequencyType
import com.plannermvp.app.data.local.HabitTargetType
import com.plannermvp.app.data.repository.HabitRepository
import com.plannermvp.app.domain.habits.HabitStats
import com.plannermvp.app.domain.habits.calculateHabitStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUi(
    val habit: HabitEntity,
    val stats: HabitStats,
    val todayCheckIn: HabitCheckInEntity?
)

class HabitsViewModel(
    private val repository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val habits: StateFlow<List<HabitUi>> = repository.observeActiveHabits()
        .flatMapLatest { habitList ->
            if (habitList.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(habitList.map { habit -> repository.observeCheckIns(habit.id).map { habit to it } }) { pairs ->
                    val today = LocalDate.now()
                    val todayIso = today.toString()
                    pairs.map { (habit, checkIns) ->
                        HabitUi(
                            habit = habit,
                            stats = calculateHabitStats(habit, checkIns, today),
                            todayCheckIn = checkIns.firstOrNull { it.date == todayIso }
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addHabit(
        name: String,
        frequencyType: HabitFrequencyType,
        frequencyTarget: Int,
        weekdays: String?,
        targetType: HabitTargetType,
        targetValue: Int,
        targetUnit: String?,
        reminderHour: Int?,
        reminderMinute: Int?
    ) {
        if (name.isBlank()) return
        val reminderTime = if (reminderHour != null && reminderMinute != null) {
            "%02d:%02d".format(reminderHour, reminderMinute)
        } else null

        viewModelScope.launch {
            val habitId = repository.createHabit(
                name = name,
                frequencyType = frequencyType,
                frequencyTarget = frequencyTarget,
                weekdays = weekdays,
                targetType = targetType,
                targetValue = targetValue,
                targetUnit = targetUnit,
                reminderTime = reminderTime
            )
            if (reminderHour != null && reminderMinute != null) {
                reminderScheduler.scheduleDaily(habitId, reminderHour, reminderMinute)
            }
        }
    }

    fun toggleBinaryToday(habit: HabitEntity, currentlyDone: Boolean) {
        viewModelScope.launch { repository.setBinaryCheckIn(habit.id, done = !currentlyDone) }
    }

    fun adjustQuantityToday(habit: HabitEntity, delta: Int) {
        viewModelScope.launch { repository.adjustQuantityCheckIn(habit.id, delta) }
    }

    fun archiveHabit(habit: HabitEntity) {
        reminderScheduler.cancel(habit.id)
        viewModelScope.launch { repository.archiveHabit(habit) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                HabitsViewModel(app.habitRepository, app.habitReminderScheduler)
            }
        }
    }
}
