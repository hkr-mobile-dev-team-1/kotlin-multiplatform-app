package com.teamschedulerapp.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Utility functions for date parsing and formatting
 */
object DateUtils {

    /**
     * Safely parse a date string that might be in various formats:
     * - ISO date: "2025-10-28"
     * - ISO timestamp: "2025-10-28T00:00:00+00:00"
     * - ISO timestamp with Z: "2025-10-28T00:00:00Z"
     *
     * Returns a LocalDate or null if parsing fails
     */
    @OptIn(ExperimentalTime::class)
    fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null

        return try {
            // Try parsing as just a date first (YYYY-MM-DD)
            if (dateString.length == 10 && dateString.count { it == '-' } == 2) {
                LocalDate.parse(dateString)
            } else {
                // Parse as full timestamp and extract date
                val instant = Instant.parse(dateString)
                instant.toLocalDateTime(TimeZone.UTC).date
            }
        } catch (e: Exception) {
            println("Error parsing date '$dateString': ${e.message}")
            null
        }
    }

    /**
     * Format a date string for display
     * Input: "2025-10-28" or "2025-10-28T00:00:00+00:00"
     * Output: "Oct 28, 2025"
     */
    fun formatDateForDisplay(dateString: String?): String {
        val date = parseDate(dateString) ?: return ""
        return formatLocalDateForDisplay(date)
    }

    /**
     * Format a LocalDate for display
     * Output: "Oct 28, 2025"
     */
    fun formatLocalDateForDisplay(date: LocalDate): String {
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "$monthName ${date.day}, ${date.year}"
    }
}