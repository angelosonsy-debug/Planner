package com.plannermvp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.plannermvp.app.navigation.AppDestination
import com.plannermvp.app.navigation.AppNavHost
import com.plannermvp.app.navigation.navigateToTab
import com.plannermvp.app.ui.theme.PlannerMvpTheme
import com.plannermvp.app.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ThemedAppRoot() }
    }
}

@Composable
private fun ThemedAppRoot() {
    val settingsVm = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory)
    val settings   by settingsVm.settings.collectAsState()

    // Release 1.0: theme driven by settings (default = "light")
    PlannerMvpTheme(themeMode = settings.themeMode) {
        AppRoot()
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { PlannerBottomBar(navController) }
    ) { innerPadding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun PlannerBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        AppDestination.bottomBarOrder.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick  = { navController.navigateToTab(destination.route) },
                icon     = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                label    = { Text(stringResource(destination.labelRes)) }
            )
        }
    }
}
