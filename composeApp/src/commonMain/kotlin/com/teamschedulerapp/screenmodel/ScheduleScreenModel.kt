package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.repositories.AvailabilityRepository
import com.teamschedulerapp.domain.toAvailability
import com.teamschedulerapp.model.User
import com.teamschedulerapp.navigation.TeamManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.teamschedulerapp.domain.toAttendee
import com.teamschedulerapp.repositories.UserRepository

class ScheduleScreenModel(
    private val availabilityRepository: AvailabilityRepository,
    private val userRepository: UserRepository,
    private val userId: String,
) : ScreenModel {

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    // holds (Attendee, ownerId)
    private val _attendeesForDay = MutableStateFlow<List<Pair<Attendee, String>>>(emptyList())
    val attendeesForDay = _attendeesForDay.asStateFlow()

    fun submitAttendance(
        teamId: String,
        date: LocalDate,
        attendee: Attendee
    ) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false
            try {
                val availability = attendee.toAvailability(userId, teamId, date)
                val ok = availabilityRepository.upsertAvailability(availability)
                if (ok) {
                    _success.value = true
                    println("Attendance saved successfully")
                } else {
                    _error.value = "Failed to save attendance"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // load
    fun loadDay(
        teamId: String,
        date: LocalDate,
        teamMembers: List<User> = emptyList()
    ) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // 1) fetch rows for the team+date
                val rows = availabilityRepository.getAvailabilityForTeamOnDate(
                    teamId = teamId,
                    date   = date.toString()
                )
                // 2) name map from TeamManager’s members (fallback to “Member”)
                // from teamMembers
                val baseMap: Map<String, String> = teamMembers
                    .mapNotNull { u -> u.id?.let { it to displayNameOf(u) } }
                    .toMap()

                // resolve missing IDs
                val missingIds = rows.map { it.userId }.distinct().filter { it !in baseMap }
                val fetchedMap: Map<String, String> = missingIds.associateWith { id ->
                    val u = userRepository.getUserById(id)
                    if (u != null) displayNameOf(u) else "Member"
                }

                val nameMap = baseMap + fetchedMap

                // 3) map to UI: Pair(Attendee, ownerId)
                val list = rows.map { row ->
                    val display = nameMap[row.userId] ?: "Member"
                    val attendee = row.toAttendee(displayName = display)
                    attendee to row.userId
                }

                _attendeesForDay.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAttendance(
        teamId: String,
        date: LocalDate,
        attendee: Attendee,
        onDone: () -> Unit
    ) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false
            try {
                val row = attendee.toAvailability(userId, teamId, date)
                val exists = availabilityRepository
                    .getAvailabilityForUserOnDate(teamId, userId, row.date)

                val ok = if (exists == null) {
                    availabilityRepository.setAvailability(row) != null
                } else {
                    availabilityRepository.updateAttendanceByKeys(
                        teamId = teamId,
                        userId = userId,
                        dateIso = row.date,
                        from = row.startTime ?: "",
                        to = row.endTime ?: "",
                    )
                }

                if (!ok) throw IllegalStateException("Save failed")

                _success.value = true
                // reload to reflect the changes
                loadDay(teamId, date, teamMembers = emptyList())
                onDone()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun displayNameOf(u: User): String {
        fun String?.clean() = this?.trim().takeUnless { it.isNullOrBlank() }
        val first = u.firstName.clean()
        val last  = u.lastName.clean()
        val full  = listOfNotNull(first, last).joinToString(" ").trim()
        return when {
            full.isNotBlank() -> full
            !u.email.isNullOrBlank() -> u.email!!
            else -> "Member"
        }
    }

}