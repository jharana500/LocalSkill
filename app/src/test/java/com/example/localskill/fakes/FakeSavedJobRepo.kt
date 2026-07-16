package com.example.localskill.fakes

import com.example.localskill.model.JobModel
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSavedJobRepo : SavedJobRepo {

    val savedIdsByUser = mutableMapOf<String, MutableSet<String>>()
    var jobsById: Map<String, JobModel> = emptyMap()
    private val savedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun saveJob(userId: String, jobId: String): ResultState<Unit> {
        savedIdsByUser.getOrPut(userId) { mutableSetOf() }.add(jobId)
        savedIdsFlow.value = savedIdsByUser[userId].orEmpty()
        return ResultState.Success(Unit)
    }

    override suspend fun unsaveJob(userId: String, jobId: String): ResultState<Unit> {
        savedIdsByUser[userId]?.remove(jobId)
        savedIdsFlow.value = savedIdsByUser[userId].orEmpty()
        return ResultState.Success(Unit)
    }

    override suspend fun getSavedJobIds(userId: String): ResultState<Set<String>> =
        ResultState.Success(savedIdsByUser[userId].orEmpty())

    override suspend fun getSavedJobs(userId: String): ResultState<List<JobModel>> =
        ResultState.Success(savedIdsByUser[userId].orEmpty().mapNotNull { jobsById[it] })

    override fun observeSavedJobIds(userId: String) = savedIdsFlow
}
