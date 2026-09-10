package com.plannermvp.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    /** Used by backup/export (Phase 11): a full one-shot snapshot, not a live Flow. */
    @Query("SELECT * FROM projects")
    suspend fun getAllOnce(): List<ProjectEntity>

    /** Used by backup/restore (Phase 11): wipes the table before re-inserting from a backup file. */
    @Query("DELETE FROM projects")
    suspend fun clearAll()
}
