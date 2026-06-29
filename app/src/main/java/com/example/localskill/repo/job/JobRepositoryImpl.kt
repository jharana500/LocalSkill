package com.example.localskill.repo.job

import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.FirebaseDatabase

class JobRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : JobRepository {
    private val jobsRef = database.getReference(Constants.JOBS)
    private val employerJobsRef = database.getReference(Constants.EMPLOYER_JOBS)

    override fun postJob(job: JobModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val jobId = job.id.ifBlank { jobsRef.push().key.orEmpty() }
        if (jobId.isBlank()) {
            callback(Resource.Error("Unable to create job id"))
            return
        }
        if (job.employerId.isBlank()) {
            callback(Resource.Error("Unable to find employer account"))
            return
        }

        val now = System.currentTimeMillis()
        val savedJob = job.copy(
            id = jobId,
            status = JobStatus.OPEN.name,
            createdAt = job.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        val updates = mapOf<String, Any>(
            "${Constants.JOBS}/$jobId" to savedJob,
            "${Constants.EMPLOYER_JOBS}/${job.employerId}/$jobId" to savedJob
        )
        jobsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getOpenJobs(callback: (Resource<List<JobModel>>) -> Unit) {
        callback(Resource.Loading)
        jobsRef.orderByChild("status").equalTo(JobStatus.OPEN.name).get()
            .addOnSuccessListener { snapshot ->
                val jobs = snapshot.children.mapNotNull { it.getValue(JobModel::class.java) }
                    .sortedByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.createdAt }
                callback(Resource.Success(jobs))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getJobById(jobId: String, callback: (Resource<JobModel>) -> Unit) {
        callback(Resource.Loading)
        if (jobId.isBlank()) {
            callback(Resource.Error("Unable to load job"))
            return
        }

        jobsRef.child(jobId).get()
            .addOnSuccessListener { snapshot ->
                val job = snapshot.getValue(JobModel::class.java)
                if (job == null) callback(Resource.Error("Job not found"))
                else callback(Resource.Success(job))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getJobsByEmployer(employerId: String, callback: (Resource<List<JobModel>>) -> Unit) {
        callback(Resource.Loading)
        employerJobsRef.child(employerId).get()
            .addOnSuccessListener { snapshot ->
                val jobs = snapshot.children.mapNotNull { it.getValue(JobModel::class.java) }
                    .sortedByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.createdAt }
                callback(Resource.Success(jobs))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun updateJob(job: JobModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        if (job.id.isBlank() || job.employerId.isBlank()) {
            callback(Resource.Error("Unable to update job"))
            return
        }

        val updatedJob = job.copy(updatedAt = System.currentTimeMillis())
        val updates = mapOf<String, Any>(
            "${Constants.JOBS}/${job.id}" to updatedJob,
            "${Constants.EMPLOYER_JOBS}/${job.employerId}/${job.id}" to updatedJob
        )
        jobsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun deleteJob(jobId: String, employerId: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        if (jobId.isBlank() || employerId.isBlank()) {
            callback(Resource.Error("Unable to delete job"))
            return
        }

        val updates = mapOf<String, Any?>(
            "${Constants.JOBS}/$jobId" to null,
            "${Constants.EMPLOYER_JOBS}/$employerId/$jobId" to null
        )
        jobsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }
}
