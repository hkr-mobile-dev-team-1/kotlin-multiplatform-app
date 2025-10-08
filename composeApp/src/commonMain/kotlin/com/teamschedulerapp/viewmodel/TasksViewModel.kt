package com.teamschedulerapp.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task
import com.teamschedulerapp.model.TaskStatus
import kotlinx.datetime.LocalDate

class TasksViewModel {
    private val _tasks = MutableStateFlow(
        listOf(
            Task(
                title = "Design login page",
                description = "Create a responsive login screen with email and password validation.",
                status = TaskStatus.IN_PROGRESS,
                startDate = LocalDate.parse("2025-10-02"),
                endDate = LocalDate.parse("2025-10-07")
            ),
            Task(
                title = "Fix API bug",
                description = "Resolve the issue causing incorrect user data to load.",
                status = TaskStatus.PENDING,
                startDate = LocalDate.parse("2025-10-09"),
                endDate = LocalDate.parse("2025-10-15")
            ),
            Task(
                title = "Implement schedule view",
                description = "Develop the calendar UI and connect it to the schedule data.",
                status = TaskStatus.DONE,
                startDate = LocalDate.parse("2025-09-25"),
                endDate = LocalDate.parse("2025-09-30")
            ),
            Task(
                title = "Add authentication flow",
                description = "Integrate Supabase authentication for login and sign-up.",
                status = TaskStatus.BLOCKED,
                startDate = LocalDate.parse("2025-10-05"),
                endDate = LocalDate.parse("2025-10-12")
            )
        )
    )
    val tasks = _tasks.asStateFlow()
}
