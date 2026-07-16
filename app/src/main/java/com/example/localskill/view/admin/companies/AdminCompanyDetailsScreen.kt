package com.example.localskill.view.admin.companies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AdminCompanyEvent
import com.example.localskill.viewmodel.AdminCompanyViewModel

@Composable
fun AdminCompanyDetailsScreen(
    viewModel: AdminCompanyViewModel,
    companyId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(companyId) { viewModel.loadCompanyDetails(companyId) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AdminCompanyEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Company review", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val company = uiState.company
        if (uiState.isLoading || company == null) {
            FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            Text(text = company.companyName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StatusChip(text = company.verificationStatus, modifier = Modifier.padding(top = Spacing.xxs))

            LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                Text(text = "Registration details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DetailRow(label = "Contact person", value = company.contactPersonName)
                DetailRow(label = "Email", value = company.email)
                DetailRow(label = "Phone", value = company.phone)
                DetailRow(label = "Industry", value = company.industry)
                DetailRow(label = "Registration number", value = company.registrationNumber)
                DetailRow(label = "PAN number", value = company.panNumber)
                DetailRow(label = "Address", value = "${company.address}, ${company.city}, ${company.district}")
            }

            if (company.isRejected && company.rejectionReason.isNotBlank()) {
                LocalSkillCard(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(text = "Previous rejection reason", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = company.rejectionReason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xxs))
                }
            }

            Text(text = "Submitted documents", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = Spacing.md))
            if (uiState.documents.isEmpty()) {
                Text(text = "No documents submitted yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.documents.forEach { document ->
                    LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
                        Text(text = document.documentType.replace('_', ' '), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(text = document.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { uriHandler.openUri(document.downloadUrl) }) { Text("Open document") }
                    }
                }
            }

            if (company.verificationStatus == CompanyVerificationStatus.PENDING.name) {
                Column(modifier = Modifier.padding(top = Spacing.lg)) {
                    LocalSkillPrimaryButton(
                        text = "Approve company",
                        onClick = viewModel::approveCompany,
                        isLoading = uiState.isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LocalSkillDestructiveButton(
                        text = "Reject with reason",
                        onClick = { showRejectDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.sm)
                    )
                }
            }
        }
    }

    if (showRejectDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
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
                        viewModel.rejectCompany(reason)
                        showRejectDialog = false
                    },
                    enabled = reason.isNotBlank()
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(top = Spacing.xs)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
