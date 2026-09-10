package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.domain.importing.ImportIssue
import com.plannermvp.app.domain.importing.ImportIssueLevel
import com.plannermvp.app.domain.importing.ImportedItem
import com.plannermvp.app.domain.importing.ValidatedImportItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImportRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var importRepository: ImportRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var projectRepository: ProjectRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskRepository = TaskRepository(db.taskDao())
        projectRepository = ProjectRepository(db.projectDao())
        importRepository = ImportRepository(taskRepository, projectRepository)
    }

    @After
    fun tearDown() { db.close() }

    private fun validated(
        project: String? = null,
        task: String,
        date: String? = null,
        priority: TaskPriority = TaskPriority.MEDIUM,
        hasError: Boolean = false
    ) = ValidatedImportItem(
        raw = ImportedItem(project = project, task = task, date = date),
        resolvedPriority = priority,
        resolvedDate = date,
        issues = if (hasError) listOf(ImportIssue(ImportIssueLevel.ERROR, "bad")) else emptyList()
    )

    @Test
    fun `confirming an import creates the project and the task`() = runTest {
        val result = importRepository.confirmImport(
            listOf(validated(project = "React", task = "Learn useEffect", date = "2026-08-12"))
        )

        assertEquals(1, result.imported)
        assertEquals(1, projectRepository.observeAll().first().size)
        val task = taskRepository.observeAll().first().single()
        assertEquals("Learn useEffect", task.title)
        assertEquals(projectRepository.observeAll().first().single().id, task.projectId)
    }

    @Test
    fun `two items for the same new project reuse one project instead of creating two`() = runTest {
        importRepository.confirmImport(
            listOf(
                validated(project = "React", task = "Learn useEffect"),
                validated(project = "React", task = "Practice useEffect")
            )
        )

        assertEquals(1, projectRepository.observeAll().first().size)
        assertEquals(2, taskRepository.observeAll().first().size)
    }

    @Test
    fun `importing the same item twice only creates one task`() = runTest {
        val item = validated(project = "React", task = "Learn useEffect", date = "2026-08-12")

        val first = importRepository.confirmImport(listOf(item))
        val second = importRepository.confirmImport(listOf(item))

        assertEquals(1, first.imported)
        assertEquals(0, second.imported)
        assertEquals(1, second.skippedDuplicates)
        assertEquals(1, taskRepository.observeAll().first().size)
    }

    @Test
    fun `items with an error are skipped and never reach the database`() = runTest {
        val result = importRepository.confirmImport(listOf(validated(task = "Bad", hasError = true)))

        assertEquals(0, result.imported)
        assertEquals(1, result.skippedErrors)
        assertEquals(0, taskRepository.observeAll().first().size)
    }

    @Test
    fun `a task with no project is created without one`() = runTest {
        importRepository.confirmImport(listOf(validated(task = "Standalone task")))

        val task = taskRepository.observeAll().first().single()
        assertEquals(null, task.projectId)
        assertEquals(0, projectRepository.observeAll().first().size)
    }
}
