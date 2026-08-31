package com.plannermvp.app.domain

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure function, pure JUnit test — no Robolectric/Room needed, runs instantly. */
class CompletionProgressCalculatorTest {

    private fun task(status: TaskStatus) = TaskEntity(title = "t", status = status)

    @Test
    fun `empty set has zero percent, not a division error`() {
        val progress = calculateCompletionProgress(emptyList())
        assertEquals(0, progress.total)
        assertEquals(0, progress.completed)
        assertEquals(0, progress.percent)
    }

    @Test
    fun `counts only completed tasks as done`() {
        val tasks = listOf(
            task(TaskStatus.COMPLETED),
            task(TaskStatus.COMPLETED),
            task(TaskStatus.PENDING),
            task(TaskStatus.CANCELLED)
        )
        val progress = calculateCompletionProgress(tasks)
        assertEquals(4, progress.total)
        assertEquals(2, progress.completed)
        assertEquals(50, progress.percent)
    }

    @Test
    fun `rounds percent down`() {
        val tasks = listOf(task(TaskStatus.COMPLETED), task(TaskStatus.PENDING), task(TaskStatus.PENDING))
        val progress = calculateCompletionProgress(tasks)
        assertEquals(33, progress.percent) // 1/3 -> 33, not 33.33 or 34
    }
}
