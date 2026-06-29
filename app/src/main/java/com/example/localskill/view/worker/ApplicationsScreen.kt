package com.example.localskill.view.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.ApplicationModel
import com.example.localskill.view.components.ApplicationCard
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LoadingState
import com.example.localskill.viewmodel.worker.WorkerApplicationsUiState

@Composable
fun ApplicationsScreen(
    state: WorkerApplicationsUiState,
    onReview: (ApplicationModel) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("My Applications", style = MaterialTheme.typography.headlineMedium)
            Text("Track the jobs you have applied for.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.isLoading) {
            item { LoadingState() }
        }
        item { ErrorMessage(state.errorMessage) }
        if (!state.isLoading && state.applications.isEmpty()) {
            item { EmptyState("You have not applied to any jobs yet.\nJobs you apply for will appear here.") }
        } else {
            items(state.applications) { application ->
                ApplicationCard(
                    application = application,
                    hasReviewed = application.id in state.reviewedApplicationIds,
                    onReview = { onReview(application) }
                )
            }
        }
    }
}
