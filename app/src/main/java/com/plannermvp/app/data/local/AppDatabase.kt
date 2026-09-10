package com.plannermvp.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Release 1.0: version bumped to 6, fallbackToDestructiveMigration() removed.
 * All migrations are explicit — user data is protected on upgrade.
 *
 * v5 → v6: adds completionSoundEnabled to settings table.
 *
 * Previous versions (v1–v5) used fallbackToDestructiveMigration during MVP
 * development when there were no real installs to protect. Now that the app
 * is in release, every schema change requires an explicit migration.
 */
@Database(
    entities = [
        SettingsEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class
    ],
    version = 6,
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

        // v5 → v6: completionSoundEnabled added to settings (default 1 = ON)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN completionSoundEnabled INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // Release 1.0: NO fallbackToDestructiveMigration — user data is protected
                    .addMigrations(MIGRATION_5_6)
                    .build()
                    .also { instance = it }
            }
    }
}
