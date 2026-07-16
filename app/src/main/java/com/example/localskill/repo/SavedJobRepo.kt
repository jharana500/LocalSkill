package com.example.localskill.repo

import com.example.localskill.model.JobModel
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface SavedJobRepo {

    suspend fun saveJob(userId: String, jobId: String): ResultState<Unit>

    suspend fun unsaveJob(userId: String, jobId: String): ResultState<Unit>

    suspend fun getSavedJobIds(userId: String): ResultState<Set<String>>

    /** Resolves each saved ID to its job; jobs that were since removed are silently skipped. */
    suspend fun getSavedJobs(userId: String): ResultState<List<JobModel>>

    /** Live saved-job-ID set so Home/Explore/Details/Saved all stay in sync automatically. */
    fun observeSavedJobIds(userId: String): Flow<Set<String>>
}
