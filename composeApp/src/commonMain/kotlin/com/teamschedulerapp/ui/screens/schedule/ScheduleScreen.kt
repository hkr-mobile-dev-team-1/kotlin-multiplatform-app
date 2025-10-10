package com.teamschedulerapp.ui.screens.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleScreen() {
    //static placeholders
    val monthTitle = "October • 2025"
    val weekdayLabels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    //6x7 grid
    val dayLabels: List<String> = (1..42).map { i -> if (i in 1..30) i.toString() else "" }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { /* noop for now */ }) { Text("◀") }
            Text(monthTitle, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { /* noop for now */ }) { Text("▶") }
        }

        Spacer(Modifier.height(8.dp))

        // weekday row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdayLabels.forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 6x7 grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dayLabels) { label ->
                DayCellStatic(label)
            }
        }
    }
}

@Composable
private fun DayCellStatic(label: String) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { /* noop */ },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}