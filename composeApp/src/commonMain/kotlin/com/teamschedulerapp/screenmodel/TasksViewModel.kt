package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskWithUsers
import com.teamschedulerapp.model.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TasksScreenModel : ScreenModel {
    private val _tasksWithUsers = MutableStateFlow<List<TaskWithUsers>>(emptyList())
    val tasksWithUsers: StateFlow<List<TaskWithUsers>> = _tasksWithUsers.asStateFlow()

    init {
        loadTasksWithUsers()
    }

    private fun loadTasksWithUsers() {
        _tasksWithUsers.update { tasksWithUsers -> listOf(
            TaskWithUsers(
                task = Task(
                    id = "1",
                    teamId = "1",
                    title = "Design login page",
                    description = "Create a responsive login screen with email and password validation.",
                    status = "In progress",
                    priority = "Low",
                    dueDate = "2025-10-07"
                ),
                assignedUsers = emptyList()
            ),
            TaskWithUsers(
                task = Task(
                    id = "2",
                    teamId = "1",
                    title = "Fix API bug",
                    description = "Resolve the issue causing incorrect user data to load.",
                    status = "Pending",
                    priority = "High",
                    dueDate = "2025-10-15"
                ),
                assignedUsers = listOf(
                    User(
                        id = "1",
                        firstName = "Elina",
                        lastName = "Rosato",
                        email = "elinarosato@gmail.com"
                    ),
                    User(
                        id = "2",
                        firstName = "Dimple",
                        lastName = "Narkhede",
                        email = "dimplenarkhede@gmail.com"
                    ),
                    User(
                        id = "3",
                        firstName = "Dario",
                        lastName = "Ostojic",
                        email = "darioostojic2@gmail.com"
                    ),
                    User(
                        id = "4",
                        firstName = "Andre",
                        lastName = "Sandblom",
                        email = "andresandblom@gmail.com"
                    ),
                    User(
                        id = "5",
                        firstName = "Kate",
                        lastName = "Arvay",
                        email = "katearvay@gmail.com"
                    )
                )
            ),
            TaskWithUsers(
                task = Task(
                    id = "3",
                    teamId = "1",
                    title = "Implement schedule view",
                    description = "Develop the calendar UI and connect it to the schedule data.",
                    status = "Done",
                    priority = "medium",
                    dueDate = "2025-10-15"
                ),
                assignedUsers = listOf(
                    User(
                        id = "1",
                        firstName = "Elina",
                        lastName = "Rosato",
                        email = "elinarosato@gmail.com"
                    )
                )
            ),
            TaskWithUsers(
                task = Task(
                    id = "4",
                    teamId = "1",
                    title = "Add authentication flow",
                    description = "Integrate Supabase authentication for login and sign-up.",
                    status = "Blocked",
                    priority = "Low",
                    dueDate = "2025-10-15"
                ),
                assignedUsers = listOf(
                    User(
                        id = "3",
                        firstName = "Dario",
                        lastName = "Ostojic",
                        email = "darioostojic@gmail.com"
                    ),
                    User(
                        id = "4",
                        firstName = "Andre",
                        lastName = "Sandblom",
                        email = "andresandblom@gmail.com"
                    ),
                )
            )
        )}
    }
}
