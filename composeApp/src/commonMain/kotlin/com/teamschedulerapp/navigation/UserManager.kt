/**
 * UserManager.kt
 *
 * Manages the current authenticated user's state globally across the application.
 *
 * Provides easy access to the current user's ID and profile information.
 * Observes authentication state changes and automatically updates.
 */
package com.teamschedulerapp.navigation

import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.User
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserManager {
    private val supabase = SupabaseClientManager.client

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Initialize user state from Supabase auth
     * Call this when app starts or user logs in
     */
    suspend fun initialize(authRepository: com.teamschedulerapp.data.AuthRepository) {
        val authUser = supabase.auth.currentUserOrNull()
        _currentUserId.value = authUser?.id

        // Optionally fetch full user profile
        authUser?.id?.let { userId ->
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    /**
     * Clear user state on logout
     */
    fun clear() {
        _currentUserId.value = null
        _currentUser.value = null
    }

    /**
     * Get current user ID, throws if not authenticated
     */
    fun requireUserId(): String {
        return _currentUserId.value
            ?: throw IllegalStateException("User not authenticated")
    }
}