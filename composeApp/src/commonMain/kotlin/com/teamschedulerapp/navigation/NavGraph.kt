package com.teamschedulerapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teamschedulerapp.ui.screens.schedule.ScheduleScreen
import com.teamschedulerapp.ui.screens.settings.SettingsScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Schedule : Screen("schedule", "Schedule", Icons.Default.DateRange)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.CheckCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Screen.Schedule, Screen.Tasks, Screen.Settings).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Schedule.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier
        ) {
            composable(Screen.Schedule.route) {
                ScheduleScreen()
            }
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}