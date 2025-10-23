package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskAssignment
import com.teamschedulerapp.model.TaskWithUsers
import com.teamschedulerapp.model.Team
import com.teamschedulerapp.model.TeamMember
import com.teamschedulerapp.model.User
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskScreenModel(
    private val taskRepository: TaskRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val taskAssignmentRepository: TaskAssignmentRepository,
) : ScreenModel {
    private val _tasksWithUsers = MutableStateFlow<List<TaskWithUsers>>(emptyList())
    val tasksWithUsers: StateFlow<List<TaskWithUsers>> = _tasksWithUsers.asStateFlow()

    private val _teamMembers = MutableStateFlow<List<User>>(emptyList())
    val teamMembers: StateFlow<List<User>> = _teamMembers.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Listen to team changes
        screenModelScope.launch {
            TeamManager.currentTeam.collect { team ->
                team?.id?.let { teamId ->
                    loadTasksForTeam(teamId)
                }
            }
        }
    }

    private suspend fun loadTasksForTeam(teamId: String) {
        _isLoading.value = true
        _error.value = null

        try {
            // 1. Get all tasks for the team
            val tasks = taskRepository.getTasksForTeam(teamId)

            // 2. For each task, get its assignments and then the users
            val tasksWithUsers = tasks.map { task ->
                val taskId = task.id ?: return
                val assignments = taskAssignmentRepository.getAssignmentsForTask(taskId)
                val users = assignments.mapNotNull { assignment ->
                    userRepository.getUserById(assignment.userId)
                }
                TaskWithUsers(task = task, assignedUsers = users)
            }

            _tasksWithUsers.value = tasksWithUsers
        } catch (e: Exception) {
            _error.value = "Failed to load tasks: ${e.message}"
            println("Error loading tasks: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    fun createTask(
        title: String,
        description: String?,
        status: String,
        priority: String,
        assignedUserIds: List<String>?,
        dueDate: String?
    ) {
        val currentTeam = TeamManager.currentTeam.value ?: return
        val teamId = currentTeam.id ?: return

        screenModelScope.launch {
            try {
                val task = Task(
                    teamId = teamId,
                    title = title,
                    description = description,
                    status = status,
                    priority = priority,
                    dueDate = dueDate
                )

                val createdTask = taskRepository.createTask(task)

                // Add users assigned to task
                if (createdTask != null && createdTask.id != null) {
                    val taskId = createdTask.id

                    assignedUserIds?.forEach { userId ->
                        taskAssignmentRepository.assignUserToTask(
                            TaskAssignment(taskId = taskId, userId = userId)
                        )
                    }

                    loadTasksForTeam(currentTeam.id)

                }
            } catch (e: Exception) {
                _error.value = "Failed to create task: ${e.message}"
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

                    _teamMembers.value = users

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

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        status: String,
        priority: String,
        assignedUserIds: List<String>,
        dueDate: String?
    ) {
        val currentTeam = TeamManager.currentTeam.value ?: return
        val teamId = currentTeam?.id ?: return

        screenModelScope.launch {
            try {
                val success = taskRepository.updateTask(
                    taskId = taskId,
                    title = title,
                    description = description,
                    status = status,
                    priority = priority,
                    dueDate = dueDate
                )

                if (success) {
                    // Handle user assignments
                    val currentAssignments = taskAssignmentRepository.getAssignmentsForTask(taskId)
                    val currentUserIds = currentAssignments.map { it.userId }

                    // Remove users no longer assigned
                    val usersToRemove = currentUserIds.filter { it !in assignedUserIds }
                    usersToRemove.forEach { userId ->
                        taskAssignmentRepository.removeAssignment(taskId, userId)
                    }

                    // Add new users
                    val usersToAdd = assignedUserIds.filter { it !in currentUserIds }
                    usersToAdd.forEach { userId ->
                        taskAssignmentRepository.assignUserToTask(
                            TaskAssignment(taskId = taskId, userId = userId)
                        )
                    }

                    loadTasksForTeam(currentTeam.id)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun deleteTask(taskId: String) {
        val currentTeam = TeamManager.currentTeam.value ?: return
        val teamId = currentTeam?.id ?: return

        screenModelScope.launch {
            try {
                val success = taskRepository.deleteTask(taskId)
                if (success) {
                    loadTasksForTeam(currentTeam.id)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete task: ${e.message}"
            }
        }
    }
}