package com.teamschedulerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

fun formatDueDate(dueDateString: String): String {
    val localDate = LocalDate.parse(dueDateString)
    val month = localDate.month.name
        .lowercase()
        .replaceFirstChar { it.uppercase() }
        .take(3)
    val day = localDate.dayOfMonth
    val year = localDate.year

    return "$month $day, $year"
}

@Composable
fun DateRange(
    startDate: String?,
    endDate: String?,
    big: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.DateRange,
            contentDescription = "Schedule",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (big) 24.dp else 16.dp)
        )
        if (endDate == null && startDate == null) {
            Text(
                text = "Untracked",
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (startDate != null && endDate != null) {
            Text(
                text = formatDueDate(startDate),
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "To",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (big) 20.dp else 14.dp)
            )
            Text(
                text = formatDueDate(endDate),
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (endDate != null) {
            Text(
                text = "Due:",
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDueDate(endDate),
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (startDate != null) {
            Text(
                text = "Start:",
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDueDate(startDate),
                style = if (big) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}