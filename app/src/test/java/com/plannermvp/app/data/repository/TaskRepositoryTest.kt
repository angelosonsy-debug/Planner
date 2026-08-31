package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.data.local.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TaskRepository(db.taskDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `creating a task with only title, date, priority is enough`() = runTest {
        repository.createTask(title = "Learn useEffect", date = "2026-08-12", priority = TaskPriority.HIGH)

        val tasks = repository.observeAll().first()
        assertEquals(1, tasks.size)
        assertEquals("Learn useEffect", tasks.first().title)
        assertEquals("2026-08-12", tasks.first().date)
        assertEquals(TaskPriority.HIGH, tasks.first().priority)
        assertEquals(TaskStatus.PENDING, tasks.first().status)
    }

    @Test
    fun `toggling complete sets status and completedAt, toggling again clears it`() = runTest {
        repository.createTask(title = "Read")
        val task = repository.observeAll().first().first()

        repository.toggleComplete(task)
        val completed = repository.observeAll().first().first()
        assertEquals(TaskStatus.COMPLETED, completed.status)
        assertNotNull(completed.completedAt)

        repository.toggleComplete(completed)
        val reopened = repository.observeAll().first().first()
        assertEquals(TaskStatus.PENDING, reopened.status)
        assertNull(reopened.completedAt)
    }

    @Test
    fun `postponing a dated task moves it exactly one day forward`() = runTest {
        repository.createTask(title = "Speaking", date = "2026-08-12")
        val task = repository.observeAll().first().first()

        repository.postponeToTomorrow(task)

        val postponed = repository.observeAll().first().first()
        assertEquals("2026-08-13", postponed.date)
        assertEquals(TaskStatus.PENDING, postponed.status)
    }

    @Test
    fun `deleting a task removes it`() = runTest {
        repository.createTask(title = "Temp")
        val task = repository.observeAll().first().first()

        repository.deleteTask(task)

        assertEquals(0, repository.observeAll().first().size)
    }

    @Test
    fun `setQuadrant Q1 writes importance true and urgency true`() = runTest {
        repository.createTask(title = "Focus")
        val task = repository.observeAll().first().first()

        repository.setQuadrant(task, com.plannermvp.app.domain.matrix.MatrixQuadrant.Q1)

        val updated = repository.observeAll().first().first()
        assertEquals(true, updated.importance)
        assertEquals(true, updated.urgency)
    }

    @Test
    fun `setQuadrant Q4 writes importance false and urgency false`() = runTest {
        repository.createTask(title = "Someday")
        val task = repository.observeAll().first().first()

        repository.setQuadrant(task, com.plannermvp.app.domain.matrix.MatrixQuadrant.Q4)

        val updated = repository.observeAll().first().first()
        assertEquals(false, updated.importance)
        assertEquals(false, updated.urgency)
    }

    @Test
    fun `completing a daily recurring task creates tomorrow's occurrence`() = runTest {
        repository.createTask(title = "Speaking", date = "2026-08-12")
        val created = repository.observeAll().first().first()
        repository.updateTaskDetails(
            created, title = created.title, date = created.date, startTime = null,
            durationMinutes = null, priority = created.priority, recurringRule = "daily"
        )
        val withRule = repository.observeAll().first().first()

        val result = repository.toggleComplete(withRule)

        assertEquals(TaskStatus.COMPLETED, result.updated.status)
        assertEquals("2026-08-13", result.followUp?.date)
        assertEquals("daily", result.followUp?.recurringRule)

        val all = repository.observeAll().first()
        assertEquals(2, all.size)
        val next = all.first { it.date == "2026-08-13" }
        assertEquals(TaskStatus.PENDING, next.status)
        assertEquals("daily", next.recurringRule)
    }

    @Test
    fun `completing a non-recurring task does not create a follow-up`() = runTest {
        repository.createTask(title = "One-off", date = "2026-08-12")
        val task = repository.observeAll().first().first()

        val result = repository.toggleComplete(task)

        assertEquals(null, result.followUp)
        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun `rescheduleTo moves a task to an arbitrary date directly`() = runTest {
        repository.createTask(title = "Task", date = "2026-08-12")
        val task = repository.observeAll().first().first()

        repository.rescheduleTo(task, "2026-09-01")

        assertEquals("2026-09-01", repository.observeAll().first().first().date)
    }

    @Test
    fun `updateTaskDetails changes title, date, time, duration and priority together`() = runTest {
        repository.createTask(title = "Old title")
        val task = repository.observeAll().first().first()

        repository.updateTaskDetails(
            task,
            title = "New title",
            date = "2026-08-20",
            startTime = "14:30",
            durationMinutes = 45,
            priority = TaskPriority.HIGH,
            recurringRule = "weekly"
        )

        val updated = repository.observeAll().first().first()
        assertEquals("New title", updated.title)
        assertEquals("2026-08-20", updated.date)
        assertEquals("14:30", updated.startTime)
        assertEquals(45, updated.durationMinutes)
        assertEquals(TaskPriority.HIGH, updated.priority)
        assertEquals("weekly", updated.recurringRule)
    }
}
