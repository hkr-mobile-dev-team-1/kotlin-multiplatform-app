package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Availability(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("team_id") val teamId: String,
    val date: String, // Maybe be Localdate??
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val note: String? = null,
    val status: String = "available"
)
