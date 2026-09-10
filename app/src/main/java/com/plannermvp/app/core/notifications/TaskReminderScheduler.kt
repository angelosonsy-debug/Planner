package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** One-time (not periodic) — a task happens once, unlike a daily habit. */
class TaskReminderScheduler(private val context: Context) {

    fun scheduleAt(taskId: String, instant: LocalDateTime) {
        val delayMillis = instant.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        if (delayMillis <= 0) {
            // Already in the past — don't schedule a reminder for a moment that's gone.
            cancel(taskId)
            return
        }

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(TaskReminderWorker.KEY_TASK_ID to taskId))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(workName(taskId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }

    companion object {
        fun workName(taskId: String) = "task_reminder_$taskId"
    }
}
