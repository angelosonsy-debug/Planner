package com.plannermvp.app.domain.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class TaskReminderTimingTest {

    @Test
    fun `date and time together produce an instant`() {
        val instant = taskReminderInstant("2026-08-13", "14:30")
        assertEquals(LocalDateTime.of(2026, 8, 13, 14, 30), instant)
    }

    @Test
    fun `no start time means no reminder instant, even with a date`() {
        assertNull(taskReminderInstant("2026-08-13", null))
    }

    @Test
    fun `no date means no reminder instant, even with a time`() {
        assertNull(taskReminderInstant(null, "14:30"))
    }

    @Test
    fun `a malformed time does not crash, just yields no instant`() {
        assertNull(taskReminderInstant("2026-08-13", "not-a-time"))
    }
}
