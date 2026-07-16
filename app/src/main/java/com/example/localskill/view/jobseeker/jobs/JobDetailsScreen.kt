package com.example.localskill.view.jobseeker.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.utils.SalaryFormatter
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobDetailsUiState
import com.example.localskill.viewmodel.JobViewModel

@Composable
fun JobDetailsScreen(
    viewModel: JobViewModel,
    jobId: String,
    onBack: () -> Unit,
    onApplyClick: (String) -> Unit
) {
    val uiState by viewModel.detailsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(jobId) {
        viewModel.loadJobDetails(jobId)
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Job Details", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.notFound || uiState.job == null -> EmptyState(
                    title = "This job is unavailable",
                    description = uiState.errorMessage ?: "It may have been removed or closed."
                )

                else -> JobDetailsContent(
                    job = uiState.job!!,
                    isSaved = uiState.isSaved,
                    hasApplied = uiState.hasApplied,
                    applicationStatus = uiState.applicationStatus,
                    onSaveToggle = { viewModel.toggleSaveJob(jobId) },
                    onApplyClick = { onApplyClick(jobId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobDetailsContent(
    job: JobModel,
    isSaved: Boolean,
    hasApplied: Boolean,
    applicationStatus: String?,
    onSaveToggle: () -> Unit,
    onApplyClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Row(verticalAlignment = Alignment.Top) {
                    RemoteAvatar(imageUrl = job.companyLogoUrl, fallbackText = job.companyName)
                    Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
                        Text(text = job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = job.companyName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (job.companyVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified company",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = Spacing.xxs).height(16.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onSaveToggle) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Unsave job" else "Save job",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    if (job.location.isNotBlank()) StatusChip(text = job.location)
                    if (job.jobType.isNotBlank()) StatusChip(text = job.jobType.replace('_', ' '))
                    if (job.workplaceType.isNotBlank()) StatusChip(text = job.workplaceType)
                }
            }

            item {
                DetailRow(label = "Salary", value = SalaryFormatter.format(job.minimumSalary, job.maximumSalary, job.currency))
            }
            if (job.experienceLevel.isNotBlank()) {
                item { DetailRow(label = "Experience", value = job.experienceLevel) }
            }
            if (job.educationRequirement.isNotBlank()) {
                item { DetailRow(label = "Education", value = job.educationRequirement) }
            }
            item { DetailRow(label = "Vacancies", value = job.vacancyCount.toString()) }
            item { DetailRow(label = "Deadline", value = DateUtils.formatDeadline(job.applicationDeadline)) }
            item { DetailRow(label = "Posted", value = DateUtils.formatRelative(job.createdAt)) }

            if (job.description.isNotBlank()) {
                item {
                    SectionHeader(title = "Description", modifier = Modifier.padding(top = Spacing.sm))
                }
                item { Text(text = job.description, style = MaterialTheme.typography.bodyMedium) }
            }

            if (job.responsibilities.isNotEmpty()) {
                item { SectionHeader(title = "Responsibilities", modifier = Modifier.padding(top = Spacing.sm)) }
                items(job.responsibilities) { BulletText(it) }
            }

            if (job.requirements.isNotEmpty()) {
                item { SectionHeader(title = "Requirements", modifier = Modifier.padding(top = Spacing.sm)) }
                items(job.requirements) { BulletText(it) }
            }

            if (job.skills.isNotEmpty()) {
                item { SectionHeader(title = "Skills", modifier = Modifier.padding(top = Spacing.sm)) }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                        job.skills.forEach { skill -> StatusChip(text = skill, tone = StatusChipTone.NEUTRAL) }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
        }

        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(Spacing.md)
            ) {
                when {
                    hasApplied -> {
                        StatusChip(
                            text = "Applied · ${applicationStatus?.replace('_', ' ') ?: "Applied"}",
                            tone = StatusChipTone.SUCCESS
                        )
                    }

                    !job.isOpenForApplications -> {
                        Text(
                            text = if (job.isExpired) "Applications closed" else "This job is no longer accepting applications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {
                        LocalSkillPrimaryButton(
                            text = "Apply now",
                            onClick = onApplyClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BulletText(text: String) {
    Row {
        Text(text = "•  ", style = MaterialTheme.typography.bodyMedium)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
