package com.example.localskill.view.company.applicants

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.ApplicantStatusFilter
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.ApplicantViewModel

@Composable
fun ApplicantsScreen(
    viewModel: ApplicantViewModel,
    onApplicantClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.applicantsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadApplicants() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Applicants",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(ApplicantStatusFilter.entries) { tab ->
                FilterChip(
                    selected = uiState.filter.statusFilter == tab,
                    onClick = { viewModel.setStatusFilter(tab) },
                    label = { Text(tabLabel(tab)) }
                )
            }
        }

        val filtered = uiState.filtered

        when {
            uiState.isLoading -> FullScreenLoading()

            uiState.errorMessage != null && uiState.applications.isEmpty() -> ErrorMessage(
                message = uiState.errorMessage.orEmpty(),
                modifier = Modifier.padding(Spacing.lg)
            )

            filtered.isEmpty() -> EmptyState(
                title = "No applicants here",
                description = "Applications for your jobs will show up in this tab."
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filtered, key = { it.id }) { application ->
                    ApplicantRow(application = application, onClick = { onApplicantClick(application.id) })
                }
            }
        }
    }
}

@Composable
internal fun ApplicantRow(application: ApplicationModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RemoteAvatar(imageUrl = null, fallbackText = application.jobTitle)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.sm)
            ) {
                Text(
                    text = application.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Applied ${DateUtils.formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = application.status.replace('_', ' '), tone = statusTone(application.status))
        }
    }
}

private fun tabLabel(tab: ApplicantStatusFilter): String = when (tab) {
    ApplicantStatusFilter.ALL -> "All"
    ApplicantStatusFilter.NEW -> "New"
    ApplicantStatusFilter.UNDER_REVIEW -> "Under review"
    ApplicantStatusFilter.SHORTLISTED -> "Shortlisted"
    ApplicantStatusFilter.INTERVIEW -> "Interview"
    ApplicantStatusFilter.HIRED -> "Hired"
    ApplicantStatusFilter.REJECTED -> "Rejected"
    ApplicantStatusFilter.WITHDRAWN -> "Withdrawn"
}

private fun statusTone(status: String): StatusChipTone = when (status) {
    ApplicationStatus.HIRED.name -> StatusChipTone.SUCCESS
    ApplicationStatus.REJECTED.name, ApplicationStatus.WITHDRAWN.name -> StatusChipTone.ERROR
    ApplicationStatus.SHORTLISTED.name, ApplicationStatus.INTERVIEW.name -> StatusChipTone.WARNING
    else -> StatusChipTone.NEUTRAL
}
