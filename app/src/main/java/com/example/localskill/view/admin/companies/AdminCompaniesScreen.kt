package com.example.localskill.view.admin.companies

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
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminCompanyFilterTab
import com.example.localskill.viewmodel.AdminCompanyViewModel

@Composable
fun AdminCompaniesScreen(
    viewModel: AdminCompanyViewModel,
    onCompanyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.companiesUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadCompanies() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Companies",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(AdminCompanyFilterTab.entries) { tab ->
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

            uiState.errorMessage != null && uiState.companies.isEmpty() ->
                ErrorMessage(message = uiState.errorMessage.orEmpty(), modifier = Modifier.padding(Spacing.lg))

            filtered.isEmpty() -> EmptyState(title = "No companies here", description = "Companies matching this filter will show up here.")

            else -> LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filtered, key = { it.id }) { company ->
                    CompanyRow(company = company, onClick = { onCompanyClick(company.id) })
                }
            }
        }
    }
}

@Composable
private fun CompanyRow(company: CompanyModel, onClick: () -> Unit) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = company.companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = "Registered ${DateUtils.formatDate(company.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(text = statusLabel(company.verificationStatus), tone = statusTone(company.verificationStatus))
        }
    }
}

private fun statusLabel(status: String) = status.lowercase().replaceFirstChar { it.uppercase() }

private fun statusTone(status: String): StatusChipTone = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> StatusChipTone.SUCCESS
    CompanyVerificationStatus.PENDING.name -> StatusChipTone.WARNING
    CompanyVerificationStatus.REJECTED.name -> StatusChipTone.ERROR
    else -> StatusChipTone.NEUTRAL
}
