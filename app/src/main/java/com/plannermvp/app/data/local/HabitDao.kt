package com.plannermvp.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    /**
     * Used by backup/export (Phase 11): ALL habits, including archived ones —
     * observeActive() deliberately excludes those, but a backup shouldn't
     * silently drop someone's history just because a habit was archived.
     */
    @Query("SELECT * FROM habits")
    suspend fun getAllOnce(): List<HabitEntity>

    /** Used by backup/restore (Phase 11): wipes the table before re-inserting from a backup file. */
    @Query("DELETE FROM habits")
    suspend fun clearAll()
}
