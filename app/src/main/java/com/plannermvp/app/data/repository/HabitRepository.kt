package com.plannermvp.app.data.repository

import com.plannermvp.app.data.local.HabitCheckInDao
import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitDao
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitFrequencyType
import com.plannermvp.app.data.local.HabitTargetType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: HabitCheckInDao
) {
    fun observeActiveHabits(): Flow<List<HabitEntity>> = habitDao.observeActive()

    fun observeCheckIns(habitId: String): Flow<List<HabitCheckInEntity>> = checkInDao.observeForHabit(habitId)

    /** Minimal creation path mirrors Section 15's "keep it small and achievable" defaults. */
    suspend fun createHabit(
        name: String,
        frequencyType: HabitFrequencyType = HabitFrequencyType.DAILY,
        frequencyTarget: Int = 1,
        weekdays: String? = null,
        targetType: HabitTargetType = HabitTargetType.BINARY,
        targetValue: Int = 1,
        targetUnit: String? = null,
        reminderTime: String? = null
    ): String {
        val habit = HabitEntity(
            name = name.trim(),
            frequencyType = frequencyType,
            frequencyTarget = frequencyTarget.coerceAtLeast(1),
            weekdays = weekdays,
            targetType = targetType,
            targetValue = targetValue.coerceAtLeast(1),
            targetUnit = targetUnit?.trim()?.takeIf { it.isNotBlank() },
            reminderTime = reminderTime
        )
        habitDao.insert(habit)
        return habit.id
    }

    suspend fun archiveHabit(habit: HabitEntity) {
        habitDao.update(habit.copy(active = false))
    }

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.delete(habit)

    /** Binary check-in for a given day (today by default). Toggling off deletes the row. */
    suspend fun setBinaryCheckIn(habitId: String, done: Boolean, date: String = today()) {
        if (done) {
            checkInDao.upsert(HabitCheckInEntity(habitId = habitId, date = date, value = 1))
        } else {
            checkInDao.deleteForDate(habitId, date)
        }
    }

    /** Adjusts a quantity habit's logged amount for the day by [delta] (never below zero). */
    suspend fun adjustQuantityCheckIn(habitId: String, delta: Int, date: String = today()) {
        val current = checkInDao.getForDate(habitId, date)?.value ?: 0
        val next = (current + delta).coerceAtLeast(0)
        if (next == 0) {
            checkInDao.deleteForDate(habitId, date)
        } else {
            checkInDao.upsert(HabitCheckInEntity(habitId = habitId, date = date, value = next))
        }
    }

    private fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
}
