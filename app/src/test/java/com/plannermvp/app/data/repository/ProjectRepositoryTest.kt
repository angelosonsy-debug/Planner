package com.plannermvp.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProjectRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProjectRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProjectRepository(db.projectDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `creating a project only needs a name`() = runTest {
        repository.createProject(name = "React")

        val projects = repository.observeAll().first()
        assertEquals(1, projects.size)
        assertEquals("React", projects.first().name)
    }

    @Test
    fun `deleting a project removes it`() = runTest {
        repository.createProject(name = "Temp")
        val project = repository.observeAll().first().first()

        repository.deleteProject(project)

        assertEquals(0, repository.observeAll().first().size)
    }
}
