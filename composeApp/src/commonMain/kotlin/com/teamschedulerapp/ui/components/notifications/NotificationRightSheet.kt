package com.teamschedulerapp.ui.components.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.teamschedulerapp.model.Notification
import com.teamschedulerapp.ui.components.ConfirmationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationRightSheet(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    onDeleteNotification: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearAllDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val unreadCount = notifications.count { !it.isRead }

    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Trigger animation on mount
    LaunchedEffect(Unit) {
        visible = true
    }

    // Filter notifications based on selected tab
    val filteredNotifications = when (selectedTab) {
        0 -> notifications // All
        1 -> notifications.filter { it.type == "task_assignment" }
        2 -> notifications.filter { it.type == "task_completed" }
        else -> notifications
    }

    // Handle dismiss with animation
    fun handleDismiss() {
        scope.launch {
            visible = false
            delay(300) // Wait for animation to complete
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim/Overlay
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        onClick = { handleDismiss() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .zIndex(1f)
            )
        }

        // Side Sheet - Slides from right
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { it }, // Start from right (full width off screen)
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it }, // Exit to right
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth() // Full width
                    .zIndex(2f),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with back arrow, title, and close icon
                    Surface(
                        tonalElevation = 0.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column (
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back arrow
                                IconButton(onClick = { handleDismiss() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Title
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Close icon
                                IconButton(onClick = { handleDismiss() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Tabs
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                indicator = { tabPositions ->
                                    if (selectedTab < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                            color = MaterialTheme.colorScheme.primary,
                                            height = 3.dp
                                        )
                                    }
                                },
                                divider = {}
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "All",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (selectedTab == 0)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = notifications.size.toString(),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selectedTab == 0)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "Tasks",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (selectedTab == 1)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = notifications.count { it.type == "task" }.toString(),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selectedTab == 1)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Tab(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "Teams",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (selectedTab == 2)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = notifications.count { it.type == "team" }.toString(),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selectedTab == 2)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Content
                    if (filteredNotifications.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You're all caught up!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        // Notification list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(filteredNotifications, key = { it.id }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onNotificationClick = onNotificationClick,
                                    onDeleteClick = onDeleteNotification
                                )
                            }

                            // Footer buttons
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onMarkAllAsRead,
                                        modifier = Modifier.weight(1f),
                                        enabled = unreadCount > 0
                                    ) {
                                        Text("Mark all as read")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear all confirmation dialog
    ConfirmationDialog(
        showDialog = showClearAllDialog,
        title = "Clear All Notifications",
        message = "Are you sure you want to clear all notifications? This action cannot be undone.",
        onConfirm = {
            onClearAll()
            handleDismiss()
        },
        onDismiss = { showClearAllDialog = false }
    )
}