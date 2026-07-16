package com.example.localskill.fakes

import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.utils.ResultState

/** Mirrors CompanyJobRepoImpl's ownership and publish-gate rules so ViewModel tests exercise real business rules. */
class FakeCompanyJobRepo(private val companyRepo: CompanyRepo) : CompanyJobRepo {

    val jobs = mutableMapOf<String, JobModel>()
    private var idCounter = 0

    override suspend fun createDraft(job: JobModel): ResultState<JobModel> {
        val id = job.id.ifBlank { "job-${idCounter++}" }
        val draft = job.copy(id = id, status = JobStatus.DRAFT.name)
        jobs[id] = draft
        return ResultState.Success(draft)
    }

    override suspend fun updateJob(companyId: String, job: JobModel): ResultState<Unit> {
        val owned = requireOwned(companyId, job.id) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        if (owned.data.status != JobStatus.DRAFT.name && owned.data.status != JobStatus.ACTIVE.name) {
            return ResultState.Error("Closed or expired jobs can no longer be edited.")
        }
        jobs[job.id] = job.copy(companyId = owned.data.companyId, status = owned.data.status)
        return ResultState.Success(Unit)
    }

    override suspend fun publishJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwned(companyId, jobId) ?: return ResultState.Error("This job was not found.")
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
        if (violations.isNotEmpty()) return ResultState.Error(violations.first())

        jobs[jobId] = job.copy(status = JobStatus.ACTIVE.name)
        return ResultState.Success(Unit)
    }

    override suspend fun closeJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwned(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        if (owned.data.status != JobStatus.ACTIVE.name) return ResultState.Error("Only active jobs can be closed.")
        jobs[jobId] = owned.data.copy(status = JobStatus.CLOSED.name)
        return ResultState.Success(Unit)
    }

    override suspend fun reopenJob(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwned(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val job = owned.data
        if (job.status != JobStatus.CLOSED.name) return ResultState.Error("Only closed jobs can be reopened.")
        if (!JobValidationUtils.isDeadlineValid(job.applicationDeadline)) {
            return ResultState.Error("This job's deadline has passed. Update the deadline before reopening.")
        }
        jobs[jobId] = job.copy(status = JobStatus.ACTIVE.name)
        return ResultState.Success(Unit)
    }

    override suspend fun deleteDraft(companyId: String, jobId: String): ResultState<Unit> {
        val owned = requireOwned(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        if (owned !is ResultState.Success) return owned.asUnit()
        val job = owned.data
        if (job.status != JobStatus.DRAFT.name) {
            return ResultState.Error("Only draft jobs can be deleted. Close published jobs instead.")
        }
        if (job.applicationCount > 0) {
            return ResultState.Error("This job has applications and cannot be deleted.")
        }
        jobs.remove(jobId)
        return ResultState.Success(Unit)
    }

    override suspend fun getCompanyJobs(companyId: String): ResultState<List<JobModel>> =
        ResultState.Success(jobs.values.filter { it.companyId == companyId }.sortedByDescending { it.createdAt })

    override suspend fun getCompanyJobById(companyId: String, jobId: String): ResultState<JobModel> {
        val owned = requireOwned(companyId, jobId) ?: return ResultState.Error("This job was not found.")
        return owned
    }

    private fun requireOwned(companyId: String, jobId: String): ResultState<JobModel>? {
        val job = jobs[jobId] ?: return null
        return if (job.companyId != companyId) {
            ResultState.Error("You do not have permission to manage this job.")
        } else {
            ResultState.Success(job)
        }
    }

    private fun ResultState<JobModel>.asUnit(): ResultState<Unit> = when (this) {
        is ResultState.Error -> ResultState.Error(message, throwable)
        else -> ResultState.Error("Unable to update this job. Please try again.")
    }
}
