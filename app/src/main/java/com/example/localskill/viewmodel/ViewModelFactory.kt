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

        modelClass.isAssignableFrom(JobViewModel::class.java) ->
            JobViewModel(
                appContainer.authRepo,
                appContainer.jobRepo,
                appContainer.savedJobRepo,
                appContainer.applicationRepo,
                appContainer.reportRepo
            ) as T

        modelClass.isAssignableFrom(ApplicationViewModel::class.java) ->
            ApplicationViewModel(
                appContainer.authRepo,
                appContainer.jobRepo,
                appContainer.applicationRepo,
                appContainer.jobSeekerProfileRepo
            ) as T

        modelClass.isAssignableFrom(SavedJobViewModel::class.java) ->
            SavedJobViewModel(appContainer.authRepo, appContainer.savedJobRepo) as T

        modelClass.isAssignableFrom(JobSeekerProfileViewModel::class.java) ->
            JobSeekerProfileViewModel(
                appContainer.authRepo,
                appContainer.userRepo,
                appContainer.jobSeekerProfileRepo,
                appContainer.applicationRepo,
                appContainer.savedJobRepo,
                appContainer.fileRepo
            ) as T

        modelClass.isAssignableFrom(JobSeekerSettingsViewModel::class.java) ->
            JobSeekerSettingsViewModel(
                appContainer.appPreferencesRepo,
                appContainer.jobSeekerProfileRepo,
                appContainer.authRepo
            ) as T

        modelClass.isAssignableFrom(CompanyDashboardViewModel::class.java) ->
            CompanyDashboardViewModel(
                appContainer.authRepo,
                appContainer.companyRepo,
                appContainer.companyJobRepo,
                appContainer.applicantRepo
            ) as T

        modelClass.isAssignableFrom(CompanyProfileViewModel::class.java) ->
            CompanyProfileViewModel(
                appContainer.authRepo,
                appContainer.companyRepo,
                appContainer.companyJobRepo,
                appContainer.fileRepo
            ) as T

        modelClass.isAssignableFrom(CompanyVerificationViewModel::class.java) ->
            CompanyVerificationViewModel(
                appContainer.authRepo,
                appContainer.companyRepo,
                appContainer.fileRepo
            ) as T

        modelClass.isAssignableFrom(CompanyJobViewModel::class.java) ->
            CompanyJobViewModel(
                appContainer.authRepo,
                appContainer.companyRepo,
                appContainer.companyJobRepo,
                appContainer.jobRepo
            ) as T

        modelClass.isAssignableFrom(ApplicantViewModel::class.java) ->
            ApplicantViewModel(
                appContainer.authRepo,
                appContainer.applicantRepo,
                appContainer.companyJobRepo,
                appContainer.userRepo
            ) as T

        modelClass.isAssignableFrom(CompanySettingsViewModel::class.java) ->
            CompanySettingsViewModel(
                appContainer.appPreferencesRepo,
                appContainer.companyRepo,
                appContainer.authRepo
            ) as T

        modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) ->
            AdminDashboardViewModel(appContainer.adminRepo, appContainer.reportRepo) as T

        modelClass.isAssignableFrom(AdminCompanyViewModel::class.java) ->
            AdminCompanyViewModel(appContainer.authRepo, appContainer.adminRepo, appContainer.companyRepo) as T

        modelClass.isAssignableFrom(AdminUserViewModel::class.java) ->
            AdminUserViewModel(appContainer.authRepo, appContainer.adminRepo) as T

        modelClass.isAssignableFrom(AdminJobViewModel::class.java) ->
            AdminJobViewModel(appContainer.authRepo, appContainer.adminRepo) as T

        modelClass.isAssignableFrom(AdminReportViewModel::class.java) ->
            AdminReportViewModel(appContainer.authRepo, appContainer.reportRepo) as T

        modelClass.isAssignableFrom(AdminCategoryViewModel::class.java) ->
            AdminCategoryViewModel(appContainer.authRepo, appContainer.adminRepo) as T

        modelClass.isAssignableFrom(AdminSettingsViewModel::class.java) ->
            AdminSettingsViewModel(appContainer.appPreferencesRepo) as T

        modelClass.isAssignableFrom(NotificationViewModel::class.java) ->
            NotificationViewModel(appContainer.authRepo, appContainer.notificationRepo) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
