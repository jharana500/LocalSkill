package com.example.localskill.fakes

import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.repo.JobRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeJobRepo : JobRepo {

    var activeJobs: List<JobModel> = emptyList()
    var categories: List<JobCategoryModel> = emptyList()
    var jobByIdResult: ResultState<JobModel>? = null
    var forceError: String? = null

    private val activeJobsFlow = MutableStateFlow<List<JobModel>>(emptyList())

    override suspend fun getActiveJobs(): ResultState<List<JobModel>> =
        forceError?.let { ResultState.Error(it) } ?: ResultState.Success(activeJobs)

    override suspend fun getFeaturedJobs(limit: Int): ResultState<List<JobModel>> =
        forceError?.let { ResultState.Error(it) }
            ?: ResultState.Success(activeJobs.filter { it.featured }.take(limit))

    override suspend fun getRecentJobs(limit: Int): ResultState<List<JobModel>> =
        forceError?.let { ResultState.Error(it) }
            ?: ResultState.Success(activeJobs.sortedByDescending { it.createdAt }.take(limit))

    override suspend fun getJobsByCategory(categoryId: String): ResultState<List<JobModel>> =
        forceError?.let { ResultState.Error(it) }
            ?: ResultState.Success(activeJobs.filter { it.categoryId == categoryId })

    override suspend fun getJobById(jobId: String): ResultState<JobModel> =
        jobByIdResult ?: activeJobs.find { it.id == jobId }?.let { ResultState.Success(it) }
            ?: ResultState.Error("This job could not be found.")

    override suspend fun getCategories(): ResultState<List<JobCategoryModel>> = ResultState.Success(categories)

    override fun observeActiveJobs() = activeJobsFlow
}
