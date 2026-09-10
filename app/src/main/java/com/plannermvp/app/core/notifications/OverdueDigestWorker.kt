package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.domain.tasks.overdueTasks
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** Section 29: one consolidated notification, never one per overdue task. */
class OverdueDigestWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? PlannerApp ?: return Result.failure()
        if (!app.settingsRepository.get().overdueDigestEnabled) return Result.success()

        // One-shot read: collect the first emission rather than staying subscribed.
        val tasks = app.database.taskDao().observeActionable().first()
        val overdueCount = overdueTasks(tasks, LocalDate.now()).size
        if (overdueCount > 0) {
            NotificationHelper.showOverdueDigest(applicationContext, overdueCount)
        }
        return Result.success()
    }
}
