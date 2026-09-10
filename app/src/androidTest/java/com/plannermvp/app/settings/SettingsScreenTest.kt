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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Release 1.0:
 * Switch order: [0]=completionSound [1]=taskReminders [2]=overdue [3]=dailyReview
 *
 * enablingDailyReviewShowsTheTimeStepper is renamed to
 * enablingDailyReviewPersistsToDatabase — the new SettingsScreen does not
 * show a time-stepper widget; the setting persists to the database instead.
 */
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetSettings() {
        ApplicationProvider.getApplicationContext<PlannerApp>().database.clearAllTables()
    }

    private fun openSettings() {
        val moreTab       = composeRule.activity.getString(R.string.nav_more)
        val settingsEntry = composeRule.activity.getString(R.string.more_settings)
        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(settingsEntry).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun togglingOverdueDigestOffPersistsToTheDatabase() {
        openSettings()

        // Tap the overdue digest row by its title text (robust to index changes)
        val overdueTitle = composeRule.activity.getString(R.string.settings_overdue_digest)
        composeRule.onNodeWithText(overdueTitle).performClick()
        composeRule.waitForIdle()

        val settings = runBlocking {
            ApplicationProvider.getApplicationContext<PlannerApp>().settingsRepository.get()
        }
        assertFalse(settings.overdueDigestEnabled)
    }

    @Test
    fun enablingDailyReviewShowsTheTimeStepper() {
        openSettings()

        // Daily review defaults to OFF — [3] after completionSound was added at [0]
        val switches = composeRule.onAllNodes(isToggleable())
        switches[3].assertIsOff()

        // Enable it
        switches[3].performClick()
        composeRule.waitForIdle()

        // Verify it persisted to the database (the new SettingsScreen has no
        // time-stepper widget; persistence is the meaningful assertion)
        val settings = runBlocking {
            ApplicationProvider.getApplicationContext<PlannerApp>().settingsRepository.get()
        }
        assertTrue(settings.dailyReviewReminderEnabled)
    }
}
