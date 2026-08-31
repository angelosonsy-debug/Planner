package com.plannermvp.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.plannermvp.app.PlannerApp

/**
 * Fires once a day for a habit with a reminder set. Looks the habit up
 * fresh each time rather than trusting stale input data — if it was
 * deleted or archived since the reminder was scheduled, this cancels
 * its own periodic work instead of notifying about nothing.
 */
class HabitReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
        val app = applicationContext as? PlannerApp ?: return Result.failure()
        val habit = app.database.habitDao().getById(habitId)

        if (habit == null || !habit.active || habit.reminderTime == null) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(HabitReminderScheduler.workName(habitId))
            return Result.success()
        }

        NotificationHelper.showHabitReminder(
            context = applicationContext,
            habitId = habit.id,
            habitTitle = habit.name,
            habitBody = REMINDER_BODY
        )
        return Result.success()
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        private const val REMINDER_BODY = "Time for your habit check-in."
    }
}
