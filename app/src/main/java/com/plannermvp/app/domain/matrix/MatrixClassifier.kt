package com.plannermvp.app.domain.matrix

import com.plannermvp.app.data.local.TaskEntity
import java.time.LocalDate

enum class MatrixQuadrant { Q1, Q2, Q3, Q4 }

/**
 * Section 45: Task -> importance + urgency -> MatrixClassifier -> quadrant.
 * Kept as a pure, isolated function so classification logic is testable
 * without a database or a screen.
 *
 * Urgency (Section 11, "Dynamic Urgency") is the *effective* urgency: the
 * task's explicit urgency flag (set by dragging it into a quadrant,
 * Section 12) OR the fact that it's due today or overdue. A task due next
 * week isn't urgent just because someone dragged an unrelated task into
 * Q1 earlier.
 */
fun classifyQuadrant(task: TaskEntity, today: LocalDate): MatrixQuadrant {
    val urgent = task.urgency || isDueTodayOrOverdue(task.date, today)
    return when {
        task.importance && urgent -> MatrixQuadrant.Q1
        task.importance && !urgent -> MatrixQuadrant.Q2
        !task.importance && urgent -> MatrixQuadrant.Q3
        else -> MatrixQuadrant.Q4
    }
}

/** Section 12: moving a task into a quadrant sets real importance/urgency data. */
fun MatrixQuadrant.toImportanceUrgency(): Pair<Boolean, Boolean> = when (this) {
    MatrixQuadrant.Q1 -> true to true
    MatrixQuadrant.Q2 -> true to false
    MatrixQuadrant.Q3 -> false to true
    MatrixQuadrant.Q4 -> false to false
}

private fun isDueTodayOrOverdue(date: String?, today: LocalDate): Boolean {
    if (date.isNullOrBlank()) return false
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return false
    return !parsed.isAfter(today)
}
