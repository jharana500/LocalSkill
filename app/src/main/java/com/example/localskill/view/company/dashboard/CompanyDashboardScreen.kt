package com.example.localskill.view.company.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.view.common.components.JobCard
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyDashboardViewModel

@Composable
fun CompanyDashboardScreen(
    viewModel: CompanyDashboardViewModel,
    onPostJobClick: () -> Unit,
    onReviewApplicantsClick: () -> Unit,
    onVerificationClick: () -> Unit,
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    when {
        uiState.isLoading -> FullScreenLoading(modifier = modifier.fillMaxSize())

        uiState.errorMessage != null -> Column(modifier = modifier.padding(Spacing.lg)) {
            ErrorMessage(message = uiState.errorMessage.orEmpty())
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RemoteAvatar(imageUrl = uiState.company.logoUrl, fallbackText = uiState.company.companyName)
                    Column(modifier = Modifier.padding(start = Spacing.sm)) {
                        Text(
                            text = uiState.company.companyName.ifBlank { "Your company" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        StatusChip(
                            text = verificationLabel(uiState.company.verificationStatus),
                            tone = verificationTone(uiState.company.verificationStatus)
                        )
                    }
                }
            }

            if (!uiState.company.isVerified) {
                item {
                    LocalSkillCard {
                        Text(text = "Verification required", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = when (uiState.company.verificationStatus) {
                                CompanyVerificationStatus.REJECTED.name ->
                                    "Your verification was rejected. Update your details and resubmit to unlock job publishing."

                                CompanyVerificationStatus.PENDING.name ->
                                    "Your verification is under review. You can prepare job posts as drafts in the meantime."

                                else -> "Complete your company profile and submit verification documents to publish jobs."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                        LocalSkillTextButton(
                            text = "Go to verification",
                            onClick = onVerificationClick,
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }
            }

            item { DashboardStatsGrid(stats = uiState.stats) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    LocalSkillPrimaryButton(
                        text = "Post a job",
                        onClick = onPostJobClick,
                        enabled = uiState.company.isVerified,
                        modifier = Modifier.weight(1f)
                    )
                    LocalSkillSecondaryButton(
                        text = "Review applicants",
                        onClick = onReviewApplicantsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.recentJobs.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent job posts",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
                items(uiState.recentJobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        isSaved = false,
                        onSaveToggle = {},
                        onClick = { onJobClick(job.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStatsGrid(stats: CompanyDashboardStatsModel, modifier: Modifier = Modifier) {
    val entries = listOf(
        "Active jobs" to stats.activeJobs,
        "Draft jobs" to stats.draftJobs,
        "Closed jobs" to stats.closedJobs,
        "Total applications" to stats.totalApplications,
        "Awaiting review" to stats.awaitingReview,
        "Shortlisted" to stats.shortlisted,
        "Interviews" to stats.scheduledInterviews,
        "Hired" to stats.hired
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(entries) { (label, value) ->
            LocalSkillCard {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun verificationLabel(status: String): String = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> "Verified"
    CompanyVerificationStatus.PENDING.name -> "Under review"
    CompanyVerificationStatus.REJECTED.name -> "Rejected"
    else -> "Draft"
}

private fun verificationTone(status: String): StatusChipTone = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> StatusChipTone.SUCCESS
    CompanyVerificationStatus.PENDING.name -> StatusChipTone.WARNING
    CompanyVerificationStatus.REJECTED.name -> StatusChipTone.ERROR
    else -> StatusChipTone.NEUTRAL
}
