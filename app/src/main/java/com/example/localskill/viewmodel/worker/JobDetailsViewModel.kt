package com.example.localskill.viewmodel.worker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.JobStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
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
import java.util.UUID

data class JobDetailsUiState(
    val job: JobModel? = null,
    val applicationMessage: String = "",
    val hasApplied: Boolean = false,
    val isLoading: Boolean = false,
    val isApplying: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class JobDetailsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val jobRepository: JobRepository = JobRepositoryImpl(),
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(JobDetailsUiState())
        private set

    fun load(jobId: String) {
        jobRepository.getJobById(jobId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> {
                    checkExistingApplication(result.data.id)
                    uiState.copy(isLoading = false, job = result.data)
                }
            }
        }
    }

    fun onApplicationMessageChange(value: String) {
        uiState = uiState.copy(applicationMessage = value, errorMessage = null, successMessage = null)
    }

    fun applyToJob() {
        val job = uiState.job
        val workerId = authRepository.currentUserId()
        when {
            workerId == null -> {
                uiState = uiState.copy(errorMessage = "Session expired. Please login again")
                return
            }
            job == null -> {
                uiState = uiState.copy(errorMessage = "Job details are not available")
                return
            }
            job.status != JobStatus.OPEN.name -> {
                uiState = uiState.copy(errorMessage = "This job is not open for applications")
                return
            }
            uiState.hasApplied -> {
                uiState = uiState.copy(errorMessage = "You have already applied to this job.")
                return
            }
        }

        applicationRepository.hasWorkerApplied(job.id, workerId) { checkResult ->
            when (checkResult) {
                Resource.Loading -> uiState = uiState.copy(isApplying = true, errorMessage = null, successMessage = null)
                is Resource.Error -> uiState = uiState.copy(isApplying = false, errorMessage = checkResult.message)
                is Resource.Success -> {
                    if (checkResult.data) {
                        uiState = uiState.copy(
                            isApplying = false,
                            hasApplied = true,
                            errorMessage = "You have already applied to this job."
                        )
                    } else {
                        loadWorkerAndSubmit(job, workerId)
                    }
                }
            }
        }
    }

    private fun checkExistingApplication(jobId: String) {
        val workerId = authRepository.currentUserId() ?: return
        applicationRepository.hasWorkerApplied(jobId, workerId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState
                is Resource.Error -> uiState.copy(errorMessage = result.message)
                is Resource.Success -> uiState.copy(hasApplied = result.data)
            }
        }
    }

    private fun loadWorkerAndSubmit(job: JobModel, workerId: String) {
        userRepository.getUser(workerId) { result ->
            when (result) {
                Resource.Loading -> uiState = uiState.copy(isApplying = true, errorMessage = null, successMessage = null)
                is Resource.Error -> submitApplication(job, workerId, "", "", "")
                is Resource.Success -> submitApplication(
                    job = job,
                    workerId = workerId,
                    workerName = result.data.fullName,
                    workerPhone = result.data.phone,
                    workerLocation = result.data.location
                )
            }
        }
    }

    private fun submitApplication(
        job: JobModel,
        workerId: String,
        workerName: String,
        workerPhone: String,
        workerLocation: String
    ) {
        val application = ApplicationModel(
            id = UUID.randomUUID().toString(),
            jobId = job.id,
            workerId = workerId,
            employerId = job.employerId,
            message = uiState.applicationMessage.trim(),
            jobTitle = job.title,
            jobLocation = job.location,
            jobBudget = job.budget,
            workerName = workerName,
            workerPhone = workerPhone,
            workerLocation = workerLocation
        )
        applicationRepository.applyToJob(application) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isApplying = true, errorMessage = null, successMessage = null)
                is Resource.Error -> uiState.copy(isApplying = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(
                    isApplying = false,
                    hasApplied = true,
                    applicationMessage = "",
                    successMessage = "Application submitted successfully."
                ).also { createNewApplicationNotification(application) }
            }
        }
    }

    private fun createNewApplicationNotification(application: ApplicationModel) {
        notificationRepository.createNotification(
            NotificationModel(
                receiverId = application.employerId,
                senderId = application.workerId,
                senderName = application.workerName,
                title = "New Job Application",
                message = "A worker has applied to your job.",
                type = NotificationType.NEW_APPLICATION.name,
                relatedId = application.id,
                relatedType = "APPLICATION"
            )
        ) { }
    }
}
