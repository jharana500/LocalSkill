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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.EducationModel
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

@Composable
fun ManageEducationScreen(
    viewModel: JobSeekerProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingEntry by remember { mutableStateOf<EducationModel?>(null) }
    var entryPendingDelete by remember { mutableStateOf<EducationModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Education", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingEntry = EducationModel() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add education")
            }
        }
    ) { innerPadding ->
        if (uiState.profile.education.isEmpty()) {
            EmptyState(
                title = "No education added",
                description = "Add your education to strengthen your profile.",
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.profile.education, key = { it.id }) { entry ->
                    EducationRow(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { entryPendingDelete = entry }
                    )
                }
            }
        }
    }

    editingEntry?.let { entry ->
        EducationFormSheet(
            initial = entry,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                if (entry.id.isBlank()) viewModel.addEducation(updated) else viewModel.updateEducation(updated)
                editingEntry = null
            }
        )
    }

    entryPendingDelete?.let { entry ->
        ConfirmationDialog(
            title = "Delete education?",
            message = "This will remove \"${entry.institution}\" from your profile.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.removeEducation(entry.id)
                entryPendingDelete = null
            },
            onDismiss = { entryPendingDelete = null }
        )
    }
}

@Composable
private fun EducationRow(entry: EducationModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    LocalSkillCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.institution, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                if (entry.qualification.isNotBlank()) {
                    Text(text = entry.qualification, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = DateUtils.formatEducationDuration(entry.startYear, entry.endYear, entry.currentlyStudying),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit education") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete education") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EducationFormSheet(
    initial: EducationModel,
    onDismiss: () -> Unit,
    onSave: (EducationModel) -> Unit
) {
    var institution by remember { mutableStateOf(initial.institution) }
    var qualification by remember { mutableStateOf(initial.qualification) }
    var fieldOfStudy by remember { mutableStateOf(initial.fieldOfStudy) }
    var startYear by remember { mutableStateOf(if (initial.startYear > 0) initial.startYear.toString() else "") }
    var endYear by remember { mutableStateOf(initial.endYear?.takeIf { it > 0 }?.toString().orEmpty()) }
    var currentlyStudying by remember { mutableStateOf(initial.currentlyStudying) }
    var description by remember { mutableStateOf(initial.description) }

    val sheetState = rememberModalBottomSheetState()
    val institutionError = if (institution.isBlank()) "Institution is required" else null
    val qualificationError = if (qualification.isBlank()) "Qualification is required" else null
    val yearsError = JobValidationUtils.validateEducationYears(
        startYear.toIntOrNull() ?: 0,
        endYear.toIntOrNull(),
        currentlyStudying
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Text(text = if (initial.id.isBlank()) "Add education" else "Edit education", style = MaterialTheme.typography.titleLarge)

            LocalSkillTextField(
                value = institution,
                onValueChange = { institution = it },
                label = "Institution",
                errorMessage = institutionError,
                modifier = Modifier.padding(top = Spacing.md)
            )
            LocalSkillTextField(
                value = qualification,
                onValueChange = { qualification = it },
                label = "Qualification",
                errorMessage = qualificationError,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            LocalSkillTextField(
                value = fieldOfStudy,
                onValueChange = { fieldOfStudy = it },
                label = "Field of study",
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Row(modifier = Modifier.padding(top = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LocalSkillTextField(
                    value = startYear,
                    onValueChange = { startYear = it.filter(Char::isDigit) },
                    label = "Start year",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                LocalSkillTextField(
                    value = endYear,
                    onValueChange = { endYear = it.filter(Char::isDigit) },
                    label = "End year",
                    enabled = !currentlyStudying,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            LocalSkillCheckbox(
                checked = currentlyStudying,
                onCheckedChange = { currentlyStudying = it },
                label = "I currently study here",
                modifier = Modifier.padding(top = Spacing.xs)
            )
            LocalSkillTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                singleLine = false,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            if (yearsError != null) {
                ErrorMessage(message = yearsError, modifier = Modifier.padding(top = Spacing.sm))
            }

            LocalSkillPrimaryButton(
                text = "Save",
                enabled = institutionError == null && qualificationError == null && yearsError == null,
                onClick = {
                    onSave(
                        initial.copy(
                            institution = institution.trim(),
                            qualification = qualification.trim(),
                            fieldOfStudy = fieldOfStudy.trim(),
                            startYear = startYear.toIntOrNull() ?: 0,
                            endYear = if (currentlyStudying) null else endYear.toIntOrNull(),
                            currentlyStudying = currentlyStudying,
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
