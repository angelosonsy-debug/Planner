package com.plannermvp.app.matrix

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
 * Phase 5 acceptance test: a plain task (no date, not flagged) lands in
 * Q4 by default; moving it to Q1 through the chip's menu actually changes
 * its quadrant (Section 12: real data change, not just a visual one).
 */
class MatrixScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun movingATaskFromQ4ToQ1ActuallyReclassifiesIt() {
        val taskTitle = "Untagged task"

        // Create a plain task via the Tasks tab (no date, default priority).
        val tasksTab = composeRule.activity.getString(R.string.nav_tasks)
        val addTask = composeRule.activity.getString(R.string.add_task)
        val titleLabel = composeRule.activity.getString(R.string.task_title_label)
        val save = composeRule.activity.getString(R.string.action_save)
        composeRule.onNodeWithText(tasksTab).performClick()
        composeRule.onNodeWithContentDescription(addTask).performClick()
        composeRule.onNodeWithText(titleLabel).performTextInput(taskTitle)
        composeRule.onNodeWithText(save).performClick()

        // Go to the Matrix via More.
        val moreTab = composeRule.activity.getString(R.string.nav_more)
        val matrixEntry = composeRule.activity.getString(R.string.more_matrix)
        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(matrixEntry).performClick()

        // It should start in Q4 (not important, not urgent, no date).
        composeRule.onNode(hasTestTag("quadrantPane_Q4") and hasAnyDescendant(hasText(taskTitle))).assertExists()

        // Move it to Q1 via the chip's menu.
        composeRule.onNodeWithText(taskTitle).performClick()
        composeRule.onNode(hasTestTag("matrixMoveTo_Q1")).performClick()

        // Now it should be in Q1, and gone from Q4.
        composeRule.onNode(hasTestTag("quadrantPane_Q1") and hasAnyDescendant(hasText(taskTitle))).assertExists()
        composeRule.onNode(hasTestTag("quadrantPane_Q4") and hasAnyDescendant(hasText(taskTitle))).assertDoesNotExist()
    }
}
