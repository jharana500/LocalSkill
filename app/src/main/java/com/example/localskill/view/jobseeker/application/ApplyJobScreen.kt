package com.example.localskill.view.jobseeker.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.utils.Constants
import com.example.localskill.utils.SalaryFormatter
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.ApplicationViewModel
import com.example.localskill.viewmodel.ApplyJobUiState

@Composable
fun ApplyJobScreen(
    viewModel: ApplicationViewModel,
    jobId: String,
    onBack: () -> Unit,
    onManageResumeClick: () -> Unit,
    onSubmissionSuccess: () -> Unit
) {
    val uiState by viewModel.applyUiState.collectAsStateWithLifecycle()

    LaunchedEffect(jobId) {
        viewModel.loadApplyScreen(jobId)
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Apply for Job", onBack = onBack) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())

            uiState.isSuccess -> ApplicationSuccessContent(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                onDoneClick = onSubmissionSuccess
            )

            uiState.jobUnavailable || uiState.job == null -> EmptyState(
                title = "You can't apply to this job",
                description = uiState.errorMessage ?: "This job is no longer accepting applications.",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )

            else -> ApplyJobContent(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                uiState = uiState,
                onCoverLetterChanged = viewModel::onCoverLetterChanged,
                onManageResumeClick = onManageResumeClick,
                onSubmit = viewModel::submitApplication
            )
        }
    }
}

@Composable
private fun ApplyJobContent(
    uiState: ApplyJobUiState,
    onCoverLetterChanged: (String) -> Unit,
    onManageResumeClick: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val job = uiState.job ?: return

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(Spacing.lg)
    ) {
        LocalSkillCard {
            Text(text = job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = job.companyName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = SalaryFormatter.format(job.minimumSalary, job.maximumSalary, job.currency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Resume",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.lg)
        )

        LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
            if (uiState.resume?.isPresent == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.resume.fileName,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = Spacing.sm),
                        maxLines = 1
                    )
                    LocalSkillTextButton(text = "Replace", onClick = onManageResumeClick)
                }
            } else {
                Column {
                    Text(
                        text = "No resume on file yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    LocalSkillTextButton(text = "Add a resume", onClick = onManageResumeClick)
                }
            }
        }

        Text(
            text = "Cover letter (optional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.lg)
        )

        androidx.compose.material3.OutlinedTextField(
            value = uiState.coverLetter,
            onValueChange = onCoverLetterChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs),
            minLines = 5,
            placeholder = { Text("Tell the employer why you're a great fit...") }
        )
        Text(
            text = "${uiState.coverLetter.length}/${Constants.MAX_COVER_LETTER_LENGTH}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xxs),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )

        if (uiState.errorMessage != null) {
            ErrorMessage(message = uiState.errorMessage, modifier = Modifier.padding(top = Spacing.sm))
        }

        LocalSkillPrimaryButton(
            text = "Submit application",
            onClick = onSubmit,
            isLoading = uiState.isSubmitting,
            enabled = uiState.resume?.isPresent == true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg)
        )
    }
}

@Composable
private fun ApplicationSuccessContent(
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = Spacing.md).size(56.dp)
        )
        Text(text = "Application submitted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "The employer will review your application and update its status soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs)
        )
        LocalSkillPrimaryButton(
            text = "View my applications",
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg)
        )
    }
}
