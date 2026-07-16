package com.example.localskill.repo

import com.example.localskill.model.JobModel
import com.example.localskill.model.SavedJobModel
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

class SavedJobRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : SavedJobRepo {

    private val savedJobsRef: DatabaseReference = database.getReference(Constants.SAVED_JOBS_NODE)
    private val jobsRef: DatabaseReference = database.getReference(Constants.JOBS_NODE)

    override suspend fun saveJob(userId: String, jobId: String): ResultState<Unit> = try {
        val record = SavedJobModel(jobId = jobId, userId = userId, savedAt = System.currentTimeMillis())
        savedJobsRef.child(userId).child(jobId).setValue(record.toMap()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun unsaveJob(userId: String, jobId: String): ResultState<Unit> = try {
        savedJobsRef.child(userId).child(jobId).removeValue().await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getSavedJobIds(userId: String): ResultState<Set<String>> = try {
        val snapshot = savedJobsRef.child(userId).get().await()
        ResultState.Success(snapshot.children.mapNotNull { it.key }.toSet())
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getSavedJobs(userId: String): ResultState<List<JobModel>> = try {
        val snapshot = savedJobsRef.child(userId).get().await()
        val entries = snapshot.children.mapNotNull { it.getValue(SavedJobModel::class.java) }
            .sortedByDescending { it.savedAt }
        val jobs = entries.mapNotNull { entry ->
            jobsRef.child(entry.jobId).get().await().getValue(JobModel::class.java)
        }
        ResultState.Success(jobs)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override fun observeSavedJobIds(userId: String): Flow<Set<String>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull { it.key }.toSet())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        savedJobsRef.child(userId).addValueEventListener(listener)
        awaitClose { savedJobsRef.child(userId).removeEventListener(listener) }
    }
}
