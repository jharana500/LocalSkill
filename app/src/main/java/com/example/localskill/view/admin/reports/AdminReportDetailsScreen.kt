package com.example.localskill.view.admin.reports

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
import com.example.localskill.model.ReportStatus
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
import com.example.localskill.viewmodel.AdminReportEvent
import com.example.localskill.viewmodel.AdminReportViewModel

@Composable
fun AdminReportDetailsScreen(
    viewModel: AdminReportViewModel,
    reportId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var resolutionNote by remember { mutableStateOf("") }

    LaunchedEffect(reportId) { viewModel.loadReportDetails(reportId) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminReportEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Report details", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val report = uiState.report
        if (uiState.isLoading || report == null) {
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
            Text(text = report.reason, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StatusChip(text = report.status.replace('_', ' '), modifier = Modifier.padding(top = Spacing.xxs))

            LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                Text(text = "Reported ${report.targetType.lowercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "Target ID: ${report.targetId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Filed ${DateUtils.formatDate(report.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (report.description.isNotBlank()) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(text = "Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = report.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
                }
            }

            if (report.status == ReportStatus.PENDING.name || report.status == ReportStatus.UNDER_REVIEW.name) {
                if (report.status == ReportStatus.PENDING.name) {
                    LocalSkillSecondaryButton(
                        text = "Mark under review",
                        onClick = viewModel::markUnderReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.lg)
                    )
                }

                LocalSkillTextField(
                    value = resolutionNote,
                    onValueChange = { resolutionNote = it },
                    label = "Resolution note",
                    singleLine = false,
                    modifier = Modifier.padding(top = Spacing.md)
                )

                LocalSkillPrimaryButton(
                    text = "Resolve",
                    onClick = { viewModel.resolveReport(resolutionNote) },
                    enabled = resolutionNote.isNotBlank(),
                    isLoading = uiState.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                )
                LocalSkillDestructiveButton(
                    text = "Reject",
                    onClick = { viewModel.rejectReport(resolutionNote) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm)
                )
            } else if (report.resolutionNote.isNotBlank()) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(text = "Resolution note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = report.resolutionNote, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
                }
            }
        }
    }
}
