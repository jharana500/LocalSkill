package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.ApplicantFilterModel
import com.example.localskill.model.ApplicantStatusFilter
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ApplicantsUiState(
    val isLoading: Boolean = true,
    val applications: List<ApplicationModel> = emptyList(),
    val jobs: List<JobModel> = emptyList(),
    val filter: ApplicantFilterModel = ApplicantFilterModel(),
    val errorMessage: String? = null
) {
    val filtered: List<ApplicationModel>
        get() = applications
            .filter { filter.jobId == null || it.jobId == filter.jobId }
            .filter { filter.statusFilter.matches(it.status) }
}

data class ApplicantDetailsUiState(
    val isLoading: Boolean = true,
    val application: ApplicationModel? = null,
    val applicantProfile: JobSeekerProfileModel? = null,
    val applicantUser: UserModel? = null,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null
)

sealed class ApplicantEvent {
    data class ShowMessage(val message: String) : ApplicantEvent()
}

class ApplicantViewModel(
    private val authRepo: AuthRepo,
    private val applicantRepo: ApplicantRepo,
    private val companyJobRepo: CompanyJobRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _applicantsUiState = MutableStateFlow(ApplicantsUiState())
    val applicantsUiState: StateFlow<ApplicantsUiState> = _applicantsUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(ApplicantDetailsUiState())
    val detailsUiState: StateFlow<ApplicantDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<ApplicantEvent>(Channel.BUFFERED)
    val events: Flow<ApplicantEvent> = _events.receiveAsFlow()

    fun loadApplicants(jobId: String? = null) {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _applicantsUiState.value = _applicantsUiState.value.copy(isLoading = true, errorMessage = null)
            val applicationsResult = if (jobId != null) {
                applicantRepo.getJobApplicants(companyId, jobId)
            } else {
                applicantRepo.getCompanyApplications(companyId)
            }
            val jobsResult = companyJobRepo.getCompanyJobs(companyId)

            when (applicationsResult) {
                is ResultState.Success -> _applicantsUiState.value = _applicantsUiState.value.copy(
                    isLoading = false,
                    applications = applicationsResult.data,
                    jobs = (jobsResult as? ResultState.Success)?.data ?: emptyList(),
                    filter = _applicantsUiState.value.filter.copy(jobId = jobId)
                )

                is ResultState.Error -> _applicantsUiState.value = _applicantsUiState.value.copy(
                    isLoading = false,
                    errorMessage = applicationsResult.message
                )

                else -> Unit
            }
        }
    }

    fun setStatusFilter(filter: ApplicantStatusFilter) {
        _applicantsUiState.value = _applicantsUiState.value.copy(
            filter = _applicantsUiState.value.filter.copy(statusFilter = filter)
        )
    }

    fun setJobFilter(jobId: String?) {
        loadApplicants(jobId)
    }

    fun loadApplicantDetails(applicationId: String) {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _detailsUiState.value = ApplicantDetailsUiState(isLoading = true)
            when (val result = applicantRepo.getApplicationDetails(companyId, applicationId)) {
                is ResultState.Success -> {
                    val application = result.data
                    val profileResult = applicantRepo.getApplicantProfile(application.applicantId)
                    val userResult = userRepo.getUserById(application.applicantId)
                    _detailsUiState.value = ApplicantDetailsUiState(
                        isLoading = false,
                        application = application,
                        applicantProfile = (profileResult as? ResultState.Success)?.data,
                        applicantUser = (userResult as? ResultState.Success)?.data
                    )
                }

                is ResultState.Error -> _detailsUiState.value = ApplicantDetailsUiState(
                    isLoading = false,
                    errorMessage = result.message
                )

                else -> Unit
            }
        }
    }

    fun updateStatus(newStatus: String, companyMessage: String = "") {
        val companyId = authRepo.currentUserId() ?: return
        val applicationId = _detailsUiState.value.application?.id ?: return
        if (_detailsUiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = true)
            when (val result = applicantRepo.updateApplicationStatus(companyId, applicationId, newStatus, companyMessage)) {
                is ResultState.Success -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = false)
                    _events.send(ApplicantEvent.ShowMessage("Application status updated."))
                    loadApplicantDetails(applicationId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = false)
                    _events.send(ApplicantEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun scheduleInterview(interviewDate: Long, interviewLocation: String, companyMessage: String = "") {
        val companyId = authRepo.currentUserId() ?: return
        val applicationId = _detailsUiState.value.application?.id ?: return
        if (_detailsUiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = true)
            when (val result = applicantRepo.scheduleInterview(companyId, applicationId, interviewDate, interviewLocation, companyMessage)) {
                is ResultState.Success -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = false)
                    _events.send(ApplicantEvent.ShowMessage("Interview scheduled."))
                    loadApplicantDetails(applicationId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isUpdatingStatus = false)
                    _events.send(ApplicantEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun rejectApplication(companyMessage: String) = updateStatus(ApplicationStatus.REJECTED.name, companyMessage)
}
