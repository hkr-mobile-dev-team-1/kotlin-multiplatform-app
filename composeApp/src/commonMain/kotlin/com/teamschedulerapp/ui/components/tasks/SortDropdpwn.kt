package com.teamschedulerapp.ui.components.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SortDropdown(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = sortExpanded,
        onExpandedChange = { sortExpanded = it },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedSort) {
                        "priority_high_low" -> "Priority"
                        "priority_low_high" -> "Priority"
                        "status_pending_first" -> "Status"
                        "status_done_first" -> "Status"
                        "due_date_nearest" -> "Due Date"
                        "due_date_furthest" -> "Due Date"
                        else -> "Sort"
                    }
                )

                // Show arrow icon based on sort direction
                when (selectedSort) {
                    "priority_high_low", "status_done_first", "due_date_furthest" -> {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Descending",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    "priority_low_high", "status_pending_first", "due_date_nearest" -> {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Ascending",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        ExposedDropdownMenu(
            expanded = sortExpanded,
            onDismissRequest = { sortExpanded = false },
            modifier = Modifier
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            // Priority Section
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            listOf(
                "priority_high_low" to "High to Low",
                "priority_low_high" to "Low to High"
            ).forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSortChange(value)
                        sortExpanded = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = selectedSort == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = Color.Gray
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Status Section
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            listOf(
                "status_pending_first" to "Pending First",
                "status_done_first" to "Done First"
            ).forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSortChange(value)
                        sortExpanded = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = selectedSort == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = Color.Gray
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Due Date Section
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            listOf(
                "due_date_nearest" to "Nearest First",
                "due_date_furthest" to "Furthest First"
            ).forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSortChange(value)
                        sortExpanded = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = selectedSort == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = Color.Gray
                            )
                        )
                    }
                )
            }
        }
    }
}