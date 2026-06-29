package com.example.localskill.viewmodel.worker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.ApplicationModel
import com.example.localskill.repo.application.ApplicationRepository
import com.example.localskill.repo.application.ApplicationRepositoryImpl
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.review.ReviewRepository
import com.example.localskill.repo.review.ReviewRepositoryImpl
import com.example.localskill.utils.Resource

data class WorkerApplicationsUiState(
    val applications: List<ApplicationModel> = emptyList(),
    val reviewedApplicationIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ApplicationViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(WorkerApplicationsUiState())
        private set

    fun load() {
        val workerId = authRepository.currentUserId()
        if (workerId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }

        applicationRepository.getApplicationsForWorker(workerId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, applications = result.data)
            }
        }
        reviewRepository.getReviewsByUser(workerId) { result ->
            if (result is Resource.Success) {
                uiState = uiState.copy(reviewedApplicationIds = result.data.map { it.applicationId }.toSet())
            }
            if (result is Resource.Error) {
                uiState = uiState.copy(errorMessage = result.message)
            }
        }
    }
}
