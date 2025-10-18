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
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.domain.buildMonthGrid
import com.teamschedulerapp.domain.firstOfMonth
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.model.CalendarDay
import com.teamschedulerapp.ui.components.schedule.AttendanceDialog
import kotlinx.datetime.*

@Composable
fun ScheduleScreen() {
    // time anchors
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(tz).date }

    // local screen state
    var monthFirst by remember { mutableStateOf(today.firstOfMonth()) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    // attendance state (UI only
    var attendanceByDate by remember {
        mutableStateOf<Map<LocalDate, List<Attendee>>>(emptyMap())
    }

    // build grid for visible month
    val baseDays = remember(monthFirst, today) {
        buildMonthGrid(
            monthFirstDay = monthFirst,
            today = today,
            headcountFor = { d -> attendanceByDate[d]?.size ?: 0 }
        )
    }

    val days = baseDays

    // dialog trigger
    var showDialogFor by remember { mutableStateOf<LocalDate?>(null) }

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
                    onClick = {
                        if (day.isCurrentMonth) {
                            selected = day.date
                            showDialogFor = day.date  // open the attendance dialog
                        }
                    }
                )
            }
        }
        // prepare attendee tiles for selected day
        //Spacer(Modifier.height(12.dp))
        // attendees for the selected day (chips)
        //val attendees = attendanceByDate[selected] ?: emptyList()
        //AttendeeRow(attendees)

        // Dialog
        val dateForDialog = showDialogFor
        if (dateForDialog != null) {
            AttendanceDialog(
                date = dateForDialog,
                onConfirm = { name, from, to ->
                    // Upsert attendee for this date (demo uses name as unique key) - TODO: wire to DB
                    attendanceByDate = attendanceByDate.toMutableMap().apply {
                        val list = (this[dateForDialog] ?: emptyList()).toMutableList()
                        val idx =
                            list.indexOfFirst { it.displayName.equals(name, ignoreCase = true) }
                        val newA = Attendee(displayName = name, from = from, to = to)
                        if (idx >= 0) list[idx] = newA else list += newA
                        this[dateForDialog] = list
                    }
                    showDialogFor = null
                },
                onDismiss = { showDialogFor = null }
            )
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

    val isOverflow = !day.isCurrentMonth

    val bg = when {
        isOverflow -> MaterialTheme.colorScheme.surface
        selected -> MaterialTheme.colorScheme.primaryContainer
        day.isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val dayNumberColor = if (isOverflow)
        LocalContentColor.current.copy(alpha = 0.35f) else LocalContentColor.current

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isOverflow) Modifier.semantics { disabled() } else Modifier
            )
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        color = bg,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (selected && !isOverflow) 2.dp else 0.dp
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = dayNumberColor,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                if (!isOverflow && day.headcount > 0) {
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
