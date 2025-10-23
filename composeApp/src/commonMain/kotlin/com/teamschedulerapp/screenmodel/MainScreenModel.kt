package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.model.Team
import com.teamschedulerapp.model.User
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
                val teams = teamRepository.getTeamsForUser(userId)
                println("TeamScreenModel - Loaded ${teams.size} teams")
                TeamManager.setUserTeams(teams)
            } catch (e: Exception) {
                _error.value = "Failed to load teams: ${e.message}"
                println("TeamScreenModel - Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTeam(name: String, description: String) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Create team
                val createdTeam = teamRepository.createTeam(
                    Team(
                        name = name,
                        description = description.ifBlank { null }
                    )
                )

                if (createdTeam != null) {
                    println("Team created")
                    loadUserTeams()
                    TeamManager.selectTeam(createdTeam)
                } else {
                    _error.value = "Failed to create team"
                }
            } catch (e: Exception) {
                _error.value = "Failed to create team: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTeamMembers(teamId : String) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val teamMembers = teamMemberRepository.getMembersForTeam(teamId)

                if (teamMembers.isNotEmpty()) {
                    println("Team Members fetched: ${teamMembers.size}")

                    // Map each TeamMember to User
                    val users = teamMembers.mapNotNull { teamMember ->
                        userRepository.getUserById(teamMember.userId)
                    }

                    println("Users fetched: ${users.size}")

                    TeamManager.setCurrentTeamMembers(users)

                } else {
                    println("No team members found")
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch team members: ${e.message}"
                println("Error fetching team members: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

}