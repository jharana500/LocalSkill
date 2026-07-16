package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.ReportRepo
import com.example.localskill.model.ReportStatus
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val stats: AdminDashboardStatsModel = AdminDashboardStatsModel(),
    val pendingCompanies: List<CompanyModel> = emptyList(),
    val recentUsers: List<UserModel> = emptyList(),
    val recentJobs: List<JobModel> = emptyList(),
    val recentReports: List<JobReportModel> = emptyList(),
    val recentActivity: List<AdminActivityModel> = emptyList(),
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val adminRepo: AdminRepo,
    private val reportRepo: ReportRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )

            val statsResult = adminRepo.getDashboardStats()
            if (statsResult !is ResultState.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = (statsResult as? ResultState.Error)?.message ?: "Unable to load the dashboard."
                )
                return@launch
            }

            val pendingCompanies = (adminRepo.getPendingCompanies() as? ResultState.Success)?.data.orEmpty()
            val users = (adminRepo.getAllUsers() as? ResultState.Success)?.data.orEmpty()
            val jobs = (adminRepo.getAllJobsForModeration() as? ResultState.Success)?.data.orEmpty()
            val reports = (reportRepo.getReports(ReportStatus.PENDING.name) as? ResultState.Success)?.data.orEmpty()
            val activity = (adminRepo.getActivityLog(10) as? ResultState.Success)?.data.orEmpty()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                stats = statsResult.data,
                pendingCompanies = pendingCompanies.take(5),
                recentUsers = users.sortedByDescending { it.createdAt }.take(5),
                recentJobs = jobs.sortedByDescending { it.createdAt }.take(5),
                recentReports = reports.take(5),
                recentActivity = activity
            )
        }
    }
}
