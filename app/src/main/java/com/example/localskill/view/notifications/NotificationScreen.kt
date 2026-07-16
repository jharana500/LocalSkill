package com.example.localskill.view.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.model.UserRole
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.NotificationEvent
import com.example.localskill.viewmodel.NotificationViewModel

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    role: UserRole,
    onBack: () -> Unit,
    onOpenRoute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationEvent.Navigate -> onOpenRoute(event.route)
                is NotificationEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            LocalSkillTopAppBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = viewModel::markAllRead,
                        enabled = uiState.unreadCount > 0
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Mark all notifications as read")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.notifications.isEmpty() -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                uiState.errorMessage != null && uiState.notifications.isEmpty() -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ErrorMessage(message = uiState.errorMessage.orEmpty())
                    Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = Spacing.md)) {
                        Text("Retry")
                    }
                }

                uiState.notifications.isEmpty() -> EmptyState(
                    title = "No notifications yet",
                    description = "Important application, verification, and moderation updates will appear here.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            role = role,
                            onClick = { viewModel.openNotification(notification, role) },
                            onMarkRead = { viewModel.markRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationModel,
    role: UserRole,
    onClick: () -> Unit,
    onMarkRead: () -> Unit
) {
    val accent = roleAccent(role)
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = notificationAccessibilityText(notification) }
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (notification.isRead) 0.12f else 0.22f))
                    .padding(Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Icon(notificationIcon(notification), contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notificationTitle(notification),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accent)
                                .padding(Spacing.xxs)
                        )
                    }
                }
                Text(
                    text = notificationMessage(notification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
                Text(
                    text = DateUtils.formatRelative(notification.createdAt).ifBlank { "Recently" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                if (!notification.isRead) {
                    Text(
                        text = "Mark as read",
                        color = accent,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(top = Spacing.sm)
                            .clickable(onClick = onMarkRead)
                    )
                }
            }
        }
    }
}

private fun notificationTitle(notification: NotificationModel): String = when (notification.typedType) {
    NotificationType.APPLICATION_SUBMITTED -> "Application submitted"
    NotificationType.APPLICATION_UNDER_REVIEW -> "Application under review"
    NotificationType.APPLICATION_SHORTLISTED -> "You were shortlisted"
    NotificationType.INTERVIEW_SCHEDULED -> "Interview scheduled"
    NotificationType.APPLICATION_HIRED -> "Application hired"
    NotificationType.APPLICATION_REJECTED -> "Application update"
    NotificationType.APPLICATION_WITHDRAWN_CONFIRMATION -> "Application withdrawn"
    NotificationType.NEW_APPLICATION -> "New application received"
    NotificationType.APPLICATION_WITHDRAWN -> "Application withdrawn"
    NotificationType.VERIFICATION_SUBMITTED_CONFIRMATION -> "Verification submitted"
    NotificationType.COMPANY_VERIFIED -> "Company verified"
    NotificationType.COMPANY_VERIFICATION_REJECTED -> "Verification rejected"
    NotificationType.JOB_MODERATED -> "Job removed from discovery"
    NotificationType.JOB_RESTORED -> "Job restored"
    NotificationType.NEW_VERIFICATION_REQUEST -> "New verification request"
    NotificationType.COMPANY_VERIFICATION_RESUBMITTED -> "Verification resubmitted"
    NotificationType.NEW_JOB_REPORT -> "New job report"
    NotificationType.REPORT_RESOLVED -> "Report resolved"
    NotificationType.REPORT_REJECTED -> "Report rejected"
    null -> "Notification"
}

private fun notificationMessage(notification: NotificationModel): String = when (notification.typedType) {
    NotificationType.INTERVIEW_SCHEDULED -> "Open this notification to review the interview details."
    NotificationType.NEW_APPLICATION -> "A candidate applied to one of your jobs."
    NotificationType.NEW_VERIFICATION_REQUEST -> "A company is waiting for verification review."
    NotificationType.NEW_JOB_REPORT -> "A user submitted a report for moderation."
    NotificationType.COMPANY_VERIFIED -> "Your company can now publish jobs."
    NotificationType.COMPANY_VERIFICATION_REJECTED -> "Review the reason and resubmit when ready."
    NotificationType.JOB_MODERATED -> "An administrator removed this job from discovery."
    NotificationType.JOB_RESTORED -> "This job is visible in discovery again."
    else -> "Open this notification to view the related record."
}

private fun notificationIcon(notification: NotificationModel): ImageVector = when (notification.typedType) {
    NotificationType.NEW_APPLICATION,
    NotificationType.APPLICATION_SUBMITTED,
    NotificationType.APPLICATION_UNDER_REVIEW,
    NotificationType.APPLICATION_SHORTLISTED,
    NotificationType.INTERVIEW_SCHEDULED,
    NotificationType.APPLICATION_HIRED,
    NotificationType.APPLICATION_REJECTED,
    NotificationType.APPLICATION_WITHDRAWN,
    NotificationType.APPLICATION_WITHDRAWN_CONFIRMATION -> Icons.Default.Work

    NotificationType.COMPANY_VERIFIED,
    NotificationType.COMPANY_VERIFICATION_REJECTED,
    NotificationType.NEW_VERIFICATION_REQUEST,
    NotificationType.COMPANY_VERIFICATION_RESUBMITTED,
    NotificationType.VERIFICATION_SUBMITTED_CONFIRMATION -> Icons.Default.Business

    else -> Icons.Default.Campaign
}

private fun roleAccent(role: UserRole): Color = when (role) {
    UserRole.JOB_SEEKER -> Color(0xFF2563EB)
    UserRole.COMPANY -> Color(0xFF16A34A)
    UserRole.ADMIN -> Color(0xFF7C3AED)
}

private fun notificationAccessibilityText(notification: NotificationModel): String =
    listOf(
        if (notification.isRead) "Read notification" else "Unread notification",
        notificationTitle(notification),
        DateUtils.formatRelative(notification.createdAt)
    ).filter { it.isNotBlank() }.joinToString(separator = ", ")
