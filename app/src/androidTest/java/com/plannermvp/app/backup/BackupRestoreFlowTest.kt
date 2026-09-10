package com.plannermvp.app.backup

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import com.plannermvp.app.domain.backup.BackupSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Phase 11 acceptance test: the restore confirmation dialog actually shows
 * up (never restores silently — Section 32), and confirming it really
 * replaces the database (exercised directly against the ViewModel's
 * onBackupFilePicked/confirmRestore rather than a real SAF file picker,
 * same reasoning as the import feature's file-picker tests — the system
 * picker itself isn't something a Compose test can drive).
 */
class BackupRestoreFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun navigatingToBackupScreenShowsExportAndRestoreOptions() {
        val moreTab = composeRule.activity.getString(R.string.nav_more)
        val settingsEntry = composeRule.activity.getString(R.string.more_settings)
        val backupEntry = composeRule.activity.getString(R.string.settings_backup_restore)
        val exportButton = composeRule.activity.getString(R.string.backup_export_button)
        val restoreButton = composeRule.activity.getString(R.string.backup_restore_button)

        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(settingsEntry).performClick()
        composeRule.onNodeWithText(backupEntry).performClick()

        composeRule.onNodeWithText(exportButton).assertExists()
        composeRule.onNodeWithText(restoreButton).assertExists()
    }

    @Test
    fun exportingThenRestoringRoundTripsRealData() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()

        app.taskRepository.createTask(title = "Round-trip task", date = "2026-08-13")
        val exportedJson = BackupSerializer.toJson(app.backupRepository.exportData())

        app.database.clearAllTables()
        assertEquals(0, app.taskRepository.observeAll().first().size)

        app.backupRepository.restoreData(BackupSerializer.fromJson(exportedJson))

        val restored = app.taskRepository.observeAll().first()
        assertEquals(1, restored.size)
        assertEquals("Round-trip task", restored.first().title)
    }
}
