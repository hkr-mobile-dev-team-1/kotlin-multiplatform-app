package com.teamschedulerapp.domain

import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.model.Availability
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun Availability.toAttendee(displayName: String): Attendee = Attendee(
    displayName = displayName,
    from = this.startTime?.let(::parseHmOrNull),
    to = this.endTime?.let(::parseHmOrNull)
)

fun Attendee.toAvailability(
    userId: String,
    teamId: String,
    date: LocalDate
): Availability {
    return Availability(
        id = "",
        userId = userId,
        teamId = teamId,
        date = date.toString(),
        startTime = from?.let {
            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
        },
        endTime = to?.let {
            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
        },
        note = null,
        status = "available"
    )
}

private fun parseHmOrNull(text: String?): LocalTime? {
    if (text.isNullOrBlank()) return null
    return try {
        val (h, m) = text.split(":").map { it.toInt() }
        LocalTime(h, m)
    } catch (_: Exception) {
        null
    }
}