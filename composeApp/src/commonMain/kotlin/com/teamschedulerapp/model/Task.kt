package com.teamschedulerapp.model

import kotlinx.datetime.LocalDate

data class Task(
    val id: Number,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority? = null,
    val status: TaskStatus? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class User(
    val userId: Number,
    val userName: String
)

data class TaskWithUsers(
    val task: Task,
    val assignedUsers: List<User>
)
