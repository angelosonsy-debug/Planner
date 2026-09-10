package com.plannermvp.app.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Release 1.0 update:
 * - Date field is a DatePicker button → skip date input (default = today)
 * - Checkbox is the first toggleable node in the row
 * - After completing, switch to COMPLETED filter to verify
 */
class CreateAndCompleteTaskTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        ApplicationProvider.getApplicationContext<PlannerApp>().database.clearAllTables()
    }

    @Test
    fun tappingTheCheckboxTogglesTaskCompletion() {
        val tasksTab   = composeRule.activity.getString(R.string.nav_tasks)
        val addTask    = composeRule.activity.getString(R.string.add_task)
        val titleLabel = composeRule.activity.getString(R.string.task_title_label)
        val save       = composeRule.activity.getString(R.string.action_save)

        // Create task (date defaults to today via the DatePicker button default)
        composeRule.onNodeWithText(tasksTab).performClick()
        composeRule.onNodeWithContentDescription(addTask).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleLabel).performTextInput("Buy groceries")
        composeRule.onNodeWithText(save).performClick()
        composeRule.waitForIdle()

        // Task appears in active list
        composeRule.onNodeWithText("Buy groceries").assertIsDisplayed()

        // Tap the checkbox (first toggleable in the list)
        composeRule.onNode(isToggleable()).performClick()
        composeRule.waitForIdle()

        // Switch to COMPLETED filter
        composeRule.onNodeWithText("المكتملة").performClick()
        composeRule.waitForIdle()

        // Task should appear in completed view
        composeRule.onNodeWithText("Buy groceries").assertIsDisplayed()
    }
}
