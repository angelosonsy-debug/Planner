package com.plannermvp.app.data.repository

import com.plannermvp.app.data.local.TaskDao
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.local.TaskStatus
import com.plannermvp.app.domain.matrix.MatrixQuadrant
import com.plannermvp.app.domain.matrix.toImportanceUrgency
import com.plannermvp.app.domain.tasks.nextOccurrenceDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** [followUp] is the auto-created next occurrence of a recurring task, if one was created. */
data class TaskCompletionResult(val updated: TaskEntity, val followUp: TaskEntity?)

class TaskRepository(private val taskDao: TaskDao) {

    fun observeAll(): Flow<List<TaskEntity>> = taskDao.observeAll()

    fun observeActionable(): Flow<List<TaskEntity>> = taskDao.observeActionable()

    fun observeByDate(date: String): Flow<List<TaskEntity>> = taskDao.observeByDate(date)

    fun observeByProject(projectId: String): Flow<List<TaskEntity>> = taskDao.observeByProject(projectId)

    /** Minimal creation path — Title/Date/Priority only (Section 2); Section 8/24 fields optional. */
    suspend fun createTask(
        title: String,
        date: String? = null,
        priority: TaskPriority = TaskPriority.MEDIUM,
        projectId: String? = null,
        startTime: String? = null,
        durationMinutes: Int? = null,
        recurringRule: String? = null
    ): TaskEntity {
        val task = TaskEntity(
            title = title.trim(),
            date = date,
            priority = priority,
            projectId = projectId,
            startTime = startTime,
            durationMinutes = durationMinutes,
            recurringRule = recurringRule
        )
        taskDao.insert(task)
        return task
    }

    /** Used by import to skip a task that's already in the database (Section 6). */
    suspend fun existsSimilar(title: String, date: String?, projectId: String?): Boolean =
        taskDao.countMatching(title.trim(), date, projectId) > 0

    /** Creates a task carrying the extra fields an import can supply (recurring rule, notes). */
    suspend fun createTaskFromImport(
        title: String,
        date: String?,
        priority: TaskPriority,
        projectId: String?,
        recurringRule: String?,
        notes: String?
    ) {
        taskDao.insert(
            TaskEntity(
                title = title.trim(),
                date = date,
                priority = priority,
                projectId = projectId,
                recurringRule = recurringRule,
                notes = notes
            )
        )
    }

    /**
     * Section 24/44: completing a recurring task creates its next occurrence
     * automatically — a fresh pending task the recurrence interval later,
     * carrying over the same title/project/priority/schedule/rule so the
     * chain continues. Un-completing never removes the follow-up task that
     * was already created; that's a deliberate simplification (undo just
     * undoes *this* instance, not the whole chain).
     */
    suspend fun toggleComplete(task: TaskEntity): TaskCompletionResult {
        val nowCompleted = task.status != TaskStatus.COMPLETED
        val updated = task.copy(
            status = if (nowCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING,
            completedAt = if (nowCompleted) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.update(updated)

        var followUp: TaskEntity? = null
        if (nowCompleted && task.recurringRule != null) {
            val next = nextOccurrenceDate(task.date, task.recurringRule)
            if (next != null) {
                followUp = TaskEntity(
                    title = task.title,
                    description = task.description,
                    projectId = task.projectId,
                    date = next,
                    startTime = task.startTime,
                    durationMinutes = task.durationMinutes,
                    priority = task.priority,
                    recurringRule = task.recurringRule,
                    notes = task.notes
                )
                taskDao.insert(followUp)
            }
        }
        return TaskCompletionResult(updated, followUp)
    }

    /** Today -> Postponed -> Tomorrow (Section 24). */
    suspend fun postponeToTomorrow(task: TaskEntity): TaskEntity {
        val currentDate = task.date?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
            ?: LocalDate.now()
        val updated = task.copy(
            date = currentDate.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
            status = TaskStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.update(updated)
        return updated
    }

    /** Section 24: a direct reschedule to any date, not just "tomorrow". */
    suspend fun rescheduleTo(task: TaskEntity, newDate: String?): TaskEntity {
        val updated = task.copy(date = newDate, updatedAt = System.currentTimeMillis())
        taskDao.update(updated)
        return updated
    }

    suspend fun updateTask(task: TaskEntity) =
        taskDao.update(task.copy(updatedAt = System.currentTimeMillis()))

    /** Full edit — Section 24's Date/Time/Duration, plus title/priority/recurrence. */
    suspend fun updateTaskDetails(
        task: TaskEntity,
        title: String,
        date: String?,
        startTime: String?,
        durationMinutes: Int?,
        priority: TaskPriority,
        recurringRule: String?
    ): TaskEntity {
        val updated = task.copy(
            title = title.trim(),
            date = date,
            startTime = startTime,
            durationMinutes = durationMinutes,
            priority = priority,
            recurringRule = recurringRule,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.update(updated)
        return updated
    }

    /** Section 12: dragging a task into a quadrant changes its real data, not just its position. */
    suspend fun setQuadrant(task: TaskEntity, quadrant: MatrixQuadrant) {
        val (importance, urgency) = quadrant.toImportanceUrgency()
        taskDao.update(task.copy(importance = importance, urgency = urgency, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
}
