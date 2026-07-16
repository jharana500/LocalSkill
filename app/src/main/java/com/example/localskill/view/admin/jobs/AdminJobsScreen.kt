package com.example.localskill.view.admin.jobs

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminJobFilterTab
import com.example.localskill.viewmodel.AdminJobViewModel

@Composable
fun AdminJobsScreen(
    viewModel: AdminJobViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedJob by remember { mutableStateOf<JobModel?>(null) }

    LaunchedEffect(Unit) { viewModel.loadJobs() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Job moderation",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(AdminJobFilterTab.entries) { tab ->
                FilterChip(
                    selected = uiState.filterTab == tab,
                    onClick = { viewModel.setFilterTab(tab) },
                    label = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        val filtered = uiState.filtered

        when {
            uiState.isLoading -> FullScreenLoading()

            uiState.errorMessage != null && uiState.jobs.isEmpty() ->
                ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(Spacing.lg))

            filtered.isEmpty() -> EmptyState(title = "No jobs here", description = "Jobs matching this filter will show up here.")

            else -> LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filtered, key = { it.id }) { job ->
                    AdminJobRow(job = job, onClick = { selectedJob = job })
                }
            }
        }
    }

    selectedJob?.let { job ->
        JobModerationDialog(
            job = job,
            onDismiss = { selectedJob = null },
            onRemove = { reason ->
                viewModel.removeJob(job.id, reason)
                selectedJob = null
            },
            onRestore = {
                viewModel.restoreJob(job.id)
                selectedJob = null
            }
        )
    }
}

@Composable
private fun AdminJobRow(job: JobModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = "${job.companyName} · Posted ${DateUtils.formatRelative(job.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = if (job.isDiscoverable) job.status.replace('_', ' ') else "Removed",
                tone = if (job.isDiscoverable) StatusChipTone.NEUTRAL else StatusChipTone.ERROR
            )
        }
    }
}

@Composable
private fun JobModerationDialog(
    job: JobModel,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit,
    onRestore: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(job.title) },
        text = {
            Column {
                Text(text = "Posted by ${job.companyName}", style = MaterialTheme.typography.bodyMedium)
                if (job.isDiscoverable) {
                    LocalSkillTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = "Reason for removal",
                        singleLine = false,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
            }
        },
        confirmButton = {
            if (job.isDiscoverable) {
                TextButton(onClick = { onRemove(reason) }, enabled = reason.isNotBlank()) { Text("Remove from discovery") }
            } else {
                TextButton(onClick = onRestore) { Text("Restore") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
