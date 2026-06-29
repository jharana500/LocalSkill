package com.example.localskill.viewmodel.employer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.job.JobRepository
import com.example.localskill.repo.job.JobRepositoryImpl
import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.utils.Resource
import com.example.localskill.utils.Validators

data class PostJobUiState(
    val title: String = "",
    val description: String = "",
    val requiredSkills: String = "",
    val budget: String = "",
    val location: String = "",
    val area: String = "",
    val city: String = "",
    val jobType: String = "",
    val deadline: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaved: Boolean = false
)

class PostJobViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val jobRepository: JobRepository = JobRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(PostJobUiState())
        private set

    fun update(field: String, value: String) {
        uiState = when (field) {
            "title" -> uiState.copy(title = value, errorMessage = null, successMessage = null, isSaved = false)
            "description" -> uiState.copy(description = value, errorMessage = null, successMessage = null, isSaved = false)
            "requiredSkills" -> uiState.copy(requiredSkills = value, errorMessage = null, successMessage = null, isSaved = false)
            "budget" -> uiState.copy(budget = value, errorMessage = null, successMessage = null, isSaved = false)
            "location" -> uiState.copy(location = value, errorMessage = null, successMessage = null, isSaved = false)
            "area" -> uiState.copy(area = value, errorMessage = null, successMessage = null, isSaved = false)
            "city" -> uiState.copy(city = value, errorMessage = null, successMessage = null, isSaved = false)
            "jobType" -> uiState.copy(jobType = value, errorMessage = null, successMessage = null, isSaved = false)
            else -> uiState.copy(deadline = value, errorMessage = null, successMessage = null, isSaved = false)
        }
    }

    fun postJob() {
        val error = Validators.required(uiState.title, "Job title")
            ?: Validators.required(uiState.description, "Job description")
            ?: Validators.required(uiState.requiredSkills, "Required skills")
            ?: Validators.required(uiState.budget, "Budget or salary")
            ?: Validators.required(uiState.city, "Job city")
            ?: Validators.required(uiState.location, "Job location")
            ?: Validators.required(uiState.jobType, "Job type")
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }
        val employerId = authRepository.currentUserId()
        if (employerId == null) {
            uiState = uiState.copy(errorMessage = "Please login again")
            return
        }
        val job = JobModel(
            employerId = employerId,
            title = uiState.title.trim(),
            description = uiState.description.trim(),
            requiredSkills = uiState.requiredSkills.trim(),
            budget = uiState.budget.trim(),
            location = uiState.location.trim(),
            area = uiState.area.trim(),
            city = uiState.city.trim(),
            jobType = uiState.jobType.trim(),
            deadline = uiState.deadline.trim()
        )
        jobRepository.postJob(job) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> PostJobUiState(
                    successMessage = "Job posted successfully",
                    isSaved = true
                ).also { createNearbyJobFoundationNotification(employerId) }
            }
        }
    }

    private fun createNearbyJobFoundationNotification(employerId: String) {
        notificationRepository.createNotification(
            NotificationModel(
                receiverId = employerId,
                senderId = employerId,
                title = "Nearby Job Notification Ready",
                message = "Your job is posted. Matching-worker notifications can be sent later from a trusted backend.",
                type = NotificationType.NEARBY_JOB.name,
                relatedType = "JOB"
            )
        ) { }
    }
}
