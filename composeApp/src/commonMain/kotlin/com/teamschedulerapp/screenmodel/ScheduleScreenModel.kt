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

class ScheduleScreenModel(
    private val availabilityRepository: AvailabilityRepository,
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
                val nameMap: Map<String, String> = teamMembers
                    .mapNotNull { u ->
                        val id = u.id ?: return@mapNotNull null
                        val full = listOfNotNull(u.firstName, u.lastName)
                            .joinToString(" ")
                            .ifBlank { u.email ?: "Member" }
                        id to full
                    }.toMap()

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
}