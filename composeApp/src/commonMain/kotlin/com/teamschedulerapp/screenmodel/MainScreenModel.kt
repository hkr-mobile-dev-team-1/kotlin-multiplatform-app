package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.model.Team
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenModel(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val userId: String
) : ScreenModel {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserTeams()
    }

    fun loadUserTeams() {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get all teams for the user
                val teams = teamRepository.getTeamsForUser()

                // For each team, fetch its members and compose UserTeamWithMembers
                val teamsWithMembers = teams.map { team ->
                    // Get team members (TeamMember objects with userId, teamId, isAdmin)
                    val teamMembers = teamMemberRepository.getMembersForTeam(team.id ?: "")

                    // For each team member, fetch the user details and combine
                    val membersWithUser = teamMembers.mapNotNull { teamMember ->
                        // Fetch user details
                        val user = userRepository.getUserById(teamMember.userId)

                        // Combine user info with admin status
                        user?.let {
                            TeamMemberWithUser(
                                id = it.id,
                                firstName = it.firstName,
                                lastName = it.lastName,
                                email = it.email,
                                isAdmin = teamMember.isAdmin
                            )
                        }
                    }
                    TeamWithMembers(
                        id = team.id,
                        name = team.name,
                        description = team.description,
                        createdBy = team.createdBy,
                        members = membersWithUser
                    )
                }
                TeamManager.setUserTeams(teamsWithMembers)
                println("MainScreenModel - Loaded ${teamsWithMembers.size} teams with members")
            } catch (e: Exception) {
                println("MainScreenModel - Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun createTeam(name: String, description: String) {
        _isLoading.value = true
        _error.value = null

        try {
            val createdTeam = teamRepository.createTeam(
                name = name,
                description = description.ifBlank { null }
            )

            if (createdTeam != null) {
                println("Team created")
                loadUserTeams()
                TeamManager.userTeams.value.find { it.id == createdTeam.id }?.let { team ->
                    TeamManager.selectTeam(team)
                }
            } else {
                throw Exception("Failed to create team")
            }
        } catch (e: Exception) {
            println("MainScreenModel - Error creating team: ${e.message}")
            throw e // Rethrow so UI can catch and display snackbar
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateTeam(teamId: String, name: String, description: String) {
        _isLoading.value = true
        _error.value = null

        try {
            val success = teamRepository.updateTeam(
                teamId = teamId,
                name = name,
                description = description.ifBlank { null }
            )

            if (success) {
                println("Team updated: $teamId")
                loadUserTeams()
                TeamManager.currentTeam.value?.let { currentTeam ->
                    if (currentTeam.id == teamId) {
                        TeamManager.userTeams.value.find { it.id == teamId }?.let { updatedTeam ->
                            TeamManager.selectTeam(updatedTeam)
                        }
                    }
                }
            } else {
                throw Exception("Failed to update team")
            }
        } catch (e: Exception) {
            println("MainScreenModel - Error updating team: ${e.message}")
            throw e // Rethrow so UI can catch and display snackbar
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteTeam(teamId: String) {
        _isLoading.value = true
        _error.value = null

        try {
            val success = teamRepository.deleteTeam(teamId)

            if (success) {
                println("Team deleted: $teamId")

                // If deleted team was the current team, clear it
                if (TeamManager.currentTeam.value?.id == teamId) {
                    TeamManager.clearTeam()
                }

                loadUserTeams()
            } else {
                throw Exception("Failed to delete team")
            }
        } catch (e: Exception) {
            println("MainScreenModel - Error deleting team: ${e.message}")
            throw e // Rethrow so UI can catch and display snackbar
        } finally {
            _isLoading.value = false
        }
    }

    fun isUserAdminOfTeam(teamId: String): Boolean {
        return TeamManager.isUserAdminOfTeam(teamId, userId)
    }

    fun isUserAdminOfCurrentTeam(): Boolean {
        return TeamManager.isUserAdminOfCurrentTeam(userId)
    }


}