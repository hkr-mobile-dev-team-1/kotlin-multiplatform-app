package com.teamschedulerapp.navigation

import com.teamschedulerapp.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TeamManager {
    private val _currentTeam = MutableStateFlow<Team?>(null)
    val currentTeam: StateFlow<Team?> = _currentTeam.asStateFlow()

    private val _userTeams = MutableStateFlow<List<Team>>(emptyList())
    val userTeams: StateFlow<List<Team>> = _userTeams.asStateFlow()

    fun selectTeam(team: Team) {
        _currentTeam.value = team
    }

    fun setUserTeams(teams: List<Team>) {
        _userTeams.value = teams
        if (_currentTeam.value == null && teams.isNotEmpty()) {
            _currentTeam.value = teams.first() // Auto-select first team
        }
    }

    fun clearTeam() {
        _currentTeam.value = null
    }

    val hasTeams: Boolean
        get() = _userTeams.value.isNotEmpty()
}