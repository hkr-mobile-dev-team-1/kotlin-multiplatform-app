package com.teamschedulerapp

import com.teamschedulerapp.data.SupabaseClientManager
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

fun testSupabaseInit(): String {
    return try {
        val client = SupabaseClientManager.client
        "✅ Supabase client initialized successfully!"
    } catch (e: Exception) {
        "❌ Failed to initialize client: ${e.message}"
    }
}