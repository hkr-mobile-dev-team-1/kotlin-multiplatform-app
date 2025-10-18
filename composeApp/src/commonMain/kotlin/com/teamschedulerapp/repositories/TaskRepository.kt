package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.Task
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TaskRepository(private val postgrest: Postgrest) {

    suspend fun createTask(task: Task): Boolean{
        return try {
            withContext(Dispatchers.IO){
                postgrest
                    .from("tasks")
                    .insert(task)
            }
            true
        }catch (e: Exception){
            println("Error creating task: ${e.message}")
            false
        }
    }

    suspend fun getTaskForTeam(teamId: String): List<Task>{
        return try {
            withContext(Dispatchers.IO){
                postgrest
                    .from("tasks")
                    .select{
                        filter {
                            eq("team_id", teamId)
                        }
                    }.decodeList<Task>()
            }
        } catch (e: Exception){
            println("Error fetching tasks: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTaskById(taskId: String): Task? {
        return try {
            withContext(Dispatchers.IO){
                postgrest
                    .from("tasks")
                    .select {
                        filter {
                            eq("id", taskId)
                        }
                    }.decodeSingle<Task>()
            }
        }catch (e: Exception){
            println("Error fetching task: ${e.message}")
            null
        }
    }

    suspend fun updateTask(taskId: String,
                           title: String?,
                           description: String?,
                           status: String?,
                           priority: String?,
                           dueDate: String?): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                postgrest
                    .from("tasks")
                    .update({
                        title?.let { set("title", it) }
                        description?.let{ set("description", it) }
                        status?.let { set("status", it) }
                        priority?.let{ set("priority",it) }
                        dueDate?.let { set("due_date", it) }
                    }) {
                        filter {
                            eq("id",taskId)
                        }
                    }
            }
            true
        }catch (e:Exception) {
            println("Error updating task: ${e.message}")
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean{
        return try {
            withContext(Dispatchers.IO) {
                postgrest
                    .from("tasks")
                    .delete{
                        filter {
                            eq("id",taskId)
                        }
                    }
            }
            true
        }catch(e: Exception) {
            println("Error deleting task: ${e.message}")
            false
        }
    }

}