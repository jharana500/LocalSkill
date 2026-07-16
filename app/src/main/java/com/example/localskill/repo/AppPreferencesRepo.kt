package com.example.localskill.repo

import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.Flow

/**
 * Local-only app preferences. Never store passwords, tokens, or other
 * sensitive personal information here — Firebase Authentication already
 * manages authentication persistence.
 */
interface AppPreferencesRepo {

    val isOnboardingCompleted: Flow<Boolean>

    val appTheme: Flow<AppTheme>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setAppTheme(theme: AppTheme)

    suspend fun getOrCreateDeviceId(): String
}
