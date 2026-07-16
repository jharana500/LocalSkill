package com.example.localskill.view.company.applicants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.ApplicantEvent
import com.example.localskill.viewmodel.ApplicantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantDetailsScreen(
    viewModel: ApplicantViewModel,
    applicationId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    var showRejectDialog by remember { mutableStateOf(false) }
    var showInterviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(applicationId) { viewModel.loadApplicantDetails(applicationId) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ApplicantEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Applicant", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val application = uiState.application
        if (uiState.isLoading || application == null) {
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
                Column {
                    Text(
                        text = uiState.applicantUser?.fullName ?: "Applicant",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.applicantProfile?.headline.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(text = application.status.replace('_', ' '))
            }

            Text(
                text = "Applied for ${application.jobTitle}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Text(
                text = "Applied ${DateUtils.formatDate(application.appliedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val skills = uiState.applicantProfile?.skills.orEmpty()
            if (skills.isNotEmpty()) {
                Text(
                    text = "Skills",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = Spacing.md)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(skills) { skill -> StatusChip(text = skill.name) }
                }
            }

            if (application.coverLetter.isNotBlank()) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(
                        text = "Cover letter",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.coverLetter,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }

            LocalSkillSecondaryButton(
                text = "View resume",
                onClick = { if (application.resumeUrl.isNotBlank()) uriHandler.openUri(application.resumeUrl) },
                enabled = application.resumeUrl.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md)
            )

            if (application.companyMessage.isNotBlank()) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(
                        text = "Your message",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.companyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }

            if (application.status == ApplicationStatus.INTERVIEW.name && application.interviewDate != null) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(text = "Interview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${DateUtils.formatInterviewDateTime(application.interviewDate)} · ${application.interviewLocation}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }

            StatusActions(
                application = application,
                isUpdating = uiState.isUpdatingStatus,
                onAdvance = { next -> viewModel.updateStatus(next) },
                onReject = { showRejectDialog = true },
                onScheduleInterview = { showInterviewDialog = true }
            )
        }
    }

    if (showRejectDialog) {
        RejectDialog(
            onConfirm = { message ->
                viewModel.rejectApplication(message)
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    if (showInterviewDialog) {
        val datePickerState = rememberDatePickerState()
        var location by remember { mutableStateOf("") }
        DatePickerDialog(
            onDismissRequest = { showInterviewDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        viewModel.scheduleInterview(date, location)
                    }
                    showInterviewDialog = false
                }) { Text("Schedule") }
            },
            dismissButton = {
                TextButton(onClick = { showInterviewDialog = false }) { Text("Cancel") }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)
                LocalSkillTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location or meeting link",
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
            }
        }
    }
}

@Composable
private fun StatusActions(
    application: ApplicationModel,
    isUpdating: Boolean,
    onAdvance: (String) -> Unit,
    onReject: () -> Unit,
    onScheduleInterview: () -> Unit
) {
    val nextStatus = when (application.status) {
        ApplicationStatus.APPLIED.name -> ApplicationStatus.UNDER_REVIEW.name
        ApplicationStatus.UNDER_REVIEW.name -> ApplicationStatus.SHORTLISTED.name
        ApplicationStatus.INTERVIEW.name -> ApplicationStatus.HIRED.name
        else -> null
    }
    val canReject = ApplicationModel.isValidCompanyTransition(application.status, ApplicationStatus.REJECTED.name)
    val canScheduleInterview = application.status == ApplicationStatus.SHORTLISTED.name

    Column(modifier = Modifier.padding(top = Spacing.lg)) {
        if (nextStatus != null) {
            LocalSkillPrimaryButton(
                text = "Move to ${nextStatus.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
                onClick = { onAdvance(nextStatus) },
                isLoading = isUpdating,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (canScheduleInterview) {
            LocalSkillSecondaryButton(
                text = "Schedule interview",
                onClick = onScheduleInterview,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
            )
        }
        if (canReject) {
            LocalSkillDestructiveButton(
                text = "Reject application",
                onClick = onReject,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
            )
        }
    }
}

@Composable
private fun RejectDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject application?") },
        text = {
            LocalSkillTextField(
                value = message,
                onValueChange = { message = it },
                label = "Message to applicant (optional)",
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(message) }) { Text("Reject") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
