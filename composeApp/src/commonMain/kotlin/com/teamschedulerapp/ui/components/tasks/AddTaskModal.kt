package com.teamschedulerapp.ui.components.tasks

import com.teamschedulerapp.model.User
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
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.screenmodel.TaskScreenModel
import com.teamschedulerapp.ui.components.UserLabel
import kotlinx.datetime.*
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AddTaskModal(
    screenModel: TaskScreenModel,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        status: String,
        priority: String,
        assignedMembers: List<TeamMemberWithUser>,
        dueDate: String?
    ) -> Unit
) {
    val currentTeam by TeamManager.currentTeam.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("pending") }
    var selectedPriority by remember { mutableStateOf<String>("medium") }
    var selectedMembers by remember { mutableStateOf(emptyList<TeamMemberWithUser>()) }
    var selectedDueDate by remember { mutableStateOf<String?>(null) }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                    text = "Add New Task",
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
                       listOf<String>("pending", "in progress", "blocked", "done").forEach { status ->
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
                    value = selectedDueDate?.let { dateString ->
                        val date = LocalDate.parse(dateString)
                        "${date.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }.take(3)} ${date.dayOfMonth}, ${date.year}"
                    } ?: "",
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
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
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
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank()
                ) {
                    Text("Create Task")
                }
            }
        }
    }
}