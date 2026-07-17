package com.example.localskill.view.admin.companies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.view.theme.SuccessColor
import com.example.localskill.viewmodel.AdminCompanyEvent
import com.example.localskill.viewmodel.AdminCompanyFilterTab
import com.example.localskill.viewmodel.AdminCompanyViewModel

@Composable
fun AdminCompaniesScreen(
    viewModel: AdminCompanyViewModel,
    onCompanyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.companiesUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var rejectingCompanyId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadCompanies() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminCompanyEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
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
                        CompanyRow(
                            company = company,
                            isProcessing = uiState.processingCompanyId == company.id,
                            onClick = { onCompanyClick(company.id) },
                            onApprove = { viewModel.approveCompanyFromList(company.id) },
                            onReject = { rejectingCompanyId = company.id }
                        )
                    }
                }
            }
        }
    }

    val rejectingId = rejectingCompanyId
    if (rejectingId != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectingCompanyId = null },
            title = { Text("Reject verification?") },
            text = {
                LocalSkillTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Rejection reason",
                    singleLine = false
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rejectCompanyFromList(rejectingId, reason)
                        rejectingCompanyId = null
                    },
                    enabled = reason.isNotBlank()
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { rejectingCompanyId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CompanyRow(
    company: CompanyModel,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    LocalSkillCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = company.companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = "Registered ${DateUtils.formatDate(company.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(
                    text = statusLabel(company.verificationStatus),
                    tone = statusTone(company.verificationStatus),
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }

            if (company.verificationStatus == CompanyVerificationStatus.PENDING.name) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(
                        onClick = onApprove,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = SuccessColor)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Approve ${company.companyName}")
                    }
                    IconButton(
                        onClick = onReject,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reject ${company.companyName}")
                    }
                }
            }
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
