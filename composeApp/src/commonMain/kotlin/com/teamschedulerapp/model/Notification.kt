package com.teamschedulerapp.model

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
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val type: String // "task", "team", "schedule", "general"
)