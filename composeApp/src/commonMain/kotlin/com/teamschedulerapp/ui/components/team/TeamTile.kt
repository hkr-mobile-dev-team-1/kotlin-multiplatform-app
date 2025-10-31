package com.teamschedulerapp.ui.components.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TeamWithMembers
import kotlin.random.Random

fun getColorForUser(teamId: String): Color {
    val seed = teamId.hashCode()
    val random = Random(seed)
    val red = random.nextInt(110, 256)
    val green = random.nextInt(110, 256)
    val blue = random.nextInt(110, 256)
    return Color(red, green, blue)
}
@Composable
fun TeamTile(
    team: TeamWithMembers?,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )

    if (team != null && team.id != null) {
        Box(
            modifier = modifier.background(getColorForUser(team.id)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = team.name.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.People,
                contentDescription = "Team",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
