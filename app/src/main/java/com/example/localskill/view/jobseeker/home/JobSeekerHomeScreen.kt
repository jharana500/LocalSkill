package com.example.localskill.view.jobseeker.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.JobCard
import com.example.localskill.view.common.components.JobCardSkeleton
import com.example.localskill.view.common.components.LocalSkillSnackbarHost
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.RemoteAvatar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.common.states.FullScreenLoading
import com.example.localskill.view.jobseeker.home.components.CategoryCard
import com.example.localskill.view.jobseeker.home.components.HeroBanner
import com.example.localskill.view.jobseeker.home.components.HomeSectionHeader
import com.example.localskill.view.jobseeker.home.components.ProfileCompletionCard
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.JobSeekerHomeEvent
import com.example.localskill.viewmodel.JobSeekerHomeViewModel

@Composable
fun JobSeekerHomeScreen(
    viewModel: JobSeekerHomeViewModel,
    onSearchSubmit: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllJobsClick: () -> Unit,
    onJobClick: (String) -> Unit,
    onCompleteProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is JobSeekerHomeEvent.NavigateToExplore -> onSearchSubmit(event.query)
                is JobSeekerHomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> FullScreenLoading()

            uiState.errorMessage != null && uiState.featuredJobs.isEmpty() && uiState.recentJobs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.padding(Spacing.lg)) {
                        ErrorMessage(message = uiState.errorMessage.orEmpty())
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    item {
                        HomeHeader(
                            fullName = uiState.fullName,
                            city = uiState.profile?.city.orEmpty(),
                            profileImageUrl = uiState.profile?.profileImageUrl,
                            isRefreshing = uiState.isRefreshing,
                            onRefreshClick = viewModel::refresh
                        )
                    }

                    item {
                        LocalSkillTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            label = "Search jobs, companies, or skills",
                            imeAction = ImeAction.Search,
                            onImeAction = viewModel::onSearchSubmit,
                            trailingIcon = {
                                IconButton(onClick = viewModel::onSearchSubmit) {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )
                    }

                    item { HeroBanner() }

                    if (uiState.categories.isNotEmpty()) {
                        item {
                            HomeSectionHeader(title = "Categories", onSeeAllClick = onSeeAllJobsClick)
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                items(uiState.categories, key = { it.id }) { category ->
                                    CategoryCard(category = category, onClick = { onCategoryClick(category.id) })
                                }
                            }
                        }
                    }

                    item {
                        ProfileCompletionCard(
                            completionPercentage = uiState.profileCompletion,
                            missingSections = uiState.missingProfileSections,
                            onCompleteProfileClick = onCompleteProfileClick
                        )
                    }

                    item {
                        HomeSectionHeader(title = "Featured jobs", onSeeAllClick = onSeeAllJobsClick)
                    }

                    if (uiState.featuredJobs.isEmpty()) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                items(2) { JobCardSkeleton(modifier = Modifier.size(280.dp, 160.dp)) }
                            }
                        }
                    } else {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                items(uiState.featuredJobs, key = { it.id }) { job ->
                                    JobCard(
                                        job = job,
                                        isSaved = uiState.savedJobIds.contains(job.id),
                                        onSaveToggle = { viewModel.toggleSaveJob(job.id) },
                                        onClick = { onJobClick(job.id) },
                                        modifier = Modifier.size(280.dp, 190.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { HomeSectionHeader(title = "Recent jobs") }

                    if (uiState.recentJobs.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No jobs available yet",
                                description = "Check back soon for new opportunities."
                            )
                        }
                    } else {
                        items(uiState.recentJobs, key = { it.id }) { job ->
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
        }

        LocalSkillSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HomeHeader(
    fullName: String,
    city: String,
    profileImageUrl: String?,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    val firstName = fullName.trim().substringBefore(" ").ifBlank { "there" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RemoteAvatar(imageUrl = profileImageUrl, fallbackText = fullName.ifBlank { "?" })
            Column(modifier = Modifier.padding(start = Spacing.sm)) {
                Text(
                    text = "Hi, $firstName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (city.isNotBlank()) {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        IconButton(onClick = onRefreshClick) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh dashboard")
            }
        }
    }
}
