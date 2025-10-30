package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.TeamMember
import com.teamschedulerapp.model.User
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TeamMemberRepository(
    private val postgrest: Postgrest,
    private val userRepository: UserRepository
) {

    suspend fun addMemberToTeam( teamId: String, userId: String, isAdmin: Boolean = false): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members").insert(
                    mapOf(
                        "team_id" to teamId,
                        "user_id" to userId,
                        "is_admin" to isAdmin
                    ))
            }
            true
        } catch (e: Exception) {
            println("Error adding member: ${e.message}")
            false
        }
    }

    // Idea will be that the invitation link will parse the token with encoded teamId
    // and the invited users id to then be calling the function below
    suspend fun acceptTeamInvitation(teamId: String, userId: String): Boolean{
        return addMemberToTeam(teamId,userId)
    }
    suspend fun getMembersForTeam(teamId: String): List<TeamMember> {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members")
                    .select {
                        filter { eq("team_id", teamId) }
                }.decodeList<TeamMember>()
            }
        } catch (e: Exception) {
            println("Error fetching members: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTeamWithUserInfo(teamId: String): List<User> {
        val teamMembers = getMembersForTeam(teamId)
        val users = mutableListOf<User>()

        for(member in teamMembers) {
            val user = userRepository.getUserById(member.userId)
            user?.let { users.add(it) }
        }

        return users
    }

    suspend fun setMemberAdmin(teamId: String, userId: String, isAdmin: Boolean): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members").update({
                    set("is_admin", isAdmin)
                }) {
                    filter {
                        eq("team_id", teamId)
                        eq("user_id", userId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Error updating admin status: ${e.message}")
            false
        }
    }

    suspend fun removeMember(teamId: String, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members").delete {
                    filter {
                        eq("team_id", teamId)
                        eq("user_id", userId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Error removing member: ${e.message}")
            false
        }
    }
}