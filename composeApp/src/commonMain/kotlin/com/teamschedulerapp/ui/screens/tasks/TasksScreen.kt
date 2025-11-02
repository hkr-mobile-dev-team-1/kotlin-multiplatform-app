package com.teamschedulerapp.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskWithAssignments
import com.teamschedulerapp.navigation.TeamManager
import com.teamschedulerapp.screenmodel.TaskScreenModel
import com.teamschedulerapp.ui.components.NoTeamsEmptyState
import com.teamschedulerapp.ui.components.tasks.AddTaskModal
import com.teamschedulerapp.ui.components.tasks.TaskCard
import com.teamschedulerapp.ui.components.tasks.TaskDetailModal
import com.teamschedulerapp.ui.components.tasks.FilterDropdown
import com.teamschedulerapp.ui.components.tasks.FilterOption
import com.teamschedulerapp.ui.components.tasks.SortDropdown
import com.teamschedulerapp.ui.components.tasks.applyFilters
import com.teamschedulerapp.ui.components.tasks.applySorting
import com.teamschedulerapp.utils.showErrorSnackbar
import com.teamschedulerapp.utils.showSuccessSnackbar
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun TasksScreen (
    screenModel: TaskScreenModel,
    snackbarHostState: SnackbarHostState? = null,
    onCreateTeam: () -> Unit = {}
) {
    val currentTeam by TeamManager.currentTeam.collectAsState()
    val tasksWithAssignments by screenModel.tasksWithAssignments.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val currentUserId = SupabaseClientManager.client.auth.currentUserOrNull()?.id
    var showAddTaskModal by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskWithAssignments?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<FilterOption>(FilterOption(emptySet<String>(),emptySet<String>())) }
    var selectedSort by remember { mutableStateOf<String>("due_date_nearest") }

    val scope = rememberCoroutineScope()
    val tabs = listOf("All Tasks", "My Tasks", "Unassigned")

    val listState = rememberLazyListState()

    LaunchedEffect(selectedSort, selectedFilter) {
        listState.animateScrollToItem(0)
    }

    // Show empty state if no team is selected
    if (currentTeam == null) {
        NoTeamsEmptyState(
            onCreateTeam = onCreateTeam
        )
        return
    }

    val tabFilteredTasks = when (selectedTab) {
        0 -> tasksWithAssignments
        1 -> tasksWithAssignments.filter { taskWithAssignments ->
            taskWithAssignments.assignedMembers.any { it.id == currentUserId }
        }
        2 -> tasksWithAssignments.filter { it.assignedMembers.isEmpty() }
        else -> tasksWithAssignments
    }
    val filteredTasks = applyFilters(tabFilteredTasks, selectedFilter)
    val sortedTasks = applySorting(filteredTasks, selectedSort)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showAddTaskModal = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F5F5),
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFF5F5F5),
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterDropdown(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                modifier = Modifier.weight(1f)
            )

            SortDropdown(
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it },
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Content Area
        if (sortedTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "No tasks yet"
                            1 -> "No tasks assigned to you"
                            2 -> "No unassigned tasks"
                            else -> "No tasks"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tasks will appear here when created",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(sortedTasks, key = { it.id }) { taskWithAssignments ->
                    TaskCard(
                        taskWithAssignments = taskWithAssignments,
                        openTaskDetail = {
                            selectedTask = taskWithAssignments
                            editMode = false
                        },
                        onEditClick = {
                            selectedTask = taskWithAssignments
                            editMode = true
                        },
                        onDeleteClick = {
                            scope.launch {
                                try {
                                    screenModel.deleteTask(taskWithAssignments.id)

                                    // Show success snackbar
                                    snackbarHostState?.showSuccessSnackbar("Task deleted successfully")
                                } catch (e: Exception) {
                                    // Show error snackbar
                                    snackbarHostState?.showErrorSnackbar("Failed to delete task")
                                }
                            }
                            selectedTask = null
                        }

                    )
                }
            }
        }
    }

    // Add Task Modal
    if (showAddTaskModal) {
        AddTaskModal(
            screenModel = screenModel,
            onDismiss = { showAddTaskModal = false },
            onSave = { title, description, status, priority, assignedMembers, dueDate ->
                scope.launch {
                    try {
                        screenModel.createTask(
                            Task(
                                title = title,
                                teamId = TeamManager.currentTeam.value?.id!!,
                                description = description,
                                status = status,
                                priority = priority,
                                dueDate = dueDate
                            ),
                            assignedMembers,
                        )

                        showAddTaskModal = false

                        // Show success snackbar
                        snackbarHostState?.showSuccessSnackbar("Task created successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState?.showErrorSnackbar("Failed to create task")
                    }
                }
            }
        )
    }

    // Task Description Modal
    selectedTask?.let { taskWithAssignment ->
        TaskDetailModal(
            taskWithAssignment = taskWithAssignment,
            isEditMode = editMode,
            onDismiss = { selectedTask = null },
            onDelete = {
                scope.launch {
                    try {
                        screenModel.deleteTask(taskWithAssignment.id)

                        // Show success snackbar
                        snackbarHostState?.showSuccessSnackbar("Task deleted successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState?.showErrorSnackbar("Failed to delete task")
                    }
                }
                selectedTask = null
            },
            onSave = { title, description, status, priority, assignedMembers, dueDate ->
                scope.launch {
                    try {
                        screenModel.updateTask(
                            Task(
                                id = taskWithAssignment.id,
                                title = title,
                                description = description,
                                status = status,
                                priority = priority,
                                dueDate = dueDate,
                                teamId = ""
                            ),
                            assignedMembers,
                        )

                        showAddTaskModal = false

                        // Show success snackbar
                        snackbarHostState?.showSuccessSnackbar("Task updated successfully")
                    } catch (e: Exception) {
                        // Show error snackbar
                        snackbarHostState?.showErrorSnackbar("Failed to update task")
                    }
                }
                selectedTask = null
            }
        )
    }
}