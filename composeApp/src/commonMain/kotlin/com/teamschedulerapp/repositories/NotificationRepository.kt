package com.teamschedulerapp.repositories

import com.teamschedulerapp.model.Notification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive

data class NotificationRepository(private val supabase: SupabaseClient) {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    /**
     * Fetch all notifications for the current user's teams
     */
    suspend fun fetchNotifications(userId: String): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest
                .from("notifications")
                .select {
                    // Direct filter parameter
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Notification>()

            _notifications.value = result
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Subscribe to realtime notifications for a specific user
     * This listens to both the database changes AND custom realtime broadcasts
     */
    suspend fun subscribeToNotifications(userId: String, onNewNotification: (Notification) -> Unit) {
        try {
            // Create a unique channel for this user
            val channelName = "user:$userId:notifications"

            realtimeChannel = supabase.realtime.channel(channelName)

            // Subscribe to database INSERT events on the notifications table
            val databaseChanges = realtimeChannel!!.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "notifications"
                // No server filter - we filter in the flow
            }

            // Listen to database changes
            databaseChanges
                .onEach { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            // Decode the new notification
                            val notification = action.decodeRecord<Notification>()

                            // Update local state
                            _notifications.value = listOf(notification) + _notifications.value

                            // Callback for UI updates (e.g., show toast)
                            onNewNotification(notification)
                        }
                        is PostgresAction.Update -> {
                            val updatedNotification = action.decodeRecord<Notification>()
                            _notifications.value = _notifications.value.map {
                                if (it.id == updatedNotification.id) updatedNotification else it
                            }
                        }
                        is PostgresAction.Delete -> {
                            val deletedId = action.oldRecord["id"]?.jsonPrimitive?.content
                            _notifications.value = _notifications.value.filter { it.id != deletedId }
                        }
                        else -> {}
                    }
                }
                .catch { e ->
                    println("Error in notification realtime flow: ${e.message}")
                }
                .launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO))

            // Subscribe to the channel
            realtimeChannel!!.subscribe()

            println("✅ Subscribed to notifications for user: $userId")
        } catch (e: Exception) {
            println("❌ Error subscribing to notifications: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Unsubscribe from realtime notifications
     */
    suspend fun unsubscribeFromNotifications() {
        try {
            realtimeChannel?.unsubscribe()
            realtimeChannel = null
            println("✅ Unsubscribed from notifications")
        } catch (e: Exception) {
            println("❌ Error unsubscribing from notifications: ${e.message}")
        }
    }

    /**
     * Mark a notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest
                .from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("id", notificationId)
                    }
                }

            // Update local state
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark all notifications as read for the current user
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest
                .from("notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }

            // Update local state
            _notifications.value = _notifications.value.map {
                it.copy(isRead = true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest
                .from("notifications")
                .delete {
                    filter {
                        eq("id", notificationId)
                    }
                }

            // Update local state
            _notifications.value = _notifications.value.filter { it.id != notificationId }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all notifications for the current user
     */
    suspend fun clearAllNotifications(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest
                .from("notifications")
                .delete {
                    filter { eq("user_id", userId) }
                }

            // Clear local state
            _notifications.value = emptyList()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
