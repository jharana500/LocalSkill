package com.example.localskill.view.employer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.view.components.ApplicationCard
import com.example.localskill.view.components.DashboardStatCard
import com.example.localskill.view.components.EmptyState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LoadingState
import com.example.localskill.viewmodel.employer.EmployerApplicationsUiState

@Composable
fun ApplicationsScreen(
    state: EmployerApplicationsUiState,
    onFilterSelected: (String) -> Unit,
    onAccept: (ApplicationModel) -> Unit,
    onReject: (ApplicationModel) -> Unit,
    onReview: (ApplicationModel) -> Unit
) {
    val filters = listOf("ALL", ApplicationStatus.PENDING.name, ApplicationStatus.ACCEPTED.name, ApplicationStatus.REJECTED.name)

    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Applications Received", style = MaterialTheme.typography.headlineMedium)
            Text("Review workers who applied to your posted jobs.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                    DashboardStatCard("Total", state.applications.size.toString())
                }
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                    DashboardStatCard("Pending", state.pendingApplications.size.toString())
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                    DashboardStatCard("Accepted", state.acceptedApplications.size.toString())
                }
                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                    DashboardStatCard("Rejected", state.rejectedApplications.size.toString())
                }
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = state.selectedStatusFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.lowercase().replaceFirstChar { it.titlecase() }) }
                    )
                }
            }
        }
        if (state.isLoading) {
            item { LoadingState() }
        }
        item {
            ErrorMessage(state.errorMessage)
            state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
        if (!state.isLoading && state.visibleApplications.isEmpty()) {
            item {
                EmptyState("No applications received yet.\nApplications from workers will appear here after they apply to your jobs.")
            }
        } else {
            items(state.visibleApplications) { application ->
                ApplicationCard(
                    application = application,
                    showWorkerDetails = true,
                    isUpdating = state.isUpdating,
                    hasReviewed = application.id in state.reviewedApplicationIds,
                    onAccept = { onAccept(application) },
                    onReject = { onReject(application) },
                    onReview = { onReview(application) }
                )
            }
        }
    }
}
