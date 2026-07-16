package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobFilterModel
import com.example.localskill.model.JobModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.utils.JobFilterUtils
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class JobSearchUiState(
    val isLoading: Boolean = false,
    val allJobs: List<JobModel> = emptyList(),
    val results: List<JobModel> = emptyList(),
    val filter: JobFilterModel = JobFilterModel(),
    val categories: List<JobCategoryModel> = emptyList(),
    val savedJobIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

data class JobDetailsUiState(
    val isLoading: Boolean = true,
    val job: JobModel? = null,
    val isSaved: Boolean = false,
    val hasApplied: Boolean = false,
    val applicationStatus: String? = null,
    val notFound: Boolean = false,
    val errorMessage: String? = null
)

sealed class JobEvent {
    data class ShowMessage(val message: String) : JobEvent()
}

private const val SEARCH_DEBOUNCE_MILLIS = 300L

class JobViewModel(
    private val authRepo: AuthRepo,
    private val jobRepo: JobRepo,
    private val savedJobRepo: SavedJobRepo,
    private val applicationRepo: ApplicationRepo
) : ViewModel() {

    private val _searchUiState = MutableStateFlow(JobSearchUiState())
    val searchUiState: StateFlow<JobSearchUiState> = _searchUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(JobDetailsUiState())
    val detailsUiState: StateFlow<JobDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<JobEvent>(Channel.BUFFERED)
    val events: Flow<JobEvent> = _events.receiveAsFlow()

    private var searchDebounceJob: Job? = null
    private var jobsLoaded = false

    init {
        observeSavedJobs()
    }

    /** Loads jobs once per ViewModel instance; safe to call repeatedly from LaunchedEffect(Unit). */
    fun ensureJobsLoaded(initialQuery: String = "") {
        if (jobsLoaded) return
        jobsLoaded = true
        if (initialQuery.isNotBlank()) {
            _searchUiState.value = _searchUiState.value.copy(
                filter = _searchUiState.value.filter.copy(query = initialQuery)
            )
        }
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _searchUiState.value = _searchUiState.value.copy(isLoading = true, errorMessage = null)
            val jobsResult = jobRepo.getActiveJobs()
            val categoriesResult = jobRepo.getCategories()
            when (jobsResult) {
                is ResultState.Success -> _searchUiState.value = _searchUiState.value.copy(
                    isLoading = false,
                    allJobs = jobsResult.data,
                    categories = (categoriesResult as? ResultState.Success)?.data ?: emptyList(),
                    results = JobFilterUtils.applyFilters(jobsResult.data, _searchUiState.value.filter)
                )

                is ResultState.Error -> _searchUiState.value =
                    _searchUiState.value.copy(isLoading = false, errorMessage = jobsResult.message)

                else -> Unit
            }
        }
    }

    fun onQueryChanged(query: String) {
        _searchUiState.value = _searchUiState.value.copy(filter = _searchUiState.value.filter.copy(query = query))
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            applyFiltersToState()
        }
    }

    fun clearQuery() {
        searchDebounceJob?.cancel()
        _searchUiState.value = _searchUiState.value.copy(filter = _searchUiState.value.filter.copy(query = ""))
        applyFiltersToState()
    }

    fun updateFilter(transform: (JobFilterModel) -> JobFilterModel) {
        _searchUiState.value = _searchUiState.value.copy(filter = transform(_searchUiState.value.filter))
        applyFiltersToState()
    }

    fun clearAllFilters() {
        _searchUiState.value = _searchUiState.value.copy(
            filter = JobFilterModel(query = _searchUiState.value.filter.query)
        )
        applyFiltersToState()
    }

    private fun applyFiltersToState() {
        val state = _searchUiState.value
        _searchUiState.value = state.copy(results = JobFilterUtils.applyFilters(state.allJobs, state.filter))
    }

    private fun observeSavedJobs() {
        val userId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            savedJobRepo.observeSavedJobIds(userId).collect { ids ->
                _searchUiState.value = _searchUiState.value.copy(savedJobIds = ids)
                val currentJobId = _detailsUiState.value.job?.id
                if (currentJobId != null) {
                    _detailsUiState.value = _detailsUiState.value.copy(isSaved = ids.contains(currentJobId))
                }
            }
        }
    }

    fun toggleSaveJob(jobId: String) {
        val userId = authRepo.currentUserId() ?: return
        val alreadySaved = _searchUiState.value.savedJobIds.contains(jobId)
        viewModelScope.launch {
            val result = if (alreadySaved) savedJobRepo.unsaveJob(userId, jobId) else savedJobRepo.saveJob(userId, jobId)
            if (result is ResultState.Error) {
                _events.send(JobEvent.ShowMessage(result.message))
            }
        }
    }

    fun loadJobDetails(jobId: String) {
        val userId = authRepo.currentUserId()
        viewModelScope.launch {
            _detailsUiState.value = JobDetailsUiState(isLoading = true)
            when (val jobResult = jobRepo.getJobById(jobId)) {
                is ResultState.Success -> {
                    var applicationStatus: String? = null
                    if (userId != null) {
                        val appliedResult = applicationRepo.hasApplied(userId, jobId)
                        if (appliedResult is ResultState.Success && appliedResult.data) {
                            val applications = applicationRepo.getUserApplications(userId)
                            if (applications is ResultState.Success) {
                                applicationStatus = applications.data.firstOrNull { it.jobId == jobId }?.status
                            }
                        }
                    }
                    _detailsUiState.value = JobDetailsUiState(
                        isLoading = false,
                        job = jobResult.data,
                        isSaved = _searchUiState.value.savedJobIds.contains(jobId),
                        hasApplied = applicationStatus != null,
                        applicationStatus = applicationStatus
                    )
                }

                is ResultState.Error -> _detailsUiState.value = JobDetailsUiState(
                    isLoading = false,
                    notFound = true,
                    errorMessage = jobResult.message
                )

                else -> Unit
            }
        }
    }
}
