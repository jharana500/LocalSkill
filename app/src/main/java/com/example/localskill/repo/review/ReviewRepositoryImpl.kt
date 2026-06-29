package com.example.localskill.repo.review

import com.example.localskill.model.ReviewModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class ReviewRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ReviewRepository {
    private val reviewsRef = database.getReference(Constants.REVIEWS)
    private val userReviewsRef = database.getReference(Constants.USER_REVIEWS)
    private val givenReviewsRef = database.getReference(Constants.GIVEN_REVIEWS)
    private val applicationReviewsRef = database.getReference(Constants.APPLICATION_REVIEWS)
    private val usersRef = database.getReference(Constants.USERS)

    override fun addReview(review: ReviewModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val reviewId = review.id.ifBlank { reviewsRef.push().key.orEmpty() }
        if (reviewId.isBlank() || review.applicationId.isBlank() || review.jobId.isBlank() || review.reviewerId.isBlank() || review.receiverId.isBlank()) {
            callback(Resource.Error("Unable to submit review. Please try again."))
            return
        }

        hasUserReviewedApplication(review.applicationId, review.reviewerId) { existing ->
            when (existing) {
                Resource.Loading -> Unit
                is Resource.Error -> callback(Resource.Error(existing.message))
                is Resource.Success -> {
                    if (existing.data) {
                        callback(Resource.Error("You have already reviewed this work."))
                        return@hasUserReviewedApplication
                    }
                    val now = System.currentTimeMillis()
                    val saved = review.copy(id = reviewId, comment = review.comment.trim(), createdAt = now, updatedAt = now)
                    val updates = mapOf<String, Any>(
                        "${Constants.REVIEWS}/$reviewId" to saved,
                        "${Constants.USER_REVIEWS}/${review.receiverId}/$reviewId" to saved,
                        "${Constants.GIVEN_REVIEWS}/${review.reviewerId}/$reviewId" to saved,
                        "${Constants.APPLICATION_REVIEWS}/${review.applicationId}/$reviewId" to saved,
                        "${Constants.JOB_REVIEWS}/${review.jobId}/$reviewId" to saved
                    )
                    reviewsRef.root.updateChildren(updates)
                        .addOnSuccessListener { updateAverage(review.receiverId, callback) }
                        .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
                }
            }
        }
    }

    override fun hasUserReviewedApplication(applicationId: String, reviewerId: String, callback: (Resource<Boolean>) -> Unit) {
        callback(Resource.Loading)
        applicationReviewsRef.child(applicationId).orderByChild("reviewerId").equalTo(reviewerId).get()
            .addOnSuccessListener { callback(Resource.Success(it.exists())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getReviewsForUser(userId: String, callback: (Resource<List<ReviewModel>>) -> Unit) {
        callback(Resource.Loading)
        userReviewsRef.child(userId).get()
            .addOnSuccessListener { callback(Resource.Success(it.toReviewList())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getReviewsByUser(userId: String, callback: (Resource<List<ReviewModel>>) -> Unit) {
        callback(Resource.Loading)
        givenReviewsRef.child(userId).get()
            .addOnSuccessListener { callback(Resource.Success(it.toReviewList())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getReviewsForApplication(applicationId: String, callback: (Resource<List<ReviewModel>>) -> Unit) {
        callback(Resource.Loading)
        applicationReviewsRef.child(applicationId).get()
            .addOnSuccessListener { callback(Resource.Success(it.toReviewList())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun calculateUserAverageRating(userId: String, callback: (Resource<Pair<Double, Int>>) -> Unit) {
        callback(Resource.Loading)
        userReviewsRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.toReviewList()
                val count = reviews.size
                val average = if (count == 0) 0.0 else reviews.map { it.rating }.average()
                callback(Resource.Success(average to count))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    private fun updateAverage(userId: String, callback: (Resource<Unit>) -> Unit) {
        calculateUserAverageRating(userId) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Error -> callback(Resource.Error(result.message))
                is Resource.Success -> {
                    usersRef.child(userId).updateChildren(
                        mapOf(
                            "averageRating" to result.data.first,
                            "totalReviews" to result.data.second,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                        .addOnSuccessListener { callback(Resource.Success(Unit)) }
                        .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
                }
            }
        }
    }
}

private fun DataSnapshot.toReviewList(): List<ReviewModel> =
    children.mapNotNull { it.getValue(ReviewModel::class.java) }
        .sortedByDescending { it.updatedAt.takeIf { updatedAt -> updatedAt > 0L } ?: it.createdAt }
