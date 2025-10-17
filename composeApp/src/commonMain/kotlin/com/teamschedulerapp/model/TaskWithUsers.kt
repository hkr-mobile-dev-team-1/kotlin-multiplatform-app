package com.teamschedulerapp.model

data class TaskWithUsers(
    val task: Task,
    val assignedUsers: List<User>
)
