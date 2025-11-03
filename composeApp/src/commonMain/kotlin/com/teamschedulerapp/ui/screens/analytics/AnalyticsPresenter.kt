package com.teamschedulerapp.ui.screens.analytics

import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.Task
import io.github.jan.supabase.postgrest.from

data class TasksPerUser(val userName: String, val count: Int)
data class KeyCount(val key: String, val count: Int)

class AnalyticsPresenter {

    private val client = SupabaseClientManager.client

    /**
     * Fetch tasks from Supabase, or return mock data if Postgrest is unavailable or fails.
     */
    suspend fun fetchTasks(): List<Task> {
        return try {
            // Access Postgrest safely through the `from()` extension
            client.from("tasks").select().decodeList<Task>()
        } catch (e: Exception) {
            // Fallback mock data so analytics still renders
            listOf(
                Task(id = "1", teamId = "team-1", title = "Mock task A", description = "Fallback task", startDate = null, createdBy = "user-1", status = "open", priority = "high"),
                Task(id = "2", teamId = "team-1", title = "Mock task B", description = "Fallback task", startDate = null, createdBy = "user-2", status = "done", priority = "low"),
                Task(id = "3", teamId = "team-2", title = "Mock task C", description = "Fallback task", startDate = null, createdBy = "user-1", status = "in_progress", priority = "medium")
            )
        }
    }

    suspend fun countByStatus(): List<KeyCount> {
        val tasks = fetchTasks()
        return tasks.groupingBy { it.status }.eachCount()
            .map { KeyCount(it.key, it.value) }
            .sortedByDescending { it.count }
    }

    suspend fun countByPriority(): List<KeyCount> {
        val tasks = fetchTasks()
        return tasks.groupingBy { it.priority }.eachCount()
            .map { KeyCount(it.key, it.value) }
            .sortedByDescending { it.count }
    }
    suspend fun countByUser(): List<TasksPerUser> {
        val tasks = fetchTasks()
        val counts = tasks.groupingBy { it.createdBy ?: "Unknown" }.eachCount()
        return counts.map { TasksPerUser(it.key, it.value) }.sortedByDescending { it.count }
    }

}
