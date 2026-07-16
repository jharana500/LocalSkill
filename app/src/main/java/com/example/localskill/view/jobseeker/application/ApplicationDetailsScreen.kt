package com.example.localskill.view.jobseeker.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.ApplicationModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.ApplicationEvent
import com.example.localskill.viewmodel.ApplicationViewModel

@Composable
fun ApplicationDetailsScreen(
    viewModel: ApplicationViewModel,
    applicationId: String,
    onBack: () -> Unit,
    onViewJobClick: (String) -> Unit
) {
    val uiState by viewModel.applicationDetailsUiState.collectAsStateWithLifecycle()
    var showWithdrawConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(applicationId) {
        viewModel.loadApplicationDetails(applicationId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ApplicationEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Application Details", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())

            uiState.notFound || uiState.application == null -> EmptyState(
                title = "Application not found",
                description = uiState.errorMessage ?: "This application may have been removed.",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )

            else -> ApplicationDetailsContent(
                application = uiState.application!!,
                isWithdrawing = uiState.isWithdrawing,
                onWithdrawClick = { showWithdrawConfirmation = true },
                onViewJobClick = { onViewJobClick(uiState.application!!.jobId) },
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        }
    }

    if (showWithdrawConfirmation) {
        ConfirmationDialog(
            title = "Withdraw application?",
            message = "You won't be able to undo this. The employer will see this application as withdrawn.",
            confirmLabel = "Withdraw",
            onConfirm = {
                showWithdrawConfirmation = false
                viewModel.withdrawApplication()
            },
            onDismiss = { showWithdrawConfirmation = false }
        )
    }
}

@Composable
private fun ApplicationDetailsContent(
    application: ApplicationModel,
    isWithdrawing: Boolean,
    onWithdrawClick: () -> Unit,
    onViewJobClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        LocalSkillCard {
            ApplicationHeader(application = application, onViewJobClick = onViewJobClick)
        }

        SectionHeader(title = "Status", modifier = Modifier.padding(top = Spacing.lg))
        LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
            StatusChip(text = application.status.replace('_', ' '))
            Text(
                text = "Last updated ${DateUtils.formatDate(application.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            if (application.companyMessage.isNotBlank()) {
                Text(
                    text = application.companyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }
            if (application.interviewDate != null) {
                Text(
                    text = "Interview: ${DateUtils.formatDate(application.interviewDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }
        }

        if (application.coverLetter.isNotBlank()) {
            SectionHeader(title = "Cover letter", modifier = Modifier.padding(top = Spacing.lg))
            Text(
                text = application.coverLetter,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        if (application.status in ApplicationModel.WITHDRAWABLE_STATUSES) {
            LocalSkillDestructiveButton(
                text = "Withdraw application",
                onClick = onWithdrawClick,
                enabled = !isWithdrawing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl)
            )
        }
    }
}

@Composable
private fun ApplicationHeader(application: ApplicationModel, onViewJobClick: () -> Unit) {
    Column {
        Text(text = application.jobTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = application.companyName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Applied ${DateUtils.formatDate(application.appliedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xxs)
        )
        androidx.compose.material3.TextButton(onClick = onViewJobClick, modifier = Modifier.padding(top = Spacing.xs)) {
            Text("View job posting")
        }
    }
}
