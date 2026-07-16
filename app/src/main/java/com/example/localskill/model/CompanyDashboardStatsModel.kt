package com.example.localskill.model

data class CompanyDashboardStatsModel(
    val activeJobs: Int = 0,
    val draftJobs: Int = 0,
    val closedJobs: Int = 0,
    val totalApplications: Int = 0,
    val awaitingReview: Int = 0,
    val shortlisted: Int = 0,
    val scheduledInterviews: Int = 0,
    val hired: Int = 0
)
