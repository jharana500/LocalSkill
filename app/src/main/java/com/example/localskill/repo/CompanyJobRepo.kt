package com.example.localskill.repo

import com.example.localskill.model.JobModel
import com.example.localskill.utils.ResultState

/**
 * Company-owned job CRUD. Every write validates that the acting company
 * actually owns the job before touching it — a company can never modify
 * another company's job through this repository.
 */
interface CompanyJobRepo {

    suspend fun createDraft(job: JobModel): ResultState<JobModel>

    /** DRAFT jobs may be edited freely; ACTIVE jobs may have their content updated but not their status. */
    suspend fun updateJob(companyId: String, job: JobModel): ResultState<Unit>

    /** Requires a verified company and a complete, valid DRAFT job. */
    suspend fun publishJob(companyId: String, jobId: String): ResultState<Unit>

    suspend fun closeJob(companyId: String, jobId: String): ResultState<Unit>

    /** Only allowed while the job's original deadline is still in the future. */
    suspend fun reopenJob(companyId: String, jobId: String): ResultState<Unit>

    /** Only allowed for DRAFT jobs with no applications. */
    suspend fun deleteDraft(companyId: String, jobId: String): ResultState<Unit>

    suspend fun getCompanyJobs(companyId: String): ResultState<List<JobModel>>

    suspend fun getCompanyJobById(companyId: String, jobId: String): ResultState<JobModel>
}
