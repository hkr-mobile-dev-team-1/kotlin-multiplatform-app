package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes specification from table Tasks
 */
@Serializable
data class Task(
    val id: String? = null,
    @SerialName("team_id")val teamId: String,
    val title: String,
    val description: String? = null,
    @SerialName("start_date")val startDate: String? = null,
    @SerialName("end_date")val endDate: String? = null,
    @SerialName("created_by")val createdBy: String? = null,
    val status: String,
    val priority: String,
)
