package com.plannermvp.app.data.repository

import com.plannermvp.app.data.local.SettingsDao
import com.plannermvp.app.data.local.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

    /** Never null to callers — falls back to defaults until the row is first written. */
    fun observe(): Flow<SettingsEntity> = settingsDao.observe().map { it ?: SettingsEntity() }

    suspend fun get(): SettingsEntity = settingsDao.get() ?: SettingsEntity()

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        settingsDao.upsert(get().copy(taskRemindersEnabled = enabled))
    }

    suspend fun setOverdueDigestEnabled(enabled: Boolean) {
        settingsDao.upsert(get().copy(overdueDigestEnabled = enabled))
    }

    suspend fun setDailyReviewReminder(enabled: Boolean, time: String) {
        settingsDao.upsert(get().copy(dailyReviewReminderEnabled = enabled, dailyReviewReminderTime = time))
    }
}
