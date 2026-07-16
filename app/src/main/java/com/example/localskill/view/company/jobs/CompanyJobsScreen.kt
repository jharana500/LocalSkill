package com.example.localskill.view.company.jobs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyJobViewModel

@Composable
fun CompanyJobsScreen(
    viewModel: CompanyJobViewModel,
    onJobClick: (String) -> Unit,
    onPostJobClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.jobsUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadJobs() }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onPostJobClick) {
                Icon(Icons.Default.Add, contentDescription = "Post a job")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = "My Jobs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(Spacing.lg)
            )

            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.errorMessage != null && uiState.jobs.isEmpty() ->
                    ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(horizontal = Spacing.lg))

                uiState.jobs.isEmpty() -> EmptyState(
                    title = "No jobs yet",
                    description = "Tap the + button to post your first job."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.jobs, key = { it.id }) { job ->
                        CompanyJobRow(job = job, onClick = { onJobClick(job.id) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompanyJobRow(job: JobModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(
                        text = job.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(text = job.status.replace('_', ' '), tone = jobStatusTone(job.status))
            }
            Text(
                text = "${job.applicationCount} applicant${if (job.applicationCount == 1) "" else "s"} · Posted ${DateUtils.formatRelative(job.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs)
            )
        }
    }
}

private fun jobStatusTone(status: String): StatusChipTone = when (status) {
    JobStatus.ACTIVE.name -> StatusChipTone.SUCCESS
    JobStatus.CLOSED.name, JobStatus.EXPIRED.name -> StatusChipTone.NEUTRAL
    else -> StatusChipTone.WARNING
}
