package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.repository.TaskRepository
import com.plannermvp.app.domain.matrix.MatrixQuadrant
import com.plannermvp.app.domain.matrix.classifyQuadrant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MatrixUiState(
    val q1: List<TaskEntity> = emptyList(),
    val q2: List<TaskEntity> = emptyList(),
    val q3: List<TaskEntity> = emptyList(),
    val q4: List<TaskEntity> = emptyList()
)

class MatrixViewModel(private val repository: TaskRepository) : ViewModel() {

    val uiState: StateFlow<MatrixUiState> = repository.observeActionable()
        .map { tasks ->
            val today = LocalDate.now()
            val grouped = tasks.groupBy { classifyQuadrant(it, today) }
            MatrixUiState(
                q1 = grouped[MatrixQuadrant.Q1].orEmpty(),
                q2 = grouped[MatrixQuadrant.Q2].orEmpty(),
                q3 = grouped[MatrixQuadrant.Q3].orEmpty(),
                q4 = grouped[MatrixQuadrant.Q4].orEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MatrixUiState())

    fun moveTo(task: TaskEntity, quadrant: MatrixQuadrant) {
        viewModelScope.launch { repository.setQuadrant(task, quadrant) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                MatrixViewModel(app.taskRepository)
            }
        }
    }
}
