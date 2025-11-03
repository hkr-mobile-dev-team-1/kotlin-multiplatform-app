package com.teamschedulerapp.screenmodel

import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.data.AuthRepository
import com.teamschedulerapp.model.Notification
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.navigation.UserManager
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.TeamRepository
import com.teamschedulerapp.repositories.UserRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenModel(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
) : ScreenModel {

    val userId = UserManager.getCurrentUserId()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _notifications = MutableStateFlow<List<com.teamschedulerapp.model.Notification>>(emptyList())
    val notifications: StateFlow<List<com.teamschedulerapp.model.Notification>> = _notifications.asStateFlow()

    init {
        loadUserTeams()
        loadNotifications()
    }

    fun loadUserTeams() {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get all teams for the user
                val teams = teamRepository.getTeamsForUser()

                // For each team, fetch its members and compose UserTeamWithMembers
                val teamsWithMembers = teams.mapNotNull { team ->
                    // Skip teams without an ID
                    val teamId = team.id ?: return@mapNotNull null

                    // Get team members (TeamMember objects with userId, teamId, isAdmin)
                    val teamMembers = teamMemberRepository.getMembersForTeam(teamId)

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
                        id = teamId,
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
                println("MainScreenModel - Team updated: $teamId")
                loadUserTeams()
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
    // Notification management
    private fun loadNotifications() {
        screenModelScope.launch {
            // TODO: Replace with actual repository call when backend is ready
            // For now, using mock data
            _notifications.value = generateMockNotifications()
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        screenModelScope.launch {
            _notifications.value = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
        }
    }

    fun markAllNotificationsAsRead() {
        screenModelScope.launch {
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        }
    }

    fun deleteNotification(notificationId: String) {
        screenModelScope.launch {
            _notifications.value = _notifications.value.filter { it.id != notificationId }
        }
    }

    fun clearAllNotifications() {
        screenModelScope.launch {
            _notifications.value = emptyList()
        }
    }

    // Mock data generator - remove when backend is ready
    private fun generateMockNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = "1",
                title = "New Task Assigned",
                message = "You've been assigned to 'Update documentation'",
                timestamp = "2 hours ago",
                isRead = false,
                type = "task"
            ),
            Notification(
                id = "2",
                title = "Team Invite",
                message = "You've been invited to join 'Marketing Team'",
                timestamp = "5 hours ago",
                isRead = false,
                type = "team"
            ),
            Notification(
                id = "3",
                title = "Schedule Change",
                message = "Your availability for Monday has been updated",
                timestamp = "1 day ago",
                isRead = true,
                type = "schedule"
            ),
            Notification(
                id = "4",
                title = "Task Completed",
                message = "John completed 'Design wireframes'",
                timestamp = "2 days ago",
                isRead = true,
                type = "task"
            )
        )
    }
}