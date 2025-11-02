package com.teamschedulerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Snackbar

/**
 * Enum to represent different snackbar types
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO
}

/**
 * Custom Snackbar Host
 */
@Composable
fun CustomSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            // Extract type from message prefix (format: "TYPE:message")
            val snackbarType = when {
                data.visuals.message.startsWith("SUCCESS:") -> SnackbarType.SUCCESS
                data.visuals.message.startsWith("ERROR:") -> SnackbarType.ERROR
                else -> SnackbarType.INFO
            }

            val actualMessage = data.visuals.message.substringAfter(":", data.visuals.message)

            CustomSnackbar(
                snackbarData = data,
                type = snackbarType,
                message = actualMessage
            )
        }
    )
}

/**
 * Custom Snackbar with icon and themed colors
 */
@Composable
private fun CustomSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType,
    message: String,
    modifier: Modifier = Modifier
) {
    val (icon, containerColor, contentColor, iconColor) = when (type) {
        SnackbarType.SUCCESS -> listOf(
            Icons.Default.CheckCircle,
            Color(0xFFdff3da),
            Color(0xFF217e25),
            Color(0xFF217e25)  // Green icon
        )
        SnackbarType.ERROR -> listOf(
            Icons.Default.Error,
            Color(0xFFf8e0db),
            Color(0xFFd90f14),
            Color(0xFFd90f14)  // Red icon
        )
        SnackbarType.INFO -> listOf(
            Icons.Default.Info,
            Color(0xFFe0e7ee),
            Color(0xFF0b53ae),
            Color(0xFF0b53ae)  // Blue icon
        )
    }

    Snackbar(
        modifier = modifier.padding(8.dp),
        action = {
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(actionLabel)
                }
            }
        },
        dismissAction = {
            if (snackbarData.visuals.withDismissAction) {
                IconButton(
                    onClick = { snackbarData.dismiss() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        containerColor = containerColor as Color,
        contentColor = contentColor as Color,
        actionContentColor = Color.White,
        dismissActionContentColor = contentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon as ImageVector,
                contentDescription = null,
                tint = iconColor as Color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}