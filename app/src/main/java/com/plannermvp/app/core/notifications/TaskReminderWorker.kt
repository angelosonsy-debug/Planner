package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.data.local.TaskStatus

/**
 * Fires once at a task's scheduled start time. Looks the task up fresh —
 * if it was completed, deleted, or rescheduled since being scheduled
 * (rescheduling replaces this one-time work via TaskReminderScheduler
 * anyway, but this is a defensive second check), it simply does nothing.
 */
class TaskReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val app = applicationContext as? PlannerApp ?: return Result.failure()
        val task = app.database.taskDao().getById(taskId) ?: return Result.success()

        if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.CANCELLED) {
            return Result.success()
        }
        if (!app.settingsRepository.get().taskRemindersEnabled) {
            return Result.success()
        }

        NotificationHelper.showTaskReminder(applicationContext, task.id, task.title)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
    }
}
