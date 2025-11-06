package com.teamschedulerapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Notification data model
 *
 * @param id Unique identifier for the notification
 * @param createdAt ISO timestamp when notification was created
 * @param teamId Team associated with the notification
 * @param userId User who should receive the notification (null for shared notifications)
 * @param actor User who triggered the notification
 * @param type Type of notification (task_assigned, task_completed, etc.)
 * @param payload JSON object containing notification-specific data
 * @param isRead Whether the notification has been read
 */
@Serializable
data class Notification(
    val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val actor: String? = null,
    val type: String, // "task_completed", "task_assigned", etc.
    val payload: JsonObject, // Structured JSON data
    @SerialName("is_read") val isRead: Boolean = false
)

/**
 * Typed payload models for different notification types
 */
@Serializable
data class TaskAssignedPayload(
    @SerialName("task_id") val taskId: String,
    @SerialName("task_title") val taskTitle: String,
    @SerialName("team_id") val teamId: String,
    @SerialName("assigned_to") val assignedTo: String,
    val actor: String? = null,
    val type: String,
)

@Serializable
data class TaskCompletedPayload(
    @SerialName("task_id") val taskId: String,
    @SerialName("task_title") val taskTitle: String,
    @SerialName("team_id") val teamId: String,
    val actor: String? = null,
    val type: String,
)

/**
 * Helper extension to parse payload into specific types
 */
inline fun <reified T> Notification.parsePayload(): T? {
    return try {
        Json.decodeFromJsonElement(payload)
    } catch (e: Exception) {
        null
    }
}