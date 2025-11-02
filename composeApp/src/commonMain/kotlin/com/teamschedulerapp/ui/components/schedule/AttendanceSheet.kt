@file:OptIn(ExperimentalMaterial3Api::class)

package com.teamschedulerapp.ui.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AttendanceRightSheet(
    visible: Boolean,
    date: LocalDate,
    displayName: String,
    initialFrom: LocalTime?,
    initialTo: LocalTime?,
    is24Hour: Boolean = true,
    confirmLabel: String = "Save",
    dismissLabel: String = "Cancel",
    onDismiss: () -> Unit,
    onConfirm: (from: LocalTime?, to: LocalTime?) -> Unit,
) {
    // default to current time if time not provided
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time }

    // two independent picker states
    val fromState = rememberTimePickerState(
        initialHour = (initialFrom ?: now).hour,
        initialMinute = (initialFrom ?: now).minute,
        is24Hour = is24Hour
    )
    val toState = rememberTimePickerState(
        initialHour = (initialTo ?: now).hour,
        initialMinute = (initialTo ?: now).minute,
        is24Hour = is24Hour
    )

    val dateLabel = remember(date) {
        val dow = date.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }.take(3)
        val y = date.year
        val m = date.month.number.toString().padStart(2, '0')
        val d = date.day.toString().padStart(2, '0')
        "$dow, $y-$m-$d"
    }

    // sliding panel
    Box(Modifier.fillMaxSize()) {
        // scrim
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onDismiss)
            )
        }

        // panel
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(tween(220)) { it },   // from right
            exit = slideOutHorizontally(tween(200)) { it }    // to right
        ) {
            Surface(
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 320.dp, max = 420.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Add attendance", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // FROM
                    Text("From", style = MaterialTheme.typography.labelLarge)
                    TimeInput(state = fromState)

                    // TO
                    Text("To", style = MaterialTheme.typography.labelLarge)
                    TimeInput(state = toState)

                    Spacer(Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) { Text(dismissLabel) }

                        Button(
                            onClick = {
                                val from = LocalTime(fromState.hour, fromState.minute)
                                val to   = LocalTime(toState.hour, toState.minute)
                                onConfirm(from, to)
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(confirmLabel) }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeField(state: TimePickerState, modifier: Modifier = Modifier) {
    TimeInput(state = state)
}