package com.example.localskill.view.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.view.theme.SuccessColor
import com.example.localskill.view.theme.WarningColor
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
                Column {
                    Text(text = "Admin dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Platform overview and pending actions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }

            item { AdminStatsGrid(stats = uiState.stats) }

            if (uiState.pendingCompanies.isNotEmpty()) {
                item {
                    SectionHeader(
                        icon = Icons.Default.PendingActions,
                        title = "Pending verification requests",
                        count = uiState.pendingCompanies.size
                    )
                }
                items(uiState.pendingCompanies, key = { it.id }) { company ->
                    PendingCompanyRow(company = company, onClick = { onPendingCompanyClick(company.id) })
                }
            }

            if (uiState.recentActivity.isNotEmpty()) {
                item {
                    SectionHeader(icon = Icons.Default.History, title = "Recent administrator activity")
                }
                items(uiState.recentActivity, key = { it.id }) { activity ->
                    ActivityRow(summary = activity.summary, timestamp = activity.createdAt)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, modifier: Modifier = Modifier, count: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = Spacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = Spacing.xs)
        )
        if (count != null) {
            StatusChip(text = count.toString(), tone = StatusChipTone.WARNING, modifier = Modifier.padding(start = Spacing.xs))
        }
    }
}

private data class StatEntry(val label: String, val value: Int, val icon: ImageVector, val tone: StatusChipTone)

@Composable
private fun AdminStatsGrid(stats: AdminDashboardStatsModel, modifier: Modifier = Modifier) {
    val entries = listOf(
        StatEntry("Total users", stats.totalUsers, Icons.Default.Groups, StatusChipTone.NEUTRAL),
        StatEntry("Active job seekers", stats.activeJobSeekers, Icons.Default.Person, StatusChipTone.NEUTRAL),
        StatEntry("Total companies", stats.totalCompanies, Icons.Default.Business, StatusChipTone.NEUTRAL),
        StatEntry("Verified companies", stats.verifiedCompanies, Icons.Default.Verified, StatusChipTone.SUCCESS),
        StatEntry("Pending verification", stats.pendingCompanies, Icons.Default.PendingActions, StatusChipTone.WARNING),
        StatEntry("Rejected companies", stats.rejectedCompanies, Icons.Default.Cancel, StatusChipTone.ERROR),
        StatEntry("Active jobs", stats.activeJobs, Icons.Default.Work, StatusChipTone.SUCCESS),
        StatEntry("Draft jobs", stats.draftJobs, Icons.Default.Drafts, StatusChipTone.NEUTRAL),
        StatEntry("Closed jobs", stats.closedJobs, Icons.Default.Archive, StatusChipTone.NEUTRAL),
        StatEntry("Total applications", stats.totalApplications, Icons.Default.Description, StatusChipTone.NEUTRAL),
        StatEntry("Open reports", stats.openReports, Icons.Default.Flag, StatusChipTone.WARNING),
        StatEntry("Suspended accounts", stats.suspendedAccounts, Icons.Default.Block, StatusChipTone.ERROR)
    )

    // A plain (non-lazy) grid: this is nested inside the dashboard's own LazyColumn,
    // and a lazy layout nested in another lazy layout without a bounded height crashes
    // with "measured with an infinity maximum height constraints".
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        entries.chunked(2).forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                rowEntries.forEach { entry -> StatTile(entry = entry, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StatTile(entry: StatEntry, modifier: Modifier = Modifier) {
    val accent = when (entry.tone) {
        StatusChipTone.NEUTRAL -> MaterialTheme.colorScheme.primary
        StatusChipTone.SUCCESS -> SuccessColor
        StatusChipTone.WARNING -> WarningColor
        StatusChipTone.ERROR -> MaterialTheme.colorScheme.error
    }

    LocalSkillCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = entry.icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Text(
            text = entry.value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = Spacing.xs)
        )
        Text(text = entry.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PendingCompanyRow(company: CompanyModel, onClick: () -> Unit) {
    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = company.companyName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    StatusChip(text = "Pending", tone = StatusChipTone.WARNING, modifier = Modifier.padding(start = Spacing.xs))
                }
                Text(
                    text = "Submitted ${DateUtils.formatDate(company.verificationSubmittedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
            TextButton(onClick = onClick) {
                Text("Review")
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ActivityRow(summary: String, timestamp: Long) {
    LocalSkillCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.padding(start = Spacing.sm)) {
                Text(text = summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = DateUtils.formatRelative(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
