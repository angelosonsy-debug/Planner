package com.plannermvp.app.domain.backup

import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.data.local.TaskEntity

/**
 * Section 32: everything the app knows, in one portable snapshot. This is
 * a plain domain type — not a Room entity — precisely so the backup file
 * format can stay stable even as the database schema evolves underneath
 * it (Section 32's "clear versioned format"). BACKUP_FORMAT_VERSION is
 * the thing that changes when this shape does, not the database version.
 */
const val BACKUP_FORMAT_VERSION = 1

data class BackupData(
    val formatVersion: Int = BACKUP_FORMAT_VERSION,
    val exportedAtEpochMillis: Long = System.currentTimeMillis(),
    val settings: SettingsEntity,
    val projects: List<ProjectEntity>,
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val habitCheckIns: List<HabitCheckInEntity>
) {
    val summary: BackupSummary
        get() = BackupSummary(
            projects = projects.size,
            tasks = tasks.size,
            habits = habits.size,
            habitCheckIns = habitCheckIns.size
        )
}

data class BackupSummary(
    val projects: Int,
    val tasks: Int,
    val habits: Int,
    val habitCheckIns: Int
)
