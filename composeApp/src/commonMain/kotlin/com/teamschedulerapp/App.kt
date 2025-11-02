package com.teamschedulerapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.teamschedulerapp.navigation.Login
import com.teamschedulerapp.navigation.ResetPassword

@Composable
fun App(sessionFragment: String? = null) {
    MaterialTheme {
        if (sessionFragment != null) {
            Navigator(ResetPassword(sessionFragment))
        } else {
            Navigator(Login)
        }
    }
}