package com.teamschedulerapp.model

import kotlinx.serialization.SerialName

/**
 * Notification data model
 *
 * @param id Unique identifier for the notification
 * @param title Notification title/headline
 * @param message Detailed notification message
 * @param timestamp Human-readable timestamp (e.g., "2 hours ago")
 * @param isRead Whether the notification has been read
 * @param type Type of notification (task, team, schedule, etc.)
 */
data class Notification(
    val id: String,
    @SerialName("created_at")val createdAt: String? = null,
    @SerialName("team_id")val teamId: String? = null,
    @SerialName("user_id")val userId: String? = null,
    val type: String, // "task_completed", "task_assigned"
    val message: String,
    val isRead: Boolean = false,
    val actor: String? = null,
    )