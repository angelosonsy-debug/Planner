package com.plannermvp.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Phase 1 acceptance test: proves Room is wired correctly end-to-end
 * (entity -> DAO -> in-memory database) before any real feature is built
 * on top of it. Runs on the JVM via Robolectric, no emulator needed.
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `settings row is absent until first write`() = runTest {
        assertNull(db.settingsDao().get())
    }

    @Test
    fun `upsert then get returns the same settings`() = runTest {
        val dao = db.settingsDao()
        dao.upsert(SettingsEntity(themeMode = "dark", onboardingCompleted = true))

        val stored = dao.get()
        assertEquals("dark", stored?.themeMode)
        assertEquals(true, stored?.onboardingCompleted)
    }

    @Test
    fun `upsert overwrites the single settings row instead of inserting a second one`() = runTest {
        val dao = db.settingsDao()
        dao.upsert(SettingsEntity(themeMode = "light"))
        dao.upsert(SettingsEntity(themeMode = "dark"))

        assertEquals("dark", dao.get()?.themeMode)
    }
}
