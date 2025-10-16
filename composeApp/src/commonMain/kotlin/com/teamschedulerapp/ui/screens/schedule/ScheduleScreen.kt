package com.teamschedulerapp.ui.screens.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.data.buildMonthGrid
import com.teamschedulerapp.data.firstOfMonth
import com.teamschedulerapp.model.CalendarDay
import kotlinx.datetime.*

@Composable
fun ScheduleScreen() {
    // time anchors
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(tz).date }

    // local screen state
    var monthFirst by remember { mutableStateOf(today.firstOfMonth()) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    // build grid for visible month
    val days = remember(monthFirst, today) {
        buildMonthGrid(
            monthFirstDay = monthFirst,
            today = today,
            headcountFor = { _ -> 0 } // placeholder; repo later
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // header
        Header(
            monthFirst = monthFirst,
            onPrev = { monthFirst = shiftMonth(monthFirst, -1) },
            onNext = { monthFirst = shiftMonth(monthFirst, +1) }
        )

        Spacer(Modifier.height(8.dp))
        WeekdayRow()
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { day ->
                DayCell(
                    day = day,
                    selected = selected == day.date,
                    onClick = { if (day.isCurrentMonth) selected = day.date }
                )
            }
        }
        // Show currently selected date? TODO: later distinguish from tap selection
        selected?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Selected: $it",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun Header(
    monthFirst: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val title = "${monthFirst.month.name.lowercase().replaceFirstChar { it.titlecase() }} • ${monthFirst.year}"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrev) { Text("◀") }
        Text(title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onNext) { Text("▶") }
    }
}

@Composable
private fun WeekdayRow() {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, selected: Boolean, onClick: () -> Unit) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        day.isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        color = bg,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.TopStart)
            )
            if (day.headcount > 0) {
                Text(
                    text = "${day.headcount}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

private fun shiftMonth(date: LocalDate, delta: Int): LocalDate {
    val y = date.year + (date.monthNumber - 1 + delta).floorDiv(12)
    val m = ((date.monthNumber - 1 + delta) % 12 + 12) % 12 + 1
    return LocalDate(y, m, 1)
}
