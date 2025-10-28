package com.teamschedulerapp.ui.screens.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


import com.teamschedulerapp.ui.components.schedule.AttendanceDialog
import com.teamschedulerapp.ui.components.schedule.AttendeeList
import com.teamschedulerapp.ui.components.schedule.DeleteDialog
import com.teamschedulerapp.repositories.AvailabilityRepository
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.domain.toAvailability
import com.teamschedulerapp.model.Attendee

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// lib
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import com.teamschedulerapp.domain.toAttendee


@OptIn(ExperimentalTime::class)
@Composable
fun ScheduleScreen(
    availabilityRepository: AvailabilityRepository,
    userId: String,
    currentUserDisplayName: String,
) {
    // time anchors
    // today (for highlighting)
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    // current month - which showing first
    val currentMonth = remember { YearMonth.now() }

    // calendar range (how far back and forth)
    val startMonth   = remember { currentMonth.minusMonths(12) }
    val endMonth     = remember { currentMonth.plusMonths(12) }

    // days of week lib (set to begin with Monday)
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }

    // calendar state
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
    )

    // UI events - dialog, attendance edit
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    // attendance state
    var attendanceByDate by remember {
        mutableStateOf<Map<LocalDate, List<Attendee>>>(emptyMap())
    }
    var editTarget by remember { mutableStateOf<Attendee?>(null) }
    // dialog trigger
    var showDialogFor by remember { mutableStateOf<LocalDate?>(null) }

    // delete dialog trigger
    var pendingDelete by remember { mutableStateOf<Attendee?>(null) }


    // repo scopes (DB related)
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }


    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val team by TeamManager.currentTeam.collectAsState()
    val teamId = team?.id ?: return
    // members related to a specific manager (later for reading)
    val members by TeamManager.currentTeamMembers.collectAsState()

    Column(Modifier.fillMaxSize().padding(3.dp)) {
        TopAppBar(
            title = {
                Text(
                    "Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        // month header - follow swipe
        MonthHeader(state)
        // Calendar grid lib
        HorizontalCalendar(
            state = state,
            monthHeader = { month ->
                // weekday scrolls lib
                val days = month.weekDays.first().map { it.date.dayOfWeek }
                DaysOfWeekTitle(days, Modifier.padding(bottom = 8.dp))
            },
            dayContent = { day ->
                val isOverflow = day.position != DayPosition.MonthDate
                val isSelected = selected == day.date
                val isToday = day.date == today
                val headcount = attendanceByDate[day.date]?.size ?: 0

                DayCell(
                    day = day,
                    isOverflow = isOverflow,
                    isSelected = isSelected,
                    isToday = isToday,
                    headcount = headcount,
                    onClick = {
                        if (!isOverflow) {
                            // toggle selection
                            selected = if (isSelected) null else day.date
                        }
                    }
                )
            }
        )
        // Show currently selected date label
        selected?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Selected: $it",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // retrieve team members for selected date from DB and map to show on UI (tiny caching)
        LaunchedEffect(selected, teamId) {
            val date = selected ?: return@LaunchedEffect
            loading = true
            loadError = null
            try {
                val rows = availabilityRepository.getAvailabilityForTeamOnDate(
                    teamId = teamId,
                    date = date.toString()
                )
                val list = rows.map {
                    it.toAttendee(displayName = currentUserDisplayName ?: "Member")
                }
                attendanceByDate = attendanceByDate.toMutableMap().apply { this[date] = list }
            } catch (t: Throwable) {
                loadError = t.message
            } finally {
                loading = false
            }
        }
        // show attendees and button when day is selected
        if (selected != null) {
            // Attendee tiles for selected day
            Spacer(Modifier.height(12.dp))
            val attendees = attendanceByDate[selected] ?: emptyList()
            AttendeeList(
                attendees,
                onEdit = { a ->
                    editTarget = a
                    showDialogFor = selected
                },
                onDelete = { a ->
                   pendingDelete = a
                }

            )

            Spacer(Modifier.height(12.dp))

            // Button to trigger the attendance dialog
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showDialogFor = selected },   // open dialog
                    ) { Text("Add my attendance") }
                }
            }
        }

        // Dialog
        showDialogFor?.let { date ->
            //collecting data, passing the prefilled first last name
            androidx.compose.runtime.key(date to (editTarget?.displayName ?: "")) {
            AttendanceDialog(
                date = date,
                initialName = currentUserDisplayName,
                initialFrom = editTarget?.from,
                initialTo = editTarget?.to,
                onConfirm = { name, from, to ->
                    saving = true
                    saveError = null
                    scope.launch {
                        try {
                            // build UI model Attendee
                            val attendee = Attendee(displayName = name, from = from, to = to)
                            // map DB row
                            val row = attendee.toAvailability(
                                userId = userId,
                                teamId = teamId,
                                date   = date
                            )
                            // upsert to DB
                            val ok = availabilityRepository.upsertAvailability(row)
                            if (!ok) throw IllegalStateException("Insert/upsert failed")
                            // local UI state mirrors change
                            attendanceByDate = attendanceByDate.toMutableMap().apply {
                            val list = (this[date] ?: emptyList()).toMutableList()
                            val idx =
                                list.indexOfFirst { it.displayName.equals(name, ignoreCase = true) }
                                if (idx >= 0) list[idx] = attendee else list += attendee
                                this[date] = list
                        }
                            // bye bye dialog
                            editTarget = null
                            showDialogFor = null

                        } catch (t: Throwable) {
                            saveError = t.message ?: "Unknown error"
                        } finally {
                            saving = false
                        }
                    }
                },
                onDismiss = {
                    editTarget = null
                    showDialogFor = null
                }
            )
            }
        }

    pendingDelete?.let { toDelete ->
        DeleteDialog(
            onDismissRequest = { pendingDelete = null },
            onConfirmation =  {
                val date = selected
                if (date != null) {
                    attendanceByDate = attendanceByDate.toMutableMap().apply {
                        val updated = (this[date] ?: emptyList())
                            .filterNot { it.displayName.equals(toDelete.displayName, true) }
                        this[date] = updated
                    }
                }
                pendingDelete = null
            },
            dialogTitle = "Remove attendance",
            dialogText = "Are you sure you want to remove your attendance?",
        )
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isOverflow: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    headcount: Int,
    onClick: () -> Unit
) {
    // cell bg colors
    val bg = when {
        isOverflow -> MaterialTheme.colorScheme.surface
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday    -> MaterialTheme.colorScheme.surfaceVariant
        else       -> MaterialTheme.colorScheme.surface
    }

    // text color for calendar days (dim overflow)
    val dayNumberColor = if (isOverflow)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.onSurface

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isSelected && !isOverflow) 2.dp else 0.dp,
        modifier = Modifier.aspectRatio(1f).clickable(enabled = !isOverflow, onClick = onClick)
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
            Text(
                day.date.day.toString(),
                modifier = Modifier.align(Alignment.Center),
                color = dayNumberColor
            )
            if (!isOverflow && headcount > 0) {
                Text(
                    headcount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

// setup days of week titles lib
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { dow ->
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dow.name.lowercase().replaceFirstChar { it.titlecase() }.take(3),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun MonthHeader(state: CalendarState, modifier: Modifier = Modifier) {
    // visible month as user swipes - reactive
    val visibleMonth by remember(state) {
        derivedStateOf { state.firstVisibleMonth.yearMonth }
    }

    val monthTitle = "${visibleMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }} • ${visibleMonth.year}"

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
            ) { Text(monthTitle, style = MaterialTheme.typography.titleMedium)
        }
    }
}


