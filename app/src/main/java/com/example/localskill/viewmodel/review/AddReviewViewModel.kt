package com.example.localskill.viewmodel.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.model.ReviewModel
import com.example.localskill.model.UserRole
import com.example.localskill.repo.application.ApplicationRepository
import com.example.localskill.repo.application.ApplicationRepositoryImpl
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.review.ReviewRepository
import com.example.localskill.repo.review.ReviewRepositoryImpl
import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.Resource

data class AddReviewUiState(
    val applicationId: String = "",
    val jobId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverRole: String = "",
    val reviewerName: String = "",
    val reviewerRole: String = "",
    val jobTitle: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val hasReviewed: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AddReviewViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(AddReviewUiState())
        private set

    fun load(applicationId: String, receiverId: String) {
        val reviewerId = authRepository.currentUserId()
        if (reviewerId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }
        applicationRepository.getApplicationById(applicationId) { result ->
            when (result) {
                Resource.Loading -> uiState = uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> loadUsers(result.data, reviewerId, receiverId)
            }
        }
        reviewRepository.hasUserReviewedApplication(applicationId, reviewerId) { result ->
            if (result is Resource.Success) uiState = uiState.copy(hasReviewed = result.data)
            if (result is Resource.Error) uiState = uiState.copy(errorMessage = result.message)
        }
    }

    fun onRatingSelected(value: Int) {
        uiState = uiState.copy(rating = value, errorMessage = null, successMessage = null)
    }

    fun onCommentChange(value: String) {
        uiState = uiState.copy(comment = value, errorMessage = null, successMessage = null)
    }

    fun submit() {
        val reviewerId = authRepository.currentUserId()
        when {
            reviewerId == null -> {
                uiState = uiState.copy(errorMessage = "Session expired. Please login again")
                return
            }
            uiState.hasReviewed -> {
                uiState = uiState.copy(errorMessage = "You have already reviewed this work.")
                return
            }
            uiState.rating !in 1..5 -> {
                uiState = uiState.copy(errorMessage = "Please select a rating.")
                return
            }
            uiState.comment.isBlank() -> {
                uiState = uiState.copy(errorMessage = "Review comment is required.")
                return
            }
        }

        val review = ReviewModel(
            jobId = uiState.jobId,
            applicationId = uiState.applicationId,
            reviewerId = reviewerId,
            reviewerName = uiState.reviewerName,
            receiverId = uiState.receiverId,
            receiverName = uiState.receiverName,
            reviewerRole = uiState.reviewerRole,
            receiverRole = uiState.receiverRole,
            rating = uiState.rating,
            comment = uiState.comment.trim(),
            jobTitle = uiState.jobTitle
        )
        reviewRepository.addReview(review) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isSubmitting = true, errorMessage = null, successMessage = null)
                is Resource.Error -> uiState.copy(isSubmitting = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(
                    isSubmitting = false,
                    hasReviewed = true,
                    successMessage = "Review submitted successfully."
                ).also { createReviewNotification(review) }
            }
        }
    }

    private fun createReviewNotification(review: ReviewModel) {
        notificationRepository.createNotification(
            NotificationModel(
                receiverId = review.receiverId,
                senderId = review.reviewerId,
                senderName = review.reviewerName,
                title = "New Review Received",
                message = "You received a new rating and review.",
                type = NotificationType.REVIEW_RECEIVED.name,
                relatedId = review.applicationId,
                relatedType = "REVIEW"
            )
        ) { }
    }

    private fun loadUsers(application: ApplicationModel, reviewerId: String, receiverId: String) {
        if (application.status != ApplicationStatus.ACCEPTED.name) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Reviews are only available for accepted work.")
            return
        }
        val receiverRole = if (receiverId == application.workerId) UserRole.WORKER.name else UserRole.EMPLOYER.name
        val reviewerRole = if (reviewerId == application.workerId) UserRole.WORKER.name else UserRole.EMPLOYER.name
        uiState = uiState.copy(
            applicationId = application.id,
            jobId = application.jobId,
            receiverId = receiverId,
            receiverRole = receiverRole,
            reviewerRole = reviewerRole,
            jobTitle = application.jobTitle,
            isLoading = false
        )
        userRepository.getUser(receiverId) { result ->
            if (result is Resource.Success) uiState = uiState.copy(receiverName = result.data.fullName)
        }
        userRepository.getUser(reviewerId) { result ->
            if (result is Resource.Success) uiState = uiState.copy(reviewerName = result.data.fullName)
        }
    }
}
