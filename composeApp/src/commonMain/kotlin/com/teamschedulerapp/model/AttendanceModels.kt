package com.teamschedulerapp.model

import kotlinx.datetime.LocalTime

data class Attendee(
    val displayName: String,
    val from: LocalTime? = null,
    val to: LocalTime? = null
)

fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }