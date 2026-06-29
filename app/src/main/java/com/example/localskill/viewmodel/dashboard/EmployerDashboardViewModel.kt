package com.example.localskill.viewmodel.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.application.ApplicationRepository
import com.example.localskill.repo.application.ApplicationRepositoryImpl
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.job.JobRepository
import com.example.localskill.repo.job.JobRepositoryImpl
import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.Resource

data class EmployerDashboardUiState(
    val user: UserModel? = null,
    val jobs: List<JobModel> = emptyList(),
    val applications: List<ApplicationModel> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EmployerDashboardViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val jobRepository: JobRepository = JobRepositoryImpl(),
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(EmployerDashboardUiState())
        private set

    fun load() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }
        userRepository.getUser(userId) { result ->
            when (result) {
                Resource.Loading -> uiState = uiState.copy(isLoading = true)
                is Resource.Error -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState = uiState.copy(user = result.data, isLoading = false)
            }
        }
        jobRepository.getJobsByEmployer(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, jobs = result.data)
            }
        }
        applicationRepository.getApplicationsForEmployer(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, applications = result.data)
            }
        }
        notificationRepository.getUserNotifications(userId) { result ->
            if (result is Resource.Success) {
                uiState = uiState.copy(unreadNotifications = result.data.count { !it.isRead })
            }
        }
    }

    fun logout() = authRepository.logout()
}
