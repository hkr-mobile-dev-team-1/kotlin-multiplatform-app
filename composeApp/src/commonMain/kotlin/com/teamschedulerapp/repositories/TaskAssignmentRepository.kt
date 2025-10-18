package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskAssignment
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TaskAssignmentRepository(private val postgrest: Postgrest) {

    suspend fun assignUserToTask(assignment: TaskAssignment): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("task_assignments").insert(assignment)
            }
            true
        } catch (e: Exception) {
            println("Error assigning user: ${e.message}")
            false
        }
    }

    suspend fun getAssignmentsForTask(taskId: String): List<TaskAssignment> {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("task_assignments").select {
                    filter { eq("task_id", taskId) }
                }.decodeList<TaskAssignment>()
            }
        } catch (e: Exception) {
            println("Error fetching assignments: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAssignedTasksForUser(userId: String): List<TaskAssignment> {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("task_assignments").select {
                    filter { eq("user_id", userId) }
                }.decodeList<TaskAssignment>()
            }
        } catch (e: Exception) {
            println("Error fetching user tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun removeAssignment(taskId: String, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest.from("task_assignments").delete {
                    filter {
                        eq("task_id", taskId)
                        eq("user_id", userId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("Error removing assignment: ${e.message}")
            false
        }
    }

}