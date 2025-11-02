@file:OptIn(ExperimentalMaterial3Api::class)
package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
@Composable
fun InputTimePicker(
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true,
    initialHour: Int? = null,
    initialMinute: Int? = null,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour ?: currentTime.hour,
        initialMinute = initialMinute ?: currentTime.minute,
        is24Hour = is24Hour
    )

    Column(modifier = modifier.padding(16.dp)) {
        TimeInput(state = timePickerState)

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(dismissLabel)
            }

            Button(
                onClick = { onConfirm(timePickerState) },
                modifier = Modifier.weight(1f)
            ) {
                Text(confirmLabel)
            }
        }
    }
}

