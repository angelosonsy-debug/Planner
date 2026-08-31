package com.plannermvp.app.domain

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus

data class CompletionProgress(val completed: Int, val total: Int) {
    /** 0..100, or 0 for an empty set instead of dividing by zero. */
    val percent: Int
        get() = if (total == 0) 0 else (completed * 100) / total
}

/**
 * Generic completed/total progress over any list of tasks — used for both
 * a project's progress (Section 9/38) and Today's daily progress
 * (Section 10). Pure on purpose so it's trivially unit-testable.
 */
fun calculateCompletionProgress(tasks: List<TaskEntity>): CompletionProgress {
    val total = tasks.size
    val completed = tasks.count { it.status == TaskStatus.COMPLETED }
    return CompletionProgress(completed = completed, total = total)
}
