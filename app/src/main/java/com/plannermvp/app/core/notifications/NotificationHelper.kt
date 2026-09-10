package com.plannermvp.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Section 20/29: separate channels so a user who mutes one kind of
 * reminder doesn't have to mute all of them. Task-start reminders sit at
 * DEFAULT importance (time-sensitive — the user chose a specific minute
 * for a reason); the overdue digest and daily review nudge sit at LOW
 * (quiet, no sound/heads-up — "don't be annoying" per Section 29).
 */
object NotificationHelper {

    const val HABIT_CHANNEL_ID = "habit_reminders"
    const val TASK_CHANNEL_ID = "task_reminders"
    const val DIGEST_CHANNEL_ID = "digest_reminders"

    const val ACTION_COMPLETE_TASK = "com.plannermvp.app.action.COMPLETE_TASK"
    const val EXTRA_TASK_ID = "task_id"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(HABIT_CHANNEL_ID, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily reminders for habits you've set a reminder time on."
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(TASK_CHANNEL_ID, "Task reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A reminder at a task's scheduled start time."
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(DIGEST_CHANNEL_ID, "Daily summaries", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Overdue tasks digest and the daily review nudge — quiet, no sound."
            }
        )
    }

    private fun hasPostPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Best-effort: silently does nothing if the permission was never granted (Android 13+). */
    fun showHabitReminder(context: Context, habitId: String, habitTitle: String, habitBody: String) {
        if (!hasPostPermission(context)) return
        val notification = NotificationCompat.Builder(context, HABIT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(habitTitle)
            .setContentText(habitBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    /** Includes a "Mark complete" action so the reminder is actionable, not just informational. */
    fun showTaskReminder(context: Context, taskId: String, taskTitle: String) {
        if (!hasPostPermission(context)) return

        val completeIntent = Intent(ACTION_COMPLETE_TASK).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(taskTitle)
            .setContentText("Starting now")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(android.R.drawable.checkbox_on_background, "Mark complete", completePendingIntent)
            .build()
        NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
    }

    fun showOverdueDigest(context: Context, overdueCount: Int) {
        if (!hasPostPermission(context) || overdueCount <= 0) return
        val notification = NotificationCompat.Builder(context, DIGEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Overdue tasks")
            .setContentText("You have $overdueCount overdue task${if (overdueCount == 1) "" else "s"}.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        NotificationManagerCompat.from(context).notify(OVERDUE_DIGEST_NOTIFICATION_ID, notification)
    }

    fun showDailyReviewReminder(context: Context) {
        if (!hasPostPermission(context)) return
        val notification = NotificationCompat.Builder(context, DIGEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Daily review")
            .setContentText("Take a minute to review today before you wrap up.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        NotificationManagerCompat.from(context).notify(DAILY_REVIEW_NOTIFICATION_ID, notification)
    }

    fun dismiss(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private const val OVERDUE_DIGEST_NOTIFICATION_ID = -1001
    private const val DAILY_REVIEW_NOTIFICATION_ID = -1002
}
