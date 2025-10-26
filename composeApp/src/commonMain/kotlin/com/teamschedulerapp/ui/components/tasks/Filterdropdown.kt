package com.teamschedulerapp.ui.components.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FilterOption(
    val priority: Set<String> = setOf(),
    val status: Set<String> = setOf()
)

@Composable
fun FilterDropdown(
    selectedFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = filterExpanded,
        onExpandedChange = { filterExpanded = it },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (selectedFilter.priority.isNotEmpty() || selectedFilter.status.isNotEmpty())
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedFilter.priority.isNotEmpty() || selectedFilter.status.isNotEmpty()) {
                    val count = selectedFilter.priority.size + selectedFilter.status.size
                    "Filter ($count)"
                } else "Filter"
            )
        }

        ExposedDropdownMenu(
            expanded = filterExpanded,
            onDismissRequest = { filterExpanded = false },
            modifier = Modifier
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            // Clear All Button
            OutlinedButton(
                onClick = {
                    onFilterChange(FilterOption())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                enabled = selectedFilter.priority.isNotEmpty() || selectedFilter.status.isNotEmpty(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray,
                ),
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Clear All",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Priority Section
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            listOf(
                "high" to "High",
                "medium" to "Medium",
                "low" to "Low"
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
                        onFilterChange(
                            selectedFilter.copy(
                                priority = if (value in selectedFilter.priority) {
                                    selectedFilter.priority - value
                                } else {
                                    selectedFilter.priority + value
                                }
                            )
                        )
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = value in selectedFilter.priority,
                            onCheckedChange = { checked ->
                                onFilterChange(
                                    selectedFilter.copy(
                                        priority = if (checked) {
                                            selectedFilter.priority + value
                                        } else {
                                            selectedFilter.priority - value
                                        }
                                    )
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
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
                "pending" to "Pending",
                "in progress" to "In Progress",
                "blocked" to "Blocked",
                "done" to "Done"
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
                        onFilterChange(
                            selectedFilter.copy(
                                status = if (value in selectedFilter.status) {
                                    selectedFilter.status - value
                                } else {
                                    selectedFilter.status + value
                                }
                            )
                        )
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = value in selectedFilter.status,
                            onCheckedChange = { checked ->
                                onFilterChange(
                                    selectedFilter.copy(
                                        status = if (checked) {
                                            selectedFilter.status + value
                                        } else {
                                            selectedFilter.status - value
                                        }
                                    )
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                    }
                )
            }
        }
    }
}