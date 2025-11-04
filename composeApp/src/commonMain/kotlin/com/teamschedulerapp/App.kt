package com.teamschedulerapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.teamschedulerapp.navigation.Login
import com.teamschedulerapp.navigation.UpdatePassword
import com.teamschedulerapp.ui.theme.AppTheme

@Composable
fun App(sessionFragment: String? = null) {
    AppTheme {
        if (sessionFragment != null) {
            Navigator(UpdatePassword(sessionFragment))
        } else {
            Navigator(Login)
        }
    }
}