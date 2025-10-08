package com.teamschedulerapp.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teamschedulerapp.model.Task

class TasksViewModel {
    private val _tasks = MutableStateFlow(
        listOf(
            Task("Design login page", "Due next week", "In progress"),
            Task("Fix API bug", "Critical issue", "Pending")
        )
    )
    val tasks = _tasks.asStateFlow()
}
