package com.plannermvp.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ProjectStatus { ACTIVE, COMPLETED, ARCHIVED }

/** Project model per Section 9 of the product spec. */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    /** ISO-8601 date, nullable. */
    val deadline: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
