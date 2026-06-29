package com.example.localskill.view.employer

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
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.JobCard
import com.example.localskill.view.components.LoadingState
import com.example.localskill.viewmodel.employer.PostedJobsUiState

@Composable
fun PostedJobsScreen(state: PostedJobsUiState) {
    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Posted Jobs", style = MaterialTheme.typography.headlineMedium)
            Text("Review the jobs you have created.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.isLoading) {
            item { LoadingState() }
        }
        item { ErrorMessage(state.errorMessage) }
        if (!state.isLoading && state.jobs.isEmpty()) {
            item { EmptyState("Your posted jobs will appear here once you create one.") }
        } else {
            items(state.jobs) { job -> JobCard(job) }
        }
    }
}
