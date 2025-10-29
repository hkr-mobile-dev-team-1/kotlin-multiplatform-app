package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskAssignment
import com.teamschedulerapp.model.TaskWithUsers
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.repositories.TaskAssignmentRepository
import com.teamschedulerapp.repositories.TaskRepository
import com.teamschedulerapp.repositories.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskScreenModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val taskAssignmentRepository: TaskAssignmentRepository,
) : ScreenModel {
    private val _tasksWithUsers = MutableStateFlow<List<TaskWithUsers>>(emptyList())
    val tasksWithUsers: StateFlow<List<TaskWithUsers>> = _tasksWithUsers.asStateFlow()

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

            val tasksWithUsers = tasks.mapNotNull { task ->
                val taskId = task.id
                if (taskId == null) {
                    println("Warning: Task has no ID, skipping: $task")
                    return@mapNotNull null
                }
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

    suspend fun createTask(task: Task, assignedUserIds: List<String>) {
        try {
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

            println("Task to create: $createdTask")

            if (createdTask == null) {
                throw Exception("Failed to create task in database")
            } else if (createdTask.id != null) {
                println("Created Task: $createdTask")
                val taskId = createdTask.id

                assignedUserIds.forEach { userId ->
                    taskAssignmentRepository.assignUserToTask(
                        TaskAssignment(taskId = taskId, userId = userId)
                    )
                }

                loadTasksForTeam(TeamManager.currentTeam.value?.id!!)
            }
        } catch (e: Exception) {
            _error.value = "Failed to create task: ${e.message}"
            throw e  // Re-throw so TasksScreen can catch it and display snackbar
        }
    }

    suspend fun updateTask(task: Task, assignedUserIds: List<String> ) {
        try {
            val success = taskRepository.updateTask(
                taskId = task.id!!,
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
            val usersToRemove = currentUserIds.filter { it !in assignedUserIds }
            usersToRemove.forEach { userId ->
                taskAssignmentRepository.removeAssignment(task.id, userId)
            }

            // Add new users
            val usersToAdd = assignedUserIds.filter { it !in currentUserIds }
            usersToAdd.forEach { userId ->
                taskAssignmentRepository.assignUserToTask(
                    TaskAssignment(taskId = task.id, userId = userId)
                )
            }

            loadTasksForTeam(TeamManager.currentTeam.value?.id!!)
        } catch (e: Exception) {
            _error.value = "Failed to update task: ${e.message}"
            throw e  // Re-throw so TasksScreen can catch it and display snackbar
        }
    }

    suspend fun deleteTask(taskId: String) {
        try {
            val success = taskRepository.deleteTask(taskId)
            if (!success) {
                throw Exception("Failed to delete task from database")
            }
            loadTasksForTeam(TeamManager.currentTeam.value?.id!!)
        } catch (e: Exception) {
            _error.value = "Failed to delete task: ${e.message}"
            throw e  // Re-throw so TasksScreen can catch it and display snackbar
        }
    }
}