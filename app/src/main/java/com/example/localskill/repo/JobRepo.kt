package com.example.localskill.repo

import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.Flow

/**
 * Read-only from the Job Seeker side: creating, editing, publishing, and
 * moderating jobs belongs to the Company/Admin repositories built in a
 * later phase.
 */
interface JobRepo {

    suspend fun getActiveJobs(): ResultState<List<JobModel>>

    suspend fun getFeaturedJobs(limit: Int = 10): ResultState<List<JobModel>>

    suspend fun getRecentJobs(limit: Int = 20): ResultState<List<JobModel>>

    suspend fun getJobsByCategory(categoryId: String): ResultState<List<JobModel>>

    suspend fun getJobById(jobId: String): ResultState<JobModel>

    suspend fun getCategories(): ResultState<List<JobCategoryModel>>

    /** Live view of all active jobs, for screens that benefit from real-time updates. */
    fun observeActiveJobs(): Flow<List<JobModel>>
}
