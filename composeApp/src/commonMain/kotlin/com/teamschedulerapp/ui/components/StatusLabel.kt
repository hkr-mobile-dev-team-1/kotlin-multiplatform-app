package com.teamschedulerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamschedulerapp.model.TaskStatus

@Composable
fun StatusLabel(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.PENDING -> Color(0xFFFFDA95)     // Soft Yellow
        TaskStatus.IN_PROGRESS -> Color(0xFFB5AFFF) // Soft Blue/Purple
        TaskStatus.DONE -> Color(0xFF4CAF50)        // Green
        TaskStatus.BLOCKED -> Color(0xFFFCAEAF)     // Red/Pink
    }

    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            color = Color.Black,
            style = MaterialTheme.typography.labelSmall
        )
    }
}