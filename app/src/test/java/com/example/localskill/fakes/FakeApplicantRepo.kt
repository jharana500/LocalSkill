package com.example.localskill.fakes

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.utils.ResultState

/** Mirrors ApplicantRepoImpl's ownership, transition, and interview-date rules for ViewModel tests. */
class FakeApplicantRepo : ApplicantRepo {

    val applications = mutableMapOf<String, ApplicationModel>()
    var applicantProfileResult: ResultState<JobSeekerProfileModel> = ResultState.Success(JobSeekerProfileModel())

    override suspend fun getCompanyApplications(companyId: String): ResultState<List<ApplicationModel>> =
        ResultState.Success(applications.values.filter { it.companyId == companyId }.sortedByDescending { it.appliedAt })

    override suspend fun getJobApplicants(companyId: String, jobId: String): ResultState<List<ApplicationModel>> =
        ResultState.Success(
            applications.values.filter { it.companyId == companyId && it.jobId == jobId }.sortedByDescending { it.appliedAt }
        )

    override suspend fun getApplicationDetails(companyId: String, applicationId: String): ResultState<ApplicationModel> {
        val owned = requireOwned(companyId, applicationId) ?: return ResultState.Error("This application was not found.")
        return owned
    }

    override suspend fun getApplicantProfile(userId: String): ResultState<JobSeekerProfileModel> = applicantProfileResult

    override suspend fun updateApplicationStatus(
        companyId: String,
        applicationId: String,
        newStatus: String,
        companyMessage: String
    ): ResultState<Unit> {
        val owned = requireOwned(companyId, applicationId) ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val application = owned.data
        if (!ApplicationModel.isValidCompanyTransition(application.status, newStatus)) {
            return ResultState.Error("This status change is not allowed from ${application.status}.")
        }
        applications[applicationId] = application.copy(status = newStatus)
        return ResultState.Success(Unit)
    }

    override suspend fun addCompanyMessage(companyId: String, applicationId: String, message: String): ResultState<Unit> {
        val owned = requireOwned(companyId, applicationId) ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        applications[applicationId] = owned.data.copy(companyMessage = message)
        return ResultState.Success(Unit)
    }

    override suspend fun scheduleInterview(
        companyId: String,
        applicationId: String,
        interviewDate: Long,
        interviewLocation: String,
        companyMessage: String
    ): ResultState<Unit> {
        val owned = requireOwned(companyId, applicationId) ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val application = owned.data

        if (interviewDate <= System.currentTimeMillis()) {
            return ResultState.Error("Interview date must be in the future.")
        }
        val isReschedule = application.status == ApplicationStatus.INTERVIEW.name
        if (!isReschedule && !ApplicationModel.isValidCompanyTransition(application.status, ApplicationStatus.INTERVIEW.name)) {
            return ResultState.Error("This application cannot be moved to interview from ${application.status}.")
        }

        applications[applicationId] = application.copy(
            status = ApplicationStatus.INTERVIEW.name,
            interviewDate = interviewDate,
            interviewLocation = interviewLocation
        )
        return ResultState.Success(Unit)
    }

    override suspend fun getApplicantStats(companyId: String): ResultState<CompanyDashboardStatsModel> {
        val data = applications.values.filter { it.companyId == companyId }
        return ResultState.Success(
            CompanyDashboardStatsModel(
                totalApplications = data.size,
                awaitingReview = data.count {
                    it.status == ApplicationStatus.APPLIED.name || it.status == ApplicationStatus.UNDER_REVIEW.name
                },
                shortlisted = data.count { it.status == ApplicationStatus.SHORTLISTED.name },
                scheduledInterviews = data.count { it.status == ApplicationStatus.INTERVIEW.name },
                hired = data.count { it.status == ApplicationStatus.HIRED.name }
            )
        )
    }

    private fun requireOwned(companyId: String, applicationId: String): ResultState<ApplicationModel>? {
        val application = applications[applicationId] ?: return null
        return if (application.companyId != companyId) {
            ResultState.Error("This application was not found.")
        } else {
            ResultState.Success(application)
        }
    }

    private fun ResultState<ApplicationModel>.asUnit(): ResultState<Unit> = when (this) {
        is ResultState.Error -> ResultState.Error(message, throwable)
        else -> ResultState.Error("Unable to update this application. Please try again.")
    }
}
