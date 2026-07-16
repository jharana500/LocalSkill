package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompanyDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val company: CompanyModel = CompanyModel(),
    val stats: CompanyDashboardStatsModel = CompanyDashboardStatsModel(),
    val recentJobs: List<JobModel> = emptyList(),
    val recentApplications: List<ApplicationModel> = emptyList(),
    val errorMessage: String? = null
)

class CompanyDashboardViewModel(
    private val authRepo: AuthRepo,
    private val companyRepo: CompanyRepo,
    private val companyJobRepo: CompanyJobRepo,
    private val applicantRepo: ApplicantRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyDashboardUiState())
    val uiState: StateFlow<CompanyDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(isRefresh: Boolean = false) {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )

            val companyResult = companyRepo.getCompany(companyId)
            val jobsResult = companyJobRepo.getCompanyJobs(companyId)
            val applicantStatsResult = applicantRepo.getApplicantStats(companyId)
            val applicationsResult = applicantRepo.getCompanyApplications(companyId)

            if (companyResult !is ResultState.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = (companyResult as? ResultState.Error)?.message ?: "Unable to load your dashboard."
                )
                return@launch
            }

            val jobs = (jobsResult as? ResultState.Success)?.data.orEmpty()
            val applicantStats = (applicantStatsResult as? ResultState.Success)?.data ?: CompanyDashboardStatsModel()
            val applications = (applicationsResult as? ResultState.Success)?.data.orEmpty()

            val stats = applicantStats.copy(
                activeJobs = jobs.count { it.status == JobStatus.ACTIVE.name },
                draftJobs = jobs.count { it.status == JobStatus.DRAFT.name },
                closedJobs = jobs.count { it.status == JobStatus.CLOSED.name }
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                company = companyResult.data,
                stats = stats,
                recentJobs = jobs.take(5),
                recentApplications = applications.take(5)
            )
        }
    }
}
