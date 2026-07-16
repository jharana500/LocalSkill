package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.localskill.AppContainer

@Suppress("UNCHECKED_CAST")
class LocalSkillViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(appContainer.authRepo) as T

        modelClass.isAssignableFrom(AppSessionViewModel::class.java) ->
            AppSessionViewModel(appContainer.authRepo, appContainer.appPreferencesRepo) as T

        modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
            OnboardingViewModel(appContainer.appPreferencesRepo) as T

        modelClass.isAssignableFrom(UserViewModel::class.java) ->
            UserViewModel(appContainer.userRepo) as T

        modelClass.isAssignableFrom(JobSeekerHomeViewModel::class.java) ->
            JobSeekerHomeViewModel(
                appContainer.authRepo,
                appContainer.userRepo,
                appContainer.jobRepo,
                appContainer.savedJobRepo,
                appContainer.jobSeekerProfileRepo
            ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
