package com.teamschedulerapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.teamschedulerapp.navigation.AppNavHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}