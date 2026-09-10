package com.plannermvp.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCheckInDao {

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId ORDER BY date ASC")
    fun observeForHabit(habitId: String): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getForDate(habitId: String, date: String): HabitCheckInEntity?

    /** Relies on the unique (habitId, date) index: same-day check-ins replace, they don't stack. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun deleteForDate(habitId: String, date: String)

    /** Used by backup/export (Phase 11): every check-in across every habit, not scoped to one. */
    @Query("SELECT * FROM habit_check_ins")
    suspend fun getAllOnce(): List<HabitCheckInEntity>

    /** Used by backup/restore (Phase 11): wipes the table before re-inserting from a backup file. */
    @Query("DELETE FROM habit_check_ins")
    suspend fun clearAll()
}
