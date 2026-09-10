package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "daily_review_reminder"

/** Fires once a day at the user-chosen time (Settings), only while enabled. */
class DailyReviewReminderScheduler(private val context: Context) {

    fun enable(hour: Int, minute: Int) {
        val delay = minutesUntilNextOccurrence(LocalDateTime.now(), hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyReviewReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    fun disable() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
