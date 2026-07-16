package com.example.localskill.view.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Onboarding : AppRoute("onboarding")
    data object RoleSelection : AppRoute("role_selection")
    data object Login : AppRoute("login")
    data object JobSeekerRegistration : AppRoute("job_seeker_registration")
    data object CompanyRegistration : AppRoute("company_registration")
    data object ForgotPassword : AppRoute("forgot_password")
    data object EmailVerification : AppRoute("email_verification")
    data object AccountStatus : AppRoute("account_status")
    data object JobSeekerEntry : AppRoute("job_seeker_entry")
    data object CompanyEntry : AppRoute("company_entry")
    data object AdminEntry : AppRoute("admin_entry")
}
