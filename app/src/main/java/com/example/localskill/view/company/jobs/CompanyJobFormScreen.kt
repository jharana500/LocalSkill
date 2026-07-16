package com.example.localskill.view.company.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobType
import com.example.localskill.model.WorkplaceType
import com.example.localskill.utils.DateUtils
import com.example.localskill.view.common.components.LocalSkillCard
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyJobEvent
import com.example.localskill.viewmodel.CompanyJobViewModel

private val EXPERIENCE_LEVELS = listOf("Entry", "Mid", "Senior", "Lead")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CompanyJobFormScreen(
    viewModel: CompanyJobViewModel,
    jobId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onPublished: () -> Unit
) {
    val uiState by viewModel.formUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeadlinePicker by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) { viewModel.loadForm(jobId) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompanyJobEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(uiState.publishSuccess) { if (uiState.publishSuccess) onPublished() }
    LaunchedEffect(uiState.saveSuccess) { if (uiState.saveSuccess) onSaved() }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = if (uiState.isEditingExisting) "Edit job" else "Post a job", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
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
            if (uiState.violations.isNotEmpty()) {
                ErrorMessage(message = uiState.violations.joinToString("\n"))
            }

            LocalSkillTextField(value = uiState.title, onValueChange = viewModel::updateTitle, label = "Job title")

            SectionHeader(title = "Category", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.categoryId == category.id,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category.name) }
                    )
                }
            }

            LocalSkillTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = "Description",
                singleLine = false,
                modifier = Modifier.padding(top = Spacing.md)
            )

            ListEditorSection(
                title = "Responsibilities",
                items = uiState.responsibilities,
                onAdd = viewModel::addResponsibility,
                onRemove = viewModel::removeResponsibility
            )

            ListEditorSection(
                title = "Requirements",
                items = uiState.requirements,
                onAdd = viewModel::addRequirement,
                onRemove = viewModel::removeRequirement
            )

            SectionHeader(title = "Skills", modifier = Modifier.padding(top = Spacing.md))
            SkillChipEditor(skills = uiState.skills, onAdd = viewModel::addSkill, onRemove = viewModel::removeSkill)

            LocalSkillTextField(
                value = uiState.location,
                onValueChange = viewModel::updateLocation,
                label = "Location",
                modifier = Modifier.padding(top = Spacing.md)
            )

            SectionHeader(title = "Job type", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                JobType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.jobType == type.name,
                        onClick = { viewModel.updateJobType(type.name) },
                        label = { Text(type.name.replace('_', ' ')) }
                    )
                }
            }

            SectionHeader(title = "Workplace", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                WorkplaceType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.workplaceType == type.name,
                        onClick = { viewModel.updateWorkplaceType(type.name) },
                        label = { Text(type.name) }
                    )
                }
            }

            SectionHeader(title = "Salary range (NPR/month)", modifier = Modifier.padding(top = Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LocalSkillTextField(
                    value = uiState.minimumSalary?.toLong()?.toString().orEmpty(),
                    onValueChange = { viewModel.updateMinimumSalary(it.toDoubleOrNull()) },
                    label = "Min",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                LocalSkillTextField(
                    value = uiState.maximumSalary?.toLong()?.toString().orEmpty(),
                    onValueChange = { viewModel.updateMaximumSalary(it.toDoubleOrNull()) },
                    label = "Max",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeader(title = "Experience level", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                EXPERIENCE_LEVELS.forEach { level ->
                    FilterChip(
                        selected = uiState.experienceLevel == level,
                        onClick = { viewModel.updateExperienceLevel(level) },
                        label = { Text(level) }
                    )
                }
            }

            LocalSkillTextField(
                value = uiState.educationRequirement,
                onValueChange = viewModel::updateEducationRequirement,
                label = "Education requirement",
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillTextField(
                value = uiState.vacancyCount.toString(),
                onValueChange = { text -> text.toIntOrNull()?.let { viewModel.updateVacancyCount(it) } },
                label = "Vacancy count",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(top = Spacing.md)
            )

            SectionHeader(title = "Application deadline", modifier = Modifier.padding(top = Spacing.md))
            LocalSkillCard(modifier = Modifier.padding(top = Spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.applicationDeadline > 0) DateUtils.formatDate(uiState.applicationDeadline) else "No deadline set"
                    )
                    LocalSkillSecondaryButton(text = "Choose date", onClick = { showDeadlinePicker = true })
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl, bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                LocalSkillSecondaryButton(
                    text = "Save draft",
                    onClick = viewModel::saveDraft,
                    modifier = Modifier.weight(1f)
                )
                LocalSkillPrimaryButton(
                    text = "Publish",
                    onClick = viewModel::publish,
                    isLoading = uiState.isPublishing,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDeadlinePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.applicationDeadline.takeIf { it > 0 }
        )
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDeadline(it) }
                    showDeadlinePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDeadlinePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ListEditorSection(
    title: String,
    items: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    SectionHeader(title = title, modifier = Modifier.padding(top = Spacing.md))
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "• $item", modifier = Modifier.weight(1f))
            IconButton(onClick = { onRemove(index) }) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        LocalSkillTextField(
            value = draft,
            onValueChange = { draft = it },
            label = "Add $title",
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = {
            onAdd(draft)
            draft = ""
        }) { Text("Add") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillChipEditor(
    skills: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        skills.forEach { skill ->
            FilterChip(selected = true, onClick = { onRemove(skill) }, label = { Text(skill) })
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Spacing.xs)) {
        LocalSkillTextField(
            value = draft,
            onValueChange = { draft = it },
            label = "Add a skill",
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = {
            onAdd(draft)
            draft = ""
        }) { Text("Add") }
    }
}
