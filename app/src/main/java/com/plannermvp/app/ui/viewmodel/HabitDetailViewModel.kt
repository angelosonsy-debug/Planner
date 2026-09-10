package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.repository.HabitRepository
import com.plannermvp.app.domain.habits.HabitStats
import com.plannermvp.app.domain.habits.calculateHabitStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HabitDetailUiState(
    val habit: HabitEntity,
    val stats: HabitStats,
    val checkInsByDate: Map<String, HabitCheckInEntity>
)

/**
 * habitId is passed in directly rather than pulled from a SavedStateHandle
 * nav argument — simpler to wire for an MVP than the extra
 * CreationExtras plumbing, and just as testable.
 */
class HabitDetailViewModel(repository: HabitRepository, habitId: String) : ViewModel() {

    val state: StateFlow<HabitDetailUiState?> = combine(
        repository.observeActiveHabits().map { list -> list.firstOrNull { it.id == habitId } },
        repository.observeCheckIns(habitId)
    ) { habit, checkIns ->
        habit?.let {
            HabitDetailUiState(
                habit = it,
                stats = calculateHabitStats(it, checkIns, LocalDate.now()),
                checkInsByDate = checkIns.associateBy { c -> c.date }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
