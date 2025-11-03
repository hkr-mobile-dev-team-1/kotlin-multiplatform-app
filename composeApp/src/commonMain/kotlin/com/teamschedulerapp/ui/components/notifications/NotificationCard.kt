package com.teamschedulerapp.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.Notification

@Composable
fun NotificationCard(
    notification: Notification,
    onNotificationClick: (Notification) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Different background for read vs unread - no card, just surface
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onNotificationClick(notification) },
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) {
            MaterialTheme.colorScheme.surface // Same as background when read
        } else {
            MaterialTheme.colorScheme.surfaceContainer // Slightly different when unread
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // User Avatar / Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Title with bold text for names/actions
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle/context (if provided in message)
                if (notification.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Optional: Small icon or letter for project/context
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = notification.message.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getNotificationColor(notification.type)
                            )
                        }

                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timestamp
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Unread indicator dot (only show for unread)
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "task" -> Icons.Default.Assignment
        "team" -> Icons.Default.Group
        "schedule" -> Icons.Default.CalendarMonth
        "comment" -> Icons.Default.ChatBubble
        "invite" -> Icons.Default.PersonAdd
        "upload" -> Icons.Default.Upload
        "mention" -> Icons.Default.AlternateEmail
        else -> Icons.Default.Notifications
    }
}

@Composable
private fun getNotificationColor(type: String): Color {
    return when (type) {
        "task" -> Color(0xFF6366F1) // Indigo
        "team" -> Color(0xFF8B5CF6) // Purple
        "schedule" -> Color(0xFF10B981) // Green
        "comment" -> Color(0xFF3B82F6) // Blue
        "invite" -> Color(0xFFEC4899) // Pink
        "upload" -> Color(0xFFF59E0B) // Amber
        "mention" -> Color(0xFF06B6D4) // Cyan
        else -> MaterialTheme.colorScheme.primary
    }
}