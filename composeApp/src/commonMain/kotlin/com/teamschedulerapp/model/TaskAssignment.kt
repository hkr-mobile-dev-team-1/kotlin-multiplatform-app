package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskAssignment(
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("assignedAt") val assignedAt: String? = null
)
