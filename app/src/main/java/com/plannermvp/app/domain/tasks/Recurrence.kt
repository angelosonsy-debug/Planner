package com.plannermvp.app.domain.tasks

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Section 24/44: completing a recurring task should produce its next
 * occurrence automatically. Pure function — no repository, no database —
 * so the date math is testable on its own. Returns null when there's
 * nothing to advance (no rule, no date to advance from, or an
 * unrecognized rule — same "don't crash on unknown data" spirit as the
 * importer).
 */
fun nextOccurrenceDate(currentDate: String?, recurringRule: String?): String? {
    if (currentDate.isNullOrBlank() || recurringRule.isNullOrBlank()) return null
    val date = runCatching { LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() ?: return null

    val next = when (recurringRule.trim().lowercase()) {
        "daily" -> date.plusDays(1)
        "weekly" -> date.plusWeeks(1)
        "monthly" -> date.plusMonths(1)
        else -> return null
    }
    return next.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
