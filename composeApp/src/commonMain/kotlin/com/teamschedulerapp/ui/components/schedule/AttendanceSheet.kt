@file:OptIn(ExperimentalMaterial3Api::class)
package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AttendanceSheet(
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
    if (!visible) return

    // picker state
    val fromState = rememberTimePickerState(
        initialHour = (initialFrom ?: LocalTime(9, 0)).hour,
        initialMinute = (initialFrom ?: LocalTime(9, 0)).minute,
        is24Hour = is24Hour
    )
    val toState = rememberTimePickerState(
        initialHour = (initialTo ?: LocalTime(17, 0)).hour,
        initialMinute = (initialTo ?: LocalTime(17, 0)).minute,
        is24Hour = is24Hour
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add attendance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            // date label
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = date.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabeledField(label = "From", labelWidth = 32.dp) { InputTimePicker(state = fromState, modifier = Modifier.scale(0.85f)) }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    LabeledField(label = "To", labelWidth = 32.dp) { InputTimePicker(state = toState, modifier = Modifier.scale(0.85f)) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
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
            Spacer(Modifier.height(4.dp))
        }
    }
}


@Composable
private fun LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 39.dp,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(39.dp)
        )
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            content()
        }
    }
}