package com.plannermvp.app

import android.app.Application
import com.plannermvp.app.core.notifications.DailyReviewReminderScheduler
import com.plannermvp.app.core.notifications.HabitReminderScheduler
import com.plannermvp.app.core.notifications.NotificationHelper
import com.plannermvp.app.core.notifications.OverdueDigestScheduler
import com.plannermvp.app.core.notifications.TaskReminderScheduler
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.repository.BackupRepository
import com.plannermvp.app.data.repository.HabitRepository
import com.plannermvp.app.data.repository.ImportRepository
import com.plannermvp.app.data.repository.ProjectRepository
import com.plannermvp.app.data.repository.SettingsRepository
import com.plannermvp.app.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class. Holds one Room database and its repositories for the
 * whole app's lifetime — small enough for MVP that a DI framework (Hilt)
 * isn't worth the setup cost yet (Section 42: don't over-abstract).
 */
class PlannerApp : Application() {

    /** Application-scoped: only used for the one-shot startup sync below. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val projectRepository: ProjectRepository by lazy { ProjectRepository(database.projectDao()) }
    val importRepository: ImportRepository by lazy { ImportRepository(taskRepository, projectRepository) }
    val habitRepository: HabitRepository by lazy { HabitRepository(database.habitDao(), database.habitCheckInDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(database.settingsDao()) }
    val backupRepository: BackupRepository by lazy { BackupRepository(database) }

    val habitReminderScheduler: HabitReminderScheduler by lazy { HabitReminderScheduler(this) }
    val taskReminderScheduler: TaskReminderScheduler by lazy { TaskReminderScheduler(this) }
    val overdueDigestScheduler: OverdueDigestScheduler by lazy { OverdueDigestScheduler(this) }
    val dailyReviewReminderScheduler: DailyReviewReminderScheduler by lazy { DailyReviewReminderScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)

        // Periodic WorkManager schedules survive process death on their own, but on a
        // fresh install (or after Settings changed while the app wasn't running) there's
        // nothing to resume from — re-apply the current preferences once at startup.
        // enqueueUniquePeriodicWork(..., KEEP/REPLACE) below makes this idempotent.
        //
        // Wrapped in try/catch: under Robolectric (and possibly other non-standard
        // launch paths), WorkManager isn't guaranteed to be initialized by the time
        // Application.onCreate() runs here, and WorkManager.getInstance() throws
        // IllegalStateException if it isn't. In a real app install this never
        // triggers — WorkManager auto-initializes via its own manifest-declared
        // ContentProvider before any Application.onCreate() runs — so this only
        // guards test/non-standard environments, never masks a real production issue.
        applicationScope.launch {
            try {
                val settings = settingsRepository.get()
                if (settings.overdueDigestEnabled) overdueDigestScheduler.enable() else overdueDigestScheduler.disable()
                if (settings.dailyReviewReminderEnabled) {
                    val (hour, minute) = parseHourMinute(settings.dailyReviewReminderTime)
                    dailyReviewReminderScheduler.enable(hour, minute)
                } else {
                    dailyReviewReminderScheduler.disable()
                }
            } catch (e: IllegalStateException) {
                // WorkManager not initialized in this environment — see comment above.
            }
        }
    }

    private fun parseHourMinute(hhmm: String): Pair<Int, Int> {
        val parts = hhmm.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour to minute
    }
}
