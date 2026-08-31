package com.plannermvp.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.plannermvp.app.ui.screens.BackupScreen
import com.plannermvp.app.ui.screens.HabitDetailScreen
import com.plannermvp.app.ui.screens.HabitsScreen
import com.plannermvp.app.ui.screens.ImportScreen
import com.plannermvp.app.ui.screens.MatrixScreen
import com.plannermvp.app.ui.screens.MoreScreen
import com.plannermvp.app.ui.screens.ProjectsScreen
import com.plannermvp.app.ui.screens.SettingsScreen
import com.plannermvp.app.ui.screens.TasksScreen
import com.plannermvp.app.ui.screens.TodayScreen

/** Routes reachable from within a tab but not part of the bottom bar itself. */
object NestedRoute {
    const val IMPORT = "import"
    const val MATRIX = "matrix"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val HABIT_DETAIL = "habit_detail/{habitId}"

    fun habitDetail(habitId: String) = "habit_detail/$habitId"
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.TODAY.route,
        modifier = modifier
    ) {
        composable(AppDestination.TODAY.route) { TodayScreen() }
        composable(AppDestination.TASKS.route) { TasksScreen() }
        composable(AppDestination.PROJECTS.route) { ProjectsScreen() }
        composable(AppDestination.HABITS.route) {
            HabitsScreen(onHabitClick = { habitId -> navController.navigate(NestedRoute.habitDetail(habitId)) })
        }
        composable(AppDestination.MORE.route) {
            MoreScreen(
                onMatrixClick = { navController.navigate(NestedRoute.MATRIX) },
                onImportClick = { navController.navigate(NestedRoute.IMPORT) },
                onSettingsClick = { navController.navigate(NestedRoute.SETTINGS) }
            )
        }
        composable(NestedRoute.IMPORT) { ImportScreen() }
        composable(NestedRoute.MATRIX) { MatrixScreen() }
        composable(NestedRoute.SETTINGS) {
            SettingsScreen(onBackupClick = { navController.navigate(NestedRoute.BACKUP) })
        }
        composable(NestedRoute.BACKUP) { BackupScreen() }
        composable(NestedRoute.HABIT_DETAIL) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            if (habitId != null) HabitDetailScreen(habitId = habitId)
        }
    }
}
