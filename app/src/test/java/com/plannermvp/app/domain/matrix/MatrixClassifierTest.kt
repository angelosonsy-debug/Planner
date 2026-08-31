package com.plannermvp.app.domain.matrix

import com.plannermvp.app.data.local.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MatrixClassifierTest {

    private val today = LocalDate.of(2026, 8, 13)

    private fun task(importance: Boolean, urgency: Boolean, date: String? = null) =
        TaskEntity(title = "t", importance = importance, urgency = urgency, date = date)

    @Test
    fun `important and explicitly urgent is Q1`() {
        assertEquals(MatrixQuadrant.Q1, classifyQuadrant(task(importance = true, urgency = true), today))
    }

    @Test
    fun `important and due today is Q1 even without the urgency flag set`() {
        val t = task(importance = true, urgency = false, date = "2026-08-13")
        assertEquals(MatrixQuadrant.Q1, classifyQuadrant(t, today))
    }

    @Test
    fun `important and overdue is Q1`() {
        val t = task(importance = true, urgency = false, date = "2026-08-01")
        assertEquals(MatrixQuadrant.Q1, classifyQuadrant(t, today))
    }

    @Test
    fun `important and due later is Q2`() {
        val t = task(importance = true, urgency = false, date = "2026-09-01")
        assertEquals(MatrixQuadrant.Q2, classifyQuadrant(t, today))
    }

    @Test
    fun `important with no date and no urgency flag is Q2`() {
        assertEquals(MatrixQuadrant.Q2, classifyQuadrant(task(importance = true, urgency = false), today))
    }

    @Test
    fun `not important but due today is Q3`() {
        val t = task(importance = false, urgency = false, date = "2026-08-13")
        assertEquals(MatrixQuadrant.Q3, classifyQuadrant(t, today))
    }

    @Test
    fun `not important and not urgent is Q4`() {
        assertEquals(MatrixQuadrant.Q4, classifyQuadrant(task(importance = false, urgency = false), today))
    }

    @Test
    fun `quadrant to importance-urgency mapping matches Section 12 exactly`() {
        assertEquals(true to true, MatrixQuadrant.Q1.toImportanceUrgency())
        assertEquals(true to false, MatrixQuadrant.Q2.toImportanceUrgency())
        assertEquals(false to true, MatrixQuadrant.Q3.toImportanceUrgency())
        assertEquals(false to false, MatrixQuadrant.Q4.toImportanceUrgency())
    }
}
