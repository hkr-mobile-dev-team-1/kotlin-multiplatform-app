package com.teamschedulerapp.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.ui.MainScreen
import com.teamschedulerapp.ui.screens.login.LoginScreen
import com.teamschedulerapp.ui.screens.schedule.ScheduleScreen
import com.teamschedulerapp.ui.screens.settings.SettingsScreen
import com.teamschedulerapp.ui.screens.signup.SignupScreen
import com.teamschedulerapp.ui.screens.tasks.TasksScreen

object Login : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LoginScreen(
            onNavigateToSignUp = { navigator.push(Register) },
            onLoginSuccess = {
                navigator.replace(MainScreen) // replace clears backstack
            }
        )
    }
}

object Register : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SignupScreen(
            onNavigateToLogin = { navigator.pop() },
            onSignupSuccess = { navigator.replace(MainScreen) }
        )
    }
}

object MainScreen : Screen {
    @Composable
    override fun Content() = MainScreen() // Changed from ScheduleScreen()
}
