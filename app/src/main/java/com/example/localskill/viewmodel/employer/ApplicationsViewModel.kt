package com.example.localskill.viewmodel.employer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.repo.application.ApplicationRepository
import com.example.localskill.repo.application.ApplicationRepositoryImpl
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.review.ReviewRepository
import com.example.localskill.repo.review.ReviewRepositoryImpl
import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.utils.Resource

data class EmployerApplicationsUiState(
    val applications: List<ApplicationModel> = emptyList(),
    val pendingApplications: List<ApplicationModel> = emptyList(),
    val acceptedApplications: List<ApplicationModel> = emptyList(),
    val rejectedApplications: List<ApplicationModel> = emptyList(),
    val reviewedApplicationIds: Set<String> = emptySet(),
    val selectedStatusFilter: String = "ALL",
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val visibleApplications: List<ApplicationModel>
        get() = when (selectedStatusFilter) {
            ApplicationStatus.PENDING.name -> pendingApplications
            ApplicationStatus.ACCEPTED.name -> acceptedApplications
            ApplicationStatus.REJECTED.name -> rejectedApplications
            else -> applications
        }
}

class EmployerApplicationsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(EmployerApplicationsUiState())
        private set

    fun load() {
        val employerId = authRepository.currentUserId()
        if (employerId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }

        applicationRepository.getApplicationsForEmployer(employerId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.withApplications(result.data).copy(isLoading = false)
            }
        }
        reviewRepository.getReviewsByUser(employerId) { result ->
            if (result is Resource.Success) {
                uiState = uiState.copy(reviewedApplicationIds = result.data.map { it.applicationId }.toSet())
            }
            if (result is Resource.Error) {
                uiState = uiState.copy(errorMessage = result.message)
            }
        }
    }

    fun selectStatusFilter(status: String) {
        uiState = uiState.copy(selectedStatusFilter = status, errorMessage = null, successMessage = null)
    }

    fun accept(application: ApplicationModel) {
        updateStatus(application, ApplicationStatus.ACCEPTED.name)
    }

    fun reject(application: ApplicationModel) {
        updateStatus(application, ApplicationStatus.REJECTED.name)
    }

    private fun updateStatus(application: ApplicationModel, status: String) {
        if (application.status != ApplicationStatus.PENDING.name) return
        applicationRepository.updateApplicationStatus(application, status) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isUpdating = true, errorMessage = null, successMessage = null)
                is Resource.Error -> uiState.copy(isUpdating = false, errorMessage = result.message)
                is Resource.Success -> {
                    val updated = uiState.applications.map {
                        if (it.id == application.id) it.copy(status = status, updatedAt = System.currentTimeMillis()) else it
                    }
                    createStatusNotification(application, status)
                    uiState.withApplications(updated).copy(
                        isUpdating = false,
                        successMessage = if (status == ApplicationStatus.ACCEPTED.name) "Application accepted." else "Application rejected."
                    )
                }
            }
        }
    }

    private fun createStatusNotification(application: ApplicationModel, status: String) {
        val type = if (status == ApplicationStatus.ACCEPTED.name) {
            NotificationType.APPLICATION_ACCEPTED
        } else {
            NotificationType.APPLICATION_REJECTED
        }
        notificationRepository.createNotification(
            NotificationModel(
                receiverId = application.workerId,
                senderId = application.employerId,
                senderName = "Employer",
                title = if (status == ApplicationStatus.ACCEPTED.name) "Application Accepted" else "Application Rejected",
                message = if (status == ApplicationStatus.ACCEPTED.name) {
                    "Your application has been accepted."
                } else {
                    "Your application was not selected for this job."
                },
                type = type.name,
                relatedId = application.id,
                relatedType = "APPLICATION"
            )
        ) { }
    }
}

private fun EmployerApplicationsUiState.withApplications(applications: List<ApplicationModel>): EmployerApplicationsUiState =
    copy(
        applications = applications,
        pendingApplications = applications.filter { it.status == ApplicationStatus.PENDING.name },
        acceptedApplications = applications.filter { it.status == ApplicationStatus.ACCEPTED.name },
        rejectedApplications = applications.filter { it.status == ApplicationStatus.REJECTED.name }
    )
