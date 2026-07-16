package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompanySettingsUiState(
    val isLoading: Boolean = true,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val companyName: String = "",
    val email: String = "",
    val verificationStatus: String = ""
)

class CompanySettingsViewModel(
    private val appPreferencesRepo: AppPreferencesRepo,
    private val companyRepo: CompanyRepo,
    private val authRepo: AuthRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanySettingsUiState())
    val uiState: StateFlow<CompanySettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepo.appTheme.collect { theme ->
                _uiState.value = _uiState.value.copy(appTheme = theme, isLoading = false)
            }
        }
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            val result = companyRepo.getCompany(companyId)
            if (result is ResultState.Success) {
                _uiState.value = _uiState.value.copy(
                    companyName = result.data.companyName,
                    email = result.data.email,
                    verificationStatus = result.data.verificationStatus
                )
            }
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { appPreferencesRepo.setAppTheme(theme) }
    }
}
