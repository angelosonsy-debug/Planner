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
import com.plannermvp.app.data.local.TaskPriority
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Release 1.0 update:
 * Insert task directly via repository to avoid DatePicker interaction.
 * Task with no importance/urgency lands in Q4 by default.
 */
class MatrixScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        ApplicationProvider.getApplicationContext<PlannerApp>().database.clearAllTables()
    }

    @Test
    fun movingATaskFromQ4ToQ1ActuallyReclassifiesIt() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        val taskTitle = "Untagged task"

        // Insert directly — avoids DatePicker dialog
        runBlocking {
            app.taskRepository.createTask(
                title           = taskTitle,
                date            = null,
                priority        = TaskPriority.MEDIUM,
                projectId       = null,
                startTime       = null,
                durationMinutes = null,
                recurringRule   = null
            )
        }

        // Open Matrix via More
        val moreTab    = composeRule.activity.getString(R.string.nav_more)
        val matrixEntry = composeRule.activity.getString(R.string.more_matrix)
        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(matrixEntry).performClick()
        composeRule.waitForIdle()

        // Task starts in Q4 (not important, not urgent)
        composeRule.onNode(
            hasTestTag("quadrantPane_Q4") and hasAnyDescendant(hasText(taskTitle))
        ).assertExists()

        // Move to Q1 via the chip menu
        composeRule.onNodeWithText(taskTitle).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasTestTag("matrixMoveTo_Q1")).performClick()
        composeRule.waitForIdle()

        // Now in Q1, gone from Q4
        composeRule.onNode(
            hasTestTag("quadrantPane_Q1") and hasAnyDescendant(hasText(taskTitle))
        ).assertExists()
        composeRule.onNode(
            hasTestTag("quadrantPane_Q4") and hasAnyDescendant(hasText(taskTitle))
        ).assertDoesNotExist()
    }
}
