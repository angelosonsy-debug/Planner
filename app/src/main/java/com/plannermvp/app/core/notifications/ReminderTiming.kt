package com.plannermvp.app.core.notifications

import java.time.Duration
import java.time.LocalDateTime

/**
 * Pure scheduling math, isolated from WorkManager so it's unit-testable:
 * how long from now until the next occurrence of hour:minute (today if
 * that time hasn't passed yet today, otherwise tomorrow).
 */
fun minutesUntilNextOccurrence(now: LocalDateTime, hour: Int, minute: Int): Long {
    var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!target.isAfter(now)) {
        target = target.plusDays(1)
    }
    return Duration.between(now, target).toMinutes().coerceAtLeast(0)
}
