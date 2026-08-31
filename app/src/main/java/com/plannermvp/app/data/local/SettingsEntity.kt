package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding app-wide settings (Section 31 of the product spec).
 * We key it by a fixed id so there is always exactly one row.
 *
 * The three notification toggles are Phase 9 (Section 29): task-start
 * reminders and the overdue digest default ON since they're low-noise and
 * opt-out; the daily review reminder defaults OFF since it's a
 * higher-commitment, opt-in habit (Section 26 review isn't a built screen
 * yet, so this is just the reminder scaffolding for when it is).
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val themeMode: String = "system", // "light" | "dark" | "system"
    val languageOverride: String? = null, // null = follow system locale
    val onboardingCompleted: Boolean = false,
    val taskRemindersEnabled: Boolean = true,
    val overdueDigestEnabled: Boolean = true,
    val dailyReviewReminderEnabled: Boolean = false,
    /** "HH:mm", 24-hour. Only used when dailyReviewReminderEnabled is true. */
    val dailyReviewReminderTime: String = "20:00"
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
