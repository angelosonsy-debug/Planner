package com.plannermvp.app.domain.tasks

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Section 29: a task only gets a "starting now" reminder if it has both a
 * date and a start time — a date alone isn't specific enough to notify at.
 * Pure function, no Context/WorkManager involved.
 */
fun taskReminderInstant(date: String?, startTime: String?): LocalDateTime? {
    if (date.isNullOrBlank() || startTime.isNullOrBlank()) return null
    val parsedDate = runCatching { LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        ?: return null
    val parsedTime = runCatching { LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")) }
        .getOrNull() ?: return null
    return LocalDateTime.of(parsedDate, parsedTime)
}
