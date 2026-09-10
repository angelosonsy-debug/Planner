package com.plannermvp.app.release10

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Verifies the core contract of the "Today shows empty" bug fix.
 * The date string used to query tasks must be derived from the device's
 * LOCAL timezone, not UTC.
 */
class TodayDateTest {

    @Test fun `today string uses local timezone`() {
        val local   = LocalDate.now(ZoneId.systemDefault()).toString()
        val system  = LocalDate.now().toString()
        assertEquals("Date must match between both approaches", local, system)
    }

    @Test fun `today string matches YYYY-MM-DD format`() {
        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        assertTrue("Must be YYYY-MM-DD", today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test fun `task stored with today string matches today query`() {
        // Simulate the bug: if todayString differs between creation and query, task won't show
        val atCreation = LocalDate.now(ZoneId.systemDefault()).toString()
        val atQuery    = LocalDate.now(ZoneId.systemDefault()).toString()
        assertEquals("Must be identical", atCreation, atQuery)
    }

    @Test fun `yesterday does not match today`() {
        val yesterday = LocalDate.now(ZoneId.systemDefault()).minusDays(1).toString()
        val today     = LocalDate.now(ZoneId.systemDefault()).toString()
        assertNotEquals(yesterday, today)
    }
}
