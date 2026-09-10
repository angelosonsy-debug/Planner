package com.plannermvp.app.domain.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceTest {

    @Test
    fun `daily advances by exactly one day`() {
        assertEquals("2026-08-14", nextOccurrenceDate("2026-08-13", "daily"))
    }

    @Test
    fun `weekly advances by seven days`() {
        assertEquals("2026-08-20", nextOccurrenceDate("2026-08-13", "weekly"))
    }

    @Test
    fun `monthly advances by one calendar month`() {
        assertEquals("2026-09-13", nextOccurrenceDate("2026-08-13", "monthly"))
    }

    @Test
    fun `daily correctly rolls over a month boundary`() {
        assertEquals("2026-09-01", nextOccurrenceDate("2026-08-31", "daily"))
    }

    @Test
    fun `no rule means no next occurrence`() {
        assertNull(nextOccurrenceDate("2026-08-13", null))
    }

    @Test
    fun `no date to advance from means no next occurrence`() {
        assertNull(nextOccurrenceDate(null, "daily"))
    }

    @Test
    fun `an unrecognized rule is ignored rather than guessed at`() {
        assertNull(nextOccurrenceDate("2026-08-13", "fortnightly"))
    }

    @Test
    fun `a malformed date does not crash, just yields no next occurrence`() {
        assertNull(nextOccurrenceDate("not-a-date", "daily"))
    }
}
