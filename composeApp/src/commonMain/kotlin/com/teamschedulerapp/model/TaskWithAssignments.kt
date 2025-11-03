package com.teamschedulerapp.model

import kotlinx.serialization.SerialName

/**
 * Combines task information with their assigned members.
 * This provides all task details (teamId, title, desctiption, createdBy, etc)
 * along with the list of users who are members of the team
 * and whether they are have admin role.
 */
data class TaskWithAssignments(
    val id: String,
    @SerialName("team_id")val teamId: String,
    val title: String,
    val description: String? = null,
    @SerialName("start_date")val startDate: String? = null,
    @SerialName("end_date")val endDate: String? = null,
    @SerialName("created_by")val createdBy: String? = null,
    val status: String,
    val priority: String,
    val assignedMembers: List<TeamMemberWithUser>
)
