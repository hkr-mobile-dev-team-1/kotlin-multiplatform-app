package com.teamschedulerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable confirmation dialog
 *
 * @param showDialog Whether to show the dialog
 * @param title The title of the dialog (e.g., "Delete Task", "Delete Team")
 * @param message The confirmation message
 * @param itemName Optional name of the item being deleted (will be bolded in message)
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user cancels or dismisses
 */
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    itemName: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (itemName != null) {
                    Text(
                        text = message.replace("{item}", "\"$itemName\""),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            iconContentColor = MaterialTheme.colorScheme.error
        )
    }
}