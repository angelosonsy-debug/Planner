package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.plannermvp.app.PlannerApp

class DailyReviewReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? PlannerApp ?: return Result.failure()
        if (app.settingsRepository.get().dailyReviewReminderEnabled) {
            NotificationHelper.showDailyReviewReminder(applicationContext)
        }
        return Result.success()
    }
}
