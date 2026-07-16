package com.example.localskill.repo

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.JobModel
import com.example.localskill.utils.ResultState

interface ApplicationRepo {

    suspend fun hasApplied(userId: String, jobId: String): ResultState<Boolean>

    /**
     * Creates the application, the user/job indexes, and increments the
     * job's application count as one multi-location update.
     */
    suspend fun submitApplication(
        job: JobModel,
        applicantId: String,
        resumeUrl: String,
        coverLetter: String
    ): ResultState<ApplicationModel>

    suspend fun getUserApplications(userId: String): ResultState<List<ApplicationModel>>

    suspend fun getApplicationById(applicationId: String): ResultState<ApplicationModel>

    /** Only the application's own applicant may withdraw it, and only from an eligible status. */
    suspend fun withdrawApplication(userId: String, applicationId: String): ResultState<Unit>
}
