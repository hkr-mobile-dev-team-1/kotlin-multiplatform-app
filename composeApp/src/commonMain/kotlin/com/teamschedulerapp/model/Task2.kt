package com.teamschedulerapp.model

import kotlinx.datetime.LocalDate

data class Task2(
    val title: String,
    val description: String,
    val status: TaskStatus,
    val startDate: LocalDate,
    val endDate: LocalDate
)
