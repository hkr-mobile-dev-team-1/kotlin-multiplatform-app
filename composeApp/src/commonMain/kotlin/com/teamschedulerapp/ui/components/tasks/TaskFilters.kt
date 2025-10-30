package com.teamschedulerapp.ui.components.tasks

import com.teamschedulerapp.model.TaskWithAssignments
import com.teamschedulerapp.utils.DateUtils

/**
 * Apply filter options to a list of tasks
 */
fun applyFilters(
    tasks: List<TaskWithAssignments>,
    filter: FilterOption
): List<TaskWithAssignments> {
    return tasks.filter { taskWithUsers ->
        val task = taskWithUsers.task
        val priorityMatch = filter.priority.isEmpty() || task.priority in filter.priority
        val statusMatch = filter.status.isEmpty() || task.status in filter.status
        priorityMatch && statusMatch
    }
}

/**
 * Apply sort option to a list of tasks
 */
fun applySorting(
    tasks: List<TaskWithAssignments>,
    sortOption: String?
): List<TaskWithAssignments> {
    return when (sortOption) {
        "priority_high_low" -> tasks.sortedBy { task ->
            when (task.task.priority) {
                "high" -> 1
                "medium" -> 2
                "low" -> 3
                else -> 4
            }
        }
        "priority_low_high" -> tasks.sortedBy { task ->
            when (task.task.priority) {
                "low" -> 1
                "medium" -> 2
                "high" -> 3
                else -> 4
            }
        }
        "status_pending_first" -> tasks.sortedBy { task ->
            when (task.task.status) {
                "pending" -> 1
                "in progress" -> 2
                "blocked" -> 3
                "done" -> 4
                else -> 5
            }
        }
        "status_done_first" -> tasks.sortedBy { task ->
            when (task.task.status) {
                "done" -> 1
                "blocked" -> 2
                "in progress" -> 3
                "pending" -> 4
                else -> 5
            }
        }
        "due_date_nearest" -> tasks.sortedWith { a, b ->
            val dateA = DateUtils.parseDate(a.task.dueDate)
            val dateB = DateUtils.parseDate(b.task.dueDate)
            when {
                dateA == null && dateB == null -> 0
                dateA == null -> 1
                dateB == null -> -1
                else -> dateA.compareTo(dateB)
            }
        }
        "due_date_furthest" -> tasks.sortedWith { a, b ->
            val dateA = DateUtils.parseDate(a.task.dueDate)
            val dateB = DateUtils.parseDate(b.task.dueDate)
            when {
                dateA == null && dateB == null -> 0
                dateA == null -> 1
                dateB == null -> -1
                else -> dateB.compareTo(dateA)
            }
        }
        else -> tasks
    }
}