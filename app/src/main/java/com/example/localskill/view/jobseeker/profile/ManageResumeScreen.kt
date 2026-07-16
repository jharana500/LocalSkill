package com.example.localskill.view.jobseeker.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.utils.DateUtils
import com.example.localskill.utils.FileValidationUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillDestructiveButton
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerProfileViewModel
import com.example.localskill.viewmodel.ProfileEvent
import kotlin.math.roundToInt

@Composable
fun ManageResumeScreen(
    viewModel: JobSeekerProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.uploadResume(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Resume", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(Spacing.lg)
        ) {
            val resume = uiState.profile.resume

            if (resume.isPresent) {
                LocalSkillCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
                            Text(text = resume.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${formatFileSize(resume.fileSizeBytes)} · Uploaded ${DateUtils.formatDate(resume.uploadedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    LocalSkillSecondaryButton(
                        text = "Replace",
                        onClick = { launcher.launch(FileValidationUtils.ACCEPTED_RESUME_MIME_TYPES.toTypedArray()) },
                        modifier = Modifier.weight(1f)
                    )
                    LocalSkillDestructiveButton(
                        text = "Remove",
                        onClick = { showRemoveConfirmation = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                EmptyState(
                    title = "No resume added",
                    description = "Add a PDF or Word document so you're ready to apply for jobs."
                )
                LocalSkillPrimaryButton(
                    text = "Add resume",
                    onClick = { launcher.launch(FileValidationUtils.ACCEPTED_RESUME_MIME_TYPES.toTypedArray()) },
                    isLoading = uiState.isMutating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md)
                )
            }
        }
    }

    if (showRemoveConfirmation) {
        ConfirmationDialog(
            title = "Remove resume?",
            message = "You'll need to add a new resume before applying to jobs again.",
            confirmLabel = "Remove",
            onConfirm = {
                viewModel.removeResume()
                showRemoveConfirmation = false
            },
            onDismiss = { showRemoveConfirmation = false }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val kb = bytes / 1024.0
    return if (kb < 1024) "${kb.roundToInt()} KB" else "${(kb / 1024.0 * 10).roundToInt() / 10.0} MB"
}
