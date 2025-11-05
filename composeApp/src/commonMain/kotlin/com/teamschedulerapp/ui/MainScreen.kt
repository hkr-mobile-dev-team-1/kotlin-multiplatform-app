package com.teamschedulerapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Notifications
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
import com.teamschedulerapp.navigation.UserManager
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.MainScreenModel
import com.teamschedulerapp.screenmodel.TaskScreenModel
import com.teamschedulerapp.ui.components.team.AdminBadge
import com.teamschedulerapp.ui.components.CustomSnackbarHost
import com.teamschedulerapp.ui.components.team.TeamDetailModal
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
import com.teamschedulerapp.ui.components.notifications.NotificationRightSheet
import com.teamschedulerapp.ui.theme.AppTheme
import com.teamschedulerapp.ui.theme.ThemePreferences
import com.teamschedulerapp.ui.theme.ThemePreferences.getThemeMode

object ScheduleTab : Tab {
    var snackbarHostState: SnackbarHostState? = null
    var onCreateTeam: (() -> Unit)? = null

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
        val supabase = SupabaseClientManager.client
        val authUser = supabase.auth.currentUserOrNull() ?: return
        val userId = authUser.id

        // repositories
        val availabilityRepository = remember { AvailabilityRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }

        // logged-in user
        var appUser by remember { mutableStateOf<User?>(null) }

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
            currentUserDisplayName = displayName,
            snackbarHostState = snackbarHostState,
            onCreateTeam = { onCreateTeam?.invoke() }
        )
    }
}

object TasksTab : Tab {
    var snackbarHostState: SnackbarHostState? = null
    var onCreateTeam: (() -> Unit)? = null

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
            snackbarHostState = snackbarHostState,
            onCreateTeam = { onCreateTeam?.invoke() }
        )
    }
}

object AnalyticsTab : Tab {
    var onCreateTeam: (() -> Unit)? = null

    override val options: TabOptions
        @Composable
        get() {
            val title = "Analytics"
            val icon = rememberVectorPainter(Icons.Rounded.BarChart)
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        AnalyticsScreen(
            onCreateTeam = { TasksTab.onCreateTeam?.invoke() }
        )
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

        val supabase = SupabaseClientManager.client
        val authRepository = remember { AuthRepository(supabase) }
        val tabNavigator = LocalNavigator.currentOrThrow
        val rootNavigator = tabNavigator.parent ?: tabNavigator

        val currentUser by UserManager.currentUser.collectAsState()

        if (currentUser == null) {
            // You can show a loading indicator while the user is initializing
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        SettingsScreen(
            user = currentUser!!,
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
    // Supabase
    val supabase = SupabaseClientManager.client
    val teamRepository = remember { TeamRepository(supabase.postgrest) }
    val userRepository = remember { UserRepository(supabase.postgrest) }
    val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }
    val mainScreenModel = remember {
        MainScreenModel(
            teamRepository = teamRepository,
            teamMemberRepository = teamMemberRepository,
            userRepository = userRepository,
        )
    }
    val userId = UserManager.getCurrentUserId()
    val currentTeam by TeamManager.currentTeam.collectAsState()
    val userTeams by TeamManager.userTeams.collectAsState()
    val isTeamManagerInitialized by TeamManager.isInitialized.collectAsState()

    val themeMode = remember { ThemePreferences.getThemeMode() }

    if (!isTeamManagerInitialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var showTeamSelector by remember { mutableStateOf(false) }
    var showCreateTeamModal by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<TeamWithMembers?>(null) }

    val scope = rememberCoroutineScope()

    // Snackbar setup
    val snackbarHostState = remember { SnackbarHostState() }
    TasksTab.snackbarHostState = snackbarHostState
    ScheduleTab.snackbarHostState = snackbarHostState

    // Set onCreateTeam callbacks
    TasksTab.onCreateTeam = { showCreateTeamModal = true }
    ScheduleTab.onCreateTeam = { showCreateTeamModal = true }
    AnalyticsTab.onCreateTeam = { showCreateTeamModal = true }

    var showNotificationModal by remember { mutableStateOf(false) }
    val notifications by mainScreenModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val isCurrentTeamAdmin = currentTeam?.members?.find { it.id == userId }?.isAdmin ?: false


    val userThemeMode by ThemePreferences.themeMode.collectAsState()

    AppTheme(themeMode = userThemeMode) {
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
                        actions = {
                            IconButton(
                                onClick = { showNotificationModal = true },
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .offset(x = 2.dp, y = (-1).dp)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }
                            }
                        }
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
                            mainScreenModel.deleteTeam(team.id)
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

        // Notification Modal
        if (showNotificationModal) {
            NotificationRightSheet(
                notifications = notifications,
                onDismiss = { showNotificationModal = false },
                onNotificationClick = { notification ->
                    // Handle notification click (mark as read, navigate, etc.)
                },
                onDeleteNotification = {},
                onMarkAllAsRead = {},
                onClearAll = {
                    // Clear all notifications
                }
            )
        }

        if (showCreateTeamModal) {
            TeamDetailModal(
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
            TeamDetailModal(
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