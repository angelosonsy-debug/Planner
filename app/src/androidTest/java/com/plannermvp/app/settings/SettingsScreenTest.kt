package com.plannermvp.app.settings

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Phase 9 acceptance test: the notification toggles in Settings actually
 * flip and persist to the database (the underlying WorkManager scheduling
 * itself isn't independently verified here — see README's Known gaps).
 * Switches are found by list position: [0] task reminders, [1] overdue
 * digest, [2] daily review, matching the order they're laid out in
 * SettingsScreen.
 */
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetSettings() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    private fun openSettings() {
        val moreTab = composeRule.activity.getString(R.string.nav_more)
        val settingsEntry = composeRule.activity.getString(R.string.more_settings)
        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(settingsEntry).performClick()
    }

    @Test
    fun togglingOverdueDigestOffPersistsToTheDatabase() {
        openSettings()

        val switches = composeRule.onAllNodes(isToggleable())
        switches[1].assertIsOn() // overdue digest defaults to enabled
        switches[1].performClick()
        switches[1].assertIsOff()

        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        val settings = runBlocking { app.settingsRepository.get() }
        assertFalse(settings.overdueDigestEnabled)
    }

    @Test
    fun enablingDailyReviewShowsTheTimeStepper() {
        openSettings()

        val timeLabel = composeRule.activity.getString(R.string.settings_review_time_label)
        val switches = composeRule.onAllNodes(isToggleable())
        switches[2].assertIsOff() // daily review defaults to disabled

        switches[2].performClick()

        composeRule.onNodeWithText(timeLabel).assertExists()
    }
}
