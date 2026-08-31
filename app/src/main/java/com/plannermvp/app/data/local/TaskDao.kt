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

    /** Section 11: the Matrix only cares about tasks still worth doing something about. */
    @Query("SELECT * FROM tasks WHERE status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun observeActionable(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY date IS NULL, date ASC")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    /** Used by import to skip re-adding a task that already exists (Section 6: prevent duplicate import). */
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

    /** Used by backup/export (Phase 11): a full one-shot snapshot, not a live Flow. */
    @Query("SELECT * FROM tasks")
    suspend fun getAllOnce(): List<TaskEntity>

    /** Used by backup/restore (Phase 11): wipes the table before re-inserting from a backup file. */
    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}
