package com.teamschedulerapp.model

data class User(
    val name: String,
    val email: String,
    val role: String,
    val profileImageUrl: String? = null
)
