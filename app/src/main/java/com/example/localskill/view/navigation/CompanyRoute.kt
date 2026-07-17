package com.example.localskill.view.navigation

sealed class CompanyRoute(val route: String) {
    data object Dashboard : CompanyRoute("company/dashboard")
    data object Jobs : CompanyRoute("company/jobs")
    data object Applicants : CompanyRoute("company/applicants")
    data object Profile : CompanyRoute("company/profile")
    data object Settings : CompanyRoute("company/settings")
    data object Notifications : CompanyRoute("company/notifications")

    data object Verification : CompanyRoute("company/verification")
    data object EditProfile : CompanyRoute("company/profile/edit")

    /** Optional jobId argument (default ""): blank means "create new draft". */
    data object JobForm : CompanyRoute("company/jobs/form?jobId={jobId}") {
        const val BASE_ROUTE = "company/jobs/form"

        fun createRoute(jobId: String? = null): String =
            if (jobId.isNullOrBlank()) BASE_ROUTE else "$BASE_ROUTE?jobId=$jobId"
    }

    data object JobDetails : CompanyRoute("company/jobs/{jobId}") {
        fun createRoute(jobId: String) = "company/jobs/$jobId"
    }

    data object JobApplicants : CompanyRoute("company/jobs/{jobId}/applicants") {
        fun createRoute(jobId: String) = "company/jobs/$jobId/applicants"
    }

    data object ApplicantDetails : CompanyRoute("company/applicants/{applicationId}") {
        fun createRoute(applicationId: String) = "company/applicants/$applicationId"
    }

    companion object {
        const val JOB_ID_ARG = "jobId"
        const val APPLICATION_ID_ARG = "applicationId"

        /** Bottom-navigation destinations — every other route hides the bottom bar. */
        val MAIN_DESTINATIONS by lazy {
            setOf(
                Dashboard.route,
                Jobs.route,
                Applicants.route,
                Profile.route,
                Settings.route
            )
        }
    }
}
