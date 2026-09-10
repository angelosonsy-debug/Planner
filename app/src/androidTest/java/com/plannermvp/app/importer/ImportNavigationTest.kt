package com.plannermvp.app.importer

import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * Phase 4 acceptance test (navigation slice): More -> Import Plan reaches
 * the Import screen and shows the "Choose File" entry point. Actually
 * driving the system file picker needs Espresso-Intents stubbing, which
 * is out of scope here — the parsing/validation/import logic itself is
 * covered thoroughly by JVM unit tests instead (Txt/CsvPlanParserTest,
 * ImportValidatorTest, ImportRepositoryTest).
 */
class ImportNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearDatabase() {
        val app = ApplicationProvider.getApplicationContext<PlannerApp>()
        app.database.clearAllTables()
    }

    @Test
    fun tappingImportPlanFromMoreOpensTheImportScreen() {
        val moreTab = composeRule.activity.getString(R.string.nav_more)
        val importEntry = composeRule.activity.getString(R.string.more_import)
        val chooseFile = composeRule.activity.getString(R.string.import_select_file)

        composeRule.onNodeWithText(moreTab).performClick()
        composeRule.onNodeWithText(importEntry).performClick()

        composeRule.onNodeWithText(chooseFile).assertExists()
    }
}
