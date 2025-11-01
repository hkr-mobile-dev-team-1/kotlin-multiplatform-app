package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.Availability
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AvailabilityRepository(private val postgrest: Postgrest) {

    suspend fun setAvailability(availability: Availability): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("availability").insert(availability)
            }
            true
        } catch (e: Exception) {
            println("Error setting availability: ${e.message}")
            false
        }
    }

    suspend fun getAvailabilityForUser(userId: String): List<Availability> {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("availability").select {
                    filter { eq("user_id", userId) }
                }.decodeList<Availability>()
            }
        } catch (e: Exception) {
            println("Error fetching availability: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAvailabilityForTeamOnDate(
        teamId: String,
        date: String
    ): List<Availability> = withContext(Dispatchers.IO) {
        postgrest.from("availability").select {
            filter {
                eq("team_id", teamId)
                eq("date", date)
            }
        }.decodeList<Availability>()
    }


    suspend fun deleteAvailabilityByKeys(
        userId: String,
        teamId: String,
        dateIso: String, // "YYYY-MM-DD"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            postgrest.from("availability").delete {
                filter {
                    eq("user_id", userId)
                    eq("team_id", teamId)
                    eq("date", dateIso)
                }
            }
            true
        } catch (e: Exception) {
            println("Error deleting availability: ${e.message}")
            false
        }
    }

    suspend fun updateAttendanceByKeys(
        teamId: String,
        userId: String,
        dateIso: String,
        from: String,
        to: String,
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("availability").update({
                    set("start_time", from)
                    set("end_time",   to)
                }) {
                    filter {
                        eq("team_id", teamId)
                        eq("user_id", userId)
                        eq("date",    dateIso)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Error updating availability: ${e.message}")
            false
        }
    }

    suspend fun getAvailabilityForUserOnDate(
        teamId: String,
        userId: String,
        dateIso: String
    ): Availability? = try {
        withContext(Dispatchers.IO) {
            postgrest.from("availability").select {
                filter {
                    eq("team_id", teamId)
                    eq("user_id", userId)
                    eq("date",    dateIso)
                }
                limit(1)
            }.decodeList<Availability>().firstOrNull()
        }
    } catch (e: Exception) {
        println("Error fetching availability (one): ${e.message}")
        null
    }
}