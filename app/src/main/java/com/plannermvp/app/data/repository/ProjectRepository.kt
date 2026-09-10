package com.plannermvp.app.data.repository

import com.plannermvp.app.data.local.ProjectDao
import com.plannermvp.app.data.local.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    fun observeAll(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    suspend fun createProject(name: String, description: String? = null, deadline: String? = null) {
        projectDao.insert(
            ProjectEntity(name = name.trim(), description = description?.trim(), deadline = deadline)
        )
    }

    /** Used by import (Section 39-ish: tasks can reference a project by name). */
    suspend fun findOrCreateProject(name: String): String {
        val trimmed = name.trim()
        val existing = projectDao.findByName(trimmed)
        if (existing != null) return existing.id

        val created = ProjectEntity(name = trimmed)
        projectDao.insert(created)
        return created.id
    }

    suspend fun updateProject(project: ProjectEntity) = projectDao.update(project)

    suspend fun deleteProject(project: ProjectEntity) = projectDao.delete(project)
}
