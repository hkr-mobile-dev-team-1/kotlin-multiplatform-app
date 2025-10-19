package com.teamschedulerapp.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(private val supabase: SupabaseClient) {

    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {

        try {
            // Step 1: Sign up the user. On success, this returns the new user object.
            val newUser = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // Step 2: Try to get the current user after sign-up
            val currentUser = supabase.auth.currentUserOrNull()

            // Step 3: If no user is found, stop and return an error
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not returned"))
            }

            // Step 4: Get the user's ID which will act as a foreign key in Step 5
            val userId = currentUser.id

            // Step 5: Build the profile data as JSON
            val userProfile = buildJsonObject {
                put("id", userId)
                put("first_name", firstName)
                put("last_name", lastName)
            }

            // Step 6: Insert the profile into the "users" table in the database
            supabase.postgrest
                .from("users")
                .insert(userProfile)

            // Step 7: If everything worked, return success
            return@withContext Result.success(Unit)

        } catch (error: Exception) {
            // If something goes wrong (e.g. network error), return a failure
            return@withContext Result.failure(error)
        }
    }


    suspend fun loginUser(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
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
