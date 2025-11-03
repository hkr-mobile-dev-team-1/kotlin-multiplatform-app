package com.teamschedulerapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.teamschedulerapp.navigation.Login
import com.teamschedulerapp.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        Navigator(Login)
    }
}