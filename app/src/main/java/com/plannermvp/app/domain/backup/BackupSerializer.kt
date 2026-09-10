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
import org.json.JSONArray
import org.json.JSONObject

/** Thrown for any structurally broken or unreadable backup file — never lets a bad file crash the app. */
class BackupParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Plain org.json (already part of the Android SDK — no new dependency)
 * rather than a reflection-based serializer, so every field mapping is
 * explicit and a future schema change can't silently break an old backup
 * file without it being visible right here.
 */
object BackupSerializer {

    fun toJson(data: BackupData): String {
        val root = JSONObject()
        root.put("formatVersion", data.formatVersion)
        root.put("exportedAtEpochMillis", data.exportedAtEpochMillis)
        root.put("settings", data.settings.toJson())
        root.put("projects", JSONArray(data.projects.map { it.toJson() }))
        root.put("tasks", JSONArray(data.tasks.map { it.toJson() }))
        root.put("habits", JSONArray(data.habits.map { it.toJson() }))
        root.put("habitCheckIns", JSONArray(data.habitCheckIns.map { it.toJson() }))
        return root.toString(2)
    }

    /** Throws [BackupParseException] on anything malformed — callers must catch it. */
    fun fromJson(text: String): BackupData {
        try {
            val root = JSONObject(text)
            val formatVersion = root.getInt("formatVersion")
            if (formatVersion != BACKUP_FORMAT_VERSION) {
                throw BackupParseException("Unsupported backup format version: $formatVersion")
            }
            return BackupData(
                formatVersion = formatVersion,
                exportedAtEpochMillis = root.getLong("exportedAtEpochMillis"),
                settings = root.getJSONObject("settings").toSettingsEntity(),
                projects = root.getJSONArray("projects").map { it.toProjectEntity() },
                tasks = root.getJSONArray("tasks").map { it.toTaskEntity() },
                habits = root.getJSONArray("habits").map { it.toHabitEntity() },
                habitCheckIns = root.getJSONArray("habitCheckIns").map { it.toHabitCheckInEntity() }
            )
        } catch (e: BackupParseException) {
            throw e
        } catch (e: Exception) {
            throw BackupParseException("This file isn't a valid backup (${e.javaClass.simpleName}).", e)
        }
    }

    // ---- SettingsEntity ----

    private fun SettingsEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("themeMode", themeMode)
        putOrNull("languageOverride", languageOverride)
        put("onboardingCompleted", onboardingCompleted)
        put("taskRemindersEnabled", taskRemindersEnabled)
        put("overdueDigestEnabled", overdueDigestEnabled)
        put("dailyReviewReminderEnabled", dailyReviewReminderEnabled)
        put("dailyReviewReminderTime", dailyReviewReminderTime)
    }

    private fun JSONObject.toSettingsEntity(): SettingsEntity = SettingsEntity(
        id = optInt("id", SettingsEntity.SINGLETON_ID),
        themeMode = optString("themeMode", "system"),
        languageOverride = optStringOrNull("languageOverride"),
        onboardingCompleted = optBoolean("onboardingCompleted", false),
        taskRemindersEnabled = optBoolean("taskRemindersEnabled", true),
        overdueDigestEnabled = optBoolean("overdueDigestEnabled", true),
        dailyReviewReminderEnabled = optBoolean("dailyReviewReminderEnabled", false),
        dailyReviewReminderTime = optString("dailyReviewReminderTime", "20:00")
    )

    // ---- ProjectEntity ----

    private fun ProjectEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        putOrNull("description", description)
        putOrNull("icon", icon)
        putOrNull("color", color)
        put("status", status.name)
        putOrNull("deadline", deadline)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toProjectEntity(): ProjectEntity = ProjectEntity(
        id = getString("id"),
        name = getString("name"),
        description = optStringOrNull("description"),
        icon = optStringOrNull("icon"),
        color = optStringOrNull("color"),
        status = ProjectStatus.valueOf(getString("status")),
        deadline = optStringOrNull("deadline"),
        createdAt = getLong("createdAt")
    )

    // ---- TaskEntity ----

    private fun TaskEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        putOrNull("description", description)
        putOrNull("projectId", projectId)
        putOrNull("date", date)
        putOrNull("startTime", startTime)
        putOrNull("durationMinutes", durationMinutes)
        put("priority", priority.name)
        put("importance", importance)
        put("urgency", urgency)
        put("status", status.name)
        putOrNull("recurringRule", recurringRule)
        putOrNull("notes", notes)
        put("createdAt", createdAt)
        putOrNull("completedAt", completedAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toTaskEntity(): TaskEntity = TaskEntity(
        id = getString("id"),
        title = getString("title"),
        description = optStringOrNull("description"),
        projectId = optStringOrNull("projectId"),
        date = optStringOrNull("date"),
        startTime = optStringOrNull("startTime"),
        durationMinutes = optIntOrNull("durationMinutes"),
        priority = TaskPriority.valueOf(getString("priority")),
        importance = optBoolean("importance", false),
        urgency = optBoolean("urgency", false),
        status = TaskStatus.valueOf(getString("status")),
        recurringRule = optStringOrNull("recurringRule"),
        notes = optStringOrNull("notes"),
        createdAt = getLong("createdAt"),
        completedAt = optLongOrNull("completedAt"),
        updatedAt = getLong("updatedAt")
    )

    // ---- HabitEntity ----

    private fun HabitEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        putOrNull("description", description)
        putOrNull("icon", icon)
        putOrNull("color", color)
        put("frequencyType", frequencyType.name)
        put("frequencyTarget", frequencyTarget)
        putOrNull("weekdays", weekdays)
        put("targetType", targetType.name)
        put("targetValue", targetValue)
        putOrNull("targetUnit", targetUnit)
        putOrNull("reminderTime", reminderTime)
        put("active", active)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toHabitEntity(): HabitEntity = HabitEntity(
        id = getString("id"),
        name = getString("name"),
        description = optStringOrNull("description"),
        icon = optStringOrNull("icon"),
        color = optStringOrNull("color"),
        frequencyType = HabitFrequencyType.valueOf(getString("frequencyType")),
        frequencyTarget = optInt("frequencyTarget", 1),
        weekdays = optStringOrNull("weekdays"),
        targetType = HabitTargetType.valueOf(getString("targetType")),
        targetValue = optInt("targetValue", 1),
        targetUnit = optStringOrNull("targetUnit"),
        reminderTime = optStringOrNull("reminderTime"),
        active = optBoolean("active", true),
        createdAt = getLong("createdAt")
    )

    // ---- HabitCheckInEntity ----

    private fun HabitCheckInEntity.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("habitId", habitId)
        put("date", date)
        put("value", value)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toHabitCheckInEntity(): HabitCheckInEntity = HabitCheckInEntity(
        id = getString("id"),
        habitId = getString("habitId"),
        date = getString("date"),
        value = optInt("value", 1),
        createdAt = getLong("createdAt")
    )

    // ---- small JSONObject helpers (org.json has no built-in nullable put/get) ----

    private fun JSONObject.putOrNull(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (!has(key) || isNull(key)) null else getLong(key)

    private fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { index -> transform(getJSONObject(index)) }
}
