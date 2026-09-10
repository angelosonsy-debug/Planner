package com.plannermvp.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.plannermvp.app.ui.screens.BackupScreen
import com.plannermvp.app.ui.screens.CalendarScreen
import com.plannermvp.app.ui.screens.HabitDetailScreen
import com.plannermvp.app.ui.screens.HabitsScreen
import com.plannermvp.app.ui.screens.ImportScreen
import com.plannermvp.app.ui.screens.MatrixScreen
import com.plannermvp.app.ui.screens.MoreScreen
import com.plannermvp.app.ui.screens.ProjectsScreen
import com.plannermvp.app.ui.screens.SettingsScreen
import com.plannermvp.app.ui.screens.TasksScreen
import com.plannermvp.app.ui.screens.TodayScreen

/** Routes reachable from within a tab (nested), not in the bottom bar. */
object NestedRoute {
    const val IMPORT       = "import"
    const val MATRIX       = "matrix"
    const val SETTINGS     = "settings"
    const val BACKUP       = "backup"
    const val CALENDAR     = "calendar"   // Release 1.0: was missing, Calendar never opened
    const val HABIT_DETAIL = "habit_detail/{habitId}"

    fun habitDetail(habitId: String) = "habit_detail/$habitId"
}

/**
 * Navigate to a top-level bottom-bar tab.
 *
 * Release 1.0 fix: saveState = false + restoreState = false.
 * Previously saveState = true meant that More→Settings→Today→More
 * would restore Settings instead of showing More root.
 */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = false   // KEY: do not save child back-stack
        }
        launchSingleTop = true
        restoreState    = false // KEY: do not restore previously-saved state
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController    = navController,
        startDestination = AppDestination.TODAY.route,
        modifier         = modifier
    ) {
        // ── Top-level tabs ────────────────────────────────────────────────
        composable(AppDestination.TODAY.route)    { TodayScreen() }
        composable(AppDestination.TASKS.route)    { TasksScreen() }
        composable(AppDestination.PROJECTS.route) { ProjectsScreen() }
        composable(AppDestination.HABITS.route) {
            HabitsScreen(onHabitClick = { navController.navigate(NestedRoute.habitDetail(it)) })
        }
        composable(AppDestination.MORE.route) {
            MoreScreen(
                onMatrixClick   = { navController.navigate(NestedRoute.MATRIX) },
                onImportClick   = { navController.navigate(NestedRoute.IMPORT) },
                onCalendarClick = { navController.navigate(NestedRoute.CALENDAR) },
                onSettingsClick = { navController.navigate(NestedRoute.SETTINGS) },
                onBackupClick   = { navController.navigate(NestedRoute.BACKUP) }
            )
        }

        // ── More children ─────────────────────────────────────────────────
        composable(NestedRoute.MATRIX)   { MatrixScreen() }
        composable(NestedRoute.IMPORT)   { ImportScreen() }
        composable(NestedRoute.CALENDAR) { CalendarScreen() }  // Release 1.0: was missing
        composable(NestedRoute.SETTINGS) {
            SettingsScreen(onBackupClick = { navController.navigate(NestedRoute.BACKUP) })
        }
        composable(NestedRoute.BACKUP) { BackupScreen() }

        // ── Habit detail ──────────────────────────────────────────────────
        composable(NestedRoute.HABIT_DETAIL) { entry ->
            val habitId = entry.arguments?.getString("habitId")
            if (habitId != null) HabitDetailScreen(habitId = habitId)
        }
    }
}
