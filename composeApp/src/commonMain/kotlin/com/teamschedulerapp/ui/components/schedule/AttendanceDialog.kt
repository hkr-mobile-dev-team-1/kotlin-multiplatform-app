package com.teamschedulerapp.ui.components.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


@Composable
fun AttendanceDialog(
    date: LocalDate,
    onConfirm: (name: String, from: LocalTime?, to: LocalTime?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fromText by remember { mutableStateOf("") }
    var toText by remember { mutableStateOf("") }

    // validation flags
    val fromTime = remember(fromText) { parseTimeOrNull(fromText) }
    val toTime   = remember(toText)   { parseTimeOrNull(toText) }
    val fromErr  = fromText.isNotBlank() && fromTime == null
    val toErr    = toText.isNotBlank() && toTime == null
    val orderErr = (fromTime != null && toTime != null && fromTime > toTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark attendance") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Date: $date", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fromText, onValueChange = { fromText = it },
                    label = { Text("From (HH:MM)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = fromErr,
                    supportingText = {
                        if (fromErr) Text("Use 00–23:00–59 (e.g., 09:30)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = toText, onValueChange = { toText = it },
                    label = { Text("To (HH:MM)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = toErr || (!toErr && orderErr),
                    supportingText = {
                        when {
                            toErr -> Text("Use 00–23:00–59 (e.g., 17:45)")
                            orderErr -> Text("End time must be after start time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val canConfirm = name.isNotBlank() && !fromErr && !toErr && !orderErr
            TextButton(
                enabled = canConfirm,
                onClick = { onConfirm(name.trim(), fromTime, toTime) }
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// strict time parser
private val HHMM = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
private fun parseTimeOrNull(text: String): LocalTime? {
    val t = text.trim()
    if (t.isEmpty()) return null
    val m = HHMM.matchEntire(t) ?: return null
    val (h, min) = m.destructured
    return LocalTime(h.toInt(), min.toInt())
}
