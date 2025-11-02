package com.teamschedulerapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.SettingsScreenModel
import com.teamschedulerapp.ui.components.CustomSnackbarHost
import com.teamschedulerapp.ui.components.team.TeamDetailModal
import com.teamschedulerapp.ui.screens.settings.ManageTeamsScreen
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

object ManageTeamsScreenWrapper : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabase = SupabaseClientManager.client

        val teamRepository = remember { TeamRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }
        val userId = remember { UserManager.getCurrentUserId() }

        val settingsScreenModel = remember {
            SettingsScreenModel(
                teamRepository = teamRepository,
                teamMemberRepository = teamMemberRepository,
                userRepository = userRepository,
                userId = userId
            )
        }

        val scope = rememberCoroutineScope()
        val userTeams by TeamManager.userTeams.collectAsState()
        val isLoading by settingsScreenModel.isLoading.collectAsState()

        var showCreateTeamModal by remember { mutableStateOf(false) }

        // Custom Snackbar
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { CustomSnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    ManageTeamsScreen(
                        userId = userId,
                        teams = userTeams,
                        onCreateTeam = { showCreateTeamModal = true },
                        onTeamSelected = { team -> TeamManager.selectTeam(team) },
                        onBack = { navigator.pop() }
                    )
                }

                if (showCreateTeamModal) {
                    TeamDetailModal(
                        onDismiss = { showCreateTeamModal = false },
                        onSave = { name, description ->
                            scope.launch {
                                try {
                                    settingsScreenModel.createTeam(name, description)
                                    snackbarHostState.showSuccessSnackbar("Team created successfully")
                                } catch (e: Exception) {
                                    snackbarHostState.showErrorSnackbar("Failed to create team")
                                } finally {
                                    showCreateTeamModal = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
