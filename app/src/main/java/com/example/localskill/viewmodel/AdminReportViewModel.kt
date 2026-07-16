package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.ReportRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class AdminReportsUiState(
    val isLoading: Boolean = true,
    val reports: List<JobReportModel> = emptyList(),
    val filterStatus: String? = ReportStatus.PENDING.name,
    val errorMessage: String? = null
)

data class AdminReportDetailsUiState(
    val isLoading: Boolean = true,
    val report: JobReportModel? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

sealed class AdminReportEvent {
    data class ShowMessage(val message: String) : AdminReportEvent()
}

class AdminReportViewModel(
    private val authRepo: AuthRepo,
    private val reportRepo: ReportRepo
) : ViewModel() {

    private val _reportsUiState = MutableStateFlow(AdminReportsUiState())
    val reportsUiState: StateFlow<AdminReportsUiState> = _reportsUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(AdminReportDetailsUiState())
    val detailsUiState: StateFlow<AdminReportDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<AdminReportEvent>(Channel.BUFFERED)
    val events: Flow<AdminReportEvent> = _events.receiveAsFlow()

    fun loadReports() {
        viewModelScope.launch {
            _reportsUiState.value = _reportsUiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = reportRepo.getReports(_reportsUiState.value.filterStatus)) {
                is ResultState.Success -> _reportsUiState.value = _reportsUiState.value.copy(isLoading = false, reports = result.data)
                is ResultState.Error -> _reportsUiState.value = _reportsUiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun setFilterStatus(status: String?) {
        _reportsUiState.value = _reportsUiState.value.copy(filterStatus = status)
        loadReports()
    }

    fun loadReportDetails(reportId: String) {
        viewModelScope.launch {
            _detailsUiState.value = AdminReportDetailsUiState(isLoading = true)
            when (val result = reportRepo.getReportById(reportId)) {
                is ResultState.Success -> _detailsUiState.value = AdminReportDetailsUiState(isLoading = false, report = result.data)
                is ResultState.Error -> _detailsUiState.value = AdminReportDetailsUiState(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun markUnderReview() {
        val adminId = authRepo.currentUserId() ?: return
        val reportId = _detailsUiState.value.report?.id ?: return
        viewModelScope.launch {
            when (val result = reportRepo.markUnderReview(adminId, reportId)) {
                is ResultState.Success -> loadReportDetails(reportId)
                is ResultState.Error -> _events.send(AdminReportEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun resolveReport(note: String) {
        val adminId = authRepo.currentUserId() ?: return
        val reportId = _detailsUiState.value.report?.id ?: return
        if (_detailsUiState.value.isProcessing) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isProcessing = true)
            when (val result = reportRepo.resolveReport(adminId, reportId, note)) {
                is ResultState.Success -> {
                    _events.send(AdminReportEvent.ShowMessage("Report resolved."))
                    loadReportDetails(reportId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isProcessing = false)
                    _events.send(AdminReportEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun rejectReport(note: String) {
        val adminId = authRepo.currentUserId() ?: return
        val reportId = _detailsUiState.value.report?.id ?: return
        if (_detailsUiState.value.isProcessing) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isProcessing = true)
            when (val result = reportRepo.rejectReport(adminId, reportId, note)) {
                is ResultState.Success -> {
                    _events.send(AdminReportEvent.ShowMessage("Report rejected."))
                    loadReportDetails(reportId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isProcessing = false)
                    _events.send(AdminReportEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }
}
