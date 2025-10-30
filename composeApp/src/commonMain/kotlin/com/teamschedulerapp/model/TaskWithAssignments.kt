package com.teamschedulerapp.model

data class TaskWithAssignments(
    val task: Task,
    val assignedMembers: List<TeamMemberWithUser>
)
