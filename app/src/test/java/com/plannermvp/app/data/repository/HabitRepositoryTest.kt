package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.local.HabitTargetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HabitRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: HabitRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabitRepository(db.habitDao(), db.habitCheckInDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `creating a habit only needs a name, defaults are small and achievable`() = runTest {
        repository.createHabit(name = "Reading")

        val habit = repository.observeActiveHabits().first().single()
        assertEquals("Reading", habit.name)
        assertEquals(1, habit.targetValue)
    }

    @Test
    fun `checking in binary today then unchecking removes the check-in`() = runTest {
        repository.createHabit(name = "Reading")
        val habit = repository.observeActiveHabits().first().single()

        repository.setBinaryCheckIn(habit.id, done = true, date = "2026-08-13")
        assertEquals(1, repository.observeCheckIns(habit.id).first().size)

        repository.setBinaryCheckIn(habit.id, done = false, date = "2026-08-13")
        assertEquals(0, repository.observeCheckIns(habit.id).first().size)
    }

    @Test
    fun `checking in binary twice for the same day does not create two rows`() = runTest {
        repository.createHabit(name = "Reading")
        val habit = repository.observeActiveHabits().first().single()

        repository.setBinaryCheckIn(habit.id, done = true, date = "2026-08-13")
        repository.setBinaryCheckIn(habit.id, done = true, date = "2026-08-13")

        assertEquals(1, repository.observeCheckIns(habit.id).first().size)
    }

    @Test
    fun `adjusting quantity accumulates across calls for the same day`() = runTest {
        repository.createHabit(name = "Vocabulary", targetType = HabitTargetType.QUANTITY, targetValue = 10)
        val habit = repository.observeActiveHabits().first().single()

        repository.adjustQuantityCheckIn(habit.id, delta = 3, date = "2026-08-13")
        repository.adjustQuantityCheckIn(habit.id, delta = 4, date = "2026-08-13")

        val checkIn = repository.observeCheckIns(habit.id).first().single()
        assertEquals(7, checkIn.value)
    }

    @Test
    fun `adjusting quantity below zero clears the day's check-in instead of going negative`() = runTest {
        repository.createHabit(name = "Vocabulary", targetType = HabitTargetType.QUANTITY, targetValue = 10)
        val habit = repository.observeActiveHabits().first().single()

        repository.adjustQuantityCheckIn(habit.id, delta = 2, date = "2026-08-13")
        repository.adjustQuantityCheckIn(habit.id, delta = -5, date = "2026-08-13")

        assertNull(repository.observeCheckIns(habit.id).first().firstOrNull { it.date == "2026-08-13" })
    }

    @Test
    fun `archiving a habit removes it from the active list`() = runTest {
        repository.createHabit(name = "Temp")
        val habit = repository.observeActiveHabits().first().single()

        repository.archiveHabit(habit)

        assertEquals(0, repository.observeActiveHabits().first().size)
    }

    @Test
    fun `specific weekdays and reminder time are stored as given`() = runTest {
        repository.createHabit(
            name = "Gym",
            frequencyType = com.plannermvp.app.data.local.HabitFrequencyType.SPECIFIC_WEEKDAYS,
            weekdays = "MO,WE,FR",
            reminderTime = "09:30"
        )

        val habit = repository.observeActiveHabits().first().single()
        assertEquals("MO,WE,FR", habit.weekdays)
        assertEquals("09:30", habit.reminderTime)
    }
}
