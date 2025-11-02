package com.teamschedulerapp.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.ui.MainScreen
import com.teamschedulerapp.ui.screens.login.LoginScreen
import com.teamschedulerapp.ui.screens.login.ResetPasswordScreen
import com.teamschedulerapp.ui.screens.login.UpdatePasswordScreen
import com.teamschedulerapp.ui.screens.signup.SignupScreen

object Login : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LoginScreen(
            onNavigateToSignUp = { navigator.push(Register) },
            onLoginSuccess = { navigator.replace(MainScreen) },
            onNavigateToResetPassword = { navigator.push(RequestPasswordReset) }
        )
    }
}

object RequestPasswordReset : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ResetPasswordScreen(onNavigateBack = { navigator.pop() })
    }
}

data class ResetPassword(val sessionFragment: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ResetPasswordScreen(onNavigateBack = { navigator.pop() }, sessionFragment = sessionFragment)
    }
}

object UpdatePassword : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        UpdatePasswordScreen(
            onPasswordUpdated = { navigator.replaceAll(Login) }
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
    override fun Content() = MainScreen()
}
