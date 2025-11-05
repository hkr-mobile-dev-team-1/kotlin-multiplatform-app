package com.teamschedulerapp.ui.theme

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ThemePreferences {
    private val settings = Settings()
    private const val THEME_KEY = "theme_mode"

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private const val THEME_PREF_KEY = "theme_mode"

    fun ThemePreferences.getThemeMode(): ThemeMode {
        // read stored string and map to enum; fallback to SYSTEM
        val name = settings.getString(THEME_PREF_KEY, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun loadTheme(): ThemeMode {
        return when (settings.getString(THEME_KEY, ThemeMode.SYSTEM.name)) {
            ThemeMode.DARK.name -> ThemeMode.DARK
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            else -> ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        settings[THEME_KEY] = mode.name
        _themeMode.update { mode }
    }
}
