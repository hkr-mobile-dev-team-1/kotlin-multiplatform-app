package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.TeamMember
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TeamMemberRepository(private val postgrest: Postgrest) {

    suspend fun addMemberToTeam(member: TeamMember): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members").insert(member)
            }
            true
        } catch (e: Exception) {
            println("Error adding member: ${e.message}")
            false
        }
    }

    suspend fun getMembersForTeam(teamId: String): List<TeamMember> {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("team_members").select {
                    filter { eq("team_id", teamId) }
                }.decodeList<TeamMember>()
            }
        } catch (e: Exception) {
            println("Error fetching members: ${e.message}")
            emptyList()
        }
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