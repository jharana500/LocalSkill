package com.example.localskill.repo

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ApplicantRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val jobSeekerProfileRepo: JobSeekerProfileRepo
) : ApplicantRepo {

    private val applicationsRef: DatabaseReference = database.getReference(Constants.APPLICATIONS_NODE)
    private val companyApplicationsRef: DatabaseReference = database.getReference(Constants.COMPANY_APPLICATIONS_NODE)
    private val jobApplicationsRef: DatabaseReference = database.getReference(Constants.JOB_APPLICATIONS_NODE)
    private val jobsRef: DatabaseReference = database.getReference(Constants.JOBS_NODE)

    override suspend fun getCompanyApplications(companyId: String): ResultState<List<ApplicationModel>> = try {
        val indexSnapshot = companyApplicationsRef.child(companyId).get().await()
        val indexedIds = indexSnapshot.children.mapNotNull { it.key }.toSet()
        val indexed = indexedIds.mapNotNull { id ->
            applicationsRef.child(id).get().await().getValue(ApplicationModel::class.java)
        }

        // Applications submitted before the companyApplications index existed (Phase 3)
        // still carry a companyId field, so a fallback scan recovers them safely.
        val allSnapshot = applicationsRef.get().await()
        val legacy = allSnapshot.children.mapNotNull { it.getValue(ApplicationModel::class.java) }
            .filter { it.companyId == companyId && it.id !in indexedIds }

        ResultState.Success((indexed + legacy).sortedByDescending { it.appliedAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getJobApplicants(companyId: String, jobId: String): ResultState<List<ApplicationModel>> {
        val jobOwnership = verifyJobOwnership(companyId, jobId)
        if (jobOwnership != null) return jobOwnership

        return try {
            val indexSnapshot = jobApplicationsRef.child(jobId).get().await()
            val applicationIds = indexSnapshot.children.mapNotNull { it.key }
            val applications = applicationIds.mapNotNull { id ->
                applicationsRef.child(id).get().await().getValue(ApplicationModel::class.java)
            }
            ResultState.Success(applications.sortedByDescending { it.appliedAt })
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getApplicationDetails(companyId: String, applicationId: String): ResultState<ApplicationModel> {
        val owned = requireOwnedApplication(companyId, applicationId)
        return owned ?: ResultState.Error("This application was not found.")
    }

    override suspend fun getApplicantProfile(userId: String): ResultState<JobSeekerProfileModel> =
        jobSeekerProfileRepo.getProfile(userId)

    override suspend fun updateApplicationStatus(
        companyId: String,
        applicationId: String,
        newStatus: String,
        companyMessage: String
    ): ResultState<Unit> {
        val owned = requireOwnedApplication(companyId, applicationId)
            ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnitError()
        val application = owned.data

        if (!ApplicationModel.isValidCompanyTransition(application.status, newStatus)) {
            return ResultState.Error("This status change is not allowed from ${application.status}.")
        }

        return try {
            val updates = mutableMapOf<String, Any?>(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            if (companyMessage.isNotBlank()) updates["companyMessage"] = companyMessage
            applicationsRef.child(applicationId).updateChildren(updates).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun addCompanyMessage(companyId: String, applicationId: String, message: String): ResultState<Unit> {
        val owned = requireOwnedApplication(companyId, applicationId)
            ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnitError()

        return try {
            applicationsRef.child(applicationId).updateChildren(
                mapOf("companyMessage" to message, "updatedAt" to System.currentTimeMillis())
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun scheduleInterview(
        companyId: String,
        applicationId: String,
        interviewDate: Long,
        interviewLocation: String,
        companyMessage: String
    ): ResultState<Unit> {
        val owned = requireOwnedApplication(companyId, applicationId)
            ?: return ResultState.Error("This application was not found.")
        if (owned !is ResultState.Success) return owned.asUnitError()
        val application = owned.data

        if (interviewDate <= System.currentTimeMillis()) {
            return ResultState.Error("Interview date must be in the future.")
        }
        val isReschedule = application.status == ApplicationStatus.INTERVIEW.name
        if (!isReschedule && !ApplicationModel.isValidCompanyTransition(application.status, ApplicationStatus.INTERVIEW.name)) {
            return ResultState.Error("This application cannot be moved to interview from ${application.status}.")
        }

        return try {
            val updates = mutableMapOf<String, Any?>(
                "status" to ApplicationStatus.INTERVIEW.name,
                "interviewDate" to interviewDate,
                "interviewLocation" to interviewLocation,
                "updatedAt" to System.currentTimeMillis()
            )
            if (companyMessage.isNotBlank()) updates["companyMessage"] = companyMessage
            applicationsRef.child(applicationId).updateChildren(updates).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getApplicantStats(companyId: String): ResultState<CompanyDashboardStatsModel> {
        val applications = getCompanyApplications(companyId)
        if (applications !is ResultState.Success) {
            return ResultState.Error((applications as? ResultState.Error)?.message ?: "Unable to load statistics.")
        }
        val data = applications.data
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

    private suspend fun verifyJobOwnership(companyId: String, jobId: String): ResultState<List<ApplicationModel>>? = try {
        val job = jobsRef.child(jobId).get().await()
        val ownerId = job.child("companyId").getValue(String::class.java)
        when {
            !job.exists() -> ResultState.Error("This job was not found.")
            ownerId != companyId -> ResultState.Error("You do not have permission to view these applicants.")
            else -> null
        }
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    private fun ResultState<ApplicationModel>.asUnitError(): ResultState<Unit> = when (this) {
        is ResultState.Error -> ResultState.Error(message, throwable)
        else -> ResultState.Error("Unable to update this application. Please try again.")
    }

    private suspend fun requireOwnedApplication(companyId: String, applicationId: String): ResultState<ApplicationModel>? = try {
        val snapshot = applicationsRef.child(applicationId).get().await()
        val application = snapshot.getValue(ApplicationModel::class.java)
        when {
            application == null -> null
            application.companyId != companyId -> ResultState.Error("You do not have permission to view this application.")
            else -> ResultState.Success(application)
        }
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }
}
