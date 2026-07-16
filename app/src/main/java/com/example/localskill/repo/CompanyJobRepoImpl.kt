package com.example.localskill.repo

import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CompanyJobRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val companyRepo: CompanyRepo
) : CompanyJobRepo {

    private val jobsRef: DatabaseReference = database.getReference(Constants.JOBS_NODE)
    private val companyJobsRef: DatabaseReference = database.getReference(Constants.COMPANY_JOBS_NODE)

    override suspend fun createDraft(job: JobModel): ResultState<JobModel> = try {
        val jobId = jobsRef.push().key ?: return ResultState.Error("Unable to create job. Please try again.")
        val now = System.currentTimeMillis()
        val draft = job.copy(id = jobId, status = JobStatus.DRAFT.name, createdAt = now, updatedAt = now)

        val updates = mapOf(
            "${Constants.JOBS_NODE}/$jobId" to draft.asFirebaseMap(),
            "${Constants.COMPANY_JOBS_NODE}/${draft.companyId}/$jobId" to true
        )
        database.reference.updateChildren(updates).await()
        ResultState.Success(draft)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun updateJob(companyId: String, job: JobModel): ResultState<Unit> {
        val owned = requireOwnedJob(companyId, job.id) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val existing = owned.data
        if (existing.status != JobStatus.DRAFT.name && existing.status != JobStatus.ACTIVE.name) {
            return ResultState.Error("Closed or expired jobs can no longer be edited.")
        }

        return try {
            val updated = job.copy(
                id = existing.id,
                companyId = existing.companyId,
                status = existing.status,
                applicationCount = existing.applicationCount,
                moderationStatus = existing.moderationStatus,
                moderationReason = existing.moderationReason,
                createdAt = existing.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            jobsRef.child(existing.id).setValue(updated.asFirebaseMap()).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun publishJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwnedJob(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val job = owned.data
        if (job.status != JobStatus.DRAFT.name) {
            return ResultState.Error("Only draft jobs can be published.")
        }

        val company = companyRepo.getCompany(companyId)
        if (company !is ResultState.Success || !company.data.isVerified) {
            return ResultState.Error("Your company must be verified before publishing jobs.")
        }

        val violations = JobValidationUtils.validatePublishReadiness(
            title = job.title,
            description = job.description,
            categoryId = job.categoryId,
            location = job.location,
            jobType = job.jobType,
            workplaceType = job.workplaceType,
            vacancyCount = job.vacancyCount,
            applicationDeadline = job.applicationDeadline,
            minimumSalary = job.minimumSalary,
            maximumSalary = job.maximumSalary,
            skills = job.skills
        )
        if (violations.isNotEmpty()) {
            return ResultState.Error(violations.first())
        }

        return try {
            jobsRef.child(jobId).updateChildren(
                mapOf("status" to JobStatus.ACTIVE.name, "updatedAt" to System.currentTimeMillis())
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun closeJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwnedJob(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        if (owned.data.status != JobStatus.ACTIVE.name) {
            return ResultState.Error("Only active jobs can be closed.")
        }
        return try {
            jobsRef.child(jobId).updateChildren(
                mapOf("status" to JobStatus.CLOSED.name, "updatedAt" to System.currentTimeMillis())
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun reopenJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwnedJob(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val job = owned.data
        if (job.status != JobStatus.CLOSED.name) {
            return ResultState.Error("Only closed jobs can be reopened.")
        }
        if (!JobValidationUtils.isDeadlineValid(job.applicationDeadline)) {
            return ResultState.Error("This job's deadline has passed. Update the deadline before reopening.")
        }
        return try {
            jobsRef.child(jobId).updateChildren(
                mapOf("status" to JobStatus.ACTIVE.name, "updatedAt" to System.currentTimeMillis())
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deleteDraft(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwnedJob(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val job = owned.data
        if (job.status != JobStatus.DRAFT.name) {
            return ResultState.Error("Only draft jobs can be deleted. Close published jobs instead.")
        }
        if (job.applicationCount > 0) {
            return ResultState.Error("This job has applications and cannot be deleted.")
        }

        return try {
            val updates = mapOf(
                "${Constants.JOBS_NODE}/$jobId" to null,
                "${Constants.COMPANY_JOBS_NODE}/$companyId/$jobId" to null
            )
            database.reference.updateChildren(updates).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getCompanyJobs(companyId: String): ResultState<List<JobModel>> = try {
        val indexSnapshot = companyJobsRef.child(companyId).get().await()
        val jobIds = indexSnapshot.children.mapNotNull { it.key }
        val jobs = jobIds.mapNotNull { id -> jobsRef.child(id).get().await().getValue(JobModel::class.java) }
        ResultState.Success(jobs.sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getCompanyJobById(companyId: String, jobId: String): ResultState<JobModel> {
        val owned = requireOwnedJob(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        return owned
    }

    private suspend fun requireOwnedJob(companyId: String, jobId: String): ResultState<JobModel>? = try {
        val snapshot = jobsRef.child(jobId).get().await()
        val job = snapshot.getValue(JobModel::class.java)
        when {
            job == null -> null
            job.companyId != companyId -> ResultState.Error("You do not have permission to manage this job.")
            else -> ResultState.Success(job)
        }
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    private fun ResultState<JobModel>.asUnit(): ResultState<Unit> = when (this) {
        is ResultState.Error -> ResultState.Error(message, throwable)
        else -> ResultState.Error("Unable to update this job. Please try again.")
    }

    private fun JobModel.asFirebaseMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "companyId" to companyId,
        "companyName" to companyName,
        "companyLogoUrl" to companyLogoUrl,
        "companyVerified" to companyVerified,
        "title" to title,
        "description" to description,
        "responsibilities" to responsibilities,
        "requirements" to requirements,
        "skills" to skills,
        "categoryId" to categoryId,
        "categoryName" to categoryName,
        "location" to location,
        "jobType" to jobType,
        "workplaceType" to workplaceType,
        "minimumSalary" to minimumSalary,
        "maximumSalary" to maximumSalary,
        "currency" to currency,
        "experienceLevel" to experienceLevel,
        "educationRequirement" to educationRequirement,
        "vacancyCount" to vacancyCount,
        "applicationDeadline" to applicationDeadline,
        "status" to status,
        "applicationCount" to applicationCount,
        "featured" to featured,
        "moderationStatus" to moderationStatus,
        "moderationReason" to moderationReason,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
