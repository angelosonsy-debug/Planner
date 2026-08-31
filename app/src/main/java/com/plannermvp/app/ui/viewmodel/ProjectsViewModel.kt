package com.plannermvp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.repository.ProjectRepository
import com.plannermvp.app.data.repository.TaskRepository
import com.plannermvp.app.domain.CompletionProgress
import com.plannermvp.app.domain.calculateCompletionProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectWithProgress(val project: ProjectEntity, val progress: CompletionProgress)

class ProjectsViewModel(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val projects: StateFlow<List<ProjectWithProgress>> =
        combine(projectRepository.observeAll(), taskRepository.observeAll()) { projects, allTasks ->
            val tasksByProject: Map<String, List<TaskEntity>> = allTasks.filter { it.projectId != null }
                .groupBy { it.projectId!! }
            projects.map { project ->
                ProjectWithProgress(
                    project = project,
                    progress = calculateCompletionProgress(tasksByProject[project.id].orEmpty())
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addProject(name: String, deadline: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch { projectRepository.createProject(name = name, deadline = deadline) }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { projectRepository.deleteProject(project) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PlannerApp)
                ProjectsViewModel(app.projectRepository, app.taskRepository)
            }
        }
    }
}
