package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class HabitFrequencyType { DAILY, TIMES_PER_WEEK, SPECIFIC_WEEKDAYS, WEEKLY, CUSTOM }
enum class HabitTargetType { BINARY, QUANTITY }

/**
 * Habit model per Section 14 of the product spec. Small, achievable
 * targets by design (Section 15) — targetValue defaults to 1.
 *
 * reminderTime is now wired to a real daily Android notification via
 * WorkManager (see core/notifications/) — not just stored data anymore.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val frequencyType: HabitFrequencyType = HabitFrequencyType.DAILY,
    /** e.g. 3 for "3 times/week"; ignored for other frequency types. */
    val frequencyTarget: Int = 1,
    /** Comma-separated two-letter day codes for SPECIFIC_WEEKDAYS, e.g. "MO,WE,FR". */
    val weekdays: String? = null,
    val targetType: HabitTargetType = HabitTargetType.BINARY,
    /** e.g. 2 for "2 pages/day"; ignored for BINARY (always effectively 1). */
    val targetValue: Int = 1,
    /** Freeform unit label for quantity habits: "pages", "words", "minutes". */
    val targetUnit: String? = null,
    /** "HH:mm", 24-hour. Non-null means a daily reminder is scheduled for this habit. */
    val reminderTime: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
