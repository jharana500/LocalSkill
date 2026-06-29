package com.example.localskill.viewmodel.worker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.JobModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.job.JobRepository
import com.example.localskill.repo.job.JobRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.LocationUtils
import com.example.localskill.utils.Resource

data class WorkerJobsUiState(
    val jobs: List<JobModel> = emptyList(),
    val filteredJobs: List<JobModel> = emptyList(),
    val searchQuery: String = "",
    val selectedJobType: String = "",
    val selectedLocation: String = "",
    val selectedRadiusKm: Double? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val userCity: String = "",
    val userArea: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class JobViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val jobRepository: JobRepository = JobRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(WorkerJobsUiState())
        private set

    fun loadOpenJobs() {
        authRepository.currentUserId()?.let { userId ->
            userRepository.getUser(userId) { result ->
                if (result is Resource.Success) {
                    uiState = uiState.copy(
                        userLatitude = result.data.latitude,
                        userLongitude = result.data.longitude,
                        userCity = result.data.city,
                        userArea = result.data.area
                    )
                    uiState = uiState.copy(filteredJobs = filterJobs(uiState.jobs))
                }
            }
        }
        jobRepository.getOpenJobs { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(
                    jobs = result.data,
                    filteredJobs = filterJobs(result.data),
                    isLoading = false
                )
            }
        }
    }

    fun onSearchChange(value: String) {
        uiState = uiState.copy(
            searchQuery = value,
            filteredJobs = filterJobs(uiState.jobs, searchQuery = value),
            errorMessage = null
        )
    }

    fun onJobTypeSelected(value: String) {
        uiState = uiState.copy(
            selectedJobType = value,
            filteredJobs = filterJobs(uiState.jobs, jobType = value),
            errorMessage = null
        )
    }

    fun onRadiusSelected(value: Double?) {
        uiState = uiState.copy(
            selectedRadiusKm = value,
            filteredJobs = filterJobs(uiState.jobs, radiusKm = value),
            errorMessage = null
        )
    }

    fun distanceText(job: JobModel): String? =
        LocationUtils.distanceKmOrNull(uiState.userLatitude, uiState.userLongitude, job.latitude, job.longitude)
            ?.let(LocationUtils::formatDistance)

    private fun filterJobs(
        jobs: List<JobModel>,
        searchQuery: String = uiState.searchQuery,
        jobType: String = uiState.selectedJobType,
        radiusKm: Double? = uiState.selectedRadiusKm
    ): List<JobModel> {
        val query = searchQuery
        val normalizedQuery = query.trim().lowercase()
        val normalizedType = jobType.trim().lowercase()
        val filtered = jobs.filter { job ->
            val matchesQuery = normalizedQuery.isBlank() ||
                job.title.contains(normalizedQuery, ignoreCase = true) ||
                job.requiredSkills.contains(normalizedQuery, ignoreCase = true) ||
                job.location.contains(normalizedQuery, ignoreCase = true) ||
                job.city.contains(normalizedQuery, ignoreCase = true) ||
                job.area.contains(normalizedQuery, ignoreCase = true) ||
                job.jobType.contains(normalizedQuery, ignoreCase = true)
            val matchesType = normalizedType.isBlank() || job.jobType.equals(jobType, ignoreCase = true)
            val distance = LocationUtils.distanceKmOrNull(uiState.userLatitude, uiState.userLongitude, job.latitude, job.longitude)
            val matchesRadius = radiusKm == null || distance?.let { it <= radiusKm } ?: textLocationMatches(job)
            matchesQuery && matchesType && matchesRadius
        }
        return filtered.sortedWith(compareBy<JobModel> {
            LocationUtils.distanceKmOrNull(uiState.userLatitude, uiState.userLongitude, it.latitude, it.longitude)
                ?: Double.MAX_VALUE
        }.thenByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.createdAt })
    }

    private fun textLocationMatches(job: JobModel): Boolean {
        if (uiState.userCity.isNotBlank() && job.city.equals(uiState.userCity, ignoreCase = true)) return true
        if (uiState.userArea.isNotBlank() && job.area.equals(uiState.userArea, ignoreCase = true)) return true
        return false
    }
}
