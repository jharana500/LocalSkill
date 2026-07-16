package com.example.localskill.view.admin.reports

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminReportViewModel

private val STATUS_TABS = listOf(
    null to "All",
    ReportStatus.PENDING.name to "Pending",
    ReportStatus.UNDER_REVIEW.name to "Under review",
    ReportStatus.RESOLVED.name to "Resolved",
    ReportStatus.REJECTED.name to "Rejected"
)

@Composable
fun AdminReportsScreen(
    viewModel: AdminReportViewModel,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.reportsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadReports() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(STATUS_TABS) { (status, label) ->
                FilterChip(
                    selected = uiState.filterStatus == status,
                    onClick = { viewModel.setFilterStatus(status) },
                    label = { Text(label) }
                )
            }
        }

        when {
            uiState.isLoading -> FullScreenLoading()

            uiState.errorMessage != null && uiState.reports.isEmpty() ->
                ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(Spacing.lg))

            uiState.reports.isEmpty() -> EmptyState(title = "No reports here", description = "Reports matching this filter will show up here.")

            else -> LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.reports, key = { it.id }) { report ->
                    ReportRow(report = report, onClick = { onReportClick(report.id) })
                }
            }
        }
    }
}

@Composable
private fun ReportRow(report: JobReportModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = report.reason, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = "${report.targetType} · ${DateUtils.formatRelative(report.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = report.status.replace('_', ' '), tone = statusTone(report.status))
        }
    }
}

private fun statusTone(status: String): StatusChipTone = when (status) {
    ReportStatus.RESOLVED.name -> StatusChipTone.SUCCESS
    ReportStatus.REJECTED.name -> StatusChipTone.ERROR
    ReportStatus.UNDER_REVIEW.name -> StatusChipTone.WARNING
    else -> StatusChipTone.NEUTRAL
}
