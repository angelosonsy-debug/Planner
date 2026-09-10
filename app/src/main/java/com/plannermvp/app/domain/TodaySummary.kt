package com.plannermvp.app.domain

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.local.TaskStatus

data class TodaySummary(
    val topThree: List<TaskEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val progress: CompletionProgress = CompletionProgress(0, 0)
)

private val TaskPriority.sortRank: Int
    get() = when (this) {
        TaskPriority.HIGH -> 0
        TaskPriority.MEDIUM -> 1
        TaskPriority.LOW -> 2
    }

/**
 * Today screen logic (Section 10): pending tasks sorted by priority first,
 * the top 3 pending ones highlighted separately, plus daily progress over
 * *all* of today's tasks (completed ones count toward progress even though
 * they drop out of the sorted-to-top list). Pure function — no ViewModel,
 * no database — so it's unit-testable on its own.
 */
fun buildTodaySummary(tasksForToday: List<TaskEntity>): TodaySummary {
    val sorted = tasksForToday.sortedWith(
        compareBy({ it.status == TaskStatus.COMPLETED }, { it.priority.sortRank })
    )
    val topThree = sorted.filter { it.status != TaskStatus.COMPLETED }.take(3)
    return TodaySummary(
        topThree = topThree,
        tasks = sorted,
        progress = calculateCompletionProgress(tasksForToday)
    )
}
