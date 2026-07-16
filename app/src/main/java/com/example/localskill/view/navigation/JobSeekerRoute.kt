package com.example.localskill.view.navigation

sealed class JobSeekerRoute(val route: String) {
    data object Graph : JobSeekerRoute("job_seeker_graph")
    data object Home : JobSeekerRoute("job_seeker/home")

    /**
     * The query argument is optional (has a default), so both
     * navigate("job_seeker/explore") and
     * navigate("job_seeker/explore?query=...") resolve to this same
     * destination — the registered template below is what
     * NavBackStackEntry.destination.route always returns, regardless of
     * which form was used to navigate here.
     */
    data object Explore : JobSeekerRoute("job_seeker/explore?query={query}") {
        const val QUERY_ARG = "query"
        const val BASE_ROUTE = "job_seeker/explore"

        fun createRoute(query: String = ""): String {
            if (query.isBlank()) return BASE_ROUTE
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            return "$BASE_ROUTE?query=$encoded"
        }
    }

    data object Applications : JobSeekerRoute("job_seeker/applications")
    data object Saved : JobSeekerRoute("job_seeker/saved")
    data object Profile : JobSeekerRoute("job_seeker/profile")

    data object JobDetails : JobSeekerRoute("job_seeker/job/{jobId}") {
        fun createRoute(jobId: String) = "job_seeker/job/$jobId"
    }

    data object ApplyJob : JobSeekerRoute("job_seeker/apply/{jobId}") {
        fun createRoute(jobId: String) = "job_seeker/apply/$jobId"
    }

    data object ApplicationDetails : JobSeekerRoute("job_seeker/application/{applicationId}") {
        fun createRoute(applicationId: String) = "job_seeker/application/$applicationId"
    }

    data object EditProfile : JobSeekerRoute("job_seeker/profile/edit")
    data object Education : JobSeekerRoute("job_seeker/profile/education")
    data object Experience : JobSeekerRoute("job_seeker/profile/experience")
    data object Skills : JobSeekerRoute("job_seeker/profile/skills")
    data object Resume : JobSeekerRoute("job_seeker/profile/resume")
    data object Settings : JobSeekerRoute("job_seeker/settings")

    companion object {
        const val JOB_ID_ARG = "jobId"
        const val APPLICATION_ID_ARG = "applicationId"

        /** Bottom-navigation destinations — every other route hides the bottom bar. */
        val MAIN_DESTINATIONS = setOf(
            Home.route,
            Explore.route,
            Applications.route,
            Saved.route,
            Profile.route
        )
    }
}
