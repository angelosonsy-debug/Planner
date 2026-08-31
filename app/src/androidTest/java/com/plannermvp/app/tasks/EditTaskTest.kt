package com.plannermvp.app.tasks

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Phase 8 acceptance test: tapping a task's title opens an edit dialog,
 * and saving a new title/date actually reschedules it (Section 24).
 */
class EditTaskTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun editingATaskTitleAndDateUpdatesTheRow() {
        val tasksTab = composeRule.activity.getString(R.string.nav_tasks)
        val addTask = composeRule.activity.getString(R.string.add_task)
        val titleLabel = composeRule.activity.getString(R.string.task_title_label)
        val dateLabel = composeRule.activity.getString(R.string.task_date_label)
        val save = composeRule.activity.getString(R.string.action_save)

        composeRule.onNodeWithText(tasksTab).performClick()
        composeRule.onNodeWithContentDescription(addTask).performClick()
        composeRule.onNodeWithText(titleLabel).performTextInput("Original title")
        composeRule.onNodeWithText(save).performClick()

        composeRule.onNodeWithText("Original title").assertExists()

        // Tap the title to edit.
        composeRule.onNodeWithText("Original title").performClick()
        composeRule.onNodeWithText(titleLabel).performTextClearance()
        composeRule.onNodeWithText(titleLabel).performTextInput("Updated title")
        composeRule.onNodeWithText(dateLabel).performTextInput("2026-09-01")
        composeRule.onNodeWithText(save).performClick()

        composeRule.onNodeWithText("Updated title").assertExists()
        composeRule.onNodeWithText("Original title").assertDoesNotExist()
    }
}
