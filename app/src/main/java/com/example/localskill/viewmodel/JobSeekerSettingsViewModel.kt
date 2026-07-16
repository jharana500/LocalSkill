package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.ProfileVisibility
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobSeekerSettingsUiState(
    val isLoading: Boolean = true,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val profileVisibility: String = ProfileVisibility.PUBLIC.name
)

class JobSeekerSettingsViewModel(
    private val appPreferencesRepo: AppPreferencesRepo,
    private val jobSeekerProfileRepo: JobSeekerProfileRepo,
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerSettingsUiState())
    val uiState: StateFlow<JobSeekerSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepo.appTheme.collect { theme ->
                _uiState.value = _uiState.value.copy(appTheme = theme, isLoading = false)
            }
        }
        loadProfileVisibility()
    }

    private fun loadProfileVisibility() {
        val userId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            val result = jobSeekerProfileRepo.getProfile(userId)
            if (result is ResultState.Success) {
                _uiState.value = _uiState.value.copy(profileVisibility = result.data.profileVisibility)
            }
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { appPreferencesRepo.setAppTheme(theme) }
    }

    fun setProfileVisibility(visibility: String) {
        val userId = authRepo.currentUserId() ?: return
        _uiState.value = _uiState.value.copy(profileVisibility = visibility)
        viewModelScope.launch { jobSeekerProfileRepo.updateProfileVisibility(userId, visibility) }
    }
}
