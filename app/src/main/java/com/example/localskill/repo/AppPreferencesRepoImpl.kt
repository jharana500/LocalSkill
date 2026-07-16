package com.example.localskill.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "localskill_preferences")

class AppPreferencesRepoImpl(private val context: Context) : AppPreferencesRepo {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val APP_THEME = stringPreferencesKey("app_theme")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] ?: false }

    override val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.APP_THEME]
                ?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.SYSTEM
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences -> preferences[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences -> preferences[Keys.APP_THEME] = theme.name }
    }

    override suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.first()[Keys.DEVICE_ID]
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        context.dataStore.edit { preferences -> preferences[Keys.DEVICE_ID] = created }
        return created
    }
}
