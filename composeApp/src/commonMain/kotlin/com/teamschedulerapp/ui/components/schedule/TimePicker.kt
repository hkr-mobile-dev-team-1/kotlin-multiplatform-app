@file:OptIn(ExperimentalMaterial3Api::class)
package com.teamschedulerapp.ui.components.schedule

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@Composable
fun InputTimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier,
    scale: Float = 0.7f
) {
    TimeInput(
        state = state,
        modifier = modifier.scale(scale)
    )
}

