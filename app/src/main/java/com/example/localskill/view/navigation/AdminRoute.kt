package com.example.localskill.view.navigation

sealed class AdminRoute(val route: String) {
    data object Dashboard : AdminRoute("admin/dashboard")
    data object Companies : AdminRoute("admin/companies")
    data object Jobs : AdminRoute("admin/jobs")
    data object Reports : AdminRoute("admin/reports")
    data object More : AdminRoute("admin/more")

    data object Users : AdminRoute("admin/users")
    data object Categories : AdminRoute("admin/categories")
    data object Activity : AdminRoute("admin/activity")
    data object Settings : AdminRoute("admin/settings")

    data object CompanyDetails : AdminRoute("admin/companies/{companyId}") {
        fun createRoute(companyId: String) = "admin/companies/$companyId"
    }

    data object ReportDetails : AdminRoute("admin/reports/{reportId}") {
        fun createRoute(reportId: String) = "admin/reports/$reportId"
    }

    companion object {
        const val COMPANY_ID_ARG = "companyId"
        const val REPORT_ID_ARG = "reportId"

        /** Bottom-navigation destinations — every other route hides the bottom bar. */
        val MAIN_DESTINATIONS = setOf(
            Dashboard.route,
            Companies.route,
            Jobs.route,
            Reports.route,
            More.route
        )
    }
}
