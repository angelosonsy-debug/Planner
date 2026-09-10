package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.ProjectEntity
import com.plannermvp.app.data.local.SettingsEntity
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.domain.backup.BackupData
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: BackupRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BackupRepository(db)
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `exporting an empty database yields empty lists, not a crash`() = runTest {
        val data = repository.exportData()
        assertTrue(data.projects.isEmpty())
        assertTrue(data.tasks.isEmpty())
        assertTrue(data.habits.isEmpty())
        assertTrue(data.habitCheckIns.isEmpty())
    }

    @Test
    fun `export then restore round-trips every table`() = runTest {
        db.projectDao().insert(ProjectEntity(id = "p1", name = "React"))
        db.taskDao().insert(TaskEntity(id = "t1", title = "Learn hooks", projectId = "p1"))
        db.habitDao().insert(HabitEntity(id = "h1", name = "Reading"))
        db.habitCheckInDao().upsert(HabitCheckInEntity(id = "c1", habitId = "h1", date = "2026-08-12"))
        db.settingsDao().upsert(SettingsEntity(themeMode = "dark"))

        val exported = repository.exportData()

        // Wipe everything to prove restore actually repopulates it, not just leaves it alone.
        db.habitCheckInDao().clearAll()
        db.habitDao().clearAll()
        db.taskDao().clearAll()
        db.projectDao().clearAll()

        repository.restoreData(exported)

        assertEquals(1, db.projectDao().getAllOnce().size)
        assertEquals(1, db.taskDao().getAllOnce().size)
        assertEquals(1, db.habitDao().getAllOnce().size)
        assertEquals(1, db.habitCheckInDao().getAllOnce().size)
        assertEquals("dark", db.settingsDao().get()?.themeMode)
    }

    @Test
    fun `restoring drops a check-in whose habit is not included in the backup`() = runTest {
        val data = emptyBackup().copy(
            habitCheckIns = listOf(HabitCheckInEntity(habitId = "missing-habit", date = "2026-08-12"))
        )

        repository.restoreData(data)

        assertEquals(0, db.habitCheckInDao().getAllOnce().size)
    }

    @Test
    fun `restoring a task whose project is not included clears the project link instead of failing`() = runTest {
        val data = emptyBackup().copy(
            tasks = listOf(TaskEntity(id = "t1", title = "Orphan", projectId = "missing-project"))
        )

        repository.restoreData(data)

        val restored = db.taskDao().getAllOnce().single()
        assertEquals("Orphan", restored.title)
        assertNull(restored.projectId)
    }

    @Test
    fun `restoring replaces existing data rather than merging with it`() = runTest {
        db.taskDao().insert(TaskEntity(id = "old", title = "Old task"))

        repository.restoreData(emptyBackup().copy(tasks = listOf(TaskEntity(id = "new", title = "New task"))))

        val remaining = db.taskDao().getAllOnce()
        assertEquals(1, remaining.size)
        assertEquals("New task", remaining.single().title)
    }

    private fun emptyBackup(): BackupData = BackupData(
        settings = SettingsEntity(),
        projects = emptyList(),
        tasks = emptyList(),
        habits = emptyList(),
        habitCheckIns = emptyList()
    )
}
