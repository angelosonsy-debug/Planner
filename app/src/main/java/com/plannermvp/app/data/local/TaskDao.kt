package com.plannermvp.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY date IS NULL, date ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun observeActionable(): Flow<List<TaskEntity>>

    /**
     * Release 1.0 fix: date stored as "YYYY-MM-DD" string.
     * Caller must pass LocalDate.now(ZoneId.systemDefault()).toString()
     * to avoid off-by-one day errors from UTC vs local time.
     */
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<TaskEntity>>

    /** Used by CalendarViewModel to find which days in a month have tasks. */
    @Query("SELECT * FROM tasks WHERE date LIKE :monthPrefix || '-%' ORDER BY date ASC")
    fun observeByMonthPrefix(monthPrefix: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY date IS NULL, date ASC")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks WHERE title = :title AND date IS :date AND projectId IS :projectId")
    suspend fun countMatching(title: String, date: String?, projectId: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAllOnce(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    /**
     * Release 1.0: when a project is deleted, keep tasks but remove their
     * project association (set projectId to NULL). Tasks are NOT deleted.
     */
    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId")
    suspend fun clearProjectAssociation(projectId: String)
}
