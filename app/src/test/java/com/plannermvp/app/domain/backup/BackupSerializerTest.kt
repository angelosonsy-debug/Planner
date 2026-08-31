package com.plannermvp.app.domain.backup

import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitFrequencyType
import com.plannermvp.app.data.local.HabitTargetType
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.data.local.ProjectStatus
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.local.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Runs under Robolectric even though it touches no Room/Context: org.json
 * is part of the Android SDK, not the JVM, so a plain (non-Robolectric)
 * unit test only sees the stub android.jar — with this module's
 * `isReturnDefaultValues = true`, every JSONObject/JSONArray call would
 * silently return null/defaults instead of doing anything, which is
 * exactly what caused this test to NPE before this was added. Robolectric
 * loads the real android-all jar, giving org.json its actual behavior.
 */
@RunWith(RobolectricTestRunner::class)
class BackupSerializerTest {

    private fun sampleData(): BackupData = BackupData(
        exportedAtEpochMillis = 1_723_000_000_000L,
        settings = SettingsEntity(
            themeMode = "dark",
            languageOverride = "en",
            onboardingCompleted = true,
            taskRemindersEnabled = false,
            overdueDigestEnabled = true,
            dailyReviewReminderEnabled = true,
            dailyReviewReminderTime = "21:30"
        ),
        projects = listOf(
            ProjectEntity(
                id = "p1", name = "React", description = "Learn hooks", icon = "code", color = "#123456",
                status = ProjectStatus.ACTIVE, deadline = "2026-09-01", createdAt = 1L
            ),
            ProjectEntity(id = "p2", name = "No extras", createdAt = 2L)
        ),
        tasks = listOf(
            TaskEntity(
                id = "t1", title = "Learn useEffect", description = "read docs", projectId = "p1",
                date = "2026-08-12", startTime = "14:00", durationMinutes = 60,
                priority = TaskPriority.HIGH, importance = true, urgency = true,
                status = TaskStatus.COMPLETED, recurringRule = "daily", notes = "some notes",
                createdAt = 10L, completedAt = 20L, updatedAt = 30L
            ),
            TaskEntity(id = "t2", title = "Bare minimum task", createdAt = 40L, updatedAt = 40L)
        ),
        habits = listOf(
            HabitEntity(
                id = "h1", name = "Reading", description = "daily reading", icon = "book", color = "#654321",
                frequencyType = HabitFrequencyType.SPECIFIC_WEEKDAYS, frequencyTarget = 3, weekdays = "MO,WE,FR",
                targetType = HabitTargetType.QUANTITY, targetValue = 10, targetUnit = "pages",
                reminderTime = "09:00", active = false, createdAt = 100L
            )
        ),
        habitCheckIns = listOf(
            HabitCheckInEntity(id = "c1", habitId = "h1", date = "2026-08-12", value = 5, createdAt = 200L)
        )
    )

    @Test
    fun `round trip preserves every field exactly`() {
        val original = sampleData()
        val json = BackupSerializer.toJson(original)
        val restored = BackupSerializer.fromJson(json)

        assertEquals(original, restored)
    }

    @Test
    fun `round trip preserves minimal entities with all-default optional fields`() {
        val original = BackupData(
            settings = SettingsEntity(),
            projects = listOf(ProjectEntity(id = "p1", name = "Bare")),
            tasks = listOf(TaskEntity(id = "t1", title = "Bare task")),
            habits = listOf(HabitEntity(id = "h1", name = "Bare habit")),
            habitCheckIns = emptyList()
        )
        val restored = BackupSerializer.fromJson(BackupSerializer.toJson(original))
        assertEquals(original, restored)
    }

    @Test
    fun `garbage input throws BackupParseException instead of crashing`() {
        assertThrows(BackupParseException::class.java) {
            BackupSerializer.fromJson("this is not json at all")
        }
    }

    @Test
    fun `a well-formed but unrecognized format version throws BackupParseException`() {
        val json = """{"formatVersion": 999, "exportedAtEpochMillis": 1, "settings": {}, "projects": [], "tasks": [], "habits": [], "habitCheckIns": []}"""
        assertThrows(BackupParseException::class.java) {
            BackupSerializer.fromJson(json)
        }
    }

    @Test
    fun `missing required fields throw BackupParseException rather than a raw JSONException leaking out`() {
        val json = """{"formatVersion": 1}"""
        assertThrows(BackupParseException::class.java) {
            BackupSerializer.fromJson(json)
        }
    }
}
