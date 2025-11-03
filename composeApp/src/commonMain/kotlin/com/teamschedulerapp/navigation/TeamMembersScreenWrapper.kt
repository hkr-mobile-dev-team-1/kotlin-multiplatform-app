package com.teamschedulerapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.SettingsScreenModel
import com.teamschedulerapp.ui.components.CustomSnackbarHost
import com.teamschedulerapp.ui.screens.settings.TeamMembersScreen
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class TeamMembersScreenWrapper(private val team: TeamWithMembers) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabase = SupabaseClientManager.client

        val teamRepository = remember { TeamRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }

        val userId = remember { UserManager.getCurrentUserId() }

        val settingsModel = remember {
            SettingsScreenModel(
                teamRepository = teamRepository,
                teamMemberRepository = teamMemberRepository,
                userRepository = userRepository,
                userId = userId
            )
        }

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val isLoading by settingsModel.isLoading.collectAsState()

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
                    TeamMembersScreen(
                        team = team,
                        onBack = { navigator.pop() },
                        onRemoveMember = { member ->
                            scope.launch {
                                try {
                                    val success = teamMemberRepository.removeMember(
                                        teamId = team.id ?: return@launch,
                                        userId = member.id
                                    )
                                    if (success) {
                                        settingsModel.loadUserTeams()
                                        snackbarHostState.showSuccessSnackbar("Member removed successfully")
                                    } else {
                                        snackbarHostState.showErrorSnackbar("Failed to remove member")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showErrorSnackbar("Error: ${e.message}")
                                }
                            }
                        },
                        onMakeAdmin = { member ->
                            scope.launch {
                                // later replace this with your real "make admin" API call
                                snackbarHostState.showSuccessSnackbar("${member.firstName ?: "User"} promoted to admin (mock)")
                            }
                        }
                    )
                }
            }
        }
    }
}
