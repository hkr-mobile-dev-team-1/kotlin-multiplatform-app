package com.teamschedulerapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.AuthRepository
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.MainScreenModel
import com.teamschedulerapp.ui.components.team.CreateTeamModal
import com.teamschedulerapp.ui.screens.settings.ManageTeamsScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

object ManageTeamsScreenWrapper : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val supabase = SupabaseClientManager.client

        // repositories
        val authRepository = remember { AuthRepository(supabase) }
        val teamRepository = remember { TeamRepository(supabase.postgrest) }
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }

        // main screen model (for team creation)
        val userIdForModel = supabase.auth.currentUserOrNull()?.id ?: ""
        val mainScreenModel = remember {
            MainScreenModel(
                teamRepository = teamRepository,
                teamMemberRepository = teamMemberRepository,
                userRepository = userRepository,
                userId = userIdForModel
            )
        }

        var userId by remember { mutableStateOf<String?>(null) }
        var teams by remember { mutableStateOf<List<TeamWithMembers>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        var showCreateTeamModal by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        // Helper function to fetch user’s teams
        suspend fun fetchTeams() {
            try {
                val fetchedTeams = teamRepository.getTeamsForUser()
                teams = fetchedTeams.map { team ->
                    TeamWithMembers(
                        id = team.id,
                        name = team.name,
                        members = emptyList()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initial load
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val user = try { authRepository.getCurrentUser() } catch (_: Exception) { null }
                    userId = user?.id ?: supabase.auth.currentUserOrNull()?.id
                    fetchTeams()
                } finally {
                    isLoading = false
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            ManageTeamsScreen(
                userId = userId ?: "",
                teams = teams,
                onCreateTeam = { showCreateTeamModal = true },
                onTeamSelected = { team ->
                    TeamManager.selectTeam(team)
                },
                onBack = { navigator.pop() }
            )
        }

        // Create team modal
        if (showCreateTeamModal) {
            CreateTeamModal(
                onDismiss = { showCreateTeamModal = false },
                onSave = { name, description ->
                    scope.launch {
                        try {
                            mainScreenModel.createTeam(name, description)
                            // ✅ Refresh the team list right after creation
                            fetchTeams()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            showCreateTeamModal = false
                        }
                    }
                }
            )
        }
    }
}
