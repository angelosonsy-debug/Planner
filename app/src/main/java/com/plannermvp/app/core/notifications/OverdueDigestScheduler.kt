package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val OVERDUE_CHECK_HOUR = 9
private const val OVERDUE_CHECK_MINUTE = 0
private const val WORK_NAME = "overdue_digest_check"

/** Fires once a day, around 9am local time, if enabled in Settings. */
class OverdueDigestScheduler(private val context: Context) {

    fun enable() {
        val delay = minutesUntilNextOccurrence(LocalDateTime.now(), OVERDUE_CHECK_HOUR, OVERDUE_CHECK_MINUTE)
        val request = PeriodicWorkRequestBuilder<OverdueDigestWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun disable() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
