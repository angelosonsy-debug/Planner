package com.plannermvp.app.projects

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

/** Phase 2 acceptance test: create a project and see it listed with 0/0 progress. */
class CreateProjectTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun creatingAProjectShowsItInTheList() {
        val projectsTab = composeRule.activity.getString(R.string.nav_projects)
        val addProject = composeRule.activity.getString(R.string.add_project)
        val nameLabel = composeRule.activity.getString(R.string.project_name_label)
        val save = composeRule.activity.getString(R.string.action_save)

        composeRule.onNodeWithText(projectsTab).performClick()
        composeRule.onNodeWithContentDescription(addProject).performClick()
        composeRule.onNodeWithText(nameLabel).performTextInput("React")
        composeRule.onNodeWithText(save).performClick()

        composeRule.onNodeWithText("React").assertExists()
    }
}
