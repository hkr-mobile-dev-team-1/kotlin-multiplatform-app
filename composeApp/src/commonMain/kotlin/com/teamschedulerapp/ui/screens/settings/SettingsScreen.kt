package com.teamschedulerapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.model.User

@Composable
fun SettingsScreen(user: User) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {

            item {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    UserProfileSection(user)
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                SettingsSection(
                    title = "Appearance",
                    content = { ThemeSection() }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // New Notifications Section
            item {
                SettingsSection(
                    title = "Notifications",
                    content = { NotificationsSection() }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                SettingsSection(
                    title = "Account",
                    content = { LogoutSection(onLogout = { /* TODO: hook up Supabase logout */ }) }
                )
            }
        }
    }
}

@Composable
fun UserProfileSection(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile picture",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = user.name, style = MaterialTheme.typography.titleMedium)
        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = user.role,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun ThemeSection() {
    var isDarkMode by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isDarkMode,
            onCheckedChange = { isDarkMode = it }
        )
    }
}

@Composable
fun NotificationsSection() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }

    Column {
        //  Master switch — enables or disables all notifications
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable Notifications",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email notifications toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Email Notifications",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (notificationsEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = emailNotifications,
                onCheckedChange = { emailNotifications = it },
                enabled = notificationsEnabled
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Push notifications toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Push Notifications",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (notificationsEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = pushNotifications,
                onCheckedChange = { pushNotifications = it },
                enabled = notificationsEnabled
            )
        }
    }
}


@Composable
fun LogoutSection(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Log Out")
    }
}
