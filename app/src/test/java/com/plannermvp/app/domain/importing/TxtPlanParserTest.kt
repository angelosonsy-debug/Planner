package com.plannermvp.app.domain.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TxtPlanParserTest {

    @Test
    fun `parses the spec's example format into tasks under their projects`() {
        val text = """
            PROJECT: React

            TASK: Learn useEffect
            DATE: 2026-08-12
            DURATION: 60m
            PRIORITY: high

            TASK: Practice useEffect
            DATE: 2026-08-13
            DURATION: 60m
            PRIORITY: medium

            PROJECT: English B1-B2

            TASK: Speaking
            DATE: daily
            DURATION: 20m
            PRIORITY: high
        """.trimIndent()

        val items = TxtPlanParser.parse(text)

        assertEquals(3, items.size)
        assertEquals("React", items[0].project)
        assertEquals("Learn useEffect", items[0].task)
        assertEquals("2026-08-12", items[0].date)
        assertEquals("high", items[0].priority)

        assertEquals("English B1-B2", items[2].project)
        assertEquals("Speaking", items[2].task)
    }

    @Test
    fun `DATE daily becomes a null date with recurring set instead`() {
        val text = "TASK: Vocabulary\nDATE: daily\nPRIORITY: medium"
        val item = TxtPlanParser.parse(text).single()

        assertNull(item.date)
        assertEquals("daily", item.recurring)
    }

    @Test
    fun `blank lines are ignored`() {
        val text = "TASK: A\n\n\nDATE: 2026-08-12\n\nTASK: B"
        val items = TxtPlanParser.parse(text)
        assertEquals(2, items.size)
    }

    @Test
    fun `an unknown field is preserved in notes instead of being dropped`() {
        val text = "TASK: Vocabulary\nCOUNT: 15 words\nPRIORITY: medium"
        val item = TxtPlanParser.parse(text).single()
        assertTrue(item.notes?.contains("15 words") == true)
    }

    @Test
    fun `a task with no title is skipped rather than crashing`() {
        val text = "TASK:\nDATE: 2026-08-12"
        val items = TxtPlanParser.parse(text)
        assertEquals(0, items.size)
    }

    @Test
    fun `empty input yields an empty list, not an exception`() {
        assertEquals(0, TxtPlanParser.parse("").size)
    }
}
