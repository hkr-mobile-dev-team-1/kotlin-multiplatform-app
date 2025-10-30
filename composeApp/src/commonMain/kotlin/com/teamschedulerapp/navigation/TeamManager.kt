package com.teamschedulerapp.navigation

import com.teamschedulerapp.model.TeamWithMembers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TeamManager {
    private val _currentTeam = MutableStateFlow<TeamWithMembers?>(null)
    val currentTeam: StateFlow<TeamWithMembers?> = _currentTeam.asStateFlow()

    private val _userTeams = MutableStateFlow<List<TeamWithMembers>>(emptyList())
    val userTeams: StateFlow<List<TeamWithMembers>> = _userTeams.asStateFlow()

    fun selectTeam(team: TeamWithMembers) {
        _currentTeam.value = team
    }

    fun setUserTeams(teams: List<TeamWithMembers>) {
        _userTeams.value = teams
        if (_currentTeam.value == null && teams.isNotEmpty()) {
            _currentTeam.value = teams.first() // Auto-select first team
        }
    }

    fun isUserAdminOfTeam(teamId: String, userId: String): Boolean {
        val team = _userTeams.value.find { it.id == teamId }
        return team?.members?.find { it.id == userId }?.isAdmin ?: false
    }

    fun isUserAdminOfCurrentTeam(userId: String): Boolean {
        val currentTeamId = _currentTeam.value?.id ?: return false
        return isUserAdminOfTeam(currentTeamId, userId)
    }


    fun clearTeam() {
        _currentTeam.value = null
    }

    val hasTeams: Boolean
        get() = _userTeams.value.isNotEmpty()
}