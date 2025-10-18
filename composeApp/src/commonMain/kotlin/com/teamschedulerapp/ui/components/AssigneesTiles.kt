package com.teamschedulerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.User
import kotlin.random.Random

fun getColorForUser(userId: Number): Color {
    val seed = userId.hashCode()
    val random = Random(seed)
    val red = random.nextInt(100, 256)
    val green = random.nextInt(100, 256)
    val blue = random.nextInt(100, 256)
    return Color(red, green, blue)
}


@Composable
fun AssigneesTiles (assignedUsers: List<User>) {
    if (assignedUsers.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-8).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val visibleUsers = assignedUsers.take(4)
            visibleUsers.forEach { user ->
                UserTile(
                    text = user.userName.firstOrNull()?.uppercase() ?: "",
                    color = getColorForUser(user.userId)
                )
            }

            if (assignedUsers.size > 4) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = 0.6f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${assignedUsers.size - 4}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}