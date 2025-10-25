package com.teamschedulerapp.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.repositories.AvailabilityRepository
import com.teamschedulerapp.domain.toAvailability
import com.teamschedulerapp.navigation.TeamManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ScheduleScreenModel(
    private val availabilityRepository: AvailabilityRepository,
    private val userId: String,
) : ScreenModel {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

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
}