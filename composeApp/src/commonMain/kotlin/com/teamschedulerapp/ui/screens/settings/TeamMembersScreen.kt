package com.teamschedulerapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.TeamMemberWithUser
import com.teamschedulerapp.model.TeamWithMembers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersScreen(
    team: TeamWithMembers,
    onBack: () -> Unit,
    onRemoveMember: (TeamMemberWithUser) -> Unit,
    onMakeAdmin: (TeamMemberWithUser) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(team.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Team Members for ${team.name}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (team.members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No members in this team yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    team.members.forEach { member ->
                        TeamMemberItem(
                            member = member,
                            onRemoveMember = onRemoveMember,
                            onMakeAdmin = onMakeAdmin
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMemberItem(
    member: TeamMemberWithUser,
    onRemoveMember: (TeamMemberWithUser) -> Unit,
    onMakeAdmin: (TeamMemberWithUser) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Member name / email (TeamMemberWithUser has firstName/lastName/email)
            Column(modifier = Modifier.weight(1f)) {
                val fullName = listOfNotNull(member.firstName, member.lastName).joinToString(" ").ifBlank { "Unknown" }
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = member.email ?: "No email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Three-dot menu
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.width(220.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Remove User From Team",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            expanded = false
                            onRemoveMember(member)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Make User Admin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            expanded = false
                            onMakeAdmin(member)
                        }
                    )
                }
            }
        }
    }
}
