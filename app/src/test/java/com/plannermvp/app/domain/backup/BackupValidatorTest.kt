package com.plannermvp.app.domain.backup

import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.data.local.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidatorTest {

    private fun emptyData(formatVersion: Int = BACKUP_FORMAT_VERSION) = BackupData(
        formatVersion = formatVersion,
        settings = SettingsEntity(),
        projects = emptyList(),
        tasks = emptyList(),
        habits = emptyList(),
        habitCheckIns = emptyList()
    )

    @Test
    fun `a clean empty backup has no issues`() {
        assertTrue(BackupValidator.validate(emptyData()).isEmpty())
    }

    @Test
    fun `an unsupported format version is a blocking error`() {
        val issues = BackupValidator.validate(emptyData(formatVersion = 999))
        assertTrue(BackupValidator.hasBlockingError(issues))
    }

    @Test
    fun `duplicate task IDs are a warning, not a blocking error`() {
        val data = emptyData().copy(
            tasks = listOf(TaskEntity(id = "t1", title = "A"), TaskEntity(id = "t1", title = "B"))
        )
        val issues = BackupValidator.validate(data)
        assertFalse(issues.isEmpty())
        assertFalse(BackupValidator.hasBlockingError(issues))
    }

    @Test
    fun `a habit check-in for a habit not in the backup is flagged`() {
        val data = emptyData().copy(
            habitCheckIns = listOf(HabitCheckInEntity(habitId = "missing-habit", date = "2026-08-12"))
        )
        val issues = BackupValidator.validate(data)
        assertTrue(issues.any { it.message.contains("check-in") })
        assertFalse(BackupValidator.hasBlockingError(issues))
    }

    @Test
    fun `a task referencing a project not in the backup is flagged but not blocking`() {
        val data = emptyData().copy(
            tasks = listOf(TaskEntity(id = "t1", title = "Orphan", projectId = "missing-project"))
        )
        val issues = BackupValidator.validate(data)
        assertTrue(issues.any { it.message.contains("project") })
        assertFalse(BackupValidator.hasBlockingError(issues))
    }

    @Test
    fun `a task whose project IS in the backup raises no issue`() {
        val data = emptyData().copy(
            projects = listOf(ProjectEntity(id = "p1", name = "React")),
            tasks = listOf(TaskEntity(id = "t1", title = "Fine", projectId = "p1"))
        )
        assertEquals(0, BackupValidator.validate(data).size)
    }
}
