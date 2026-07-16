package com.example.localskill.view.jobseeker.application

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.ApplicationFilterTab
import com.example.localskill.viewmodel.ApplicationViewModel
import com.example.localskill.viewmodel.ApplicationsUiState

@Composable
fun ApplicationsScreen(
    viewModel: ApplicationViewModel,
    onApplicationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.applicationsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadApplications()
    }

    ApplicationsContent(
        uiState = uiState,
        onSelectTab = viewModel::selectApplicationsTab,
        onApplicationClick = onApplicationClick,
        modifier = modifier
    )
}

@Composable
internal fun ApplicationsContent(
    uiState: ApplicationsUiState,
    onSelectTab: (ApplicationFilterTab) -> Unit,
    onApplicationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "My Applications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )

        ScrollableTabRow(selectedTabIndex = ApplicationFilterTab.entries.indexOf(uiState.selectedTab)) {
            ApplicationFilterTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    text = { Text(tabLabel(tab)) }
                )
            }
        }

        val filtered = uiState.applications.filter { uiState.selectedTab.matches(it.status) }

        when {
            uiState.isLoading -> FullScreenLoading()

            uiState.errorMessage != null && uiState.applications.isEmpty() -> ErrorMessage(
                message = uiState.errorMessage.orEmpty(),
                modifier = Modifier.padding(Spacing.lg)
            )

            filtered.isEmpty() -> EmptyState(
                title = "No applications here",
                description = "Jobs you apply to will show up in this tab."
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filtered, key = { it.id }) { application ->
                    ApplicationRow(application = application, onClick = { onApplicationClick(application.id) })
                }
            }
        }
    }
}

@Composable
internal fun ApplicationRow(application: ApplicationModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = application.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = application.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Applied ${DateUtils.formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
            StatusChip(text = application.status.replace('_', ' '), tone = statusTone(application.status))
        }
    }
}

private fun tabLabel(tab: ApplicationFilterTab): String = when (tab) {
    ApplicationFilterTab.ALL -> "All"
    ApplicationFilterTab.ACTIVE -> "Active"
    ApplicationFilterTab.SHORTLISTED -> "Shortlisted"
    ApplicationFilterTab.INTERVIEW -> "Interview"
    ApplicationFilterTab.HIRED -> "Hired"
    ApplicationFilterTab.REJECTED -> "Rejected"
    ApplicationFilterTab.WITHDRAWN -> "Withdrawn"
}

private fun statusTone(status: String): StatusChipTone = when (status) {
    ApplicationStatus.HIRED.name -> StatusChipTone.SUCCESS
    ApplicationStatus.REJECTED.name, ApplicationStatus.WITHDRAWN.name -> StatusChipTone.ERROR
    ApplicationStatus.SHORTLISTED.name, ApplicationStatus.INTERVIEW.name -> StatusChipTone.WARNING
    else -> StatusChipTone.NEUTRAL
}
