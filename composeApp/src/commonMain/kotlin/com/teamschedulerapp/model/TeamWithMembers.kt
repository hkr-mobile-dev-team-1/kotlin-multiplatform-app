package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamWithMembers(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    val members: List<TeamMemberWithUser> = emptyList()
)