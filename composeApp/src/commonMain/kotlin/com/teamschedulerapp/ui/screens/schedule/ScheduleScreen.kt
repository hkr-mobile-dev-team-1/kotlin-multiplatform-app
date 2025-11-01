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
import com.teamschedulerapp.model.Attendee
import com.teamschedulerapp.screenmodel.ScheduleScreenModel
import com.teamschedulerapp.repositories.UserRepository
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.ui.components.schedule.DaysOfWeekTitle
import com.teamschedulerapp.ui.components.schedule.DayCell
import com.teamschedulerapp.ui.components.schedule.MonthHeader

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// lib
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar


@OptIn(ExperimentalTime::class)
@Composable
fun ScheduleScreen(
    availabilityRepository: AvailabilityRepository,
    userRepository: UserRepository,
    userId: String,
    currentUserDisplayName: String,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
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
    var editTarget by remember { mutableStateOf<Attendee?>(null) }
    // dialog trigger
    var showDialogFor by remember { mutableStateOf<LocalDate?>(null) }
    // delete dialog trigger
    var pendingDelete by remember { mutableStateOf<Attendee?>(null) }

    val teamWithMembers by TeamManager.currentTeam.collectAsState()
    val teamId = teamWithMembers?.id ?: return
    // wire team members
    val teamMembers: List<TeamMemberWithUser> = teamWithMembers?.members ?: emptyList()

    // bring in screen model
    val screenModel = remember(availabilityRepository, userRepository, userId) {
        ScheduleScreenModel(availabilityRepository, userRepository, userId)
    }
    // pair attendee + owner
    val attendeesPairs by screenModel.attendeesForDay.collectAsState()
    val isLoading by screenModel.isLoading.collectAsState()
    val error by screenModel.error.collectAsState()

    val headcounts by screenModel.headcounts.collectAsState()

    // Load whenever team or selected date changes
    LaunchedEffect(teamId, selected) {
        selected?.let { date ->
            screenModel.loadDay(teamId = teamId, date = date, teamMembers = teamMembers)
        }
    }

    // plug in headcount load
    val visibleMonth by remember(state) { derivedStateOf { state.firstVisibleMonth.yearMonth } }

    LaunchedEffect(teamId, visibleMonth) {
        screenModel.loadHeadcountsForMonth(teamId, visibleMonth)
    }

    // reset to current month on screen re-entry
    LaunchedEffect(Unit) {
        state.scrollToMonth(currentMonth)
    }

    // react to UI events for snackbars to fire
    LaunchedEffect(Unit) {
        screenModel.uiEvents.collect { ev ->
            when (ev) {
                is ScheduleScreenModel.UiEvent.Success -> snackbarHostState.showSuccessSnackbar(ev.msg)
                is ScheduleScreenModel.UiEvent.Error   -> snackbarHostState.showErrorSnackbar(ev.msg)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

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
                val headcount = if (!isOverflow) (headcounts[day.date] ?: 0) else 0

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
        // show attendees and button when day is selected
        if (selected != null) {
            // Attendee tiles for selected day (wire owners to get "editability")
            Spacer(Modifier.height(12.dp))
            val attendees = attendeesPairs.map { it.first }
            val owners    = attendeesPairs.map { it.second }
            AttendeeList(
                attendees = attendees,
                canEdit = { a ->
                    val idx = attendees.indexOf(a)
                    owners.getOrNull(idx) == userId
                },
                onEdit = { a ->
                    val idx = attendees.indexOf(a)
                    if (owners.getOrNull(idx) == userId) {
                        editTarget = a
                        showDialogFor = selected
                    }
                },
                onDelete = { a ->
                    val idx = attendees.indexOf(a)
                    if (owners.getOrNull(idx) == userId) {
                        pendingDelete = a
                    }
                },
                // scrollable
                modifier = Modifier.weight(1f)
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
                    val attendee = Attendee(displayName = name, from = from, to = to)
                    screenModel.saveAttendance(teamId, date, attendee, teamMembers) {
                        editTarget = null
                        showDialogFor = null
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
                val date = selected ?: return@DeleteDialog
                screenModel.deleteAttendance(teamId, userId, date, teamMembers)
                pendingDelete = null
            },
            dialogTitle = "Remove attendance",
            dialogText = "Are you sure you want to remove your attendance?",
        )
    }
}
}