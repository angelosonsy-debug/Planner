package com.plannermvp.app.domain.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CsvPlanParserTest {

    @Test
    fun `parses the header-defined columns regardless of order`() {
        val csv = """
            Project,Task,Date,Duration,Priority,Recurring,Notes
            React,Learn useEffect,2026-08-12,60m,high,,Read the docs first
        """.trimIndent()

        val item = CsvPlanParser.parse(csv).single()

        assertEquals("React", item.project)
        assertEquals("Learn useEffect", item.task)
        assertEquals("2026-08-12", item.date)
        assertEquals("60m", item.duration)
        assertEquals("high", item.priority)
        assertEquals("Read the docs first", item.notes)
    }

    @Test
    fun `columns can be reordered as long as the header says so`() {
        val csv = "Task,Project,Priority\nSpeaking,English,high"
        val item = CsvPlanParser.parse(csv).single()

        assertEquals("Speaking", item.task)
        assertEquals("English", item.project)
        assertEquals("high", item.priority)
    }

    @Test
    fun `date column value daily becomes recurring instead of a literal date`() {
        val csv = "Task,Date\nVocabulary,daily"
        val item = CsvPlanParser.parse(csv).single()

        assertNull(item.date)
        assertEquals("daily", item.recurring)
    }

    @Test
    fun `rows with a blank task cell are skipped`() {
        val csv = "Task,Priority\n,high\nReal task,medium"
        val items = CsvPlanParser.parse(csv)
        assertEquals(1, items.size)
        assertEquals("Real task", items.first().task)
    }

    @Test
    fun `a file with no Task column yields nothing usable`() {
        val csv = "Project,Date\nReact,2026-08-12"
        assertEquals(0, CsvPlanParser.parse(csv).size)
    }

    @Test
    fun `empty input yields an empty list, not an exception`() {
        assertEquals(0, CsvPlanParser.parse("").size)
    }
}
