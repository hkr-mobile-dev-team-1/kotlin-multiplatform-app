package com.teamschedulerapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.teamschedulerapp.data.AuthRepository
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.User
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.navigation.Login
import com.teamschedulerapp.navigation.ManageTeamsScreenWrapper
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.MainScreenModel
import com.teamschedulerapp.screenmodel.TaskScreenModel
import com.teamschedulerapp.ui.components.team.AdminBadge
import com.teamschedulerapp.ui.components.CustomSnackbarHost
import com.teamschedulerapp.ui.components.team.CreateTeamModal
import com.teamschedulerapp.ui.components.team.TeamSelectorModal
import com.teamschedulerapp.ui.components.team.TeamTile
import com.teamschedulerapp.ui.screens.analytics.AnalyticsScreen
import com.teamschedulerapp.ui.screens.schedule.ScheduleScreen
import com.teamschedulerapp.ui.screens.settings.ManageTeamsScreen
import com.teamschedulerapp.ui.screens.settings.SettingsScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import com.teamschedulerapp.repositories.AvailabilityRepository

object ScheduleTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Schedule"
            val icon: Painter? = rememberVectorPainter(Icons.Rounded.CalendarMonth)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        // get Supabase client
        val supabase = com.teamschedulerapp.data.SupabaseClientManager.client
        val authUser = supabase.auth.currentUserOrNull() ?: return
        val userId = authUser.id

        // repositories
        val availabilityRepository = remember { AvailabilityRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }

        // logged-in user
        var appUser by remember { mutableStateOf<com.teamschedulerapp.model.User?>(null) }

        LaunchedEffect(userId) {
            appUser = userRepository.getUserById(userId)
        }

        //get a display user name
        val displayName = listOfNotNull(
            appUser?.firstName?.takeIf { it.isNotBlank() },
            appUser?.lastName?.takeIf { it.isNotBlank() },
        ).joinToString(" ")
            .ifBlank {
                // fallback to auth metadata then email
                authUser.userMetadata?.get("full_name")?.toString()?.takeIf { it.isNotBlank() }
                    ?: authUser.email ?: "You"
            }

        ScheduleScreen(
            availabilityRepository = availabilityRepository,
            userRepository = userRepository,
            userId = userId,
            currentUserDisplayName = displayName
        )
    }
}

object TasksTab : Tab {
    var snackbarHostState: SnackbarHostState? = null
    override val options: TabOptions
        @Composable
        get() {
            val title = "Tasks"
            val icon = rememberVectorPainter(Icons.Rounded.ViewAgenda)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        val supabase = SupabaseClientManager.client
        val taskRepository = remember { TaskRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val taskAssignmentRepository = remember { TaskAssignmentRepository(supabase.postgrest) }
        val screenModel = rememberScreenModel {
            TaskScreenModel(
                taskRepository = taskRepository,
                userRepository = userRepository,
                taskAssignmentRepository = taskAssignmentRepository
            )
        }
        TasksScreen(
            screenModel = screenModel,
            snackbarHostState = snackbarHostState
        )
    }
}

object AnalyticsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Analytics"
            val icon = rememberVectorPainter(Icons.Rounded.BarChart)
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        AnalyticsScreen()
    }
}

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Settings"
            val icon = rememberVectorPainter(Icons.Rounded.Settings)
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
        val supabase = SupabaseClientManager.client
        val authRepository = remember { AuthRepository(supabase) }
        val tabNavigator = LocalNavigator.currentOrThrow
        val rootNavigator = tabNavigator.parent ?: tabNavigator

        SettingsScreen(
            user = dummyUser,
            authRepository = authRepository,
            onBack = {
                rootNavigator.replaceAll(Login)
            },
            onManageTeams = {
                rootNavigator.push(ManageTeamsScreenWrapper)
            }

        )
    }
}


@Composable
fun MainScreen() {
    val currentTeam by TeamManager.currentTeam.collectAsState()
    val userTeams by TeamManager.userTeams.collectAsState()

    var showTeamSelector by remember { mutableStateOf(false) }
    var showCreateTeamModal by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<TeamWithMembers?>(null) }

    val scope = rememberCoroutineScope()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    TasksTab.snackbarHostState = snackbarHostState


    // Supabase
    val supabase = SupabaseClientManager.client
    val userId = supabase.auth.currentUserOrNull()?.id ?: return
    val teamRepository = remember { TeamRepository(supabase.postgrest) }
    val userRepository = remember { UserRepository(supabase.postgrest) }
    val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }

    val isCurrentTeamAdmin = currentTeam?.members?.find { it.id == userId }?.isAdmin ?: false

    val mainScreenModel = remember {
        MainScreenModel(
            teamRepository = teamRepository,
            teamMemberRepository = teamMemberRepository,
            userRepository = userRepository,
            userId = userId
        )
    }

    TabNavigator(ScheduleTab) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showTeamSelector = true }
                                .widthIn(max = 280.dp)
                        ) {
                            TeamTile(currentTeam)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentTeam?.name ?: "Select team",
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isCurrentTeamAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AdminBadge()
                            }
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
                    TabNavigationItem(AnalyticsTab)
                    TabNavigationItem(SettingsTab)
                }
            },
            snackbarHost = { CustomSnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                CurrentTab()
            }
        }
    }

    // Team selector modal
    if (showTeamSelector) {
        TeamSelectorModal(
            userId = userId,
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
            onEditTeam = { team ->
                teamToEdit = team
                showTeamSelector = false
            },
            onDeleteTeam = { team ->
                scope.launch {
                    try {
                        mainScreenModel.deleteTeam(team.id ?: "")
                        // Show success snackbar
                        snackbarHostState.showSuccessSnackbar("Team deleted successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState.showErrorSnackbar("Failed to delete team")
                    }
                }
                showTeamSelector = false
            },
            onDismiss = { showTeamSelector = false }
        )
    }

    if (showCreateTeamModal) {
        CreateTeamModal(
            teamToEdit = teamToEdit,
            onDismiss = { showCreateTeamModal = false },
            onSave = { name, description ->
                scope.launch {
                    try {
                        mainScreenModel.createTeam(name, description)
                        // Show success snackbar
                        snackbarHostState.showSuccessSnackbar("Team created successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState.showErrorSnackbar("Failed to create team")
                    }
                }
                showCreateTeamModal = false
            }
        )
    }

    // Edit team modal
    teamToEdit?.let { team ->
        CreateTeamModal(
            teamToEdit = team,
            onDismiss = { teamToEdit = null },
            onSave = { name, description ->
                scope.launch {
                    try {
                        mainScreenModel.updateTeam(team.id ?: "", name, description)
                        // Show success snackbar
                        snackbarHostState.showSuccessSnackbar("Team updated successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState.showErrorSnackbar("Failed to update team")
                    }
                }
                teamToEdit = null
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
            tab.options.icon?.let { painter ->
                Icon(
                    painter = painter,
                    contentDescription = tab.options.title
                )
            }
        },
        label = { Text(tab.options.title) }
    )
}
