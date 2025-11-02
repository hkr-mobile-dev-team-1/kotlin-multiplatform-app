package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Combines team information with their team members.
 * This provides all team details (name, description, createdBy) along with the
 * list of users who are members of the team and whether they are have admin role.
 */
@Serializable
data class TeamWithMembers(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    val members: List<TeamMemberWithUser> = emptyList()
)