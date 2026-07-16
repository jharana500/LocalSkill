package com.example.localskill.view.admin.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onPendingCompanyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    when {
        uiState.isLoading -> FullScreenLoading(modifier = modifier.fillMaxSize())

        uiState.errorMessage != null -> Column(modifier = modifier) {
            ErrorMessage(message = uiState.errorMessage.orEmpty())
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Text(text = "Platform overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            item { AdminStatsGrid(stats = uiState.stats) }

            if (uiState.pendingCompanies.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending verification requests",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
                items(uiState.pendingCompanies, key = { it.id }) { company ->
                    PendingCompanyRow(company = company, onClick = { onPendingCompanyClick(company.id) })
                }
            }

            if (uiState.recentActivity.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent administrator activity",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
                items(uiState.recentActivity, key = { it.id }) { activity ->
                    LocalSkillCard {
                        Text(text = activity.summary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = DateUtils.formatRelative(activity.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatsGrid(stats: AdminDashboardStatsModel, modifier: Modifier = Modifier) {
    val entries = listOf(
        "Total users" to stats.totalUsers,
        "Active job seekers" to stats.activeJobSeekers,
        "Total companies" to stats.totalCompanies,
        "Verified companies" to stats.verifiedCompanies,
        "Pending verification" to stats.pendingCompanies,
        "Rejected companies" to stats.rejectedCompanies,
        "Active jobs" to stats.activeJobs,
        "Draft jobs" to stats.draftJobs,
        "Closed jobs" to stats.closedJobs,
        "Total applications" to stats.totalApplications,
        "Open reports" to stats.openReports,
        "Suspended accounts" to stats.suspendedAccounts
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(entries) { (label, value) ->
            LocalSkillCard {
                Text(text = value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PendingCompanyRow(company: CompanyModel, onClick: () -> Unit) {
    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = company.companyName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Text(
            text = "Submitted ${DateUtils.formatDate(company.verificationSubmittedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onClick) { Text("Review") }
    }
}
