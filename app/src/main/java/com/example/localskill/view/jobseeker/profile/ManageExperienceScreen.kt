package com.example.localskill.view.jobseeker.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.ExperienceModel
import com.example.localskill.utils.DateUtils
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillCheckbox
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.dialogs.ConfirmationDialog
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerProfileViewModel
import com.example.localskill.viewmodel.ProfileEvent
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ManageExperienceScreen(
    viewModel: JobSeekerProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingEntry by remember { mutableStateOf<ExperienceModel?>(null) }
    var entryPendingDelete by remember { mutableStateOf<ExperienceModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Experience", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingEntry = ExperienceModel() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add experience")
            }
        }
    ) { innerPadding ->
        if (uiState.profile.experience.isEmpty()) {
            EmptyState(
                title = "No experience added",
                description = "Add your work experience to strengthen your profile.",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.profile.experience, key = { it.id }) { entry ->
                    ExperienceRow(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { entryPendingDelete = entry }
                    )
                }
            }
        }
    }

    editingEntry?.let { entry ->
        ExperienceFormSheet(
            initial = entry,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                if (entry.id.isBlank()) viewModel.addExperience(updated) else viewModel.updateExperience(updated)
                editingEntry = null
            }
        )
    }

    entryPendingDelete?.let { entry ->
        ConfirmationDialog(
            title = "Delete experience?",
            message = "This will remove \"${entry.jobTitle}\" at \"${entry.company}\" from your profile.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.removeExperience(entry.id)
                entryPendingDelete = null
            },
            onDismiss = { entryPendingDelete = null }
        )
    }
}

@Composable
private fun ExperienceRow(entry: ExperienceModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    LocalSkillCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.jobTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                if (entry.company.isNotBlank()) {
                    Text(text = entry.company, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = DateUtils.formatExperienceDuration(entry.startDate, entry.endDate, entry.currentlyWorking),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit experience") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete experience") }
            }
        }
    }
}

private fun parseMonthYear(text: String): Long? {
    if (text.isBlank()) return null
    return runCatching {
        SimpleDateFormat("MM/yyyy", Locale.US).parse(text)?.time
    }.getOrNull()
}

private fun formatMonthYearInput(millis: Long?): String {
    if (millis == null || millis <= 0L) return ""
    return SimpleDateFormat("MM/yyyy", Locale.US).format(java.util.Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceFormSheet(
    initial: ExperienceModel,
    onDismiss: () -> Unit,
    onSave: (ExperienceModel) -> Unit
) {
    var jobTitle by remember { mutableStateOf(initial.jobTitle) }
    var company by remember { mutableStateOf(initial.company) }
    var location by remember { mutableStateOf(initial.location) }
    var employmentType by remember { mutableStateOf(initial.employmentType) }
    var startDateText by remember { mutableStateOf(formatMonthYearInput(initial.startDate)) }
    var endDateText by remember { mutableStateOf(formatMonthYearInput(initial.endDate)) }
    var currentlyWorking by remember { mutableStateOf(initial.currentlyWorking) }
    var description by remember { mutableStateOf(initial.description) }

    val sheetState = rememberModalBottomSheetState()
    val jobTitleError = if (jobTitle.isBlank()) "Job title is required" else null
    val companyError = if (company.isBlank()) "Company is required" else null
    val startDate = parseMonthYear(startDateText)
    val endDate = if (currentlyWorking) null else parseMonthYear(endDateText)
    val datesError = if (startDateText.isNotBlank() && startDate == null) {
        "Use MM/YYYY format"
    } else {
        JobValidationUtils.validateExperienceDates(startDate ?: 0L, endDate, currentlyWorking)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Text(text = if (initial.id.isBlank()) "Add experience" else "Edit experience", style = MaterialTheme.typography.titleLarge)

            LocalSkillTextField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                label = "Job title",
                errorMessage = jobTitleError,
                modifier = Modifier.padding(top = Spacing.md)
            )
            LocalSkillTextField(
                value = company,
                onValueChange = { company = it },
                label = "Company",
                errorMessage = companyError,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            LocalSkillTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                modifier = Modifier.padding(top = Spacing.sm)
            )
            LocalSkillTextField(
                value = employmentType,
                onValueChange = { employmentType = it },
                label = "Employment type",
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Row(modifier = Modifier.padding(top = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LocalSkillTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = "Start (MM/YYYY)",
                    modifier = Modifier.weight(1f)
                )
                LocalSkillTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = "End (MM/YYYY)",
                    enabled = !currentlyWorking,
                    modifier = Modifier.weight(1f)
                )
            }
            LocalSkillCheckbox(
                checked = currentlyWorking,
                onCheckedChange = { currentlyWorking = it },
                label = "I currently work here",
                modifier = Modifier.padding(top = Spacing.xs)
            )
            LocalSkillTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                singleLine = false,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            if (datesError != null) {
                ErrorMessage(message = datesError, modifier = Modifier.padding(top = Spacing.sm))
            }

            LocalSkillPrimaryButton(
                text = "Save",
                enabled = jobTitleError == null && companyError == null && datesError == null,
                onClick = {
                    onSave(
                        initial.copy(
                            jobTitle = jobTitle.trim(),
                            company = company.trim(),
                            location = location.trim(),
                            employmentType = employmentType.trim(),
                            startDate = startDate ?: 0L,
                            endDate = endDate,
                            currentlyWorking = currentlyWorking,
                            description = description.trim()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg, bottom = Spacing.md)
            )
        }
    }
}
