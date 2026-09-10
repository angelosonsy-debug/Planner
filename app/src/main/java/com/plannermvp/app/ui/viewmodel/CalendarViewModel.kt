package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarUiState(
    val displayMonth:         YearMonth        = YearMonth.now(),
    val selectedDate:         LocalDate        = LocalDate.now(ZoneId.systemDefault()),
    val datesWithTasks:       Set<LocalDate>   = emptySet(),
    val tasksForSelectedDate: List<TaskEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _displayMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.systemDefault()))

    val uiState: StateFlow<CalendarUiState> = combine(
        _displayMonth,
        _selectedDate
    ) { month: YearMonth, selected: LocalDate -> Pair(month, selected) }
        .flatMapLatest { (month: YearMonth, selected: LocalDate) ->
            val selectedStr = selected.toString()   // "YYYY-MM-DD"
            val monthStr    = month.toString()       // "YYYY-MM"

            // Use observeAll + client-side filter — avoids adding new DAO method
            taskRepository.observeAll()
                .combine(taskRepository.observeByDate(selectedStr)) { allTasks: List<TaskEntity>, dayTasks: List<TaskEntity> ->
                    val datesInMonth: Set<LocalDate> = allTasks
                        .mapNotNull { t: TaskEntity ->
                            t.date
                                ?.takeIf { it.startsWith(monthStr) }
                                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        }
                        .toSet()

                    CalendarUiState(
                        displayMonth         = month,
                        selectedDate         = selected,
                        datesWithTasks       = datesInMonth,
                        tasksForSelectedDate = dayTasks
                    )
                }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState()
        )

    fun previousMonth() { _displayMonth.value = _displayMonth.value.minusMonths(1) }
    fun nextMonth()     { _displayMonth.value = _displayMonth.value.plusMonths(1) }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val ym = YearMonth.from(date)
        if (ym != _displayMonth.value) _displayMonth.value = ym
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                CalendarViewModel(app.taskRepository)
            }
        }
    }
}
