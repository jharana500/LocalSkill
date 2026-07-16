package com.example.localskill.view.company.verification

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.CompanyDocumentType
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.utils.FileValidationUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.StatusChip
import com.example.localskill.view.common.components.StatusChipTone
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyVerificationEvent
import com.example.localskill.viewmodel.CompanyVerificationViewModel

private val REQUIRED_DOCUMENTS = listOf(
    CompanyDocumentType.REGISTRATION_CERTIFICATE to "Company registration certificate",
    CompanyDocumentType.PAN_DOCUMENT to "PAN document"
)
private val OPTIONAL_DOCUMENTS = listOf(
    CompanyDocumentType.VAT_DOCUMENT to "VAT document (optional)",
    CompanyDocumentType.SUPPORTING_DOCUMENT to "Supporting document (optional)"
)

@Composable
fun CompanyVerificationScreen(
    viewModel: CompanyVerificationViewModel,
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadVerification() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompanyVerificationEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    var pendingDocumentType by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val type = pendingDocumentType
        if (uri != null && type != null) viewModel.uploadDocument(type, uri)
        pendingDocumentType = null
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Company verification", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> FullScreenLoading(modifier = Modifier.padding(innerPadding).fillMaxSize())

            uiState.errorMessage != null -> Column(modifier = Modifier.padding(innerPadding).padding(Spacing.lg)) {
                ErrorMessage(message = uiState.errorMessage.orEmpty())
            }

            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg)
            ) {
                StatusChip(text = statusLabel(uiState.company.verificationStatus), tone = statusTone(uiState.company.verificationStatus))

                if (uiState.company.isRejected && uiState.company.rejectionReason.isNotBlank()) {
                    LocalSkillCard(modifier = Modifier.padding(top = Spacing.sm)) {
                        Text(text = "Rejection reason", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = uiState.company.rejectionReason,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    }
                }

                if (uiState.company.isPending) {
                    Text(
                        text = "Your verification is under review. This usually takes 1-2 business days.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }

                if (uiState.company.isVerified) {
                    Text(
                        text = "Your company is verified. You can publish jobs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }

                if (!uiState.company.isVerified && !uiState.company.isPending) {
                    Text(
                        text = "Company profile",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.lg)
                    )
                    if (uiState.company.missingProfileSections.isEmpty()) {
                        Text(
                            text = "Profile complete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    } else {
                        Text(
                            text = "Missing: ${uiState.company.missingProfileSections.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    }
                    LocalSkillSecondaryButton(
                        text = "Edit company profile",
                        onClick = onEditProfileClick,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )

                    Text(
                        text = "Verification documents",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Spacing.lg)
                    )
                    (REQUIRED_DOCUMENTS + OPTIONAL_DOCUMENTS).forEach { (type, label) ->
                        DocumentRow(
                            label = label,
                            uploaded = uiState.documents.any { it.documentType == type.name },
                            isUploading = uiState.uploadingDocumentType == type.name,
                            onUpload = {
                                pendingDocumentType = type.name
                                launcher.launch(FileValidationUtils.ACCEPTED_DOCUMENT_MIME_TYPES.toTypedArray())
                            }
                        )
                    }

                    LocalSkillPrimaryButton(
                        text = "Submit for verification",
                        onClick = viewModel::submitVerification,
                        isLoading = uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.lg)
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(
    label: String,
    uploaded: Boolean,
    isUploading: Boolean,
    onUpload: () -> Unit
) {
    LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (uploaded) Icons.Default.CheckCircle else Icons.Default.Description,
                contentDescription = null,
                tint = if (uploaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.sm)
            )
            LocalSkillSecondaryButton(
                text = if (uploaded) "Replace" else "Upload",
                onClick = onUpload,
                enabled = !isUploading
            )
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> "Verified"
    CompanyVerificationStatus.PENDING.name -> "Under review"
    CompanyVerificationStatus.REJECTED.name -> "Rejected"
    else -> "Draft"
}

private fun statusTone(status: String): StatusChipTone = when (status) {
    CompanyVerificationStatus.VERIFIED.name -> StatusChipTone.SUCCESS
    CompanyVerificationStatus.PENDING.name -> StatusChipTone.WARNING
    CompanyVerificationStatus.REJECTED.name -> StatusChipTone.ERROR
    else -> StatusChipTone.NEUTRAL
}
