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
}