package com.plannermvp.app.domain.tasks

import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus
import java.time.LocalDate

/** Section 29: a task counts as overdue once its date has passed and it's still actionable. */
fun overdueTasks(tasks: List<TaskEntity>, today: LocalDate): List<TaskEntity> =
    tasks.filter { task ->
        val date = task.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        date != null && date.isBefore(today) &&
            task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED
    }
