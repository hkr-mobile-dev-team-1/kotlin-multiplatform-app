package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.User
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserRepository(private val postgrest: Postgrest) {

    suspend fun createUser(user: User): Boolean {
        return try{
            withContext(Dispatchers.IO) {
                postgrest
                    .from("users")
                    .insert(user)
            }
            true
        }catch (e: Exception) {
            println("Error creating user: ${e.message}")
            false
        }
    }

    suspend fun getUserById(userId: String): User?{
        return try {
            withContext(Dispatchers.IO){
                postgrest
                    .from("users")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingle<User>()
            }
        }catch (e: Exception){
            println("Error getting user: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email:String): String?{
        return try {
            withContext(Dispatchers.IO){
                val result = postgrest.rpc("get_user_id_by_email", mapOf("email" to email))
                result.decodeSingleOrNull<String>()
            }
        }catch (e: Exception) {
            println("Error fetching users email: ${e.message}")
            null
        }
    }

    suspend fun updateUser(userId: String, firstName: String?, lastName: String?, email: String?): Boolean{
        return try {
            withContext(Dispatchers.IO){
                postgrest
                    .from("users")
                    .update({
                        firstName?.let { set("first_name", it) }
                        lastName?.let { set("last_name", it) }
                        email?.let{ set("email", it) }
                    }) {
                        filter{
                            eq("id",userId)
                        }
                    }
            }
            true
        }catch(e: Exception){
            println("Error updating user: ${e.message}")
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest
                    .from("users")
                    .delete{
                        filter {
                            eq("id",userId)
                        }
                    }
            }
            true
        }catch (e:Exception) {
            println("Error deleting user: ${e.message}")
            false
        }
    }
}