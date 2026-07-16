package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminSettingsViewModel(
    private val appPreferencesRepo: AppPreferencesRepo
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = appPreferencesRepo.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppTheme.SYSTEM
    )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch { appPreferencesRepo.setAppTheme(theme) }
    }
}
