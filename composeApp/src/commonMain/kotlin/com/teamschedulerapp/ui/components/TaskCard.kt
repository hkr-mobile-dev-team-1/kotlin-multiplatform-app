package com.teamschedulerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.Task

@Composable
fun TaskCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                StatusLabel(task.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(task.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))
            DateRange(startDate = task.startDate.toString(), endDate = task.endDate.toString())
        }
    }
}


