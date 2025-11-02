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
import com.teamschedulerapp.repositories.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
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
     * This is called when app starts or user logs in / signs up
     */
    suspend fun initialize() {
        val userRepository = UserRepository(supabase.postgrest)
        val authUser = supabase.auth.currentUserOrNull()
        _currentUserId.value = authUser?.id

        authUser?.id?.let { userId ->
            _currentUser.value = userRepository.getUserById(userId)
        }

        println("UserManager - Initialized with user: ${_currentUser.value?.firstName} ${_currentUser.value?.lastName}")
    }

    /**
     * Clear user state on logout
     */
    fun clear() {
        _currentUserId.value = null
        _currentUser.value = null
        // Also reset TeamManager
        TeamManager.reset()
        println("UserManager - Cleared")
    }

    /**
     * Get current user ID, throws if not authenticated
     */
    fun getCurrentUserId(): String {
        return _currentUserId.value
            ?: throw IllegalStateException("User not authenticated")
    }

    /**
     * Get current user, throws if not authenticated
     */
    fun getCurrentUser(): User {
        return _currentUser.value
            ?: throw IllegalStateException("User not authenticated")
    }
}