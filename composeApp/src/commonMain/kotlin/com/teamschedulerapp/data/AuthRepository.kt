package com.teamschedulerapp.data

import com.teamschedulerapp.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class AuthRepository(private val supabase: SupabaseClient) {

    /* The 'registerUser' function creates a new user in the private 'auth.users' table.
       We use that user's unique ID as a foreign key in our own 'users' table to create
       a corresponding public user with first/last name in that 'users' table. */
    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Run the code block below on a background thread so the network operations don't block the UI responsiveness.
        try {
            // Sign up a user using Supabase's Email provider (a built-in object for email/password auth).
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = supabase.auth.currentUserOrNull()

            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not returned"))
            }

            // Get the user's ID from internal Supabase 'auth.users' table which will act
            // as a foreign key in User object
            val userId = currentUser.id

            // Build the user data as a User object (saved under userProfile variable)
            val userProfile = User(
                id = userId,
                firstName = firstName,
                lastName = lastName,
                email = email
            )

            // Insert the "userProfile" object into the "users" table in the database
            supabase.postgrest
                .from("users")
                .insert(userProfile)

            return@withContext Result.success(Unit)

        } catch (error: Exception) {
            return@withContext Result.failure(error)
        }
    }


    suspend fun loginUser(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try { // Login a user using Supabase's Email provider
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
