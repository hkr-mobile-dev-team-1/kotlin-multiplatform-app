/**
 * TeamManager.kt
 *
 * Manages the global state for teams and team selection across the application.
 *
 * Key Responsibilities:
 * - Stores all teams the current user belongs to
 * - Tracks which team is currently selected by the user
 * - Persists the selected team ID to local storage for session continuity
 * - Provides utility functions for team selection and admin permission checks
 *
 * Architecture:
 * - Uses StateFlow for reactive state management (observable by UI)
 * - Selected team is stored as an ID (String), while full team objects are in userTeams
 * - Current team is derived by finding the team with the selected ID in userTeams list
 * - This ensures single source of truth and automatic updates when team data changes
 *
 * Persistence:
 * - Uses multiplatform-settings library to persist selected team ID locally
 * - Team selection survives app restarts and sessions
 *
 * Usage:
 * ```
 * val currentTeam by TeamManager.currentTeam.collectAsState()
 * val userTeams by TeamManager.userTeams.collectAsState()
 * ```
 */

package com.teamschedulerapp.navigation

import com.teamschedulerapp.model.TeamWithMembers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

object TeamManager {
    private val settings: Settings = Settings()
    private const val SELECTED_TEAM_ID_KEY = "selected_team_id"
    private val _selectedTeamId = MutableStateFlow<String?>(null)
    val selectedTeamId: StateFlow<String?> = _selectedTeamId.asStateFlow()

    private val _userTeams = MutableStateFlow<List<TeamWithMembers>>(emptyList())
    val userTeams: StateFlow<List<TeamWithMembers>> = _userTeams.asStateFlow()

    val currentTeam: StateFlow<TeamWithMembers?> =
        combine(userTeams, selectedTeamId) { teams, teamId ->
            teams.find { it.id == teamId }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = null
        )


    fun selectTeam(team: TeamWithMembers) {
        team.id?.let { teamId ->
            _selectedTeamId.value = teamId
            // Save to persistent storage
            settings[SELECTED_TEAM_ID_KEY] = teamId
        }
    }

    fun selectTeamById(teamId: String) {
        _selectedTeamId.value = teamId
        settings[SELECTED_TEAM_ID_KEY] = teamId
    }

    fun setUserTeams(teams: List<TeamWithMembers>) {
        _userTeams.value = teams
        // Try to restore previously selected team from storage
        val savedTeamId = settings.getStringOrNull(SELECTED_TEAM_ID_KEY)
        val savedTeamExists = teams.any { it.id == savedTeamId }

        if (savedTeamExists) {
            // Restore the saved team
            _selectedTeamId.value = savedTeamId
        } else if (_selectedTeamId.value == null && teams.isNotEmpty()) {
            // Fallback: Auto-select first team if no saved team
            selectTeam(teams.first())
        } else if (!teams.any { it.id == _selectedTeamId.value }) {
            // Current team no longer exists (was deleted), clear selection
            clearTeam()
        }
    }

    fun isUserAdminOfTeam(teamId: String, userId: String): Boolean {
        val team = _userTeams.value.find { it.id == teamId }
        return team?.members?.find { it.id == userId }?.isAdmin ?: false
    }

    fun isUserAdminOfCurrentTeam(userId: String): Boolean {
        val currentTeamId = _selectedTeamId.value ?: return false
        return isUserAdminOfTeam(currentTeamId, userId)
    }


    fun clearTeam() {
        _selectedTeamId.value = null
        settings.remove(SELECTED_TEAM_ID_KEY)
    }

    val hasTeams: Boolean
        get() = _userTeams.value.isNotEmpty()
}