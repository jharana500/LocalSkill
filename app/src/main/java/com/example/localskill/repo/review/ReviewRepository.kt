package com.example.localskill.repo.review

import com.example.localskill.model.ReviewModel
import com.example.localskill.utils.Resource

interface ReviewRepository {
    fun addReview(review: ReviewModel, callback: (Resource<Unit>) -> Unit)
    fun hasUserReviewedApplication(applicationId: String, reviewerId: String, callback: (Resource<Boolean>) -> Unit)
    fun getReviewsForUser(userId: String, callback: (Resource<List<ReviewModel>>) -> Unit)
    fun getReviewsByUser(userId: String, callback: (Resource<List<ReviewModel>>) -> Unit)
    fun getReviewsForApplication(applicationId: String, callback: (Resource<List<ReviewModel>>) -> Unit)
    fun calculateUserAverageRating(userId: String, callback: (Resource<Pair<Double, Int>>) -> Unit)
}
