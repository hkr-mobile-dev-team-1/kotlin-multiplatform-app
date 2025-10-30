package com.teamschedulerapp.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/**
 * Extension functions for showing snackbars with icons
 */
suspend fun SnackbarHostState.showSuccessSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        message = "SUCCESS:$message",
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = true
    )
}

suspend fun SnackbarHostState.showErrorSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        message = "ERROR:$message",
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = true
    )
}

suspend fun SnackbarHostState.showInfoSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        message = "INFO:$message",
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = true
    )
}