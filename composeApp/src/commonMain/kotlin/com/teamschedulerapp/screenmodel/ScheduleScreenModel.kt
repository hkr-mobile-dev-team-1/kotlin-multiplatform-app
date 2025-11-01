package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.repositories.AvailabilityRepository
import com.teamschedulerapp.domain.toAvailability
import com.teamschedulerapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.teamschedulerapp.domain.toAttendee
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.repositories.UserRepository
import kotlinx.datetime.*

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


class ScheduleScreenModel(
    private val availabilityRepository: AvailabilityRepository,
    private val userRepository: UserRepository,
    private val userId: String,
) : ScreenModel {

    // emit UI events for snackbars reactions
    sealed class UiEvent {
        data class Success(val msg: String) : UiEvent()
        data class Error(val msg: String) : UiEvent()
    }

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

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

    // headcounts
    private val _headcounts = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val headcounts = _headcounts.asStateFlow()

    // headcount load
    fun loadHeadcountsForMonth(teamId: String, ym: YearMonth) {
        screenModelScope.launch {
            try {
                _isLoading.value = true
                val start = LocalDate(ym.year, ym.month, 1)
                // compute the end (next month - one day)
                val end = start.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

                val data = availabilityRepository.getHeadcountsForRange(teamId, start, end)
                _headcounts.value = data
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
        teamMembers: List<TeamMemberWithUser>
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
                // 2) map team members using TeamMemberWithUser
                val nameMap: Map<String, String> = teamMembers.associate { m ->
                    val full = listOfNotNull(m.firstName, m.lastName)
                        .joinToString(" ")
                        .ifBlank { m.email ?: "Member" }
                    m.id to full
                }
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

    // handles both save and edit
    fun saveAttendance(
        teamId: String,
        date: LocalDate,
        attendee: Attendee,
        teamMembers: List<TeamMemberWithUser>,
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
                loadDay(teamId, date, teamMembers)
                // load headcount
                _headcounts.value = _headcounts.value.toMutableMap().apply {
                    this[date] = (this[date] ?: 0) + (if (exists == null) 1 else 0)
                }
                onDone()
                _uiEvents.tryEmit(UiEvent.Success("Attendance saved"))
            } catch (e: Exception) {
                _error.value = e.message
                _uiEvents.tryEmit(UiEvent.Error("Failed to save attendance"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAttendance(teamId: String, userId: String, date: LocalDate, teamMembers: List<TeamMemberWithUser>) {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val ok = availabilityRepository.deleteAvailabilityByKeys(
                    userId = userId,
                    teamId = teamId,
                    dateIso = date.toString()
                )
                if (ok) {
                    // reload
                    loadDay(teamId, date, teamMembers)
                    // recompute headcount
                    _headcounts.value = _headcounts.value.toMutableMap().apply {
                        this[date] = (this[date] ?: 1).coerceAtLeast(1) - 1
                        _uiEvents.tryEmit(UiEvent.Success("Attendance deleted"))
                    }
                } else {
                    _error.value = "Failed to delete attendance"
                    _uiEvents.tryEmit(UiEvent.Error("Failed to delete attendance"))
                }
            } catch (e: Exception) {
                _error.value = e.message
                _uiEvents.tryEmit(UiEvent.Error("Failed to delete attendance"))
            } finally {
                _isLoading.value = false
            }
        }
    }


    // optional helper (keeping for now)
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