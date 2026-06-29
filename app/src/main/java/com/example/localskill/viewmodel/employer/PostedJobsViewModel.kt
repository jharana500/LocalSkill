package com.example.localskill.viewmodel.employer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.JobModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.job.JobRepository
import com.example.localskill.repo.job.JobRepositoryImpl
import com.example.localskill.utils.Resource

data class PostedJobsUiState(
    val jobs: List<JobModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PostedJobsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val jobRepository: JobRepository = JobRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(PostedJobsUiState())
        private set

    fun load() {
        val employerId = authRepository.currentUserId()
        if (employerId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }

        jobRepository.getJobsByEmployer(employerId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, jobs = result.data)
            }
        }
    }
}
