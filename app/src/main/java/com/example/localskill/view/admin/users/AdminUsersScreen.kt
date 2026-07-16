package com.example.localskill.view.admin.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.AccountStatus
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminUserEvent
import com.example.localskill.viewmodel.AdminUserViewModel

@Composable
fun AdminUsersScreen(
    viewModel: AdminUserViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSuspendUser by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(Unit) { viewModel.loadUsers() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminUserEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Users", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LocalSkillTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = "Search by name or email",
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(UserRole.entries) { role ->
                    FilterChip(
                        selected = uiState.roleFilter == role,
                        onClick = { viewModel.setRoleFilter(if (uiState.roleFilter == role) null else role) },
                        label = { Text(role.name.replace('_', ' ')) }
                    )
                }
                items(AccountStatus.entries) { status ->
                    FilterChip(
                        selected = uiState.statusFilter == status,
                        onClick = { viewModel.setStatusFilter(if (uiState.statusFilter == status) null else status) },
                        label = { Text(status.name) }
                    )
                }
            }

            val filtered = uiState.filtered

            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.errorMessage != null && uiState.users.isEmpty() ->
                    ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(Spacing.lg))

                filtered.isEmpty() -> EmptyState(title = "No users found", description = "Try a different search or filter.")

                else -> LazyColumn(
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(filtered, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            isSelf = user.id == uiState.currentAdminId,
                            onSuspend = { pendingSuspendUser = user },
                            onReactivate = { viewModel.reactivateUser(user.id) }
                        )
                    }
                }
            }
        }
    }

    pendingSuspendUser?.let { user ->
        ConfirmationDialog(
            title = "Suspend ${user.fullName}?",
            message = "This account will lose access to the platform until reactivated.",
            confirmLabel = "Suspend",
            onConfirm = {
                viewModel.suspendUser(user.id)
                pendingSuspendUser = null
            },
            onDismiss = { pendingSuspendUser = null }
        )
    }
}

@Composable
private fun UserRow(
    user: UserModel,
    isSelf: Boolean,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit
) {
    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${user.role.replace('_', ' ')} · Joined ${DateUtils.formatDate(user.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = user.accountStatus, tone = statusTone(user.accountStatus))
        }
        if (!isSelf) {
            Row(modifier = Modifier.padding(top = Spacing.xs)) {
                if (user.accountStatus == AccountStatus.SUSPENDED.name) {
                    TextButton(onClick = onReactivate) { Text("Reactivate") }
                } else {
                    TextButton(onClick = onSuspend) { Text("Suspend") }
                }
            }
        }
    }
}

private fun statusTone(status: String): StatusChipTone = when (status) {
    AccountStatus.ACTIVE.name -> StatusChipTone.SUCCESS
    AccountStatus.SUSPENDED.name -> StatusChipTone.ERROR
    else -> StatusChipTone.WARNING
}
