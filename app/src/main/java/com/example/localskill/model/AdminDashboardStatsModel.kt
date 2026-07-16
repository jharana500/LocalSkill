package com.example.localskill.model

data class AdminDashboardStatsModel(
    val totalUsers: Int = 0,
    val activeJobSeekers: Int = 0,
    val totalCompanies: Int = 0,
    val verifiedCompanies: Int = 0,
    val pendingCompanies: Int = 0,
    val rejectedCompanies: Int = 0,
    val activeJobs: Int = 0,
    val draftJobs: Int = 0,
    val closedJobs: Int = 0,
    val totalApplications: Int = 0,
    val openReports: Int = 0,
    val suspendedAccounts: Int = 0
)
