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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TaskWithUsers
import com.teamschedulerapp.ui.components.DateRange
import com.teamschedulerapp.ui.components.UserLabel
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun TaskDetailModal(
    task: TaskWithUsers,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (
        title: String,
        description: String,
        status: String,
        priority: String,
        assignedUserIds: List<String>,
        dueDate: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(task.task.title) }
    var description by remember { mutableStateOf(task.task.description ?: "") }
    var selectedStatus by remember { mutableStateOf(task.task.status) }
    var selectedPriority by remember { mutableStateOf(task.task.priority) }
    var selectedUserIds by remember { mutableStateOf(task.assignedUsers.map { it -> it.id }) }
    var selectedDueDate by remember { mutableStateOf<String?>(task.task.dueDate ?: "") }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(isEditMode) }

    // Mock users
    val availableUsers = remember {
        listOf(
            User(
                id = "1",
                firstName = "Elina",
                lastName = "Rosato",
                email = "elinarosato@gmail.com"
            ),
            User(
                id = "2",
                firstName = "Dimple",
                lastName = "Narkhede",
                email = "dimplenarkhede@gmail.com"
            ),
            User(
                id = "3",
                firstName = "Dario",
                lastName = "Ostojic",
                email = "darioostojic@gmail.com"
            ),
            User(
                id = "4",
                firstName = "Andre",
                lastName = "Sandblom",
                email = "andresandblom@gmail.com"
            ),
            User(
                id = "5",
                firstName = "Kate",
                lastName = "Arvay",
                email = "katearvay@gmail.com"
            ),
            User(
                id = "6",
                firstName = "Dani",
                lastName = "Marcarini",
                email = "danimarcarini@gmail.com"
            )
        )
    }

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
                                "Pending",
                                "In progress",
                                "Blocked",
                                "Done"
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
                            listOf<String>("High", "Medium", "Low").forEach { priority ->
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
                            "${
                                date.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                                    .take(3)
                            } ${date.dayOfMonth}, ${date.year}"
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
                            availableUsers.forEach { user ->
                                val isSelected = selectedUserIds.contains(user.id)

                                UserLabel(
                                    user = user,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedUserIds = if (isSelected) {
                                            selectedUserIds - user.id
                                        } else {
                                            selectedUserIds + user.id
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
                        if (task.assignedUsers.isEmpty()) {
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
                                task.assignedUsers.forEach { user ->
                                    UserLabel(
                                        user = user,
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
                                    selectedUserIds,
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