package com.teamschedulerapp.calendar

import kotlinx.datetime.LocalDate

/**
 * Represents a single cell in the calendar grid (one calendar day).
 *
 * @param date The actual date (YYYY-MM-DD).
 * @param isCurrentMonth Whether this date belongs to the month currently displayed.
 * @param isToday Whether this date matches the current system date.
 * @param taskCount Number of tasks assigned for this date.
 * @param headcount Number of users checked in for this date.
 */

data class CalendarDay(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val isToday: Boolean,
    val taskCount: Int,
    val headcount: Int
)

/**
 * UI state for the calendar screen.
 * This is the single source of truth that the UI observes and renders.
 *
 * @param monthFirstDay The first day of the month currently displayed.
 * @param selected The currently selected date, if any.
 * @param days The list of all visible days (usually 42 cells = 6x7 grid).
 * @param loading Whether data is being loaded from the repository.
 * @param error Optional error message (null if no error).
 */

data class CalendarUiState(
    val monthFirstDay: LocalDate,
    val selected: LocalDate? = null,
    val days: List<CalendarDay> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

/**
 * All possible user actions that can occur on the calendar screen.
 * These are processed by the [CalendarPresenter], which updates [CalendarUiState].
 */

sealed interface CalendarAction {
    /** Jump to a specific month (first day provided). */
    data class GoToMonth(val monthFirstDay: LocalDate): CalendarAction

    /** Navigate to next month. */
    data object NextMonth : CalendarAction

    /** Navigate to previous month. */
    data object PrevMonth : CalendarAction

    /** Select a specific date on the calendar. */
    data class Select(val day: LocalDate): CalendarAction

    /** Reload data for the current month. */
    data object Refresh : CalendarAction
}