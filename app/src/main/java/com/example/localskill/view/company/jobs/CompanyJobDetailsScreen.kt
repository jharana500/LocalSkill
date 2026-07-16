package com.example.localskill.view.company.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.utils.SalaryFormatter
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyJobEvent
import com.example.localskill.viewmodel.CompanyJobViewModel

@Composable
fun CompanyJobDetailsScreen(
    viewModel: CompanyJobViewModel,
    jobId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onViewApplicantsClick: (String) -> Unit
) {
    val jobsUiState by viewModel.jobsUiState.collectAsStateWithLifecycle()
    val job = jobsUiState.jobs.firstOrNull { it.id == jobId }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadJobs() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompanyJobEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Job details", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (job == null) {
            FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                StatusChip(text = job.status.replace('_', ' '))
            }
            Text(
                text = "${job.location} · ${SalaryFormatter.format(job.minimumSalary, job.maximumSalary, job.currency)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs)
            )
            Text(
                text = "${job.applicationCount} applicant${if (job.applicationCount == 1) "" else "s"} · Deadline ${DateUtils.formatDate(job.applicationDeadline)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs)
            )

            LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                Text(text = "Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = job.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
            }

            LocalSkillSecondaryButton(
                text = "View applicants (${job.applicationCount})",
                onClick = { onViewApplicantsClick(job.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md)
            )

            JobLifecycleActions(
                job = job,
                onEdit = { onEditClick(job.id) },
                onPublish = { onEditClick(job.id) },
                onClose = { viewModel.closeJob(job.id) },
                onReopen = { viewModel.reopenJob(job.id) },
                onDelete = { showDeleteConfirmation = true }
            )
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete draft?",
            message = "This draft job will be permanently deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteDraft(job!!.id)
                showDeleteConfirmation = false
                onBack()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun JobLifecycleActions(
    job: JobModel,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onClose: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.padding(top = Spacing.lg)) {
        when (job.status) {
            JobStatus.DRAFT.name -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    LocalSkillSecondaryButton(text = "Edit", onClick = onEdit, modifier = Modifier.weight(1f))
                    LocalSkillPrimaryButton(text = "Publish", onClick = onPublish, modifier = Modifier.weight(1f))
                }
                LocalSkillDestructiveButton(
                    text = "Delete draft",
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                )
            }

            JobStatus.ACTIVE.name -> {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    LocalSkillSecondaryButton(text = "Edit", onClick = onEdit, modifier = Modifier.weight(1f))
                    LocalSkillDestructiveButton(text = "Close job", onClick = onClose, modifier = Modifier.weight(1f))
                }
            }

            JobStatus.CLOSED.name -> {
                LocalSkillPrimaryButton(
                    text = if (job.isExpired) "Deadline passed — update to reopen" else "Reopen job",
                    onClick = onReopen,
                    enabled = !job.isExpired,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> Unit
        }
    }
}
