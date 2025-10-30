package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskAssignment
import com.teamschedulerapp.model.TaskWithAssignments
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.model.TeamWithMembers
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.TeamMemberRepository
import com.teamschedulerapp.repositories.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskScreenModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val taskAssignmentRepository: TaskAssignmentRepository,
) : ScreenModel {
    private val _tasksWithAssignments = MutableStateFlow<List<TaskWithAssignments>>(emptyList())
    val tasksWithAssignments: StateFlow<List<TaskWithAssignments>> = _tasksWithAssignments.asStateFlow()

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
            val tasks = taskRepository.getTasksForTeam(teamId)

            val tasksWithAssignments = tasks.mapNotNull { task ->
                val taskId = task.id
                if (taskId == null) {
                    println("Warning: Task has no ID, skipping: $task")
                    return@mapNotNull null
                }
                val assignments = taskAssignmentRepository.getAssignmentsForTask(taskId)

                val assignedMembers = assignments.mapNotNull { assignment ->
                    // Get user details
                    val user = userRepository.getUserById(assignment.userId)

                    // Check if this user is a team member and get their admin status
                    val teamMember = TeamManager.currentTeam.value?.members?.find { it.id == assignment.userId }

                    // Create TeamMemberWithUser
                    user?.let {
                        TeamMemberWithUser(
                            id = it.id,
                            firstName = it.firstName,
                            lastName = it.lastName,
                            email = it.email,
                            isAdmin = teamMember?.isAdmin ?: false
                        )
                    }
                }

                TaskWithAssignments(
                    task = task,
                    assignedMembers = assignedMembers
                )
            }
            _tasksWithAssignments.value = tasksWithAssignments
        } catch (e: Exception) {
            _error.value = "Failed to load tasks: ${e.message}"
            println("Error loading tasks: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createTask(task: Task, assignedMembers: List<TeamMemberWithUser>) {
        try {
            println("Created task with $task ")

            val createdTask = taskRepository.createTask(
                Task(
                    teamId = task.teamId,
                    title = task.title,
                    description = task.description,
                    status = task.status,
                    priority = task.priority,
                    dueDate = task.dueDate
                )
            )

            if (createdTask != null && createdTask.id != null) {
                println("Created Task: $createdTask")
                val taskId = createdTask.id

                assignedMembers.forEach { member ->
                    taskAssignmentRepository.assignUserToTask(
                        TaskAssignment(taskId = taskId, userId = member.id)
                    )
                }
                // Safe reload
                TeamManager.currentTeam.value?.id?.let { teamId ->
                    loadTasksForTeam(teamId)
                }

            } else {
                throw Exception("Failed to create task")
            }
        } catch (e: Exception) {
            println("TaskScreenModel - Error creating task: ${e.message}")
            throw e // Rethrow so UI can catch and display snackbar
        }
    }

    suspend fun updateTask(task: Task, assignedMembers: List<TeamMemberWithUser> ) {
        try {
            val taskId = task.id
            if (taskId == null) {
                _error.value = "Cannot update task without ID"
                throw Exception("Cannot update task without ID")
            }

            val success = taskRepository.updateTask(
                taskId = taskId,
                title = task.title,
                description = task.description,
                status = task.status,
                priority = task.priority,
                dueDate = task.dueDate
            )

            if (!success) {
                throw Exception("Failed to update task in database")
            }

            // Handle user assignments
            val currentAssignments = taskAssignmentRepository.getAssignmentsForTask(task.id)
            val currentUserIds = currentAssignments.map { it.userId }

            // Remove users no longer assigned
            val usersToRemove = currentUserIds
                .filter { userId -> userId !in assignedMembers
                    .map { member -> member.id } }
            usersToRemove.forEach { userId ->
                taskAssignmentRepository.removeAssignment(taskId, userId)
            }

            // Add new users
            val usersToAdd = assignedMembers
                .map { member -> member.id }
                .filter { userId -> userId !in currentUserIds }
            usersToAdd.forEach { userId ->
                taskAssignmentRepository.assignUserToTask(
                    TaskAssignment(taskId = task.id, userId = userId)
                )
            }

            // Safe reload
            TeamManager.currentTeam.value?.id?.let { teamId ->
                loadTasksForTeam(teamId)
            }
        } catch (e: Exception) {
            _error.value = "Failed to update task: ${e.message}"
            println("TaskScreenModel - Error updating task: ${e.message}")
            throw e // Rethrow so UI can catch and display snackbar            }

        }
    }

    suspend fun deleteTask(taskId: String) {
        try {
            val success = taskRepository.deleteTask(taskId)
            if (success) {
                // Safe reload
                TeamManager.currentTeam.value?.id?.let { teamId ->
                    loadTasksForTeam(teamId)
                }
            } else {
                throw Exception("Failed to delete task")
            }
        } catch (e: Exception) {
            _error.value = "Failed to delete task: ${e.message}"
            throw e // Rethrow so UI can catch and display snackbar            }
        }
    }
}