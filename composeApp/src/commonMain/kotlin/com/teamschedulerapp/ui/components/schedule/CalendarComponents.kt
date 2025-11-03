package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarDay
import kotlinx.datetime.DayOfWeek


@Composable
fun DayCell(
    day: CalendarDay,
    isOverflow: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    headcount: Int,
    onClick: () -> Unit
) {
    // cell bg colors
    val bg = when {
        isOverflow -> MaterialTheme.colorScheme.surface
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday    -> MaterialTheme.colorScheme.surfaceVariant
        else       -> MaterialTheme.colorScheme.surface
    }

    // text color for calendar days (dim overflow)
    val dayNumberColor = if (isOverflow)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.onSurface

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isSelected && !isOverflow) 2.dp else 0.dp,
        modifier = Modifier.aspectRatio(1f).clickable(enabled = !isOverflow, onClick = onClick)
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
            Text(
                day.date.day.toString(),
                modifier = Modifier.align(Alignment.Center),
                color = dayNumberColor
            )
            if (!isOverflow && headcount > 0) {
                Text(
                    headcount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

// setup days of week titles lib
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { dow ->
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dow.name.lowercase().replaceFirstChar { it.titlecase() }.take(3),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun MonthHeader(state: CalendarState, modifier: Modifier = Modifier) {
    // visible month as user swipes - reactive
    val visibleMonth by remember(state) {
        derivedStateOf { state.firstVisibleMonth.yearMonth }
    }

    val monthTitle = "${visibleMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }} • ${visibleMonth.year}"

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 25.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) { Text(monthTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,)
        }
    }
}


