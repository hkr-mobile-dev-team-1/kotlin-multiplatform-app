package com.teamschedulerapp.ui.components.schedule

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(
                text = dialogText,
                textAlign = TextAlign.Center
                )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}