package com.example.localskill.fakes

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.utils.ResultState

class FakeApplicationRepo : ApplicationRepo {

    val applications = mutableMapOf<String, ApplicationModel>()
    var submitResult: ResultState<ApplicationModel>? = null
    var withdrawResult: ResultState<Unit>? = null

    override suspend fun hasApplied(userId: String, jobId: String): ResultState<Boolean> =
        ResultState.Success(applications.values.any { it.applicantId == userId && it.jobId == jobId })

    override suspend fun submitApplication(
        job: JobModel,
        applicantId: String,
        resumeUrl: String,
        coverLetter: String
    ): ResultState<ApplicationModel> {
        submitResult?.let { return it }
        val application = ApplicationModel(
            id = "app-${applications.size + 1}",
            jobId = job.id,
            applicantId = applicantId,
            companyId = job.companyId,
            jobTitle = job.title,
            companyName = job.companyName,
            resumeUrl = resumeUrl,
            coverLetter = coverLetter,
            status = ApplicationStatus.APPLIED.name,
            appliedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        applications[application.id] = application
        return ResultState.Success(application)
    }

    override suspend fun getUserApplications(userId: String): ResultState<List<ApplicationModel>> =
        ResultState.Success(applications.values.filter { it.applicantId == userId })

    override suspend fun getApplicationById(applicationId: String): ResultState<ApplicationModel> =
        applications[applicationId]?.let { ResultState.Success(it) } ?: ResultState.Error("Application not found.")

    override suspend fun withdrawApplication(userId: String, applicationId: String): ResultState<Unit> {
        withdrawResult?.let { return it }
        val application = applications[applicationId] ?: return ResultState.Error("Application not found.")
        if (application.applicantId != userId) return ResultState.Error("You can only withdraw your own applications.")
        if (application.status !in ApplicationModel.WITHDRAWABLE_STATUSES) {
            return ResultState.Error("This application can no longer be withdrawn.")
        }
        applications[applicationId] = application.copy(status = ApplicationStatus.WITHDRAWN.name)
        return ResultState.Success(Unit)
    }
}
