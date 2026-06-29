package com.example.localskill.repo.application

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.FirebaseDatabase

class ApplicationRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ApplicationRepository {
    private val applicationsRef = database.getReference(Constants.APPLICATIONS)
    private val workerApplicationsRef = database.getReference(Constants.WORKER_APPLICATIONS)
    private val employerApplicationsRef = database.getReference(Constants.EMPLOYER_APPLICATIONS)
    private val jobApplicationsRef = database.getReference(Constants.JOB_APPLICATIONS)

    override fun applyToJob(application: ApplicationModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val applicationId = application.id.ifBlank { applicationsRef.push().key.orEmpty() }
        if (applicationId.isBlank()) {
            callback(Resource.Error("Unable to create application id"))
            return
        }
        if (application.jobId.isBlank() || application.workerId.isBlank() || application.employerId.isBlank()) {
            callback(Resource.Error("Unable to submit application"))
            return
        }

        val now = System.currentTimeMillis()
        val savedApplication = application.copy(
            id = applicationId,
            message = application.message.trim(),
            status = ApplicationStatus.PENDING.name,
            appliedAt = application.appliedAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        val updates = mapOf<String, Any>(
            "${Constants.APPLICATIONS}/$applicationId" to savedApplication,
            "${Constants.WORKER_APPLICATIONS}/${application.workerId}/$applicationId" to savedApplication,
            "${Constants.EMPLOYER_APPLICATIONS}/${application.employerId}/$applicationId" to savedApplication,
            "${Constants.JOB_APPLICATIONS}/${application.jobId}/$applicationId" to savedApplication
        )
        applicationsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun hasWorkerApplied(jobId: String, workerId: String, callback: (Resource<Boolean>) -> Unit) {
        callback(Resource.Loading)
        if (jobId.isBlank() || workerId.isBlank()) {
            callback(Resource.Error("Unable to check application status"))
            return
        }

        jobApplicationsRef.child(jobId).orderByChild("workerId").equalTo(workerId).get()
            .addOnSuccessListener { snapshot -> callback(Resource.Success(snapshot.exists())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getApplicationById(applicationId: String, callback: (Resource<ApplicationModel>) -> Unit) {
        callback(Resource.Loading)
        applicationsRef.child(applicationId).get()
            .addOnSuccessListener { snapshot ->
                val application = snapshot.getValue(ApplicationModel::class.java)
                if (application == null) callback(Resource.Error("Application not found"))
                else callback(Resource.Success(application))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getApplicationsForWorker(workerId: String, callback: (Resource<List<ApplicationModel>>) -> Unit) {
        callback(Resource.Loading)
        workerApplicationsRef.child(workerId).get()
            .addOnSuccessListener { snapshot ->
                callback(Resource.Success(snapshot.toApplicationList()))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getApplicationsForJob(jobId: String, callback: (Resource<List<ApplicationModel>>) -> Unit) {
        callback(Resource.Loading)
        jobApplicationsRef.child(jobId).get()
            .addOnSuccessListener { snapshot ->
                callback(Resource.Success(snapshot.toApplicationList()))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getApplicationsForEmployer(employerId: String, callback: (Resource<List<ApplicationModel>>) -> Unit) {
        callback(Resource.Loading)
        employerApplicationsRef.child(employerId).get()
            .addOnSuccessListener { snapshot ->
                callback(Resource.Success(snapshot.toApplicationList()))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun updateApplicationStatus(
        application: ApplicationModel,
        newStatus: String,
        callback: (Resource<Unit>) -> Unit
    ) {
        callback(Resource.Loading)
        if (
            application.id.isBlank() ||
            application.workerId.isBlank() ||
            application.employerId.isBlank() ||
            application.jobId.isBlank()
        ) {
            callback(Resource.Error("Unable to update application"))
            return
        }
        if (newStatus !in setOf(ApplicationStatus.PENDING.name, ApplicationStatus.ACCEPTED.name, ApplicationStatus.REJECTED.name)) {
            callback(Resource.Error("Unsupported application status"))
            return
        }

        val now = System.currentTimeMillis()
        val statusUpdates = mapOf<String, Any>(
            "${Constants.APPLICATIONS}/${application.id}/status" to newStatus,
            "${Constants.APPLICATIONS}/${application.id}/updatedAt" to now,
            "${Constants.WORKER_APPLICATIONS}/${application.workerId}/${application.id}/status" to newStatus,
            "${Constants.WORKER_APPLICATIONS}/${application.workerId}/${application.id}/updatedAt" to now,
            "${Constants.EMPLOYER_APPLICATIONS}/${application.employerId}/${application.id}/status" to newStatus,
            "${Constants.EMPLOYER_APPLICATIONS}/${application.employerId}/${application.id}/updatedAt" to now,
            "${Constants.JOB_APPLICATIONS}/${application.jobId}/${application.id}/status" to newStatus,
            "${Constants.JOB_APPLICATIONS}/${application.jobId}/${application.id}/updatedAt" to now
        )
        applicationsRef.root.updateChildren(statusUpdates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }
}

private fun com.google.firebase.database.DataSnapshot.toApplicationList(): List<ApplicationModel> =
    children.mapNotNull { it.getValue(ApplicationModel::class.java) }
        .sortedByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.appliedAt }
