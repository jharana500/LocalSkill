package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class ApplicationFilterTab {
    ALL, ACTIVE, SHORTLISTED, INTERVIEW, HIRED, REJECTED, WITHDRAWN;

    fun matches(status: String): Boolean = when (this) {
        ALL -> true
        ACTIVE -> status == ApplicationStatus.APPLIED.name || status == ApplicationStatus.UNDER_REVIEW.name
        SHORTLISTED -> status == ApplicationStatus.SHORTLISTED.name
        INTERVIEW -> status == ApplicationStatus.INTERVIEW.name
        HIRED -> status == ApplicationStatus.HIRED.name
        REJECTED -> status == ApplicationStatus.REJECTED.name
        WITHDRAWN -> status == ApplicationStatus.WITHDRAWN.name
    }
}

data class ApplyJobUiState(
    val isLoading: Boolean = true,
    val job: JobModel? = null,
    val resume: ResumeModel? = null,
    val coverLetter: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val jobUnavailable: Boolean = false,
    val errorMessage: String? = null
)

data class ApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<ApplicationModel> = emptyList(),
    val selectedTab: ApplicationFilterTab = ApplicationFilterTab.ALL,
    val errorMessage: String? = null
)

data class ApplicationDetailsUiState(
    val isLoading: Boolean = true,
    val application: ApplicationModel? = null,
    val isWithdrawing: Boolean = false,
    val notFound: Boolean = false,
    val errorMessage: String? = null
)

sealed class ApplicationEvent {
    data class ShowMessage(val message: String) : ApplicationEvent()
}

class ApplicationViewModel(
    private val authRepo: AuthRepo,
    private val jobRepo: JobRepo,
    private val applicationRepo: ApplicationRepo,
    private val jobSeekerProfileRepo: JobSeekerProfileRepo
) : ViewModel() {

    private val _applyUiState = MutableStateFlow(ApplyJobUiState())
    val applyUiState: StateFlow<ApplyJobUiState> = _applyUiState.asStateFlow()

    private val _applicationsUiState = MutableStateFlow(ApplicationsUiState())
    val applicationsUiState: StateFlow<ApplicationsUiState> = _applicationsUiState.asStateFlow()

    private val _applicationDetailsUiState = MutableStateFlow(ApplicationDetailsUiState())
    val applicationDetailsUiState: StateFlow<ApplicationDetailsUiState> = _applicationDetailsUiState.asStateFlow()

    private val _events = Channel<ApplicationEvent>(Channel.BUFFERED)
    val events: Flow<ApplicationEvent> = _events.receiveAsFlow()

    // ---------------------------------------------------------------------
    // Apply
    // ---------------------------------------------------------------------

    fun loadApplyScreen(jobId: String) {
        val userId = authRepo.currentUserId()
        viewModelScope.launch {
            _applyUiState.value = ApplyJobUiState(isLoading = true)

            val job = (jobRepo.getJobById(jobId) as? ResultState.Success)?.data
            if (job == null || !job.isOpenForApplications) {
                _applyUiState.value = ApplyJobUiState(isLoading = false, job = job, jobUnavailable = true)
                return@launch
            }

            if (userId != null) {
                val alreadyApplied = applicationRepo.hasApplied(userId, jobId)
                if (alreadyApplied is ResultState.Success && alreadyApplied.data) {
                    _applyUiState.value = ApplyJobUiState(
                        isLoading = false,
                        job = job,
                        jobUnavailable = true,
                        errorMessage = "You have already applied to this job."
                    )
                    return@launch
                }
            }

            val resume = userId?.let { (jobSeekerProfileRepo.getProfile(it) as? ResultState.Success)?.data?.resume }
            _applyUiState.value = ApplyJobUiState(isLoading = false, job = job, resume = resume)
        }
    }

    fun onCoverLetterChanged(value: String) {
        _applyUiState.value = _applyUiState.value.copy(coverLetter = value, errorMessage = null)
    }

    fun submitApplication() {
        val state = _applyUiState.value
        val job = state.job ?: return
        val userId = authRepo.currentUserId() ?: return
        if (state.isSubmitting || state.isSuccess) return

        val coverLetterError = JobValidationUtils.validateCoverLetter(state.coverLetter)
        if (coverLetterError != null) {
            _applyUiState.value = state.copy(errorMessage = coverLetterError)
            return
        }
        val resumeUrl = state.resume?.downloadUrl
        if (resumeUrl.isNullOrBlank()) {
            _applyUiState.value = state.copy(errorMessage = "Add a resume to your profile before applying.")
            return
        }

        viewModelScope.launch {
            _applyUiState.value = _applyUiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = applicationRepo.submitApplication(job, userId, resumeUrl, state.coverLetter.trim())) {
                is ResultState.Success -> _applyUiState.value =
                    _applyUiState.value.copy(isSubmitting = false, isSuccess = true)

                is ResultState.Error -> _applyUiState.value =
                    _applyUiState.value.copy(isSubmitting = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    // ---------------------------------------------------------------------
    // Applications list
    // ---------------------------------------------------------------------

    fun loadApplications() {
        val userId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _applicationsUiState.value = _applicationsUiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = applicationRepo.getUserApplications(userId)) {
                is ResultState.Success -> _applicationsUiState.value =
                    _applicationsUiState.value.copy(isLoading = false, applications = result.data)

                is ResultState.Error -> _applicationsUiState.value =
                    _applicationsUiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun selectApplicationsTab(tab: ApplicationFilterTab) {
        _applicationsUiState.value = _applicationsUiState.value.copy(selectedTab = tab)
    }

    // ---------------------------------------------------------------------
    // Application details
    // ---------------------------------------------------------------------

    fun loadApplicationDetails(applicationId: String) {
        viewModelScope.launch {
            _applicationDetailsUiState.value = ApplicationDetailsUiState(isLoading = true)
            when (val result = applicationRepo.getApplicationById(applicationId)) {
                is ResultState.Success -> _applicationDetailsUiState.value =
                    ApplicationDetailsUiState(isLoading = false, application = result.data)

                is ResultState.Error -> _applicationDetailsUiState.value =
                    ApplicationDetailsUiState(isLoading = false, notFound = true, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun withdrawApplication() {
        val userId = authRepo.currentUserId() ?: return
        val applicationId = _applicationDetailsUiState.value.application?.id ?: return
        if (_applicationDetailsUiState.value.isWithdrawing) return

        viewModelScope.launch {
            _applicationDetailsUiState.value = _applicationDetailsUiState.value.copy(isWithdrawing = true)
            when (val result = applicationRepo.withdrawApplication(userId, applicationId)) {
                is ResultState.Success -> {
                    loadApplicationDetails(applicationId)
                    _events.send(ApplicationEvent.ShowMessage("Application withdrawn."))
                }

                is ResultState.Error -> {
                    _applicationDetailsUiState.value = _applicationDetailsUiState.value.copy(isWithdrawing = false)
                    _events.send(ApplicationEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }
}
