package com.example.localskill.view.jobseeker.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.JobCard
import com.example.localskill.view.common.components.LocalSkillSnackbarHost
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.SavedJobEvent
import com.example.localskill.viewmodel.SavedJobsUiState
import com.example.localskill.viewmodel.SavedJobViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun SavedJobsScreen(
    viewModel: SavedJobViewModel,
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSavedJobs()
    }

    SavedJobsContent(
        uiState = uiState,
        events = viewModel.events,
        onJobClick = onJobClick,
        onUnsaveJob = viewModel::unsaveJob,
        onRetry = viewModel::loadSavedJobs,
        modifier = modifier
    )
}

@Composable
internal fun SavedJobsContent(
    uiState: SavedJobsUiState,
    events: Flow<SavedJobEvent>,
    onJobClick: (String) -> Unit,
    onUnsaveJob: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is SavedJobEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Saved Jobs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(Spacing.lg)
            )

            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.errorMessage != null && uiState.savedJobs.isEmpty() -> Column(
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                ) {
                    ErrorMessage(message = uiState.errorMessage.orEmpty())
                    LocalSkillTextButton(text = "Retry", onClick = onRetry)
                }

                uiState.savedJobs.isEmpty() -> EmptyState(
                    title = "No saved jobs yet",
                    description = "Tap the bookmark icon on any job to save it for later."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.savedJobs, key = { it.id }) { job ->
                        JobCard(
                            job = job,
                            isSaved = true,
                            onSaveToggle = { onUnsaveJob(job.id) },
                            onClick = { onJobClick(job.id) }
                        )
                    }
                }
            }
        }

        LocalSkillSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
