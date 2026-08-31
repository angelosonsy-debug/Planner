package com.plannermvp.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Phase 9 bumps to v5 for the three new notification-preference columns on
 * SettingsEntity. Still pre-release, so a destructive migration remains
 * fine — real Migration objects start once there's an actual install to
 * protect (Section 42: don't over-abstract before you need to).
 */
@Database(
    entities = [
        SettingsEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckInDao(): HabitCheckInDao

    companion object {
        private const val DB_NAME = "planner-mvp.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
