package com.teamschedulerapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
    onMakeAdmin: (TeamMemberWithUser) -> Unit,
    onInviteMember: (String) -> Unit // now takes a String (user ID or email)
) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var newMemberInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {
            // --- Add New Member Tile ---
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showInviteDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Member",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Invite New Member",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Team Members",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )

            if (team.members.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No members in this team yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column {
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

        // --- Add Member Dialog ---
        if (showInviteDialog) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Invite New Member") },
                text = {
                    OutlinedTextField(
                        value = newMemberInput,
                        onValueChange = { newMemberInput = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newMemberInput.isNotBlank()) {
                                onInviteMember(newMemberInput.trim())
                                showInviteDialog = false
                                newMemberInput = ""
                            }
                        }
                    ) {
                        Text("Invite")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fullName = listOfNotNull(member.firstName, member.lastName)
                .joinToString(" ")
                .ifBlank { "Unknown" }

            Text(
                text = fullName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

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
