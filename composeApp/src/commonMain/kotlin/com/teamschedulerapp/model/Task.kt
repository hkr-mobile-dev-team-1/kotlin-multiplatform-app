package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    @SerialName("team_id")val teamId: String,
    val title: String,
    val description: String? = null,
    @SerialName("due_date")val dueDate: String? = null,
    @SerialName("created_by")val createdBy: String? = null,
    val status: String = "open",
    val priority: String = "medium",
)
