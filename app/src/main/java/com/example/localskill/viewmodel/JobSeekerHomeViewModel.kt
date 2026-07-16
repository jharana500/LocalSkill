package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class JobSeekerHomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val fullName: String = "",
    val profile: JobSeekerProfileModel? = null,
    val profileCompletion: Int = 0,
    val missingProfileSections: List<String> = emptyList(),
    val categories: List<JobCategoryModel> = emptyList(),
    val featuredJobs: List<JobModel> = emptyList(),
    val recentJobs: List<JobModel> = emptyList(),
    val savedJobIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

sealed class JobSeekerHomeEvent {
    data class NavigateToExplore(val query: String) : JobSeekerHomeEvent()
    data class ShowMessage(val message: String) : JobSeekerHomeEvent()
}

class JobSeekerHomeViewModel(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val jobRepo: JobRepo,
    private val savedJobRepo: SavedJobRepo,
    private val jobSeekerProfileRepo: JobSeekerProfileRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerHomeUiState())
    val uiState: StateFlow<JobSeekerHomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = Channel<JobSeekerHomeEvent>(Channel.BUFFERED)
    val events: Flow<JobSeekerHomeEvent> = _events.receiveAsFlow()

    init {
        loadDashboard()
        observeSavedJobs()
    }

    private fun currentUserId(): String? = authRepo.currentUserId()

    fun loadDashboard(isRefresh: Boolean = false) {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )

            val userResult = userRepo.getUserById(userId)
            val profileResult = jobSeekerProfileRepo.getProfile(userId)
            val categoriesResult = jobRepo.getCategories()
            val featuredResult = jobRepo.getFeaturedJobs()
            val recentResult = jobRepo.getRecentJobs()

            val errorMessage = listOfNotNull(
                (profileResult as? ResultState.Error)?.message,
                (categoriesResult as? ResultState.Error)?.message,
                (featuredResult as? ResultState.Error)?.message,
                (recentResult as? ResultState.Error)?.message
            ).firstOrNull()

            val profile = (profileResult as? ResultState.Success)?.data

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                fullName = (userResult as? ResultState.Success)?.data?.fullName.orEmpty(),
                profile = profile,
                profileCompletion = profile?.completionPercentage ?: 0,
                missingProfileSections = profile?.missingSections ?: emptyList(),
                categories = (categoriesResult as? ResultState.Success)?.data ?: emptyList(),
                featuredJobs = (featuredResult as? ResultState.Success)?.data ?: emptyList(),
                recentJobs = (recentResult as? ResultState.Success)?.data ?: emptyList(),
                errorMessage = errorMessage
            )
        }
    }

    fun refresh() = loadDashboard(isRefresh = true)

    private fun observeSavedJobs() {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            savedJobRepo.observeSavedJobIds(userId).collect { ids ->
                _uiState.value = _uiState.value.copy(savedJobIds = ids)
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    fun onSearchSubmit() {
        viewModelScope.launch {
            _events.send(JobSeekerHomeEvent.NavigateToExplore(_searchQuery.value.trim()))
        }
    }

    fun toggleSaveJob(jobId: String) {
        val userId = currentUserId() ?: return
        val currentlySaved = _uiState.value.savedJobIds.contains(jobId)
        viewModelScope.launch {
            val result = if (currentlySaved) {
                savedJobRepo.unsaveJob(userId, jobId)
            } else {
                savedJobRepo.saveJob(userId, jobId)
            }
            if (result is ResultState.Error) {
                _events.send(JobSeekerHomeEvent.ShowMessage(result.message))
            }
        }
    }
}
