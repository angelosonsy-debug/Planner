package com.plannermvp.app.domain.habits

import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitTargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitStatsTest {

    private val today = LocalDate.of(2026, 8, 13) // a Thursday

    private fun checkIn(date: LocalDate, value: Int = 1) =
        HabitCheckInEntity(habitId = "h1", date = date.toString(), value = value)

    private fun binaryHabit() = HabitEntity(id = "h1", name = "Reading", targetType = HabitTargetType.BINARY)

    private fun quantityHabit(target: Int) =
        HabitEntity(id = "h1", name = "Vocabulary", targetType = HabitTargetType.QUANTITY, targetValue = target)

    @Test
    fun `no check-ins means every stat is zero, not a crash`() {
        val stats = calculateHabitStats(binaryHabit(), emptyList(), today)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.bestStreak)
        assertEquals(0, stats.totalCompletions)
    }

    @Test
    fun `a check-in today and yesterday gives a current streak of two`() {
        val checkIns = listOf(checkIn(today), checkIn(today.minusDays(1)))
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `not checking in today yet does not break the streak from yesterday`() {
        val checkIns = listOf(checkIn(today.minusDays(1)), checkIn(today.minusDays(2)))
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `a gap two days ago breaks the current streak even if today is done`() {
        val checkIns = listOf(checkIn(today), checkIn(today.minusDays(2))) // yesterday missing
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun `best streak finds the longest run even if it is not the current one`() {
        val checkIns = listOf(
            checkIn(today.minusDays(20)), checkIn(today.minusDays(19)), checkIn(today.minusDays(18)),
            checkIn(today.minusDays(17)), checkIn(today.minusDays(16)), // 5-day run in the past
            checkIn(today) // 1-day run now
        )
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(5, stats.bestStreak)
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun `quantity habit only counts a day as done once the target is reached`() {
        val habit = quantityHabit(target = 10)
        val checkIns = listOf(checkIn(today, value = 5)) // under target
        val stats = calculateHabitStats(habit, checkIns, today)
        assertEquals(0, stats.currentStreak)
        assertFalse(isCheckInComplete(habit, checkIns.first()))
    }

    @Test
    fun `quantity habit counts the day once the target is met or exceeded`() {
        val habit = quantityHabit(target = 10)
        val checkIns = listOf(checkIn(today, value = 12))
        assertTrue(isCheckInComplete(habit, checkIns.first()))
        assertEquals(1, calculateHabitStats(habit, checkIns, today).currentStreak)
    }

    @Test
    fun `completions this week only count the trailing seven days including today`() {
        val checkIns = (0..9).map { checkIn(today.minusDays(it.toLong())) } // 10 days straight
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(7, stats.completionsThisWeek)
        assertEquals(10, stats.totalCompletions)
    }

    @Test
    fun `completion rate is based on the trailing thirty days`() {
        val checkIns = (0..14).map { checkIn(today.minusDays(it.toLong())) } // 15 of last 30 days
        val stats = calculateHabitStats(binaryHabit(), checkIns, today)
        assertEquals(15, stats.completionsThisMonth)
        assertEquals(50, stats.completionRatePercent)
    }
}
