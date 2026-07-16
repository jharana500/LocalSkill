package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SavedJobsUiState(
    val isLoading: Boolean = false,
    val savedJobs: List<JobModel> = emptyList(),
    val errorMessage: String? = null
)

sealed class SavedJobEvent {
    data class ShowMessage(val message: String) : SavedJobEvent()
}

class SavedJobViewModel(
    private val authRepo: AuthRepo,
    private val savedJobRepo: SavedJobRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedJobsUiState())
    val uiState: StateFlow<SavedJobsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SavedJobEvent>(Channel.BUFFERED)
    val events: Flow<SavedJobEvent> = _events.receiveAsFlow()

    fun loadSavedJobs() {
        val userId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = savedJobRepo.getSavedJobs(userId)) {
                is ResultState.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, savedJobs = result.data)

                is ResultState.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun unsaveJob(jobId: String) {
        val userId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = savedJobRepo.unsaveJob(userId, jobId)) {
                is ResultState.Success -> _uiState.value = _uiState.value.copy(
                    savedJobs = _uiState.value.savedJobs.filterNot { it.id == jobId }
                )

                is ResultState.Error -> _events.send(SavedJobEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }
}
