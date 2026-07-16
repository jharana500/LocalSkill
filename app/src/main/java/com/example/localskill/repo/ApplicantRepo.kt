package com.example.localskill.repo

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.utils.ResultState

/**
 * Company-side review of applications submitted to its own jobs. Job
 * Seeker submission and withdrawal remain [ApplicationRepo]'s job; every
 * method here validates that the acting company actually owns the
 * application (or the job) before reading or writing it.
 */
interface ApplicantRepo {

    suspend fun getCompanyApplications(companyId: String): ResultState<List<ApplicationModel>>

    suspend fun getJobApplicants(companyId: String, jobId: String): ResultState<List<ApplicationModel>>

    suspend fun getApplicationDetails(companyId: String, applicationId: String): ResultState<ApplicationModel>

    suspend fun getApplicantProfile(userId: String): ResultState<JobSeekerProfileModel>

    /** Validated against [ApplicationModel.isValidCompanyTransition]; rejects illegal jumps. */
    suspend fun updateApplicationStatus(
        companyId: String,
        applicationId: String,
        newStatus: String,
        companyMessage: String = ""
    ): ResultState<Unit>

    suspend fun addCompanyMessage(companyId: String, applicationId: String, message: String): ResultState<Unit>

    /** Requires a future interviewDate; moves the application to INTERVIEW. */
    suspend fun scheduleInterview(
        companyId: String,
        applicationId: String,
        interviewDate: Long,
        interviewLocation: String,
        companyMessage: String = ""
    ): ResultState<Unit>

    /** Only the application-derived counters are populated; job counters default to 0. */
    suspend fun getApplicantStats(companyId: String): ResultState<CompanyDashboardStatsModel>
}
