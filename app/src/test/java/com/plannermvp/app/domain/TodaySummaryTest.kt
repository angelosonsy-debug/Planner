package com.plannermvp.app.domain

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.local.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TodaySummaryTest {

    private fun task(
        title: String,
        priority: TaskPriority = TaskPriority.MEDIUM,
        status: TaskStatus = TaskStatus.PENDING
    ) = TaskEntity(title = title, priority = priority, status = status)

    @Test
    fun `no tasks today yields an empty summary, not a crash`() {
        val summary = buildTodaySummary(emptyList())
        assertEquals(emptyList<TaskEntity>(), summary.topThree)
        assertEquals(emptyList<TaskEntity>(), summary.tasks)
        assertEquals(0, summary.progress.total)
    }

    @Test
    fun `top three are pending tasks ordered high to low priority`() {
        val tasks = listOf(
            task("Low task", TaskPriority.LOW),
            task("High task", TaskPriority.HIGH),
            task("Medium task", TaskPriority.MEDIUM)
        )
        val summary = buildTodaySummary(tasks)
        assertEquals(listOf("High task", "Medium task", "Low task"), summary.topThree.map { it.title })
    }

    @Test
    fun `completed tasks are excluded from top three but still counted in progress`() {
        val tasks = listOf(
            task("Done", TaskPriority.HIGH, TaskStatus.COMPLETED),
            task("Pending", TaskPriority.MEDIUM)
        )
        val summary = buildTodaySummary(tasks)
        assertEquals(listOf("Pending"), summary.topThree.map { it.title })
        assertEquals(1, summary.progress.completed)
        assertEquals(2, summary.progress.total)
    }

    @Test
    fun `only the first three pending tasks make the top three`() {
        val tasks = (1..5).map { task("Task $it", TaskPriority.HIGH) }
        val summary = buildTodaySummary(tasks)
        assertEquals(3, summary.topThree.size)
        assertEquals(5, summary.tasks.size)
    }
}
