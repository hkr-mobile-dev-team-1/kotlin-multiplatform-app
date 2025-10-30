package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Combines user information with their team membership status.
 * This provides all user details (firstName, lastName, email) along with
 * whether they are an admin of the team.
 */
@Serializable
data class TeamMemberWithUser(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String,
    @SerialName("is_admin") val isAdmin: Boolean = false,
)
