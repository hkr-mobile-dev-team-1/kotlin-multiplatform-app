package com.teamschedulerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TaskStatus

@Composable
fun StatusLabel(status: TaskStatus) {
    val (backgroundColor, textColor) = when (status) {
        TaskStatus.PENDING -> Color(0xFFf2f2f2) to Color(0xFF595861)
        TaskStatus.IN_PROGRESS -> Color(0xFFf3f1d3) to Color(0xFFb29c30)
        TaskStatus.DONE -> Color(0xFFe6f6f0) to Color(0xFF1d8a5f)
        TaskStatus.BLOCKED -> Color(0xFFffe8ea) to Color(0xFFcd797b)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = "Priority",
                tint = textColor,
                modifier = Modifier.size(8.dp)
            )
            Text(
                text = status.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                color = textColor,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}