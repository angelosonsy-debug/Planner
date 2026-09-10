package com.plannermvp.app.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ReminderTimingTest {

    @Test
    fun `a time later today schedules for later today`() {
        val now = LocalDateTime.of(2026, 8, 13, 9, 0)
        val minutes = minutesUntilNextOccurrence(now, hour = 20, minute = 0)
        assertEquals(11 * 60, minutes) // 9:00 -> 20:00 is 11 hours
    }

    @Test
    fun `a time already passed today schedules for tomorrow`() {
        val now = LocalDateTime.of(2026, 8, 13, 9, 0)
        val minutes = minutesUntilNextOccurrence(now, hour = 8, minute = 0)
        assertEquals(23 * 60, minutes) // 9:00 today -> 8:00 tomorrow is 23 hours
    }

    @Test
    fun `the exact current minute counts as already passed, not right now`() {
        val now = LocalDateTime.of(2026, 8, 13, 9, 0)
        val minutes = minutesUntilNextOccurrence(now, hour = 9, minute = 0)
        assertEquals(24 * 60, minutes)
    }
}
