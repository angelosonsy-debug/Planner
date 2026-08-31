package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SettingsRepository(db.settingsDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `before anything is saved, defaults are task reminders and overdue digest on, daily review off`() = runTest {
        val settings = repository.get()
        assertTrue(settings.taskRemindersEnabled)
        assertTrue(settings.overdueDigestEnabled)
        assertEquals(false, settings.dailyReviewReminderEnabled)
    }

    @Test
    fun `disabling task reminders persists and does not touch the other toggles`() = runTest {
        repository.setTaskRemindersEnabled(false)

        val settings = repository.get()
        assertEquals(false, settings.taskRemindersEnabled)
        assertTrue(settings.overdueDigestEnabled)
    }

    @Test
    fun `enabling daily review reminder stores both the flag and the time`() = runTest {
        repository.setDailyReviewReminder(enabled = true, time = "21:15")

        val settings = repository.get()
        assertTrue(settings.dailyReviewReminderEnabled)
        assertEquals("21:15", settings.dailyReviewReminderTime)
    }

    @Test
    fun `observe reflects an update made through get-then-upsert`() = runTest {
        repository.setOverdueDigestEnabled(false)
        assertEquals(false, repository.observe().first().overdueDigestEnabled)
    }
}
