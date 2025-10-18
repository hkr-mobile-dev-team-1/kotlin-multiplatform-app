package com.teamschedulerapp.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.teamschedulerapp.screenmodel.TasksScreenModel
import com.teamschedulerapp.ui.screens.schedule.ScheduleScreen
import com.teamschedulerapp.ui.screens.settings.SettingsScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen

object ScheduleTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Schedule"
            val icon: Painter? = rememberVectorPainter(Icons.Default.DateRange)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        ScheduleScreen()
    }
}

object TasksTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Tasks"
            val icon = rememberVectorPainter(Icons.Default.CheckCircle)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { TasksScreenModel() }
        TasksScreen(screenModel = screenModel)
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Settings"
            val icon = rememberVectorPainter(Icons.Default.Settings)
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        SettingsScreen()
    }
}

@Composable
fun MainScreen() {
    TabNavigator(ScheduleTab) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TabNavigationItem(ScheduleTab)
                    TabNavigationItem(TasksTab)
                    TabNavigationItem(SettingsTab)
                }
            }
        ) {
            CurrentTab()
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            val icon = tab.options.icon as? ImageVector
            icon?.let { Icon(it, contentDescription = tab.options.title) }
        },
        label = { Text(tab.options.title) }
    )
}
