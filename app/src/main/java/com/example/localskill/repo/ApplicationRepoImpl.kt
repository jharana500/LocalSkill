package com.example.localskill.repo

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationType
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await

class ApplicationRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val notificationRepo: NotificationRepo? = null
) : ApplicationRepo {

    private val applicationsRef = database.getReference(Constants.APPLICATIONS_NODE)
    private val userApplicationsRef = database.getReference(Constants.USER_APPLICATIONS_NODE)
    private val jobsRef = database.getReference(Constants.JOBS_NODE)
    private val companiesRef = database.getReference(Constants.COMPANIES_NODE)

    override suspend fun hasApplied(userId: String, jobId: String): ResultState<Boolean> = try {
        val snapshot = userApplicationsRef.child(userId).child(jobId).get().await()
        ResultState.Success(snapshot.exists())
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun submitApplication(
        job: JobModel,
        applicantId: String,
        resumeUrl: String,
        coverLetter: String
    ): ResultState<ApplicationModel> {
        val alreadyApplied = hasApplied(applicantId, job.id)
        if (alreadyApplied is ResultState.Success && alreadyApplied.data) {
            return ResultState.Error("You have already applied to this job.")
        }

        return try {
            val applicationId = applicationsRef.push().key
                ?: return ResultState.Error("Unable to create application. Please try again.")

            val now = System.currentTimeMillis()
            val application = ApplicationModel(
                id = applicationId,
                jobId = job.id,
                applicantId = applicantId,
                companyId = job.companyId,
                jobTitle = job.title,
                companyName = job.companyName,
                resumeUrl = resumeUrl,
                coverLetter = coverLetter,
                status = ApplicationStatus.APPLIED.name,
                appliedAt = now,
                updatedAt = now
            )

            val updates = mapOf(
                "${Constants.APPLICATIONS_NODE}/$applicationId" to application.toMap(),
                "${Constants.USER_APPLICATIONS_NODE}/$applicantId/${job.id}" to applicationId,
                "${Constants.JOB_APPLICATIONS_NODE}/${job.id}/$applicationId" to true,
                "${Constants.COMPANY_APPLICATIONS_NODE}/${job.companyId}/$applicationId" to true,
                "${Constants.JOBS_NODE}/${job.id}/applicationCount" to ServerValue.increment(1)
            )
            database.reference.updateChildren(updates).await()
            val companyOwnerId =
                companiesRef.child(job.companyId).child("ownerUserId").get().await().getValue(String::class.java)
                    .orEmpty()
            if (companyOwnerId.isNotBlank()) {
                notificationRepo?.createNotificationIfAbsent(
                    recipientId = companyOwnerId,
                    senderId = applicantId,
                    type = NotificationType.NEW_APPLICATION,
                    relatedEntityType = NotificationEntityType.APPLICATION,
                    relatedEntityId = applicationId,
                    secondaryEntityId = job.id,
                    eventKey = "application:$applicationId:new_application"
                )
            }
            notificationRepo?.createNotificationIfAbsent(
                recipientId = applicantId,
                senderId = applicantId,
                type = NotificationType.APPLICATION_SUBMITTED,
                relatedEntityType = NotificationEntityType.APPLICATION,
                relatedEntityId = applicationId,
                secondaryEntityId = job.id,
                eventKey = "application:$applicationId:submitted"
            )
            ResultState.Success(application)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getUserApplications(userId: String): ResultState<List<ApplicationModel>> = try {
        val indexSnapshot = userApplicationsRef.child(userId).get().await()
        val applicationIds = indexSnapshot.children.mapNotNull { it.getValue(String::class.java) }
        val applications = applicationIds.mapNotNull { id ->
            applicationsRef.child(id).get().await().getValue(ApplicationModel::class.java)
        }
        ResultState.Success(applications.sortedByDescending { it.appliedAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getApplicationById(applicationId: String): ResultState<ApplicationModel> = try {
        val snapshot = applicationsRef.child(applicationId).get().await()
        val application = snapshot.getValue(ApplicationModel::class.java)
        if (application != null) ResultState.Success(application) else ResultState.Error("Application not found.")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun withdrawApplication(userId: String, applicationId: String): ResultState<Unit> {
        val current = getApplicationById(applicationId)
        if (current !is ResultState.Success) {
            return ResultState.Error((current as? ResultState.Error)?.message ?: "Application not found.")
        }
        val application = current.data
        if (application.applicantId != userId) {
            return ResultState.Error("You can only withdraw your own applications.")
        }
        if (application.status !in ApplicationModel.WITHDRAWABLE_STATUSES) {
            return ResultState.Error("This application can no longer be withdrawn.")
        }

        return try {
            val now = System.currentTimeMillis()
            applicationsRef.child(applicationId).updateChildren(
                mapOf(
                    "status" to ApplicationStatus.WITHDRAWN.name,
                    "updatedAt" to now
                )
            ).await()
            notificationRepo?.createNotificationIfAbsent(
                recipientId = userId,
                senderId = userId,
                type = NotificationType.APPLICATION_WITHDRAWN_CONFIRMATION,
                relatedEntityType = NotificationEntityType.APPLICATION,
                relatedEntityId = applicationId,
                secondaryEntityId = application.jobId,
                eventKey = "application:$applicationId:withdrawn:$now:applicant"
            )
            val companyOwnerId = companiesRef.child(application.companyId).child("ownerUserId").get().await()
                .getValue(String::class.java).orEmpty()
            if (companyOwnerId.isNotBlank()) {
                notificationRepo?.createNotificationIfAbsent(
                    recipientId = companyOwnerId,
                    senderId = userId,
                    type = NotificationType.APPLICATION_WITHDRAWN,
                    relatedEntityType = NotificationEntityType.APPLICATION,
                    relatedEntityId = applicationId,
                    secondaryEntityId = application.jobId,
                    eventKey = "application:$applicationId:withdrawn:$now:company"
                )
            }
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }
}
