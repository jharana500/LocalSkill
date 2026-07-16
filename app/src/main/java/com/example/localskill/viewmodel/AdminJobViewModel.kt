package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobModerationStatus
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class AdminJobFilterTab {
    ALL, ACTIVE, DRAFT, CLOSED, FLAGGED, REMOVED;

    fun matches(job: JobModel): Boolean = when (this) {
        ALL -> true
        ACTIVE -> job.status == JobStatus.ACTIVE.name && job.isDiscoverable
        DRAFT -> job.status == JobStatus.DRAFT.name
        CLOSED -> job.status == JobStatus.CLOSED.name
        FLAGGED -> job.normalizedModerationStatus == JobModerationStatus.FLAGGED.name
        REMOVED -> job.normalizedModerationStatus == JobModerationStatus.REMOVED.name
    }
}

data class AdminJobsUiState(
    val isLoading: Boolean = true,
    val jobs: List<JobModel> = emptyList(),
    val filterTab: AdminJobFilterTab = AdminJobFilterTab.ALL,
    val errorMessage: String? = null
) {
    val filtered: List<JobModel> get() = jobs.filter { filterTab.matches(it) }
}

sealed class AdminJobEvent {
    data class ShowMessage(val message: String) : AdminJobEvent()
}

class AdminJobViewModel(
    private val authRepo: AuthRepo,
    private val adminRepo: AdminRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminJobsUiState())
    val uiState: StateFlow<AdminJobsUiState> = _uiState.asStateFlow()

    private val _events = Channel<AdminJobEvent>(Channel.BUFFERED)
    val events: Flow<AdminJobEvent> = _events.receiveAsFlow()

    fun loadJobs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = adminRepo.getAllJobsForModeration()) {
                is ResultState.Success -> _uiState.value = _uiState.value.copy(isLoading = false, jobs = result.data)
                is ResultState.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun setFilterTab(tab: AdminJobFilterTab) {
        _uiState.value = _uiState.value.copy(filterTab = tab)
    }

    fun removeJob(jobId: String, reason: String) {
        val adminId = authRepo.currentUserId() ?: return
        if (reason.isBlank()) return
        viewModelScope.launch {
            when (val result = adminRepo.removeJob(adminId, jobId, reason)) {
                is ResultState.Success -> {
                    _events.send(AdminJobEvent.ShowMessage("Job removed from discovery."))
                    loadJobs()
                }

                is ResultState.Error -> _events.send(AdminJobEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun restoreJob(jobId: String) {
        val adminId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = adminRepo.restoreJob(adminId, jobId)) {
                is ResultState.Success -> {
                    _events.send(AdminJobEvent.ShowMessage("Job restored."))
                    loadJobs()
                }

                is ResultState.Error -> _events.send(AdminJobEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }
}
