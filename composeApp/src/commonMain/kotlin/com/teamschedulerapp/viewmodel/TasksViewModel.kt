package com.teamschedulerapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskPriority
import com.teamschedulerapp.model.TaskStatus
import com.teamschedulerapp.model.TaskWithUsers
import com.teamschedulerapp.model.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class TasksViewModel : ViewModel() {
    private val _tasksWithUsers = MutableStateFlow<List<TaskWithUsers>>(emptyList())
    val tasksWithUsers: StateFlow<List<TaskWithUsers>> = _tasksWithUsers.asStateFlow()

    init {
        loadTasksWithUsers()
    }

    private fun loadTasksWithUsers() {
        _tasksWithUsers.update { taskWithUsers -> listOf(
            TaskWithUsers(
                task = Task(
                    id = 1,
                    title = "Design login page",
                    description = "Create a responsive login screen with email and password validation.",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.LOW,
                    startDate = LocalDate.parse("2025-10-02"),
                    endDate = LocalDate.parse("2025-10-07")
                ),
                assignedUsers = emptyList()
            ),
            TaskWithUsers(
                task = Task(
                    id = 1,
                    title = "Fix API bug",
                    description = "Resolve the issue causing incorrect user data to load.",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.HIGH,
                    startDate = LocalDate.parse("2025-10-09"),
                    endDate = LocalDate.parse("2025-10-15")
                ),
                assignedUsers = listOf(
                    User(
                        userId = 1,
                        userName = "Elina"
                    ),
                    User(
                        userId = 1,
                        userName = "Dimple"
                    )
                )
            ),
            TaskWithUsers(
                task = Task(
                    id = 1,
                    title = "Implement schedule view",
                    description = "Develop the calendar UI and connect it to the schedule data.",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.MODERATE,
                    startDate = LocalDate.parse("2025-10-09"),
                    endDate = LocalDate.parse("2025-10-15")
                ),
                assignedUsers = listOf(
                    User(
                        userId = 1,
                        userName = "Elina"
                    )
                )
            ),
            TaskWithUsers(
                task = Task(
                    id = 1,
                    title = "Add authentication flow",
                    description = "Integrate Supabase authentication for login and sign-up.",
                    status = TaskStatus.BLOCKED,
                    priority = TaskPriority.TOP,
                    startDate = LocalDate.parse("2025-10-09"),
                    endDate = LocalDate.parse("2025-10-15")
                ),
                assignedUsers = listOf(
                    User(
                        userId = 1,
                        userName = "Dario"
                    ),
                    User(
                        userId = 1,
                        userName = "Andre"
                    )
                )
            )
        )}
    }
}
