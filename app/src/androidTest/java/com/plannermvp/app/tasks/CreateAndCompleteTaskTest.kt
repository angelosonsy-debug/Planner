package com.plannermvp.app.tasks

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
 * Phase 2 acceptance test: create a task with just a title (Section 2 /
 * Section 50 acceptance criteria), then mark it complete from the list.
 * The database is cleared before each test so results never depend on
 * what an earlier test in the same instrumentation run left behind.
 */
class CreateAndCompleteTaskTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    private fun createTask(title: String) {
        val tasksTab = composeRule.activity.getString(R.string.nav_tasks)
        val addTask = composeRule.activity.getString(R.string.add_task)
        val titleLabel = composeRule.activity.getString(R.string.task_title_label)
        val save = composeRule.activity.getString(R.string.action_save)

        composeRule.onNodeWithText(tasksTab).performClick()
        composeRule.onNodeWithContentDescription(addTask).performClick()
        composeRule.onNodeWithText(titleLabel).performTextInput(title)
        composeRule.onNodeWithText(save).performClick()
    }

    @Test
    fun creatingATaskWithOnlyATitleShowsItInTheList() {
        createTask("Learn useEffect")
        composeRule.onNodeWithText("Learn useEffect").assertExists()
    }

    @Test
    fun tappingTheCheckboxTogglesTaskCompletion() {
        createTask("Practice useEffect")

        composeRule.onNodeWithText("Practice useEffect").assertExists()
        val checkbox = composeRule.onNode(isToggleable())
        checkbox.assertIsOff()
        checkbox.performClick()
        checkbox.assertIsOn()
    }
}
