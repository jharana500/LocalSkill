package com.example.localskill.repo

import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobRepoImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : JobRepo {

    private val jobsRef: DatabaseReference = database.getReference(Constants.JOBS_NODE)
    private val categoriesRef: DatabaseReference = database.getReference(Constants.JOB_CATEGORIES_NODE)

    override suspend fun getActiveJobs(): ResultState<List<JobModel>> = try {
        val snapshot = jobsRef.get().await()
        val jobs = snapshot.children.mapNotNull { it.getValue(JobModel::class.java) }
            .filter { it.status == JobStatus.ACTIVE.name }
        ResultState.Success(jobs)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getFeaturedJobs(limit: Int): ResultState<List<JobModel>> =
        when (val result = getActiveJobs()) {
            is ResultState.Success -> ResultState.Success(
                result.data.filter { it.featured }.sortedByDescending { it.createdAt }.take(limit)
            )

            else -> result
        }

    override suspend fun getRecentJobs(limit: Int): ResultState<List<JobModel>> =
        when (val result = getActiveJobs()) {
            is ResultState.Success -> ResultState.Success(
                result.data.sortedByDescending { it.createdAt }.take(limit)
            )

            else -> result
        }

    override suspend fun getJobsByCategory(categoryId: String): ResultState<List<JobModel>> =
        when (val result = getActiveJobs()) {
            is ResultState.Success -> ResultState.Success(result.data.filter { it.categoryId == categoryId })
            else -> result
        }

    override suspend fun getJobById(jobId: String): ResultState<JobModel> = try {
        val snapshot = jobsRef.child(jobId).get().await()
        val job = snapshot.getValue(JobModel::class.java)
        if (job != null) ResultState.Success(job) else ResultState.Error("This job could not be found.")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getCategories(): ResultState<List<JobCategoryModel>> = try {
        val snapshot = categoriesRef.get().await()
        val categories = snapshot.children.mapNotNull { it.getValue(JobCategoryModel::class.java) }
        ResultState.Success(categories)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override fun observeActiveJobs(): Flow<List<JobModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobs = snapshot.children.mapNotNull { it.getValue(JobModel::class.java) }
                    .filter { it.status == JobStatus.ACTIVE.name }
                trySend(jobs)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        jobsRef.addValueEventListener(listener)
        awaitClose { jobsRef.removeEventListener(listener) }
    }
}
