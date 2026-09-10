package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row settings table. id is always SINGLETON_ID.
 *
 * Release 1.0 addition: completionSoundEnabled — short chime when a task
 * is completed (ON by default). Backed by MIGRATION_5_6.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val themeMode: String = "system",
    val languageOverride: String? = null,
    val onboardingCompleted: Boolean = false,
    val taskRemindersEnabled: Boolean = true,
    val overdueDigestEnabled: Boolean = true,
    val dailyReviewReminderEnabled: Boolean = false,
    val dailyReviewReminderTime: String = "20:00",
    /** Release 1.0: short chime on task completion. Default ON. */
    val completionSoundEnabled: Boolean = true
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
