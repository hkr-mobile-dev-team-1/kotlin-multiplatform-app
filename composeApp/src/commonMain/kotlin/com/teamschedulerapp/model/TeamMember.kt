package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamMember(
    @SerialName("user_id") val userId: String,
    @SerialName("team_id") val teamId: String,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("joined_at") val joinedAt: String? = null
)
