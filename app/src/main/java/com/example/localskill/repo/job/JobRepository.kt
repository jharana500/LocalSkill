package com.example.localskill.repo.job

import com.example.localskill.model.JobModel
import com.example.localskill.utils.Resource

interface JobRepository {
    fun postJob(job: JobModel, callback: (Resource<Unit>) -> Unit)
    fun getOpenJobs(callback: (Resource<List<JobModel>>) -> Unit)
    fun getJobById(jobId: String, callback: (Resource<JobModel>) -> Unit)
    fun getJobsByEmployer(employerId: String, callback: (Resource<List<JobModel>>) -> Unit)
    fun updateJob(job: JobModel, callback: (Resource<Unit>) -> Unit)
    fun deleteJob(jobId: String, employerId: String, callback: (Resource<Unit>) -> Unit)
}
