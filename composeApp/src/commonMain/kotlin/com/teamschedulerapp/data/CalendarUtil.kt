package com.teamschedulerapp.data

import com.teamschedulerapp.model.CalendarDay
import kotlinx.datetime.*

/** Returns the first day of the month for the given date. */
fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year, month, 1)

/** Build a 6x7 calendar grid for the month containing [monthFirstDay]. Monday = first column. */
fun buildMonthGrid(
    monthFirstDay: LocalDate,
    today: LocalDate,
    headcountFor: (LocalDate) -> Int = { 0 },      // how many people for the day - placeholder 0
): List<CalendarDay> {
    val firstOfMonth = monthFirstDay.firstOfMonth()

    // Kotlinx datetime: Monday = 1 … Sunday = 7
    val firstDow = firstOfMonth.dayOfWeek.isoDayNumber
    val leadingBlanks = (firstDow - 1) // how many days from previous month to show

    val daysInMonth = firstOfMonth.lengthOfMonth()
    val totalCells = 42
    val cells = mutableListOf<CalendarDay>()

    // Start date shown in the grid:
    val gridStart = firstOfMonth.minus(DatePeriod(days = leadingBlanks))

    repeat(totalCells) { idx ->
        val date = gridStart.plus(DatePeriod(days = idx))
        val isCurrentMonth = date.month == firstOfMonth.month && date.year == firstOfMonth.year
        val isToday = date == today
        cells += CalendarDay(
            date = date,
            isCurrentMonth = isCurrentMonth,
            isToday = isToday,
            headcount = headcountFor(date)
        )
    }
    return cells
}

private fun LocalDate.lengthOfMonth(): Int {
    val first = firstOfMonth()
    val firstNext = if (monthNumber == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, monthNumber + 1, 1)
    return (firstNext.toEpochDays() - first.toEpochDays()).toInt()
}