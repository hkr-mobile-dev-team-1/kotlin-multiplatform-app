package com.teamschedulerapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme

// define single brand seed here (or inject it)
val BrandSeed = Color(0xFFFF6F00)

// Add the ThemeMode enum here (no separate file needed)
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM, // now takes ThemeMode
    seed: Color = BrandSeed,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // generate full Material3 ColorScheme (primary/secondary/tertiary/surface/etc.)
    val scheme = rememberDynamicColorScheme(seedColor = seed, isDark = dark)

    MaterialTheme(
        colorScheme = scheme,
        // optional plug in typography
        // typography = AppTypography,
        // shapes = AppShapes,
        content = content
    )
}
