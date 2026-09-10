package com.plannermvp.app.release10

import com.plannermvp.app.domain.importing.DatePlausibility
import com.plannermvp.app.domain.importing.ImportDateSanityChecker
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ImportDateSanityTest {

    private val today = LocalDate.now(ZoneId.systemDefault()).toString()

    @Test fun `today is VALID`() = assertEquals(DatePlausibility.VALID, ImportDateSanityChecker.evaluate(today))

    @Test fun `tomorrow is VALID`() {
        val tomorrow = LocalDate.now(ZoneId.systemDefault()).plusDays(1).toString()
        assertEquals(DatePlausibility.VALID, ImportDateSanityChecker.evaluate(tomorrow))
    }

    @Test fun `15 days ago is RECENT_PAST`() {
        val date = LocalDate.now(ZoneId.systemDefault()).minusDays(15).toString()
        assertEquals(DatePlausibility.RECENT_PAST, ImportDateSanityChecker.evaluate(date))
    }

    @Test fun `90 days ago is OLD_PAST`() {
        val date = LocalDate.now(ZoneId.systemDefault()).minusDays(90).toString()
        assertEquals(DatePlausibility.OLD_PAST, ImportDateSanityChecker.evaluate(date))
    }

    @Test fun `2023 date is VERY_OLD in 2026`() {
        assertEquals(DatePlausibility.VERY_OLD, ImportDateSanityChecker.evaluate("2023-01-01"))
    }

    @Test fun `3 years future is FAR_FUTURE`() {
        val date = LocalDate.now(ZoneId.systemDefault()).plusYears(3).toString()
        assertEquals(DatePlausibility.FAR_FUTURE, ImportDateSanityChecker.evaluate(date))
    }

    @Test fun `VALID and RECENT_PAST checked by default`() {
        assertTrue(DatePlausibility.VALID.isDefaultChecked)
        assertTrue(DatePlausibility.RECENT_PAST.isDefaultChecked)
    }

    @Test fun `OLD_PAST and VERY_OLD unchecked by default`() {
        assertFalse(DatePlausibility.OLD_PAST.isDefaultChecked)
        assertFalse(DatePlausibility.VERY_OLD.isDefaultChecked)
    }

    @Test fun `40 percent suspicious triggers anomaly`() {
        val dates = (1..40).map { "2023-01-01" } +
                    (1..60).map { LocalDate.now(ZoneId.systemDefault()).plusDays(it.toLong()).toString() }
        val summary = ImportDateSanityChecker.analyseAll(dates)
        assertTrue(summary.anomalyDetected)
    }

    @Test fun `25 percent suspicious does not trigger anomaly`() {
        val dates = (1..25).map { "2023-01-01" } +
                    (1..75).map { LocalDate.now(ZoneId.systemDefault()).plusDays(it.toLong()).toString() }
        val summary = ImportDateSanityChecker.analyseAll(dates)
        assertFalse(summary.anomalyDetected)
    }

    @Test fun `empty list produces no anomaly`() {
        assertFalse(ImportDateSanityChecker.analyseAll(emptyList()).anomalyDetected)
    }

    @Test fun `null dates excluded from analysis`() {
        val summary = ImportDateSanityChecker.analyseAll(listOf(null, null, today))
        assertEquals(1, summary.total)
    }
}
