package com.example.localskill.model

data class NotificationModel(
    val id: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedId: String = "",
    val relatedType: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

enum class NotificationType {
    NEW_APPLICATION,
    APPLICATION_ACCEPTED,
    APPLICATION_REJECTED,
    REVIEW_RECEIVED,
    NEARBY_JOB,
    SYSTEM
}
