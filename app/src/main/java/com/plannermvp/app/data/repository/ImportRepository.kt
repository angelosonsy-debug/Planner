package com.plannermvp.app.data.repository

import com.plannermvp.app.domain.importing.ValidatedImportItem

data class ImportResult(val imported: Int, val skippedDuplicates: Int, val skippedErrors: Int)

/**
 * The last two steps of the import pipeline from Section 43
 * (Preview -> User Confirmation -> Repository -> Room). Takes exactly the
 * items the user left checked in the preview and writes them, creating any
 * missing project on the fly and skipping anything that already exists.
 */
class ImportRepository(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository
) {
    suspend fun confirmImport(items: List<ValidatedImportItem>): ImportResult {
        var imported = 0
        var skippedDuplicates = 0
        var skippedErrors = 0

        for (item in items) {
            if (item.hasError) {
                skippedErrors++
                continue
            }

            val projectId = item.raw.project
                ?.takeIf { it.isNotBlank() }
                ?.let { projectRepository.findOrCreateProject(it) }

            if (taskRepository.existsSimilar(item.raw.task, item.resolvedDate, projectId)) {
                skippedDuplicates++
                continue
            }

            taskRepository.createTaskFromImport(
                title = item.raw.task,
                date = item.resolvedDate,
                priority = item.resolvedPriority,
                projectId = projectId,
                recurringRule = item.raw.recurring,
                notes = buildNotes(item)
            )
            imported++
        }

        return ImportResult(imported, skippedDuplicates, skippedErrors)
    }

    private fun buildNotes(item: ValidatedImportItem): String? {
        val parts = listOfNotNull(
            item.raw.duration?.let { "Duration: $it" },
            item.raw.notes
        )
        return parts.joinToString("; ").takeIf { it.isNotBlank() }
    }
}
