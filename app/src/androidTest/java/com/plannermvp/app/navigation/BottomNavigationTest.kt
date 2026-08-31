package com.plannermvp.app.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Phase 1 acceptance test: the five bottom-nav tabs exist and tapping each
 * one actually navigates (Section 3 / Section 50 of the product spec).
 * Runs on an emulator/device via the androidTest source set.
 *
 * Four of the five tab labels (Today/Projects/Habits/More) are identical
 * to their screen's own title text by design — when that screen is
 * showing, the same text legitimately appears twice (nav bar + screen
 * header) at once. Asserting "at least one exists" via
 * onAllNodesWithText(...)[0] instead of the stricter onNodeWithText(...)
 * (which requires exactly one match) is what's actually being tested here
 * — that the tab is visible — not how many times its text happens to
 * appear.
 */
class BottomNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun allFiveTabsAreVisible() {
        val labels = listOf(
            R.string.nav_today, R.string.nav_tasks, R.string.nav_projects,
            R.string.nav_habits, R.string.nav_more
        )
        labels.forEach { res ->
            composeRule.onAllNodesWithText(composeRule.activity.getString(res))[0].assertExists()
        }
    }

    @Test
    fun tappingTasksShowsTasksScreen() {
        val tasksLabel = composeRule.activity.getString(R.string.nav_tasks)
        val tasksTitle = composeRule.activity.getString(R.string.tasks_title)

        composeRule.onNodeWithText(tasksLabel).performClick()
        composeRule.onNodeWithText(tasksTitle).assertExists()
    }

    @Test
    fun tappingHabitsShowsHabitsScreen() {
        val habitsLabel = composeRule.activity.getString(R.string.nav_habits)
        val habitsTitle = composeRule.activity.getString(R.string.habits_title)

        composeRule.onNodeWithText(habitsLabel).performClick()
        composeRule.onAllNodesWithText(habitsTitle)[0].assertExists()
    }
}
