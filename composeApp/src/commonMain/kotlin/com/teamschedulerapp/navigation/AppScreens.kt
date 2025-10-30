package com.teamschedulerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.ui.MainScreen
import com.teamschedulerapp.ui.screens.login.LoginScreen
import com.teamschedulerapp.ui.screens.signup.SignupScreen
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
        val teamMemberRepository = TeamMemberRepository(supabase.postgrest)


        LoginScreen(
            onNavigateToSignUp = { navigator.push(Register) },
            onLoginSuccess = { navigator.replace(MainScreen) }
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
        val teamMemberRepository = TeamMemberRepository(supabase.postgrest)

        SignupScreen(
            onNavigateToLogin = { navigator.pop() },
            onSignupSuccess = { navigator.replace(MainScreen) }
        )
    }
}

object MainScreen : Screen {
    @Composable
    override fun Content() = MainScreen()
}
