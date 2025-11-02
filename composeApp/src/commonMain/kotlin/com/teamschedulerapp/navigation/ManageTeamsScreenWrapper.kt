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
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.screenmodel.MainScreenModel
import com.teamschedulerapp.ui.components.team.TeamDetailModal
import com.teamschedulerapp.ui.screens.settings.ManageTeamsScreen
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar
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
        val userRepository = remember { UserRepository(supabase.postgrest) }
        val teamMemberRepository = remember { TeamMemberRepository(supabase.postgrest, userRepository) }
        var userId by remember { mutableStateOf<String?>(supabase.auth.currentUserOrNull()?.id) }
        var isLoading by remember { mutableStateOf(true) }
        var showCreateTeamModal by remember { mutableStateOf(false) }

        val mainScreenModel = remember {
            MainScreenModel(
                teamRepository = teamRepository,
                teamMemberRepository = teamMemberRepository,
                userRepository = userRepository,
                userId = userId
            )
        }

        val scope = rememberCoroutineScope()
        val userTeams by TeamManager.userTeams.collectAsState()

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
                TeamDetailModal(
                    onDismiss = { showCreateTeamModal = false },
                    onSave = { name, description ->
                        scope.launch {
                            try {
                                mainScreenModel.createTeam(name, description)
                                // TODO: Show success snackbar
                            } catch (e: Exception) {
                                // TODO: Show error snackbar
                            }
                        }
                    }
                )
            }
        }
    }
}
