package com.plannermvp.app.domain.importing

import com.plannermvp.app.data.local.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportValidatorTest {

    @Test
    fun `a clean item has no issues and keeps its values`() {
        val item = ImportedItem(task = "Learn useEffect", date = "2026-08-12", priority = "high")
        val validated = ImportValidator.validate(listOf(item)).single()

        assertTrue(validated.issues.isEmpty())
        assertEquals("2026-08-12", validated.resolvedDate)
        assertEquals(TaskPriority.HIGH, validated.resolvedPriority)
        assertFalse(validated.hasError)
    }

    @Test
    fun `a blank task title is an error`() {
        val item = ImportedItem(task = "")
        val validated = ImportValidator.validate(listOf(item)).single()
        assertTrue(validated.hasError)
    }

    @Test
    fun `an invalid date becomes null with a warning instead of blocking the import`() {
        val item = ImportedItem(task = "Task", date = "not-a-date")
        val validated = ImportValidator.validate(listOf(item)).single()

        assertEquals(null, validated.resolvedDate)
        assertTrue(validated.hasWarning)
        assertFalse(validated.hasError)
    }

    @Test
    fun `an unrecognized priority defaults to medium with a warning`() {
        val item = ImportedItem(task = "Task", priority = "urgent!!")
        val validated = ImportValidator.validate(listOf(item)).single()

        assertEquals(TaskPriority.MEDIUM, validated.resolvedPriority)
        assertTrue(validated.hasWarning)
    }

    @Test
    fun `missing priority defaults quietly to medium, no warning`() {
        val item = ImportedItem(task = "Task")
        val validated = ImportValidator.validate(listOf(item)).single()

        assertEquals(TaskPriority.MEDIUM, validated.resolvedPriority)
        assertTrue(validated.issues.isEmpty())
    }

    @Test
    fun `the second of two identical items in the same batch is flagged as a duplicate`() {
        val items = listOf(
            ImportedItem(project = "React", task = "Learn useEffect", date = "2026-08-12"),
            ImportedItem(project = "React", task = "Learn useEffect", date = "2026-08-12")
        )
        val validated = ImportValidator.validate(items)

        assertTrue(validated[0].issues.isEmpty())
        assertTrue(validated[1].hasWarning)
    }

    @Test
    fun `same title but different project is not a duplicate`() {
        val items = listOf(
            ImportedItem(project = "React", task = "Speaking"),
            ImportedItem(project = "English", task = "Speaking")
        )
        val validated = ImportValidator.validate(items)

        assertTrue(validated[0].issues.isEmpty())
        assertTrue(validated[1].issues.isEmpty())
    }
}
