package com.teamschedulerapp.ui.components.tasks

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

@Composable
fun StatusLabel(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFf2f2f2) to Color(0xFF797979)
        "in progress" -> Color(0xFFffeecf) to Color(0xFFd98d00)
        "done" -> Color(0xFFcffcdb) to Color(0xFF189f3c)
        "blocked" -> Color(0xFFf8d9d6) to Color(0xFFcb2050)
        else -> Color(0xFFf8d9d6) to Color(0xFFcb2050)
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
                text = status.replaceFirstChar { it.uppercase() },
                color = textColor,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}