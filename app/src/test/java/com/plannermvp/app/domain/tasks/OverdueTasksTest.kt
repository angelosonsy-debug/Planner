package com.plannermvp.app.domain.tasks

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class OverdueTasksTest {

    private val today = LocalDate.of(2026, 8, 13)

    private fun task(date: String?, status: TaskStatus = TaskStatus.PENDING) =
        TaskEntity(title = "t", date = date, status = status)

    @Test
    fun `a task dated before today is overdue`() {
        val result = overdueTasks(listOf(task("2026-08-01")), today)
        assertEquals(1, result.size)
    }

    @Test
    fun `a task dated today is not overdue`() {
        val result = overdueTasks(listOf(task("2026-08-13")), today)
        assertEquals(0, result.size)
    }

    @Test
    fun `a task with no date is never overdue`() {
        val result = overdueTasks(listOf(task(null)), today)
        assertEquals(0, result.size)
    }

    @Test
    fun `a completed task is not overdue even if its date passed`() {
        val result = overdueTasks(listOf(task("2026-08-01", TaskStatus.COMPLETED)), today)
        assertEquals(0, result.size)
    }

    @Test
    fun `a cancelled task is not overdue`() {
        val result = overdueTasks(listOf(task("2026-08-01", TaskStatus.CANCELLED)), today)
        assertEquals(0, result.size)
    }

    @Test
    fun `an in-progress task past its date is still overdue`() {
        val result = overdueTasks(listOf(task("2026-08-01", TaskStatus.IN_PROGRESS)), today)
        assertEquals(1, result.size)
    }
}
