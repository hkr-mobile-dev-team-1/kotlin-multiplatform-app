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
import com.teamschedulerapp.ui.components.DateRange
import com.teamschedulerapp.ui.components.UserLabel
import com.teamschedulerapp.utils.DateUtils.formatDateForDisplay
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun TaskDetailModal(
    taskWithAssignment: TaskWithAssignments,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (
        title: String,
        description: String,
        status: String,
        priority: String,
        assignedMembers: List<TeamMemberWithUser>,
        dueDate: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(taskWithAssignment.title) }
    var description by remember { mutableStateOf(taskWithAssignment.description ?: "") }
    var selectedStatus by remember { mutableStateOf(taskWithAssignment.status) }
    var selectedPriority by remember { mutableStateOf(taskWithAssignment.priority) }
    var selectedMembers by remember { mutableStateOf(taskWithAssignment.assignedMembers) }
    var selectedDueDate by remember { mutableStateOf<String?>(taskWithAssignment.dueDate) }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(isEditMode) }

    val currentTeam by TeamManager.currentTeam.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.DarkGray.copy(alpha = 0.6f),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                    text = if (isEditing) "Edit Task" else "Task Detail",
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

            if (isEditing) {
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
                        singleLine = true
                    )

                    // Task Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
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

                    // Due Date Field
                    OutlinedTextField(
                        value = formatDateForDisplay(selectedDueDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date") },
                        placeholder = { Text("Select date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date Picker Dialog
                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        selectedDueDate = Instant.fromEpochMilliseconds(millis)
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date
                                            .toString()
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // Assignees Section
                    Column {
                        Text(
                            text = "Assign to",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentTeam?.members?.forEach { member ->
                                val isSelected = selectedMembers
                                    .map { member -> member.id }
                                    .contains(member.id)

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
                    }
                }
            }
            else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
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
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PriorityLabel(priority = selectedPriority)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Due Date
                    Column() {
                        Text(
                            text = "Due Date ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DateRange(startDate = null, endDate = selectedDueDate, big = true)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Assignees Row
                    Column {
                        Text(
                            text = "Assignees",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (taskWithAssignment.assignedMembers.isEmpty()) {
                            Text(
                                text = "No team member was assigned to this task.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                taskWithAssignment.assignedMembers.forEach { member ->
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

                Spacer(modifier = Modifier.height(12.dp))
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
                                    selectedDueDate
                                )
                                onDismiss()
                            }
                        } else {
                            isEditing = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = if (isEditing) title.isNotBlank() else true
                ) {
                    Text(if (isEditing) "Save" else "Edit Task")
                }
            }
        }
    }
}