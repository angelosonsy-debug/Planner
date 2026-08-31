package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class TaskPriority { HIGH, MEDIUM, LOW }

enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED }

/**
 * Task model per Section 8 of the product spec. Only title/date/priority
 * are required to create one (Section 2: "reduce cognitive load") —
 * everything else defaults or stays null.
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val projectId: String? = null,
    /** ISO-8601 date, e.g. "2026-08-12". Null = no date / Inbox. */
    val date: String? = null,
    val startTime: String? = null,
    val durationMinutes: Int? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val importance: Boolean = false,
    val urgency: Boolean = false,
    val status: TaskStatus = TaskStatus.PENDING,
    val recurringRule: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
