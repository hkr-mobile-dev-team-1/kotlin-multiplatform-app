package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
)
