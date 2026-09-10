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
import com.plannermvp.app.ui.util.CompletionSoundPlayer
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

    /**
     * Whether WorkManager is ready in this process. It always is in a real
     * installed app (WorkManager's own ContentProvider initializes it before
     * any Application.onCreate() runs). It may NOT be under Robolectric or
     * non-standard launch paths, where WorkManager.getInstance() throws
     * IllegalStateException. Checking synchronously here before launching any
     * coroutine prevents the exception from ever reaching the background
     * dispatcher — which would make it uncatchable from inside the coroutine
     * and surface as UncaughtExceptionsBeforeTest.
     */
    private fun isWorkManagerReady(): Boolean = try {
        androidx.work.WorkManager.getInstance(this)
        true
    } catch (_: IllegalStateException) {
        false
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        CompletionSoundPlayer.init(this)

        // Periodic WorkManager schedules survive process death on their own, but on a
        // fresh install (or after Settings changed while the app wasn't running) there's
        // nothing to resume from — re-apply the current preferences once at startup.
        // enqueueUniquePeriodicWork(..., KEEP/REPLACE) makes this idempotent.
        //
        // The isWorkManagerReady() guard is checked synchronously before the coroutine
        // is even launched — this prevents the IllegalStateException from escaping onto
        // the background dispatcher (where it would become an UncaughtException that
        // kotlinx-coroutines-test would fail the *next* test with), which is exactly
        // what the simple try/catch-inside-the-coroutine didn't fully prevent.
        if (!isWorkManagerReady()) return

        applicationScope.launch {
            val settings = settingsRepository.get()
            if (settings.overdueDigestEnabled) {
                overdueDigestScheduler.enable()
            } else {
                overdueDigestScheduler.disable()
            }
            if (settings.dailyReviewReminderEnabled) {
                val (hour, minute) = parseHourMinute(settings.dailyReviewReminderTime)
                dailyReviewReminderScheduler.enable(hour, minute)
            } else {
                dailyReviewReminderScheduler.disable()
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
