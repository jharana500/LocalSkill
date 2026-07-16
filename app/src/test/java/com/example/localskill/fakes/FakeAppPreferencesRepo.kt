package com.example.localskill.fakes

import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferencesRepo(
    initialOnboardingCompleted: Boolean = true,
    initialAppTheme: AppTheme = AppTheme.SYSTEM
) : AppPreferencesRepo {

    private val onboardingCompletedFlow = MutableStateFlow(initialOnboardingCompleted)
    private val appThemeFlow = MutableStateFlow(initialAppTheme)

    override val isOnboardingCompleted: Flow<Boolean> = onboardingCompletedFlow
    override val appTheme: Flow<AppTheme> = appThemeFlow

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompletedFlow.value = completed
    }

    override suspend fun setAppTheme(theme: AppTheme) {
        appThemeFlow.value = theme
    }
}
