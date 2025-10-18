package com.teamschedulerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TaskPriority
import androidx.compose.ui.graphics.Color

@Composable
fun PriorityLabel(priority: TaskPriority) {
    val (backgroundColor, textColor) = when (priority) {
        TaskPriority.HIGH -> Color(0xFFf3d4fc) to Color(0xFFa832cb)
        TaskPriority.MEDIUM -> Color(0xFFd1f6f0) to Color(0xFF00aca3)
        else -> Color(0xFFd9d6fd) to Color(0xFF4231f3)
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "Priority",
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = priority.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}