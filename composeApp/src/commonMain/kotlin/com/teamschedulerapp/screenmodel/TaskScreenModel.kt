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

    fun createTask(task: Task, assignedUserIds: List<String>) {
        screenModelScope.launch {
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

                if (createdTask != null && createdTask.id != null) {
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
            }
        }
    }

    fun updateTask(task: Task, assignedUserIds: List<String> ) {
        screenModelScope.launch {
            try {
                val success = taskRepository.updateTask(
                    taskId = task.id!!,
                    title = task.title,
                    description = task.description,
                    status = task.status,
                    priority = task.priority,
                    dueDate = task.dueDate
                )

                if (success) {
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
                }
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun deleteTask(taskId: String) {
        screenModelScope.launch {
            try {
                val success = taskRepository.deleteTask(taskId)
                if (success) {
                    loadTasksForTeam(TeamManager.currentTeam.value?.id!!)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete task: ${e.message}"
            }
        }
    }
}