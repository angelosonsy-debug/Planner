package com.plannermvp.app.habits

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
 * Phase 6 acceptance test: create a binary habit, check it in for today,
 * and see the streak update (Section 16/17/50 acceptance criteria).
 */
class HabitsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun creatingABinaryHabitAndCheckingItInUpdatesTheStreak() {
        val habitsTab = composeRule.activity.getString(R.string.nav_habits)
        val addHabit = composeRule.activity.getString(R.string.add_habit)
        val nameLabel = composeRule.activity.getString(R.string.habit_name_label)
        val save = composeRule.activity.getString(R.string.action_save)

        composeRule.onNodeWithText(habitsTab).performClick()
        composeRule.onNodeWithContentDescription(addHabit).performClick()
        composeRule.onNodeWithText(nameLabel).performTextInput("Reading")
        composeRule.onNodeWithText(save).performClick()

        composeRule.onNodeWithText("Reading").assertExists()

        val checkbox = composeRule.onNode(isToggleable())
        checkbox.assertIsOff()
        checkbox.performClick()
        checkbox.assertIsOn()

        // 1-day streak should now show.
        val oneDayStreak = composeRule.activity.getString(R.string.habit_streak_format, 1)
        composeRule.onNodeWithText(oneDayStreak).assertExists()
    }
}
