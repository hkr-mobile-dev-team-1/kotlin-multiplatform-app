package com.teamschedulerapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.*
import com.teamschedulerapp.model.User
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.screenmodel.TaskScreenModel
import com.teamschedulerapp.ui.screens.schedule.ScheduleScreen
import com.teamschedulerapp.ui.screens.settings.SettingsScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.screenmodel.MainScreenModel
import com.teamschedulerapp.ui.components.team.CreateTeamModal
import com.teamschedulerapp.ui.components.team.TeamSelectorModal
import com.teamschedulerapp.ui.components.team.TeamTile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.UserRepository

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
        val supabase = com.teamschedulerapp.data.SupabaseClientManager.client
        val taskRepository = remember { TaskRepository(supabase.postgrest) }
        val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val taskAssignmentRepository = remember { TaskAssignmentRepository(supabase.postgrest) }
        val screenModel = rememberScreenModel {
            TaskScreenModel(
                taskRepository = taskRepository,
                teamMemberRepository = teamMemberRepository,
                userRepository = userRepository,
                taskAssignmentRepository = taskAssignmentRepository
            )
        }
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
        val dummyUser = remember {
            User(
                id = "12345",
                firstName = "Jane",
                lastName = "Doe",
                email = "jane.doe@example.com"
            )
        }

        SettingsScreen(user = dummyUser)
    }
}


@Composable
fun MainScreen() {
    val currentTeam by TeamManager.currentTeam.collectAsState()
    val userTeams by TeamManager.userTeams.collectAsState()
    var showTeamSelector by remember { mutableStateOf(false) }
    var showCreateTeamModal by remember { mutableStateOf(false) }

    val supabase = com.teamschedulerapp.data.SupabaseClientManager.client
    val userId = supabase.auth.currentUserOrNull()?.id ?: return
    val teamRepository = remember { com.teamschedulerapp.repositories.TeamRepository(supabase.postgrest) }
    val teamMemberRepository = remember { com.teamschedulerapp.repositories.TeamMemberRepository(supabase.postgrest) }

    val teamScreenModel = remember {
        MainScreenModel(
            teamRepository = teamRepository,
            teamMemberRepository = teamMemberRepository,
            userId = userId
        )
    }

    TabNavigator(ScheduleTab) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showTeamSelector = true }
                        ) {
                            TeamTile(currentTeam)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = currentTeam?.name ?: "Select team")
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Change team"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    TabNavigationItem(ScheduleTab)
                    TabNavigationItem(TasksTab)
                    TabNavigationItem(SettingsTab)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                CurrentTab()
            }
        }
    }

    // Team selector modal
    if (showTeamSelector) {
        TeamSelectorModal(
            teams = userTeams,
            currentTeam = currentTeam,
            onTeamSelected = { team ->
                TeamManager.selectTeam(team)
                showTeamSelector = false
            },
            onCreateTeam = {
                showTeamSelector = false
                showCreateTeamModal = true
            },
            onDismiss = { showTeamSelector = false }
        )
    }

    if (showCreateTeamModal) {
        CreateTeamModal(
            onDismiss = { showCreateTeamModal = false },
            onSave = { name, description ->
                teamScreenModel.createTeam(name, description)
                showCreateTeamModal = false
            }
        )
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
