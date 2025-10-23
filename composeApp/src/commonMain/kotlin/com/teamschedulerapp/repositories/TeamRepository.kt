package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.Team
import com.teamschedulerapp.model.TeamMember
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TeamRepository(private val postgrest: Postgrest) {
    suspend fun createTeam(team: Team): Team? {
        return try {
            withContext(Dispatchers.IO) {
                postgrest
                    .from("teams")
                    .insert(team){
                        select()
                    }.decodeSingle<Team>()
            }
        } catch(e: Exception) {
            println("Error creating team: ${e.message}")
            null
        }
    }

    suspend fun getTeamById(teamId: String): Team? {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("teams")
                    .select {
                    filter {
                        eq("id", teamId)
                    }
                }.decodeSingle<Team>()
            }
        } catch (e: Exception) {
            println("Error fetching team: ${e.message}")
            null
        }
    }

    suspend fun getTeamsForUser(userId: String): List<Team> {
        return try {
            withContext(Dispatchers.IO) {
                val teamMembers = postgrest.from("team_members")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<TeamMember>()
                println("Found ${teamMembers.size} team memberships for user $userId")


                if (teamMembers.isEmpty()) {
                    emptyList()
                } else {
                    val teamIds = teamMembers.map { it.teamId }
                    println("Fetching teams with IDs: $teamIds")

                    postgrest.from("teams")
                        .select {
                            filter {
                                isIn("id", teamIds)
                            }
                        }.decodeList<Team>()
                }
            }
        } catch (e: Exception) {
            println("Error fetching teams for user: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateTeam(teamId: String, name: String?, description: String?): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("teams")
                    .update({
                    name?.let { set("name", it) }
                    description?.let { set("description", it) }
                }) {
                    filter {
                        eq("id", teamId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Error updating team: ${e.message}")
            false
        }
    }

    suspend fun deleteTeam(teamId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("teams").delete {
                    filter {
                        eq("id", teamId) }
                }
            }
            true
        } catch (e: Exception) {
            println("Error deleting team: ${e.message}")
            false
        }
    }
}