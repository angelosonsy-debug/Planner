package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Thin wrapper around WorkManager so the rest of the app never touches it directly. */
class HabitReminderScheduler(private val context: Context) {

    fun scheduleDaily(habitId: String, hour: Int, minute: Int) {
        val initialDelayMinutes = minutesUntilNextOccurrence(LocalDateTime.now(), hour, minute)

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(HabitReminderWorker.KEY_HABIT_ID to habitId))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(workName(habitId), ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    fun cancel(habitId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(habitId))
    }

    companion object {
        fun workName(habitId: String) = "habit_reminder_$habitId"
    }
}
