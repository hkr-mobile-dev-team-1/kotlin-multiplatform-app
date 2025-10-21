package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.model.initialsOf
import kotlinx.datetime.LocalTime

private fun LocalTime.formatHm(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

@Composable
fun AttendeeList(attendees: List<Attendee>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        attendees.forEach { att ->
            AttendeeChip(att)
        }
    }
}

@Composable
private fun AttendeeChip(
    att: Attendee,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val initials = remember(att.displayName) { initialsOf(att.displayName) }
    val window = when {
        att.from != null && att.to != null -> "${att.from.formatHm()}–${att.to.formatHm()}"
        att.from != null -> "${att.from.formatHm()}–"
        att.to != null -> "–${att.to.formatHm()}"
        else -> null
    }

    Surface(
        modifier = modifier
        .width(180.dp)         // fixed width
        .height(56.dp),        // fixed height
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    att.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if(window != null) {
                    Text(
                        window,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
private fun OverflowChip(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.width(96.dp).height(56.dp), contentAlignment = Alignment.Center) {
            Text("+$count more", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// initials generator for the tile
private fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+"))
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}