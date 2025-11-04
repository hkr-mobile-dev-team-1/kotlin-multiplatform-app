package com.teamschedulerapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme

// define single brand seed here (or inject it)
val BrandSeed = Color(0xFFFF6F00)

@Composable
fun AppTheme(
    seed: Color = BrandSeed,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
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
