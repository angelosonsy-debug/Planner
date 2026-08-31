package com.plannermvp.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.ui.graphics.vector.ImageVector
import com.plannermvp.app.R

/**
 * The five bottom-navigation destinations from the product spec (Section 3):
 * Today, Tasks, Projects, Habits, More. Kept flat and small on purpose —
 * "Reduce cognitive load" (Section 2).
 */
enum class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    TODAY("today", R.string.nav_today, Icons.Filled.Home),
    TASKS("tasks", R.string.nav_tasks, Icons.Filled.CheckCircle),
    PROJECTS("projects", R.string.nav_projects, Icons.Filled.Folder),
    HABITS("habits", R.string.nav_habits, Icons.Filled.Repeat),
    MORE("more", R.string.nav_more, Icons.Filled.MoreHoriz);

    companion object {
        val bottomBarOrder = listOf(TODAY, TASKS, PROJECTS, HABITS, MORE)
    }
}
