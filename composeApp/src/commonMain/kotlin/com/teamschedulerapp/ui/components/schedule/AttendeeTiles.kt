package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.model.initialsOf

@Composable
fun AttendeeRow(attendees: List<Attendee>) {
    if (attendees.isEmpty()) return
    Text("Attendees", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(attendees) { a -> AttendeeChip(a) }
    }
}

@Composable
private fun AttendeeChip(a: Attendee) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 1.dp) {
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
                Text(initialsOf(a.displayName), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(a.displayName, style = MaterialTheme.typography.bodyMedium)
                val window = listOfNotNull(a.from, a.to).takeIf { it.isNotEmpty() }?.joinToString("–")
                if (window != null) Text(window, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}