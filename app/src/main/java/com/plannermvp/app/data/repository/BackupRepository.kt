package com.plannermvp.app.data.repository

import androidx.room.withTransaction
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.domain.backup.BackupData

/**
 * Section 32: reads/writes the full app snapshot. Kept as its own
 * repository (rather than spreading this across Task/Project/Habit
 * repositories) since backup is a cross-cutting concern touching every
 * table at once, not a feature any single existing repository owns.
 */
class BackupRepository(private val database: AppDatabase) {

    suspend fun exportData(): BackupData {
        val settings = database.settingsDao().get() ?: SettingsEntity()
        return BackupData(
            settings = settings,
            projects = database.projectDao().getAllOnce(),
            tasks = database.taskDao().getAllOnce(),
            habits = database.habitDao().getAllOnce(),
            habitCheckIns = database.habitCheckInDao().getAllOnce()
        )
    }

    /**
     * Wipes every table and re-inserts the backup's data, all inside one
     * transaction — either the whole restore lands, or (on any failure)
     * none of it does; the app never ends up half-replaced.
     *
     * Sanitizes rather than fails on the few inconsistencies
     * BackupValidator only warns about: a check-in for a habit that isn't
     * in this backup is dropped (its foreign key has nothing to point
     * at); a task whose project isn't in this backup keeps the task but
     * clears the project link, instead of the whole restore aborting
     * over a handful of bad references.
     */
    suspend fun restoreData(data: BackupData) {
        database.withTransaction {
            database.habitCheckInDao().clearAll()
            database.habitDao().clearAll()
            database.taskDao().clearAll()
            database.projectDao().clearAll()

            data.projects.forEach { database.projectDao().insert(it) }

            val restoredProjectIds = data.projects.map { it.id }.toSet()
            data.tasks.forEach { task ->
                val sanitized = if (task.projectId != null && task.projectId !in restoredProjectIds) {
                    task.copy(projectId = null)
                } else {
                    task
                }
                database.taskDao().insert(sanitized)
            }

            data.habits.forEach { database.habitDao().insert(it) }

            val restoredHabitIds = data.habits.map { it.id }.toSet()
            data.habitCheckIns
                .filter { it.habitId in restoredHabitIds }
                .forEach { database.habitCheckInDao().upsert(it) }

            database.settingsDao().upsert(data.settings.copy(id = SettingsEntity.SINGLETON_ID))
        }
    }
}
