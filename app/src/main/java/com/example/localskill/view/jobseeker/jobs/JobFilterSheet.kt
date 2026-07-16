package com.example.localskill.view.jobseeker.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobFilterModel
import com.example.localskill.model.JobType
import com.example.localskill.model.WorkplaceType
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.SectionHeader
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.Spacing

private val EXPERIENCE_LEVELS = listOf("Entry", "Mid", "Senior", "Lead")
private val DATE_POSTED_OPTIONS = listOf(1 to "Today", 7 to "This week", 30 to "This month")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobFilterSheet(
    filter: JobFilterModel,
    categories: List<JobCategoryModel>,
    onApply: (JobFilterModel) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(filter) { mutableStateOf(filter) }
    val sheetState = rememberModalBottomSheetState()
    val salaryError = JobValidationUtils.validateSalaryRange(draft.minimumSalary, draft.maximumSalary)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Text(text = "Filters", style = MaterialTheme.typography.titleLarge)

            SectionHeader(title = "Category", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = draft.categoryId == category.id,
                        onClick = {
                            draft = draft.copy(categoryId = if (draft.categoryId == category.id) null else category.id)
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            SectionHeader(title = "Job type", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                JobType.entries.forEach { type ->
                    FilterChip(
                        selected = draft.jobType == type.name,
                        onClick = { draft = draft.copy(jobType = if (draft.jobType == type.name) null else type.name) },
                        label = { Text(type.name.replace('_', ' ')) }
                    )
                }
            }

            SectionHeader(title = "Workplace", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                WorkplaceType.entries.forEach { type ->
                    FilterChip(
                        selected = draft.workplaceType == type.name,
                        onClick = {
                            draft = draft.copy(workplaceType = if (draft.workplaceType == type.name) null else type.name)
                        },
                        label = { Text(type.name) }
                    )
                }
            }

            SectionHeader(title = "Experience level", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                EXPERIENCE_LEVELS.forEach { level ->
                    FilterChip(
                        selected = draft.experienceLevel == level,
                        onClick = {
                            draft = draft.copy(experienceLevel = if (draft.experienceLevel == level) null else level)
                        },
                        label = { Text(level) }
                    )
                }
            }

            SectionHeader(title = "Location", modifier = Modifier.padding(top = Spacing.md))
            LocalSkillTextField(
                value = draft.location.orEmpty(),
                onValueChange = { draft = draft.copy(location = it.ifBlank { null }) },
                label = "City or location"
            )

            SectionHeader(title = "Salary range (NPR/month)", modifier = Modifier.padding(top = Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LocalSkillTextField(
                    value = draft.minimumSalary?.toLong()?.toString().orEmpty(),
                    onValueChange = { text -> draft = draft.copy(minimumSalary = text.toDoubleOrNull()) },
                    label = "Min",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                LocalSkillTextField(
                    value = draft.maximumSalary?.toLong()?.toString().orEmpty(),
                    onValueChange = { text -> draft = draft.copy(maximumSalary = text.toDoubleOrNull()) },
                    label = "Max",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            if (salaryError != null) {
                ErrorMessage(message = salaryError, modifier = Modifier.padding(top = Spacing.xs))
            }

            SectionHeader(title = "Date posted", modifier = Modifier.padding(top = Spacing.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                DATE_POSTED_OPTIONS.forEach { (days, label) ->
                    FilterChip(
                        selected = draft.datePostedWithinDays == days,
                        onClick = {
                            draft = draft.copy(
                                datePostedWithinDays = if (draft.datePostedWithinDays == days) null else days
                            )
                        },
                        label = { Text(label) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Spacing.md)
            ) {
                Switch(
                    checked = draft.verifiedCompanyOnly,
                    onCheckedChange = { draft = draft.copy(verifiedCompanyOnly = it) }
                )
                Text(text = "Verified companies only", modifier = Modifier.padding(start = Spacing.sm))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg, bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                LocalSkillSecondaryButton(
                    text = "Reset",
                    onClick = { draft = JobFilterModel(query = draft.query, sortOption = draft.sortOption) },
                    modifier = Modifier.weight(1f)
                )
                LocalSkillPrimaryButton(
                    text = "Apply filters",
                    onClick = {
                        if (salaryError == null) {
                            onApply(draft)
                            onDismiss()
                        }
                    },
                    enabled = salaryError == null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
