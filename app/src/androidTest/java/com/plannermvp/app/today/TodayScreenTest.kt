package com.plannermvp.app.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskPriority
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Release 1.0 update:
 * - Tasks inserted directly via repository (avoids DatePicker dialog)
 * - Checkbox found via isToggleable() (no testTag needed)
 */
class TodayScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        ApplicationProvider.getApplicationContext<PlannerApp>().database.clearAllTables()
    }

    private fun insertTodayTask(title: String) {
        val app      = ApplicationProvider.getApplicationContext<PlannerApp>()
        val todayStr = LocalDate.now(ZoneId.systemDefault()).toString()
        runBlocking {
            app.taskRepository.createTask(
                title           = title,
                date            = todayStr,
                priority        = TaskPriority.MEDIUM,
                projectId       = null,
                startTime       = null,
                durationMinutes = null,
                recurringRule   = null
            )
        }
    }

    @Test
    fun taskDatedTodayAppearsOnTodayTabWithProgress() {
        insertTodayTask("Today task")

        val todayTab = composeRule.activity.getString(R.string.nav_today)
        composeRule.onNodeWithText(todayTab).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Today task").assertIsDisplayed()
        composeRule.onNodeWithText("0 / 1").assertIsDisplayed()
    }

    @Test
    fun completingATaskFromTodayUpdatesDailyProgress() {
        insertTodayTask("Task to complete")

        val todayTab = composeRule.activity.getString(R.string.nav_today)
        composeRule.onNodeWithText(todayTab).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Task to complete").assertIsDisplayed()

        // Tap the checkbox (first toggleable node)
        composeRule.onNode(isToggleable()).performClick()
        composeRule.waitForIdle()

        // Progress should update — task is now in completed section
        // Expand completed section
        composeRule.onNodeWithText("المهام المكتملة (1)").assertIsDisplayed()
        composeRule.onNodeWithText("1 / 1").assertIsDisplayed()
    }
}
