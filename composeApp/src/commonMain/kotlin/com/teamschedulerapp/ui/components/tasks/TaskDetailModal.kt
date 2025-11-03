package com.teamschedulerapp.ui.components.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TaskWithAssignments
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.navigation.UserManager
import com.teamschedulerapp.ui.components.DateRange
import com.teamschedulerapp.ui.components.UserLabel
import com.teamschedulerapp.utils.DateUtils.formatDateForDisplay
import kotlinx.datetime.*
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun TaskDetailModal(
    taskWithAssignments: TaskWithAssignments?,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (
        title: String,
        description: String,
        status: String,
        priority: String,
        assignedMembers: List<TeamMemberWithUser>,
        startDate: String?,
        endDate: String?
    ) -> Unit
) {
    val currentTeam by TeamManager.currentTeam.collectAsState()
    val currentUserId = UserManager.getCurrentUserId()
    val isCurrentUserAdmin = TeamManager.isUserAdminOfTeam(currentTeam?.id!!, currentUserId)

    // Initialize state from existing task or defaults
    var title by remember { mutableStateOf(taskWithAssignments?.title ?: "") }
    var description by remember { mutableStateOf(taskWithAssignments?.description ?: "") }
    var selectedStatus by remember { mutableStateOf(taskWithAssignments?.status ?: "pending") }
    var selectedPriority by remember { mutableStateOf(taskWithAssignments?.priority ?: "medium") }
    var selectedMembers by remember { mutableStateOf(taskWithAssignments?.assignedMembers ?: emptyList()) }
    var selectedStartDate by remember { mutableStateOf(taskWithAssignments?.startDate) }
    var selectedEndDate by remember { mutableStateOf(taskWithAssignments?.endDate) }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var showDatePicker: Int? by remember { mutableStateOf(null) }
    var isEditing by remember { mutableStateOf(isEditMode) }

    // Validation
    val isTitleValid = title.isNotBlank()
    val hasChanges = remember(title, description, selectedStatus, selectedPriority, selectedMembers, selectedStartDate, selectedEndDate) {
        if (taskWithAssignments == null) {
            title.isNotBlank() || description.isNotBlank()
        } else {
            title != taskWithAssignments.title ||
                    description != taskWithAssignments.description ||
                    selectedStatus != taskWithAssignments.status ||
                    selectedPriority != taskWithAssignments.priority ||
                    selectedMembers != taskWithAssignments.assignedMembers ||
                    selectedStartDate != taskWithAssignments.startDate ||
                    selectedEndDate != taskWithAssignments.endDate
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.DarkGray.copy(alpha = 0.6f),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        taskWithAssignments == null -> "Create New Task"
                        isEditing -> "Edit Task"
                        else -> "Task Details"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            HorizontalDivider()

            if (isEditing || taskWithAssignments == null) {

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Task Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = title.isBlank() && title.isNotEmpty(),
                        supportingText = {
                            if (title.isBlank() && title.isNotEmpty()) {
                                Text("Title is required", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    // Task Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                        placeholder = { Text("Add task details...") }

                    )

                    // Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = " ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            leadingIcon = {
                                Box(modifier = Modifier.padding(start = 10.dp)) {
                                    StatusLabel(status = selectedStatus)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            containerColor = Color.White
                        ) {
                            listOf<String>(
                                "pending",
                                "in progress",
                                "blocked",
                                "done"
                            ).forEach { status ->
                                DropdownMenuItem(
                                    text = { StatusLabel(status = status) },
                                    onClick = {
                                        selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Dropdown
                    ExposedDropdownMenuBox(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = " ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            leadingIcon = {
                                Box(modifier = Modifier.padding(start = 10.dp)) {
                                    PriorityLabel(priority = selectedPriority)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false },
                            containerColor = Color.White
                        ) {
                            listOf<String>("high", "medium", "low").forEach { priority ->
                                DropdownMenuItem(
                                    text = { PriorityLabel(priority = priority) },
                                    onClick = {
                                        selectedPriority = priority
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Start Date Field
                    OutlinedTextField(
                        value = formatDateForDisplay(selectedStartDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Date") },
                        placeholder = { Text("Select date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = 0 }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // End Date Field
                    OutlinedTextField(
                        value = formatDateForDisplay(selectedEndDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Date") },
                        placeholder = { Text("Select date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = 1 }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date Picker Dialog
                    if (showDatePicker != null) {
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = null },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                                            .toString()
                                        if (showDatePicker == 0) {
                                            selectedStartDate = selectedDate
                                        } else if (showDatePicker == 1) {
                                            selectedEndDate = selectedDate
                                        }
                                    }
                                    showDatePicker = null
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = null }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // Clear dates option
                    if (selectedStartDate != null || selectedEndDate != null) {
                        TextButton(
                            onClick = {
                                selectedStartDate = null
                                selectedEndDate = null
                            }
                        ) {
                            Text("Clear dates")
                        }
                    }


                    // Assign Members
                    Column {
                        Text(
                            text = if (isCurrentUserAdmin) "Assign to" else "Assign yourself",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentTeam?.members.isNullOrEmpty()) {
                            Text(
                                text = "No team members available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (isCurrentUserAdmin) {
                            // Admin can assign anyone
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentTeam?.members?.forEach { member ->
                                    val isSelected = selectedMembers.contains(member)

                                    UserLabel(
                                        member = member,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedMembers = if (isSelected) {
                                                selectedMembers - member
                                            } else {
                                                selectedMembers + member
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            // Non-admin can only assign themselves
                            val currentUserMember = currentTeam?.members?.find { it.id == currentUserId }

                            if (currentUserMember != null) {
                                val isAssigned = selectedMembers.contains(currentUserMember)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    UserLabel(
                                        member = currentUserMember,
                                        isSelected = isAssigned,
                                        onClick = {
                                            selectedMembers = if (isAssigned) {
                                                emptyList()
                                            } else {
                                                listOf(currentUserMember)
                                            }
                                        }
                                    )

                                    Text(
                                        text = if (isAssigned) "Assigned to you" else "Tap to assign yourself",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "You are not a member of this team",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            else {
                // VIEW MODE - Display Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Task Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Task Description (if exists)
                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(0.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status
                    Column() {
                        Text(
                            text = "Status ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        StatusLabel(selectedStatus)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Priority
                    Column() {
                        Text(
                            text = "Priority ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PriorityLabel(priority = selectedPriority)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Due Dates
                    Column() {
                        Text(
                            text = "Due dates ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DateRange(startDate = selectedStartDate, endDate = selectedEndDate, big = true)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Assignees
                    Column {
                        Text(
                            text = "Assignees",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (taskWithAssignments.assignedMembers.isEmpty()) {
                            Text(
                                text = "No team members assigned",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                taskWithAssignments.assignedMembers.forEach { member ->
                                    UserLabel(
                                        member = member,
                                        isSelected = false,
                                        onClick = { }
                                    )
                                }
                            }
                        }

                    }
                }
            }


            // Footer Buttons
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { if (isEditing) onDismiss() else onDelete() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Cancel" else "Delete")
                }
                Button(
                    onClick = {
                        if (isEditing) {
                            if (title.isNotBlank()) {
                                onSave(
                                    title,
                                    description,
                                    selectedStatus,
                                    selectedPriority,
                                    selectedMembers,
                                    selectedStartDate,
                                    selectedEndDate
                                )
                                onDismiss()
                            }
                        } else {
                            isEditing = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = if (isEditing) {isTitleValid && hasChanges} else true
                ) {
                    Text(
                        text = when {
                            taskWithAssignments == null -> "Create"
                            isEditing -> "Save"
                            else -> "Edit"
                        },
                    )
                }
            }
        }
    }
}