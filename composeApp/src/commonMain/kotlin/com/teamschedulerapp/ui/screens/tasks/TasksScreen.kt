package com.teamschedulerapp.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.ui.components.TaskCard
import com.teamschedulerapp.viewmodel.TasksViewModel

@Composable
fun TasksScreen(viewModel: TasksViewModel = TasksViewModel()) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tasks") })
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(task)
                }
            }
        }
    }
}
