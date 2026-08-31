package com.plannermvp.app.today

import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import java.time.LocalDate

/**
 * Phase 3 acceptance test: a task dated today shows up on the Today tab
 * (Top 3 + Today's Tasks + Daily Progress), and completing it from there
 * updates the progress card (Section 10 / Section 50 acceptance criteria).
 */
class TodayScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    private fun createTaskForToday(title: String) {
        val tasksTab = composeRule.activity.getString(R.string.nav_tasks)
        val addTask = composeRule.activity.getString(R.string.add_task)
        val titleLabel = composeRule.activity.getString(R.string.task_title_label)
        val dateLabel = composeRule.activity.getString(R.string.task_date_label)
        val save = composeRule.activity.getString(R.string.action_save)

        composeRule.onNodeWithText(tasksTab).performClick()
        composeRule.onNodeWithContentDescription(addTask).performClick()
        composeRule.onNodeWithText(titleLabel).performTextInput(title)
        composeRule.onNodeWithText(dateLabel).performTextInput(LocalDate.now().toString())
        composeRule.onNodeWithText(save).performClick()
    }

    @Test
    fun taskDatedTodayAppearsOnTodayTabWithProgress() {
        createTaskForToday("Speaking practice")

        val todayTab = composeRule.activity.getString(R.string.nav_today)
        composeRule.onNodeWithText(todayTab).performClick()

        // Appears twice by design: it's the only task today, so it's both
        // the Top 3 highlight and the one entry in the full task list.
        composeRule.onAllNodesWithText("Speaking practice")[0].assertExists()
        composeRule.onNodeWithText("0 / 1").assertExists()
        composeRule.onNodeWithText("0%").assertExists()
    }

    @Test
    fun completingATaskFromTodayUpdatesDailyProgress() {
        createTaskForToday("Vocabulary")

        val todayTab = composeRule.activity.getString(R.string.nav_today)
        composeRule.onNodeWithText(todayTab).performClick()

        composeRule.onAllNodes(isToggleable())[0].performClick()

        composeRule.onNodeWithText("1 / 1").assertExists()
        composeRule.onNodeWithText("100%").assertExists()
    }
}
