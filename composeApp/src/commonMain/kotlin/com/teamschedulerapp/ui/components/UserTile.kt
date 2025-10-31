package com.teamschedulerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
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

fun getColorForUser(userId: String): Color {
    val seed = userId.hashCode()
    val random = Random(seed)
    val red = random.nextInt(110, 256)
    val green = random.nextInt(110, 256)
    val blue = random.nextInt(110, 256)
    return Color(red, green, blue)
}
@Composable
fun UserTile (user: User) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(getColorForUser(user.id)),
        contentAlignment = Alignment.Center
    ) {
        // If we have a user.profileImageUrl, we can use AsyncImage (from Coil)
        // For now, just show initials
        if (user.firstName != null) {
            Text(
                text = user.firstName.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "User",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(8.dp)
            )
        }

    }
}