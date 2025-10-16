package com.teamschedulerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TaskPriority

@Composable
fun PriorityLabel(priority: TaskPriority) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = when (priority) {
            TaskPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
            TaskPriority.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
    ) {
        Text(
            text = "Priority",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (priority) {
                TaskPriority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                TaskPriority.MODERATE -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}