package com.example.localskill.view.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.NotificationModel
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LoadingState
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.NotificationCard
import com.example.localskill.viewmodel.notification.NotificationsUiState

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (NotificationModel) -> Unit,
    onDelete: (NotificationModel) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium)
            Text("Stay updated with your jobs, applications, and reviews.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${state.unreadCount} unread", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
        if (state.unreadCount > 0) {
            item { LocalSkillButton("Mark all as read", onClick = onMarkAllRead) }
        }
        if (state.isLoading) {
            item { LoadingState() }
        }
        item { ErrorMessage(state.errorMessage) }
        if (!state.isLoading && state.notifications.isEmpty()) {
            item {
                EmptyState("No notifications yet.\nImportant updates about jobs, applications, and reviews will appear here.")
            }
        } else {
            items(state.notifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification) },
                    onDelete = { onDelete(notification) }
                )
            }
        }
    }
}
