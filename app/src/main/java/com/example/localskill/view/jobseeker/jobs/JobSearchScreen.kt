package com.example.localskill.view.jobseeker.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.model.JobFilterModel
import com.example.localskill.model.JobSortOption
import com.example.localskill.view.common.components.JobCard
import com.example.localskill.view.common.components.LocalSkillSnackbarHost
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobEvent
import com.example.localskill.viewmodel.JobViewModel

@Composable
fun JobSearchScreen(
    viewModel: JobViewModel,
    initialQuery: String,
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.searchUiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.ensureJobsLoaded(initialQuery)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is JobEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
            LocalSkillTextField(
                value = uiState.filter.query,
                onValueChange = viewModel::onQueryChanged,
                label = "Search jobs, companies, or skills",
                trailingIcon = if (uiState.filter.query.isNotBlank()) {
                    {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                } else {
                    null
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val count = uiState.results.size
                Text(
                    text = "$count job${if (count == 1) "" else "s"} found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Box {
                        LocalSkillTextButton(text = "Sort", onClick = { showSortMenu = true })
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            JobSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(sortOptionLabel(option)) },
                                    onClick = {
                                        viewModel.updateFilter { it.copy(sortOption = option.name) }
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    LocalSkillTextButton(
                        text = if (uiState.filter.activeFilterCount > 0) {
                            "Filters (${uiState.filter.activeFilterCount})"
                        } else {
                            "Filters"
                        },
                        onClick = { showFilterSheet = true }
                    )
                }
            }

            if (uiState.filter.hasActiveFilters) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.padding(top = Spacing.xs)
                ) {
                    items(activeFilterLabels(uiState.filter)) { (label, clear) ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.updateFilter(clear) },
                            label = { Text(label) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.height(16.dp)
                                )
                            }
                        )
                    }
                    item {
                        LocalSkillTextButton(text = "Clear all", onClick = viewModel::clearAllFilters)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            when {
                uiState.isLoading -> FullScreenLoading()

                uiState.errorMessage != null && uiState.results.isEmpty() -> Column {
                    ErrorMessage(message = uiState.errorMessage.orEmpty())
                    LocalSkillTextButton(text = "Retry", onClick = viewModel::loadJobs)
                }

                uiState.results.isEmpty() -> EmptyState(
                    title = "No jobs found",
                    description = "Try adjusting your search or filters."
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(uiState.results, key = { it.id }) { job ->
                        JobCard(
                            job = job,
                            isSaved = uiState.savedJobIds.contains(job.id),
                            onSaveToggle = { viewModel.toggleSaveJob(job.id) },
                            onClick = { onJobClick(job.id) }
                        )
                    }
                }
            }
        }

        LocalSkillSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showFilterSheet) {
        JobFilterSheet(
            filter = uiState.filter,
            categories = uiState.categories,
            onApply = { updated -> viewModel.updateFilter { updated } },
            onDismiss = { showFilterSheet = false }
        )
    }
}

private fun sortOptionLabel(option: JobSortOption): String = when (option) {
    JobSortOption.MOST_RELEVANT -> "Most relevant"
    JobSortOption.NEWEST -> "Newest"
    JobSortOption.SALARY_HIGH_TO_LOW -> "Salary: high to low"
    JobSortOption.SALARY_LOW_TO_HIGH -> "Salary: low to high"
    JobSortOption.DEADLINE_SOONEST -> "Deadline soonest"
}

private fun activeFilterLabels(filter: JobFilterModel): List<Pair<String, (JobFilterModel) -> JobFilterModel>> =
    buildList {
        filter.categoryId?.let { add("Category" to { f: JobFilterModel -> f.copy(categoryId = null) }) }
        filter.location?.let { add("Location: $it" to { f: JobFilterModel -> f.copy(location = null) }) }
        filter.jobType?.let { add(it.replace('_', ' ') to { f: JobFilterModel -> f.copy(jobType = null) }) }
        filter.workplaceType?.let { add(it to { f: JobFilterModel -> f.copy(workplaceType = null) }) }
        filter.experienceLevel?.let { add(it to { f: JobFilterModel -> f.copy(experienceLevel = null) }) }
        if (filter.minimumSalary != null || filter.maximumSalary != null) {
            add("Salary" to { f: JobFilterModel -> f.copy(minimumSalary = null, maximumSalary = null) })
        }
        filter.datePostedWithinDays?.let { add("Recent" to { f: JobFilterModel -> f.copy(datePostedWithinDays = null) }) }
        if (filter.verifiedCompanyOnly) {
            add("Verified only" to { f: JobFilterModel -> f.copy(verifiedCompanyOnly = false) })
        }
    }
