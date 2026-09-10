package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * One row per habit per day it was checked in (Section 16/44). The unique
 * index on (habitId, date) is what makes a check-in an *upsert*: logging
 * again for the same day replaces the row instead of adding a second one.
 * Streaks/statistics are always computed from these rows, never stored as
 * a standalone fact (Section 44), so there's nothing here to get out of
 * sync.
 */
@Entity(
    tableName = "habit_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index(value = ["habitId", "date"], unique = true)]
)
data class HabitCheckInEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    /** ISO-8601 date. */
    val date: String,
    /** Binary habits: 1 = done. Quantity habits: the amount logged that day. */
    val value: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
