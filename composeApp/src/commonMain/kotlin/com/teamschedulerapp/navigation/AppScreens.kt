package com.teamschedulerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.ui.MainScreen
import com.teamschedulerapp.ui.screens.login.LoginScreen
import com.teamschedulerapp.ui.screens.signup.SignupScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

object Login : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val supabase = SupabaseClientManager.client
        val teamRepository = TeamRepository(supabase.postgrest)

        LoginScreen(
            onNavigateToSignUp = { navigator.push(Register) },
            onLoginSuccess = {
                scope.launch {
                    // Load user's teams after successful login
                    val userId = supabase.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val teams = teamRepository.getTeamsForUser(userId)
                        TeamManager.setUserTeams(teams)
                    }
                    navigator.replace(MainScreen)
                }
            }
        )
    }
}

object Register : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val supabase = SupabaseClientManager.client
        val teamRepository = TeamRepository(supabase.postgrest)

        SignupScreen(
            onNavigateToLogin = { navigator.pop() },
            onSignupSuccess = {
                scope.launch {
                    // Load user's teams after successful signup
                    val userId = supabase.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val teams = teamRepository.getTeamsForUser(userId)
                        TeamManager.setUserTeams(teams)
                    }
                    navigator.replace(MainScreen)
                }
            }
        )
    }
}

object MainScreen : Screen {
    @Composable
    override fun Content() = MainScreen() // Changed from ScheduleScreen()
}
