package com.teamschedulerapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.AuthRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.TeamWithMembers
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
        val authRepository = remember { AuthRepository(supabase) }
        val teamRepository = remember { TeamRepository(supabase.postgrest) }

        var userId by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var showCreateTeamModal by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val userTeams by TeamManager.userTeams.collectAsState()

        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val currentUser = supabase.auth.currentUserOrNull()
                    userId = currentUser?.id
                    if (userId != null) {
                        val fetchedTeams = teamRepository.getTeamsForUser().map { team ->
                            TeamWithMembers(
                                id = team.id,
                                name = team.name,
                                description = team.description ?: "",
                                members = emptyList()
                            )
                        }
                        TeamManager.setUserTeams(fetchedTeams)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
                CircularProgressIndicator()
            }
        } else {
            ManageTeamsScreen(
                userId = userId ?: "",
                teams = userTeams,
                onCreateTeam = { showCreateTeamModal = true },
                onTeamSelected = { team -> TeamManager.selectTeam(team) },
                onBack = { navigator.pop() }
            )

            if (showCreateTeamModal) {
                CreateTeamModal(
                    onDismiss = { showCreateTeamModal = false },
                    onSave = { name, description ->
                        scope.launch {
                            val newTeam = teamRepository.createTeam(name, description)
                            if (newTeam != null) {
                                val updatedTeams = userTeams + TeamWithMembers(
                                    id = newTeam.id,
                                    name = newTeam.name,
                                    description = newTeam.description ?: "",
                                    members = emptyList()
                                )
                                TeamManager.setUserTeams(updatedTeams)
                            }
                            showCreateTeamModal = false
                        }
                    }
                )
            }
        }
    }
}
