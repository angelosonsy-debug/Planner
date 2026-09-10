package com.plannermvp.app.domain.habits

import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitTargetType
import java.time.LocalDate

data class HabitStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completionsThisWeek: Int = 0,
    val completionsThisMonth: Int = 0,
    val totalCompletions: Int = 0,
    /** % of the last 30 days that were completed. */
    val completionRatePercent: Int = 0
)

/** Was this day's check-in enough to count as "done" for the habit? */
fun isCheckInComplete(habit: HabitEntity, checkIn: HabitCheckInEntity?): Boolean {
    if (checkIn == null) return false
    return when (habit.targetType) {
        HabitTargetType.BINARY -> checkIn.value >= 1
        HabitTargetType.QUANTITY -> checkIn.value >= habit.targetValue
    }
}

/**
 * Section 17/19/44: everything here is derived from check-in rows, never
 * stored as its own fact — so there's no way for a streak counter to drift
 * out of sync with reality (Section 44: "لا تخزن الـstreak كحقيقة وحيدة").
 * Pure function, no database, no ViewModel — trivially unit-testable.
 */
fun calculateHabitStats(habit: HabitEntity, checkIns: List<HabitCheckInEntity>, today: LocalDate): HabitStats {
    val doneDates: Set<LocalDate> = checkIns
        .filter { isCheckInComplete(habit, it) }
        .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        .toSet()

    if (doneDates.isEmpty()) return HabitStats()

    // Current streak: consecutive done-days ending today, or ending yesterday
    // if today hasn't been checked in yet (a day isn't "missed" until it's over).
    var currentStreak = 0
    var cursor = if (doneDates.contains(today)) today else today.minusDays(1)
    while (doneDates.contains(cursor)) {
        currentStreak++
        cursor = cursor.minusDays(1)
    }

    // Best streak ever: longest run of consecutive done-days in the whole history.
    var bestStreak = 0
    var run = 0
    var previous: LocalDate? = null
    for (date in doneDates.sorted()) {
        run = if (previous != null && previous.plusDays(1) == date) run + 1 else 1
        bestStreak = maxOf(bestStreak, run)
        previous = date
    }

    val weekStart = today.minusDays(6)
    val monthStart = today.minusDays(29)
    val completionsThisWeek = doneDates.count { it in weekStart..today }
    val completionsThisMonth = doneDates.count { it in monthStart..today }

    return HabitStats(
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        completionsThisWeek = completionsThisWeek,
        completionsThisMonth = completionsThisMonth,
        totalCompletions = doneDates.size,
        completionRatePercent = (completionsThisMonth * 100) / 30
    )
}
