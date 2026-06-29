package com.example.localskill.view.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.LoadingState
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField
import com.example.localskill.viewmodel.worker.JobDetailsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JobDetailsScreen(
    state: JobDetailsUiState,
    onMessageChange: (String) -> Unit,
    onApplyClick: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.isLoading) {
            LoadingState()
        }
        ErrorMessage(state.errorMessage)
        state.job?.let { job ->
            JobDetailsContent(
                job = job,
                applicationMessage = state.applicationMessage,
                hasApplied = state.hasApplied,
                isApplying = state.isApplying,
                successMessage = state.successMessage,
                onMessageChange = onMessageChange,
                onApplyClick = onApplyClick
            )
        }
        if (!state.isLoading && state.job == null && state.errorMessage == null) {
            EmptyState("Job details are not available.")
        }
    }
}

@Composable
private fun JobDetailsContent(
    job: JobModel,
    applicationMessage: String,
    hasApplied: Boolean,
    isApplying: Boolean,
    successMessage: String?,
    onMessageChange: (String) -> Unit,
    onApplyClick: () -> Unit
) {
    Text(job.title, style = MaterialTheme.typography.headlineMedium)
    Text("${job.jobType} - ${job.status}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
    DetailSection("Description", job.description)
    DetailSection("Required skills", job.requiredSkills)
    DetailSection("Budget or salary", job.budget)
    DetailSection("Location", job.location)
    if (job.deadline.isNotBlank()) DetailSection("Deadline", job.deadline)
    if (job.createdAt > 0L) DetailSection("Posted", job.createdAt.formatDate())
    DetailSection("Employer", job.employerId)
    if (!hasApplied && job.status == JobStatus.OPEN.name) {
        LocalSkillTextField(
            value = applicationMessage,
            onValueChange = onMessageChange,
            label = "Message to employer",
            minLines = 3
        )
        Text("Explain why you are suitable for this job.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    Spacer(Modifier.height(4.dp))
    LocalSkillButton(
        text = when {
            hasApplied -> "Already Applied"
            job.status != JobStatus.OPEN.name -> "Applications Closed"
            else -> "Apply Now"
        },
        isLoading = isApplying,
        onClick = onApplyClick,
        enabled = !hasApplied && job.status == JobStatus.OPEN.name
    )
}

@Composable
private fun DetailSection(title: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Long.formatDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))
